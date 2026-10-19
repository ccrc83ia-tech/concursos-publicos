package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "concursos")
public class ConcursoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(name = "proceso_seleccion", length = 50)
    private String procesoSeleccion;

    @Column(name = "numero_inscripcion", length = 50)
    private String numeroInscripcion;

    @Column(name = "numero_empleo", length = 50)
    private String numeroEmpleo;

    @Column(name = "nivel_cargo", length = 50)
    private String nivelCargo;

    @Column(name = "denominacion", length = 100)
    private String denominacion;

    @Column(name = "fecha_examen")
    private LocalDate fechaExamen;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "concurso", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public UUID getId()                            { return id; }
    public void setId(UUID id)                     { this.id = id; }
    public UUID getUsuarioId()                     { return usuarioId; }
    public void setUsuarioId(UUID usuarioId)       { this.usuarioId = usuarioId; }
    public String getNombre()                      { return nombre; }
    public void setNombre(String nombre)           { this.nombre = nombre; }
    public String getProcesoSeleccion()            { return procesoSeleccion; }
    public void setProcesoSeleccion(String p)      { this.procesoSeleccion = p; }
    public String getNumeroInscripcion()           { return numeroInscripcion; }
    public void setNumeroInscripcion(String n)     { this.numeroInscripcion = n; }
    public String getNumeroEmpleo()                { return numeroEmpleo; }
    public void setNumeroEmpleo(String n)          { this.numeroEmpleo = n; }
    public String getNivelCargo()                  { return nivelCargo; }
    public void setNivelCargo(String n)            { this.nivelCargo = n; }
    public String getDenominacion()                { return denominacion; }
    public void setDenominacion(String d)          { this.denominacion = d; }
    public LocalDate getFechaExamen()              { return fechaExamen; }
    public void setFechaExamen(LocalDate f)        { this.fechaExamen = f; }
    public boolean isActivo()                      { return activo; }
    public void setActivo(boolean activo)          { this.activo = activo; }
    public List<ItemEntity> getItems()             { return items; }
    public void setItems(List<ItemEntity> items)   { this.items = items; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime t)      { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()            { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)      { this.updatedAt = t; }
}
