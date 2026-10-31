import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { Home, BookOpen, LogOut, GraduationCap, User, Menu, X } from 'lucide-react'
import { useAuthStore } from '@/store/authStore'

const navLinks = [
  { to: '/dashboard', label: 'Inicio',    icon: Home },
  { to: '/concursos', label: 'Mis concursos', icon: BookOpen },
]

export default function Layout({ children }: { children: React.ReactNode }) {
  const { usuario, logout } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()
  const [menuAbierto, setMenuAbierto] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const estaActivo = (to: string) => location.pathname.startsWith(to)

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      {/* Header */}
      <header className="bg-brand-800 text-white shadow-md sticky top-0 z-20">
        <div className="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
          {/* Marca */}
          <Link to="/dashboard" className="flex items-center gap-2.5">
            <span className="inline-flex items-center justify-center w-9 h-9 rounded-xl bg-white/15">
              <GraduationCap size={22} />
            </span>
            <span className="flex flex-col leading-tight">
              <span className="text-lg font-bold tracking-tight">Preparación Concursos</span>
              <span className="text-brand-200 text-xs hidden sm:block">Para concursos del SIMO / CNSC</span>
            </span>
          </Link>

          {/* Navegación escritorio */}
          <nav className="hidden md:flex items-center gap-1">
            {navLinks.map((l) => {
              const Icono = l.icon
              return (
                <Link
                  key={l.to}
                  to={l.to}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg text-base font-medium transition-colors
                    ${estaActivo(l.to) ? 'bg-white/15 text-white' : 'text-brand-100 hover:bg-white/10'}`}
                >
                  <Icono size={18} /> {l.label}
                </Link>
              )
            })}
            <div className="ml-3 flex items-center gap-3 border-l border-white/20 pl-3">
              <span className="flex items-center gap-2 text-sm text-brand-100">
                <span className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-white/15">
                  <User size={16} />
                </span>
                <span className="hidden lg:block max-w-[140px] truncate">{usuario?.nombre}</span>
              </span>
              <button
                onClick={handleLogout}
                className="flex items-center gap-1.5 text-sm text-brand-100 hover:text-white px-2 py-1.5 rounded-lg hover:bg-white/10 transition-colors"
              >
                <LogOut size={16} /> Salir
              </button>
            </div>
          </nav>

          {/* Botón de menú móvil */}
          <button
            onClick={() => setMenuAbierto(v => !v)}
            className="md:hidden inline-flex items-center justify-center w-11 h-11 rounded-lg hover:bg-white/10"
            aria-label={menuAbierto ? 'Cerrar menú' : 'Abrir menú'}
            aria-expanded={menuAbierto}
          >
            {menuAbierto ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {/* Menú móvil desplegable */}
        {menuAbierto && (
          <nav className="md:hidden border-t border-white/10 bg-brand-800 px-4 py-3 space-y-1 animate-fade-in">
            <div className="flex items-center gap-2 px-2 py-2 text-brand-100 text-sm">
              <User size={18} /> {usuario?.nombre}
            </div>
            {navLinks.map((l) => {
              const Icono = l.icon
              return (
                <Link
                  key={l.to}
                  to={l.to}
                  onClick={() => setMenuAbierto(false)}
                  className={`flex items-center gap-3 px-3 py-3 rounded-lg text-base font-medium
                    ${estaActivo(l.to) ? 'bg-white/15 text-white' : 'text-brand-100 hover:bg-white/10'}`}
                >
                  <Icono size={20} /> {l.label}
                </Link>
              )
            })}
            <button
              onClick={handleLogout}
              className="w-full flex items-center gap-3 px-3 py-3 rounded-lg text-base font-medium text-brand-100 hover:bg-white/10"
            >
              <LogOut size={20} /> Salir
            </button>
          </nav>
        )}
      </header>

      {/* Contenido */}
      <main className="flex-1 max-w-6xl mx-auto w-full px-4 py-6 sm:py-8 pb-24 md:pb-8">
        {children}
      </main>

      {/* Navegación inferior fija (solo móvil) — más fácil de alcanzar con el pulgar */}
      <nav className="md:hidden fixed bottom-0 inset-x-0 bg-white border-t border-slate-200 flex z-20">
        {navLinks.map((l) => {
          const Icono = l.icon
          const activo = estaActivo(l.to)
          return (
            <Link
              key={l.to}
              to={l.to}
              className={`flex-1 flex flex-col items-center gap-0.5 py-2.5 text-xs font-medium
                ${activo ? 'text-brand-700' : 'text-slate-500'}`}
            >
              <Icono size={22} strokeWidth={activo ? 2.5 : 2} />
              {l.label}
            </Link>
          )
        })}
        <button
          onClick={() => setMenuAbierto(true)}
          className="flex-1 flex flex-col items-center gap-0.5 py-2.5 text-xs font-medium text-slate-500"
        >
          <User size={22} />
          Cuenta
        </button>
      </nav>

      {/* Footer */}
      <footer className="hidden md:block bg-white border-t border-slate-200 text-center text-sm text-slate-400 py-4">
        Preparación para Concursos de Méritos · SIMO / CNSC — Colombia
      </footer>
    </div>
  )
}
