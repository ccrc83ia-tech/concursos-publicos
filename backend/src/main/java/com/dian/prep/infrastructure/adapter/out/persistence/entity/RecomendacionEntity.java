package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recomendaciones")
@Getter
@Setter
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
}
