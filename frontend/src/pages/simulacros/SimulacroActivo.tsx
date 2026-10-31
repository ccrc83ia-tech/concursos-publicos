import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Clock, Flag, ChevronLeft, ChevronRight, CheckCircle2 } from 'lucide-react'
import { obtenerSimulacro, responderPregunta, finalizarSimulacro } from '@/api/simulacros'
import type { Simulacro, OpcionRespuesta } from '@/types'
import Spinner from '@/components/shared/Spinner'

const OPCIONES: OpcionRespuesta[] = ['A', 'B', 'C', 'D']

function fmt(seg: number): string {
  const m = Math.floor(seg / 60)
  const s = seg % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

export default function SimulacroActivo() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const [sim, setSim]         = useState<Simulacro | null>(null)
  const [actual, setActual]   = useState(0)
  const [respuestas, setRespuestas] = useState<Record<string, OpcionRespuesta | null>>({})
  const [revision, setRevision]     = useState<Record<string, boolean>>({})
  const [transcurrido, setTranscurrido] = useState(0)
  const [loading, setLoading] = useState(true)
  const [finalizando, setFinalizando] = useState(false)
  const inicio = useRef<number>(Date.now())
  const finalizadoRef = useRef(false)
  // Refs "espejo" para que el listener de teclado (registrado una sola vez) lea
  // siempre el estado vigente sin necesidad de re-suscribirse en cada render.
  const simRef = useRef<Simulacro | null>(null)
  const actualRef = useRef(0)
  const seleccionarRef = useRef<((op: OpcionRespuesta) => void) | null>(null)

  useEffect(() => {
    if (!id) return
    obtenerSimulacro(id)
      .then(s => {
        setSim(s)
        const r: Record<string, OpcionRespuesta | null> = {}
        const rev: Record<string, boolean> = {}
        s.preguntas.forEach(p => { r[p.simulacroPreguntaId] = p.respuestaDada; rev[p.simulacroPreguntaId] = p.marcadaParaRevision })
        setRespuestas(r)
        setRevision(rev)
      })
      .finally(() => setLoading(false))
  }, [id])

  // Cronómetro
  useEffect(() => {
    const t = setInterval(() => {
      setTranscurrido(Math.floor((Date.now() - inicio.current) / 1000))
    }, 1000)
    return () => clearInterval(t)
  }, [])

  // Atajos de teclado: A/B/C/D responden; ←/→ navegan entre preguntas.
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const tag = (e.target as HTMLElement)?.tagName
      if (tag === 'INPUT' || tag === 'TEXTAREA') return

      const key = e.key.toUpperCase()
      if (['A', 'B', 'C', 'D'].includes(key)) {
        const preg = simRef.current?.preguntas[actualRef.current]
        if (!preg) return
        const texto = key === 'A' ? preg.opcionA : key === 'B' ? preg.opcionB : key === 'C' ? preg.opcionC : preg.opcionD
        if ((texto ?? '').trim() !== '') seleccionarRef.current?.(key as OpcionRespuesta)
      } else if (e.key === 'ArrowLeft') {
        setActual(a => Math.max(0, a - 1))
      } else if (e.key === 'ArrowRight') {
        setActual(a => Math.min((simRef.current?.preguntas.length ?? 1) - 1, a + 1))
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  useEffect(() => { simRef.current = sim }, [sim])
  useEffect(() => { actualRef.current = actual }, [actual])

  const limite = sim?.tiempoLimiteSegundos ?? null
  const restante = limite != null ? Math.max(0, limite - transcurrido) : null
  const tiempoCritico = restante != null && restante <= 60
  const tiempoAlerta  = restante != null && restante > 60 && restante <= 300

  // Auto-finaliza al agotarse el tiempo
  useEffect(() => {
    if (restante === 0 && !finalizadoRef.current) {
      handleFinalizar(true)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [restante])

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>
  if (!sim) return <p className="text-red-500 p-8">No encontramos este simulacro.</p>

  const preguntas = sim.preguntas
  const p = preguntas[actual]

  const seleccionar = async (opcion: OpcionRespuesta) => {
    const nueva = respuestas[p.simulacroPreguntaId] === opcion ? null : opcion
    setRespuestas(prev => ({ ...prev, [p.simulacroPreguntaId]: nueva }))
    try {
      await responderPregunta(sim.id, {
        simulacroPreguntaId: p.simulacroPreguntaId,
        respuesta: nueva,
        marcadaParaRevision: revision[p.simulacroPreguntaId] ?? false,
        tiempoRespuestaSegundos: 0,
      })
    } catch { /* se reintenta al finalizar */ }
  }
  seleccionarRef.current = seleccionar

  const toggleRevision = () => {
    const nuevo = !revision[p.simulacroPreguntaId]
    setRevision(prev => ({ ...prev, [p.simulacroPreguntaId]: nuevo }))
    responderPregunta(sim.id, {
      simulacroPreguntaId: p.simulacroPreguntaId,
      respuesta: respuestas[p.simulacroPreguntaId] ?? null,
      marcadaParaRevision: nuevo,
      tiempoRespuestaSegundos: 0,
    }).catch(() => {})
  }

  const handleFinalizar = async (auto = false) => {
    if (finalizadoRef.current) return

    if (!auto) {
      const sinResponder = preguntas.length - Object.values(respuestas).filter(Boolean).length
      if (sinResponder > 0) {
        const ok = window.confirm(
          `Te quedan ${sinResponder} pregunta${sinResponder === 1 ? '' : 's'} sin responder. ` +
          `¿Quieres terminar de todos modos? No podrás volver a este simulacro.`,
        )
        if (!ok) return
      }
    }

    finalizadoRef.current = true
    setFinalizando(true)
    try {
      await finalizarSimulacro(sim.id, transcurrido)
      navigate(`/simulacro/${sim.id}/resultado`)
    } catch {
      finalizadoRef.current = false
      setFinalizando(false)
    }
  }

  const respondidas = Object.values(respuestas).filter(Boolean).length
  const opcionTexto = (op: OpcionRespuesta) =>
    op === 'A' ? p.opcionA : op === 'B' ? p.opcionB : op === 'C' ? p.opcionC : p.opcionD
  const marcadaActual = revision[p.simulacroPreguntaId]

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Barra superior con cronómetro */}
      <header className="sticky top-0 bg-white border-b border-slate-200 z-10 shadow-sm">
        <div className="max-w-3xl mx-auto px-4 py-3 flex items-center justify-between gap-3">
          <span className="text-base font-semibold text-slate-700">
            Pregunta {actual + 1} <span className="text-slate-400 font-normal">de {preguntas.length}</span>
          </span>
          {restante != null && (
            <span
              className={`inline-flex items-center gap-1.5 font-mono font-bold text-xl px-3 py-1 rounded-lg
                ${tiempoCritico ? 'bg-red-50 text-red-600 animate-pulse' : tiempoAlerta ? 'bg-amber-50 text-amber-700' : 'text-slate-700'}`}
              role="timer"
              aria-live={tiempoCritico ? 'assertive' : 'off'}
              aria-label={`Tiempo restante ${fmt(restante)}`}
            >
              <Clock size={20} /> {fmt(restante)}
            </span>
          )}
          <button onClick={() => handleFinalizar(false)} disabled={finalizando} className="btn-primary btn-sm">
            {finalizando && <Spinner size="sm" />}
            Terminar
          </button>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6">
        {/* Grilla de navegación */}
        <nav className="flex flex-wrap gap-2 mb-5" aria-label="Ir a una pregunta">
          {preguntas.map((q, i) => {
            const resp = respuestas[q.simulacroPreguntaId]
            const rev = revision[q.simulacroPreguntaId]
            const estado = rev ? 'marcada para revisar' : resp ? 'respondida' : 'sin responder'
            return (
              <button
                key={q.simulacroPreguntaId}
                onClick={() => setActual(i)}
                aria-label={`Pregunta ${i + 1}, ${estado}`}
                aria-current={i === actual ? 'true' : undefined}
                className={`w-9 h-9 rounded-lg text-sm font-bold border-2 transition-colors
                  ${i === actual ? 'ring-2 ring-brand-400 ring-offset-1' : ''}
                  ${rev ? 'bg-amber-100 border-amber-300 text-amber-800'
                    : resp ? 'bg-brand-700 border-brand-700 text-white'
                    : 'bg-white border-slate-300 text-slate-500 hover:border-brand-300'}`}
              >
                {i + 1}
              </button>
            )
          })}
        </nav>

        {/* Barra de progreso */}
        <div
          className="mb-5"
          role="progressbar"
          aria-label="Progreso del simulacro"
          aria-valuemin={0}
          aria-valuemax={preguntas.length}
          aria-valuenow={respondidas}
          aria-valuetext={`${respondidas} de ${preguntas.length} respondidas`}
        >
          <div className="flex items-center justify-between text-sm text-slate-500 mb-1.5">
            <span>Tu avance</span>
            <span className="font-semibold text-slate-700">{respondidas}/{preguntas.length} respondidas</span>
          </div>
          <div className="h-2.5 w-full bg-slate-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-brand-600 transition-all duration-300"
              style={{ width: `${Math.round((respondidas / preguntas.length) * 100)}%` }}
            />
          </div>
        </div>

        {/* Pregunta actual */}
        <div className="card">
          <div className="flex items-start justify-between gap-3 mb-5">
            <p className="font-medium text-slate-900 text-lg leading-relaxed">{p.enunciado}</p>
            <button
              onClick={toggleRevision}
              className={`shrink-0 inline-flex items-center gap-1.5 text-sm px-3 py-1.5 rounded-lg border-2 font-medium
                ${marcadaActual ? 'bg-amber-100 border-amber-300 text-amber-800' : 'border-slate-200 text-slate-500 hover:bg-slate-50'}`}
            >
              <Flag size={15} className={marcadaActual ? 'fill-amber-400' : ''} />
              {marcadaActual ? 'Marcada' : 'Revisar luego'}
            </button>
          </div>

          <div className="space-y-3" role="radiogroup" aria-label="Opciones de respuesta">
            {OPCIONES.filter(op => (opcionTexto(op) ?? '').trim() !== '').map(op => {
              const seleccionada = respuestas[p.simulacroPreguntaId] === op
              return (
                <button
                  key={op}
                  role="radio"
                  aria-checked={seleccionada}
                  aria-label={`Opción ${op}: ${opcionTexto(op)}`}
                  onClick={() => seleccionar(op)}
                  className={`w-full text-left px-4 py-4 rounded-xl border-2 text-base transition-colors flex gap-3 items-start
                    ${seleccionada ? 'bg-brand-50 border-brand-500 text-brand-900' : 'border-slate-200 hover:bg-slate-50 hover:border-slate-300'}`}
                >
                  <span
                    className={`shrink-0 w-8 h-8 rounded-lg border-2 flex items-center justify-center font-bold
                      ${seleccionada ? 'bg-brand-600 border-brand-600 text-white' : 'border-slate-300 text-slate-400'}`}
                    aria-hidden="true"
                  >
                    {op}
                  </span>
                  <span className="pt-1">{opcionTexto(op)}</span>
                </button>
              )
            })}
          </div>

          <p className="mt-4 text-xs text-slate-400">
            Consejo: en el computador puedes pulsar{' '}
            <kbd className="px-1.5 py-0.5 bg-slate-100 rounded border border-slate-200 font-mono">A</kbd>{' '}
            <kbd className="px-1.5 py-0.5 bg-slate-100 rounded border border-slate-200 font-mono">B</kbd>{' '}
            <kbd className="px-1.5 py-0.5 bg-slate-100 rounded border border-slate-200 font-mono">C</kbd> para responder y las flechas{' '}
            <kbd className="px-1.5 py-0.5 bg-slate-100 rounded border border-slate-200 font-mono">←</kbd>{' '}
            <kbd className="px-1.5 py-0.5 bg-slate-100 rounded border border-slate-200 font-mono">→</kbd> para cambiar de pregunta.
          </p>
        </div>

        {/* Navegación */}
        <div className="flex items-center justify-between mt-5 gap-3">
          <button onClick={() => setActual(a => Math.max(0, a - 1))} disabled={actual === 0} className="btn-secondary">
            <ChevronLeft size={18} /> Anterior
          </button>
          {actual < preguntas.length - 1 ? (
            <button onClick={() => setActual(a => Math.min(preguntas.length - 1, a + 1))} className="btn-primary">
              Siguiente <ChevronRight size={18} />
            </button>
          ) : (
            <button onClick={() => handleFinalizar(false)} disabled={finalizando} className="btn-success">
              {finalizando ? <Spinner size="sm" /> : <CheckCircle2 size={18} />}
              Terminar simulacro
            </button>
          )}
        </div>
      </main>
    </div>
  )
}
