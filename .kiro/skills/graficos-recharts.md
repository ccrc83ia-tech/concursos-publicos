---
name: graficos-recharts
description: Patrones para dashboards y gráficos con Recharts en el proyecto DIAN — semáforo, barras de progreso, radar de competencias
---

# Skill: Gráficos con Recharts

## Gráficos del proyecto DIAN

| Gráfico | Componente Recharts | Dónde se usa |
|---|---|---|
| Barras de dominio por ítem | `BarChart` | Dashboard principal |
| Radar de competencias | `RadarChart` | Vista por prueba |
| Línea de progreso temporal | `LineChart` | Evolución de simulacros |
| Dona de distribución | `PieChart` | % aprobado/reprobado |

## Setup — importaciones

```tsx
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell, ReferenceLine,
  RadarChart, Radar, PolarGrid, PolarAngleAxis,
  LineChart, Line, Legend,
  PieChart, Pie,
} from 'recharts'
```

## Colores del semáforo DIAN

```tsx
const COLORES_SEMAFORO = {
  VERDE:    '#16a34a',   // green-600
  AMARILLO: '#ca8a04',   // yellow-600
  ROJO:     '#dc2626',   // red-600
}

const UMBRAL_FUNCIONAL    = 70
const UMBRAL_OTRO         = 60

function colorPorPuntaje(puntaje: number, esFuncional: boolean): string {
  const umbral = esFuncional ? UMBRAL_FUNCIONAL : UMBRAL_OTRO
  if (puntaje >= umbral)  return COLORES_SEMAFORO.VERDE
  if (puntaje >= 50)      return COLORES_SEMAFORO.AMARILLO
  return COLORES_SEMAFORO.ROJO
}
```

## Gráfico de barras — dominio por ítem

```tsx
interface DatoItem {
  nombre: string       // nombre corto del indicador
  puntaje: number      // 0-100
  tipoPrueba: 'FUNCIONAL' | 'COMPORTAMENTAL' | 'INTEGRIDAD'
}

function GraficoDominioItems({ datos }: { datos: DatoItem[] }) {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-6">
      <h2 className="mb-4 text-base font-semibold text-gray-800">
        Dominio por Indicador
      </h2>
      <ResponsiveContainer width="100%" height={320}>
        <BarChart data={datos} margin={{ top: 5, right: 20, left: 0, bottom: 80 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis
            dataKey="nombre"
            tick={{ fontSize: 11 }}
            angle={-45}
            textAnchor="end"
            interval={0}
          />
          <YAxis domain={[0, 100]} unit="%" tick={{ fontSize: 12 }} />
          <Tooltip
            formatter={(value: number) => [`${value}%`, 'Puntaje']}
            contentStyle={{ borderRadius: '8px', fontSize: '13px' }}
          />
          {/* Línea de umbral funcional */}
          <ReferenceLine
            y={70}
            stroke="#dc2626"
            strokeDasharray="5 5"
            label={{ value: 'Umbral 70%', position: 'right', fontSize: 11 }}
          />
          {/* Línea de umbral comportamental/integridad */}
          <ReferenceLine
            y={60}
            stroke="#ca8a04"
            strokeDasharray="5 5"
            label={{ value: 'Umbral 60%', position: 'right', fontSize: 11 }}
          />
          <Bar dataKey="puntaje" radius={[4, 4, 0, 0]}>
            {datos.map((d, i) => (
              <Cell
                key={i}
                fill={colorPorPuntaje(d.puntaje, d.tipoPrueba === 'FUNCIONAL')}
              />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
```

## Gráfico radar — competencias por prueba

```tsx
interface DatoRadar {
  indicador: string
  puntaje: number
  fullMark: 100
}

function GraficoRadarCompetencias({
  datos,
  titulo
}: {
  datos: DatoRadar[]
  titulo: string
}) {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-6">
      <h2 className="mb-4 text-base font-semibold text-gray-800">{titulo}</h2>
      <ResponsiveContainer width="100%" height={300}>
        <RadarChart data={datos}>
          <PolarGrid />
          <PolarAngleAxis dataKey="indicador" tick={{ fontSize: 11 }} />
          <Radar
            name="Puntaje"
            dataKey="puntaje"
            stroke="#2563eb"
            fill="#2563eb"
            fillOpacity={0.2}
          />
          <Tooltip formatter={(v: number) => [`${v}%`, 'Dominio']} />
        </RadarChart>
      </ResponsiveContainer>
    </div>
  )
}
```

