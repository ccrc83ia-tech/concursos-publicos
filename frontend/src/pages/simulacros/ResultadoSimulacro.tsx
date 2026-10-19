import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { obtenerSimulacro, obtenerDesempeno } from '@/api/simulacros'
import { listarItems } from '@/api/items'
import type { Simulacro, Desempeno, Item, NivelDominio, OpcionRespuesta } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'

const DOMINIO_COLOR: Record<NivelDominio, string> = {
  ROJO:     'bg-red-500',
  AMARILLO: 'bg-yellow-400',
  VERDE:    'bg-green-500',
}
const DOMINIO_LABEL: Record<NivelDominio, string> = {
  ROJO: '🔴 Brecha crítica', AMARILLO: '🟡 En progreso', VERDE: '🟢 Dominado',
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
  if (!sim) return <Layout><p className="text-red-500">Simulacro no encontrado</p></Layout>

  const nombreItem = (itemId: string) => items.find(i => i.id === itemId)?.nombre ?? 'Indicador'
  const puntaje = sim.puntajeFinal ?? 0
  const correctas = sim.preguntas.filter(p => p.esCorrecta).length
  const letra = (op: OpcionRespuesta) =>
    ({ A: 'opcionA', B: 'opcionB', C: 'opcionC', D: 'opcionD' } as const)[op]

  return (
    <Layout>
      <div className="max-w-3xl mx-auto">
        <Link to={`/concursos/${sim.concursoId}`} className="text-sm text-gray-500 hover:text-gray-700">← Volver al concurso</Link>
        <h1 className="text-2xl font-bold text-gray-900 mt-1 mb-6">Resultado del simulacro</h1>

        {/* Puntaje global */}
        <div className="card text-center mb-6">
          <div className={`text-5xl font-bold ${puntaje >= 70 ? 'text-green-600' : puntaje >= 50 ? 'text-yellow-600' : 'text-red-600'}`}>
            {puntaje.toFixed(1)}%
          </div>
          <p className="text-gray-500 text-sm mt-1">{correctas} de {sim.preguntas.length} correctas</p>
        </div>

        {/* Desempeño por ítem (semáforo) */}
        <div className="card mb-6">
          <h2 className="font-semibold mb-4">Desempeño por indicador</h2>
          <div className="space-y-3">
            {desempeno.map(d => (
              <div key={d.itemId}>
                <div className="flex items-center justify-between text-sm mb-1">
                  <span className="text-gray-700">{nombreItem(d.itemId)}</span>
                  <span className="text-xs text-gray-500">
                    {d.preguntasCorrectas}/{d.totalPreguntas} · {DOMINIO_LABEL[d.nivelDominio]}
                  </span>
                </div>
                <div className="h-2 w-full bg-gray-200 rounded-full overflow-hidden">
                  <div className={`h-full ${DOMINIO_COLOR[d.nivelDominio]} transition-all`} style={{ width: `${d.puntajePorcentual}%` }} />
                </div>
              </div>
            ))}
            {desempeno.length === 0 && <p className="text-sm text-gray-400">Sin datos de desempeño.</p>}
          </div>
        </div>

        {/* Revisión de preguntas */}
        <div className="card">
          <button onClick={() => setVerDetalle(v => !v)} className="w-full flex items-center justify-between font-semibold">
            <span>Revisión de respuestas</span>
            <span className="text-gray-400">{verDetalle ? '▲' : '▼'}</span>
          </button>

          {verDetalle && (
            <div className="mt-4 space-y-5">
              {sim.preguntas.map((p, i) => (
                <div key={p.simulacroPreguntaId} className="border-t border-gray-100 pt-4">
                  <p className="font-medium text-gray-900 text-sm mb-2">
                    {i + 1}. {p.enunciado}
                    {p.esCorrecta ? <span className="ml-2 text-green-600">✓</span> : <span className="ml-2 text-red-500">✗</span>}
                  </p>
                  <div className="text-xs space-y-1 mb-2">
                    {(['A', 'B', 'C', 'D'] as OpcionRespuesta[]).map(op => {
                      const esCorrecta = p.respuestaCorrecta === op
                      const esDada = p.respuestaDada === op
                      return (
                        <div key={op} className={`px-2 py-1 rounded flex gap-2
                          ${esCorrecta ? 'bg-green-50 text-green-800' : esDada ? 'bg-red-50 text-red-700' : 'text-gray-600'}`}>
                          <span className="font-bold">{op}</span>
                          <span>{p[letra(op)]}</span>
                          {esCorrecta && <span className="ml-auto">✓ correcta</span>}
                          {esDada && !esCorrecta && <span className="ml-auto">tu respuesta</span>}
                        </div>
                      )
                    })}
                  </div>
                  {p.justificacionCorrecta && (
                    <p className="text-xs text-gray-600 bg-blue-50 rounded p-2">
                      💡 {p.justificacionCorrecta}
                      {p.referenciaNormativa && <span className="block text-gray-400 mt-1">📖 {p.referenciaNormativa}</span>}
                    </p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </Layout>
  )
}
