import { Loader2 } from 'lucide-react'

/**
 * Indicador de carga. Usa un icono animado consistente con el resto de la
 * iconografía (lucide). Incluye etiqueta accesible para lectores de pantalla.
 */
export default function Spinner({ size = 'md' }: { size?: 'sm' | 'md' | 'lg' }) {
  const px = { sm: 16, md: 28, lg: 44 }[size]
  return (
    <Loader2
      className="animate-spin text-brand-600"
      size={px}
      strokeWidth={2.5}
      role="status"
      aria-label="Cargando"
    />
  )
}