## Gráfico de línea — evolución histórica

```tsx
interface DatoEvolucion {
  fecha: string     // 'DD/MM'
  funcional: number
  comportamental: number
  integridad: number
}

function GraficoEvolucion({ datos }: { datos: DatoEvolucion[] }) {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-6">
      <h2 className="mb-4 text-base font-semibold text-gray-800">
        Evolución de Puntajes
      </h2>
      <ResponsiveContainer width="100%" height={260}>
        <LineChart data={datos}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis dataKey="fecha" tick={{ fontSize: 12 }} />
          <YAxis domain={[0, 100]} unit="%" tick={{ fontSize: 12 }} />
          <Tooltip formatter={(v: number) => [`${v}%`]} />
          <Legend />
          <ReferenceLine y={70} stroke="#dc2626" strokeDasharray="4 4" />
          <Line
            type="monotone"
            dataKey="funcional"
            name="Funcionales"
            stroke="#2563eb"
            strokeWidth={2}
            dot={{ r: 4 }}
          />
          <Line
            type="monotone"
            dataKey="comportamental"
            name="Comportamentales"
            stroke="#7c3aed"
            strokeWidth={2}
            dot={{ r: 4 }}
          />
          <Line
            type="monotone"
            dataKey="integridad"
            name="Integridad"
            stroke="#059669"
            strokeWidth={2}
            dot={{ r: 4 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}
```

## Gráfico de dona — resumen aprobados/reprobados

```tsx
interface DatoDona {
  name: string
  value: number
  color: string
}

function GraficoDona({ aprobados, reprobados }: { aprobados: number; reprobados: number }) {
  const datos: DatoDona[] = [
    { name: 'Dominados', value: aprobados,  color: '#16a34a' },
    { name: 'Brechas',   value: reprobados, color: '#dc2626' },
  ]

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-6">
      <h2 className="mb-2 text-base font-semibold text-gray-800">Resumen Global</h2>
      <ResponsiveContainer width="100%" height={200}>
        <PieChart>
          <Pie
            data={datos}
            cx="50%"
            cy="50%"
            innerRadius={55}
            outerRadius={80}
            dataKey="value"
            label={({ name, value }) => `${name}: ${value}`}
            labelLine={false}
          >
            {datos.map((d, i) => (
              <Cell key={i} fill={d.color} />
            ))}
          </Pie>
          <Tooltip formatter={(v: number) => [v, 'Indicadores']} />
        </PieChart>
      </ResponsiveContainer>
    </div>
  )
}
```

## Barra de progreso personalizada (sin Recharts)

Para barras de progreso individuales dentro de tarjetas usar CSS puro:

```tsx
function BarraProgreso({
  puntaje,
  esFuncional = false
}: {
  puntaje: number
  esFuncional?: boolean
}) {
  const umbral = esFuncional ? 70 : 60
  const color =
    puntaje >= umbral ? 'bg-green-500' :
    puntaje >= 50     ? 'bg-yellow-500' :
                        'bg-red-500'

  return (
    <div className="mt-1 flex items-center gap-2">
      <div className="h-2 flex-1 overflow-hidden rounded-full bg-gray-200">
        <div
          className={`h-full rounded-full transition-all duration-500 ${color}`}
          style={{ width: `${Math.min(puntaje, 100)}%` }}
        />
      </div>
      <span className="w-10 text-right text-xs font-medium text-gray-600">
        {puntaje.toFixed(0)}%
      </span>
    </div>
  )
}
```

## Transformar datos del backend al formato Recharts

```typescript
// Los datos vienen del API como DesempenoItemResponse[]
// Transformar para BarChart:
function transformarParaGrafico(desempenos: DesempenoItemResponse[]): DatoItem[] {
  return desempenos.map(d => ({
    // Nombre corto: primeras 2 palabras del indicador
    nombre: d.nombreIndicador.split(' ').slice(0, 2).join(' '),
    puntaje: Math.round(d.porcentajeAcierto),
    tipoPrueba: d.tipoPrueba,
  }))
}
```

## Notas de rendimiento

- Siempre usar `ResponsiveContainer` — nunca hardcodear width en píxeles
- Para muchos ítems (11 funcionales), rotar labels con `angle={-45}` en XAxis
- Usar `height` fijo en el contenedor padre para evitar colapso
- Los gráficos no necesitan `memo` a menos que los datos cambien frecuentemente
