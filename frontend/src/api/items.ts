import client from './client'
import type { Item, TipoPrueba, DominioPrevio } from '@/types'

export interface ItemRequest {
  nombre:               string
  descripcion?:         string
  tipoPrueba?:          TipoPrueba
  nivelCompetencia?:    number
  pesoPorcentual?:      number
  preguntasSolicitadas?: number
  dominioPrevio?:       DominioPrevio
}

export const parsarIndicadores = async (
  concursoId: string,
  textoIndicadores: string
): Promise<Item[]> => {
  const res = await client.post(`/concursos/${concursoId}/items/parsear`, {
    textoIndicadores,
  })
  return res.data
}

export const listarItems = async (concursoId: string): Promise<Item[]> => {
  const res = await client.get(`/concursos/${concursoId}/items`)
  return res.data
}

export const crearItem = async (concursoId: string, data: ItemRequest): Promise<Item> => {
  const res = await client.post(`/concursos/${concursoId}/items`, data)
  return res.data
}

export const actualizarItem = async (
  concursoId: string,
  itemId: string,
  data: ItemRequest
): Promise<Item> => {
  const res = await client.put(`/concursos/${concursoId}/items/${itemId}`, data)
  return res.data
}

export const eliminarItem = async (concursoId: string, itemId: string): Promise<void> => {
  await client.delete(`/concursos/${concursoId}/items/${itemId}`)
}
