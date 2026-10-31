import client from './client'
import type { Simulacro, Desempeno, OpcionRespuesta, TipoFeedback, FeedbackResultado } from '@/types'

export interface CrearSimulacroRequest {
  concursoId:           string
  tipo?:                string
  itemIds?:             string[]
  preguntasPorItem:     number
  tiempoLimiteSegundos?: number | null
}

export const crearSimulacro = async (data: CrearSimulacroRequest): Promise<Simulacro> => {
  const res = await client.post('/simulacros', data)
  return res.data
}

export const obtenerSimulacro = async (id: string): Promise<Simulacro> => {
  const res = await client.get(`/simulacros/${id}`)
  return res.data
}

export const responderPregunta = async (
  simulacroId: string,
  payload: {
    simulacroPreguntaId: string
    respuesta: OpcionRespuesta | null
    marcadaParaRevision: boolean
    tiempoRespuestaSegundos: number
  }
): Promise<void> => {
  await client.post(`/simulacros/${simulacroId}/responder`, payload)
}

export const finalizarSimulacro = async (
  simulacroId: string,
  tiempoUsadoSegundos: number
): Promise<Simulacro> => {
  const res = await client.post(
    `/simulacros/${simulacroId}/finalizar?tiempoUsadoSegundos=${tiempoUsadoSegundos}`
  )
  return res.data
}

export const obtenerDesempeno = async (simulacroId: string): Promise<Desempeno[]> => {
  const res = await client.get(`/simulacros/${simulacroId}/desempeno`)
  return res.data
}

export const obtenerBrechas = async (concursoId: string): Promise<Desempeno[]> => {
  const res = await client.get(`/simulacros/brechas/${concursoId}`)
  return res.data
}

/**
 * Reporta una pregunta del simulacro como problemática. Al acumular suficientes
 * reportes de usuarios distintos, el backend la retira del banco.
 */
export const reportarPregunta = async (
  simulacroId: string,
  preguntaId: string,
  payload: { tipo: TipoFeedback; comentario?: string },
): Promise<FeedbackResultado> => {
  const res = await client.post(
    `/simulacros/${simulacroId}/preguntas/${preguntaId}/feedback`,
    payload,
  )
  return res.data
}
