package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.IaGeneradorPort.ContextoGeneracion;

import java.util.List;

/**
 * Puerto de salida hacia un segundo modelo de IA que actúa como VALIDADOR
 * (revisor crítico) de las preguntas producidas por el modelo redactor.
 *
 * <p>Patrón "generador + verificador": el redactor (Groq gpt-oss-120b) propone
 * los ítems y el validador —un proveedor/modelo distinto e independiente
 * (Gemini 2.5 Flash)— decide si cada ítem es apto para estudio. Solo las
 * preguntas aprobadas se persisten. El objetivo es blindar la calidad frente a
 * alucinaciones (clave incorrecta, opciones ambiguas, normas inventadas),
 * crítico cuando el material se usa para preparar una prueba real.</p>
 *
 * <p>La validación es POR LOTE (una sola llamada al proveedor por lote) para
 * respetar los límites del free tier de Gemini (~10 RPM / ~250 RPD): revisar
 * pregunta por pregunta agotaría el cupo diario enseguida.</p>
 */
public interface ValidadorPreguntaPort {

    /**
     * Revisa un lote de preguntas en una sola interacción y devuelve un veredicto
     * por cada una, en el mismo orden que la lista de entrada.
     *
     * @param preguntas lote de preguntas candidatas generadas por el redactor
     * @param contexto  contexto del concurso/indicador que enmarca las preguntas
     * @return lista de veredictos, uno por pregunta y en el mismo orden
     */
    List<ResultadoValidacion> validarLote(List<Pregunta> preguntas, ContextoGeneracion contexto);

    /**
     * Veredicto del validador para una pregunta.
     *
     * @param aprobada        true si la pregunta es apta para estudio
     * @param motivo          razón del veredicto (para log/diagnóstico)
     * @param claveCorregida  clave correcta según el validador (A/B/C) o null si no corrige
     */
    record ResultadoValidacion(boolean aprobada, String motivo, Pregunta.OpcionRespuesta claveCorregida) {

        public static ResultadoValidacion aprobar() {
            return new ResultadoValidacion(true, "OK", null);
        }

        public static ResultadoValidacion aprobarConClave(Pregunta.OpcionRespuesta clave) {
            return new ResultadoValidacion(true, "OK (clave corregida)", clave);
        }

        public static ResultadoValidacion rechazar(String motivo) {
            return new ResultadoValidacion(false, motivo, null);
        }
    }
}
