import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  BarChart3, ChevronDown, ChevronUp, Check, X, Lightbulb, BookOpen,
  Trophy, TrendingUp, Dumbbell, RotateCcw,
} from 'lucide-react'
import { obtenerSimulacro, obtenerDesempeno } from '@/api/simulacros'
import { listarItems } from '@/api/items'
import type { Simulacro, Desempeno, Item, NivelDominio, OpcionRespuesta } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'
import PageHeader from '@/components/shared/PageHeader'
import ReportarPregunta from '@/components/shared/ReportarPregunta'

const DOMINIO_COLOR: Record<NivelDominio, string> = {
  ROJO: 'bg-red-500', AMARILLO: 'bg-amber-400', VERDE: 'bg-emerald-500',
}
const DOMINIO_LABEL: Record<NivelDominio, string> = {
  ROJO: '🔴 Necesita refuerzo', AMARILLO: '🟡 Vas mejorando', VERDE: '🟢 Dominado',
}

export default function ResultadoSimulacro() {
  const { id } = useParams<{ id: string }>()
  const [sim, setSim]             = useState<Simulacro | null>(null)
  const [desempeno, setDesempeno] = useState<Desempeno[]>([])
  const [items, setItems]         = useState<Item[]>([])
  const [loading, setLoading]     = useState(true)
  const [verDetalle, setVerDetalle] = useState(false)

  useEffect(() => {
    if (!id) return
    obtenerSimulacro(id).then(async s => {
      setSim(s)
      const des = await obtenerDesempeno(id)
      setDesempeno(des)
      try { setItems(await listarItems(s.concursoId)) } catch { /* opcional */ }
    }).finally(() => setLoading(false))
  }, [id])

  if (loading) return <Layout><div className="flex justify-center py-20"><Spinner size="lg" /></div></Layout>
  if (!sim) return <Layout><p className="text-red-500">No encontramos este simulacro.</p></Layout>

  const nombreItem = (itemId: string) => items.find(i => i.id === itemId)?.nombre ?? 'Tema'
  const puntaje = sim.puntajeFinal ?? 0
  const correctas = sim.preguntas.filter(p => p.esCorrecta).length
  const letra = (op: OpcionRespuesta) =>
    ({ A: 'opcionA', B: 'opcionB', C: 'opcionC', D: 'opcionD' } as const)[op]

  const aprobado = puntaje >= 70
  const enProgreso = puntaje >= 50 && puntaje < 70
  const colorPuntaje = aprobado ? 'text-emerald-600' : enProgreso ? 'text-amber-600' : 'text-red-600'
  const anilloPuntaje = aprobado ? 'ring-emerald-500' : enProgreso ? 'ring-amber-400' : 'ring-red-400'
  const Icono = aprobado ? Trophy : enProgreso ? TrendingUp : Dumbbell
  const mensaje = aprobado
    ? 'Muy bien: superas el puntaje de aprobación. Sigue practicando para mantener tu nivel.'
    : enProgreso
      ? 'Vas por buen camino, pero aún no llegas al 70%. Refuerza tus temas más débiles para subir.'
      : 'Es un punto de partida. Revisa abajo qué temas necesitan trabajo y vuelve a intentarlo.'

  return (
    <Layout>
      <div className="max-w-3xl mx-auto">
        <PageHeader
          titulo="Resultado de tu simulacro"
          volverA={`/concursos/${sim.concursoId}`}
          volverLabel="Volver al concurso"
        />

        {/* Puntaje */}
        <div className="card text-center mb-6">
          <div className={`mx-auto w-36 h-36 rounded-full ring-8 ${anilloPuntaje} flex flex-col items-center justify-center mb-4`}>
            <span className={`text-4xl font-extrabold ${colorPuntaje}`}>{puntaje.toFixed(0)}%</span>
            <span className="text-xs text-slate-400 mt-0.5">tu puntaje</span>
          </div>
          <p className="flex items-center justify-center gap-2 text-slate-700 font-semibold text-lg">
            <Icono size={22} className={colorPuntaje} />
            {correctas} de {sim.preguntas.length} correctas
          </p>
          <p className="text-slate-500 text-base mt-2 max-w-md mx-auto leading-relaxed">{mensaje}</p>

          <div className="flex flex-wrap gap-3 justify-center mt-6">
            <Link to={`/concursos/${sim.concursoId}/brechas`} className="btn-primary">
              <BarChart3 size={18} /> Ver mi progreso
            </Link>
            <Link to={`/concursos/${sim.concursoId}/simulacro/nuevo`} className="btn-secondary">
              <RotateCcw size={18} /> Hacer otro simulacro
            </Link>
          </div>
        </div>

        {/* Desempeño por tema */}
        <div className="card mb-6">
          <h2 className="font-bold text-slate-800 mb-4">Cómo te fue en cada tema</h2>
          <div className="space-y-4">
            {desempeno.map(d => (
              <div key={d.itemId}>
                <div className="flex items-center justify-between text-sm mb-1.5">
                  <span className="text-slate-700 font-medium">{nombreItem(d.itemId)}</span>
                  <span className="text-slate-500">
                    {d.preguntasCorrectas}/{d.totalPreguntas} · {DOMINIO_LABEL[d.nivelDominio]}
                  </span>
                </div>
                <div className="h-2.5 w-full bg-slate-200 rounded-full overflow-hidden">
                  <div className={`h-full ${DOMINIO_COLOR[d.nivelDominio]} transition-all`} style={{ width: `${d.puntajePorcentual}%` }} />
                </div>
              </div>
            ))}
            {desempeno.length === 0 && <p className="text-sm text-slate-400">Sin datos de desempeño.</p>}
          </div>
        </div>

        {/* Revisión */}
        <div className="card">
          <button
            onClick={() => setVerDetalle(v => !v)}
            className="w-full flex items-center justify-between font-bold text-slate-800"
          >
            <span className="flex items-center gap-2"><BookOpen size={18} className="text-brand-600" /> Revisar mis respuestas</span>
            {verDetalle ? <ChevronUp size={20} className="text-slate-400" /> : <ChevronDown size={20} className="text-slate-400" />}
          </button>

          {verDetalle && (
            <div className="mt-5 space-y-6 animate-fade-in">
              {sim.preguntas.map((p, i) => (
                <div key={p.simulacroPreguntaId} className="border-t border-slate-100 pt-5">
                  <p className="font-medium text-slate-900 mb-3 flex items-start gap-2">
                    <span className={`shrink-0 inline-flex items-center justify-center w-6 h-6 rounded-full text-white text-xs ${p.esCorrecta ? 'bg-emerald-500' : 'bg-red-500'}`}>
                      {p.esCorrecta ? <Check size={14} /> : <X size={14} />}
                    </span>
                    <span>{i + 1}. {p.enunciado}</span>
                  </p>
                  <div className="text-sm space-y-1.5 mb-3 pl-8">
                    {(['A', 'B', 'C', 'D'] as OpcionRespuesta[])
                      .filter(op => ((p[letra(op)] as string) ?? '').trim() !== '')
                      .map(op => {
                        const esCorrecta = p.respuestaCorrecta === op
                        const esDada = p.respuestaDada === op
                        return (
                          <div key={op} className={`px-3 py-2 rounded-lg flex gap-2 items-center
                            ${esCorrecta ? 'bg-emerald-50 text-emerald-800' : esDada ? 'bg-red-50 text-red-700' : 'text-slate-600'}`}>
                            <span className="font-bold">{op}</span>
                            <span className="flex-1">{p[letra(op)]}</span>
                            {esCorrecta && <span className="text-xs font-semibold">✓ correcta</span>}
                            {esDada && !esCorrecta && <span className="text-xs">tu respuesta</span>}
                          </div>
                        )
                      })}
                  </div>
                  {p.justificacionCorrecta && (
                    <div className="ml-8 text-sm text-slate-600 bg-brand-50 rounded-lg p-3 flex gap-2">
                      <Lightbulb size={18} className="shrink-0 text-brand-500 mt-0.5" />
                      <div>
                        {p.justificacionCorrecta}
                        {p.referenciaNormativa && <span className="block text-slate-400 mt-1 text-xs">📖 {p.referenciaNormativa}</span>}
                      </div>
                    </div>
                  )}
                  <div className="ml-8">
                    <ReportarPregunta simulacroId={sim.id} preguntaId={p.preguntaId} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </Layout>
  )
}
