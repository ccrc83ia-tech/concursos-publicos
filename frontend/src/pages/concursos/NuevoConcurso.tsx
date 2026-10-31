import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import {
  Check, ArrowRight, ArrowLeft, AlertCircle, ListChecks, CheckCircle2, Lightbulb,
} from 'lucide-react'
import { crearConcurso } from '@/api/concursos'
import { parsearIndicadores } from '@/api/items'
import type { Item } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'
import InfoTip from '@/components/shared/InfoTip'

const schema = z.object({
  nombre:            z.string().min(3, 'Escribe un nombre de al menos 3 letras'),
  procesoSeleccion:  z.string().optional(),
  numeroInscripcion: z.string().optional(),
  numeroEmpleo:      z.string().optional(),
  nivelCargo:        z.string().optional(),
  denominacion:      z.string().optional(),
  fechaExamen:       z.string().optional(),
})
type FormData = z.infer<typeof schema>

const TIPO_CHIP: Record<string, string> = {
  FUNCIONAL: 'chip-funcional', COMPORTAMENTAL: 'chip-comportamental', INTEGRIDAD: 'chip-integridad',
}
const TIPO_LABEL: Record<string, string> = {
  FUNCIONAL: 'Conocimiento técnico', COMPORTAMENTAL: 'Comportamental', INTEGRIDAD: 'Integridad',
}
const DOMINIO_LABEL: Record<string, string> = {
  NO_SE: 'No lo sé', ALGO_SE: 'Algo sé', LO_DOMINO: 'Lo domino',
}

const PASOS = ['Datos del concurso', 'Temas a evaluar', 'Listo']

