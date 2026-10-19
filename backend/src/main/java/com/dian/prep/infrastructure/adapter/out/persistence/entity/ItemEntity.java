package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "items")
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concurso_id", nullable = false)
    private ConcursoEntity concurso;

    @Column(nullable = false, length = 300)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_prueba", nullable = false, length = 20)
    private String tipoPrueba = "FUNCIONAL";

    @Column(name = "nivel_competencia")
    private Integer nivelCompetencia;

    @Column(name = "peso_porcentual")
    private Double pesoPorcentual = 0.0;

    @Column(name = "preguntas_solicitadas", nullable = false)
    private Integer preguntasSolicitadas = 50;

    @Column(name = "dominio_previo", length = 20)
    private String dominioPrevio = "NO_SE";

    @Column(name = "umbral_brecha", nullable = false)
    private Double umbralBrecha = 70.0;

    @Column(nullable = false)
    private Integer orden = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    // Getters y setters
    public UUID getId()                                { return id; }
    public void setId(UUID id)                         { this.id = id; }
    public ConcursoEntity getConcurso()                { return concurso; }
    public void setConcurso(ConcursoEntity concurso)   { this.concurso = concurso; }
    public String getNombre()                          { return nombre; }
    public void setNombre(String nombre)               { this.nombre = nombre; }
    public String getDescripcion()                     { return descripcion; }
    public void setDescripcion(String descripcion)     { this.descripcion = descripcion; }
    public String getTipoPrueba()                      { return tipoPrueba; }
    public void setTipoPrueba(String tipoPrueba)       { this.tipoPrueba = tipoPrueba; }
    public Integer getNivelCompetencia()               { return nivelCompetencia; }
    public void setNivelCompetencia(Integer nivel)     { this.nivelCompetencia = nivel; }
    public Double getPesoPorcentual()                  { return pesoPorcentual; }
    public void setPesoPorcentual(Double peso)         { this.pesoPorcentual = peso; }
    public Integer getPreguntasSolicitadas()           { return preguntasSolicitadas; }
    public void setPreguntasSolicitadas(Integer p)     { this.preguntasSolicitadas = p; }
    public String getDominioPrevio()                   { return dominioPrevio; }
    public void setDominioPrevio(String dominioPrevio) { this.dominioPrevio = dominioPrevio; }
    public Double getUmbralBrecha()                    { return umbralBrecha; }
    public void setUmbralBrecha(Double umbral)         { this.umbralBrecha = umbral; }
    public Integer getOrden()                          { return orden; }
    public void setOrden(Integer orden)                { this.orden = orden; }
    public LocalDateTime getCreatedAt()                { return createdAt; }
    public void setCreatedAt(LocalDateTime t)          { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()                { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)          { this.updatedAt = t; }
}
