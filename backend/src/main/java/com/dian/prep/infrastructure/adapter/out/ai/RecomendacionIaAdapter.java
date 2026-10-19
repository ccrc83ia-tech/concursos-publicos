package com.dian.prep.infrastructure.adapter.out.ai;

import com.dian.prep.domain.model.Recomendacion;
import com.dian.prep.domain.model.Recomendacion.ReferenciaNormativa;
import com.dian.prep.domain.model.Recomendacion.TemaFallido;
import com.dian.prep.domain.port.out.GenerarRecomendacionPort;
import com.dian.prep.infrastructure.config.IaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adaptador que produce un plan de estudio con IA (Groq principal, Gemini
 * fallback) a partir del desempeño del usuario en un ítem. Si ningún proveedor
 * está configurado, devuelve una recomendación base calculada localmente para
 * que la feature siga siendo útil sin IA.
 */
@Component
public class RecomendacionIaAdapter implements GenerarRecomendacionPort {

    private static final Logger log = LoggerFactory.getLogger(RecomendacionIaAdapter.class);

    private static final String FORMATO_JSON = """
            Responde EXCLUSIVAMENTE con un objeto JSON válido (sin texto adicional, sin markdown)
            con exactamente esta forma:
            {
              "resumenEjecutivo": "diagnóstico breve (2-3 frases) del nivel actual y el foco de mejora",
              "temasFallidos": [{"tema": "...", "descripcion": "qué estudiar y por qué"}],
              "referenciasNormativas": [{"norma": "...", "articulo": "...", "url": "URL oficial o cadena vacía"}],
              "tiempoEstudioHoras": 6.5
            }
            Incluye entre 2 y 5 temas priorizados y entre 1 y 4 referencias normativas o técnicas reales.
            """;

    private final IaProperties props;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public RecomendacionIaAdapter(IaProperties props, ObjectMapper objectMapper) {
        this.props        = props;
        this.objectMapper = objectMapper;
        this.restClient   = RestClient.builder().build();
    }

    @Override
    public Recomendacion generar(ContextoRecomendacion ctx) {
        String prompt = construirPrompt(ctx);

        if (props.groqConfigurado()) {
            try {
                return parsear(llamarGroq(prompt), ctx);
            } catch (Exception e) {
                log.warn("Groq falló al generar recomendación, intentando Gemini. Causa: {}", e.getMessage());
            }
        }
        if (props.geminiConfigurado()) {
            try {
                return parsear(llamarGemini(prompt), ctx);
            } catch (Exception e) {
                log.warn("Gemini falló al generar recomendación. Causa: {}", e.getMessage());
            }
        }
        // Sin proveedor de IA o tras fallo: recomendación base local.
        return recomendacionBase(ctx);
    }

    // --- Prompt ---

    private String construirPrompt(ContextoRecomendacion ctx) {
        return """
                Eres un tutor experto en preparación para concursos de méritos públicos en Colombia.
                Un aspirante presentó un simulacro en el concurso "%s", en el indicador "%s"
                (tipo de prueba %s) y obtuvo %d de %d respuestas correctas (%.1f%%, nivel %s).
                Elabora un plan de estudio personalizado y accionable para cerrar esta brecha.
                %s
                """.formatted(
                nvl(ctx.nombreConcurso(), "convocatoria pública"),
                nvl(ctx.nombreIndicador(), "indicador"),
                nvl(ctx.tipoPrueba(), "FUNCIONAL"),
                ctx.preguntasCorrectas(), ctx.totalPreguntas(),
                ctx.puntajePorcentual(), nvl(ctx.nivelDominio(), "ROJO"),
                FORMATO_JSON);
    }

    // --- Groq (OpenAI-compatible chat completions) ---

