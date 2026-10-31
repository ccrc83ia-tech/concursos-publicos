package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.FeedbackPregunta;

import java.util.UUID;

/**
 * Persistencia de los reportes (feedback) de los aspirantes sobre preguntas.
 */
public interface FeedbackPreguntaRepository {

    FeedbackPregunta guardar(FeedbackPregunta feedback);

    /** True si el usuario ya reportó esta pregunta (reporte único por usuario). */
    boolean yaReporto(UUID preguntaId, UUID usuarioId);

    /** Número de reportes (de usuarios distintos) acumulados sobre la pregunta. */
    long contarPorPregunta(UUID preguntaId);
}
