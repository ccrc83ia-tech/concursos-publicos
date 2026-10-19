package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.model.DesempenoItem;

import java.util.UUID;

public record DesempenoResponse(
        UUID    itemId,
        UUID    simulacroId,
        int     preguntasCorrectas,
        int     totalPreguntas,
        double  puntajePorcentual,
        String  nivelDominio
) {
    public static DesempenoResponse from(DesempenoItem d) {
        return new DesempenoResponse(
                d.getItemId(),
                d.getSimulacroId(),
                d.getPreguntasCorrectas(),
                d.getTotalPreguntas(),
                d.getPuntajePorcentual(),
                d.getNivelDominio().name());
    }
}
