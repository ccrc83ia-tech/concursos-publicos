package com.dian.prep.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResponderRequest(
        @NotNull(message = "La pregunta es obligatoria")
        UUID simulacroPreguntaId,

        String respuesta,              // "A".."D" o null para dejar en blanco
        boolean marcadaParaRevision,
        int tiempoRespuestaSegundos
) {}
