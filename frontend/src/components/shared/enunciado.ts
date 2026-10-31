/**
 * Las preguntas se guardan con el texto combinado:
 *   "ENUNCIADO: <texto de lectura del caso>. PREGUNTA: <pregunta específica>"
 * Varias preguntas comparten el mismo ENUNCIADO (3–4 por caso). Para no repetir
 * el texto de lectura en pantalla, aquí lo separamos en dos partes.
 */
export interface PartesEnunciado {
  /** Texto de lectura del caso (sin el prefijo "ENUNCIADO:"). Puede ser "". */
  lectura: string
  /** Pregunta concreta (sin el prefijo "PREGUNTA:"). Nunca vacío si había texto. */
  pregunta: string
}

/**
 * Separa el campo combinado en su parte de lectura y su pregunta. Es tolerante:
 * - acepta "ENUNCIADO:"/"CASO:" como marcador de lectura y "PREGUNTA:" como separador;
 * - si no hay marcadores, devuelve todo como pregunta (lectura vacía).
 */
export function separarEnunciado(texto: string | null | undefined): PartesEnunciado {
  const t = (texto ?? '').trim()
  if (!t) return { lectura: '', pregunta: '' }

  // Localiza el separador de pregunta (case-insensitive).
  const m = t.match(/\bPREGUNTA\s*:?/i)
  if (!m || m.index === undefined) {
    // Sin marcador de pregunta: quita un posible prefijo ENUNCIADO/CASO y usa todo como pregunta.
    return { lectura: '', pregunta: quitarPrefijoLectura(t) }
  }

  const lecturaRaw = t.slice(0, m.index)
  const preguntaRaw = t.slice(m.index + m[0].length)
  return {
    lectura: quitarPrefijoLectura(lecturaRaw).replace(/[.\s]+$/, '').trim(),
    pregunta: preguntaRaw.trim(),
  }
}

/** Clave para agrupar preguntas del mismo caso (su texto de lectura normalizado). */
export function claveEnunciado(texto: string | null | undefined): string {
  return separarEnunciado(texto).lectura.toLowerCase().replace(/\s+/g, ' ').trim()
}

function quitarPrefijoLectura(s: string): string {
  return s.replace(/^\s*(ENUNCIADO|CASO)\s*:?\s*/i, '').trim()
}
