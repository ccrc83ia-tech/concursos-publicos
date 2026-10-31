package com.dian.prep.infrastructure.adapter.out.ai;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.IaGeneradorPort.ContextoGeneracion;
import com.dian.prep.domain.port.out.ValidadorPreguntaPort;
import com.dian.prep.infrastructure.config.IaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Validador de preguntas basado en un SEGUNDO modelo de IA, independiente del
 * redactor. Revisa cada ítem con criterio crítico y aprueba solo los aptos.
 *
 * <p>Selección del proveedor validador (configurable con {@code app.ai.validacion}):
 * para que la revisión sea independiente se prefiere un proveedor distinto al
 * redactor. Por defecto usa Gemini; si no está configurado, cae a Groq con un
 * modelo distinto al redactor. Si no hay ningún proveedor disponible, el
 * validador aprueba por defecto (fail-open) para no bloquear la generación.</p>
 */
@Component
public class ValidadorPreguntaAdapter implements ValidadorPreguntaPort {

    private static final Logger log = LoggerFactory.getLogger(ValidadorPreguntaAdapter.class);

    private final IaProperties props;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public ValidadorPreguntaAdapter(IaProperties props, ObjectMapper objectMapper) {
        this.props        = props;
        this.objectMapper = objectMapper;
        this.restClient   = RestClient.builder().build();
    }

    @Override
    public ResultadoValidacion validar(Pregunta p, ContextoGeneracion ctx) {
        // Verificación estructural previa (barata, sin red): formato PJS de 3 opciones.
        String estructural = revisarEstructura(p);
        if (estructural != null) {
            return ResultadoValidacion.rechazar(estructural);
        }

        String prompt = construirPrompt(p, ctx);
        try {
            String respuesta = llamarValidador(prompt);
            if (respuesta == null || respuesta.isBlank()) {
                return faltaProveedorOFallo("respuesta vacía del validador");
            }
            return interpretar(respuesta, p);
        } catch (Exception e) {
            log.warn("Validador IA falló, se aprueba por defecto (fail-open). Causa: {}", e.getMessage());
            return ResultadoValidacion.aprobar();
        }
    }

    // --- Verificación estructural (determinista) ---

    private String revisarEstructura(Pregunta p) {
        if (vacia(p.getEnunciado()))            return "enunciado vacío";
        if (vacia(p.getOpcionA()) || vacia(p.getOpcionB()) || vacia(p.getOpcionC()))
            return "faltan opciones A/B/C";
        if (p.getRespuestaCorrecta() == null || p.getRespuestaCorrecta() == Pregunta.OpcionRespuesta.D)
            return "clave inválida (debe ser A, B o C)";
        if (vacia(p.getJustificacionCorrecta()))
            return "falta justificación de la respuesta correcta";
        // El formato PJS oficial exige un caso contextualizado.
        if (!p.getEnunciado().toUpperCase().contains("CASO"))
            return "el enunciado no presenta un caso situacional";
        return null;
    }

    // --- Prompt del validador (crítico y estricto) ---

    private String construirPrompt(Pregunta p, ContextoGeneracion ctx) {
        String opciones = """
                A) %s
                B) %s
                C) %s""".formatted(p.getOpcionA(), p.getOpcionB(), p.getOpcionC());

        return """
                Actúa como revisor experto y MUY CRÍTICO de ítems de pruebas de la CNSC
                (concurso de méritos "%s", indicador "%s", tipo de prueba %s).
                Tu tarea es decidir si el siguiente ítem de Juicio Situacional es APTO para que
                un aspirante estudie con él. Debes ser estricto: ante cualquier duda, repruébalo.

                Verifica TODOS estos criterios:
                1. La opción marcada como correcta (%s) es inequívocamente la MEJOR y única solución
                   efectiva al enunciado. Si otra opción es igual o más correcta, repruébalo.
                2. Las otras dos opciones son claramente subóptimas, pero plausibles (no absurdas).
                3. El enunciado y el caso son claros, sin ambigüedad, sin dobles negaciones ni pistas.
                4. El contenido es técnicamente correcto y pertinente al indicador evaluado.
                5. Si cita una norma o artículo, debe ser real y estar bien atribuido; si parece
                   inventado o mal citado, repruébalo.
                6. Hay una sola respuesta correcta entre A, B y C.

                ÍTEM A REVISAR
                %s
                Opciones:
                %s
                Clave propuesta: %s
                Justificación de la correcta: %s

                Responde EXCLUSIVAMENTE con un objeto JSON válido (sin markdown, sin texto extra):
                {
                  "apta": true|false,
                  "claveCorrecta": "A|B|C",
                  "motivo": "razón breve del veredicto"
                }
                Donde "claveCorrecta" es la que TÚ consideras correcta tras tu análisis
                (coincida o no con la clave propuesta).
                """.formatted(
                nvl(ctx.nombreConcurso(), "convocatoria pública"),
                nvl(ctx.nombreIndicador(), "indicador"),
                ctx.tipoPrueba(),
                p.getRespuestaCorrecta(),
                p.getEnunciado(),
                opciones,
                p.getRespuestaCorrecta(),
                nvl(p.getJustificacionCorrecta(), ""));
    }

