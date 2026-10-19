import type { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

/**
 * Logger ligero para el cliente HTTP. En desarrollo imprime en consola;
 * en producción solo registra advertencias y errores (las peticiones OK se
 * silencian para no ensuciar la consola del usuario).
 *
 * Cada error de API se correlaciona con el backend mediante el header
 * `X-Trace-Id`, que el filtro de logging del servidor devuelve en la respuesta.
 * Así un reporte del usuario ("me salió error, referencia ab12cd34") se cruza
 * directo con las trazas del backend.
 */

const esDev = import.meta.env.DEV

const traceIdDe = (r?: AxiosResponse | AxiosError['response']): string =>
  (r?.headers?.['x-trace-id'] as string | undefined) ?? '—'

const marca = (config?: InternalAxiosRequestConfig): string => {
  const metodo = config?.method?.toUpperCase() ?? 'GET'
  const url = config?.url ?? ''
  return `${metodo} ${url}`
}

export const logRequest = (config: InternalAxiosRequestConfig): void => {
  if (esDev) console.debug(`[api] → ${marca(config)}`)
}

export const logResponse = (res: AxiosResponse): void => {
  if (esDev) console.debug(`[api] ← ${res.status} ${marca(res.config)}`)
}

export const logError = (error: AxiosError): void => {
  const status = error.response?.status
  const donde = marca(error.config as InternalAxiosRequestConfig)
  const traceId = traceIdDe(error.response)

  if (status == null) {
    // Sin respuesta: red caída, CORS o timeout.
    console.error(`[api] ✗ ${donde} — sin respuesta del servidor (${error.message})`)
    return
  }

  const data = error.response?.data as { mensaje?: string } | undefined
  const detalle = `${donde} → ${status} [trace ${traceId}]${data?.mensaje ? ` — ${data.mensaje}` : ''}`

  if (status >= 500) {
    console.error(`[api] ✗ ${detalle}`)
  } else {
    console.warn(`[api] ⚠ ${detalle}`)
  }
}
