import client from './client'
import type { Recomendacion } from '@/types'

/** Genera (con IA) recomendaciones de estudio para las brechas de un simulacro finalizado. */
export const generarRecomendaciones = async (simulacroId: string): Promise<Recomendacion[]> => {
  const res = await client.post(`/recomendaciones/simulacro/${simulacroId}`)
  return res.data
}

/** Plan de estudio consolidado: última recomendación por ítem del concurso. */
export const obtenerRecomendacionesConcurso = async (concursoId: string): Promise<Recomendacion[]> => {
  const res = await client.get(`/recomendaciones/concurso/${concursoId}`)
  return res.data
}