export default function NuevoConcurso() {
  const navigate = useNavigate()
  const [paso, setPaso]              = useState<1 | 2 | 3>(1)
  const [concursoId, setConcursoId]  = useState('')
  const [textoIndicadores, setTexto] = useState('')
  const [items, setItems]            = useState<Item[]>([])
  const [loadingParseo, setLoadingParseo] = useState(false)

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const onCrearConcurso = async (data: FormData) => {
    try {
      const concurso = await crearConcurso(data)
      setConcursoId(concurso.id)
      setPaso(2)
    } catch {
      toast.error('No pudimos crear el concurso. Revisa los datos e intenta de nuevo.')
    }
  }

  const onParsearIndicadores = async () => {
    if (!textoIndicadores.trim()) {
      toast.error('Primero pega la lista de temas de tu convocatoria')
      return
    }
    setLoadingParseo(true)
    try {
      const resultado = await parsearIndicadores(concursoId, textoIndicadores)
      setItems(resultado)
      setPaso(3)
    } catch {
      toast.error('No pudimos procesar los temas. Intenta de nuevo.')
    } finally {
      setLoadingParseo(false)
    }
  }

  const contarPorTipo = (tipo: string) => items.filter((i) => i.tipoPrueba === tipo).length

  return (
    <Layout>
      <div className="max-w-2xl mx-auto">
        {/* Stepper claro con nombres */}
        <ol className="flex items-center gap-2 mb-8">
          {PASOS.map((nombre, i) => {
            const n = i + 1
            const completo = paso > n
            const activo = paso === n
            return (
              <li key={nombre} className="flex items-center gap-2 flex-1 last:flex-none">
                <div className="flex items-center gap-2">
                  <span className={`w-9 h-9 shrink-0 rounded-full flex items-center justify-center text-sm font-bold
                    ${completo ? 'bg-emerald-600 text-white' : activo ? 'bg-brand-700 text-white' : 'bg-slate-200 text-slate-500'}`}>
                    {completo ? <Check size={18} /> : n}
                  </span>
                  <span className={`text-sm font-medium hidden sm:block ${activo || completo ? 'text-slate-800' : 'text-slate-400'}`}>
                    {nombre}
                  </span>
                </div>
                {n < PASOS.length && <div className={`h-0.5 flex-1 rounded ${paso > n ? 'bg-emerald-600' : 'bg-slate-200'}`} />}
              </li>
            )
          })}
        </ol>

        {/* PASO 1 */}
        {paso === 1 && (
          <div className="card animate-fade-in">
            <h1 className="text-xl font-bold text-slate-900 mb-1">Datos del concurso</h1>
            <p className="text-slate-500 mb-6">Solo el nombre es obligatorio. El resto puedes completarlo después.</p>
            <form onSubmit={handleSubmit(onCrearConcurso)} className="space-y-5">
              <div>
                <label className="label" htmlFor="nombre">Nombre del concurso *</label>
                <input id="nombre" className="input-field" placeholder="Ej. Convocatoria DIAN 2676 — Gestor III"
                  aria-invalid={!!errors.nombre} {...register('nombre')} />
                {errors.nombre
                  ? <p className="field-error"><AlertCircle size={15} /> {errors.nombre.message}</p>
                  : <p className="help-text">Ponle un nombre que reconozcas fácilmente.</p>}
              </div>
              <div className="grid sm:grid-cols-2 gap-5">
                <div>
                  <label className="label">Número del proceso</label>
                  <input className="input-field" placeholder="Ej. 2676" {...register('procesoSeleccion')} />
                </div>
                <div>
                  <label className="label">Número de inscripción</label>
                  <input className="input-field" placeholder="Opcional" {...register('numeroInscripcion')} />
                </div>
                <div>
                  <label className="label">Número de empleo</label>
                  <input className="input-field" placeholder="Opcional" {...register('numeroEmpleo')} />
                </div>
                <div>
                  <label className="label">Nivel del cargo</label>
                  <input className="input-field" placeholder="Ej. Profesional" {...register('nivelCargo')} />
                </div>
                <div>
                  <label className="label">Nombre del cargo</label>
                  <input className="input-field" placeholder="Ej. Gestor III" {...register('denominacion')} />
                </div>
                <div>
                  <label className="label">Fecha del examen</label>
                  <input type="date" className="input-field" {...register('fechaExamen')} />
                </div>
              </div>
              <div className="flex justify-end pt-2">
                <button type="submit" disabled={isSubmitting} className="btn-primary">
                  {isSubmitting ? <Spinner size="sm" /> : null}
                  Continuar <ArrowRight size={18} />
                </button>
              </div>
            </form>
          </div>
        )}

        {/* PASO 2 */}
        {paso === 2 && (
          <div className="card animate-fade-in">
            <h1 className="text-xl font-bold text-slate-900 mb-1 flex items-center gap-2">
              Temas a evaluar
              <InfoTip texto="Son los temas (indicadores) que mide tu convocatoria. Los encuentras en el SIMO, en la hoja de 'Ejes temáticos' o 'indicadores' de tu empleo." />
            </h1>
            <p className="text-slate-500 mb-4">
              Copia la lista de temas de tu convocatoria en el SIMO y pégala aquí, un tema por línea.
              Nosotros los clasificamos automáticamente.
            </p>

            <div className="alert-info mb-4">
              <Lightbulb size={20} className="shrink-0 mt-0.5" />
              <div className="text-sm">
                <p className="font-semibold mb-1">¿Cómo pego los temas?</p>
                <p>Escribe o pega cada tema en una línea distinta. No importa si están en mayúsculas.</p>
              </div>
            </div>

            <label className="label" htmlFor="temas">Lista de temas</label>
            <textarea
              id="temas"
              className="input-field font-mono text-sm h-56 resize-none leading-relaxed"
              placeholder={'ALGORITMOS Y ESTRUCTURAS DE DATOS\nINGENIERÍA DE REQUISITOS\nRAZONAMIENTO ANALÍTICO\nCOMUNICACIÓN EFECTIVA NIVEL 1\nTRABAJO EN EQUIPO NIVEL 3\nCOMPROMISO\nHONESTIDAD'}
              value={textoIndicadores}
              onChange={(e) => setTexto(e.target.value)}
            />
            <p className="help-text">Un tema por línea.</p>

            <div className="flex justify-between mt-6">
              <button onClick={() => setPaso(1)} className="btn-secondary">
                <ArrowLeft size={18} /> Atrás
              </button>
              <button onClick={onParsearIndicadores} disabled={loadingParseo} className="btn-primary">
                {loadingParseo ? <Spinner size="sm" /> : null}
                {loadingParseo ? 'Procesando…' : 'Continuar'} {!loadingParseo && <ArrowRight size={18} />}
              </button>
            </div>
          </div>
        )}

        {/* PASO 3 */}
        {paso === 3 && (
          <div className="card animate-fade-in">
            <div className="flex items-center gap-3 mb-1">
              <span className="inline-flex items-center justify-center w-11 h-11 rounded-full bg-emerald-100 text-emerald-600">
                <CheckCircle2 size={26} />
              </span>
              <h1 className="text-xl font-bold text-slate-900">¡Tu concurso está listo!</h1>
            </div>
            <p className="text-slate-500 mb-6">
              Encontramos <strong>{items.length} temas</strong> y los clasificamos por ti.
            </p>

            <div className="grid grid-cols-3 gap-3 mb-6">
              {(['FUNCIONAL', 'COMPORTAMENTAL', 'INTEGRIDAD'] as const).map((tipo) => (
                <div key={tipo} className="bg-slate-50 rounded-xl p-4 text-center">
                  <div className="text-2xl font-bold text-slate-800">{contarPorTipo(tipo)}</div>
                  <div className="text-xs text-slate-500 mt-1">{TIPO_LABEL[tipo]}</div>
                </div>
              ))}
            </div>

            <div className="border border-slate-200 rounded-xl overflow-hidden mb-6">
              <div className="flex items-center gap-2 px-4 py-2.5 bg-slate-50 border-b border-slate-200 text-sm font-semibold text-slate-600">
                <ListChecks size={16} /> Temas detectados
              </div>
              <ul className="divide-y divide-slate-100 max-h-72 overflow-y-auto">
                {items.map((item) => (
                  <li key={item.id} className="flex items-center justify-between gap-3 px-4 py-2.5">
                    <span className="text-slate-800 font-medium text-sm">
                      {item.nombre}
                      {item.nivelCompetencia && <span className="ml-1.5 text-xs text-brand-500">Nivel {item.nivelCompetencia}</span>}
                    </span>
                    <span className="flex items-center gap-2 shrink-0">
                      <span className="text-xs text-slate-400">{DOMINIO_LABEL[item.dominioPrevio]}</span>
                      <span className={TIPO_CHIP[item.tipoPrueba]}>{TIPO_LABEL[item.tipoPrueba]}</span>
                    </span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="alert-info mb-6">
              <Lightbulb size={20} className="shrink-0 mt-0.5" />
              <p className="text-sm">
                En la siguiente pantalla podrás generar las preguntas de práctica de cada tema
                y ajustar cuántas quieres.
              </p>
            </div>

            <button onClick={() => navigate(`/concursos/${concursoId}`)} className="btn-primary btn-lg w-full">
              Ver mi concurso <ArrowRight size={20} />
            </button>
          </div>
        )}
      </div>
    </Layout>
  )
}
