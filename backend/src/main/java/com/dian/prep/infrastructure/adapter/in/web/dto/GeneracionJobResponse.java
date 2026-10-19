package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.model.GeneracionJob;

import java.util.UUID;

public record GeneracionJobResponse(
        UUID    id,
        UUID    itemId,
        String  estado,
        int     preguntasGeneradas,
        int     totalEsperadas,
        int     porcentaje,
        String  mensajeError
) {
    public static GeneracionJobResponse from(GeneracionJob j) {
        return new GeneracionJobResponse(
                j.getId(),
                j.getItemId(),
                j.getEstado().name(),
                j.getPreguntasGeneradas(),
                j.getTotalEsperadas(),
                j.porcentaje(),
                j.getMensajeError());
    }
}
