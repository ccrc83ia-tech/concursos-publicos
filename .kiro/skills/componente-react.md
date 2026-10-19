---
name: componente-react
description: Patrones y convenciones para crear componentes React con TypeScript y Tailwind CSS en el proyecto DIAN
---

# Skill: Componente React + TypeScript + Tailwind

Cuando el usuario pida crear una nueva página, componente o hook, sigue estas convenciones del proyecto.

## Estructura de un componente

```
src/
├── pages/
│   └── simulacro/
│       ├── ConfigurarSimulacro.tsx   ← página completa (ruta)
│       └── ResultadoSimulacro.tsx
├── components/
│   └── shared/
│       ├── Spinner.tsx               ← componente reutilizable
│       └── SemaforoDominio.tsx
└── hooks/
    ├── useCronometro.ts              ← hook personalizado
    └── useSimulacro.ts
```

## Plantilla de página (con auth)

```tsx
// pages/ejemplo/MiPagina.tsx
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import Spinner from '../../components/shared/Spinner'
import type { MiTipo } from '../../types'

export default function MiPagina() {
  const { token } = useAuthStore()
  const navigate = useNavigate()
  const [datos, setDatos] = useState<MiTipo[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    cargarDatos()
  }, [])

  async function cargarDatos() {
    try {
      setCargando(true)
      setError(null)
      // const resultado = await miApiCall()
      // setDatos(resultado)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error inesperado')
    } finally {
      setCargando(false)
    }
  }

  if (cargando) return <Spinner />
  if (error) return <MensajeError mensaje={error} onReintentar={cargarDatos} />

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Título</h1>
      {/* contenido */}
    </div>
  )
}
```

## Plantilla de componente reutilizable

```tsx
// components/shared/MiComponente.tsx
interface MiComponenteProps {
  titulo: string
  descripcion?: string
  onClick?: () => void
  className?: string
}

export default function MiComponente({
  titulo,
  descripcion,
  onClick,
  className = ''
}: MiComponenteProps) {
  return (
    <div className={`rounded-lg border border-gray-200 p-4 ${className}`}>
      <h2 className="font-semibold text-gray-800">{titulo}</h2>
      {descripcion && <p className="mt-1 text-sm text-gray-500">{descripcion}</p>}
      {onClick && (
        <button
          onClick={onClick}
          className="mt-3 text-sm font-medium text-blue-600 hover:text-blue-800"
        >
          Ver más
        </button>
      )}
    </div>
  )
}
```

## Plantilla de hook personalizado

```tsx
// hooks/useMiRecurso.ts
import { useState, useEffect, useCallback } from 'react'
import type { MiTipo } from '../types'

interface UseMiRecursoReturn {
  datos: MiTipo | null
  cargando: boolean
  error: string | null
  refetch: () => void
}

export function useMiRecurso(id: string): UseMiRecursoReturn {
  const [datos, setDatos] = useState<MiTipo | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const cargar = useCallback(async () => {
    try {
      setCargando(true)
      setError(null)
      // const resultado = await api.getMiRecurso(id)
      // setDatos(resultado)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error al cargar')
    } finally {
      setCargando(false)
    }
  }, [id])

  useEffect(() => {
    cargar()
  }, [cargar])

  return { datos, cargando, error, refetch: cargar }
}
```

## Clases Tailwind del proyecto

### Contenedores y layout
```tsx
// Página principal con padding
<div className="space-y-6">

// Card estándar
<div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">

// Grid de 2 columnas (responsivo)
<div className="grid grid-cols-1 gap-4 sm:grid-cols-2">

// Grid de 3 columnas
<div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
```

### Tipografía
```tsx
<h1 className="text-2xl font-bold text-gray-900">Título</h1>
<h2 className="text-lg font-semibold text-gray-800">Subtítulo</h2>
<p className="text-sm text-gray-500">Texto secundario</p>
<span className="text-xs text-gray-400">Etiqueta pequeña</span>
```

### Botones
```tsx
// Primario
<button className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50">
  Acción
</button>

// Secundario
<button className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">
  Cancelar
</button>

// Peligro
<button className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700">
  Eliminar
</button>
```

### Semáforo de dominio (colores DIAN)
```tsx
// VERDE — dominado
<span className="inline-flex rounded-full bg-green-100 px-2 text-xs font-semibold text-green-800">
  Dominado
</span>

// AMARILLO — en progreso
<span className="inline-flex rounded-full bg-yellow-100 px-2 text-xs font-semibold text-yellow-800">
  En progreso
</span>

// ROJO — brecha
<span className="inline-flex rounded-full bg-red-100 px-2 text-xs font-semibold text-red-800">
  Brecha
</span>
```

### Formularios
```tsx
<div className="space-y-4">
  <div>
    <label htmlFor="campo" className="block text-sm font-medium text-gray-700">
      Label
    </label>
    <input
      id="campo"
      type="text"
      className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
    />
  </div>
</div>
```

### Estado vacío
```tsx
<div className="rounded-lg border-2 border-dashed border-gray-300 p-12 text-center">
  <p className="text-sm text-gray-500">No hay elementos todavía.</p>
  <button className="mt-3 text-sm font-medium text-blue-600 hover:text-blue-800">
    Agregar primero
  </button>
</div>
```

### Mensajes de error y éxito
```tsx
// Error
<div className="rounded-md bg-red-50 p-4">
  <p className="text-sm font-medium text-red-800">{error}</p>
</div>

// Éxito
<div className="rounded-md bg-green-50 p-4">
  <p className="text-sm font-medium text-green-800">{mensaje}</p>
</div>
```

## Añadir una ruta nueva en App.tsx

```tsx
// En App.tsx, dentro del bloque de rutas protegidas:
<Route path="/mi-ruta" element={<MiPagina />} />

// Con parámetro de URL:
<Route path="/concursos/:concursoId/simulacro" element={<ConfigurarSimulacro />} />
```

## Añadir un endpoint a la capa API

```typescript
// api/miRecurso.ts
import client from './client'
import type { MiRequest, MiResponse } from '../types'

export const miApi = {
  listar: (): Promise<MiResponse[]> =>
    client.get('/mi-recurso').then(r => r.data),

  obtener: (id: string): Promise<MiResponse> =>
    client.get(`/mi-recurso/${id}`).then(r => r.data),

  crear: (data: MiRequest): Promise<MiResponse> =>
    client.post('/mi-recurso', data).then(r => r.data),

  actualizar: (id: string, data: Partial<MiRequest>): Promise<MiResponse> =>
    client.put(`/mi-recurso/${id}`, data).then(r => r.data),

  eliminar: (id: string): Promise<void> =>
    client.delete(`/mi-recurso/${id}`).then(r => r.data),
}
```

## Añadir tipos en types/index.ts

```typescript
// Siempre agregar al final del archivo types/index.ts

export interface MiTipo {
  id: string           // UUID siempre como string en TS
  nombre: string
  creadoEn: string     // ISO 8601 — usar new Date(creadoEn) para mostrar
}

export interface MiRequest {
  nombre: string
}
```

## Checklist antes de crear un componente

- [ ] ¿El componente tiene sus props tipadas con `interface`?
- [ ] ¿Los estados de `cargando` y `error` están manejados?
- [ ] ¿Se usa `useAuthStore()` para obtener el token si es necesario?
- [ ] ¿Las llamadas API están en `src/api/` y no inline en el componente?
- [ ] ¿Los tipos están definidos en `src/types/index.ts`?
- [ ] ¿La ruta está registrada en `App.tsx` dentro de `ProtectedRoute`?
