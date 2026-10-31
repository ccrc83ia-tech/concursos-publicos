import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Plus, ClipboardList, CalendarDays, ChevronRight,
  FilePlus2, Sparkles, Target,
} from 'lucide-react'
import { listarConcursos } from '@/api/concursos'
import { useAuthStore } from '@/store/authStore'
import type { Concurso } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'
import EmptyState from '@/components/shared/EmptyState'

/** Pasos de "cómo funciona", para orientar al usuario nuevo. */
const PASOS = [
  {
    icon: FilePlus2,
    titulo: '1. Registra tu concurso',
    texto: 'Cuéntanos a qué convocatoria del SIMO te presentas y qué temas evalúa.',
  },
  {
    icon: Sparkles,
    titulo: '2. Genera tus preguntas',
    texto: 'Creamos un banco de preguntas de práctica para cada tema, con ayuda de inteligencia artificial.',
  },
  {
    icon: Target,
    titulo: '3. Practica y mejora',
    texto: 'Haz simulacros y descubre en qué temas debes reforzar para aprobar.',
  },
]

function diasBadge(dias: number) {
  if (dias <= 0) return null
  const clase = dias <= 30 ? 'badge-rojo' : dias <= 90 ? 'badge-amarillo' : 'badge-verde'
  return <span className={clase}>Faltan {dias} días</span>
}

export default function Dashboard() {
  const usuario = useAuthStore((s) => s.usuario)
  const [concursos, setConcursos] = useState<Concurso[]>([])
  const [loading, setLoading]     = useState(true)

  useEffect(() => {
    listarConcursos()
      .then(setConcursos)
      .catch(() => setConcursos([]))
      .finally(() => setLoading(false))
  }, [])

  const primerNombre = usuario?.nombre?.split(' ')[0] ?? ''

  return (
    <Layout>
      {/* Bienvenida */}
      <div className="mb-6">
        <h1 className="text-2xl sm:text-3xl font-bold text-slate-900">
          Hola, {primerNombre} 👋
        </h1>
        <p className="text-slate-500 text-base mt-1.5">
          Este es tu espacio para prepararte. Elige un concurso o registra uno nuevo para empezar.
        </p>
      </div>

      {loading ? (
        <div className="flex justify-center py-20"><Spinner size="lg" /></div>
      ) : concursos.length === 0 ? (
        <>
          <EmptyState
            icono={ClipboardList}
            titulo="Empecemos tu preparación"
            mensaje="Aún no tienes ningún concurso registrado. Registra tu convocatoria del SIMO y te ayudamos con el resto."
            accion={
              <Link to="/concursos/nuevo" className="btn-primary btn-lg">
                <Plus size={22} /> Registrar mi primer concurso
              </Link>
            }
          />

          {/* Cómo funciona (guía para primer uso) */}
          <div className="mt-8">
            <h2 className="text-lg font-bold text-slate-800 mb-4 text-center">¿Cómo funciona?</h2>
            <div className="grid gap-4 sm:grid-cols-3">
              {PASOS.map((p) => {
                const Icono = p.icon
                return (
                  <div key={p.titulo} className="card text-center">
                    <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-brand-50 text-brand-600 mb-4">
                      <Icono size={28} strokeWidth={1.75} />
                    </div>
                    <h3 className="font-bold text-slate-800 mb-1.5">{p.titulo}</h3>
                    <p className="text-sm text-slate-500 leading-relaxed">{p.texto}</p>
                  </div>
                )
              })}
            </div>
          </div>
        </>
      ) : (
        <div className="space-y-5">
          <div className="flex items-center justify-between gap-3 flex-wrap">
            <h2 className="text-xl font-bold text-slate-800">Mis concursos</h2>
            <Link to="/concursos/nuevo" className="btn-primary btn-sm">
              <Plus size={18} /> Nuevo concurso
            </Link>
          </div>

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {concursos.map((c) => (
              <Link key={c.id} to={`/concursos/${c.id}`} className="card-interactive group flex flex-col">
                <div className="flex items-start justify-between gap-2 mb-3">
                  <span className="badge-brand">
                    {c.procesoSeleccion ? `Proceso ${c.procesoSeleccion}` : 'Convocatoria SIMO'}
                  </span>
                  {diasBadge(c.diasRestantes)}
                </div>
                <h3 className="text-lg font-bold text-slate-900 group-hover:text-brand-700 transition-colors">
                  {c.nombre}
                </h3>
                {(c.nivelCargo || c.denominacion) && (
                  <p className="text-sm text-slate-500 mt-1">
                    {[c.nivelCargo, c.denominacion].filter(Boolean).join(' — ')}
                  </p>
                )}
                {c.fechaExamen && (
                  <p className="flex items-center gap-1.5 text-sm text-slate-400 mt-3">
                    <CalendarDays size={15} />
                    Examen: {new Date(c.fechaExamen).toLocaleDateString('es-CO', { day: 'numeric', month: 'long', year: 'numeric' })}
                  </p>
                )}
                <span className="flex items-center gap-1 text-sm font-semibold text-brand-700 mt-4 pt-3 border-t border-slate-100">
                  Abrir y practicar <ChevronRight size={16} className="group-hover:translate-x-0.5 transition-transform" />
                </span>
              </Link>
            ))}

            {/* Tarjeta para agregar otro concurso */}
            <Link
              to="/concursos/nuevo"
              className="rounded-2xl border-2 border-dashed border-slate-300 flex flex-col items-center justify-center text-center p-6 text-slate-500 hover:border-brand-400 hover:text-brand-700 hover:bg-brand-50/40 transition-colors min-h-[180px]"
            >
              <Plus size={32} className="mb-2" />
              <span className="font-semibold">Registrar otro concurso</span>
            </Link>
          </div>
        </div>
      )}
    </Layout>
  )
}
