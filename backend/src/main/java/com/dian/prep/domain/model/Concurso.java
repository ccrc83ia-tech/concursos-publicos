package com.dian.prep.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Concurso {

    private UUID id;
    private UUID usuarioId;
    private String nombre;
    private String procesoSeleccion;
    private String numeroInscripcion;
    private String numeroEmpleo;
    private String nivelCargo;
    private String denominacion;
    private LocalDate fechaExamen;
    private boolean activo;
    private List<Item> items = new ArrayList<>();
    private LocalDateTime createdAt;

    public static Concurso crear(UUID usuarioId, String nombre) {
        Concurso c = new Concurso();
        c.usuarioId = usuarioId;
        c.nombre    = nombre;
        c.activo    = true;
        c.createdAt = LocalDateTime.now();
        return c;
    }

    public long diasRestantes() {
        if (fechaExamen == null) return -1;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaExamen);
    }

    // Getters y setters
    public UUID getId()                               { return id; }
    public void setId(UUID id)                        { this.id = id; }
    public UUID getUsuarioId()                        { return usuarioId; }
    public void setUsuarioId(UUID usuarioId)          { this.usuarioId = usuarioId; }
    public String getNombre()                         { return nombre; }
    public void setNombre(String nombre)              { this.nombre = nombre; }
    public String getProcesoSeleccion()               { return procesoSeleccion; }
    public void setProcesoSeleccion(String p)         { this.procesoSeleccion = p; }
    public String getNumeroInscripcion()              { return numeroInscripcion; }
    public void setNumeroInscripcion(String n)        { this.numeroInscripcion = n; }
    public String getNumeroEmpleo()                   { return numeroEmpleo; }
    public void setNumeroEmpleo(String n)             { this.numeroEmpleo = n; }
    public String getNivelCargo()                     { return nivelCargo; }
    public void setNivelCargo(String n)               { this.nivelCargo = n; }
    public String getDenominacion()                   { return denominacion; }
    public void setDenominacion(String d)             { this.denominacion = d; }
    public LocalDate getFechaExamen()                 { return fechaExamen; }
    public void setFechaExamen(LocalDate f)           { this.fechaExamen = f; }
    public boolean isActivo()                         { return activo; }
    public void setActivo(boolean activo)             { this.activo = activo; }
    public List<Item> getItems()                      { return items; }
    public void setItems(List<Item> items)            { this.items = items; }
    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime t)         { this.createdAt = t; }
}
