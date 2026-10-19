package com.dian.prep.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Plan de estudio recomendado por IA para un ítem en el que el usuario presenta
 * una brecha, generado a partir del desempeño de un simulacro. Se persiste en la
 * tabla {@code recomendaciones}.
 */
public class Recomendacion {

    private UUID id;
    private UUID usuarioId;
    private UUID itemId;
    private UUID simulacroId;
    private List<TemaFallido> temasFallidos = new ArrayList<>();
    private List<ReferenciaNormativa> referenciasNormativas = new ArrayList<>();
    private String resumenEjecutivo;
    private Double tiempoEstudioHoras;
    private LocalDateTime createdAt;

    /** Tema concreto a reforzar. */
    public record TemaFallido(String tema, String descripcion) {}

    /** Norma de referencia para el estudio del tema. */
    public record ReferenciaNormativa(String norma, String articulo, String url) {}

    public static Recomendacion crear(UUID usuarioId, UUID itemId, UUID simulacroId) {
        Recomendacion r = new Recomendacion();
        r.usuarioId   = usuarioId;
        r.itemId      = itemId;
        r.simulacroId = simulacroId;
        r.createdAt   = LocalDateTime.now();
        return r;
    }

    public UUID getId()                                      { return id; }
    public void setId(UUID id)                               { this.id = id; }
    public UUID getUsuarioId()                               { return usuarioId; }
    public void setUsuarioId(UUID usuarioId)                 { this.usuarioId = usuarioId; }
    public UUID getItemId()                                  { return itemId; }
    public void setItemId(UUID itemId)                       { this.itemId = itemId; }
    public UUID getSimulacroId()                             { return simulacroId; }
    public void setSimulacroId(UUID simulacroId)             { this.simulacroId = simulacroId; }
    public List<TemaFallido> getTemasFallidos()              { return temasFallidos; }
    public void setTemasFallidos(List<TemaFallido> t)        { this.temasFallidos = t != null ? t : new ArrayList<>(); }
    public List<ReferenciaNormativa> getReferenciasNormativas()        { return referenciasNormativas; }
    public void setReferenciasNormativas(List<ReferenciaNormativa> r)  { this.referenciasNormativas = r != null ? r : new ArrayList<>(); }
    public String getResumenEjecutivo()                      { return resumenEjecutivo; }
    public void setResumenEjecutivo(String resumen)          { this.resumenEjecutivo = resumen; }
    public Double getTiempoEstudioHoras()                    { return tiempoEstudioHoras; }
    public void setTiempoEstudioHoras(Double horas)          { this.tiempoEstudioHoras = horas; }
    public LocalDateTime getCreatedAt()                      { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)        { this.createdAt = createdAt; }
}
