package com.dian.prep.infrastructure.adapter.out.ai;

import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.port.out.IaGeneradorPort.ContextoGeneracion;
import org.springframework.stereotype.Component;

/**
 * Construye el prompt para el LLM siguiendo la metodología de diseño de ítems
 * de la CNSC (Comisión Nacional del Servicio Civil) para concursos de méritos:
 * ítems basados en situaciones problema, clasificados por proceso cognitivo
 * (evocación / comprensión / aplicación) y anclados a ejes temáticos.
 *
 * <p>El prompt se parametriza con el contexto del concurso (nombre, denominación,
 * eje temático del indicador), por lo que sirve para cualquier convocatoria del
 * SIMO y no queda atado a una entidad concreta.</p>
 */
@Component
public class PromptBuilder {

    /**
     * Lineamientos metodológicos comunes a todos los tipos de prueba, alineados
     * con cómo construye la CNSC los ítems de selección múltiple con única respuesta.
     */
    private static final String LINEAMIENTOS_CNSC = """
            Diseña los ítems siguiendo la metodología de la CNSC para pruebas escritas:
            - Cada ítem evalúa UNO de estos procesos cognitivos: EVOCACION (recordar un
              concepto/norma/dato), COMPRENSION (interpretar o explicar) o APLICACION
              (resolver una situación concreta). Prioriza COMPRENSION y APLICACION.
            - Redacta un enunciado contextualizado (situación o caso realista del ejercicio
              de la función pública), seguido de una pregunta clara y precisa.
            - 4 opciones (A-D), una sola correcta. Los distractores deben ser plausibles y
              del mismo nivel de especificidad que la correcta (nada de opciones absurdas,
              ni "todas/ninguna de las anteriores").
            - Evita pistas gramaticales, dobles negaciones y ambigüedad.
            - Varía el proceso cognitivo y la dificultad a lo largo del conjunto.
            """;

    /** Instrucciones de formato de salida JSON, comunes a todos los tipos. */
    private static final String FORMATO_JSON = """
            Responde EXCLUSIVAMENTE con un array JSON válido (sin texto adicional, sin markdown).
            Cada elemento debe tener exactamente esta forma:
            {
              "enunciado": "situación/caso + pregunta",
              "opcionA": "...", "opcionB": "...", "opcionC": "...", "opcionD": "...",
              "respuestaCorrecta": "A|B|C|D",
              "justificacionCorrecta": "por qué esa opción es correcta",
              "justificacionIncorrectas": {"A": "...", "B": "...", "C": "...", "D": "..."},
              "referenciaNormativa": "norma/artículo/manual de referencia, o cadena vacía",
              "nivelDificultad": "BASICO|INTERMEDIO|AVANZADO",
              "procesoCognitivo": "EVOCACION|COMPRENSION|APLICACION"
            }
            En justificacionIncorrectas incluye solo las opciones distintas de la correcta.
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

    private String promptFuncional(ContextoGeneracion ctx, int cantidad, String contexto) {
        return """
                Genera %d preguntas de competencias FUNCIONALES (conocimientos técnicos
                específicos del cargo) sobre el indicador "%s".
                %s.%s
                %s
                %s
                """.formatted(cantidad, ctx.nombreIndicador(), contexto, ejeTematico(ctx),
                LINEAMIENTOS_CNSC, FORMATO_JSON);
    }

    private String promptComportamental(ContextoGeneracion ctx, int cantidad, String contexto) {
        String nivel = ctx.nivelCompetencia() != null ? " nivel jerárquico " + ctx.nivelCompetencia() : "";
        return """
                Genera %d preguntas de competencias COMPORTAMENTALES sobre "%s"%s.
                %s.%s
                Formato: un escenario laboral realista en el servicio público + 4 conductas posibles.
                Una conducta es claramente la más efectiva para la competencia y el nivel evaluados;
                las demás son razonables pero subóptimas. Evalúa principalmente APLICACION.
                %s
                %s
                """.formatted(cantidad, ctx.nombreIndicador(), nivel, contexto, ejeTematico(ctx),
                LINEAMIENTOS_CNSC, FORMATO_JSON);
    }

    private String promptIntegridad(ContextoGeneracion ctx, int cantidad, String contexto) {
        return """
                Genera %d preguntas de INTEGRIDAD sobre el valor "%s".
                %s.%s
                Formato: un dilema ético en contexto de función pública + 4 decisiones posibles.
                Una decisión refleja mejor el valor evaluado; las demás representan conductas
                plausibles pero menos íntegras. Evalúa principalmente APLICACION.
                %s
                %s
                """.formatted(cantidad, ctx.nombreIndicador(), contexto, ejeTematico(ctx),
                LINEAMIENTOS_CNSC, FORMATO_JSON);
    }

    private String nvl(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    /** Marca si el indicador es de un tipo que requiere nivel. */
    public boolean requiereNivel(Item.TipoPrueba tipo) {
        return tipo == Item.TipoPrueba.COMPORTAMENTAL;
    }
}
