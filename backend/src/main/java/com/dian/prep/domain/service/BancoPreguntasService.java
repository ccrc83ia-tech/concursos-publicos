package com.dian.prep.domain.service;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.PreguntaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Sirve preguntas del banco aplicando la "regla de oro" de calidad: se entregan
 * preferentemente las VALIDADAS (revisadas por el segundo modelo) y, solo si no
 * alcanzan, se completa con las PENDIENTES de validación. Las preguntas que el
 * validador descartó (RECHAZADA / AGOTADA) NUNCA se sirven en un simulacro; un job
 * de limpieza las elimina del banco aparte. Evita duplicados dentro de la tanda.
 *
 * <p>Pertenece al dominio: coordina lecturas del {@link PreguntaRepository} sin
 * conocer detalles de persistencia ni de IA.</p>
 */
@Service
public class BancoPreguntasService {

    private final PreguntaRepository preguntaRepository;

    public BancoPreguntasService(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }

    /**
     * Devuelve hasta {@code cantidad} preguntas del ítem para un simulacro.
     * Primero toma validadas; si faltan, completa con pendientes (sin repetir).
     * Nunca devuelve rechazadas ni agotadas.
     *
     * @param itemId   ítem del que extraer preguntas
     * @param cantidad número máximo de preguntas a devolver
     * @return lista con a lo sumo {@code cantidad} preguntas, priorizando las validadas
     */
    public List<Pregunta> servirParaSimulacro(UUID itemId, int cantidad) {
        if (cantidad <= 0) return List.of();

        List<Pregunta> seleccion = new ArrayList<>(cantidad);
        Set<UUID> yaIncluidas = new HashSet<>();

        // 1) Preferir validadas.
        for (Pregunta p : preguntaRepository.buscarAleatoriasValidadasPorItemId(itemId, cantidad)) {
            if (yaIncluidas.add(p.getId())) seleccion.add(p);
        }

        // 2) Completar con PENDIENTES de validación si faltan. Nunca se usan preguntas
        //    RECHAZADAS ni AGOTADAS: el validador ya las descartó por baja calidad.
        int faltan = cantidad - seleccion.size();
        if (faltan > 0) {
            for (Pregunta p : preguntaRepository.buscarAleatoriasPendientesPorItemId(itemId, faltan)) {
                if (seleccion.size() >= cantidad) break;
                if (yaIncluidas.add(p.getId())) seleccion.add(p);
            }
        }

        return seleccion;
    }

    /** Cuántas preguntas VALIDADAS tiene el ítem (para decidir si hace falta generar más). */
    public long contarValidadas(UUID itemId) {
        return preguntaRepository.contarValidadasPorItemId(itemId);
    }

    /** Foto del banco de un ítem: conteos por estado de calidad y por origen de modelo. */
    public BancoEstado estadoBanco(UUID itemId) {
        return new BancoEstado(
                preguntaRepository.contarPorItemId(itemId),
                preguntaRepository.contarPorEstado(itemId, Pregunta.EstadoCalidad.VALIDADA),
                preguntaRepository.contarPorEstado(itemId, Pregunta.EstadoCalidad.PENDIENTE_VALIDACION),
                preguntaRepository.contarPorEstado(itemId, Pregunta.EstadoCalidad.RECHAZADA),
                preguntaRepository.contarPorEstado(itemId, Pregunta.EstadoCalidad.AGOTADA),
                preguntaRepository.contarPorOrigen(itemId, Pregunta.OrigenModelo.PROVISIONAL),
                preguntaRepository.contarPorOrigen(itemId, Pregunta.OrigenModelo.PREMIUM));
    }

    /** Resumen de observabilidad del banco de un ítem. */
    public record BancoEstado(
            long total,
            long validadas,
            long pendientes,
            long rechazadas,
            long agotadas,
            long provisionales,
            long premium) {}
}
