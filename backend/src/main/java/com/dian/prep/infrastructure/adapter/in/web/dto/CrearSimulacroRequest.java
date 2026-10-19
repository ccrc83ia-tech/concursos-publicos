package com.dian.prep.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CrearSimulacroRequest(
        @NotNull(message = "El concurso es obligatorio")
        UUID concursoId,

        String tipo,                 // POR_ITEM | COMPLETO | BRECHAS (default POR_ITEM)

        List<UUID> itemIds,          // null/vacío = todos los ítems del concurso

        @Min(value = 1, message = "Debe pedir al menos 1 pregunta por ítem")
        int preguntasPorItem,

        Integer tiempoLimiteSegundos // null = sin límite
) {}
