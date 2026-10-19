import axios from 'axios'
import type { Usuario } from '@/types'

// Auth usa /api/auth, no /api/v1
const authClient = axios.create({
  baseURL: 'http://localhost:8080/api/auth',
  headers: { 'Content-Type': 'application/json' },
})

export interface LoginRequest    { email: string; password: string }
export interface RegistroRequest { nombre: string; email: string; password: string }

export const login = async (data: LoginRequest): Promise<Usuario> => {
  const res = await authClient.post('/login', data)
  return res.data
}

export const registro = async (data: RegistroRequest): Promise<Usuario> => {
  const res = await authClient.post('/registro', data)
  return res.data
}
