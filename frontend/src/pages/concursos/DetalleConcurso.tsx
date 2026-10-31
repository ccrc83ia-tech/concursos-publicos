import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import {
  Play, BarChart3, Plus, Pencil, Trash2, Check, X, CalendarDays,
  Sparkles, AlertCircle, ListChecks,
} from 'lucide-react'
import { obtenerConcurso } from '@/api/concursos'
import { listarItems, eliminarItem, actualizarItem, parsearIndicadores } from '@/api/items'
import type { Concurso, Item, TipoPrueba, NivelDominio } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'
import PageHeader from '@/components/shared/PageHeader'
import InfoTip from '@/components/shared/InfoTip'
import GeneracionItem from '@/components/shared/GeneracionItem'

const TIPO_CHIP: Record<string, string> = {
  FUNCIONAL: 'chip-funcional', COMPORTAMENTAL: 'chip-comportamental', INTEGRIDAD: 'chip-integridad',
}
const TIPO_LABEL: Record<string, string> = {
  FUNCIONAL: 'Técnico', COMPORTAMENTAL: 'Comportamental', INTEGRIDAD: 'Integridad',
}

/** Filtro activo de la tabla: un tipo de prueba o 'TODOS'. */
type Filtro = TipoPrueba | 'TODOS'

const NIVEL_COLOR: Record<NivelDominio, string> = {
  ROJO: 'bg-red-500', AMARILLO: 'bg-amber-400', VERDE: 'bg-emerald-500',
}
const NIVEL_TEXTO: Record<NivelDominio, string> = {
  ROJO: 'text-red-600', AMARILLO: 'text-amber-600', VERDE: 'text-emerald-600',
}