    private String llamarGroq(String prompt) {
        IaProperties.Groq g = props.getGroq();
        Map<String, Object> body = Map.of(
                "model", g.getModel(),
                "max_tokens", g.getMaxTokens(),
                "temperature", 0.5,
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "Eres un tutor experto en concursos de méritos públicos en Colombia."),
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

    private JsonNode leerJson(String raw) {
        try {
            return objectMapper.readTree(raw == null ? "{}" : raw);
        } catch (Exception e) {
            throw new IllegalStateException("Respuesta no-JSON del proveedor de IA", e);
        }
    }

    // --- Parseo de la respuesta del modelo a dominio ---

    private Recomendacion parsear(String contenido, ContextoRecomendacion ctx) {
        if (contenido == null || contenido.isBlank()) return recomendacionBase(ctx);

        Recomendacion r = new Recomendacion();
        try {
            JsonNode nodo = objectMapper.readTree(extraerJsonObjeto(contenido));

            r.setResumenEjecutivo(emptyToNull(texto(nodo, "resumenEjecutivo")));

            List<TemaFallido> temas = new ArrayList<>();
            JsonNode temasNodo = nodo.get("temasFallidos");
            if (temasNodo != null && temasNodo.isArray()) {
                for (JsonNode t : temasNodo) {
                    String tema = texto(t, "tema");
                    if (tema != null && !tema.isBlank()) {
                        temas.add(new TemaFallido(tema, texto(t, "descripcion")));
                    }
                }
            }
            r.setTemasFallidos(temas);

            List<ReferenciaNormativa> refs = new ArrayList<>();
            JsonNode refsNodo = nodo.get("referenciasNormativas");
            if (refsNodo != null && refsNodo.isArray()) {
                for (JsonNode ref : refsNodo) {
                    String norma = texto(ref, "norma");
                    if (norma != null && !norma.isBlank()) {
                        refs.add(new ReferenciaNormativa(norma, texto(ref, "articulo"),
                                emptyToNull(texto(ref, "url"))));
                    }
                }
            }
            r.setReferenciasNormativas(refs);

            JsonNode horas = nodo.get("tiempoEstudioHoras");
            if (horas != null && horas.isNumber()) {
                r.setTiempoEstudioHoras(horas.asDouble());
            }
        } catch (Exception e) {
            log.warn("No se pudo parsear la recomendación de la IA: {}", e.getMessage());
            return recomendacionBase(ctx);
        }

        if (r.getResumenEjecutivo() == null && r.getTemasFallidos().isEmpty()) {
            return recomendacionBase(ctx);
        }
        if (r.getTiempoEstudioHoras() == null) {
            r.setTiempoEstudioHoras(estimarHoras(ctx));
        }
        return r;
    }

    /** Recomendación mínima calculada sin IA (fallback), para no dejar la feature vacía. */
    private Recomendacion recomendacionBase(ContextoRecomendacion ctx) {
        Recomendacion r = new Recomendacion();
        r.setResumenEjecutivo(
                "Desempeño de %.1f%% (%d/%d) en \"%s\". Refuerza los conceptos base de este indicador y repite el simulacro para medir el avance."
                        .formatted(ctx.puntajePorcentual(), ctx.preguntasCorrectas(),
                                ctx.totalPreguntas(), nvl(ctx.nombreIndicador(), "el indicador")));
        r.getTemasFallidos().add(new TemaFallido(
                nvl(ctx.nombreIndicador(), "Indicador"),
                "Estudia los fundamentos y practica preguntas de este indicador."));
        r.setTiempoEstudioHoras(estimarHoras(ctx));
        return r;
    }

    /** Estimación simple: más horas cuanto mayor es la brecha respecto al 100%. */
    private double estimarHoras(ContextoRecomendacion ctx) {
        double brecha = Math.max(0, 100.0 - ctx.puntajePorcentual());
        return Math.round((2 + brecha / 10.0) * 2) / 2.0; // paso de 0.5 h
    }

    private String extraerJsonObjeto(String contenido) {
        int inicio = contenido.indexOf('{');
        int fin = contenido.lastIndexOf('}');
        if (inicio >= 0 && fin > inicio) {
            return contenido.substring(inicio, fin + 1);
        }
        return contenido;
    }

    private String texto(JsonNode nodo, String campo) {
        if (nodo == null) return null;
        JsonNode v = nodo.get(campo);
        return v == null || v.isNull() ? null : v.asText();
    }

    private String emptyToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }

    private String nvl(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }
}
