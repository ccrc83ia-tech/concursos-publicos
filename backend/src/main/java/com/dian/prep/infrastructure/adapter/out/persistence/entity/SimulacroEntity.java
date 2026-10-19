package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "simulacros")
public class SimulacroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "concurso_id", nullable = false)
    private UUID concursoId;

    @Column(nullable = false, length = 20)
    private String tipo = "POR_ITEM";

    @Column(nullable = false, length = 20)
    private String estado = "EN_CURSO";

    @Column(name = "tiempo_limite_segundos")
    private Integer tiempoLimiteSegundos;

    @Column(name = "tiempo_usado_segundos")
    private Integer tiempoUsadoSegundos = 0;

    @Column(name = "puntaje_final")
    private Double puntajeFinal;

    @OneToMany(mappedBy = "simulacro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<SimulacroPreguntaEntity> preguntas = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "finalizado_at")
    private LocalDateTime finalizadoAt;

    public UUID getId()                               { return id; }
    public void setId(UUID id)                        { this.id = id; }
    public UUID getUsuarioId()                        { return usuarioId; }
    public void setUsuarioId(UUID usuarioId)          { this.usuarioId = usuarioId; }
    public UUID getConcursoId()                       { return concursoId; }
    public void setConcursoId(UUID concursoId)        { this.concursoId = concursoId; }
    public String getTipo()                           { return tipo; }
    public void setTipo(String tipo)                  { this.tipo = tipo; }
    public String getEstado()                         { return estado; }
    public void setEstado(String estado)              { this.estado = estado; }
    public Integer getTiempoLimiteSegundos()          { return tiempoLimiteSegundos; }
    public void setTiempoLimiteSegundos(Integer t)    { this.tiempoLimiteSegundos = t; }
    public Integer getTiempoUsadoSegundos()           { return tiempoUsadoSegundos; }
    public void setTiempoUsadoSegundos(Integer t)     { this.tiempoUsadoSegundos = t; }
    public Double getPuntajeFinal()                   { return puntajeFinal; }
    public void setPuntajeFinal(Double p)             { this.puntajeFinal = p; }
    public List<SimulacroPreguntaEntity> getPreguntas() { return preguntas; }
    public void setPreguntas(List<SimulacroPreguntaEntity> p) { this.preguntas = p; }
    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime t)         { this.createdAt = t; }
    public LocalDateTime getFinalizadoAt()            { return finalizadoAt; }
    public void setFinalizadoAt(LocalDateTime t)      { this.finalizadoAt = t; }
}
