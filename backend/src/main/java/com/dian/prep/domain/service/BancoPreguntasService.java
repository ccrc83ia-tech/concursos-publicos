package com.dian.prep.domain.service;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.PreguntaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
     * Devuelve preguntas del ítem para un simulacro, AGRUPADAS POR ENUNCIADO: cada
     * enunciado (texto de lectura) se incluye con todas sus preguntas consecutivas,
     * de modo que el aspirante lea el texto una vez y responda las 3–4 preguntas que
     * se desprenden de él. Se toman enunciados completos hasta cubrir {@code cantidad}
     * preguntas (puede excederla un poco al cerrar el último enunciado, para no partir
     * un grupo). Prioriza validadas; completa con pendientes; nunca sirve
     * rechazadas ni agotadas.
     *
     * @param itemId   ítem del que extraer preguntas
     * @param cantidad número objetivo de preguntas (se respeta el agrupamiento por enunciado)
     * @return preguntas ordenadas por enunciado (las de un mismo enunciado, juntas)
     */
    public List<Pregunta> servirParaSimulacro(UUID itemId, int cantidad) {
        if (cantidad <= 0) return List.of();

        // Se pide un conjunto amplio de candidatas (validadas primero, luego pendientes)
        // y se agrupan por enunciado. Pedimos más del cupo para tener enunciados completos.
        int holgura = cantidad * 4;
        List<Pregunta> candidatas = new ArrayList<>();
        Set<UUID> vistas = new HashSet<>();
        for (Pregunta p : preguntaRepository.buscarAleatoriasValidadasPorItemId(itemId, holgura)) {
            if (vistas.add(p.getId())) candidatas.add(p);
        }
        for (Pregunta p : preguntaRepository.buscarAleatoriasPendientesPorItemId(itemId, holgura)) {
            if (vistas.add(p.getId())) candidatas.add(p);
        }
        if (candidatas.isEmpty()) return List.of();

        // Agrupar por enunciado (el texto de lectura antes del marcador "PREGUNTA").
        Map<String, List<Pregunta>> porEnunciado = new LinkedHashMap<>();
        for (Pregunta p : candidatas) {
            porEnunciado.computeIfAbsent(claveEnunciado(p.getEnunciado()), k -> new ArrayList<>()).add(p);
        }

        // Tomar enunciados completos (en el orden aleatorio en que llegaron) hasta
        // alcanzar el cupo de preguntas; no se parte un enunciado por la mitad.
        List<Pregunta> seleccion = new ArrayList<>();
        for (List<Pregunta> grupo : porEnunciado.values()) {
            if (seleccion.size() >= cantidad) break;
            seleccion.addAll(grupo);
        }
        return seleccion;
    }

    /** Clave de agrupación: el texto del enunciado (lectura), sin la parte de la pregunta. */
    private String claveEnunciado(String enunciado) {
        if (enunciado == null) return "";
        int i = enunciado.indexOf("PREGUNTA");
        return (i >= 0 ? enunciado.substring(0, i) : enunciado).trim();
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
