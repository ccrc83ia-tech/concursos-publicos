import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { AlertCircle, UserPlus } from 'lucide-react'
import { registro } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import Spinner from '@/components/shared/Spinner'
import AuthLayout from '@/components/shared/AuthLayout'
import PasswordInput from '@/components/shared/PasswordInput'

const schema = z.object({
  nombre:    z.string().min(2, 'Escribe tu nombre completo').max(100),
  email:     z.string().min(1, 'Escribe tu correo').email('El correo no parece válido'),
  password:  z.string().min(8, 'Usa al menos 8 caracteres'),
  confirmar: z.string().min(1, 'Repite la contraseña'),
}).refine((d) => d.password === d.confirmar, {
  message: 'Las dos contraseñas deben ser iguales',
  path:    ['confirmar'],
})
type FormData = z.infer<typeof schema>

export default function Registro() {
  const navigate = useNavigate()
  const setAuth  = useAuthStore((s) => s.setAuth)

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: FormData) => {
    try {
      const usuario = await registro({ nombre: data.nombre, email: data.email, password: data.password })
      setAuth(usuario, usuario.token)
      toast.success('¡Tu cuenta está lista! Empecemos.')
      navigate('/dashboard')
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { mensaje?: string } } })
        ?.response?.data?.mensaje ?? 'No pudimos crear la cuenta. Intenta de nuevo.'
      toast.error(msg)
    }
  }

  return (
    <AuthLayout titulo="Crear mi cuenta" subtitulo="Solo toma un minuto. Es gratis.">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div>
          <label className="label" htmlFor="nombre">Nombre completo</label>
          <input
            id="nombre"
            type="text"
            placeholder="Ej. María Pérez"
            aria-invalid={!!errors.nombre}
            className="input-field"
            {...register('nombre')}
          />
          {errors.nombre && (
            <p className="field-error"><AlertCircle size={15} /> {errors.nombre.message}</p>
          )}
        </div>

        <div>
          <label className="label" htmlFor="email">Correo electrónico</label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            placeholder="correo@ejemplo.com"
            aria-invalid={!!errors.email}
            className="input-field"
            {...register('email')}
          />
          {errors.email && (
            <p className="field-error"><AlertCircle size={15} /> {errors.email.message}</p>
          )}
        </div>

        <div>
          <label className="label" htmlFor="password">Crea una contraseña</label>
          <PasswordInput
            id="password"
            autoComplete="new-password"
            placeholder="Mínimo 8 caracteres"
            aria-invalid={!!errors.password}
            {...register('password')}
          />
          {errors.password
            ? <p className="field-error"><AlertCircle size={15} /> {errors.password.message}</p>
            : <p className="help-text">Usa al menos 8 caracteres. Mézclalos para mayor seguridad.</p>}
        </div>

        <div>
          <label className="label" htmlFor="confirmar">Repite la contraseña</label>
          <PasswordInput
            id="confirmar"
            autoComplete="new-password"
            placeholder="Escríbela otra vez"
            aria-invalid={!!errors.confirmar}
            {...register('confirmar')}
          />
          {errors.confirmar && (
            <p className="field-error"><AlertCircle size={15} /> {errors.confirmar.message}</p>
          )}
        </div>

        <button type="submit" disabled={isSubmitting} className="btn-primary btn-lg w-full">
          {isSubmitting ? <Spinner size="sm" /> : <UserPlus size={20} />}
          {isSubmitting ? 'Creando tu cuenta…' : 'Crear mi cuenta'}
        </button>
      </form>

      <p className="text-center text-base text-slate-600 mt-6">
        ¿Ya tienes cuenta?{' '}
        <Link to="/login" className="text-brand-700 hover:underline font-semibold">
          Entra aquí
        </Link>
      </p>
    </AuthLayout>
  )
}
