package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.model.DesempenoItem;
import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.port.in.GestionarItemsUseCase.ItemConDominio;

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
    // Dominio REAL del tema, derivado del último simulacro:
    //  - dominioPorcentual: % de aciertos del último desempeño (0.0 si nunca se practicó).
    //  - nivelDominio: semáforo ROJO/AMARILLO/VERDE del último desempeño (null si nunca se practicó).
    //  - practicado: true si el usuario ya hizo al menos un simulacro con este tema.
    Double                        dominioPorcentual,
    DesempenoItem.NivelDominio    nivelDominio,
    boolean                       practicado,
    Double               umbralBrecha,
    Integer              orden,
    LocalDateTime        createdAt
) {
    /** Sin desempeño (p. ej. al crear o parsear): dominio 0%, sin practicar. */
    public static ItemResponse from(Item item) {
        return build(item, null);
    }

    /** Con el dominio real adjunto (último desempeño del usuario, o null). */
    public static ItemResponse from(ItemConDominio ic) {
        return build(ic.item(), ic.desempeno());
    }

    private static ItemResponse build(Item item, DesempenoItem d) {
        return new ItemResponse(
            item.getId(),
            item.getConcursoId(),
            item.getNombre(),
            item.getDescripcion(),
            item.getTipoPrueba(),
            item.getNivelCompetencia(),
            item.getPesoPorcentual(),
            item.getPreguntasSolicitadas(),
            d != null ? d.getPuntajePorcentual() : 0.0,
            d != null ? d.getNivelDominio() : null,
            d != null,
            item.getUmbralBrecha(),
            item.getOrden(),
            item.getCreatedAt()
        );
    }
}
