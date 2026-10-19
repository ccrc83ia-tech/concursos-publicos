import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listarConcursos } from '@/api/concursos'
import { useAuthStore } from '@/store/authStore'
import type { Concurso } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'

export default function Dashboard() {
  const usuario = useAuthStore((s) => s.usuario)
  const [concursos, setConcursos] = useState<Concurso[]>([])
  const [loading, setLoading]     = useState(true)

  useEffect(() => {
    listarConcursos()
      .then(setConcursos)
      .finally(() => setLoading(false))
  }, [])

  return (
    <Layout>
      {/* Bienvenida */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">
          Bienvenido, {usuario?.nombre?.split(' ')[0]} 👋
        </h1>
        <p className="text-gray-500 text-sm mt-1">
          Panel de preparación para concursos DIAN
        </p>
      </div>

      {loading ? (
        <div className="flex justify-center py-20"><Spinner size="lg" /></div>
      ) : concursos.length === 0 ? (
        /* Estado vacío */
        <div className="card text-center py-16">
          <div className="text-5xl mb-4">📋</div>
          <h2 className="text-lg font-semibold text-gray-700 mb-2">
            Aún no tiene concursos registrados
          </h2>
          <p className="text-gray-500 text-sm mb-6">
            Comience registrando su convocatoria DIAN y los indicadores a evaluar
          </p>
          <Link to="/concursos/nuevo" className="btn-primary">
            + Registrar primer concurso
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-800">Mis concursos</h2>
            <Link to="/concursos/nuevo" className="btn-primary text-sm">
              + Nuevo concurso
            </Link>
          </div>

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {concursos.map((c) => (
              <Link key={c.id} to={`/concursos/${c.id}`} className="card hover:shadow-md transition-shadow group">
                <div className="flex items-start justify-between mb-3">
                  <span className="text-xs font-medium text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">
                    {c.procesoSeleccion ?? 'Sin proceso'}
                  </span>
                  {c.diasRestantes > 0 ? (
                    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                      c.diasRestantes <= 30
                        ? 'bg-red-50 text-red-600'
                        : c.diasRestantes <= 90
                        ? 'bg-yellow-50 text-yellow-600'
                        : 'bg-green-50 text-green-600'
                    }`}>
                      {c.diasRestantes}d restantes
                    </span>
                  ) : null}
                </div>
                <h3 className="font-semibold text-gray-900 group-hover:text-blue-700 transition-colors">
                  {c.nombre}
                </h3>
                {c.denominacion && (
                  <p className="text-sm text-gray-500 mt-1">{c.nivelCargo} — {c.denominacion}</p>
                )}
                {c.fechaExamen && (
                  <p className="text-xs text-gray-400 mt-2">
                    📅 Examen: {new Date(c.fechaExamen).toLocaleDateString('es-CO')}
                  </p>
                )}
              </Link>
            ))}
          </div>
        </div>
      )}
    </Layout>
  )
}
