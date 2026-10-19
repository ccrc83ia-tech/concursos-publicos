import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isLogged = useAuthStore((s) => s.isLogged())
  return isLogged ? <>{children}</> : <Navigate to="/login" replace />
}
