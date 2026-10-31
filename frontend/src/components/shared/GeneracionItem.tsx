import { useEffect, useRef, useState } from 'react'
import { iniciarGeneracion, consultarEstadoGeneracion, obtenerEstadoBanco } from '@/api/generacion'
import type { BancoEstado, GeneracionJob } from '@/types'
import Spinner from './Spinner'
import BancoEstadoBadge from './BancoEstadoBadge'

interface Props {
  itemId: string
  /** Preguntas ya existentes al cargar (para mostrar estado inicial). */
  preguntasExistentes?: number
  /**
   * Cantidad a generar. Si se indica, se envía al backend y prevalece sobre la
   * cantidad registrada en el ítem (útil para usar el valor editado en pantalla
   * sin guardarlo primero).
   */
  cantidad?: number
}

const INTERVALO_POLL_MS = 2500

/**
 * Botón + barra de progreso de generación de preguntas con IA para un ítem.
 * Hace polling del estado mientras la generación está en curso.
 */
export default function GeneracionItem({ itemId, preguntasExistentes = 0, cantidad }: Props) {
  const [job, setJob]       = useState<GeneracionJob | null>(null)
  const [banco, setBanco]   = useState<BancoEstado | null>(null)
  const [iniciando, setIniciando] = useState(false)
  const [error, setError]   = useState('')
  const timer = useRef<ReturnType<typeof setInterval> | null>(null)

  const detenerPoll = () => {
    if (timer.current) { clearInterval(timer.current); timer.current = null }
  }

  const refrescarBanco = () => {
    obtenerEstadoBanco(itemId)
      .then(setBanco)
      .catch(() => { /* sin banco aún: no se muestra */ })
  }

  // Al montar, consulta si ya hay un job previo (p. ej. en curso tras recargar)
  // y el estado actual del banco de preguntas del ítem.
  useEffect(() => {
    consultarEstadoGeneracion(itemId)
      .then(setJob)
      .catch(() => { /* sin job previo: estado normal */ })
    refrescarBanco()
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
            refrescarBanco() // al terminar, reflejar el nuevo estado del banco
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
      const nuevo = await iniciarGeneracion(itemId, cantidad)
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
      <div className="flex flex-col items-center gap-1">
        <div className="flex items-center gap-1.5">
          {banco
            ? <BancoEstadoBadge estado={banco} detallado />
            : <span className="text-xs text-green-700">✅ {job!.preguntasGeneradas} generadas</span>}
          <button
            onClick={handleGenerar}
            className="text-blue-600 hover:text-blue-800 text-sm leading-none"
            title="Generar más preguntas"
          >
            ↻
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col items-center gap-1">
      <button
        onClick={handleGenerar}
        disabled={iniciando}
        className="text-xs bg-blue-600 hover:bg-blue-700 text-white px-2 py-1 rounded transition-colors disabled:opacity-50 flex items-center gap-1"
      >
        {iniciando && <Spinner size="sm" />}
        🤖 Generar
      </button>
      {/* Si ya hay banco, mostrar su estado de calidad; si no, el conteo simple. */}
      {banco && banco.total > 0
        ? <BancoEstadoBadge estado={banco} />
        : preguntasExistentes > 0 && !conError && (
            <div className="text-[10px] text-gray-400">{preguntasExistentes} existentes</div>
          )}
      {conError && <div className="text-[10px] text-red-500">Error: reintentar</div>}
      {error && <div className="text-[10px] text-red-500">{error}</div>}
    </div>
  )
}
