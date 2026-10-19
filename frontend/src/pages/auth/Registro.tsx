import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { registro } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import Spinner from '@/components/shared/Spinner'

const schema = z.object({
  nombre:   z.string().min(2, 'Mínimo 2 caracteres').max(100),
  email:    z.string().email('Email inválido'),
  password: z.string().min(8, 'Mínimo 8 caracteres'),
  confirmar: z.string(),
}).refine((d) => d.password === d.confirmar, {
  message: 'Las contraseñas no coinciden',
  path:    ['confirmar'],
})
type FormData = z.infer<typeof schema>

export default function Registro() {
  const navigate = useNavigate()
  const setAuth  = useAuthStore((s) => s.setAuth)
  const [error, setError] = useState('')

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: FormData) => {
    setError('')
    try {
      const usuario = await registro({ nombre: data.nombre, email: data.email, password: data.password })
      setAuth(usuario, usuario.token)
      navigate('/dashboard')
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { mensaje?: string } } })
        ?.response?.data?.mensaje ?? 'Error al registrarse'
      setError(msg)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-[#003F7F] rounded-2xl mb-4">
            <span className="text-white text-2xl font-bold">D</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">DIAN Prep</h1>
          <p className="text-gray-500 text-sm mt-1">Cree su cuenta para comenzar</p>
        </div>

        <div className="card">
          <h2 className="text-lg font-semibold text-gray-800 mb-6">Crear cuenta</h2>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="label">Nombre completo</label>
              <input type="text" placeholder="Juan Pérez" className="input-field" {...register('nombre')} />
              {errors.nombre && <p className="text-red-500 text-xs mt-1">{errors.nombre.message}</p>}
            </div>
            <div>
              <label className="label">Email</label>
              <input type="email" placeholder="correo@ejemplo.com" className="input-field" {...register('email')} />
              {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
            </div>
            <div>
              <label className="label">Contraseña</label>
              <input type="password" placeholder="Mínimo 8 caracteres" className="input-field" {...register('password')} />
              {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password.message}</p>}
            </div>
            <div>
              <label className="label">Confirmar contraseña</label>
              <input type="password" placeholder="Repita la contraseña" className="input-field" {...register('confirmar')} />
              {errors.confirmar && <p className="text-red-500 text-xs mt-1">{errors.confirmar.message}</p>}
            </div>

            <button type="submit" disabled={isSubmitting} className="btn-primary w-full flex items-center justify-center gap-2">
              {isSubmitting ? <Spinner size="sm" /> : null}
              {isSubmitting ? 'Creando cuenta...' : 'Crear cuenta'}
            </button>
          </form>
        </div>

        <p className="text-center text-sm text-gray-600 mt-4">
          ¿Ya tiene cuenta?{' '}
          <Link to="/login" className="text-blue-600 hover:underline font-medium">
            Inicie sesión
          </Link>
        </p>
      </div>
    </div>
  )
}
