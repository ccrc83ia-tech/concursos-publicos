package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.model.Item;
import jakarta.validation.constraints.*;

public record ItemRequest(
    @NotBlank(message = "El nombre del ítem es obligatorio")
    @Size(max = 300)
    String nombre,

    String descripcion,

    Item.TipoPrueba tipoPrueba,

    @Min(1) @Max(4)
    Integer nivelCompetencia,

    @DecimalMin("0.0") @DecimalMax("100.0")
    Double pesoPorcentual,

    // Mínimo 1 para permitir simulacros de prueba sin agotar la cuota del free tier de IA.
    // El tope de 200 evita lotes excesivos en una sola generación.
    @Min(1) @Max(200)
    Integer preguntasSolicitadas,

    Item.DominioPrevio dominioPrevio
) {}
