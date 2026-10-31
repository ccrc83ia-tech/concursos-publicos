import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { toast } from 'sonner'
import { Play, Clock, AlertCircle, CheckSquare, ListChecks } from 'lucide-react'
import { obtenerConcurso } from '@/api/concursos'
import { listarItems } from '@/api/items'
import { obtenerEstadoBanco } from '@/api/generacion'
import { crearSimulacro } from '@/api/simulacros'
import type { BancoEstado, Concurso, Item } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'
import PageHeader from '@/components/shared/PageHeader'

const TIPO_LABEL: Record<string, string> = {
  FUNCIONAL: 'Técnico', COMPORTAMENTAL: 'Comportamental', INTEGRIDAD: 'Integridad',
}

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

  useEffect(() => {
    if (!concursoId) return
    Promise.all([obtenerConcurso(concursoId), listarItems(concursoId)])
      .then(([c, its]) => {
        setConcurso(c)
        setItems(its)
        setSeleccion(new Set(its.map(i => i.id)))
        its.forEach(it => {
          obtenerEstadoBanco(it.id)
            .then(b => setBancos(prev => ({ ...prev, [it.id]: b })))
            .catch(() => { /* sin banco: se trata como vacío */ })
        })
      })
      .catch(() => toast.error('No se pudo cargar el concurso'))
      .finally(() => setLoading(false))
  }, [concursoId])

  const disponibles = (itemId: string) => bancos[itemId]?.total ?? 0

  const toggle = (id: string) => {
    setSeleccion(prev => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  const totalPreguntas = Array.from(seleccion).reduce((suma, id) => {
    const banco = bancos[id]
    const disp = banco ? banco.total : preguntasPorItem
    return suma + Math.min(preguntasPorItem, disp)
  }, 0)

  const handleCrear = async () => {
    if (!concursoId || seleccion.size === 0) {
      toast.error('Elige al menos un tema para practicar')
      return
    }
    setCreando(true)
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
      toast.error(e?.response?.data?.mensaje ?? 'No se pudo crear el simulacro. ¿Ya generaste preguntas para estos temas?')
    } finally {
      setCreando(false)
    }
  }

  if (loading) return <Layout><div className="flex justify-center py-20"><Spinner size="lg" /></div></Layout>
  if (!concurso) return <Layout><div className="alert-error"><AlertCircle size={20} /> No encontramos este concurso.</div></Layout>

  const sinBancoSeleccionado = items.some(i => seleccion.has(i.id) && bancos[i.id] != null && disponibles(i.id) === 0)

  return (
    <Layout>
      <div className="max-w-2xl mx-auto">
        <PageHeader
          titulo="Preparar un simulacro"
          volverA={`/concursos/${concursoId}`}
          volverLabel="Volver al concurso"
          descripcion="Elige qué temas quieres practicar y cuántas preguntas. Luego empieza cuando estés listo."
        />

        {/* Temas */}
        <div className="card mb-4">
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-bold text-slate-800 flex items-center gap-2">
              <ListChecks size={18} className="text-brand-600" /> ¿Qué temas quieres practicar?
            </h2>
            <div className="flex gap-3 text-sm font-medium">
              <button onClick={() => setSeleccion(new Set(items.map(i => i.id)))} className="text-brand-700 hover:underline">Todos</button>
              <button onClick={() => setSeleccion(new Set())} className="text-slate-500 hover:underline">Ninguno</button>
            </div>
          </div>
          <p className="text-sm text-slate-500 mb-3">{seleccion.size} de {items.length} temas elegidos</p>

          <div className="max-h-72 overflow-y-auto rounded-xl border border-slate-200 divide-y divide-slate-100">
            {items.map(item => {
              const banco = bancos[item.id]
              const total = disponibles(item.id)
              const vacio = banco != null && total === 0
              const insuficiente = banco != null && total > 0 && total < preguntasPorItem
              const marcado = seleccion.has(item.id)
              return (
                <label
                  key={item.id}
                  className={`flex items-center gap-3 px-4 py-3 cursor-pointer transition-colors ${vacio ? 'opacity-60' : 'hover:bg-slate-50'}`}
                >
                  <input
                    type="checkbox"
                    checked={marcado}
                    onChange={() => toggle(item.id)}
                    className="w-5 h-5 rounded accent-brand-700"
                  />
                  <span className="flex-1 text-slate-800 font-medium">{item.nombre}</span>

                  {banco === undefined ? (
                    <span className="text-xs text-slate-300">…</span>
                  ) : vacio ? (
                    <span className="badge-rojo" title="Aún no tiene preguntas. Genéralas en el concurso.">Sin preguntas</span>
                  ) : insuficiente ? (
                    <span className="badge-amarillo" title={`Hay ${total} preguntas disponibles`}>{total} preguntas</span>
                  ) : (
                    <span className="text-sm text-slate-500">{total} preguntas</span>
                  )}

                  <span className="text-xs text-slate-400 w-28 text-right hidden sm:block">{TIPO_LABEL[item.tipoPrueba]}</span>
                </label>
              )
            })}
          </div>

          {sinBancoSeleccionado && (
            <div className="alert-warning mt-3 text-sm">
              <AlertCircle size={18} className="shrink-0 mt-0.5" />
              <span>Algunos temas elegidos aún no tienen preguntas y se omitirán. Puedes generarlas desde el concurso.</span>
            </div>
          )}
        </div>

        {/* Opciones */}
        <div className="card mb-4 space-y-5">
          <div>
            <label className="label" htmlFor="ppi">¿Cuántas preguntas por tema?</label>
            <input
              id="ppi" type="number" min={1} max={50} value={preguntasPorItem}
              onChange={e => setPreguntasPorItem(Math.max(1, Number(e.target.value)))}
              className="input-field max-w-[160px]"
            />
            <p className="help-text">Si un tema tiene menos, usaremos las que haya.</p>
          </div>

          <div>
            <label className="flex items-center gap-3 cursor-pointer mb-2">
              <input
                type="checkbox" checked={conTiempo}
                onChange={e => setConTiempo(e.target.checked)}
                className="w-5 h-5 rounded accent-brand-700"
              />
              <span className="label mb-0 flex items-center gap-1.5"><Clock size={18} /> Con tiempo límite</span>
            </label>
            {conTiempo && (
              <div className="flex items-center gap-2 pl-8">
                <input
                  type="number" min={1} max={300} value={minutos}
                  onChange={e => setMinutos(Math.max(1, Number(e.target.value)))}
                  className="input-field max-w-[120px]"
                />
                <span className="text-slate-600">minutos</span>
              </div>
            )}
            {!conTiempo && <p className="help-text pl-8">Podrás tomarte el tiempo que necesites.</p>}
          </div>
        </div>

        {/* Resumen + acción */}
        <div className="card bg-brand-50 border-brand-200 flex items-center justify-between gap-4 flex-wrap">
          <div className="flex items-center gap-2 text-brand-900">
            <CheckSquare size={20} />
            <span className="text-base">
              <strong>{totalPreguntas} preguntas</strong>{conTiempo && <> · {minutos} min</>}
            </span>
          </div>
          <button onClick={handleCrear} disabled={creando || seleccion.size === 0} className="btn-primary btn-lg">
            {creando ? <Spinner size="sm" /> : <Play size={20} />}
            {creando ? 'Preparando…' : 'Empezar simulacro'}
          </button>
        </div>
      </div>
    </Layout>
  )
}
