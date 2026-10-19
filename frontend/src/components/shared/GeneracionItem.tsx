import { useEffect, useRef, useState } from 'react'
import { iniciarGeneracion, consultarEstadoGeneracion } from '@/api/generacion'
import type { GeneracionJob } from '@/types'
import Spinner from './Spinner'

interface Props {
  itemId: string
  /** Preguntas ya existentes al cargar (para mostrar estado inicial). */
  preguntasExistentes?: number
}

const INTERVALO_POLL_MS = 2500

/**
 * Botón + barra de progreso de generación de preguntas con IA para un ítem.
 * Hace polling del estado mientras la generación está en curso.
 */
export default function GeneracionItem({ itemId, preguntasExistentes = 0 }: Props) {
  const [job, setJob]       = useState<GeneracionJob | null>(null)
  const [iniciando, setIniciando] = useState(false)
  const [error, setError]   = useState('')
  const timer = useRef<ReturnType<typeof setInterval> | null>(null)

  const detenerPoll = () => {
    if (timer.current) { clearInterval(timer.current); timer.current = null }
  }

  // Al montar, consulta si ya hay un job previo (p. ej. en curso tras recargar).
  useEffect(() => {
    consultarEstadoGeneracion(itemId)
      .then(setJob)
      .catch(() => { /* sin job previo: estado normal */ })
    return detenerPoll
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [itemId])

  // Arranca/detiene el polling según el estado del job.
  useEffect(() => {
    const enCurso = job?.estado === 'PENDIENTE' || job?.estado === 'EN_PROCESO'
    if (enCurso && !timer.current) {
      timer.current = setInterval(async () => {
        try {
          const actualizado = await consultarEstadoGeneracion(itemId)
          setJob(actualizado)
          if (actualizado.estado === 'COMPLETADO' || actualizado.estado === 'ERROR') {
            detenerPoll()
          }
        } catch {
          detenerPoll()
        }
      }, INTERVALO_POLL_MS)
    }
    if (!enCurso) detenerPoll()
    return detenerPoll
  }, [job, itemId])

  const handleGenerar = async () => {
    setIniciando(true)
    setError('')
    try {
      const nuevo = await iniciarGeneracion(itemId)
      setJob(nuevo)
    } catch (e: any) {
      setError(e?.response?.data?.mensaje ?? 'No se pudo iniciar la generación')
    } finally {
      setIniciando(false)
    }
  }

  const enCurso    = job?.estado === 'PENDIENTE' || job?.estado === 'EN_PROCESO'
  const completado = job?.estado === 'COMPLETADO'
  const conError   = job?.estado === 'ERROR'

  if (enCurso) {
    return (
      <div className="w-40">
        <div className="flex items-center justify-between text-xs text-gray-500 mb-1">
          <span className="flex items-center gap-1"><Spinner size="sm" /> Generando…</span>
          <span>{job!.porcentaje}%</span>
        </div>
        <div className="h-1.5 w-full bg-gray-200 rounded-full overflow-hidden">
          <div className="h-full bg-blue-600 transition-all" style={{ width: `${job!.porcentaje}%` }} />
        </div>
        <div className="text-[10px] text-gray-400 mt-0.5">
          {job!.preguntasGeneradas}/{job!.totalEsperadas}
        </div>
      </div>
    )
  }

  if (completado) {
    return (
      <div className="text-xs text-green-700">
        ✅ {job!.preguntasGeneradas} generadas
        <button onClick={handleGenerar} className="ml-2 text-blue-600 hover:text-blue-800" title="Regenerar">
          ↻
        </button>
      </div>
    )
  }

  return (
    <div>
      <button
        onClick={handleGenerar}
        disabled={iniciando}
        className="text-xs bg-blue-600 hover:bg-blue-700 text-white px-2 py-1 rounded transition-colors disabled:opacity-50 flex items-center gap-1"
      >
        {iniciando && <Spinner size="sm" />}
        🤖 Generar
      </button>
      {preguntasExistentes > 0 && !conError && (
        <div className="text-[10px] text-gray-400 mt-0.5">{preguntasExistentes} existentes</div>
      )}
      {conError && <div className="text-[10px] text-red-500 mt-0.5">Error: reintentar</div>}
      {error && <div className="text-[10px] text-red-500 mt-0.5">{error}</div>}
    </div>
  )
}
