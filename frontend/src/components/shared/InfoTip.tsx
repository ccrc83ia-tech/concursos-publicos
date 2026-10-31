import { useState, useId } from 'react'
import { HelpCircle } from 'lucide-react'

interface Props {
  /** Texto explicativo en lenguaje sencillo. */
  texto: string
  etiqueta?: string
}

/**
 * Ayuda contextual accesible: un pequeño icono "?" que, al pasar el cursor o
 * recibir foco, muestra una explicación en lenguaje llano. Pensado para
 * desmitificar términos técnicos (indicador, banco de preguntas, umbral...).
 */
export default function InfoTip({ texto, etiqueta = 'Más información' }: Props) {
  const [abierto, setAbierto] = useState(false)
  const id = useId()

  return (
    <span className="relative inline-flex align-middle">
      <button
        type="button"
        aria-label={etiqueta}
        aria-describedby={abierto ? id : undefined}
        onMouseEnter={() => setAbierto(true)}
        onMouseLeave={() => setAbierto(false)}
        onFocus={() => setAbierto(true)}
        onBlur={() => setAbierto(false)}
        onClick={(e) => { e.preventDefault(); setAbierto(v => !v) }}
        className="text-slate-400 hover:text-brand-600 focus-visible:text-brand-600"
      >
        <HelpCircle size={16} />
      </button>
      {abierto && (
        <span
          id={id}
          role="tooltip"
          className="absolute left-1/2 -translate-x-1/2 top-6 z-30 w-60 rounded-lg bg-slate-800 text-white
                     text-sm font-normal leading-relaxed px-3 py-2 shadow-lg"
        >
          {texto}
        </span>
      )}
    </span>
  )
}
