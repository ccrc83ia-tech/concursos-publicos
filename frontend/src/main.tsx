import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Toaster } from 'sonner'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
    {/* Notificaciones globales: grandes y legibles, arriba-centro. */}
    <Toaster
      position="top-center"
      richColors
      closeButton
      toastOptions={{
        style: { fontSize: '15px', padding: '14px 16px' },
      }}
    />
  </StrictMode>,
)
