import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import { Plus, ClipboardList, CalendarDays, Trash2, ChevronRight } from 'lucide-react'
import { listarConcursos, eliminarConcurso } from '@/api/concursos'
import type { Concurso } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'
import PageHeader from '@/components/shared/PageHeader'
import EmptyState from '@/components/shared/EmptyState'

function diasBadge(dias: number) {
  if (dias <= 0) return null
  const clase = dias <= 30 ? 'badge-rojo' : dias <= 90 ? 'badge-amarillo' : 'badge-verde'
  return <span className={clase}>Faltan {dias} días</span>
}

export default function ListaConcursos() {
  const [concursos, setConcursos] = useState<Concurso[]>([])
  const [loading, setLoading]     = useState(true)

  const cargar = () => {
    setLoading(true)
    listarConcursos().then(setConcursos).catch(() => setConcursos([])).finally(() => setLoading(false))
  }

  useEffect(() => { cargar() }, [])

  const handleEliminar = async (e: React.MouseEvent, id: string, nombre: string) => {
    e.preventDefault()
    e.stopPropagation()
    if (!window.confirm(`¿Seguro que quieres eliminar "${nombre}"?\n\nSe borrarán sus temas y preguntas. Esta acción no se puede deshacer.`)) return
    try {
      await eliminarConcurso(id)
      toast.success('Concurso eliminado')
      cargar()
    } catch {
      toast.error('No se pudo eliminar el concurso')
    }
  }

  return (
    <Layout>
      <PageHeader
        titulo="Mis concursos"
        descripcion="Aquí están todas las convocatorias que estás preparando."
        acciones={
          concursos.length > 0 && (
            <Link to="/concursos/nuevo" className="btn-primary btn-sm">
              <Plus size={18} /> Nuevo concurso
            </Link>
          )
        }
      />

      {loading ? (
        <div className="flex justify-center py-20"><Spinner size="lg" /></div>
      ) : concursos.length === 0 ? (
        <EmptyState
          icono={ClipboardList}
          titulo="Todavía no tienes concursos"
          mensaje="Registra tu convocatoria del SIMO para empezar a practicar."
          accion={
            <Link to="/concursos/nuevo" className="btn-primary btn-lg">
              <Plus size={22} /> Registrar concurso
            </Link>
          }
        />
      ) : (
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
                  {new Date(c.fechaExamen).toLocaleDateString('es-CO', { day: 'numeric', month: 'long', year: 'numeric' })}
                </p>
              )}
              <div className="flex items-center justify-between mt-4 pt-3 border-t border-slate-100">
                <span className="flex items-center gap-1 text-sm font-semibold text-brand-700">
                  Abrir <ChevronRight size={16} />
                </span>
                <button
                  onClick={(e) => handleEliminar(e, c.id, c.nombre)}
                  className="flex items-center gap-1 text-sm text-slate-400 hover:text-red-600 px-2 py-1 rounded-lg hover:bg-red-50 transition-colors"
                  title="Eliminar concurso"
                >
                  <Trash2 size={15} /> Eliminar
                </button>
              </div>
            </Link>
          ))}
        </div>
      )}
    </Layout>
  )
}
