import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { obtenerConcurso } from '@/api/concursos'
import { listarItems } from '@/api/items'
import { crearSimulacro } from '@/api/simulacros'
import type { Concurso, Item } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'

export default function ConfigurarSimulacro() {
  const { concursoId } = useParams<{ concursoId: string }>()
  const navigate = useNavigate()

  const [concurso, setConcurso]   = useState<Concurso | null>(null)
  const [items, setItems]         = useState<Item[]>([])
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
        setSeleccion(new Set(its.map(i => i.id)))
      })
      .finally(() => setLoading(false))
  }, [concursoId])

  const toggle = (id: string) => {
    setSeleccion(prev => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  const totalPreguntas = seleccion.size * preguntasPorItem

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
            {items.map(item => (
              <label key={item.id} className="flex items-center gap-2 px-3 py-2 hover:bg-gray-50 cursor-pointer text-sm">
                <input type="checkbox" checked={seleccion.has(item.id)} onChange={() => toggle(item.id)} />
                <span className="flex-1">{item.nombre}</span>
                <span className="text-xs text-gray-400">{item.tipoPrueba}</span>
              </label>
            ))}
          </div>
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
            Total aproximado: <strong>{totalPreguntas} preguntas</strong>
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
