import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listarConcursos, eliminarConcurso } from '@/api/concursos'
import type { Concurso } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'

export default function ListaConcursos() {
  const [concursos, setConcursos] = useState<Concurso[]>([])
  const [loading, setLoading]     = useState(true)

  const cargar = () => {
    setLoading(true)
    listarConcursos().then(setConcursos).finally(() => setLoading(false))
  }

  useEffect(() => { cargar() }, [])

  const handleEliminar = async (id: string, nombre: string) => {
    if (!window.confirm(`¿Eliminar el concurso "${nombre}"? Esta acción no se puede deshacer.`)) return
    await eliminarConcurso(id)
    cargar()
  }

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Mis concursos</h1>
        <Link to="/concursos/nuevo" className="btn-primary">+ Nuevo concurso</Link>
      </div>

      {loading ? (
        <div className="flex justify-center py-20"><Spinner size="lg" /></div>
      ) : concursos.length === 0 ? (
        <div className="card text-center py-16">
          <div className="text-5xl mb-4">📋</div>
          <h2 className="text-lg font-semibold text-gray-700 mb-2">Sin concursos registrados</h2>
          <p className="text-gray-500 text-sm mb-6">Registre su convocatoria DIAN para comenzar</p>
          <Link to="/concursos/nuevo" className="btn-primary">+ Registrar concurso</Link>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {concursos.map((c) => (
            <div key={c.id} className="card hover:shadow-md transition-shadow">
              <div className="flex items-start justify-between mb-3">
                <span className="text-xs font-medium text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">
                  {c.procesoSeleccion ?? 'Sin proceso'}
                </span>
                {c.diasRestantes > 0 && (
                  <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                    c.diasRestantes <= 30 ? 'bg-red-50 text-red-600'
                    : c.diasRestantes <= 90 ? 'bg-yellow-50 text-yellow-600'
                    : 'bg-green-50 text-green-600'}`}>
                    {c.diasRestantes}d
                  </span>
                )}
              </div>
              <Link to={`/concursos/${c.id}`}>
                <h3 className="font-semibold text-gray-900 hover:text-blue-700 transition-colors">
                  {c.nombre}
                </h3>
              </Link>
              {c.denominacion && (
                <p className="text-sm text-gray-500 mt-1">{c.nivelCargo} — {c.denominacion}</p>
              )}
              {c.fechaExamen && (
                <p className="text-xs text-gray-400 mt-2">
                  📅 {new Date(c.fechaExamen).toLocaleDateString('es-CO')}
                </p>
              )}
              <div className="flex gap-2 mt-4 pt-3 border-t border-gray-100">
                <Link to={`/concursos/${c.id}`} className="btn-secondary text-xs flex-1 text-center">
                  Ver detalle
                </Link>
                <button
                  onClick={() => handleEliminar(c.id, c.nombre)}
                  className="text-xs text-red-500 hover:text-red-700 px-2"
                >
                  Eliminar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </Layout>
  )
}
