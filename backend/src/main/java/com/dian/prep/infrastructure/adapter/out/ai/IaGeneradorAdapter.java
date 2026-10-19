package com.dian.prep.infrastructure.adapter.out.ai;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.IaGeneradorPort;
import com.dian.prep.infrastructure.config.IaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador de generación con IA. Usa Groq (API compatible con OpenAI) como
 * proveedor principal y Gemini como fallback. Convierte la respuesta JSON del
 * modelo en preguntas de dominio.
 */
@Component
public class IaGeneradorAdapter implements IaGeneradorPort {

    private static final Logger log = LoggerFactory.getLogger(IaGeneradorAdapter.class);

    private final IaProperties props;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public IaGeneradorAdapter(IaProperties props, PromptBuilder promptBuilder, ObjectMapper objectMapper) {
        this.props         = props;
        this.promptBuilder = promptBuilder;
        this.objectMapper  = objectMapper;
        this.restClient    = RestClient.builder().build();
    }

    @Override
    public List<Pregunta> generarLote(ContextoGeneracion contexto, int cantidad) {
        String prompt = promptBuilder.construir(contexto, cantidad);

        if (props.groqConfigurado()) {
            try {
                return parsear(llamarGroq(prompt));
            } catch (Exception e) {
                log.warn("Groq falló, intentando Gemini. Causa: {}", e.getMessage());
            }
        }
        if (props.geminiConfigurado()) {
            return parsear(llamarGemini(prompt));
        }
        throw new IllegalStateException(
                "No hay proveedor de IA configurado. Defina GROQ_API_KEY o GEMINI_API_KEY.");
    }

    // --- Groq (OpenAI-compatible chat completions) ---

    private String llamarGroq(String prompt) {
        IaProperties.Groq g = props.getGroq();
        Map<String, Object> body = Map.of(
                "model", g.getModel(),
                "max_tokens", g.getMaxTokens(),
                "temperature", 0.7,
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "Eres un experto en diseño de pruebas para concursos de méritos públicos en Colombia."),
                        Map.of("role", "user", "content", prompt)));

        String raw = restClient.post()
                .uri(g.getBaseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + g.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        JsonNode resp = leerJson(raw);
        return resp.path("choices").path(0).path("message").path("content").asText("");
    }

    // --- Gemini (generateContent) ---

    private String llamarGemini(String prompt) {
        IaProperties.Gemini gem = props.getGemini();
        String uri = "%s/models/%s:generateContent?key=%s"
                .formatted(gem.getBaseUrl(), gem.getModel(), gem.getApiKey());

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt)))));

        String raw = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        JsonNode resp = leerJson(raw);
        return resp.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
    }

    /** Convierte el cuerpo de respuesta (texto) en árbol JSON con el ObjectMapper inyectado. */
    private JsonNode leerJson(String raw) {
        try {
            return objectMapper.readTree(raw == null ? "{}" : raw);
        } catch (Exception e) {
            throw new IllegalStateException("Respuesta no-JSON del proveedor de IA", e);
        }
    }

    // --- Parseo de la respuesta del modelo a dominio ---

    private List<Pregunta> parsear(String contenido) {
        List<Pregunta> preguntas = new ArrayList<>();
        if (contenido == null || contenido.isBlank()) return preguntas;

        try {
            JsonNode raiz = objectMapper.readTree(extraerJsonArray(contenido));
            if (!raiz.isArray()) return preguntas;

            for (JsonNode nodo : raiz) {
                Pregunta p = Pregunta.crear(null, texto(nodo, "enunciado"));
                p.setOpcionA(texto(nodo, "opcionA"));
                p.setOpcionB(texto(nodo, "opcionB"));
                p.setOpcionC(texto(nodo, "opcionC"));
                p.setOpcionD(texto(nodo, "opcionD"));
                p.setRespuestaCorrecta(parseOpcion(texto(nodo, "respuestaCorrecta")));
                p.setJustificacionCorrecta(texto(nodo, "justificacionCorrecta"));
                p.setJustificacionIncorrectas(parseMapa(nodo.get("justificacionIncorrectas")));
                p.setReferenciaNormativa(emptyToNull(texto(nodo, "referenciaNormativa")));
                p.setNivelDificultad(parseDificultad(texto(nodo, "nivelDificultad")));
                p.setProcesoCognitivo(parseProceso(texto(nodo, "procesoCognitivo")));

                if (esValida(p)) preguntas.add(p);
            }
        } catch (Exception e) {
            log.warn("No se pudo parsear la respuesta de la IA: {}", e.getMessage());
        }
        return preguntas;
    }

    /** El modelo a veces envuelve el JSON en ```json ... ```; extrae el array. */
    private String extraerJsonArray(String contenido) {
        int inicio = contenido.indexOf('[');
        int fin = contenido.lastIndexOf(']');
        if (inicio >= 0 && fin > inicio) {
            return contenido.substring(inicio, fin + 1);
        }
        return contenido;
    }

    private boolean esValida(Pregunta p) {
        return p.getEnunciado() != null && !p.getEnunciado().isBlank()
                && p.getOpcionA() != null && p.getOpcionB() != null
                && p.getOpcionC() != null && p.getOpcionD() != null
                && p.getRespuestaCorrecta() != null;
    }

    private String texto(JsonNode nodo, String campo) {
        JsonNode v = nodo.get(campo);
        return v == null || v.isNull() ? null : v.asText();
    }

    private String emptyToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }

    private Pregunta.OpcionRespuesta parseOpcion(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Pregunta.OpcionRespuesta.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Pregunta.NivelDificultad parseDificultad(String v) {
        if (v == null || v.isBlank()) return Pregunta.NivelDificultad.INTERMEDIO;
        try {
            return Pregunta.NivelDificultad.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Pregunta.NivelDificultad.INTERMEDIO;
        }
    }

    private Pregunta.ProcesoCognitivo parseProceso(String v) {
        if (v == null || v.isBlank()) return Pregunta.ProcesoCognitivo.APLICACION;
        try {
            return Pregunta.ProcesoCognitivo.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Pregunta.ProcesoCognitivo.APLICACION;
        }
    }

    private Map<String, String> parseMapa(JsonNode nodo) {
        Map<String, String> mapa = new LinkedHashMap<>();
        if (nodo != null && nodo.isObject()) {
            nodo.properties().forEach(e -> {
                if (!e.getValue().isNull()) mapa.put(e.getKey(), e.getValue().asText());
            });
        }
        return mapa;
    }
}
