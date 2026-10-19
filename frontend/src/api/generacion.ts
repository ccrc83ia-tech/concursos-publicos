import client from './client'
import type { GeneracionJob, Pregunta } from '@/types'

/** Inicia la generación asíncrona de preguntas para un ítem. */
export const iniciarGeneracion = async (itemId: string): Promise<GeneracionJob> => {
  const res = await client.post(`/items/${itemId}/generar`)
  return res.data
}

/** Consulta el progreso del último job de generación de un ítem. */
export const consultarEstadoGeneracion = async (itemId: string): Promise<GeneracionJob> => {
  const res = await client.get(`/items/${itemId}/generacion/estado`)
  return res.data
}

/** Lista las preguntas ya generadas de un ítem. */
export const listarPreguntas = async (itemId: string): Promise<Pregunta[]> => {
  const res = await client.get(`/items/${itemId}/preguntas`)
  return res.data
}
