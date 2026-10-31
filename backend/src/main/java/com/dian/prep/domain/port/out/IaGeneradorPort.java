package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.model.Pregunta;

import java.util.List;

/**
 * Puerto de salida hacia un proveedor de IA generativa (Groq, Gemini, etc.).
 * El dominio pide un lote de preguntas sin conocer qué proveedor las produce.
 */
public interface IaGeneradorPort {

    /**
     * Genera un lote de preguntas para un indicador.
     *
     * @param contexto   datos del concurso e indicador que contextualizan el prompt
     * @param cantidad   cuántas preguntas generar en esta llamada (lote)
     * @return lista de preguntas generadas (puede ser menor que {@code cantidad}
     *         si el modelo devuelve menos); nunca null
     */
    List<Pregunta> generarLote(ContextoGeneracion contexto, int cantidad);

    /**
     * Igual que {@link #generarLote}, pero usando el modelo REDACTOR PREMIUM (de mayor
     * calidad, p. ej. Gemini 3.x) en lugar del provisional. Lo usa el job de
     * mantenimiento del banco para elevar la calidad del material. Las preguntas que
     * produce se marcan con origen PREMIUM. Por defecto delega en {@link #generarLote}.
     */
    default List<Pregunta> generarLotePremium(ContextoGeneracion contexto, int cantidad) {
        return generarLote(contexto, cantidad);
    }

    /**
     * Contexto independiente del proveedor. Incluye los datos del concurso para
     * que el prompt no quede cableado a ninguna entidad concreta (sirve para
     * cualquier convocatoria del SIMO).
     */
    record ContextoGeneracion(
            Item.TipoPrueba tipoPrueba,
            String  nombreIndicador,
            Integer nivelCompetencia,   // null si no aplica
            String  nombreConcurso,
            String  entidadODenominacion, // p. ej. "Gestor III" o la entidad; puede ser null
            String  ejeTematico          // descripción/ejes del indicador para anclar el prompt; puede ser null
    ) {}
}
