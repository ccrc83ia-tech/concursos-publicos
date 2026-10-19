package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.Recomendacion;

/**
 * Puerto de salida hacia un proveedor de IA para producir un plan de estudio
 * personalizado a partir del desempeño del usuario en un ítem.
 */
public interface GenerarRecomendacionPort {

    /**
     * Genera una recomendación de estudio para un ítem con bajo desempeño.
     *
     * @param contexto datos del ítem, concurso y desempeño que contextualizan el prompt
     * @return recomendación con temas a reforzar, referencias y tiempo estimado
     */
    Recomendacion generar(ContextoRecomendacion contexto);

    /**
     * Contexto independiente del proveedor para construir el prompt de estudio.
     */
    record ContextoRecomendacion(
            String  nombreIndicador,
            String  tipoPrueba,
            String  nombreConcurso,
            int     preguntasCorrectas,
            int     totalPreguntas,
            double  puntajePorcentual,
            String  nivelDominio
    ) {}
}
