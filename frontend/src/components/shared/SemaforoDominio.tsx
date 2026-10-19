import type { NivelDominio } from '@/types'

interface Props {
  nivel:   NivelDominio
  puntaje: number
}

export default function SemaforoDominio({ nivel, puntaje }: Props) {
  const config = {
    ROJO:     { clase: 'badge-rojo',     label: 'Brecha crítica', icono: '🔴' },
    AMARILLO: { clase: 'badge-amarillo', label: 'En progreso',    icono: '🟡' },
    VERDE:    { clase: 'badge-verde',    label: 'Dominado',       icono: '🟢' },
  }[nivel]

  return (
    <span className={config.clase} title={config.label}>
      {config.icono} {puntaje.toFixed(0)}%
    </span>
  )
}
