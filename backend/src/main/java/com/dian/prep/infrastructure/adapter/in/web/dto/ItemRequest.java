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

    @Min(20) @Max(200)
    Integer preguntasSolicitadas,

    Item.DominioPrevio dominioPrevio
) {}
