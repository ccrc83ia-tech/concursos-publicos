package com.dian.prep.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Trabajo asíncrono de generación de preguntas para un ítem.
 * Mantiene el progreso para que el frontend pueda mostrar una barra en vivo.
 */
public class GeneracionJob {

    private UUID id;
    private UUID itemId;
    private EstadoGeneracion estado;
    private int preguntasGeneradas;
    private int totalEsperadas;
    private String mensajeError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum EstadoGeneracion { PENDIENTE, EN_PROCESO, COMPLETADO, ERROR }

    public static GeneracionJob crear(UUID itemId, int totalEsperadas) {
        GeneracionJob j = new GeneracionJob();
        j.itemId             = itemId;
        j.estado             = EstadoGeneracion.PENDIENTE;
        j.preguntasGeneradas = 0;
        j.totalEsperadas     = totalEsperadas;
        j.createdAt          = LocalDateTime.now();
        j.updatedAt          = LocalDateTime.now();
        return j;
    }

    public void marcarEnProceso()        { this.estado = EstadoGeneracion.EN_PROCESO; touch(); }
    public void marcarCompletado()       { this.estado = EstadoGeneracion.COMPLETADO; touch(); }

    public void marcarError(String msg) {
        this.estado = EstadoGeneracion.ERROR;
        this.mensajeError = msg;
        touch();
    }

    public void sumarGeneradas(int cantidad) {
        this.preguntasGeneradas += cantidad;
        touch();
    }

    /** Porcentaje de avance 0..100. */
    public int porcentaje() {
        if (totalEsperadas <= 0) return 0;
        return Math.min(100, (int) Math.round(preguntasGeneradas * 100.0 / totalEsperadas));
    }

    private void touch() { this.updatedAt = LocalDateTime.now(); }

    public UUID getId()                           { return id; }
    public void setId(UUID id)                    { this.id = id; }
    public UUID getItemId()                        { return itemId; }
    public void setItemId(UUID itemId)             { this.itemId = itemId; }
    public EstadoGeneracion getEstado()            { return estado; }
    public void setEstado(EstadoGeneracion e)      { this.estado = e; }
    public int getPreguntasGeneradas()             { return preguntasGeneradas; }
    public void setPreguntasGeneradas(int p)       { this.preguntasGeneradas = p; }
    public int getTotalEsperadas()                 { return totalEsperadas; }
    public void setTotalEsperadas(int t)           { this.totalEsperadas = t; }
    public String getMensajeError()                { return mensajeError; }
    public void setMensajeError(String m)          { this.mensajeError = m; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime t)      { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()            { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)      { this.updatedAt = t; }
}
