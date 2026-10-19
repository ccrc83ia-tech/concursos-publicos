import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { crearConcurso } from '@/api/concursos'
import { parsarIndicadores } from '@/api/items'
import type { Item } from '@/types'
import Spinner from '@/components/shared/Spinner'
import Layout from '@/components/shared/Layout'

const schema = z.object({
  nombre:            z.string().min(3, 'Mínimo 3 caracteres'),
  procesoSeleccion:  z.string().optional(),
  numeroInscripcion: z.string().optional(),
  numeroEmpleo:      z.string().optional(),
  nivelCargo:        z.string().optional(),
  denominacion:      z.string().optional(),
  fechaExamen:       z.string().optional(),
})
type FormData = z.infer<typeof schema>

const TIPO_LABELS = { FUNCIONAL: '🔵 Funcional', COMPORTAMENTAL: '🟡 Comportamental', INTEGRIDAD: '🟠 Integridad' }
const DOMINIO_LABELS = { NO_SE: '🔴 No sé', ALGO_SE: '🟡 Algo sé', LO_DOMINO: '🟢 Lo domino' }

export default function NuevoConcurso() {
  const navigate = useNavigate()
  const [paso, setPaso]               = useState<1 | 2 | 3>(1)
  const [concursoId, setConcursoId]   = useState('')
  const [textoIndicadores, setTexto]  = useState('')
  const [items, setItems]             = useState<Item[]>([])
  const [error, setError]             = useState('')
  const [loadingParseo, setLoadingParseo] = useState(false)

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  // Paso 1: crear el concurso
  const onCrearConcurso = async (data: FormData) => {
    setError('')
    try {
      const concurso = await crearConcurso(data)
      setConcursoId(concurso.id)
      setPaso(2)
    } catch {
      setError('Error al crear el concurso. Verifique los datos.')
    }
  }

  // Paso 2: parsear indicadores
  const onParsarIndicadores = async () => {
    if (!textoIndicadores.trim()) {
      setError('Pegue los indicadores del concurso')
      return
    }
    setError('')
    setLoadingParseo(true)
    try {
      const resultado = await parsarIndicadores(concursoId, textoIndicadores)
      setItems(resultado)
      setPaso(3)
    } catch {
      setError('Error al procesar los indicadores. Intente nuevamente.')
    } finally {
      setLoadingParseo(false)
    }
  }

  // Contar por tipo
  const contarPorTipo = (tipo: string) => items.filter((i) => i.tipoPrueba === tipo).length
  const totalPreguntas = items.reduce((s, i) => s + i.preguntasSolicitadas, 0)

  return (
    <Layout>
      <div className="max-w-2xl mx-auto">
        {/* Stepper */}
        <div className="flex items-center gap-2 mb-8">
          {[1, 2, 3].map((n) => (
            <div key={n} className="flex items-center gap-2">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold
                ${paso >= n ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-500'}`}>
                {n}
              </div>
              {n < 3 && <div className={`h-0.5 w-12 ${paso > n ? 'bg-blue-600' : 'bg-gray-200'}`} />}
            </div>
          ))}
          <div className="ml-2 text-sm text-gray-600">
            {paso === 1 && 'Datos del concurso'}
            {paso === 2 && 'Indicadores a evaluar'}
            {paso === 3 && 'Resumen y confirmar'}
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
            {error}
          </div>
        )}

        {/* PASO 1: Datos del concurso */}
        {paso === 1 && (
          <div className="card">
            <h2 className="text-lg font-semibold mb-5">Datos del concurso</h2>
            <form onSubmit={handleSubmit(onCrearConcurso)} className="space-y-4">
              <div>
                <label className="label">Nombre del concurso *</label>
                <input className="input-field" placeholder="Ej. Convocatoria 2676 — Gestor III" {...register('nombre')} />
                {errors.nombre && <p className="text-red-500 text-xs mt-1">{errors.nombre.message}</p>}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">Proceso de selección</label>
                  <input className="input-field" placeholder="2676" {...register('procesoSeleccion')} />
                </div>
                <div>
                  <label className="label">N.° de inscripción</label>
                  <input className="input-field" {...register('numeroInscripcion')} />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">N.° de empleo</label>
                  <input className="input-field" {...register('numeroEmpleo')} />
                </div>
                <div>
                  <label className="label">Nivel del cargo</label>
                  <input className="input-field" placeholder="Profesional" {...register('nivelCargo')} />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">Denominación</label>
                  <input className="input-field" placeholder="Gestor III" {...register('denominacion')} />
                </div>
                <div>
                  <label className="label">Fecha del examen</label>
                  <input type="date" className="input-field" {...register('fechaExamen')} />
                </div>
              </div>
              <div className="flex justify-end pt-2">
                <button type="submit" disabled={isSubmitting} className="btn-primary flex items-center gap-2">
                  {isSubmitting && <Spinner size="sm" />}
                  Continuar →
                </button>
              </div>
            </form>
          </div>
        )}

        {/* PASO 2: Indicadores */}
        {paso === 2 && (
          <div className="card">
            <h2 className="text-lg font-semibold mb-2">Indicadores a evaluar</h2>
            <p className="text-sm text-gray-500 mb-4">
              Pegue el listado de indicadores exactamente como aparece en el sistema SIMO de la CNSC
              para su convocatoria. El sistema los clasificará automáticamente.
            </p>
            <textarea
              className="input-field font-mono text-xs h-64 resize-none"
              placeholder={`ALGORITMOS Y ESTRUCTURAS DE DATOS\nINGENIERIA DE REQUISITOS\nRAZONAMIENTO ANALITICO\nCOMUNICACION EFECTIVA NIVEL 1\nTRABAJO EN EQUIPO NIVEL 3\nCOMPROMISO\nHONESTIDAD`}
              value={textoIndicadores}
              onChange={(e) => setTexto(e.target.value)}
            />
            <p className="text-xs text-gray-400 mt-1">
              Un indicador por línea. El sistema detecta automáticamente si es Funcional, Comportamental o de Integridad.
            </p>
            <div className="flex justify-between mt-4">
              <button onClick={() => setPaso(1)} className="btn-secondary">← Atrás</button>
              <button
                onClick={onParsarIndicadores}
                disabled={loadingParseo}
                className="btn-primary flex items-center gap-2"
              >
                {loadingParseo && <Spinner size="sm" />}
                {loadingParseo ? 'Procesando...' : 'Procesar indicadores →'}
              </button>
            </div>
          </div>
        )}

        {/* PASO 3: Resumen */}
        {paso === 3 && (
          <div className="card">
            <h2 className="text-lg font-semibold mb-1">✅ Concurso creado exitosamente</h2>
            <p className="text-sm text-gray-500 mb-5">
              Se detectaron <strong>{items.length} indicadores</strong> clasificados automáticamente.
            </p>

            {/* Resumen por tipo */}
            <div className="grid grid-cols-3 gap-3 mb-5">
              {(['FUNCIONAL', 'COMPORTAMENTAL', 'INTEGRIDAD'] as const).map((tipo) => (
                <div key={tipo} className="bg-gray-50 rounded-lg p-3 text-center">
                  <div className="text-2xl font-bold text-gray-800">{contarPorTipo(tipo)}</div>
                  <div className="text-xs text-gray-500 mt-0.5">{TIPO_LABELS[tipo]}</div>
                </div>
              ))}
            </div>

            {/* Lista de indicadores */}
            <div className="border border-gray-200 rounded-lg overflow-hidden mb-5">
              <table className="w-full text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="text-left px-3 py-2 text-xs font-medium text-gray-500">Indicador</th>
                    <th className="text-left px-3 py-2 text-xs font-medium text-gray-500">Tipo</th>
                    <th className="text-center px-3 py-2 text-xs font-medium text-gray-500">Preguntas</th>
                    <th className="text-left px-3 py-2 text-xs font-medium text-gray-500">Dominio</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {items.map((item) => (
                    <tr key={item.id} className="hover:bg-gray-50">
                      <td className="px-3 py-2 text-gray-800 font-medium">
                        {item.nombre}
                        {item.nivelCompetencia && (
                          <span className="ml-1 text-xs text-blue-500">N{item.nivelCompetencia}</span>
                        )}
                      </td>
                      <td className="px-3 py-2 text-gray-500 text-xs">{TIPO_LABELS[item.tipoPrueba]}</td>
                      <td className="px-3 py-2 text-center font-semibold text-blue-700">
                        {item.preguntasSolicitadas}
                      </td>
                      <td className="px-3 py-2 text-xs text-gray-500">{DOMINIO_LABELS[item.dominioPrevio]}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="bg-gray-50">
                  <tr>
                    <td colSpan={2} className="px-3 py-2 text-xs font-medium text-gray-600">Total</td>
                    <td className="px-3 py-2 text-center font-bold text-blue-700">{totalPreguntas}</td>
                    <td />
                  </tr>
                </tfoot>
              </table>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm text-blue-700 mb-5">
              💡 Puede ajustar la cantidad de preguntas por indicador en cualquier momento antes de generar el banco.
            </div>

            <button
              onClick={() => navigate(`/concursos/${concursoId}`)}
              className="btn-primary w-full"
            >
              Ver mi concurso →
            </button>
          </div>
        )}
      </div>
    </Layout>
  )
}
