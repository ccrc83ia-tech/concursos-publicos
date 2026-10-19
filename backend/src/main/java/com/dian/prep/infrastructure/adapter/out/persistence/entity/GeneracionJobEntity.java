package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "generacion_jobs")
public class GeneracionJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "preguntas_generadas", nullable = false)
    private int preguntasGeneradas = 0;

    @Column(name = "total_esperadas", nullable = false)
    private int totalEsperadas = 0;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public UUID getId()                       { return id; }
    public void setId(UUID id)                { this.id = id; }
    public UUID getItemId()                   { return itemId; }
    public void setItemId(UUID itemId)        { this.itemId = itemId; }
    public String getEstado()                 { return estado; }
    public void setEstado(String estado)      { this.estado = estado; }
    public int getPreguntasGeneradas()        { return preguntasGeneradas; }
    public void setPreguntasGeneradas(int p)  { this.preguntasGeneradas = p; }
    public int getTotalEsperadas()            { return totalEsperadas; }
    public void setTotalEsperadas(int t)      { this.totalEsperadas = t; }
    public String getMensajeError()           { return mensajeError; }
    public void setMensajeError(String m)     { this.mensajeError = m; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()       { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }
}
