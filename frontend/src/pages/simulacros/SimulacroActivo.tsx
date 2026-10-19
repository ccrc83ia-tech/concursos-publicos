import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
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

  const limite = sim?.tiempoLimiteSegundos ?? null
  const restante = limite != null ? Math.max(0, limite - transcurrido) : null

  // Auto-finaliza al agotarse el tiempo
  useEffect(() => {
    if (restante === 0 && !finalizadoRef.current) {
      handleFinalizar()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [restante])

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>
  if (!sim) return <p className="text-red-500 p-8">Simulacro no encontrado</p>

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

  const handleFinalizar = async () => {
    if (finalizadoRef.current) return
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

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Barra superior con cronómetro */}
      <header className="sticky top-0 bg-white border-b border-gray-200 z-10">
        <div className="max-w-3xl mx-auto px-4 py-3 flex items-center justify-between">
          <span className="text-sm text-gray-600">Pregunta {actual + 1} de {preguntas.length}</span>
          {restante != null && (
            <span className={`font-mono font-bold text-lg ${restante <= 60 ? 'text-red-600' : 'text-gray-800'}`}>
              ⏱ {fmt(restante)}
            </span>
          )}
          <button onClick={handleFinalizar} disabled={finalizando} className="btn-primary text-xs flex items-center gap-2">
            {finalizando && <Spinner size="sm" />}
            Finalizar
          </button>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-6">
        {/* Grilla de navegación */}
        <div className="flex flex-wrap gap-1.5 mb-5">
          {preguntas.map((q, i) => {
            const resp = respuestas[q.simulacroPreguntaId]
            const rev = revision[q.simulacroPreguntaId]
            return (
              <button
                key={q.simulacroPreguntaId}
                onClick={() => setActual(i)}
                className={`w-8 h-8 rounded text-xs font-medium border transition-colors
                  ${i === actual ? 'ring-2 ring-blue-500' : ''}
                  ${rev ? 'bg-yellow-100 border-yellow-300 text-yellow-800'
                    : resp ? 'bg-blue-600 border-blue-600 text-white'
                    : 'bg-white border-gray-300 text-gray-500'}`}
              >
                {i + 1}
              </button>
            )
          })}
        </div>

        {/* Pregunta actual */}
        <div className="card">
          <div className="flex items-start justify-between mb-4">
            <p className="font-medium text-gray-900 leading-relaxed">{p.enunciado}</p>
            <button
              onClick={toggleRevision}
              className={`ml-3 shrink-0 text-xs px-2 py-1 rounded border ${revision[p.simulacroPreguntaId] ? 'bg-yellow-100 border-yellow-300 text-yellow-800' : 'border-gray-200 text-gray-500 hover:bg-gray-50'}`}
            >
              {revision[p.simulacroPreguntaId] ? '★ Marcada' : '☆ Revisar'}
            </button>
          </div>

          <div className="space-y-2">
            {OPCIONES.map(op => {
              const seleccionada = respuestas[p.simulacroPreguntaId] === op
              return (
                <button
                  key={op}
                  onClick={() => seleccionar(op)}
                  className={`w-full text-left px-4 py-3 rounded-lg border text-sm transition-colors flex gap-3
                    ${seleccionada ? 'bg-blue-50 border-blue-400 text-blue-900' : 'border-gray-200 hover:bg-gray-50'}`}
                >
                  <span className={`font-bold ${seleccionada ? 'text-blue-600' : 'text-gray-400'}`}>{op}</span>
                  <span>{opcionTexto(op)}</span>
                </button>
              )
            })}
          </div>
        </div>

        {/* Navegación */}
        <div className="flex items-center justify-between mt-5">
          <button onClick={() => setActual(a => Math.max(0, a - 1))} disabled={actual === 0} className="btn-secondary disabled:opacity-40">← Anterior</button>
          <span className="text-xs text-gray-500">{respondidas}/{preguntas.length} respondidas</span>
          {actual < preguntas.length - 1 ? (
            <button onClick={() => setActual(a => Math.min(preguntas.length - 1, a + 1))} className="btn-primary">Siguiente →</button>
          ) : (
            <button onClick={handleFinalizar} disabled={finalizando} className="btn-primary flex items-center gap-2">
              {finalizando && <Spinner size="sm" />}
              Finalizar
            </button>
          )}
        </div>
      </main>
    </div>
  )
}