    // --- Interpretación del veredicto ---

    private ResultadoValidacion interpretar(String contenido, Pregunta p) {
        try {
            JsonNode nodo = objectMapper.readTree(extraerJsonObjeto(contenido));
            boolean apta = nodo.path("apta").asBoolean(false);
            String motivo = nodo.path("motivo").asText("sin motivo");
            Pregunta.OpcionRespuesta claveValidador = parseOpcion(nodo.path("claveCorrecta").asText(null));

            if (!apta) {
                return ResultadoValidacion.rechazar(motivo);
            }
            // Aprobada: si el validador señala una clave distinta y válida, se corrige.
            if (claveValidador != null && claveValidador != p.getRespuestaCorrecta()) {
                return ResultadoValidacion.aprobarConClave(claveValidador);
            }
            return ResultadoValidacion.aprobar();
        } catch (Exception e) {
            log.warn("No se pudo interpretar el veredicto del validador: {}", e.getMessage());
            return ResultadoValidacion.aprobar(); // fail-open
        }
    }

    // --- Selección y llamada al proveedor validador ---

    private String llamarValidador(String prompt) {
        String proveedor = props.getValidacion().getProvider();
        boolean prefiereGemini = "gemini".equalsIgnoreCase(proveedor);

        if (prefiereGemini && props.geminiConfigurado()) {
            return llamarGemini(prompt);
        }
        if (!prefiereGemini && props.groqConfigurado()) {
            return llamarGroq(prompt, props.getValidacion().getGroqModel());
        }
        // Fallback: el que esté disponible (idealmente distinto del redactor).
        if (props.geminiConfigurado()) return llamarGemini(prompt);
        if (props.groqConfigurado())   return llamarGroq(prompt, props.getValidacion().getGroqModel());
        return null; // ningún proveedor -> fail-open en validar()
    }

    private ResultadoValidacion faltaProveedorOFallo(String motivo) {
        log.warn("Validador sin proveedor disponible ({}). Se aprueba por defecto.", motivo);
        return ResultadoValidacion.aprobar();
    }

    private String llamarGroq(String prompt, String model) {
        IaProperties.Groq g = props.getGroq();
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 800,
                "temperature", 0.0,
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "Eres un revisor crítico de ítems de pruebas de concursos de méritos en Colombia."),
                        Map.of("role", "user", "content", prompt)));

        String raw = restClient.post()
                .uri(g.getBaseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + g.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return leerJson(raw).path("choices").path(0).path("message").path("content").asText("");
    }

    private String llamarGemini(String prompt) {
        IaProperties.Gemini gem = props.getGemini();
        String uri = "%s/models/%s:generateContent?key=%s"
                .formatted(gem.getBaseUrl(), gem.getModel(), gem.getApiKey());

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        String raw = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return leerJson(raw).path("candidates").path(0).path("content")
                .path("parts").path(0).path("text").asText("");
    }

    // --- Helpers ---

    private JsonNode leerJson(String raw) {
        try {
            return objectMapper.readTree(raw == null ? "{}" : raw);
        } catch (Exception e) {
            throw new IllegalStateException("Respuesta no-JSON del proveedor validador", e);
        }
    }

    private String extraerJsonObjeto(String contenido) {
        int inicio = contenido.indexOf('{');
        int fin = contenido.lastIndexOf('}');
        return (inicio >= 0 && fin > inicio) ? contenido.substring(inicio, fin + 1) : contenido;
    }

    private Pregunta.OpcionRespuesta parseOpcion(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Pregunta.OpcionRespuesta.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean vacia(String v) {
        return v == null || v.isBlank();
    }

    private String nvl(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }
}
