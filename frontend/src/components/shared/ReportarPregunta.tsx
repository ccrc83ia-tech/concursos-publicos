import { useState } from 'react'
import { reportarPregunta } from '@/api/simulacros'
import type { TipoFeedback } from '@/types'
import Spinner from './Spinner'

interface Props {
  simulacroId: string
  preguntaId:  string
}

const TIPOS: { value: TipoFeedback; label: string }[] = [
  { value: 'CLAVE_INCORRECTA',  label: 'La respuesta correcta está mal' },
  { value: 'VARIAS_CORRECTAS',  label: 'Hay más de una opción correcta' },
  { value: 'ENUNCIADO_CONFUSO', label: 'El enunciado es confuso o ambiguo' },
  { value: 'FUERA_DE_TEMA',     label: 'No corresponde al indicador' },
  { value: 'ERROR_REDACCION',   label: 'Error de redacción u ortografía' },
  { value: 'OTRO',              label: 'Otro' },
]

/**
 * Botón discreto para reportar una pregunta como problemática. Despliega un panel
 * con el motivo y un comentario opcional. Tras enviar, muestra confirmación (e
 * informa si el reporte provocó que la pregunta se retirara del banco).
 */
export default function ReportarPregunta({ simulacroId, preguntaId }: Props) {
  const [abierto, setAbierto]   = useState(false)
  const [tipo, setTipo]         = useState<TipoFeedback>('CLAVE_INCORRECTA')
  const [comentario, setComentario] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [enviado, setEnviado]   = useState<string | null>(null)
  const [error, setError]       = useState('')

  const enviar = async () => {
    setEnviando(true)
    setError('')
    try {
      const r = await reportarPregunta(simulacroId, preguntaId, {
        tipo,
        comentario: comentario.trim() || undefined,
      })
      setEnviado(
        r.preguntaRetirada
          ? 'Gracias. Esta pregunta se retiró del banco por reportes.'
          : 'Gracias, tu reporte fue registrado.',
      )
      setAbierto(false)
    } catch (e: any) {
      setError(e?.response?.data?.mensaje ?? 'No se pudo enviar el reporte')
    } finally {
      setEnviando(false)
    }
  }

  if (enviado) {
    return <p className="text-[11px] text-green-600 mt-1">✓ {enviado}</p>
  }

  if (!abierto) {
    return (
      <button
        onClick={() => setAbierto(true)}
        className="text-[11px] text-gray-400 hover:text-red-500 mt-1"
        title="Reportar un problema con esta pregunta"
      >
        ⚑ Reportar pregunta
      </button>
    )
  }

  return (
    <div className="mt-2 p-3 border border-gray-200 rounded-lg bg-gray-50 text-xs space-y-2">
      <p className="font-medium text-gray-700">¿Cuál es el problema con esta pregunta?</p>
      <div className="space-y-1">
        {TIPOS.map(t => (
          <label key={t.value} className="flex items-center gap-2 cursor-pointer">
            <input
              type="radio"
              name={`motivo-${preguntaId}`}
              checked={tipo === t.value}
              onChange={() => setTipo(t.value)}
            />
            <span>{t.label}</span>
          </label>
        ))}
      </div>
      <textarea
        className="input-field text-xs h-16 resize-none"
        placeholder="Comentario opcional (máx. 500 caracteres)"
        maxLength={500}
        value={comentario}
        onChange={e => setComentario(e.target.value)}
      />
      {error && <p className="text-red-500">{error}</p>}
      <div className="flex gap-2">
        <button
          onClick={enviar}
          disabled={enviando}
          className="text-xs bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded disabled:opacity-50 flex items-center gap-1"
        >
          {enviando && <Spinner size="sm" />}
          Enviar reporte
        </button>
        <button
          onClick={() => { setAbierto(false); setError('') }}
          className="text-xs text-gray-500 hover:text-gray-700 px-3 py-1 rounded border border-gray-200"
        >
          Cancelar
        </button>
      </div>
    </div>
  )
}
