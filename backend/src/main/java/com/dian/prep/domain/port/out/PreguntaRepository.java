package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.Pregunta;

import java.util.List;
import java.util.UUID;

public interface PreguntaRepository {

    Pregunta guardar(Pregunta pregunta);

    List<Pregunta> guardarTodas(List<Pregunta> preguntas);

    java.util.Optional<Pregunta> buscarPorId(UUID id);

    List<Pregunta> buscarPorItemId(UUID itemId);

    long contarPorItemId(UUID itemId);

    /** Devuelve hasta {@code limite} preguntas aleatorias del ítem (para simulacros). */
    List<Pregunta> buscarAleatoriasPorItemId(UUID itemId, int limite);

    void eliminarPorItemId(UUID itemId);
}
