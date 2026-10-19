package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.Simulacro;
import com.dian.prep.domain.model.SimulacroPregunta;

import java.util.Optional;
import java.util.UUID;

public interface SimulacroRepository {

    /** Guarda el simulacro junto con sus preguntas. */
    Simulacro guardar(Simulacro simulacro);

    Optional<Simulacro> buscarPorId(UUID id);

    /** Carga una pregunta de simulacro por su id (para registrar la respuesta). */
    Optional<SimulacroPregunta> buscarPreguntaPorId(UUID simulacroPreguntaId);

    SimulacroPregunta guardarPregunta(SimulacroPregunta pregunta);
}
