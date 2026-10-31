package com.dian.prep.infrastructure.adapter.out.ai;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.CupoIaRepository;
import com.dian.prep.domain.port.out.IaGeneradorPort.ContextoGeneracion;
import com.dian.prep.domain.port.out.ValidadorPreguntaPort;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validador de preguntas basado en un SEGUNDO modelo de IA, independiente del
 * redactor. Revisa el lote con criterio crítico y dictamina, ítem por ítem, si
 * es apto para estudio (sin reescribir; a lo sumo corrige la clave).
 *
 * <p>La validación es POR LOTE: una sola llamada al proveedor por lote. Esto
 * respeta los límites del free tier de Gemini (~10 RPM / ~250 RPD): revisar
 * pregunta por pregunta agotaría el cupo diario enseguida.</p>
 *
 * <p>Selección del proveedor validador (configurable con {@code app.ai.validacion}):
 * para que la revisión sea independiente se prefiere un proveedor distinto al
 * redactor. Por defecto usa Gemini; si no está configurado, cae a Groq con un
 * modelo distinto al redactor. Si no hay ningún proveedor disponible, o si la
 * llamada falla, el validador aprueba por defecto (fail-open) para no bloquear
 * la generación.</p>
 */
@Component
public class ValidadorPreguntaAdapter implements ValidadorPreguntaPort {

    private static final Logger log = LoggerFactory.getLogger(ValidadorPreguntaAdapter.class);

    private final IaProperties props;
    private final ObjectMapper objectMapper;
    private final CupoIaRepository cupoIa;
    private final RestClient restClient;

    public ValidadorPreguntaAdapter(IaProperties props, ObjectMapper objectMapper, CupoIaRepository cupoIa) {
        this.props        = props;
        this.objectMapper = objectMapper;
        this.cupoIa       = cupoIa;
        // Timeouts explícitos para que un cuelgue del validador no bloquee la
        // generación: ante fallo de red se cae a fail-open (aprueba pendientes).
        this.restClient   = RestClient.builder()
                .requestFactory(httpRequestFactory())
                .build();
    }

