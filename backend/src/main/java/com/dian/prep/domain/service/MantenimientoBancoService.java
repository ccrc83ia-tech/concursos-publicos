package com.dian.prep.domain.service;

import com.dian.prep.domain.model.Concurso;
import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Lógica del job diario que lleva el banco de preguntas hacia el objetivo de
 * "suficientes preguntas VALIDADAS por ítem", usando el modelo redactor premium y
 * respetando su cupo gratuito diario.
 *
 * <p>Es idempotente por día: si ya corrió hoy, no vuelve a trabajar aunque la
 * aplicación se reinicie. Prioriza los ítems donde el banco está más flojo o donde
 * el usuario tiene brecha. No lleva anotaciones de scheduling; el disparo periódico
 * lo hace un adaptador de infraestructura que invoca {@link #ejecutar(Parametros)}.</p>
 */
@Service
public class MantenimientoBancoService {

    /** Identificador lógico del job para el checkpoint de idempotencia. */
    public static final String JOB_ID = "banco-preguntas";

    private static final Logger log = LoggerFactory.getLogger(MantenimientoBancoService.class);

    private final ItemRepository itemRepository;
    private final ConcursoRepository concursoRepository;
    private final PreguntaRepository preguntaRepository;
    private final DesempenoRepository desempenoRepository;
    private final IaGeneradorPort iaGenerador;
    private final CupoIaRepository cupoIa;
    private final JobMantenimientoRepository jobRepository;

    public MantenimientoBancoService(ItemRepository itemRepository,
                                     ConcursoRepository concursoRepository,
                                     PreguntaRepository preguntaRepository,
                                     DesempenoRepository desempenoRepository,
                                     IaGeneradorPort iaGenerador,
                                     CupoIaRepository cupoIa,
                                     JobMantenimientoRepository jobRepository) {
        this.itemRepository      = itemRepository;
        this.concursoRepository  = concursoRepository;
        this.preguntaRepository  = preguntaRepository;
        this.desempenoRepository = desempenoRepository;
        this.iaGenerador         = iaGenerador;
        this.cupoIa              = cupoIa;
        this.jobRepository       = jobRepository;
    }

    /**
     * Parámetros de la corrida (los aporta la configuración de infraestructura, para
     * que el dominio no dependa de ella).
     *
     * @param modeloPremium            clave de modelo para contabilizar el cupo premium
     * @param limitePremiumDiario      tope de llamadas premium permitidas hoy
     * @param objetivoValidadasPorItem cuántas validadas se quieren por ítem
     * @param maxItemsPorCorrida       cuántos ítems atender como máximo en esta corrida
     * @param tamanoLote               cuántas preguntas pedir por llamada a la IA
     */
    public record Parametros(String modeloPremium,
                             int limitePremiumDiario,
                             int objetivoValidadasPorItem,
                             int maxItemsPorCorrida,
                             int tamanoLote) {}

    /**
     * Limpieza del banco: elimina las preguntas descartadas por el validador
     * (RECHAZADA y AGOTADA), que nunca deben servirse en simulacros. Es independiente
     * del job de mejora y seguro de ejecutar en cualquier momento. Devuelve cuántas
     * eliminó.
     */
    public int limpiarDescartadas() {
        int eliminadas = preguntaRepository.eliminarRechazadasYAgotadas();
        if (eliminadas > 0) {
            log.info("Limpieza del banco: {} preguntas rechazadas/agotadas eliminadas", eliminadas);
        }
        return eliminadas;
    }

    /**
     * Ejecuta una corrida de mantenimiento. Idempotente por día: si ya corrió hoy,
     * no hace nada. Devuelve un resumen legible de lo realizado.
     */
    public String ejecutar(Parametros params) {
        LocalDate hoy = LocalDate.now();
        if (jobRepository.ultimaCorrida(JOB_ID).filter(hoy::equals).isPresent()) {
            log.info("Mantenimiento del banco ya corrió hoy ({}); se omite.", hoy);
            return "Ya ejecutado hoy";
        }

        int itemsAtendidos = 0;
        int preguntasAgregadas = 0;
        List<Item> priorizados = priorizarItems(params.objetivoValidadasPorItem());

        for (Item item : priorizados) {
            if (itemsAtendidos >= params.maxItemsPorCorrida()) break;
            if (!cupoIa.hayCupo(hoy, params.modeloPremium(), params.limitePremiumDiario())) {
                log.info("Cupo premium diario ({}) agotado; se detiene el mantenimiento.",
                        params.modeloPremium());
                break;
            }

            int faltan = params.objetivoValidadasPorItem()
                    - (int) preguntaRepository.contarValidadasPorItemId(item.getId());
            if (faltan <= 0) continue;

            itemsAtendidos++;
            int agregadas = completarItem(item, faltan, params, hoy);
            preguntasAgregadas += agregadas;
        }

        String resumen = "Ítems atendidos: %d, preguntas validadas agregadas: %d"
                .formatted(itemsAtendidos, preguntasAgregadas);
        jobRepository.marcarCorrida(JOB_ID, hoy, resumen);
        log.info("Mantenimiento del banco completado. {}", resumen);
        return resumen;
    }

    /**
     * Genera y persiste preguntas para un ítem hasta cubrir el hueco o agotar el cupo
     * premium del día. Devuelve cuántas preguntas nuevas (ya validadas) se agregaron.
     */
    private int completarItem(Item item, int faltan, Parametros params, LocalDate hoy) {
        var contexto = contextoDe(item);
        int agregadas = 0;
        int iteraciones = 0;
        int maxIteraciones = (faltan / Math.max(1, params.tamanoLote()) + 1) * 2 + 1;

        while (agregadas < faltan && iteraciones < maxIteraciones
                && cupoIa.hayCupo(hoy, params.modeloPremium(), params.limitePremiumDiario())) {
            iteraciones++;
            int pedir = Math.min(params.tamanoLote(), faltan - agregadas);
            List<Pregunta> lote;
            try {
                lote = iaGenerador.generarLote(contexto, pedir);
            } catch (RuntimeException e) {
                log.warn("Fallo generando para ítem {}: {}", item.getId(), e.getMessage());
                break; // fail-open: se reintenta en la próxima corrida
            }
            // Cada llamada al redactor premium consume cupo.
            cupoIa.registrarUso(hoy, params.modeloPremium(), 1);

            if (lote.isEmpty()) continue;
            lote.forEach(p -> p.setItemId(item.getId()));
            // guardarTodas deduplica por hash: solo entran preguntas realmente nuevas.
            List<Pregunta> insertadas = preguntaRepository.guardarTodas(lote);
            long validadasNuevas = insertadas.stream().filter(Pregunta::estaValidada).count();
            agregadas += (int) validadasNuevas;
        }
        return agregadas;
    }

    /**
     * Ordena los ítems por prioridad para el mantenimiento:
     * <ol>
     *   <li>ítems con 0 validadas (banco vacío) van primero;</li>
     *   <li>luego los que tienen brecha registrada (ROJO/AMARILLO) en algún simulacro;</li>
     *   <li>dentro de cada grupo, primero los que menos validadas tienen.</li>
     * </ol>
     * Solo se incluyen ítems que aún no alcanzan el objetivo de validadas.
     */
    private List<Item> priorizarItems(int objetivo) {
        List<Item> todos = itemRepository.buscarTodos();

        Map<UUID, Long> validadasPorItem = new HashMap<>();
        for (Item it : todos) {
            validadasPorItem.put(it.getId(), preguntaRepository.contarValidadasPorItemId(it.getId()));
        }

        // Ítems con brecha: se mira el último desempeño por concurso (todos sus usuarios).
        Set<UUID> conBrecha = itemsConBrecha(todos);

        List<Item> candidatos = new ArrayList<>(todos.stream()
                .filter(it -> validadasPorItem.get(it.getId()) < objetivo)
                .toList());

        candidatos.sort(Comparator
                .comparingInt((Item it) -> validadasPorItem.get(it.getId()) == 0 ? 0 : 1)
                .thenComparingInt(it -> conBrecha.contains(it.getId()) ? 0 : 1)
                .thenComparingLong(it -> validadasPorItem.get(it.getId())));
        return candidatos;
    }

    /** Ítems con brecha (último desempeño NO VERDE) en cualquier usuario. */
    private Set<UUID> itemsConBrecha(List<Item> items) {
        return new HashSet<>(desempenoRepository.buscarItemIdsConBrecha());
    }

    private IaGeneradorPort.ContextoGeneracion contextoDe(Item item) {
        Concurso concurso = concursoRepository.buscarPorId(item.getConcursoId()).orElse(null);
        String nombreConcurso = concurso != null ? concurso.getNombre() : "Concurso de méritos";
        String denominacion   = concurso != null ? concurso.getDenominacion() : null;
        return new IaGeneradorPort.ContextoGeneracion(
                item.getTipoPrueba(),
                item.getNombre(),
                item.getNivelCompetencia(),
                nombreConcurso,
                denominacion,
                item.getDescripcion());
    }
}
