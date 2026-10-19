import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'

const navLinks = [
  { to: '/dashboard',  label: 'Inicio',    icon: '🏠' },
  { to: '/concursos',  label: 'Concursos', icon: '📋' },
]

export default function Layout({ children }: { children: React.ReactNode }) {
  const { usuario, logout } = useAuthStore()
  const navigate            = useNavigate()
  const location            = useLocation()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex flex-col">
      {/* Header */}
      <header className="bg-[#003F7F] text-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="text-xl font-bold tracking-tight">DIAN Prep</span>
            <span className="text-blue-300 text-sm hidden sm:block">Preparación para Concursos</span>
          </div>
          <nav className="flex items-center gap-1">
            {navLinks.map((l) => (
              <Link
                key={l.to}
                to={l.to}
                className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors
                  ${location.pathname.startsWith(l.to)
                    ? 'bg-blue-700 text-white'
                    : 'text-blue-100 hover:bg-blue-700/60'}`}
              >
                {l.icon} {l.label}
              </Link>
            ))}
            <div className="ml-3 flex items-center gap-2 border-l border-blue-600 pl-3">
              <span className="text-sm text-blue-200 hidden sm:block">{usuario?.nombre}</span>
              <button
                onClick={handleLogout}
                className="text-sm text-blue-100 hover:text-white transition-colors"
              >
                Salir
              </button>
            </div>
          </nav>
        </div>
      </header>

      {/* Contenido */}
      <main className="flex-1 max-w-7xl mx-auto w-full px-4 py-6">
        {children}
      </main>

      {/* Footer */}
      <footer className="bg-gray-100 border-t border-gray-200 text-center text-xs text-gray-500 py-3">
        DIAN Prep — Proceso de Selección DIAN 2676
      </footer>
    </div>
  )
}