    private static SimpleClientHttpRequestFactory httpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(15));
        factory.setReadTimeout(Duration.ofSeconds(90));
        return factory;
    }

    @Override
    public List<ResultadoValidacion> validarLote(List<Pregunta> preguntas, ContextoGeneracion ctx) {
        if (preguntas == null || preguntas.isEmpty()) {
            return List.of();
        }

        // 1) Verificación estructural (determinista, sin red). Lo que no pasa aquí se
        //    rechaza de una vez y NO se envía al validador remoto.
        List<ResultadoValidacion> resultados = new ArrayList<>(preguntas.size());
        List<Integer> indicesAEvaluar = new ArrayList<>();
        for (int i = 0; i < preguntas.size(); i++) {
            String fallo = revisarEstructura(preguntas.get(i));
            if (fallo != null) {
                resultados.add(ResultadoValidacion.rechazar(fallo));
            } else {
                resultados.add(null); // pendiente de dictamen remoto
                indicesAEvaluar.add(i);
            }
        }

        if (indicesAEvaluar.isEmpty()) {
            return resultados;
        }

        // 2) Dictamen remoto en UNA sola llamada para todo el lote.
        try {
            List<Pregunta> candidatas = new ArrayList<>(indicesAEvaluar.size());
            for (int idx : indicesAEvaluar) candidatas.add(preguntas.get(idx));

            String respuesta = llamarValidador(construirPrompt(candidatas, ctx));
            if (respuesta == null || respuesta.isBlank()) {
                return aprobarPendientes(resultados, "sin proveedor/respuesta vacía del validador");
            }
            aplicarVeredictos(respuesta, candidatas, indicesAEvaluar, resultados);
        } catch (Exception e) {
            log.warn("Validador IA falló, se aprueban las pendientes (fail-open). Causa: {}", e.getMessage());
            return aprobarPendientes(resultados, "fallo del validador");
        }

        // 3) Cualquier posición que quedara sin veredicto (JSON incompleto) se aprueba (fail-open).
        for (int i = 0; i < resultados.size(); i++) {
            if (resultados.get(i) == null) resultados.set(i, ResultadoValidacion.aprobar());
        }
        return resultados;
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
        // Cada ítem debe traer el texto de lectura contextualizado. En el formato actual
        // el texto se marca con "ENUNCIADO:" (y la pregunta con "PREGUNTA:"); se acepta
        // también "CASO:" por compatibilidad con material generado con el formato previo.
        String txt = p.getEnunciado().toUpperCase();
        if (!txt.contains("ENUNCIADO") && !txt.contains("CASO"))
            return "el ítem no presenta un enunciado/caso contextualizado";
        return null;
    }

    // --- Prompt del validador (crítico y estricto, por lote) ---

    private String construirPrompt(List<Pregunta> candidatas, ContextoGeneracion ctx) {
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < candidatas.size(); i++) {
            Pregunta p = candidatas.get(i);
            items.append("""
                    ---
                    ÍTEM %d
                    Enunciado: %s
                    Opciones:
                    A) %s
                    B) %s
                    C) %s
                    Clave propuesta: %s
                    Justificación de la correcta: %s
                    """.formatted(
                    i,
                    p.getEnunciado(),
                    p.getOpcionA(),
                    p.getOpcionB(),
                    p.getOpcionC(),
                    p.getRespuestaCorrecta(),
                    nvl(p.getJustificacionCorrecta(), "")));
        }

        return """
                Actúa como revisor experto y MUY CRÍTICO de ítems de pruebas de la CNSC
                (concurso de méritos "%s", indicador "%s", tipo de prueba %s).
                Debes dictaminar, para CADA ítem, si es APTO para que un aspirante estudie
                con él. Sé estricto: ante cualquier duda, repruébalo. NO reescribas los ítems;
                solo dictamina (y, si corresponde, corrige la clave).

                Verifica en cada ítem TODOS estos criterios:
                1. La clave propuesta es inequívocamente la MEJOR y única solución efectiva.
                   Si otra opción es igual o más correcta, repruébalo (o corrige la clave).
                2. Las otras dos opciones son claramente subóptimas, pero plausibles (no absurdas).
                3. El enunciado y el caso son claros, sin ambigüedad, sin dobles negaciones ni pistas.
                4. El contenido es técnicamente correcto y pertinente al indicador evaluado.
                5. Si cita una norma o artículo, debe ser real y estar bien atribuido; si parece
                   inventado o mal citado, repruébalo.
                6. Hay una sola respuesta correcta entre A, B y C.

                ÍTEMS A REVISAR (hay %d):
                %s

                Responde EXCLUSIVAMENTE con un ARRAY JSON válido (sin markdown, sin texto extra),
                con un objeto por ítem y en el MISMO orden recibido:
                [
                  { "indice": 0, "apta": true|false, "claveCorrecta": "A|B|C", "motivo": "razón breve" }
                ]
                Donde "indice" es el número del ÍTEM, "apta" tu veredicto, y "claveCorrecta" la
                opción que TÚ consideras correcta tras tu análisis (coincida o no con la propuesta).
                Devuelve exactamente %d objetos.
                """.formatted(
                nvl(ctx.nombreConcurso(), "convocatoria pública"),
                nvl(ctx.nombreIndicador(), "indicador"),
                ctx.tipoPrueba(),
                candidatas.size(),
                items.toString(),
                candidatas.size());
    }

    // --- Interpretación del veredicto por lote ---

    private void aplicarVeredictos(String contenido, List<Pregunta> candidatas,
                                   List<Integer> indicesOriginales, List<ResultadoValidacion> resultados) {
        JsonNode raiz;
        try {
            raiz = objectMapper.readTree(extraerJsonArray(contenido));
        } catch (Exception e) {
            log.warn("No se pudo interpretar el veredicto del validador: {}", e.getMessage());
            return; // deja pendientes -> se aprueban al final (fail-open)
        }
        if (!raiz.isArray()) {
            log.warn("El validador no devolvió un array JSON; se aprueban las pendientes (fail-open).");
            return;
        }

        for (JsonNode nodo : raiz) {
            int localIdx = nodo.path("indice").asInt(-1);
            if (localIdx < 0 || localIdx >= candidatas.size()) continue;

            int globalIdx = indicesOriginales.get(localIdx);
            boolean apta = nodo.path("apta").asBoolean(false);
            String motivo = nodo.path("motivo").asText("sin motivo");
            Pregunta.OpcionRespuesta claveValidador = parseOpcion(nodo.path("claveCorrecta").asText(null));
            Pregunta p = candidatas.get(localIdx);

            if (!apta) {
                resultados.set(globalIdx, ResultadoValidacion.rechazar(motivo));
            } else if (claveValidador != null && claveValidador != p.getRespuestaCorrecta()) {
                resultados.set(globalIdx, ResultadoValidacion.aprobarConClave(claveValidador));
            } else {
                resultados.set(globalIdx, ResultadoValidacion.aprobar());
            }
        }
    }

    private List<ResultadoValidacion> aprobarPendientes(List<ResultadoValidacion> resultados, String motivo) {
        log.warn("Validador no concluyente ({}). Se aprueban las pendientes por defecto.", motivo);
        for (int i = 0; i < resultados.size(); i++) {
            if (resultados.get(i) == null) resultados.set(i, ResultadoValidacion.aprobar());
        }
        return resultados;
    }

    // --- Selección y llamada al proveedor validador ---

    private String llamarValidador(String prompt) {
        String proveedor = props.getValidacion().getProvider();
        boolean prefiereGemini = "gemini".equalsIgnoreCase(proveedor);

        // Resuelve el proveedor efectivo (prefiere el configurado; cae al disponible).
        boolean usarGemini;
        if (prefiereGemini && props.geminiConfigurado())      usarGemini = true;
        else if (!prefiereGemini && props.groqConfigurado())  usarGemini = false;
        else if (props.geminiConfigurado())                   usarGemini = true;
        else if (props.groqConfigurado())                     usarGemini = false;
        else return null; // ningún proveedor -> fail-open en validarLote()

        String modelo = usarGemini ? props.getGemini().getModel() : props.getValidacion().getGroqModel();

        // Respeta el cupo diario del validador: si se agotó, no se llama (fail-open).
        LocalDate hoy = LocalDate.now();
        if (!cupoIa.hayCupo(hoy, modelo, props.getValidacion().getLimiteDiario())) {
            log.warn("Cupo diario del validador ({}) agotado; se aprueban las pendientes.", modelo);
            return null;
        }

        String respuesta = usarGemini ? llamarGemini(prompt) : llamarGroq(prompt, modelo);
        cupoIa.registrarUso(hoy, modelo, 1); // una llamada por lote
        return respuesta;
    }

    private String llamarGroq(String prompt, String model) {
        IaProperties.Groq g = props.getGroq();
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 2000,
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

    private String extraerJsonArray(String contenido) {
        int inicio = contenido.indexOf('[');
        int fin = contenido.lastIndexOf(']');
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
