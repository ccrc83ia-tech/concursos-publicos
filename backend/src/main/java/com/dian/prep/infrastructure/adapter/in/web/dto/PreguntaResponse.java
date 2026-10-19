package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.model.Pregunta;

import java.util.Map;
import java.util.UUID;

public record PreguntaResponse(
        UUID    id,
        UUID    itemId,
        String  enunciado,
        String  opcionA,
        String  opcionB,
        String  opcionC,
        String  opcionD,
        String  respuestaCorrecta,
        String  justificacionCorrecta,
        Map<String, String> justificacionIncorrectas,
        String  referenciaNormativa,
        String  nivelDificultad,
        String  procesoCognitivo,
        boolean generadaPorIa
) {
    public static PreguntaResponse from(Pregunta p) {
        return new PreguntaResponse(
                p.getId(),
                p.getItemId(),
                p.getEnunciado(),
                p.getOpcionA(),
                p.getOpcionB(),
                p.getOpcionC(),
                p.getOpcionD(),
                p.getRespuestaCorrecta() != null ? p.getRespuestaCorrecta().name() : null,
                p.getJustificacionCorrecta(),
                p.getJustificacionIncorrectas(),
                p.getReferenciaNormativa(),
                p.getNivelDificultad() != null ? p.getNivelDificultad().name() : null,
                p.getProcesoCognitivo() != null ? p.getProcesoCognitivo().name() : null,
                p.isGeneradaPorIa());
    }
}
