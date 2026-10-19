---
inclusion: fileMatch
fileMatchPattern: "**/*.{ts,tsx}"
---

# Convenciones Frontend React

## Versiones
- React 18 + TypeScript 5
- Vite 5
- Node.js v24

## Librerías

| Librería | Versión | Uso |
|---|---|---|
| react-router-dom | ^6 | Navegación |
| tailwindcss | ^3 | Estilos |
| zustand | ^5 | Estado global |
| axios | ^1 | HTTP client |
| react-hook-form | ^7 | Formularios |
| zod | ^3 | Validación schemas |
| @hookform/resolvers | ^3 | Integración zod + react-hook-form |
| recharts | ^2 | Gráficos (brechas, progreso) — Fase 2 |
| lucide-react | ^0.4 | Iconos |
| sonner | ^1 | Notificaciones toast |

## Estructura

```
frontend/src/
├── api/            → auth.ts, concursos.ts, items.ts
├── components/
│   └── shared/     → Layout, ProtectedRoute, Spinner, SemaforoDominio
├── pages/
│   ├── auth/       → Login.tsx, Registro.tsx
│   ├── dashboard/  → Dashboard.tsx
│   └── concursos/  → ListaConcursos.tsx, NuevoConcurso.tsx, DetalleConcurso.tsx
├── store/          → authStore.ts (Zustand + persist)
└── types/          → index.ts (Concurso, Item, Usuario, etc.)
```

## Rutas activas

| Ruta | Componente | Auth |
|---|---|---|
| `/login` | Login | Pública |
| `/registro` | Registro | Pública |
| `/dashboard` | Dashboard | Protegida |
| `/concursos` | ListaConcursos | Protegida |
| `/concursos/nuevo` | NuevoConcurso | Protegida |
| `/concursos/:id` | DetalleConcurso | Protegida |

## Rutas pendientes (Fase 2)

| Ruta | Componente |
|---|---|
| `/simulacro/nuevo` | ConfigurarSimulacro |
| `/simulacro/:id` | SimulacroActivo (cronómetro) |
| `/simulacro/:id/resultado` | ResultadoSimulacro |
| `/brechas/:concursoId` | DashboardBrechas |
| `/recomendaciones/:simId` | Recomendaciones |

## Convenciones

- Componentes: PascalCase
- Hooks: `use` + PascalCase (`useAuth`, `useCronometro`)
- API functions: camelCase (`crearConcurso`, `parsarIndicadores`)
- Token JWT en `localStorage` via Zustand persist (`dian_auth`)
- Axios interceptor agrega `Authorization: Bearer <token>` automáticamente
- Auth client en `api/auth.ts` apunta a `http://localhost:8080/api/auth`
- API client en `api/client.ts` apunta a `VITE_API_URL` (default: `http://localhost:8080/api/v1`)

## Variables de entorno

Archivo `frontend/.env.local`:
```
VITE_API_URL=http://localhost:8080/api/v1
```

## Comandos

```powershell
npm install      # instalar dependencias
npm run dev      # http://localhost:5173
npm run build    # build producción
```

## CSS utilitarios definidos en index.css

- `.btn-primary` — botón azul principal
- `.btn-secondary` — botón gris secundario
- `.card` — tarjeta blanca con sombra
- `.input-field` — campo de formulario
- `.label` — etiqueta de campo
- `.badge-rojo` / `.badge-amarillo` / `.badge-verde` — semáforo de dominio
