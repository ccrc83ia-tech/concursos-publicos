import { useState } from 'react'
import { Flag, CheckCircle2, Send } from 'lucide-react'
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
  { value: 'ENUNCIADO_CONFUSO', label: 'La pregunta es confusa' },
  { value: 'FUERA_DE_TEMA',     label: 'No corresponde al tema' },
  { value: 'ERROR_REDACCION',   label: 'Tiene errores de redacción' },
  { value: 'OTRO',              label: 'Otro motivo' },
]

/**
 * Botón discreto para reportar una pregunta con problemas. Despliega un panel
 * con el motivo y un comentario opcional, y confirma el envío.
 */
export default function ReportarPregunta({ simulacroId, preguntaId }: Props) {
  const [abierto, setAbierto]       = useState(false)
  const [tipo, setTipo]             = useState<TipoFeedback>('CLAVE_INCORRECTA')
  const [comentario, setComentario] = useState('')
  const [enviando, setEnviando]     = useState(false)
  const [enviado, setEnviado]       = useState<string | null>(null)
  const [error, setError]           = useState('')

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
          ? 'Gracias. Quitamos esta pregunta por los reportes recibidos.'
          : 'Gracias, registramos tu reporte.',
      )
      setAbierto(false)
    } catch (e: any) {
      setError(e?.response?.data?.mensaje ?? 'No se pudo enviar el reporte')
    } finally {
      setEnviando(false)
    }
  }

  if (enviado) {
    return <p className="flex items-center gap-1.5 text-sm text-emerald-600 mt-2"><CheckCircle2 size={15} /> {enviado}</p>
  }

  if (!abierto) {
    return (
      <button
        onClick={() => setAbierto(true)}
        className="inline-flex items-center gap-1.5 text-sm text-slate-400 hover:text-red-500 mt-2"
        title="Avísanos si algo está mal con esta pregunta"
      >
        <Flag size={14} /> Reportar un problema
      </button>
    )
  }

  return (
    <div className="mt-3 p-4 border border-slate-200 rounded-xl bg-slate-50 text-sm space-y-3">
      <p className="font-semibold text-slate-700">¿Qué problema tiene esta pregunta?</p>
      <div className="space-y-1.5">
        {TIPOS.map(t => (
          <label key={t.value} className="flex items-center gap-2.5 cursor-pointer">
            <input
              type="radio"
              name={`motivo-${preguntaId}`}
              checked={tipo === t.value}
              onChange={() => setTipo(t.value)}
              className="w-4 h-4 accent-brand-700"
            />
            <span className="text-slate-700">{t.label}</span>
          </label>
        ))}
      </div>
      <textarea
        className="input-field text-sm h-20 resize-none"
        placeholder="Cuéntanos más (opcional)"
        maxLength={500}
        value={comentario}
        onChange={e => setComentario(e.target.value)}
      />
      {error && <p className="text-red-500">{error}</p>}
      <div className="flex gap-2">
        <button onClick={enviar} disabled={enviando} className="btn-danger btn-sm">
          {enviando ? <Spinner size="sm" /> : <Send size={15} />}
          Enviar reporte
        </button>
        <button onClick={() => { setAbierto(false); setError('') }} className="btn-secondary btn-sm">
          Cancelar
        </button>
      </div>
    </div>
  )
}
