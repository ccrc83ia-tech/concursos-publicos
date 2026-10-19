package com.dian.prep.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Desempeño del usuario en un ítem tras un simulacro. Base para el dashboard
 * de brechas (semáforo de dominio).
 */
public class DesempenoItem {

    private UUID id;
    private UUID usuarioId;
    private UUID itemId;
    private UUID simulacroId;
    private int preguntasCorrectas;
    private int totalPreguntas;
    private double puntajePorcentual;
    private NivelDominio nivelDominio;
    private LocalDateTime createdAt;

    public enum NivelDominio { ROJO, AMARILLO, VERDE }

    /**
     * Construye el desempeño calculando porcentaje y nivel de dominio.
     *
     * @param umbral umbral de aprobación del ítem (%). El semáforo es:
     *               ROJO &lt; 50, AMARILLO entre 50 y el umbral, VERDE &gt;= umbral.
     */
    public static DesempenoItem calcular(UUID usuarioId, UUID itemId, UUID simulacroId,
                                         int correctas, int total, double umbral) {
        DesempenoItem d = new DesempenoItem();
        d.usuarioId          = usuarioId;
        d.itemId             = itemId;
        d.simulacroId        = simulacroId;
        d.preguntasCorrectas = correctas;
        d.totalPreguntas     = total;
        d.puntajePorcentual  = total > 0
                ? Math.round((correctas * 100.0 / total) * 100.0) / 100.0
                : 0.0;
        d.nivelDominio       = clasificarNivel(d.puntajePorcentual, umbral);
        d.createdAt          = LocalDateTime.now();
        return d;
    }

    public static NivelDominio clasificarNivel(double porcentaje, double umbral) {
        if (porcentaje < 50.0)      return NivelDominio.ROJO;
        if (porcentaje < umbral)    return NivelDominio.AMARILLO;
        return NivelDominio.VERDE;
    }

    public UUID getId()                           { return id; }
    public void setId(UUID id)                    { this.id = id; }
    public UUID getUsuarioId()                    { return usuarioId; }
    public void setUsuarioId(UUID usuarioId)      { this.usuarioId = usuarioId; }
    public UUID getItemId()                       { return itemId; }
    public void setItemId(UUID itemId)            { this.itemId = itemId; }
    public UUID getSimulacroId()                  { return simulacroId; }
    public void setSimulacroId(UUID simulacroId)  { this.simulacroId = simulacroId; }
    public int getPreguntasCorrectas()            { return preguntasCorrectas; }
    public void setPreguntasCorrectas(int c)      { this.preguntasCorrectas = c; }
    public int getTotalPreguntas()                { return totalPreguntas; }
    public void setTotalPreguntas(int t)          { this.totalPreguntas = t; }
    public double getPuntajePorcentual()          { return puntajePorcentual; }
    public void setPuntajePorcentual(double p)    { this.puntajePorcentual = p; }
    public NivelDominio getNivelDominio()         { return nivelDominio; }
    public void setNivelDominio(NivelDominio n)   { this.nivelDominio = n; }
    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime t)     { this.createdAt = t; }
}
