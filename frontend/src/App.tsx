import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from '@/components/shared/ProtectedRoute'
import Login          from '@/pages/auth/Login'
import Registro       from '@/pages/auth/Registro'
import Dashboard      from '@/pages/dashboard/Dashboard'
import ListaConcursos from '@/pages/concursos/ListaConcursos'
import NuevoConcurso  from '@/pages/concursos/NuevoConcurso'
import DetalleConcurso from '@/pages/concursos/DetalleConcurso'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Públicas */}
        <Route path="/login"    element={<Login />} />
        <Route path="/registro" element={<Registro />} />

        {/* Protegidas */}
        <Route path="/dashboard" element={
          <ProtectedRoute><Dashboard /></ProtectedRoute>
        } />
        <Route path="/concursos" element={
          <ProtectedRoute><ListaConcursos /></ProtectedRoute>
        } />
        <Route path="/concursos/nuevo" element={
          <ProtectedRoute><NuevoConcurso /></ProtectedRoute>
        } />
        <Route path="/concursos/:id" element={
          <ProtectedRoute><DetalleConcurso /></ProtectedRoute>
        } />

        {/* Redirect raíz */}
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
