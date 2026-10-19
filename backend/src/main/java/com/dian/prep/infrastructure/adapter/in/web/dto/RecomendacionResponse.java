package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.model.Recomendacion;

import java.util.List;
import java.util.UUID;

public record RecomendacionResponse(
        UUID                       id,
        UUID                       itemId,
        UUID                       simulacroId,
        String                     resumenEjecutivo,
        List<TemaFallidoDto>       temasFallidos,
        List<ReferenciaDto>        referenciasNormativas,
        Double                     tiempoEstudioHoras
) {
    public record TemaFallidoDto(String tema, String descripcion) {}

    public record ReferenciaDto(String norma, String articulo, String url) {}

    public static RecomendacionResponse from(Recomendacion r) {
        List<TemaFallidoDto> temas = r.getTemasFallidos().stream()
                .map(t -> new TemaFallidoDto(t.tema(), t.descripcion()))
                .toList();
        List<ReferenciaDto> refs = r.getReferenciasNormativas().stream()
                .map(ref -> new ReferenciaDto(ref.norma(), ref.articulo(), ref.url()))
                .toList();
        return new RecomendacionResponse(
                r.getId(), r.getItemId(), r.getSimulacroId(),
                r.getResumenEjecutivo(), temas, refs, r.getTiempoEstudioHoras());
    }
}
