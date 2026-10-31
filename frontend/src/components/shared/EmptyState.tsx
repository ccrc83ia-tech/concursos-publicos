import type { ReactNode } from 'react'
import type { LucideIcon } from 'lucide-react'

interface Props {
  icono:    LucideIcon
  titulo:   string
  mensaje:  string
  /** Acción principal (normalmente un botón o Link con clase .btn-primary). */
  accion?:  ReactNode
}

/**
 * Estado vacío amable y orientado a la acción. En vez de dejar al usuario
 * frente a una pantalla en blanco, explica qué vería aquí y qué hacer a
 * continuación (patrón recomendado para primeros usos).
 */
export default function EmptyState({ icono: Icono, titulo, mensaje, accion }: Props) {
  return (
    <div className="card text-center py-14 px-6 animate-fade-in">
      <div className="inline-flex items-center justify-center w-20 h-20 rounded-2xl bg-brand-50 text-brand-600 mb-5">
        <Icono size={40} strokeWidth={1.75} />
      </div>
      <h2 className="text-xl font-bold text-slate-800 mb-2">{titulo}</h2>
      <p className="text-slate-500 text-base max-w-md mx-auto mb-7 leading-relaxed">{mensaje}</p>
      {accion}
    </div>
  )
}
