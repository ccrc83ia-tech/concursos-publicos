import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { obtenerConcurso } from '@/api/concursos'
import { listarItems } from '@/api/items'
import { obtenerEstadoBanco } from '@/api/generacion'
import { crearSimulacro } from '@/api/simulacros'
import type { BancoEstado, Concurso, Item } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'

export default function ConfigurarSimulacro() {
  const { concursoId } = useParams<{ concursoId: string }>()
  const navigate = useNavigate()

  const [concurso, setConcurso]   = useState<Concurso | null>(null)
  const [items, setItems]         = useState<Item[]>([])
  const [bancos, setBancos]       = useState<Record<string, BancoEstado>>({})
  const [seleccion, setSeleccion] = useState<Set<string>>(new Set())
  const [preguntasPorItem, setPreguntasPorItem] = useState(10)
  const [conTiempo, setConTiempo] = useState(true)
  const [minutos, setMinutos]     = useState(30)
  const [loading, setLoading]     = useState(true)
  const [creando, setCreando]     = useState(false)
  const [error, setError]         = useState('')

  useEffect(() => {
    if (!concursoId) return
    Promise.all([obtenerConcurso(concursoId), listarItems(concursoId)])
      .then(([c, its]) => {
        setConcurso(c)
        setItems(its)
        // Por defecto, solo preseleccionamos indicadores que tengan preguntas.
        // El estado del banco llega de forma asíncrona y refina la selección.
        setSeleccion(new Set(its.map(i => i.id)))
        // Carga en paralelo el banco de cada indicador para mostrar disponibilidad.
        its.forEach(it => {
          obtenerEstadoBanco(it.id)
            .then(b => setBancos(prev => ({ ...prev, [it.id]: b })))
            .catch(() => { /* sin banco: se trata como vacío */ })
        })
      })
      .finally(() => setLoading(false))
  }, [concursoId])

  /** Preguntas disponibles de un ítem (las validadas son la fuente preferente). */
  const disponibles = (itemId: string) => bancos[itemId]?.total ?? 0

  const toggle = (id: string) => {
    setSeleccion(prev => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  // Total real estimado: por cada indicador seleccionado, lo pedido o lo disponible
  // (lo que sea menor). Mientras el banco no ha cargado, se asume lo pedido.
  const totalPreguntas = Array.from(seleccion).reduce((suma, id) => {
    const banco = bancos[id]
    const disp = banco ? banco.total : preguntasPorItem
    return suma + Math.min(preguntasPorItem, disp)
  }, 0)

  const handleCrear = async () => {
    if (!concursoId || seleccion.size === 0) {
      setError('Seleccione al menos un indicador')
      return
    }
    setCreando(true)
    setError('')
    try {
      const sim = await crearSimulacro({
        concursoId,
        tipo: 'POR_ITEM',
        itemIds: Array.from(seleccion),
        preguntasPorItem,
        tiempoLimiteSegundos: conTiempo ? minutos * 60 : null,
      })
      navigate(`/simulacro/${sim.id}`)
    } catch (e: any) {
      setError(e?.response?.data?.mensaje ?? 'No se pudo crear el simulacro. ¿Generó preguntas para estos indicadores?')
    } finally {
      setCreando(false)
    }
  }

  if (loading) return <Layout><div className="flex justify-center py-20"><Spinner size="lg" /></div></Layout>
  if (!concurso) return <Layout><p className="text-red-500">Concurso no encontrado</p></Layout>

  return (
    <Layout>
      <div className="max-w-2xl mx-auto">
        <Link to={`/concursos/${concursoId}`} className="text-sm text-gray-500 hover:text-gray-700">← Volver al concurso</Link>
        <h1 className="text-2xl font-bold text-gray-900 mt-1 mb-6">Configurar simulacro</h1>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">{error}</div>
        )}

        <div className="card mb-4">
          <h2 className="font-semibold mb-3">Indicadores a evaluar</h2>
          <div className="flex items-center justify-between mb-2 text-xs text-gray-500">
            <span>{seleccion.size} de {items.length} seleccionados</span>
            <div className="flex gap-2">
              <button onClick={() => setSeleccion(new Set(items.map(i => i.id)))} className="text-blue-600 hover:underline">Todos</button>
              <button onClick={() => setSeleccion(new Set())} className="text-gray-500 hover:underline">Ninguno</button>
            </div>
          </div>
          <div className="max-h-64 overflow-y-auto border border-gray-100 rounded-lg divide-y divide-gray-100">
            {items.map(item => {
              const banco = bancos[item.id]
              const total = disponibles(item.id)
              const vacio = banco != null && total === 0
              const insuficiente = banco != null && total > 0 && total < preguntasPorItem
              return (
                <label
                  key={item.id}
                  className={`flex items-center gap-2 px-3 py-2 cursor-pointer text-sm ${vacio ? 'opacity-60' : 'hover:bg-gray-50'}`}
                >
                  <input type="checkbox" checked={seleccion.has(item.id)} onChange={() => toggle(item.id)} />
                  <span className="flex-1">{item.nombre}</span>

                  {/* Disponibilidad del banco del indicador */}
                  {banco === undefined ? (
                    <span className="text-[10px] text-gray-300">…</span>
                  ) : vacio ? (
                    <span className="badge-rojo" title="Sin preguntas: genera antes de incluirlo">Sin banco</span>
                  ) : insuficiente ? (
                    <span className="badge-amarillo" title={`Solo hay ${total} preguntas; se pidieron ${preguntasPorItem}`}>
                      {total} disp.
                    </span>
                  ) : (
                    <span className="text-xs text-gray-500" title={`${total} preguntas en el banco`}>
                      {total} disp.
                    </span>
                  )}

                  <span className="text-xs text-gray-400 w-24 text-right">{item.tipoPrueba}</span>
                </label>
              )
            })}
          </div>
          {/* Aviso si hay indicadores seleccionados sin banco */}
          {items.some(i => seleccion.has(i.id) && bancos[i.id] != null && disponibles(i.id) === 0) && (
            <p className="mt-2 text-xs text-red-600">
              Algunos indicadores seleccionados no tienen preguntas generadas y se omitirán.
            </p>
          )}
        </div>

        <div className="card mb-4 grid sm:grid-cols-2 gap-4">
          <div>
            <label className="label">Preguntas por indicador</label>
            <input
              type="number" min={1} max={50} value={preguntasPorItem}
              onChange={e => setPreguntasPorItem(Math.max(1, Number(e.target.value)))}
              className="input-field"
            />
          </div>
          <div>
            <label className="label flex items-center gap-2">
              <input type="checkbox" checked={conTiempo} onChange={e => setConTiempo(e.target.checked)} />
              Con cronómetro
            </label>
            <input
              type="number" min={1} max={300} value={minutos} disabled={!conTiempo}
              onChange={e => setMinutos(Math.max(1, Number(e.target.value)))}
              className="input-field disabled:bg-gray-100"
              placeholder="Minutos"
            />
          </div>
        </div>

        <div className="card bg-blue-50 border-blue-200 flex items-center justify-between">
          <div className="text-sm text-blue-700">
            Hasta <strong>{totalPreguntas} preguntas</strong> disponibles
            {conTiempo && <span> · {minutos} min</span>}
          </div>
          <button onClick={handleCrear} disabled={creando || seleccion.size === 0} className="btn-primary flex items-center gap-2">
            {creando && <Spinner size="sm" />}
            Comenzar simulacro →
          </button>
        </div>
      </div>
    </Layout>
  )
}
