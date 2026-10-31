import axios from 'axios'
import type { Usuario } from '@/types'

// Auth usa /api/auth, no /api/v1. La base se deriva de VITE_API_URL (que apunta a
// .../api/v1) para no duplicar el host: se reemplaza el sufijo /v1 por /auth.
const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1'
const AUTH_BASE = API_BASE.replace(/\/v1\/?$/, '/auth')

const authClient = axios.create({
  baseURL: AUTH_BASE,
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
