package com.dian.prep.infrastructure.adapter.out.ai;

import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.port.out.IaGeneradorPort.ContextoGeneracion;
import org.springframework.stereotype.Component;

/**
 * Construye el prompt para el LLM según el tipo de prueba. El prompt se
 * parametriza con el contexto del concurso (nombre, denominación), por lo que
 * sirve para cualquier convocatoria del SIMO y no queda atado a una entidad.
 */
@Component
public class PromptBuilder {

    /** Instrucciones de formato de salida JSON, comunes a todos los tipos. */
    private static final String FORMATO_JSON = """
            Responde EXCLUSIVAMENTE con un array JSON válido (sin texto adicional, sin markdown).
            Cada elemento debe tener exactamente esta forma:
            {
              "enunciado": "texto de la pregunta",
              "opcionA": "...", "opcionB": "...", "opcionC": "...", "opcionD": "...",
              "respuestaCorrecta": "A|B|C|D",
              "justificacionCorrecta": "por qué esa opción es correcta",
              "justificacionIncorrectas": {"A": "...", "B": "...", "C": "...", "D": "..."},
              "referenciaNormativa": "norma/artículo/manual de referencia, o cadena vacía",
              "nivelDificultad": "BASICO|INTERMEDIO|AVANZADO"
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

    private String promptFuncional(ContextoGeneracion ctx, int cantidad, String contexto) {
        return """
                Genera %d preguntas de selección múltiple (única respuesta, 4 opciones A-D)
                sobre el indicador técnico "%s".
                %s.
                Las preguntas deben ser conceptuales y aplicadas, con dificultad variada.
                Incluye referencia normativa o técnica cuando aplique.
                %s
                """.formatted(cantidad, ctx.nombreIndicador(), contexto, FORMATO_JSON);
    }

    private String promptComportamental(ContextoGeneracion ctx, int cantidad, String contexto) {
        String nivel = ctx.nivelCompetencia() != null ? " nivel " + ctx.nivelCompetencia() : "";
        return """
                Genera %d preguntas de competencias comportamentales sobre "%s"%s.
                %s.
                Formato: un escenario laboral realista en el servicio público + 4 conductas posibles.
                Una conducta es claramente la más efectiva para la competencia y nivel evaluados.
                %s
                """.formatted(cantidad, ctx.nombreIndicador(), nivel, contexto, FORMATO_JSON);
    }

    private String promptIntegridad(ContextoGeneracion ctx, int cantidad, String contexto) {
        return """
                Genera %d preguntas de integridad sobre el valor "%s".
                %s.
                Formato: un dilema ético en contexto de función pública + 4 decisiones posibles.
                Una decisión refleja mejor el valor evaluado.
                %s
                """.formatted(cantidad, ctx.nombreIndicador(), contexto, FORMATO_JSON);
    }

    private String nvl(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    /** Marca si el indicador es de un tipo que requiere nivel. */
    public boolean requiereNivel(Item.TipoPrueba tipo) {
        return tipo == Item.TipoPrueba.COMPORTAMENTAL;
    }
}
