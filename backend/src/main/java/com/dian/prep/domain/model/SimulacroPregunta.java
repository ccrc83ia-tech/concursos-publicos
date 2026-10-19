package com.dian.prep.domain.model;

import com.dian.prep.domain.model.Pregunta.OpcionRespuesta;

import java.util.UUID;

/**
 * Pregunta incluida en un simulacro, junto con la respuesta del usuario.
 * Guarda una referencia al ítem para poder calcular desempeño por ítem.
 */
public class SimulacroPregunta {

    private UUID id;
    private UUID simulacroId;
    private UUID preguntaId;
    private UUID itemId;                 // desnormalizado: facilita el cálculo de desempeño
    private OpcionRespuesta respuestaDada;
    private Boolean esCorrecta;
    private int tiempoRespuestaSegundos;
    private boolean marcadaParaRevision;
    private int orden;

    public static SimulacroPregunta crear(UUID preguntaId, UUID itemId, int orden) {
        SimulacroPregunta sp = new SimulacroPregunta();
        sp.preguntaId = preguntaId;
        sp.itemId     = itemId;
        sp.orden      = orden;
        return sp;
    }

    /** True solo si fue respondida correctamente. Null/sin responder => false. */
    public boolean esCorrecta() {
        return Boolean.TRUE.equals(esCorrecta);
    }

    public UUID getId()                           { return id; }
    public void setId(UUID id)                    { this.id = id; }
    public UUID getSimulacroId()                  { return simulacroId; }
    public void setSimulacroId(UUID simulacroId)  { this.simulacroId = simulacroId; }
    public UUID getPreguntaId()                   { return preguntaId; }
    public void setPreguntaId(UUID preguntaId)    { this.preguntaId = preguntaId; }
    public UUID getItemId()                       { return itemId; }
    public void setItemId(UUID itemId)            { this.itemId = itemId; }
    public OpcionRespuesta getRespuestaDada()     { return respuestaDada; }
    public void setRespuestaDada(OpcionRespuesta r) { this.respuestaDada = r; }
    public Boolean getEsCorrecta()                { return esCorrecta; }
    public void setEsCorrecta(Boolean b)          { this.esCorrecta = b; }
    public int getTiempoRespuestaSegundos()       { return tiempoRespuestaSegundos; }
    public void setTiempoRespuestaSegundos(int t) { this.tiempoRespuestaSegundos = t; }
    public boolean isMarcadaParaRevision()        { return marcadaParaRevision; }
    public void setMarcadaParaRevision(boolean m) { this.marcadaParaRevision = m; }
    public int getOrden()                         { return orden; }
    public void setOrden(int orden)               { this.orden = orden; }
}
