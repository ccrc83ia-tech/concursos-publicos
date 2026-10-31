import type { NivelDominio } from '@/types'

interface Props {
  nivel:   NivelDominio
  puntaje: number
}

/**
 * Insignia de semáforo con lenguaje llano: en vez de "brecha crítica" usamos
 * expresiones que un usuario sin contexto técnico entiende de inmediato.
 */
export default function SemaforoDominio({ nivel, puntaje }: Props) {
  const config = {
    ROJO:     { clase: 'badge-rojo',     label: 'Necesita refuerzo', icono: '🔴' },
    AMARILLO: { clase: 'badge-amarillo', label: 'Vas mejorando',     icono: '🟡' },
    VERDE:    { clase: 'badge-verde',    label: 'Dominado',          icono: '🟢' },
  }[nivel]

  return (
    <span className={config.clase} title={config.label}>
      {config.icono} {puntaje.toFixed(0)}%
    </span>
  )
}
