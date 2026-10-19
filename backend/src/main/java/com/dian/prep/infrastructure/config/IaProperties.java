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

    public boolean groqConfigurado() {
        return groq.apiKey != null && !groq.apiKey.isBlank()
                && !groq.apiKey.equals("your-groq-api-key-here");
    }

    public boolean geminiConfigurado() {
        return gemini.apiKey != null && !gemini.apiKey.isBlank()
                && !gemini.apiKey.equals("your-gemini-api-key-here");
    }
}
