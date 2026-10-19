---
name: simulacro-cronometrado
description: Implementación de simulacros cronometrados con detección de brechas y recomendaciones
---

# Skill: Simulacros Cronometrados

## Tipos de simulacro

| Tipo | Descripción | Preguntas |
|---|---|---|
| `POR_ITEM` | Solo preguntas de un indicador | 20-50 |
| `COMPLETO` | Todos los indicadores del concurso | Proporcional al peso |
| `BRECHAS` | 70% ítems fallidos + 30% dominados | Variable |

## Flujo del simulacro (endpoints REALES ya implementados)

```
1. POST /api/v1/simulacros
   { concursoId, tipo, itemIds, preguntasPorItem, tiempoLimiteSegundos }
   → arma el simulacro con preguntas aleatorias (PreguntaRepository.buscarAleatoriasPorItemId)
   → estado EN_CURSO
   → responde SimulacroResponse SIN revelar la respuesta correcta

2. GET /api/v1/simulacros/{id}
   → el simulacro con sus preguntas (enunciado + 4 opciones)

3. POST /api/v1/simulacros/{id}/responder
   { simulacroPreguntaId, respuesta, marcadaParaRevision, tiempoRespuestaSegundos }
   → califica contra la respuesta correcta real y persiste

4. POST /api/v1/simulacros/{id}/finalizar?tiempoUsadoSegundos=NNN
   → calcula puntaje global y DesempenoItem por ítem (semáforo)
   → estado COMPLETADO; ahora SÍ se revelan respuestas y justificaciones

5. GET /api/v1/simulacros/{id}/desempeno      → desempeño por ítem del simulacro
   GET /api/v1/simulacros/brechas/{concursoId} → último desempeño por ítem (dashboard de brechas)
```

> Anti-trampa: `SimulacroResponse` oculta `respuestaCorrecta` / justificaciones mientras
> el simulacro está EN_CURSO; solo las incluye cuando está COMPLETADO.

> Las recomendaciones con IA aún NO están implementadas (pendiente).

## Lógica de brechas (IMPLEMENTADA en `DesempenoItem`)

```java
// El umbral se toma del ítem (item.getUmbralBrecha()), que viene del perfil del concurso.
NivelDominio nivel = porcentaje < 50.0   ? ROJO
                   : porcentaje < umbral ? AMARILLO
                   : VERDE;
```

El semáforo usa el umbral configurado por ítem, no un 70/60 fijo. Ver `DesempenoItem.clasificarNivel`.

## Examen de brechas 70/30 (PENDIENTE)

El tipo `BRECHAS` ya está modelado pero la selección ponderada todavía no está implementada:
```
// 70% preguntas de ítems con brecha (ROJO/AMARILLO) + 30% de dominados (VERDE)
```

## Componentes frontend

Ya existen:
- `pages/simulacros/ConfigurarSimulacro.tsx`
- `pages/simulacros/SimulacroActivo.tsx` — cronómetro inline con auto-finalización al llegar a 0
- `pages/simulacros/ResultadoSimulacro.tsx` — puntaje + semáforo por ítem + revisión de respuestas
- `api/simulacros.ts`

Pendientes:
- `pages/brechas/DashboardBrechas.tsx` — gráfico Recharts (el endpoint `/brechas/{concursoId}` ya existe)
- `pages/brechas/Recomendaciones.tsx`

> Nota: el cronómetro se implementó inline en `SimulacroActivo`, no como hook `useCronometro`
> separado. Si se reutiliza en otra pantalla, conviene extraerlo al hook de abajo.

## Hook useCronometro

```typescript
const useCronometro = (segundosInicio: number, onTiempoAgotado: () => void) => {
  const [segundos, setSegundos] = useState(segundosInicio)
  const [activo, setActivo]     = useState(true)

  useEffect(() => {
    if (!activo || segundos <= 0) {
      if (segundos <= 0) onTiempoAgotado()
      return
    }
    const timer = setInterval(() => setSegundos(s => s - 1), 1000)
    return () => clearInterval(timer)
  }, [activo, segundos])

  const formatear = () => {
    const m = Math.floor(segundos / 60).toString().padStart(2, '0')
    const s = (segundos % 60).toString().padStart(2, '0')
    return `${m}:${s}`
  }

  return { segundos, formatear, pausar: () => setActivo(false), reanudar: () => setActivo(true) }
}
```
