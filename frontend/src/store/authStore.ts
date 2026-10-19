import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Usuario } from '@/types'

interface AuthState {
  usuario:   Usuario | null
  token:     string | null
  setAuth:   (usuario: Usuario, token: string) => void
  logout:    () => void
  isLogged:  () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      usuario:  null,
      token:    null,
      setAuth:  (usuario, token) => set({ usuario, token }),
      logout:   () => set({ usuario: null, token: null }),
      isLogged: () => !!get().token,
    }),
    { name: 'dian_auth' }
  )
)
