package com.dian.prep.infrastructure.adapter.out.ai;

import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.port.out.IaGeneradorPort.ContextoGeneracion;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Map;

/**
 * Construye el prompt para el LLM siguiendo ESTRICTAMENTE la metodología de la
 * Guía de Orientación al Aspirante (GOA) de la CNSC para el Proceso de Selección
 * DIAN 2676, generalizable a cualquier convocatoria del SIMO.
 *
 * <h2>Reglas oficiales que se aplican</h2>
 * <ul>
 *   <li><b>Formato de ítem = Prueba de Juicio Situacional (PJS):</b> cada ítem
 *       parte de un CASO (situación hipotética del contexto laboral del empleo),
 *       seguido de un ENUNCIADO (la tarea a resolver) y <b>tres (3) opciones de
 *       respuesta</b> (A, B, C) de selección múltiple con <b>única respuesta</b>.
 *       Solo UNA opción presenta la solución efectiva al planteamiento.</li>
 *   <li><b>Varios enunciados por caso:</b> cada CASO se reutiliza para 3 o 4
 *       ENUNCIADOS distintos, cada uno con sus propias 3 opciones y su clave.</li>
 *   <li><b>Clave + justificaciones:</b> cada ítem indica la clave y justifica por
 *       qué la correcta lo es y por qué cada incorrecta no lo es, anclando a norma
 *       o fuente cuando corresponda.</li>
 * </ul>
 *
 * <p>El prompt se parametriza con el contexto del concurso (nombre, denominación,
 * eje temático del indicador), por lo que sirve para cualquier convocatoria del
 * SIMO y no queda atado a una entidad concreta.</p>
 *
 * <h2>Nota sobre comportamentales e integridad</h2>
 * <p>La GOA aplica a comportamentales/integridad un autoinforme tipo Likert "sin
 * respuesta correcta". Ese formato es incompatible con el motor de simulacros de
 * esta aplicación, que califica contra una respuesta correcta y calcula brechas.
 * Por eso, para estos tipos se usa un Juicio Situacional conductual (escenario +
 * conductas donde la más efectiva es la clave), que conserva la lógica situacional
 * de la CNSC y, a la vez, es evaluable por el sistema.</p>
 */
@Component
public class PromptBuilder {

    /**
     * Lineamientos metodológicos comunes a todos los tipos de prueba, alineados
     * con el formato de Prueba de Juicio Situacional (PJS) de la CNSC.
     */
    private static final String LINEAMIENTOS_PJS = """
            Diseña los ítems como Juicio Situacional de la CNSC, con material de lectura:
            - Un ENUNCIADO es un texto de lectura (caso): describe una situación, evento o
              suceso hipotético realista del contexto laboral del empleo, con los datos
              suficientes para poder formular varias preguntas sobre él.
            - De CADA ENUNCIADO se desprenden entre 3 y 4 PREGUNTAS distintas que se resuelven
              leyendo ese mismo texto. Cada pregunta plantea una tarea concreta frente a la
              cual el aspirante debe decidir la acción más adecuada según el enunciado.
            - Cada PREGUNTA tiene exactamente TRES (3) opciones de respuesta (A, B, C). Deja la
              opción D vacía (""). Una sola opción es correcta: la única que resuelve de forma
              efectiva lo planteado.
            - Las otras dos opciones son plausibles y del mismo nivel de especificidad que la
              correcta (acciones razonables pero subóptimas, incompletas o inoportunas).
              Nada de opciones absurdas, ni "todas/ninguna de las anteriores".
            - Las preguntas de un mismo enunciado deben ser diferentes entre sí (no repetir la
              misma idea), abarcando distintos aspectos del texto.
            - Evita pistas gramaticales, dobles negaciones y ambigüedad.
            - Prioriza los procesos cognitivos COMPRENSION y APLICACION sobre EVOCACION.
            - Varía la dificultad a lo largo del conjunto.
            """;

    /** Instrucciones de formato de salida JSON, comunes a todos los tipos. */
    private static final String FORMATO_JSON = """
            Responde EXCLUSIVAMENTE con un array JSON válido (sin texto adicional, sin markdown).
            Genera un objeto por cada PREGUNTA (no por cada enunciado). Todas las preguntas que
            pertenecen a un mismo ENUNCIADO deben repetir el texto del enunciado al inicio del
            campo "enunciado", con esta estructura exacta:
            "ENUNCIADO: <texto de lectura del caso>. PREGUNTA: <texto de la pregunta>"
            Cada objeto debe tener exactamente esta forma:
            {
              "enunciado": "ENUNCIADO: ... . PREGUNTA: ...",
              "opcionA": "...", "opcionB": "...", "opcionC": "...", "opcionD": "",
              "respuestaCorrecta": "A|B|C",
              "justificacionCorrecta": "Es correcta. <por qué resuelve la pregunta>",
              "justificacionIncorrectas": {"A": "Es incorrecta. ...", "B": "...", "C": "..."},
              "referenciaNormativa": "norma/artículo/manual vigente de referencia, o cadena vacía",
              "nivelDificultad": "BASICO|INTERMEDIO|AVANZADO",
              "procesoCognitivo": "EVOCACION|COMPRENSION|APLICACION"
            }
            Reglas del JSON:
            - "opcionD" SIEMPRE es cadena vacía "" (el formato tiene solo 3 opciones).
            - En "justificacionIncorrectas" incluye solo las dos opciones distintas de la correcta.
            - Mantén agrupadas y consecutivas en el array todas las preguntas de un mismo enunciado.
            - El texto tras "ENUNCIADO:" debe ser IDÉNTICO en las preguntas que lo comparten.
            """;

