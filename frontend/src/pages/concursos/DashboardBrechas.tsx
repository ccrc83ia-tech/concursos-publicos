import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Link } from 'react-router-dom'
import {
  ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Cell,
  RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar, ReferenceLine,
} from 'recharts'
import {
  Sparkles, BarChart3, BookOpen, Clock, Play, Info, ExternalLink,
} from 'lucide-react'
import { obtenerBrechas } from '@/api/simulacros'
import { obtenerRecomendacionesConcurso, generarRecomendaciones } from '@/api/recomendaciones'
import { listarItems } from '@/api/items'
import type { Desempeno, Item, NivelDominio, Recomendacion } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'
import PageHeader from '@/components/shared/PageHeader'
import EmptyState from '@/components/shared/EmptyState'

const DOMINIO_HEX: Record<NivelDominio, string> = {
  ROJO: '#ef4444', AMARILLO: '#eab308', VERDE: '#22c55e',
}
const DOMINIO_LABEL: Record<NivelDominio, string> = {
  ROJO: '🔴 Necesita refuerzo', AMARILLO: '🟡 Vas mejorando', VERDE: '🟢 Dominado',
}

/** Acorta nombres largos para que quepan en los ejes. */
const corto = (s: string, max = 22) => (s.length > max ? s.slice(0, max - 1) + '…' : s)

