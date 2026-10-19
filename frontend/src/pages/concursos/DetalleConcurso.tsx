import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { obtenerConcurso } from '@/api/concursos'
import { listarItems, eliminarItem, actualizarItem, parsearIndicadores } from '@/api/items'
import type { Concurso, Item, DominioPrevio } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'
import GeneracionItem from '@/components/shared/GeneracionItem'

const TIPO_LABELS = { FUNCIONAL: '🔵 Funcional', COMPORTAMENTAL: '🟡 Comportamental', INTEGRIDAD: '🟠 Integridad' }
const TIPO_BADGE  = { FUNCIONAL: 'bg-blue-50 text-blue-700', COMPORTAMENTAL: 'bg-yellow-50 text-yellow-700', INTEGRIDAD: 'bg-orange-50 text-orange-700' }
const DOMINIO_OPTS: { value: DominioPrevio; label: string }[] = [
  { value: 'NO_SE',     label: '🔴 No sé' },
  { value: 'ALGO_SE',   label: '🟡 Algo sé' },
  { value: 'LO_DOMINO', label: '🟢 Lo domino' },
]

export default function DetalleConcurso() {
  const { id } = useParams<{ id: string }>()
  const [concurso, setConcurso]       = useState<Concurso | null>(null)
  const [items, setItems]             = useState<Item[]>([])
  const [loading, setLoading]         = useState(true)
  const [editandoId, setEditandoId]   = useState<string | null>(null)
  const [editForm, setEditForm]       = useState<{ preguntasSolicitadas: number; dominioPrevio: DominioPrevio }>({ preguntasSolicitadas: 50, dominioPrevio: 'NO_SE' })
  const [guardando, setGuardando]     = useState(false)
  const [mostraParseo, setMostraParseo] = useState(false)
  const [textoParseo, setTextoParseo] = useState('')
  const [cargandoParseo, setCargandoParseo] = useState(false)
  const [error, setError]             = useState('')

  const cargar = () => {
    if (!id) return
    Promise.all([obtenerConcurso(id), listarItems(id)])
      .then(([c, its]) => { setConcurso(c); setItems(its) })
      .finally(() => setLoading(false))
  }

  useEffect(() => { cargar() }, [id])

  const iniciarEdicion = (item: Item) => {
    setEditandoId(item.id)
    setEditForm({ preguntasSolicitadas: item.preguntasSolicitadas, dominioPrevio: item.dominioPrevio })
  }

  const cancelarEdicion = () => { setEditandoId(null); setError('') }

  const guardarEdicion = async (item: Item) => {
    if (editForm.preguntasSolicitadas < 1 || editForm.preguntasSolicitadas > 200) {
      setError('El número de preguntas debe estar entre 1 y 200')
      return
    }
    setGuardando(true)
    setError('')
    try {
      const actualizado = await actualizarItem(item.concursoId, item.id, {
        nombre:               item.nombre,
        tipoPrueba:           item.tipoPrueba,
        preguntasSolicitadas: editForm.preguntasSolicitadas,
        dominioPrevio:        editForm.dominioPrevio,
      })
      setItems(prev => prev.map(i => i.id === actualizado.id ? actualizado : i))
      setEditandoId(null)
    } catch {
      setError('Error al guardar. Intente nuevamente.')
    } finally {
      setGuardando(false)
    }
  }

  const handleEliminar = async (item: Item) => {
    if (!window.confirm(`¿Eliminar el indicador "${item.nombre}"?`)) return
    try {
      await eliminarItem(item.concursoId, item.id)
      setItems(prev => prev.filter(i => i.id !== item.id))
    } catch {
      setError('Error al eliminar el indicador.')
    }
  }

  const handleParsear = async () => {
    if (!textoParseo.trim() || !id) return
    setCargandoParseo(true)
    setError('')
    try {
      const nuevos = await parsearIndicadores(id, textoParseo)
      setItems(prev => [...prev, ...nuevos])
      setTextoParseo('')
      setMostraParseo(false)
    } catch {
      setError('Error al procesar los indicadores.')
    } finally {
      setCargandoParseo(false)
    }
  }

  if (loading) return <Layout><div className="flex justify-center py-20"><Spinner size="lg" /></div></Layout>
  if (!concurso) return <Layout><p className="text-red-500">Concurso no encontrado</p></Layout>

  const funcionales      = items.filter(i => i.tipoPrueba === 'FUNCIONAL')
  const comportamentales = items.filter(i => i.tipoPrueba === 'COMPORTAMENTAL')
  const integridad       = items.filter(i => i.tipoPrueba === 'INTEGRIDAD')
  const totalPreguntas   = items.reduce((s, i) => s + i.preguntasSolicitadas, 0)

  return (
    <Layout>
      {/* Cabecera */}
      <div className="mb-6">
        <div className="flex items-start justify-between flex-wrap gap-3">
          <div>
            <Link to="/concursos" className="text-sm text-gray-500 hover:text-gray-700">← Mis concursos</Link>
            <h1 className="text-2xl font-bold text-gray-900 mt-1">{concurso.nombre}</h1>
            <div className="flex items-center gap-2 mt-2">
              <Link to={`/concursos/${concurso.id}/simulacro/nuevo`} className="inline-block btn-primary text-xs">
                ▶ Iniciar simulacro
              </Link>
              <Link to={`/concursos/${concurso.id}/brechas`} className="inline-block text-xs px-3 py-1.5 rounded-md border border-[#003F7F] text-[#003F7F] hover:bg-blue-50">
                📊 Dashboard de brechas
              </Link>
            </div>
            <p className="text-gray-500 text-sm mt-1">
              {concurso.nivelCargo && <span>{concurso.nivelCargo} — </span>}
              {concurso.denominacion && <span>{concurso.denominacion}</span>}
              {concurso.numeroEmpleo && <span className="ml-2 text-gray-400">· Empleo {concurso.numeroEmpleo}</span>}
            </p>
          </div>
          <div className="text-right">
            {concurso.diasRestantes > 0 && (
              <div className={`text-2xl font-bold ${concurso.diasRestantes <= 30 ? 'text-red-600' : concurso.diasRestantes <= 90 ? 'text-yellow-600' : 'text-green-600'}`}>
                {concurso.diasRestantes} días
              </div>
            )}
            {concurso.fechaExamen && (
              <div className="text-xs text-gray-500">
                📅 {new Date(concurso.fechaExamen).toLocaleDateString('es-CO')}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Resumen */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
        {[
          { label: 'Total indicadores', value: items.length,             color: 'text-gray-800' },
          { label: 'Funcionales',       value: funcionales.length,       color: 'text-blue-700' },
          { label: 'Comportamentales',  value: comportamentales.length,  color: 'text-yellow-700' },
          { label: 'Integridad',        value: integridad.length,        color: 'text-orange-700' },
        ].map(s => (
          <div key={s.label} className="card text-center py-4">
            <div className={`text-3xl font-bold ${s.color}`}>{s.value}</div>
            <div className="text-xs text-gray-500 mt-1">{s.label}</div>
          </div>
        ))}
      </div>

      {/* Error global */}
      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
          {error}
          <button onClick={() => setError('')} className="ml-2 text-red-400 hover:text-red-600">✕</button>
        </div>
      )}

      {/* Tabla de indicadores */}
      <div className="card p-0 overflow-hidden mb-4">
        <div className="px-4 py-3 border-b border-gray-100 flex items-center justify-between">
          <h2 className="font-semibold text-gray-800">Indicadores ({items.length})</h2>
          <div className="flex items-center gap-3">
            <span className="text-sm text-gray-500">{totalPreguntas} preguntas</span>
            <button
              onClick={() => setMostraParseo(!mostraParseo)}
              className="btn-primary text-xs"
            >
              + Agregar indicadores
            </button>
          </div>
        </div>

        {/* Panel agregar indicadores */}
        {mostraParseo && (
          <div className="px-4 py-4 bg-blue-50 border-b border-blue-100">
            <p className="text-sm text-blue-700 mb-2 font-medium">
              Pegue los nuevos indicadores (uno por línea):
            </p>
            <textarea
              className="input-field font-mono text-xs h-32 resize-none mb-3"
              placeholder={'RAZONAMIENTO ANALÍTICO\nMISIONALIDAD DE LA DIAN\nCOMPROMISO'}
              value={textoParseo}
              onChange={e => setTextoParseo(e.target.value)}
            />
            <div className="flex gap-2">
              <button
                onClick={handleParsear}
                disabled={cargandoParseo || !textoParseo.trim()}
                className="btn-primary text-xs flex items-center gap-2"
              >
                {cargandoParseo && <Spinner size="sm" />}
                {cargandoParseo ? 'Procesando...' : 'Agregar'}
              </button>
              <button onClick={() => { setMostraParseo(false); setTextoParseo('') }} className="btn-secondary text-xs">
                Cancelar
              </button>
            </div>
          </div>
        )}

        {items.length === 0 ? (
          <div className="text-center py-12 text-gray-500 text-sm">
            No hay indicadores. Use "+ Agregar indicadores" para comenzar.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="text-left px-4 py-2 text-xs font-medium text-gray-500">#</th>
                  <th className="text-left px-4 py-2 text-xs font-medium text-gray-500">Indicador</th>
                  <th className="text-left px-4 py-2 text-xs font-medium text-gray-500">Tipo</th>
                  <th className="text-center px-4 py-2 text-xs font-medium text-gray-500">Preguntas</th>
                  <th className="text-center px-4 py-2 text-xs font-medium text-gray-500">Dominio previo</th>
                  <th className="text-center px-4 py-2 text-xs font-medium text-gray-500">Umbral</th>
                  <th className="text-center px-4 py-2 text-xs font-medium text-gray-500">Preguntas IA</th>
                  <th className="text-center px-4 py-2 text-xs font-medium text-gray-500">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {items.map((item, idx) => (
                  <tr key={item.id} className={editandoId === item.id ? 'bg-blue-50' : 'hover:bg-gray-50'}>
                    <td className="px-4 py-2.5 text-gray-400 text-xs">{idx + 1}</td>
                    <td className="px-4 py-2.5 font-medium text-gray-800">
                      {item.nombre}
                      {item.nivelCompetencia && (
                        <span className="ml-1 text-xs text-blue-500 font-normal">N{item.nivelCompetencia}</span>
                      )}
                    </td>
                    <td className="px-4 py-2.5">
                      <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${TIPO_BADGE[item.tipoPrueba]}`}>
                        {TIPO_LABELS[item.tipoPrueba]}
                      </span>
                    </td>

                    {/* Preguntas — editable */}
                    <td className="px-4 py-2.5 text-center">
                      {editandoId === item.id ? (
                        <input
                          type="number"
                          min={1}
                          max={200}
                          value={editForm.preguntasSolicitadas}
                          onChange={e => setEditForm(f => ({ ...f, preguntasSolicitadas: Number(e.target.value) }))}
                          className="w-16 text-center border border-blue-300 rounded px-1 py-0.5 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
                        />
                      ) : (
                        <span className="font-semibold text-blue-700">{item.preguntasSolicitadas}</span>
                      )}
                    </td>

                    {/* Dominio previo — editable */}
                    <td className="px-4 py-2.5 text-center">
                      {editandoId === item.id ? (
                        <select
                          value={editForm.dominioPrevio}
                          onChange={e => setEditForm(f => ({ ...f, dominioPrevio: e.target.value as DominioPrevio }))}
                          className="border border-blue-300 rounded px-1 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-blue-500"
                        >
                          {DOMINIO_OPTS.map(o => (
                            <option key={o.value} value={o.value}>{o.label}</option>
                          ))}
                        </select>
                      ) : (
                        <span className="text-xs text-gray-500">
                          {DOMINIO_OPTS.find(o => o.value === item.dominioPrevio)?.label ?? item.dominioPrevio}
                        </span>
                      )}
                    </td>

                    <td className="px-4 py-2.5 text-center text-xs text-gray-500">
                      {item.umbralBrecha}%
                    </td>

                    {/* Generación IA */}
                    <td className="px-4 py-2.5">
                      <div className="flex justify-center">
                        <GeneracionItem
                          itemId={item.id}
                          cantidad={editandoId === item.id ? editForm.preguntasSolicitadas : item.preguntasSolicitadas}
                        />
                      </div>
                    </td>

                    {/* Acciones */}
                    <td className="px-4 py-2.5 text-center">
                      {editandoId === item.id ? (
                        <div className="flex items-center justify-center gap-1">
                          <button
                            onClick={() => guardarEdicion(item)}
                            disabled={guardando}
                            className="text-xs bg-green-600 hover:bg-green-700 text-white px-2 py-1 rounded transition-colors disabled:opacity-50"
                          >
                            {guardando ? '...' : '✓ Guardar'}
                          </button>
                          <button
                            onClick={cancelarEdicion}
                            className="text-xs text-gray-500 hover:text-gray-700 px-2 py-1 rounded border border-gray-200 hover:bg-gray-50"
                          >
                            Cancelar
                          </button>
                        </div>
                      ) : (
                        <div className="flex items-center justify-center gap-2">
                          <button
                            onClick={() => iniciarEdicion(item)}
                            className="text-xs text-blue-600 hover:text-blue-800 font-medium"
                            title="Editar preguntas y dominio previo"
                          >
                            ✏️ Editar
                          </button>
                          <button
                            onClick={() => handleEliminar(item)}
                            className="text-xs text-red-500 hover:text-red-700"
                            title="Eliminar indicador"
                          >
                            🗑️
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Próximo paso */}
      <div className="card bg-blue-50 border-blue-200">
        <div className="flex items-center gap-3">
          <span className="text-2xl">🤖</span>
          <div>
            <p className="font-semibold text-blue-800">Generación de preguntas con IA</p>
            <p className="text-sm text-blue-600">
              Use el botón 🤖 Generar de cada indicador para crear su banco de preguntas
              (hasta {totalPreguntas} en total). La generación corre en segundo plano y verá
              el avance en vivo. Requiere configurar <code>GROQ_API_KEY</code> en el backend.
            </p>
          </div>
        </div>
      </div>
    </Layout>
  )
}
