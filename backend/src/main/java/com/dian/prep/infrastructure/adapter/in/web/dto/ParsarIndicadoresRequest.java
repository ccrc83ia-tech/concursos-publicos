package com.dian.prep.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ParsarIndicadoresRequest(
    @NotBlank(message = "El texto de indicadores es obligatorio")
    String textoIndicadores
) {}