    public String construir(ContextoGeneracion ctx, int cantidad) {
        String contexto = contextoConcurso(ctx);
        return switch (ctx.tipoPrueba()) {
            case FUNCIONAL      -> promptFuncional(ctx, cantidad, contexto);
            case COMPORTAMENTAL -> promptComportamental(ctx, cantidad, contexto);
            case INTEGRIDAD     -> promptIntegridad(ctx, cantidad, contexto);
        };
    }

    private String contextoConcurso(ContextoGeneracion ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("Concurso de méritos: ").append(nvl(ctx.nombreConcurso(), "convocatoria pública"));
        if (ctx.entidadODenominacion() != null && !ctx.entidadODenominacion().isBlank()) {
            sb.append(", cargo/denominación: ").append(ctx.entidadODenominacion());
        }
        return sb.toString();
    }

    /** Añade el eje temático (descripción del indicador) si el usuario lo aportó. */
    private String ejeTematico(ContextoGeneracion ctx) {
        if (ctx.ejeTematico() == null || ctx.ejeTematico().isBlank()) return "";
        return "\nCíñete a estos ejes temáticos del indicador: " + ctx.ejeTematico().trim() + ".";
    }

    // ------------------------------------------------------------------
    // Anclaje de contenido para DIAN 2676 - Gestor III (Empleo 236861).
    //
    // El sistema es genérico, pero cuando el usuario NO aporta un eje temático,
    // estos textos dan a la IA una definición fiel de la competencia o el valor
    // para que los escenarios sean realistas y acordes al concurso. Si el eje
    // temático viene informado, ese tiene prioridad y estas definiciones solo
    // complementan. El match se hace por nombre normalizado (sin tildes/nivel).
    // ------------------------------------------------------------------

    /** Definiciones de las 10 competencias comportamentales de Gestor III (Resolución DIAN 065 de 2024). */
    private static final Map<String, String> DEF_COMPORTAMENTAL = Map.ofEntries(
            Map.entry("ADAPTABILIDAD",
                    "capacidad para ajustarse y responder eficazmente ante cambios de contexto, prioridades, " +
                    "procesos o tecnología, manteniendo el desempeño"),
            Map.entry("COMPORTAMIENTO ETICO",
                    "actuar de forma coherente con los principios y el Código de Integridad de la entidad, " +
                    "tomando decisiones alineadas con lo correcto aun ante presiones"),
            Map.entry("COMUNICACION EFECTIVA",
                    "transmitir y recibir información de manera clara, oportuna y respetuosa, adaptando el " +
                    "mensaje al interlocutor y asegurando la comprensión"),
            Map.entry("INNOVACION",
                    "proponer e implementar mejoras, ideas o soluciones nuevas que agreguen valor a los " +
                    "procesos y servicios de la entidad"),
            Map.entry("ORIENTACION AL LOGRO",
                    "orientar el esfuerzo al cumplimiento de metas y resultados con estándares de calidad, " +
                    "asumiendo responsabilidad por lo encomendado"),
            Map.entry("ORIENTACION AL USUARIO Y AL CIUDADANO",
                    "disponer la gestión a satisfacer las necesidades de usuarios y ciudadanos con amabilidad, " +
                    "respeto y soluciones efectivas"),
            Map.entry("TRABAJO EN EQUIPO",
                    "colaborar y coordinarse con otros en función de objetivos comunes, aportando y apoyando " +
                    "al grupo por encima del interés individual"));

    /** Definiciones de los 5 valores del Código de Integridad del servicio público. */
    private static final Map<String, String> DEF_INTEGRIDAD = Map.ofEntries(
            Map.entry("COMPROMISO",
                    "disposición a servir con dedicación a la ciudadanía y a los fines de la entidad, " +
                    "poniendo lo mejor de sí en el ejercicio de la función pública"),
            Map.entry("DILIGENCIA",
                    "cumplir las funciones y tareas de la mejor manera posible, con atención, cuidado y " +
                    "oportunidad, optimizando el uso de los recursos públicos"),
            Map.entry("HONESTIDAD",
                    "actuar siempre con fundamento en la verdad, con rectitud y transparencia, cuidando los " +
                    "bienes y recursos públicos"),
            Map.entry("JUSTICIA",
                    "actuar con imparcialidad garantizando los derechos de las personas, con equidad e " +
                    "igualdad y sin discriminación"),
            Map.entry("RESPETO",
                    "reconocer, valorar y tratar de manera digna a todas las personas, con sus diferencias, " +
                    "necesidades, derechos y particularidades"));

