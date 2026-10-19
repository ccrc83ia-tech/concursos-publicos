import { useEffect, useMemo, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Cell,
  RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar, ReferenceLine,
} from 'recharts'
import { obtenerBrechas } from '@/api/simulacros'
import { obtenerRecomendacionesConcurso, generarRecomendaciones } from '@/api/recomendaciones'
import { listarItems } from '@/api/items'
import type { Desempeno, Item, NivelDominio, Recomendacion } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'

const DOMINIO_HEX: Record<NivelDominio, string> = {
  ROJO:     '#ef4444',
  AMARILLO: '#eab308',
  VERDE:    '#22c55e',
}
const DOMINIO_LABEL: Record<NivelDominio, string> = {
  ROJO: '🔴 Brecha crítica', AMARILLO: '🟡 En progreso', VERDE: '🟢 Dominado',
}

/** Acorta nombres largos de indicadores para que quepan en los ejes. */
const corto = (s: string, max = 22) => (s.length > max ? s.slice(0, max - 1) + '…' : s)

export default function DashboardBrechas() {
  const { concursoId } = useParams<{ concursoId: string }>()
  const [brechas, setBrechas]       = useState<Desempeno[]>([])
  const [items, setItems]           = useState<Item[]>([])
  const [recs, setRecs]             = useState<Recomendacion[]>([])
  const [loading, setLoading]       = useState(true)
  const [generando, setGenerando]   = useState(false)
  const [error, setError]           = useState<string | null>(null)

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
    cargar().catch(() => setError('No se pudieron cargar las brechas')).finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [concursoId])

  const nombreItem = (itemId: string) => items.find(i => i.id === itemId)?.nombre ?? 'Indicador'

  const datos = useMemo(
    () => brechas.map(b => ({
      itemId:     b.itemId,
      nombre:     nombreItem(b.itemId),
      nombreCorto: corto(nombreItem(b.itemId)),
      puntaje:    b.puntajePorcentual,
      nivel:      b.nivelDominio,
    })),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [brechas, items],
  )

  const promedio = datos.length
    ? Math.round((datos.reduce((a, d) => a + d.puntaje, 0) / datos.length) * 10) / 10
    : 0
  const criticos = datos.filter(d => d.nivel === 'ROJO').length
  const dominados = datos.filter(d => d.nivel === 'VERDE').length

  // Última recomendación por ítem para mostrar junto a cada brecha.
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
        <Link to={`/concursos/${concursoId}`} className="text-sm text-gray-500 hover:text-gray-700">← Volver al concurso</Link>
        <h1 className="text-2xl font-bold text-gray-900 mt-1 mb-6">Dashboard de brechas</h1>

        {error && <p className="text-red-500 text-sm mb-4">{error}</p>}

        {datos.length === 0 ? (
          <div className="card text-center py-12">
            <p className="text-gray-500">Aún no hay desempeño registrado para este concurso.</p>
            <p className="text-gray-400 text-sm mt-1">Completa un simulacro para ver tus brechas aquí.</p>
            <Link to={`/concursos/${concursoId}/simulacro/nuevo`} className="inline-block mt-4 text-blue-600 hover:underline text-sm">
              Crear simulacro →
            </Link>
          </div>
        ) : (
          <>
            {/* Resumen */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
              <div className="card text-center">
                <div className={`text-3xl font-bold ${promedio >= 70 ? 'text-green-600' : promedio >= 50 ? 'text-yellow-600' : 'text-red-600'}`}>
                  {promedio}%
                </div>
                <p className="text-gray-500 text-sm mt-1">Promedio general</p>
              </div>
              <div className="card text-center">
                <div className="text-3xl font-bold text-red-600">{criticos}</div>
                <p className="text-gray-500 text-sm mt-1">Brechas críticas</p>
              </div>
              <div className="card text-center">
                <div className="text-3xl font-bold text-green-600">{dominados}</div>
                <p className="text-gray-500 text-sm mt-1">Indicadores dominados</p>
              </div>
            </div>

            {/* Barras por indicador */}
            <div className="card mb-6">
              <h2 className="font-semibold mb-4">Desempeño por indicador</h2>
              <ResponsiveContainer width="100%" height={Math.max(220, datos.length * 42)}>
                <BarChart data={datos} layout="vertical" margin={{ left: 12, right: 24 }}>
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                  <XAxis type="number" domain={[0, 100]} unit="%" tick={{ fontSize: 12 }} />
                  <YAxis type="category" dataKey="nombreCorto" width={160} tick={{ fontSize: 12 }} />
                  <Tooltip
                    formatter={(v: number) => [`${v}%`, 'Puntaje']}
                    labelFormatter={(_, p) => p?.[0]?.payload?.nombre ?? ''}
                  />
                  <ReferenceLine x={70} stroke="#64748b" strokeDasharray="4 4" label={{ value: 'Meta 70%', position: 'top', fontSize: 11, fill: '#64748b' }} />
                  <Bar dataKey="puntaje" radius={[0, 4, 4, 0]} barSize={20}>
                    {datos.map(d => <Cell key={d.itemId} fill={DOMINIO_HEX[d.nivel]} />)}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>

            {/* Radar de cobertura (visión global) */}
            {datos.length >= 3 && (
              <div className="card mb-6">
                <h2 className="font-semibold mb-4">Mapa de cobertura</h2>
                <ResponsiveContainer width="100%" height={320}>
                  <RadarChart data={datos} outerRadius="75%">
                    <PolarGrid />
                    <PolarAngleAxis dataKey="nombreCorto" tick={{ fontSize: 11 }} />
                    <PolarRadiusAxis angle={90} domain={[0, 100]} tick={{ fontSize: 10 }} />
                    <Radar name="Puntaje" dataKey="puntaje" stroke="#003F7F" fill="#003F7F" fillOpacity={0.35} />
                    <Tooltip formatter={(v: number) => [`${v}%`, 'Puntaje']} labelFormatter={(_, p) => p?.[0]?.payload?.nombre ?? ''} />
                  </RadarChart>
                </ResponsiveContainer>
              </div>
            )}

            {/* Plan de estudio con IA */}
            <div className="card">
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-semibold">Plan de estudio recomendado</h2>
                {simulacroId && (
                  <button
                    onClick={handleGenerar}
                    disabled={generando}
                    className="text-sm px-3 py-1.5 rounded-md bg-[#003F7F] text-white hover:bg-blue-800 disabled:opacity-50 flex items-center gap-2"
                  >
                    {generando && <Spinner size="sm" />}
                    {recs.length ? 'Regenerar con IA' : 'Generar con IA'}
                  </button>
                )}
              </div>

              {recs.length === 0 ? (
                <p className="text-sm text-gray-400">
                  Genera recomendaciones con IA para recibir un plan priorizado sobre tus brechas.
                </p>
              ) : (
                <div className="space-y-4">
                  {datos
                    .filter(d => recPorItem.has(d.itemId))
                    .map(d => {
                      const r = recPorItem.get(d.itemId)!
                      return (
                        <div key={d.itemId} className="border-t border-gray-100 pt-4 first:border-t-0 first:pt-0">
                          <div className="flex items-center justify-between mb-1">
                            <span className="font-medium text-gray-900 text-sm">{d.nombre}</span>
                            <span className="text-xs" style={{ color: DOMINIO_HEX[d.nivel] }}>
                              {DOMINIO_LABEL[d.nivel]} · {d.puntaje}%
                            </span>
                          </div>
                          {r.resumenEjecutivo && (
                            <p className="text-sm text-gray-600 mb-2">{r.resumenEjecutivo}</p>
                          )}
                          {r.temasFallidos.length > 0 && (
                            <ul className="text-sm text-gray-700 list-disc pl-5 space-y-1 mb-2">
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
                                  ? <a key={i} href={ref.url} target="_blank" rel="noreferrer" className="text-xs bg-blue-50 text-blue-700 rounded px-2 py-1 hover:underline">📖 {etiqueta}</a>
                                  : <span key={i} className="text-xs bg-gray-100 text-gray-600 rounded px-2 py-1">📖 {etiqueta}</span>
                              })}
                            </div>
                          )}
                          {r.tiempoEstudioHoras != null && (
                            <p className="text-xs text-gray-400">⏱️ Tiempo estimado de estudio: {r.tiempoEstudioHoras} h</p>
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
