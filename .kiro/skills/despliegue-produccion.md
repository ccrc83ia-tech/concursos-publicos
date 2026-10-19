---
name: despliegue-produccion
description: Guía para desplegar la aplicación en producción usando servicios gratuitos
---

# Skill: Despliegue a Producción (100% Gratuito)

## Stack de producción

| Servicio | Plan | Para qué |
|---|---|---|
| **Render** | Free | Backend Java Spring Boot |
| **Supabase** | Free | PostgreSQL en la nube |
| **Vercel** | Free | Frontend React |
| **GitHub** | Free | Código fuente + CI/CD |

## Pasos

### 1. Preparar repositorio GitHub
```powershell
cd c:\proyectos\concursos-publicos
git init
git add .
git commit -m "MVP inicial"
git remote add origin https://github.com/tu-usuario/concursos-publicos.git
git push -u origin main
```

Crear `.gitignore` en `c:\proyectos\concursos-publicos\`:
```
backend/build/
backend/.gradle/
frontend/node_modules/
frontend/dist/
*.env.local
```

### 2. Base de datos — Supabase
1. Ir a supabase.com → New project
2. Copiar la connection string: `postgresql://user:pass@host:5432/postgres`
3. Guardar como `DB_URL` en Render

### 3. Backend — Render
1. New Web Service → conectar repo GitHub → rama `main`
2. Root Directory: `backend`
3. Build Command: `./gradlew bootJar`
4. Start Command: `java -jar build/libs/dian-prep-backend-0.0.1-SNAPSHOT.jar`
5. Variables de entorno:
   ```
   DB_URL=jdbc:postgresql://...supabase.co:5432/postgres
   DB_USERNAME=postgres
   DB_PASSWORD=tu-password
   JWT_SECRET=clave-super-secreta-minimo-256-bits
   GROQ_API_KEY=gsk_...
   GEMINI_API_KEY=AI...
   ```

### 4. Frontend — Vercel
1. New Project → conectar repo GitHub
2. Root Directory: `frontend`
3. Framework: Vite
4. Variables de entorno:
   ```
   VITE_API_URL=https://tu-app.onrender.com/api/v1
   ```

### 5. Configurar CORS en production
En `SecurityConfig.java` agregar la URL de Vercel:
```java
config.setAllowedOrigins(List.of(
    "http://localhost:5173",
    "https://tu-app.vercel.app"
));
```

## Notas
- Render free tier hiberna tras 15 min de inactividad (primer request tarda ~30s)
- Supabase free tier: 500MB storage, 2 CPU hours/día — suficiente para uso personal
- Vercel free tier: ilimitado para proyectos personales