/** Celda de "Dominio" del tema: % real del último simulacro, o "Sin practicar". */
function CeldaDominio({ item }: { item: Item }) {
  if (!item.practicado || item.nivelDominio == null) {
    return (
      <span className="text-sm text-slate-400" title="Aún no has hecho un simulacro con este tema">
        Sin practicar
      </span>
    )
  }
  const pct = Math.round(item.dominioPorcentual)
  return (
    <div className="flex flex-col items-center gap-1 min-w-[90px]">
      <span className={`font-bold text-base ${NIVEL_TEXTO[item.nivelDominio]}`}>{pct}%</span>
      <div className="h-1.5 w-full bg-slate-200 rounded-full overflow-hidden">
        <div className={`h-full ${NIVEL_COLOR[item.nivelDominio]} transition-all`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  )
}

export default function DetalleConcurso() {
  const { id } = useParams<{ id: string }>()
  const [concurso, setConcurso]   = useState<Concurso | null>(null)
  const [items, setItems]         = useState<Item[]>([])
  const [loading, setLoading]     = useState(true)
  const [filtro, setFiltro]       = useState<Filtro>('TODOS')
  const [editandoId, setEditandoId] = useState<string | null>(null)
  const [editForm, setEditForm]   = useState<{ preguntasSolicitadas: number }>({ preguntasSolicitadas: 5 })
  const [guardando, setGuardando] = useState(false)
  const [mostraParseo, setMostraParseo] = useState(false)
  const [textoParseo, setTextoParseo]   = useState('')
  const [cargandoParseo, setCargandoParseo] = useState(false)

  const cargar = () => {
    if (!id) return
    Promise.all([obtenerConcurso(id), listarItems(id)])
      .then(([c, its]) => { setConcurso(c); setItems(its) })
      .catch(() => toast.error('No se pudo cargar el concurso'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { cargar() }, [id])

  const iniciarEdicion = (item: Item) => {
    setEditandoId(item.id)
    setEditForm({ preguntasSolicitadas: item.preguntasSolicitadas })
  }
  const cancelarEdicion = () => setEditandoId(null)

  const guardarEdicion = async (item: Item) => {
    if (editForm.preguntasSolicitadas < 1 || editForm.preguntasSolicitadas > 200) {
      toast.error('La cantidad debe estar entre 1 y 200')
      return
    }
    setGuardando(true)
    try {
      const actualizado = await actualizarItem(item.concursoId, item.id, {
        nombre: item.nombre,
        tipoPrueba: item.tipoPrueba,
        preguntasSolicitadas: editForm.preguntasSolicitadas,
      })
      setItems(prev => prev.map(i => i.id === actualizado.id ? actualizado : i))
      setEditandoId(null)
      toast.success('Tema actualizado')
    } catch {
      toast.error('No se pudo guardar. Intenta de nuevo.')
    } finally {
      setGuardando(false)
    }
  }

  const handleEliminar = async (item: Item) => {
    if (!window.confirm(`¿Eliminar el tema "${item.nombre}"?`)) return
    try {
      await eliminarItem(item.concursoId, item.id)
      setItems(prev => prev.filter(i => i.id !== item.id))
      toast.success('Tema eliminado')
    } catch {
      toast.error('No se pudo eliminar el tema')
    }
  }

  const handleParsear = async () => {
    if (!textoParseo.trim() || !id) return
    setCargandoParseo(true)
    try {
      const nuevos = await parsearIndicadores(id, textoParseo)
      setItems(prev => [...prev, ...nuevos])
      setTextoParseo('')
      setMostraParseo(false)
      toast.success(`Se agregaron ${nuevos.length} tema(s)`)
    } catch {
      toast.error('No se pudieron agregar los temas')
    } finally {
      setCargandoParseo(false)
    }
  }

  if (loading) return <Layout><div className="flex justify-center py-20"><Spinner size="lg" /></div></Layout>
  if (!concurso) return <Layout><div className="alert-error"><AlertCircle size={20} /> No encontramos este concurso.</div></Layout>

  const funcionales      = items.filter(i => i.tipoPrueba === 'FUNCIONAL')
  const comportamentales = items.filter(i => i.tipoPrueba === 'COMPORTAMENTAL')
  const integridad       = items.filter(i => i.tipoPrueba === 'INTEGRIDAD')

  // Tarjetas de resumen: al hacer clic filtran la tabla por ese tipo.
  const resumen: { label: string; value: number; color: string; filtro: Filtro }[] = [
    { label: 'Temas en total',   value: items.length,            color: 'text-slate-800',  filtro: 'TODOS' },
    { label: 'Técnicos',         value: funcionales.length,      color: 'text-sky-700',    filtro: 'FUNCIONAL' },
    { label: 'Comportamentales', value: comportamentales.length, color: 'text-violet-700', filtro: 'COMPORTAMENTAL' },
    { label: 'Integridad',       value: integridad.length,       color: 'text-amber-700',  filtro: 'INTEGRIDAD' },
  ]

  const itemsFiltrados = filtro === 'TODOS' ? items : items.filter(i => i.tipoPrueba === filtro)

  return (
    <Layout>
      <PageHeader
        titulo={concurso.nombre}
        volverA="/concursos"
        volverLabel="Mis concursos"
        descripcion={[concurso.nivelCargo, concurso.denominacion].filter(Boolean).join(' — ') || undefined}
        acciones={
          <>
            <Link to={`/concursos/${concurso.id}/simulacro/nuevo`} className="btn-primary btn-sm">
              <Play size={18} /> Hacer un simulacro
            </Link>
            <Link to={`/concursos/${concurso.id}/brechas`} className="btn-secondary btn-sm">
              <BarChart3 size={18} /> Ver mi progreso
            </Link>
          </>
        }
      />

      {/* Fecha de examen destacada */}
      {concurso.fechaExamen && concurso.diasRestantes > 0 && (
        <div className={`mb-6 rounded-xl px-4 py-3 flex items-center gap-3 text-sm font-medium
          ${concurso.diasRestantes <= 30 ? 'bg-red-50 text-red-700' : concurso.diasRestantes <= 90 ? 'bg-amber-50 text-amber-800' : 'bg-emerald-50 text-emerald-700'}`}>
          <CalendarDays size={20} />
          Tu examen es el {new Date(concurso.fechaExamen).toLocaleDateString('es-CO', { day: 'numeric', month: 'long', year: 'numeric' })}
          {' · '}faltan {concurso.diasRestantes} días.
        </div>
      )}

      {/* Resumen (tarjetas clicables que filtran la tabla) */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4 mb-6">
        {resumen.map(s => {
          const activo = filtro === s.filtro
          return (
            <button
              key={s.label}
              type="button"
              onClick={() => setFiltro(s.filtro)}
              aria-pressed={activo}
              title={s.filtro === 'TODOS' ? 'Mostrar todos los temas' : `Ver solo los temas de tipo ${s.label}`}
              className={`card py-4 text-center transition-all focus-visible:ring-2 focus-visible:ring-brand-500
                ${activo ? 'ring-2 ring-brand-500 border-brand-300 shadow-card-hover' : 'hover:shadow-card-hover hover:-translate-y-0.5'}`}
            >
              <div className={`text-3xl font-bold ${s.color}`}>{s.value}</div>
              <div className="text-sm text-slate-500 mt-1">{s.label}</div>
            </button>
          )
        })}
      </div>

      {/* Tabla de temas */}
      <div className="card p-0 overflow-hidden mb-6">
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between gap-3 flex-wrap">
          <h2 className="font-bold text-slate-800 flex items-center gap-2">
            <ListChecks size={18} className="text-brand-600" />
            Temas a estudiar
            {filtro !== 'TODOS' && (
              <span className="inline-flex items-center gap-1 text-sm font-normal text-slate-500">
                · {TIPO_LABEL[filtro]}
                <button
                  onClick={() => setFiltro('TODOS')}
                  className="inline-flex items-center gap-0.5 text-brand-700 hover:underline"
                >
                  <X size={13} /> quitar filtro
                </button>
              </span>
            )}
          </h2>
          <button onClick={() => setMostraParseo(!mostraParseo)} className="btn-primary btn-sm">
            <Plus size={16} /> Agregar temas
          </button>
        </div>

        {/* Panel agregar */}
        {mostraParseo && (
          <div className="px-5 py-4 bg-brand-50/60 border-b border-brand-100 animate-fade-in">
            <p className="text-sm text-slate-700 mb-2 font-semibold">Pega los nuevos temas, uno por línea:</p>
            <textarea
              className="input-field font-mono text-sm h-28 resize-none"
              placeholder={'RAZONAMIENTO ANALÍTICO\nMISIONALIDAD DE LA DIAN\nCOMPROMISO'}
              value={textoParseo}
              onChange={e => setTextoParseo(e.target.value)}
            />
            <div className="flex gap-2 mt-3">
              <button onClick={handleParsear} disabled={cargandoParseo || !textoParseo.trim()} className="btn-primary btn-sm">
                {cargandoParseo && <Spinner size="sm" />}
                {cargandoParseo ? 'Agregando…' : 'Agregar'}
              </button>
              <button onClick={() => { setMostraParseo(false); setTextoParseo('') }} className="btn-secondary btn-sm">
                Cancelar
              </button>
            </div>
          </div>
        )}

        {items.length === 0 ? (
          <div className="text-center py-14 px-6 text-slate-500">
            <p className="font-medium">Todavía no hay temas.</p>
            <p className="text-sm mt-1">Usa el botón "Agregar temas" para empezar.</p>
          </div>
        ) : itemsFiltrados.length === 0 ? (
          <div className="text-center py-14 px-6 text-slate-500">
            <p className="font-medium">No hay temas de tipo {TIPO_LABEL[filtro as TipoPrueba]}.</p>
            <button onClick={() => setFiltro('TODOS')} className="text-brand-700 hover:underline text-sm mt-1">
              Ver todos los temas
            </button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm min-w-[720px]">
              <thead className="bg-slate-50 text-slate-500">
                <tr>
                  <th className="text-left px-5 py-3 font-semibold">Tema</th>
                  <th className="text-left px-3 py-3 font-semibold">Tipo</th>
                  <th className="text-center px-3 py-3 font-semibold">
                    <span className="inline-flex items-center gap-1">
                      Preguntas
                      <InfoTip texto="Cuántas preguntas de práctica quieres crear para este tema. Puedes cambiarlo cuando quieras." />
                    </span>
                  </th>
                  <th className="text-center px-3 py-3 font-semibold">
                    <span className="inline-flex items-center gap-1">
                      Dominio
                      <InfoTip texto="Qué tan bien dominas el tema según tu último simulacro. Empieza en 'Sin practicar' y se ajusta solo cuando haces simulacros." />
                    </span>
                  </th>
                  <th className="text-center px-3 py-3 font-semibold">
                    <span className="inline-flex items-center gap-1">
                      Meta
                      <InfoTip texto="Puntaje mínimo para considerar que dominas el tema. Lo define el tipo de prueba." />
                    </span>
                  </th>
                  <th className="text-center px-3 py-3 font-semibold">Preguntas de práctica</th>
                  <th className="text-center px-5 py-3 font-semibold">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {itemsFiltrados.map((item) => {
                  const editando = editandoId === item.id
                  return (
                    <tr key={item.id} className={editando ? 'bg-brand-50/50' : 'hover:bg-slate-50'}>
                      <td className="px-5 py-3 font-semibold text-slate-800">
                        {item.nombre}
                        {item.nivelCompetencia && (
                          <span className="ml-1.5 text-xs text-brand-500 font-normal">Nivel {item.nivelCompetencia}</span>
                        )}
                      </td>
                      <td className="px-3 py-3">
                        <span className={TIPO_CHIP[item.tipoPrueba]}>{TIPO_LABEL[item.tipoPrueba]}</span>
                      </td>
                      <td className="px-3 py-3 text-center">
                        {editando ? (
                          <input
                            type="number" min={1} max={200}
                            value={editForm.preguntasSolicitadas}
                            onChange={e => setEditForm({ preguntasSolicitadas: Number(e.target.value) })}
                            className="w-20 text-center border-2 border-brand-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-200"
                          />
                        ) : (
                          <span className="font-bold text-brand-700 text-base">{item.preguntasSolicitadas}</span>
                        )}
                      </td>
                      <td className="px-3 py-3">
                        <div className="flex justify-center">
                          <CeldaDominio item={item} />
                        </div>
                      </td>
                      <td className="px-3 py-3 text-center text-sm text-slate-500">{item.umbralBrecha}%</td>
                      <td className="px-3 py-3">
                        <div className="flex justify-center">
                          <GeneracionItem
                            itemId={item.id}
                            cantidad={editando ? editForm.preguntasSolicitadas : item.preguntasSolicitadas}
                            deshabilitado={editando}
                          />
                        </div>
                      </td>
                      <td className="px-5 py-3">
                        {editando ? (
                          <div className="flex items-center justify-center gap-1.5">
                            <button onClick={() => guardarEdicion(item)} disabled={guardando} className="btn-success btn-sm">
                              {guardando ? <Spinner size="sm" /> : <Check size={15} />} Guardar
                            </button>
                            <button onClick={cancelarEdicion} className="btn-secondary btn-sm">
                              <X size={15} />
                            </button>
                          </div>
                        ) : (
                          <div className="flex items-center justify-center gap-1">
                            <button onClick={() => iniciarEdicion(item)} className="inline-flex items-center gap-1 text-sm text-brand-700 hover:bg-brand-50 px-2.5 py-1.5 rounded-lg font-medium" title="Editar">
                              <Pencil size={15} /> Editar
                            </button>
                            <button onClick={() => handleEliminar(item)} className="inline-flex items-center text-slate-400 hover:text-red-600 hover:bg-red-50 p-1.5 rounded-lg" title="Eliminar tema">
                              <Trash2 size={16} />
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Ayuda sobre generación */}
      <div className="alert-info">
        <Sparkles size={22} className="shrink-0 mt-0.5" />
        <div>
          <p className="font-semibold text-brand-900">Genera tus preguntas de práctica</p>
          <p className="text-sm mt-0.5 leading-relaxed">
            Pulsa <strong>Generar</strong> en cada tema para crear sus preguntas. El proceso trabaja
            en segundo plano, así que puedes seguir usando la app mientras tanto. Cuando termine, ya podrás
            hacer un simulacro. El <strong>dominio</strong> de cada tema empieza en "Sin practicar" y sube
            a medida que haces simulacros.
          </p>
        </div>
      </div>
    </Layout>
  )
}
