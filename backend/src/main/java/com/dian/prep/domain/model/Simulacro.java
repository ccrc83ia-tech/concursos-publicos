package com.dian.prep.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Simulacro de examen: conjunto de preguntas seleccionadas de uno o varios
 * ítems de un concurso, con cronómetro opcional y puntaje final.
 */
public class Simulacro {

    private UUID id;
    private UUID usuarioId;
    private UUID concursoId;
    private TipoSimulacro tipo;
    private EstadoSimulacro estado;
    private Integer tiempoLimiteSegundos;   // null = sin límite
    private Integer tiempoUsadoSegundos;
    private Double puntajeFinal;            // 0..100
    private List<SimulacroPregunta> preguntas = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime finalizadoAt;

    public enum TipoSimulacro { POR_ITEM, COMPLETO, BRECHAS }

    public enum EstadoSimulacro { EN_CURSO, COMPLETADO, ABANDONADO }

    public static Simulacro crear(UUID usuarioId, UUID concursoId, TipoSimulacro tipo,
                                  Integer tiempoLimiteSegundos) {
        Simulacro s = new Simulacro();
        s.usuarioId            = usuarioId;
        s.concursoId           = concursoId;
        s.tipo                 = tipo != null ? tipo : TipoSimulacro.POR_ITEM;
        s.estado               = EstadoSimulacro.EN_CURSO;
        s.tiempoLimiteSegundos = tiempoLimiteSegundos;
        s.tiempoUsadoSegundos  = 0;
        s.createdAt            = LocalDateTime.now();
        return s;
    }

    public void finalizar(int tiempoUsadoSegundos) {
        this.tiempoUsadoSegundos = tiempoUsadoSegundos;
        this.estado = EstadoSimulacro.COMPLETADO;
        this.finalizadoAt = LocalDateTime.now();
        this.puntajeFinal = calcularPuntaje();
    }

    /** Puntaje global = % de preguntas respondidas correctamente. */
    public double calcularPuntaje() {
        if (preguntas.isEmpty()) return 0.0;
        long correctas = preguntas.stream().filter(SimulacroPregunta::esCorrecta).count();
        return Math.round((correctas * 100.0 / preguntas.size()) * 100.0) / 100.0;
    }

    public UUID getId()                               { return id; }
    public void setId(UUID id)                        { this.id = id; }
    public UUID getUsuarioId()                        { return usuarioId; }
    public void setUsuarioId(UUID usuarioId)          { this.usuarioId = usuarioId; }
    public UUID getConcursoId()                       { return concursoId; }
    public void setConcursoId(UUID concursoId)        { this.concursoId = concursoId; }
    public TipoSimulacro getTipo()                    { return tipo; }
    public void setTipo(TipoSimulacro tipo)           { this.tipo = tipo; }
    public EstadoSimulacro getEstado()                { return estado; }
    public void setEstado(EstadoSimulacro estado)     { this.estado = estado; }
    public Integer getTiempoLimiteSegundos()          { return tiempoLimiteSegundos; }
    public void setTiempoLimiteSegundos(Integer t)    { this.tiempoLimiteSegundos = t; }
    public Integer getTiempoUsadoSegundos()           { return tiempoUsadoSegundos; }
    public void setTiempoUsadoSegundos(Integer t)     { this.tiempoUsadoSegundos = t; }
    public Double getPuntajeFinal()                   { return puntajeFinal; }
    public void setPuntajeFinal(Double p)             { this.puntajeFinal = p; }
    public List<SimulacroPregunta> getPreguntas()     { return preguntas; }
    public void setPreguntas(List<SimulacroPregunta> p) { this.preguntas = p; }
    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime t)         { this.createdAt = t; }
    public LocalDateTime getFinalizadoAt()            { return finalizadoAt; }
    public void setFinalizadoAt(LocalDateTime t)      { this.finalizadoAt = t; }
}
