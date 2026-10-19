import client from './client'
import type { Concurso } from '@/types'

export interface ConcursoRequest {
  nombre:            string
  procesoSeleccion?: string
  numeroInscripcion?: string
  numeroEmpleo?:     string
  nivelCargo?:       string
  denominacion?:     string
  fechaExamen?:      string
}

export const crearConcurso = async (data: ConcursoRequest): Promise<Concurso> => {
  const res = await client.post('/concursos', data)
  return res.data
}

export const listarConcursos = async (): Promise<Concurso[]> => {
  const res = await client.get('/concursos')
  return res.data
}

export const obtenerConcurso = async (id: string): Promise<Concurso> => {
  const res = await client.get(`/concursos/${id}`)
  return res.data
}

export const eliminarConcurso = async (id: string): Promise<void> => {
  await client.delete(`/concursos/${id}`)
}
