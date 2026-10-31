package com.dian.prep.infrastructure.adapter.out.ai;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.IaGeneradorPort;
import com.dian.prep.domain.port.out.ValidadorPreguntaPort;
import com.dian.prep.domain.port.out.ValidadorPreguntaPort.ResultadoValidacion;
import com.dian.prep.infrastructure.config.IaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
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
    private final ValidadorPreguntaPort validador;
    private final RestClient restClient;

    public IaGeneradorAdapter(IaProperties props, PromptBuilder promptBuilder,
                              ObjectMapper objectMapper, ValidadorPreguntaPort validador) {
        this.props         = props;
        this.promptBuilder = promptBuilder;
        this.objectMapper  = objectMapper;
        this.validador     = validador;
        // Timeouts explícitos: sin ellos, un cuelgue de red deja la llamada colgada
        // indefinidamente y el lote falla con un "I/O error ... null" tras consumir
        // el tiempo del executor. 15s para conectar, 120s para leer (la generación de
        // un lote grande puede tardar). El runner ya reintenta con backoff.
        this.restClient    = RestClient.builder()
                .requestFactory(httpRequestFactory())
                .build();
    }

    private static SimpleClientHttpRequestFactory httpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(15));
        factory.setReadTimeout(Duration.ofSeconds(120));
        return factory;
    }

    @Override
    public List<Pregunta> generarLote(ContextoGeneracion contexto, int cantidad) {
        String prompt = promptBuilder.construir(contexto, cantidad);
        List<Pregunta> generadas = redactar(prompt);
        // Cada pregunta recién redactada arranca PENDIENTE_VALIDACION. Se baraja el
        // orden de las opciones (anti-sesgo de posición) y se calcula el hash DESPUÉS
        // de barajar: el hash usa las opciones ordenadas, así que es estable para dedup.
        var rnd = java.util.concurrent.ThreadLocalRandom.current();
        generadas.forEach(p -> {
            p.setEstadoCalidad(Pregunta.EstadoCalidad.PENDIENTE_VALIDACION);
            p.setVersionGeneracion(props.getVersionGeneracion());
            p.barajarOpciones(rnd);
            p.calcularHash();
        });
        return validarLote(generadas, contexto);
    }

    @Override
    public List<Pregunta> generarLotePremium(ContextoGeneracion contexto, int cantidad) {
        if (!props.geminiConfigurado()) {
            // Sin modelo premium disponible, se degrada a la generación estándar.
            log.warn("Gemini no configurado; el lote premium cae a generación estándar.");
            return generarLote(contexto, cantidad);
        }
        String prompt = promptBuilder.construir(contexto, cantidad);
        String modeloPremium = props.getMantenimiento().getModeloPremium();
        List<Pregunta> generadas = marcarOrigen(
                parsear(llamarGemini(prompt, modeloPremium)), Pregunta.OrigenModelo.PREMIUM);
        var rnd = java.util.concurrent.ThreadLocalRandom.current();
        generadas.forEach(p -> {
            p.setEstadoCalidad(Pregunta.EstadoCalidad.PENDIENTE_VALIDACION);
            p.setVersionGeneracion(props.getVersionGeneracion());
            p.barajarOpciones(rnd);
            p.calcularHash();
        });
        return validarLote(generadas, contexto);
    }

    /**
     * Fase 1: el modelo redactor produce el lote. Marca el origen del modelo:
     * Groq = PROVISIONAL (alto cupo, cubre al usuario ya); Gemini = PREMIUM.
     */
    private List<Pregunta> redactar(String prompt) {
        if (props.groqConfigurado()) {
            try {
                return marcarOrigen(parsear(llamarGroq(prompt)), Pregunta.OrigenModelo.PROVISIONAL);
            } catch (Exception e) {
                log.warn("Groq falló, intentando Gemini. Causa: {}", e.getMessage());
            }
        }
        if (props.geminiConfigurado()) {
            return marcarOrigen(parsear(llamarGemini(prompt)), Pregunta.OrigenModelo.PREMIUM);
        }
        throw new IllegalStateException(
                "No hay proveedor de IA configurado. Defina GROQ_API_KEY o GEMINI_API_KEY.");
    }

    private List<Pregunta> marcarOrigen(List<Pregunta> preguntas, Pregunta.OrigenModelo origen) {
        preguntas.forEach(p -> p.setOrigenModelo(origen));
        return preguntas;
    }

    /**
     * Fase 2: el modelo validador revisa el lote en UNA sola llamada (respeta el
     * cupo del free tier). Solo se conservan las aprobadas; si el validador corrige
     * la clave, se aplica la corrección. Mejor devolver menos preguntas pero
     * correctas que un lote con ítems dudosos.
     */
    private List<Pregunta> validarLote(List<Pregunta> candidatas, ContextoGeneracion contexto) {
        if (candidatas.isEmpty()) {
            return candidatas;
        }
        // Sin segundo modelo que dictamine, se confía en el redactor y se marcan
        // VALIDADAS para que el simulacro pueda servirlas (si no, nunca habría validadas).
        if (!props.getValidacion().isEnabled()) {
            candidatas.forEach(Pregunta::marcarValidada);
            return candidatas;
        }
        List<ResultadoValidacion> veredictos = validador.validarLote(candidatas, contexto);

        List<Pregunta> aprobadas = new ArrayList<>();
        int rechazadas = 0;
        for (int i = 0; i < candidatas.size(); i++) {
            Pregunta p = candidatas.get(i);
            // Si el validador no devolvió veredicto para esta posición, se aprueba (fail-open).
            ResultadoValidacion v = (i < veredictos.size() && veredictos.get(i) != null)
                    ? veredictos.get(i)
                    : ResultadoValidacion.aprobar();
            if (v.aprobada()) {
                if (v.claveCorregida() != null) {
                    p.setRespuestaCorrecta(v.claveCorregida());
                    // Si cambió la clave, el hash no varía (depende de enunciado+opciones),
                    // pero lo recalculamos por robustez ante futuros cambios de criterio.
                    p.calcularHash();
                }
                p.marcarValidada();
                aprobadas.add(p);
            } else {
                rechazadas++;
                log.info("Pregunta descartada por el validador: {}", v.motivo());
            }
        }
        if (rechazadas > 0) {
            log.info("Validación de lote: {} aprobadas, {} rechazadas", aprobadas.size(), rechazadas);
        }
        return aprobadas;
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
        return llamarGemini(prompt, props.getGemini().getModel());
    }

    /** Llama a Gemini con un modelo específico (permite usar el redactor premium). */
    private String llamarGemini(String prompt, String modelo) {
        IaProperties.Gemini gem = props.getGemini();
        String uri = "%s/models/%s:generateContent?key=%s"
                .formatted(gem.getBaseUrl(), modelo, gem.getApiKey());

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
                // El formato PJS tiene solo 3 opciones; D llega vacía o ausente.
                // La columna opcion_d es NOT NULL, por eso se normaliza a "" si viene null.
                String opcionD = texto(nodo, "opcionD");
                p.setOpcionD(opcionD == null ? "" : opcionD);
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

    /**
     * Valida un ítem PJS. El formato oficial de la CNSC usa TRES opciones (A, B, C),
     * por lo que la opción D es opcional (suele venir vacía). Se exige enunciado,
     * las tres primeras opciones y una respuesta correcta en {A, B, C}.
     */
    private boolean esValida(Pregunta p) {
        boolean tresOpciones = noVacia(p.getOpcionA())
                && noVacia(p.getOpcionB())
                && noVacia(p.getOpcionC());
        boolean claveValida = p.getRespuestaCorrecta() != null
                && p.getRespuestaCorrecta() != Pregunta.OpcionRespuesta.D;
        return noVacia(p.getEnunciado()) && tresOpciones && claveValida;
    }

    private boolean noVacia(String v) {
        return v != null && !v.isBlank();
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
