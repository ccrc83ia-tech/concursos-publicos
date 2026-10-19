package com.dian.prep.domain.port.in;

import com.dian.prep.domain.model.GeneracionJob;
import com.dian.prep.domain.model.Pregunta;

import java.util.List;
import java.util.UUID;

public interface GenerarPreguntasUseCase {

    /**
     * Inicia la generación de preguntas para un ítem. Crea (o reutiliza) un job
     * y lanza el proceso en background. Devuelve el job en estado PENDIENTE/EN_PROCESO.
     */
    GeneracionJob iniciarGeneracion(UUID itemId);

    /** Consulta el estado del último job de generación del ítem. */
    GeneracionJob consultarEstado(UUID itemId);

    /** Lista las preguntas ya generadas de un ítem. */
    List<Pregunta> listarPreguntas(UUID itemId);
}
