package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "desempeno_item")
public class DesempenoItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "simulacro_id", nullable = false)
    private UUID simulacroId;

    @Column(name = "preguntas_correctas", nullable = false)
    private int preguntasCorrectas = 0;

    @Column(name = "total_preguntas", nullable = false)
    private int totalPreguntas = 0;

    @Column(name = "puntaje_porcentual", nullable = false)
    private double puntajePorcentual = 0;

    @Column(name = "nivel_dominio", nullable = false, length = 20)
    private String nivelDominio = "ROJO";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId()                        { return id; }
    public void setId(UUID id)                 { this.id = id; }
    public UUID getUsuarioId()                 { return usuarioId; }
    public void setUsuarioId(UUID u)           { this.usuarioId = u; }
    public UUID getItemId()                    { return itemId; }
    public void setItemId(UUID itemId)         { this.itemId = itemId; }
    public UUID getSimulacroId()               { return simulacroId; }
    public void setSimulacroId(UUID s)         { this.simulacroId = s; }
    public int getPreguntasCorrectas()         { return preguntasCorrectas; }
    public void setPreguntasCorrectas(int c)   { this.preguntasCorrectas = c; }
    public int getTotalPreguntas()             { return totalPreguntas; }
    public void setTotalPreguntas(int t)       { this.totalPreguntas = t; }
    public double getPuntajePorcentual()       { return puntajePorcentual; }
    public void setPuntajePorcentual(double p) { this.puntajePorcentual = p; }
    public String getNivelDominio()            { return nivelDominio; }
    public void setNivelDominio(String n)      { this.nivelDominio = n; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime t)  { this.createdAt = t; }
}
