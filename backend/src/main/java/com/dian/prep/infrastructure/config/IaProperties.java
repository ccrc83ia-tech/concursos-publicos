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
    private Groq groq = new Groq();
    private Gemini gemini = new Gemini();
    private Validacion validacion = new Validacion();

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

        public boolean isEnabled()            { return enabled; }
        public void setEnabled(boolean v)     { this.enabled = v; }
        public String getProvider()           { return provider; }
        public void setProvider(String v)     { this.provider = v; }
        public String getGroqModel()          { return groqModel; }
        public void setGroqModel(String v)    { this.groqModel = v; }
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
        private String model = "gemini-2.5-flash";
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
    public Groq getGroq()              { return groq; }
    public void setGroq(Groq v)        { this.groq = v; }
    public Gemini getGemini()          { return gemini; }
    public void setGemini(Gemini v)    { this.gemini = v; }
    public Validacion getValidacion()        { return validacion; }
    public void setValidacion(Validacion v)  { this.validacion = v; }

    public boolean groqConfigurado() {
        return groq.apiKey != null && !groq.apiKey.isBlank()
                && !groq.apiKey.equals("your-groq-api-key-here");
    }

    public boolean geminiConfigurado() {
        return gemini.apiKey != null && !gemini.apiKey.isBlank()
                && !gemini.apiKey.equals("your-gemini-api-key-here");
    }
}
