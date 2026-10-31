package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.port.in.ReportarPreguntaUseCase.ResultadoReporte;

/**
 * Resultado de reportar una pregunta: cuántos reportes acumula y si fue retirada.
 */
public record FeedbackResponse(
        long reportesAcumulados,
        boolean preguntaRetirada
) {
    public static FeedbackResponse from(ResultadoReporte r) {
        return new FeedbackResponse(r.reportesAcumulados(), r.preguntaRetirada());
    }
}
