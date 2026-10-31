import type { BancoEstado } from '@/types'

interface Props {
  estado: BancoEstado
  /** Muestra el desglose provisional/premium debajo del badge (modo compacto lo oculta). */
  detallado?: boolean
}

/**
 * Badge compacto con el "sello de calidad" del banco de un ítem: cuántas preguntas
 * están VALIDADAS (revisadas por el segundo modelo) sobre el total. El color refleja
 * la proporción validada, reutilizando la semántica de semáforo del resto de la app:
 *  - verde:   todo validado
 *  - amarillo: hay material pero parte sigue provisional/pendiente
 *  - gris:    banco vacío
 *
 * El tooltip detalla el desglose completo por estado y origen.
 */
export default function BancoEstadoBadge({ estado, detallado = false }: Props) {
  const { total, validadas, pendientes, rechazadas, agotadas, provisionales, premium } = estado

  if (total === 0) {
    return <span className="badge-neutral" title="Sin preguntas en el banco">Sin banco</span>
  }

  const todoValidado = validadas === total
  const clase = todoValidado ? 'badge-verde' : 'badge-amarillo'
  const icono = todoValidado ? '🟢' : '🟡'

  const tooltip =
    `Banco: ${total} preguntas\n` +
    `• Validadas: ${validadas}\n` +
    `• Pendientes: ${pendientes}\n` +
    `• Rechazadas: ${rechazadas}\n` +
    `• Agotadas: ${agotadas}\n` +
    `Origen → Premium: ${premium} · Provisional: ${provisionales}`

  return (
    <div className="inline-flex flex-col items-start gap-0.5">
      <span className={clase} title={tooltip}>
        {icono} {validadas}/{total} validadas
      </span>
      {detallado && provisionales > 0 && (
        <span className="text-[10px] text-gray-400" title={tooltip}>
          {provisionales} provisional{provisionales === 1 ? '' : 'es'}
          {pendientes > 0 && ` · ${pendientes} por validar`}
        </span>
      )}
    </div>
  )
}