export default function DashboardBrechas() {
  const { concursoId } = useParams<{ concursoId: string }>()
  const [brechas, setBrechas]     = useState<Desempeno[]>([])
  const [items, setItems]         = useState<Item[]>([])
  const [recs, setRecs]           = useState<Recomendacion[]>([])
  const [loading, setLoading]     = useState(true)
  const [generando, setGenerando] = useState(false)
  const [error, setError]         = useState<string | null>(null)

  const cargar = async () => {
    if (!concursoId) return
    const [des, its, rs] = await Promise.all([
      obtenerBrechas(concursoId),
      listarItems(concursoId).catch(() => [] as Item[]),
      obtenerRecomendacionesConcurso(concursoId).catch(() => [] as Recomendacion[]),
    ])
    setBrechas(des)
    setItems(its)
    setRecs(rs)
  }

  useEffect(() => {
    setLoading(true)
    cargar().catch(() => setError('No se pudo cargar tu progreso')).finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [concursoId])

  const nombreItem = (itemId: string) => items.find(i => i.id === itemId)?.nombre ?? 'Tema'

  const datos = useMemo(
    () => brechas.map(b => ({
      itemId: b.itemId,
      nombre: nombreItem(b.itemId),
      nombreCorto: corto(nombreItem(b.itemId)),
      puntaje: b.puntajePorcentual,
      nivel: b.nivelDominio,
      correctas: b.preguntasCorrectas,
      totalPreg: b.totalPreguntas,
    })),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [brechas, items],
  )

  const promedio = datos.length
    ? Math.round((datos.reduce((a, d) => a + d.puntaje, 0) / datos.length) * 10) / 10
    : 0
  const criticos = datos.filter(d => d.nivel === 'ROJO').length
  const dominados = datos.filter(d => d.nivel === 'VERDE').length

  const recPorItem = useMemo(() => {
    const m = new Map<string, Recomendacion>()
    recs.forEach(r => { if (!m.has(r.itemId)) m.set(r.itemId, r) })
    return m
  }, [recs])

  const simulacroId = brechas[0]?.simulacroId

  const handleGenerar = async () => {
    if (!simulacroId) return
    setGenerando(true)
    try {
      await generarRecomendaciones(simulacroId)
      await cargar()
    } catch {
      setError('No se pudieron generar las recomendaciones')
    } finally {
      setGenerando(false)
    }
  }

  if (loading) return <Layout><div className="flex justify-center py-20"><Spinner size="lg" /></div></Layout>

  return (
    <Layout>
      <div className="max-w-5xl mx-auto">
        <PageHeader
          titulo="Mi progreso"
          volverA={`/concursos/${concursoId}`}
          volverLabel="Volver al concurso"
          descripcion="Aquí ves en qué temas vas bien y cuáles necesitas reforzar, según tus simulacros."
        />

        {error && <div className="alert-error mb-4"><Info size={20} /> {error}</div>}

        {datos.length === 0 ? (
          <EmptyState
            icono={BarChart3}
            titulo="Todavía no hay datos de tu progreso"
            mensaje="Cuando completes un simulacro, aquí verás cómo te fue en cada tema y qué estudiar."
            accion={
              <Link to={`/concursos/${concursoId}/simulacro/nuevo`} className="btn-primary btn-lg">
                <Play size={20} /> Hacer mi primer simulacro
              </Link>
            }
          />
        ) : (
          <>
            {/* Resumen */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
              <div className="card text-center">
                <div className={`text-3xl font-bold ${promedio >= 70 ? 'text-emerald-600' : promedio >= 50 ? 'text-amber-600' : 'text-red-600'}`}>
                  {promedio}%
                </div>
                <p className="text-slate-500 text-sm mt-1">Promedio general</p>
              </div>
              <div className="card text-center">
                <div className="text-3xl font-bold text-red-600">{criticos}</div>
                <p className="text-slate-500 text-sm mt-1">Temas por reforzar</p>
              </div>
              <div className="card text-center">
                <div className="text-3xl font-bold text-emerald-600">{dominados}</div>
                <p className="text-slate-500 text-sm mt-1">Temas dominados</p>
              </div>
            </div>

            {/* Glosario de colores */}
            <div className="card mb-6 flex flex-wrap items-center gap-x-6 gap-y-2 py-4">
              <span className="text-sm font-semibold text-slate-600">¿Qué significan los colores?</span>
              <span className="flex items-center gap-2 text-sm text-slate-600"><span className="w-3.5 h-3.5 rounded-full bg-red-500" /> Necesita refuerzo (menos del 50%)</span>
              <span className="flex items-center gap-2 text-sm text-slate-600"><span className="w-3.5 h-3.5 rounded-full bg-amber-400" /> Vas mejorando</span>
              <span className="flex items-center gap-2 text-sm text-slate-600"><span className="w-3.5 h-3.5 rounded-full bg-emerald-500" /> Dominado (meta alcanzada)</span>
            </div>

            {/* Barras por tema */}
            <div className="card mb-6">
              <h2 className="font-bold text-slate-800 mb-4">Puntaje por tema</h2>
              <ResponsiveContainer width="100%" height={Math.max(220, datos.length * 42)}>
                <BarChart data={datos} layout="vertical" margin={{ left: 12, right: 24 }}>
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                  <XAxis type="number" domain={[0, 100]} unit="%" tick={{ fontSize: 12 }} />
                  <YAxis type="category" dataKey="nombreCorto" width={160} tick={{ fontSize: 12 }} />
                  <Tooltip
                    formatter={(v: number) => [`${v}%`, 'Puntaje']}
                    labelFormatter={(_, p) => {
                      const d: any = p?.[0]?.payload
                      if (!d) return ''
                      return `${d.nombre} — ${d.correctas}/${d.totalPreg} correctas`
                    }}
                  />
                  <ReferenceLine x={70} stroke="#64748b" strokeDasharray="4 4" label={{ value: 'Meta 70%', position: 'top', fontSize: 11, fill: '#64748b' }} />
                  <Bar dataKey="puntaje" radius={[0, 4, 4, 0]} barSize={20}>
                    {datos.map(d => <Cell key={d.itemId} fill={DOMINIO_HEX[d.nivel]} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>

            {/* Radar */}
            {datos.length >= 3 && (
              <div className="card mb-6">
                <h2 className="font-bold text-slate-800 mb-1">Vista general</h2>
                <p className="text-sm text-slate-500 mb-4">Mientras más amplia sea la figura, mejor dominas el conjunto de temas.</p>
                <ResponsiveContainer width="100%" height={320}>
                  <RadarChart data={datos} outerRadius="75%">
                    <PolarGrid />
                    <PolarAngleAxis dataKey="nombreCorto" tick={{ fontSize: 11 }} />
                    <PolarRadiusAxis angle={90} domain={[0, 100]} tick={{ fontSize: 10 }} />
                    <Radar name="Puntaje" dataKey="puntaje" stroke="#17487f" fill="#17487f" fillOpacity={0.35} />
                    <Tooltip formatter={(v: number) => [`${v}%`, 'Puntaje']} labelFormatter={(_, p) => p?.[0]?.payload?.nombre ?? ''} />
                  </RadarChart>
                </ResponsiveContainer>
              </div>
            )}

            {/* Plan de estudio */}
            <div className="card">
              <div className="flex items-center justify-between gap-3 mb-4 flex-wrap">
                <h2 className="font-bold text-slate-800 flex items-center gap-2">
                  <BookOpen size={18} className="text-brand-600" /> Qué estudiar ahora
                </h2>
                {simulacroId && (
                  <button onClick={handleGenerar} disabled={generando} className="btn-primary btn-sm">
                    {generando ? <Spinner size="sm" /> : <Sparkles size={16} />}
                    {recs.length ? 'Actualizar plan' : 'Crear plan de estudio'}
                  </button>
                )}
              </div>

              {recs.length === 0 ? (
                <div className="text-center py-8">
                  <Sparkles size={32} className="mx-auto text-brand-300 mb-3" />
                  <p className="text-slate-500 max-w-md mx-auto">
                    Pulsa "Crear plan de estudio" y la inteligencia artificial te dirá en qué enfocarte,
                    priorizando los temas donde más fallaste.
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {datos
                    .filter(d => recPorItem.has(d.itemId))
                    .map(d => {
                      const r = recPorItem.get(d.itemId)!
                      return (
                        <div key={d.itemId} className="border-t border-slate-100 pt-4 first:border-t-0 first:pt-0">
                          <div className="flex items-center justify-between gap-2 mb-1.5 flex-wrap">
                            <span className="font-semibold text-slate-900">
                              {d.nombre}
                              {d.totalPreg > 0 && d.totalPreg < 5 && (
                                <span className="ml-2 text-xs text-amber-600 font-normal" title={`Basado en solo ${d.totalPreg} pregunta(s)`}>
                                  ⚠ pocas preguntas
                                </span>
                              )}
                            </span>
                            <span className="text-sm font-medium" style={{ color: DOMINIO_HEX[d.nivel] }}>
                              {DOMINIO_LABEL[d.nivel]} · {d.puntaje}%
                            </span>
                          </div>
                          {r.resumenEjecutivo && <p className="text-sm text-slate-600 mb-2">{r.resumenEjecutivo}</p>}
                          {r.temasFallidos.length > 0 && (
                            <ul className="text-sm text-slate-700 list-disc pl-5 space-y-1 mb-2">
                              {r.temasFallidos.map((t, i) => (
                                <li key={i}><span className="font-medium">{t.tema}</span>{t.descripcion ? `: ${t.descripcion}` : ''}</li>
                              ))}
                            </ul>
                          )}
                          {r.referenciasNormativas.length > 0 && (
                            <div className="flex flex-wrap gap-2 mb-1">
                              {r.referenciasNormativas.map((ref, i) => {
                                const etiqueta = [ref.norma, ref.articulo].filter(Boolean).join(' ')
                                return ref.url
                                  ? <a key={i} href={ref.url} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 text-xs bg-brand-50 text-brand-700 rounded-lg px-2.5 py-1 hover:underline"><ExternalLink size={13} /> {etiqueta}</a>
                                  : <span key={i} className="inline-flex items-center gap-1 text-xs bg-slate-100 text-slate-600 rounded-lg px-2.5 py-1"><BookOpen size={13} /> {etiqueta}</span>
                              })}
                            </div>
                          )}
                          {r.tiempoEstudioHoras != null && (
                            <p className="flex items-center gap-1.5 text-xs text-slate-400 mt-1">
                              <Clock size={13} /> Tiempo estimado de estudio: {r.tiempoEstudioHoras} h
                            </p>
                          )}
                        </div>
                      )
                    })}
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </Layout>
  )
}
