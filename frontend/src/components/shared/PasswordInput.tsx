import { forwardRef, useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'

type Props = React.InputHTMLAttributes<HTMLInputElement>

/**
 * Campo de contraseña con botón para mostrar/ocultar el texto. Ver lo que se
 * escribe reduce errores de tecleo, algo especialmente útil para quienes no
 * tienen mucha práctica escribiendo contraseñas.
 */
const PasswordInput = forwardRef<HTMLInputElement, Props>(function PasswordInput(props, ref) {
  const [visible, setVisible] = useState(false)
  return (
    <div className="relative">
      <input
        ref={ref}
        type={visible ? 'text' : 'password'}
        className="input-field pr-12"
        {...props}
      />
      <button
        type="button"
        onClick={() => setVisible(v => !v)}
        aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
        className="absolute inset-y-0 right-0 flex items-center px-3.5 text-slate-400 hover:text-brand-600"
      >
        {visible ? <EyeOff size={20} /> : <Eye size={20} />}
      </button>
    </div>
  )
})

export default PasswordInput
