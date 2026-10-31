package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.service.BancoPreguntasService.BancoEstado;

/**
 * Respuesta de observabilidad del banco de preguntas de un ítem: cuántas hay en
 * cada estado de calidad y por origen de modelo. Útil para ver el avance de la
 * validación y cuánta parte del banco aún es provisional.
 */
public record BancoEstadoResponse(
        long total,
        long validadas,
        long pendientes,
        long rechazadas,
        long agotadas,
        long provisionales,
        long premium) {

    public static BancoEstadoResponse from(BancoEstado e) {
        return new BancoEstadoResponse(
                e.total(), e.validadas(), e.pendientes(), e.rechazadas(),
                e.agotadas(), e.provisionales(), e.premium());
    }
}
