---
name: perfil-clasificacion-concurso
description: Cómo funciona la clasificación configurable de indicadores que permite usar la app con cualquier concurso del SIMO, no solo DIAN
---

# Skill: Perfil de Clasificación por Concurso

## Para qué sirve
La app debe funcionar con **cualquier convocatoria del SIMO**, no solo la DIAN. Por eso
las reglas de clasificación de indicadores no están cableadas en código: viven en
`PerfilClasificacion` (`domain/model`) y pueden personalizarse por concurso.

## Qué parametriza el perfil
- `palabrasIntegridad` — set de nombres que se marcan como INTEGRIDAD (p. ej. COMPROMISO, HONESTIDAD...)
- `palabraComportamental` — palabra que marca un indicador como COMPORTAMENTAL (default `NIVEL`)
- regex de nivel — derivado de la palabra comportamental: `<palabra>\s+(\d)`
- `umbralFuncional` — % de aprobación funcional (default 70)
- `umbralComportamentalIntegridad` — % para comportamental/integridad (default 60)

## Perfil por defecto
`PerfilClasificacion.porDefecto()` reproduce la estructura típica CNSC (la de DIAN 2676).
`PerfilClasificacion.de(...)` arma un perfil con overrides parciales: lo que llega null/vacío
cae al valor por defecto, así el llamador solo especifica lo que cambia.

## Dónde se usa
- `ItemService.parsarYCrear(...)` construye el perfil desde `ParsarIndicadoresCommand` y lo
  usa para clasificar cada línea y asignar el umbral de cada ítem.
- El endpoint `POST /api/v1/concursos/{concursoId}/items/parsear` acepta en el body
  (`ParsarIndicadoresRequest`) los campos opcionales del perfil. Si no se envían, se usa el default.

## Reglas al extender
1. Nunca re-introducir constantes de clasificación DIAN hardcodeadas en `ItemService` u otros
   servicios. Toda regla específica de un concurso va en el perfil (datos), no en código.
2. Los tres `TipoPrueba` (FUNCIONAL/COMPORTAMENTAL/INTEGRIDAD) siguen siendo un enum cerrado.
   Convertirlo en entidad configurable por concurso (para concursos sin integridad o con otras
   pruebas) es deuda técnica pendiente; si se aborda, requiere migración de BD.
3. Textos y defaults del frontend deben ser genéricos (SIMO/CNSC), no "DIAN".

## Ejemplo: parsear con perfil propio
```json
POST /api/v1/concursos/{id}/items/parsear
{
  "textoIndicadores": "ALGORITMOS Y ESTRUCTURAS DE DATOS\nLIDERAZGO NIVEL 2\nHONESTIDAD",
  "palabrasIntegridad": ["HONESTIDAD", "LEALTAD"],
  "palabraComportamental": "NIVEL",
  "umbralFuncional": 65,
  "umbralComportamentalIntegridad": 55
}
```
