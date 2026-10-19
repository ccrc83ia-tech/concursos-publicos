import client from './client'
import type { Simulacro, Desempeno, OpcionRespuesta } from '@/types'

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
