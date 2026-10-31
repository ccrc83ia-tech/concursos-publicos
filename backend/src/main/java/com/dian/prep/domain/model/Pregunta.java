package com.dian.prep.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HexFormat;
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

    // --- Calidad del banco (doble IA) ---
    private EstadoCalidad estadoCalidad = EstadoCalidad.PENDIENTE_VALIDACION;
    private OrigenModelo origenModelo = OrigenModelo.PROVISIONAL;
    /** Hash SHA-256 del contenido normalizado; identifica duplicados dentro del ítem. */
    private String hashContenido;
    /** Veces que la pregunta fue sometida a validación; a los 3 rechazos pasa a AGOTADA. */
    private int intentosGeneracion;
    /** Versión del perfil/prompt con el que se generó (trazabilidad de mejoras). */
    private int versionGeneracion = 1;
    private LocalDateTime validadaAt;

    public enum OpcionRespuesta { A, B, C, D }

    public enum NivelDificultad { BASICO, INTERMEDIO, AVANZADO }

    /**
     * Estado de calidad de la pregunta dentro del banco:
     * <ul>
     *   <li>PENDIENTE_VALIDACION: redactada, aún no dictaminada por el validador.</li>
     *   <li>VALIDADA: aprobada por el validador; apta para servir en simulacros.</li>
     *   <li>RECHAZADA: el validador la descartó; se puede reintentar (regenerar).</li>
     *   <li>AGOTADA: rechazada el máximo de veces; se deja de insistir.</li>
     * </ul>
     */
    public enum EstadoCalidad { PENDIENTE_VALIDACION, VALIDADA, RECHAZADA, AGOTADA }

    /**
     * Modelo que redactó la pregunta:
     * <ul>
     *   <li>PREMIUM: modelo de mayor calidad (p. ej. Gemini 3.x), cupo reducido.</li>
     *   <li>PROVISIONAL: modelo de alto cupo (Groq) para cubrir al usuario de inmediato.</li>
     * </ul>
     */
    public enum OrigenModelo { PREMIUM, PROVISIONAL }

    /** Máximo de rechazos del validador antes de marcar la pregunta como AGOTADA. */
    public static final int MAX_INTENTOS = 3;

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

    public EstadoCalidad getEstadoCalidad()                  { return estadoCalidad; }
    public void setEstadoCalidad(EstadoCalidad e)            { this.estadoCalidad = e; }
    public OrigenModelo getOrigenModelo()                    { return origenModelo; }
    public void setOrigenModelo(OrigenModelo o)              { this.origenModelo = o; }
    public String getHashContenido()                         { return hashContenido; }
    public void setHashContenido(String h)                   { this.hashContenido = h; }
    public int getIntentosGeneracion()                       { return intentosGeneracion; }
    public void setIntentosGeneracion(int i)                 { this.intentosGeneracion = i; }
    public int getVersionGeneracion()                        { return versionGeneracion; }
    public void setVersionGeneracion(int v)                  { this.versionGeneracion = v; }
    public LocalDateTime getValidadaAt()                     { return validadaAt; }
    public void setValidadaAt(LocalDateTime t)               { this.validadaAt = t; }

    // --- Transiciones de estado de calidad ---

    /** Marca la pregunta como aprobada por el validador. */
    public void marcarValidada() {
        this.estadoCalidad = EstadoCalidad.VALIDADA;
        this.validadaAt = LocalDateTime.now();
    }

    /**
     * Registra un rechazo del validador. Al alcanzar {@link #MAX_INTENTOS} rechazos
     * la pregunta pasa a AGOTADA y se deja de reintentar (protege el cupo de IA).
     */
    public void marcarRechazada() {
        this.intentosGeneracion++;
        this.estadoCalidad = (intentosGeneracion >= MAX_INTENTOS)
                ? EstadoCalidad.AGOTADA
                : EstadoCalidad.RECHAZADA;
    }

    public boolean estaValidada() {
        return estadoCalidad == EstadoCalidad.VALIDADA;
    }

    /**
     * Baraja las tres opciones (A, B, C) para evitar el sesgo de posición, es decir
     * que la respuesta correcta tienda a caer siempre en la misma letra. Reubica la
     * clave y reindexa las justificaciones de las incorrectas de forma coherente.
     *
     * <p>No toca la opción D: el formato PJS usa solo tres opciones y D queda vacía.
     * El hash de contenido no cambia (se calcula sobre las opciones ordenadas), por
     * lo que barajar no afecta la deduplicación.</p>
     *
     * @param rnd fuente de aleatoriedad (inyectable para pruebas deterministas)
     */
    public void barajarOpciones(java.util.random.RandomGenerator rnd) {
        if (respuestaCorrecta == null || respuestaCorrecta == OpcionRespuesta.D) return;

        // Texto actual de cada letra y su justificación (si es incorrecta).
        String[] letras = { "A", "B", "C" };
        String[] textos = { opcionA, opcionB, opcionC };
        Map<String, String> justIncorrectas = justificacionIncorrectas != null
                ? justificacionIncorrectas : Map.of();

        // Permutación aleatoria de los índices 0,1,2.
        Integer[] orden = { 0, 1, 2 };
        for (int i = orden.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Integer tmp = orden[i]; orden[i] = orden[j]; orden[j] = tmp;
        }

        int indiceCorrectaOriginal = respuestaCorrecta.ordinal(); // A=0, B=1, C=2
        String[] nuevosTextos = new String[3];
        java.util.Map<String, String> nuevasJust = new java.util.LinkedHashMap<>();
        OpcionRespuesta nuevaClave = respuestaCorrecta;

        for (int destino = 0; destino < 3; destino++) {
            int origen = orden[destino];
            nuevosTextos[destino] = textos[origen];
            if (origen == indiceCorrectaOriginal) {
                nuevaClave = OpcionRespuesta.valueOf(letras[destino]);
            } else {
                // Reubica la justificación de una incorrecta a su nueva letra.
                String just = justIncorrectas.get(letras[origen]);
                if (just != null) nuevasJust.put(letras[destino], just);
            }
        }

        this.opcionA = nuevosTextos[0];
        this.opcionB = nuevosTextos[1];
        this.opcionC = nuevosTextos[2];
        this.respuestaCorrecta = nuevaClave;
        if (!nuevasJust.isEmpty()) this.justificacionIncorrectas = nuevasJust;
    }

    /**
     * Calcula (y memoriza) el hash de contenido usado para deduplicar dentro del ítem.
     * Normaliza el texto (minúsculas, sin tildes ni signos, espacios colapsados) y
     * combina el enunciado con las tres opciones ORDENADAS, de modo que dos preguntas
     * con las mismas opciones en distinto orden produzcan el mismo hash.
     *
     * @return el hash calculado (también queda guardado en {@link #hashContenido})
     */
    public String calcularHash() {
        String[] opciones = { normalizar(opcionA), normalizar(opcionB), normalizar(opcionC) };
        Arrays.sort(opciones);
        String base = normalizar(enunciado) + "|" + String.join("|", opciones);
        this.hashContenido = sha256(base);
        return this.hashContenido;
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase()
                .replaceAll("[^a-z0-9ñ ]", " ")  // deja solo alfanumérico + ñ
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de la pregunta", e);
        }
    }
}
