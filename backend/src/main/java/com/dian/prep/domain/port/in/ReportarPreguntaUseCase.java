package com.dian.prep.domain.port.in;

import com.dian.prep.domain.model.FeedbackPregunta;

import java.util.UUID;

/**
 * Caso de uso para que un aspirante reporte una pregunta como problemática durante
 * o tras un simulacro. Acumulados suficientes reportes, la pregunta se degrada y
 * deja de servirse.
 */
public interface ReportarPreguntaUseCase {

    /**
     * Registra el reporte del usuario sobre una pregunta de un simulacro suyo.
     *
     * @param simulacroId simulacro desde el que se reporta (valida la propiedad)
     * @param usuarioId   usuario autenticado que reporta
     * @param command     datos del reporte
     * @return resultado con el estado tras el reporte
     */
    ResultadoReporte reportar(UUID simulacroId, UUID usuarioId, FeedbackCommand command);

    record FeedbackCommand(
            UUID preguntaId,
            FeedbackPregunta.TipoFeedback tipo,
            String comentario
    ) {}

    /**
     * Resultado del reporte.
     *
     * @param reportesAcumulados cuántos reportes distintos tiene ya la pregunta
     * @param preguntaRetirada   true si el reporte provocó que la pregunta se retirara del banco
     */
    record ResultadoReporte(long reportesAcumulados, boolean preguntaRetirada) {}
}
