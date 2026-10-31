package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.model.FeedbackPregunta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Reporte de un aspirante sobre una pregunta problemática.
 */
public record FeedbackRequest(
        @NotNull(message = "El tipo de reporte es obligatorio")
        FeedbackPregunta.TipoFeedback tipo,

        @Size(max = 500, message = "El comentario no puede superar 500 caracteres")
        String comentario
) {}
