export interface Usuario {
  id:     string
  nombre: string
  email:  string
  rol:    'ESTUDIANTE' | 'ADMIN'
  token:  string
}

export type TipoPrueba    = 'FUNCIONAL' | 'COMPORTAMENTAL' | 'INTEGRIDAD'
export type DominioPrevio = 'NO_SE' | 'ALGO_SE' | 'LO_DOMINO'
export type NivelDominio  = 'ROJO' | 'AMARILLO' | 'VERDE'

export interface Concurso {
  id:                string
  nombre:            string
  procesoSeleccion:  string | null
  numeroInscripcion: string | null
  numeroEmpleo:      string | null
  nivelCargo:        string | null
  denominacion:      string | null
  fechaExamen:       string | null
  diasRestantes:     number
  activo:            boolean
  createdAt:         string
}

export interface Item {
  id:                   string
  concursoId:           string
  nombre:               string
  descripcion:          string | null
  tipoPrueba:           TipoPrueba
  nivelCompetencia:     number | null
  pesoPorcentual:       number
  preguntasSolicitadas: number
  dominioPrevio:        DominioPrevio
  umbralBrecha:         number
  orden:                number
  createdAt:            string
}

export interface ApiError {
  status:   number
  error:    string
  mensaje:  string
  detalles: string[]
}
