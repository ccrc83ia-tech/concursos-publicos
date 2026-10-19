package com.dian.prep.domain.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Pregunta de selección múltiple (única respuesta, 4 opciones) asociada a un ítem.
 * Puede provenir de IA o cargarse manualmente.
 */
public class Pregunta {

    private UUID id;
    private UUID itemId;
    private String enunciado;
    private String opcionA;
    private String opcionB;
    private String opcionC;
    private String opcionD;
    private OpcionRespuesta respuestaCorrecta;
    private String justificacionCorrecta;
    /** Clave = opción (A/B/C/D), valor = por qué es incorrecta. */
    private Map<String, String> justificacionIncorrectas;
    private String referenciaNormativa;
    private NivelDificultad nivelDificultad;
    /** Proceso cognitivo evaluado, según la metodología de ítems de la CNSC. */
    private ProcesoCognitivo procesoCognitivo;
    private boolean generadaPorIa;
    private LocalDateTime createdAt;

    public enum OpcionRespuesta { A, B, C, D }

    public enum NivelDificultad { BASICO, INTERMEDIO, AVANZADO }

    /**
     * Proceso cognitivo que mide el ítem, en la taxonomía usada por la CNSC
     * para el diseño de pruebas escritas de concursos de méritos:
     * <ul>
     *   <li>EVOCACION: recordar un concepto, norma o dato.</li>
     *   <li>COMPRENSION: interpretar o explicar el sentido de algo.</li>
     *   <li>APLICACION: usar el conocimiento para resolver una situación concreta.</li>
     * </ul>
     */
    public enum ProcesoCognitivo { EVOCACION, COMPRENSION, APLICACION }

    public static Pregunta crear(UUID itemId, String enunciado) {
        Pregunta p = new Pregunta();
        p.itemId           = itemId;
        p.enunciado        = enunciado;
        p.nivelDificultad  = NivelDificultad.INTERMEDIO;
        p.procesoCognitivo = ProcesoCognitivo.APLICACION;
        p.generadaPorIa    = true;
        p.createdAt        = LocalDateTime.now();
        return p;
    }

    public UUID getId()                                       { return id; }
    public void setId(UUID id)                                { this.id = id; }
    public UUID getItemId()                                   { return itemId; }
    public void setItemId(UUID itemId)                        { this.itemId = itemId; }
    public String getEnunciado()                              { return enunciado; }
    public void setEnunciado(String enunciado)                { this.enunciado = enunciado; }
    public String getOpcionA()                                { return opcionA; }
    public void setOpcionA(String opcionA)                    { this.opcionA = opcionA; }
    public String getOpcionB()                                { return opcionB; }
    public void setOpcionB(String opcionB)                    { this.opcionB = opcionB; }
    public String getOpcionC()                                { return opcionC; }
    public void setOpcionC(String opcionC)                    { this.opcionC = opcionC; }
    public String getOpcionD()                                { return opcionD; }
    public void setOpcionD(String opcionD)                    { this.opcionD = opcionD; }
    public OpcionRespuesta getRespuestaCorrecta()             { return respuestaCorrecta; }
    public void setRespuestaCorrecta(OpcionRespuesta r)       { this.respuestaCorrecta = r; }
    public String getJustificacionCorrecta()                 { return justificacionCorrecta; }
    public void setJustificacionCorrecta(String j)           { this.justificacionCorrecta = j; }
    public Map<String, String> getJustificacionIncorrectas() { return justificacionIncorrectas; }
    public void setJustificacionIncorrectas(Map<String, String> j) { this.justificacionIncorrectas = j; }
    public String getReferenciaNormativa()                   { return referenciaNormativa; }
    public void setReferenciaNormativa(String r)             { this.referenciaNormativa = r; }
    public NivelDificultad getNivelDificultad()              { return nivelDificultad; }
    public void setNivelDificultad(NivelDificultad n)        { this.nivelDificultad = n; }
    public ProcesoCognitivo getProcesoCognitivo()            { return procesoCognitivo; }
    public void setProcesoCognitivo(ProcesoCognitivo p)      { this.procesoCognitivo = p; }
    public boolean isGeneradaPorIa()                         { return generadaPorIa; }
    public void setGeneradaPorIa(boolean g)                  { this.generadaPorIa = g; }
    public LocalDateTime getCreatedAt()                      { return createdAt; }
    public void setCreatedAt(LocalDateTime t)                { this.createdAt = t; }
}
