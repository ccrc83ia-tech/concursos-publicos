package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recomendaciones")
public class RecomendacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "simulacro_id", nullable = false)
    private UUID simulacroId;

    @Column(name = "temas_fallidos", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String temasFallidos; // JSON serializado [{tema, descripcion}]

    @Column(name = "referencias_normativas", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String referenciasNormativas; // JSON serializado [{norma, articulo, url}]

    @Column(name = "resumen_ejecutivo", columnDefinition = "TEXT")
    private String resumenEjecutivo;

    @Column(name = "tiempo_estudio_horas")
    private Double tiempoEstudioHoras;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId()                            { return id; }
    public void setId(UUID id)                     { this.id = id; }
    public UUID getUsuarioId()                      { return usuarioId; }
    public void setUsuarioId(UUID u)                { this.usuarioId = u; }
    public UUID getItemId()                         { return itemId; }
    public void setItemId(UUID itemId)              { this.itemId = itemId; }
    public UUID getSimulacroId()                    { return simulacroId; }
    public void setSimulacroId(UUID s)              { this.simulacroId = s; }
    public String getTemasFallidos()                { return temasFallidos; }
    public void setTemasFallidos(String t)          { this.temasFallidos = t; }
    public String getReferenciasNormativas()        { return referenciasNormativas; }
    public void setReferenciasNormativas(String r)  { this.referenciasNormativas = r; }
    public String getResumenEjecutivo()             { return resumenEjecutivo; }
    public void setResumenEjecutivo(String r)       { this.resumenEjecutivo = r; }
    public Double getTiempoEstudioHoras()           { return tiempoEstudioHoras; }
    public void setTiempoEstudioHoras(Double h)     { this.tiempoEstudioHoras = h; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime t)       { this.createdAt = t; }
}