    /** Normaliza un nombre de indicador: mayúsculas, sin tildes y sin el sufijo "NIVEL n". */
    private String clave(String nombre) {
        if (nombre == null) return "";
        String s = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")        // quita tildes
                .toUpperCase()
                .replaceAll("\\s+NIVEL\\s+\\d+", "") // quita "NIVEL n"
                .replaceAll("[^A-Z ]", " ")       // quita signos
                .replaceAll("\\s+", " ")
                .trim();
        return s;
    }

    /** Devuelve una línea de anclaje con la definición de la competencia/valor, o "" si no se conoce. */
    private String anclaje(Map<String, String> defs, String nombreIndicador, String etiqueta) {
        String def = defs.get(clave(nombreIndicador));
        if (def == null) return "";
        return "\nPara esta " + etiqueta + ", entiéndela así: " + def + ".";
    }

    /** Indica el total de enunciados pedidos y cómo repartirlos en casos. */
    private String instruccionCantidad(int enunciados) {
        return """
                Genera %d ENUNCIADOS (textos de lectura). De CADA enunciado desprende entre 3 y 4
                PREGUNTAS que se resuelvan con ese mismo texto. Es decir, el array tendrá entre
                %d y %d objetos de pregunta en total, agrupados por enunciado.
                """.formatted(enunciados, enunciados * 3, enunciados * 4);
    }

    private String promptFuncional(ContextoGeneracion ctx, int cantidad, String contexto) {
        return """
                Vas a crear ítems de competencias FUNCIONALES (conocimientos técnicos
                aplicados del cargo) para el indicador "%s".
                %s.%s
                %s
                Los casos deben reflejar escenarios cercanos a la cotidianidad y al contexto
                real de la entidad, y evaluar la aplicación del conocimiento técnico del indicador.
                %s
                %s
                """.formatted(ctx.nombreIndicador(), contexto, ejeTematico(ctx),
                instruccionCantidad(cantidad), LINEAMIENTOS_PJS, FORMATO_JSON);
    }

    private String promptComportamental(ContextoGeneracion ctx, int cantidad, String contexto) {
        String nivel = ctx.nivelCompetencia() != null
                ? " en su nivel de desarrollo " + ctx.nivelCompetencia()
                  + " (en la escala de la entidad el nivel 1 es el mínimo y el 4 el mayor grado de desarrollo)"
                : "";
        String def = anclaje(DEF_COMPORTAMENTAL, ctx.nombreIndicador(), "competencia");
        return """
                Vas a crear ítems de competencias COMPORTAMENTALES sobre "%s"%s.
                %s.%s%s
                %s
                Cada ENUNCIADO plantea un escenario laboral realista del servicio público en la
                entidad. En cada PREGUNTA, las 3 opciones son conductas posibles: una es claramente
                la más efectiva para la competencia y el nivel evaluados; las otras dos son
                razonables pero subóptimas. Ajusta la exigencia de la conducta correcta al nivel
                indicado. Evalúa principalmente APLICACION.
                %s
                %s
                """.formatted(ctx.nombreIndicador(), nivel, contexto, ejeTematico(ctx), def,
                instruccionCantidad(cantidad), LINEAMIENTOS_PJS, FORMATO_JSON);
    }

    private String promptIntegridad(ContextoGeneracion ctx, int cantidad, String contexto) {
        String def = anclaje(DEF_INTEGRIDAD, ctx.nombreIndicador(), "valor");
        return """
                Vas a crear ítems de INTEGRIDAD sobre el valor "%s".
                %s.%s%s
                %s
                Cada ENUNCIADO plantea un dilema ético en el ejercicio de la función pública en la
                entidad. En cada PREGUNTA, las 3 opciones son decisiones posibles: una refleja mejor
                el valor evaluado; las otras dos son plausibles pero menos íntegras. Evalúa
                principalmente APLICACION.
                %s
                %s
                """.formatted(ctx.nombreIndicador(), contexto, ejeTematico(ctx), def,
                instruccionCantidad(cantidad), LINEAMIENTOS_PJS, FORMATO_JSON);
    }

    private String nvl(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    /** Marca si el indicador es de un tipo que requiere nivel. */
    public boolean requiereNivel(Item.TipoPrueba tipo) {
        return tipo == Item.TipoPrueba.COMPORTAMENTAL;
    }
}
