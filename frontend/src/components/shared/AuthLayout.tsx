import type { ReactNode } from 'react'
import { GraduationCap, CheckCircle2 } from 'lucide-react'

interface Props {
  titulo:    string
  subtitulo: string
  children:  ReactNode
}

const beneficios = [
  'Registra cualquier convocatoria del SIMO en minutos',
  'Practica con simulacros cronometrados reales',
  'Descubre en qué temas debes mejorar',
]

/**
 * Marco de dos columnas para login y registro: a la izquierda, un panel de
 * marca que explica para qué sirve la app (reduce la ansiedad del primer uso);
 * a la derecha, el formulario. En móvil se apila y se oculta el panel lateral.
 */
export default function AuthLayout({ titulo, subtitulo, children }: Props) {
  return (
    <div className="min-h-screen flex">
      {/* Panel de marca (oculto en móvil) */}
      <aside className="hidden lg:flex flex-col justify-between w-[42%] bg-brand-800 text-white p-12">
        <div className="flex items-center gap-3">
          <span className="inline-flex items-center justify-center w-11 h-11 rounded-xl bg-white/15">
            <GraduationCap size={26} />
          </span>
          <span className="text-xl font-bold">Preparación Concursos</span>
        </div>

        <div>
          <h2 className="text-3xl font-bold leading-tight mb-4">
            Prepárate con confianza para tu concurso de méritos
          </h2>
          <p className="text-brand-100 text-lg mb-8 leading-relaxed">
            Una herramienta sencilla para estudiar, practicar y saber exactamente
            qué repasar antes del examen.
          </p>
          <ul className="space-y-3">
            {beneficios.map((b) => (
              <li key={b} className="flex items-start gap-3 text-brand-50">
                <CheckCircle2 size={22} className="shrink-0 mt-0.5 text-brand-200" />
                <span className="text-base">{b}</span>
              </li>
            ))}
          </ul>
        </div>

        <p className="text-brand-300 text-sm">SIMO / CNSC — Colombia</p>
      </aside>

      {/* Formulario */}
      <main className="flex-1 flex items-center justify-center p-5 sm:p-8 bg-slate-50">
        <div className="w-full max-w-md">
          {/* Marca compacta para móvil */}
          <div className="lg:hidden text-center mb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-brand-800 rounded-2xl mb-3">
              <GraduationCap size={32} className="text-white" />
            </div>
            <h1 className="text-xl font-bold text-slate-900">Preparación Concursos</h1>
          </div>

          <div className="mb-6">
            <h1 className="text-2xl font-bold text-slate-900">{titulo}</h1>
            <p className="text-slate-500 mt-1">{subtitulo}</p>
          </div>

          {children}
        </div>
      </main>
    </div>
  )
}
