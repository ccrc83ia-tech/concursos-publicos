package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.Pregunta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PreguntaRepository {

    Pregunta guardar(Pregunta pregunta);

    /**
     * Persiste un lote de preguntas descartando duplicados: las que ya existen en el
     * ítem (mismo {@code hash_contenido}) o que colisionan entre sí dentro del lote se
     * omiten. Devuelve solo las realmente insertadas.
     */
    List<Pregunta> guardarTodas(List<Pregunta> preguntas);

    Optional<Pregunta> buscarPorId(UUID id);

    List<Pregunta> buscarPorItemId(UUID itemId);

    long contarPorItemId(UUID itemId);

    /** Cuenta las preguntas VALIDADAS del ítem (listas para servir en simulacros). */
    long contarValidadasPorItemId(UUID itemId);

    /** Cuenta las preguntas del ítem en un estado de calidad concreto. */
    long contarPorEstado(UUID itemId, Pregunta.EstadoCalidad estado);

    /** Cuenta las preguntas del ítem producidas por un origen de modelo concreto. */
    long contarPorOrigen(UUID itemId, Pregunta.OrigenModelo origen);

    /** Devuelve hasta {@code limite} preguntas aleatorias del ítem (para simulacros). */
    List<Pregunta> buscarAleatoriasPorItemId(UUID itemId, int limite);

    /**
     * Devuelve hasta {@code limite} preguntas VALIDADAS del ítem, en orden aleatorio.
     * Es la fuente preferente para armar simulacros (material con sello de calidad).
     */
    List<Pregunta> buscarAleatoriasValidadasPorItemId(UUID itemId, int limite);

    /**
     * Devuelve hasta {@code limite} preguntas PENDIENTES de validación del ítem, en
     * orden aleatorio. Es el respaldo para simulacros cuando no hay suficientes
     * validadas. Nunca incluye rechazadas ni agotadas.
     */
    List<Pregunta> buscarAleatoriasPendientesPorItemId(UUID itemId, int limite);

    /** Preguntas del ítem pendientes de validación (para el job de mantenimiento). */
    List<Pregunta> buscarPendientesPorItemId(UUID itemId);

    /**
     * Elimina del banco las preguntas descartadas por el validador (RECHAZADA y
     * AGOTADA). La ejecuta el job de limpieza periódico; devuelve cuántas borró.
     */
    int eliminarRechazadasYAgotadas();

    /** Busca una pregunta por su hash de contenido dentro del ítem (deduplicación). */
    Optional<Pregunta> buscarPorHash(UUID itemId, String hashContenido);

    void eliminarPorItemId(UUID itemId);
}
