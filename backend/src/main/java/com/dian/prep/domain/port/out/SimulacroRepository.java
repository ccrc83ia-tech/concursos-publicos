package com.dian.prep.domain.port.out;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.dian.prep.domain.model.Simulacro;
import com.dian.prep.domain.model.SimulacroPregunta;

public interface SimulacroRepository {

    /** Guarda el simulacro junto con sus preguntas. */
    Simulacro guardar(Simulacro simulacro);

    Optional<Simulacro> buscarPorId(UUID id);

    /**
     * IDs de preguntas que el usuario ya respondió correctamente en simulacros
     * previos. Sirve para despriorizarlas al armar un nuevo simulacro.
     */
    Set<UUID> buscarPreguntaIdsAcertadas(UUID usuarioId);

    /** Carga una pregunta de simulacro por su id (para registrar la respuesta). */
    Optional<SimulacroPregunta> buscarPreguntaPorId(UUID simulacroPreguntaId);

    SimulacroPregunta guardarPregunta(SimulacroPregunta pregunta);
}
