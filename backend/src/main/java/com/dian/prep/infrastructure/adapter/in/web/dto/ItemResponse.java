package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.model.Item;

import java.time.LocalDateTime;
import java.util.UUID;

public record ItemResponse(
    UUID                 id,
    UUID                 concursoId,
    String               nombre,
    String               descripcion,
    Item.TipoPrueba      tipoPrueba,
    Integer              nivelCompetencia,
    Double               pesoPorcentual,
    Integer              preguntasSolicitadas,
    Item.DominioPrevio   dominioPrevio,
    Double               umbralBrecha,
    Integer              orden,
    LocalDateTime        createdAt
) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(
            item.getId(),
            item.getConcursoId(),
            item.getNombre(),
            item.getDescripcion(),
            item.getTipoPrueba(),
            item.getNivelCompetencia(),
            item.getPesoPorcentual(),
            item.getPreguntasSolicitadas(),
            item.getDominioPrevio(),
            item.getUmbralBrecha(),
            item.getOrden(),
            item.getCreatedAt()
        );
    }
}
