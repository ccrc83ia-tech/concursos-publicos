import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { AlertCircle, LogIn } from 'lucide-react'
import { login } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import Spinner from '@/components/shared/Spinner'
import AuthLayout from '@/components/shared/AuthLayout'
import PasswordInput from '@/components/shared/PasswordInput'

const schema = z.object({
  email:    z.string().min(1, 'Escribe tu correo').email('El correo no parece válido'),
  password: z.string().min(1, 'Escribe tu contraseña'),
})
type FormData = z.infer<typeof schema>

export default function Login() {
  const navigate = useNavigate()
  const setAuth  = useAuthStore((s) => s.setAuth)

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: FormData) => {
    try {
      const usuario = await login(data)
      setAuth(usuario, usuario.token)
      toast.success(`¡Hola de nuevo, ${usuario.nombre.split(' ')[0]}!`)
      navigate('/dashboard')
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { mensaje?: string } } })
        ?.response?.data?.mensaje ?? 'El correo o la contraseña no son correctos'
      toast.error(msg)
    }
  }

  return (
    <AuthLayout titulo="Entrar a mi cuenta" subtitulo="Ingresa tus datos para continuar con tu preparación.">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
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
          <label className="label" htmlFor="password">Contraseña</label>
          <PasswordInput
            id="password"
            autoComplete="current-password"
            placeholder="Tu contraseña"
            aria-invalid={!!errors.password}
            {...register('password')}
          />
          {errors.password && (
            <p className="field-error"><AlertCircle size={15} /> {errors.password.message}</p>
          )}
        </div>

        <button type="submit" disabled={isSubmitting} className="btn-primary btn-lg w-full">
          {isSubmitting ? <Spinner size="sm" /> : <LogIn size={20} />}
          {isSubmitting ? 'Entrando…' : 'Entrar'}
        </button>
      </form>

      <p className="text-center text-base text-slate-600 mt-6">
        ¿Es tu primera vez aquí?{' '}
        <Link to="/registro" className="text-brand-700 hover:underline font-semibold">
          Crea tu cuenta gratis
        </Link>
      </p>
    </AuthLayout>
  )
}
