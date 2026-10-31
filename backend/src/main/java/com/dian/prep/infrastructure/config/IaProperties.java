package com.dian.prep.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración de los proveedores de IA (prefijo {@code app.ai}).
 */
@Component
@ConfigurationProperties(prefix = "app.ai")
public class IaProperties {

    private String provider = "groq";
    /** Versión del perfil/prompt actual; se graba en cada pregunta para trazabilidad. */
    private int versionGeneracion = 1;
    /**
     * Preguntas que genera cada ENUNCIADO (caso de lectura). Según el modelo de ítem
     * adoptado, un enunciado/caso describe una situación y de él se desprenden varias
     * preguntas que lo comparten. La Guía DIAN ilustra 1 pregunta por caso, pero el
     * banco de estudio usa 3-4 por caso para dar más práctica sobre el mismo contexto.
     * Es el factor para estimar el total de preguntas a partir de los enunciados pedidos.
     */
    private int preguntasPorEnunciado = 3;
    private Groq groq = new Groq();
    private Gemini gemini = new Gemini();
    private Validacion validacion = new Validacion();
    private Mantenimiento mantenimiento = new Mantenimiento();
    private Limpieza limpieza = new Limpieza();

    /**
     * Configuración del segundo modelo (validador). El patrón es
     * "redactor genera, validador revisa": solo se persisten las preguntas que
     * el validador aprueba. Para que la validación aporte valor, el validador
     * debe ser un proveedor/modelo distinto al redactor.
     */
    public static class Validacion {
        /** Si está activa, cada pregunta generada pasa por el validador antes de persistir. */
        private boolean enabled = true;
        /** Proveedor validador: "gemini" o "groq". Debe diferir idealmente del redactor. */
        private String provider = "gemini";
        /** Modelo del validador cuando el proveedor es groq (distinto al redactor). */
        private String groqModel = "llama-3.1-8b-instant";
        /** Tope diario de llamadas al validador (free tier). El job no lo excede. */
        private int limiteDiario = 400;

        public boolean isEnabled()            { return enabled; }
        public void setEnabled(boolean v)     { this.enabled = v; }
        public String getProvider()           { return provider; }
        public void setProvider(String v)     { this.provider = v; }
        public String getGroqModel()          { return groqModel; }
        public void setGroqModel(String v)    { this.groqModel = v; }
        public int getLimiteDiario()          { return limiteDiario; }
        public void setLimiteDiario(int v)    { this.limiteDiario = v; }
    }

    /**
     * Configuración del job diario de mantenimiento del banco: valida pendientes y
     * regenera huecos con el modelo premium, sin exceder los cupos del free tier.
     */
    public static class Mantenimiento {
        /** Habilita el worker de mejora del banco. */
        private boolean enabled = false;
        /** Intervalo entre corridas del worker, en ms (por defecto 15 min). */
        private long intervaloMs = 900_000;
        /** Retardo antes de la primera corrida tras arrancar, en ms. */
        private long initialDelayMs = 60_000;
        /** Modelo redactor premium para regenerar huecos (p. ej. Gemini 3.x). */
        private String modeloPremium = "gemini-3.8-flash";
        /** Tope diario de llamadas al redactor premium (free tier ~20 RPD). */
        private int limitePremiumDiario = 20;
        /** Objetivo de preguntas VALIDADAS por ítem al que tiende el banco. */
        private int objetivoValidadasPorItem = 30;
        /** Máximo de ítems a atender por corrida (acota el trabajo diario). */
        private int maxItemsPorCorrida = 50;

        public boolean isEnabled()                 { return enabled; }
        public void setEnabled(boolean v)          { this.enabled = v; }
        public long getIntervaloMs()               { return intervaloMs; }
        public void setIntervaloMs(long v)         { this.intervaloMs = v; }
        public long getInitialDelayMs()            { return initialDelayMs; }
        public void setInitialDelayMs(long v)      { this.initialDelayMs = v; }
        public String getModeloPremium()           { return modeloPremium; }
        public void setModeloPremium(String v)     { this.modeloPremium = v; }
        public int getLimitePremiumDiario()        { return limitePremiumDiario; }
        public void setLimitePremiumDiario(int v)  { this.limitePremiumDiario = v; }
        public int getObjetivoValidadasPorItem()   { return objetivoValidadasPorItem; }
        public void setObjetivoValidadasPorItem(int v) { this.objetivoValidadasPorItem = v; }
        public int getMaxItemsPorCorrida()         { return maxItemsPorCorrida; }
        public void setMaxItemsPorCorrida(int v)   { this.maxItemsPorCorrida = v; }
    }

    /**
     * Job de limpieza del banco: elimina las preguntas RECHAZADA/AGOTADA que el
     * validador descartó. Es independiente del job de mejora y barato de ejecutar.
     */
    public static class Limpieza {
        /** Habilita el job programado de limpieza de descartadas. */
        private boolean enabled = true;
        /** Cron de ejecución (por defecto 02:00 cada día, antes del job de mejora). */
        private String cron = "0 0 2 * * *";

        public boolean isEnabled()        { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
        public String getCron()           { return cron; }
        public void setCron(String v)     { this.cron = v; }
    }

    public static class Groq {
        private String apiKey;
        private String baseUrl = "https://api.groq.com/openai/v1";
        private String model = "llama-3.3-70b-versatile";
        private int maxTokens = 4000;

        public String getApiKey()            { return apiKey; }
        public void setApiKey(String v)      { this.apiKey = v; }
        public String getBaseUrl()           { return baseUrl; }
        public void setBaseUrl(String v)     { this.baseUrl = v; }
        public String getModel()             { return model; }
        public void setModel(String v)       { this.model = v; }
        public int getMaxTokens()            { return maxTokens; }
        public void setMaxTokens(int v)      { this.maxTokens = v; }
    }

    public static class Gemini {
        private String apiKey;
        private String model = "gemini-3.5-flash-lite";
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";

        public String getApiKey()         { return apiKey; }
        public void setApiKey(String v)   { this.apiKey = v; }
        public String getModel()          { return model; }
        public void setModel(String v)    { this.model = v; }
        public String getBaseUrl()        { return baseUrl; }
        public void setBaseUrl(String v)  { this.baseUrl = v; }
    }

    public String getProvider()        { return provider; }
    public void setProvider(String v)  { this.provider = v; }
    public int getVersionGeneracion()         { return versionGeneracion; }
    public void setVersionGeneracion(int v)   { this.versionGeneracion = v; }
    public int getPreguntasPorEnunciado()        { return preguntasPorEnunciado; }
    public void setPreguntasPorEnunciado(int v)  { this.preguntasPorEnunciado = v; }
    public Groq getGroq()              { return groq; }
    public void setGroq(Groq v)        { this.groq = v; }
    public Gemini getGemini()          { return gemini; }
    public void setGemini(Gemini v)    { this.gemini = v; }
    public Validacion getValidacion()        { return validacion; }
    public void setValidacion(Validacion v)  { this.validacion = v; }
    public Mantenimiento getMantenimiento()         { return mantenimiento; }
    public void setMantenimiento(Mantenimiento v)   { this.mantenimiento = v; }
    public Limpieza getLimpieza()                   { return limpieza; }
    public void setLimpieza(Limpieza v)             { this.limpieza = v; }

    public boolean groqConfigurado() {
        return groq.apiKey != null && !groq.apiKey.isBlank()
                && !groq.apiKey.equals("your-groq-api-key-here");
    }

    public boolean geminiConfigurado() {
        return gemini.apiKey != null && !gemini.apiKey.isBlank()
                && !gemini.apiKey.equals("your-gemini-api-key-here");
    }
}
