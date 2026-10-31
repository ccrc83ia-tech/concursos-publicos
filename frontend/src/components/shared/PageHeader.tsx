import { Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import type { ReactNode } from 'react'

interface Props {
  titulo:      string
  descripcion?: string
  /** Enlace "volver" opcional que aparece encima del título. */
  volverA?:    string
  volverLabel?: string
  /** Acciones alineadas a la derecha del encabezado (botones, etc.). */
  acciones?:   ReactNode
}

/**
 * Encabezado de página reutilizable: título grande, descripción en lenguaje
 * sencillo y un enlace "volver" opcional. Unifica la jerarquía visual en toda
 * la app para que el usuario siempre sepa dónde está.
 */
export default function PageHeader({ titulo, descripcion, volverA, volverLabel = 'Volver', acciones }: Props) {
  return (
    <div className="mb-6">
      {volverA && (
        <Link
          to={volverA}
          className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-500 hover:text-brand-700 mb-2"
        >
          <ArrowLeft size={16} /> {volverLabel}
        </Link>
      )}
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-slate-900">{titulo}</h1>
          {descripcion && (
            <p className="text-slate-500 text-base mt-1.5 max-w-2xl leading-relaxed">{descripcion}</p>
          )}
        </div>
        {acciones && <div className="flex items-center gap-2 flex-wrap">{acciones}</div>}
      </div>
    </div>
  )
}
