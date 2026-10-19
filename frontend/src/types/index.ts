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

export type EstadoGeneracion = 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADO' | 'ERROR'
export type OpcionRespuesta  = 'A' | 'B' | 'C' | 'D'
export type NivelDificultad  = 'BASICO' | 'INTERMEDIO' | 'AVANZADO'

export interface GeneracionJob {
  id:                 string
  itemId:             string
  estado:             EstadoGeneracion
  preguntasGeneradas: number
  totalEsperadas:     number
  porcentaje:         number
  mensajeError:       string | null
}

export interface Pregunta {
  id:                        string
  itemId:                    string
  enunciado:                 string
  opcionA:                   string
  opcionB:                   string
  opcionC:                   string
  opcionD:                   string
  respuestaCorrecta:         OpcionRespuesta
  justificacionCorrecta:     string
  justificacionIncorrectas:  Record<string, string>
  referenciaNormativa:       string | null
  nivelDificultad:           NivelDificultad
  generadaPorIa:             boolean
}

export type TipoSimulacro   = 'POR_ITEM' | 'COMPLETO' | 'BRECHAS'
export type EstadoSimulacro = 'EN_CURSO' | 'COMPLETADO' | 'ABANDONADO'

export interface PreguntaSimulacro {
  simulacroPreguntaId:    string
  preguntaId:             string
  orden:                  number
  enunciado:              string
  opcionA:                string
  opcionB:                string
  opcionC:                string
  opcionD:                string
  respuestaDada:          OpcionRespuesta | null
  marcadaParaRevision:    boolean
  // Solo tras finalizar:
  respuestaCorrecta:      OpcionRespuesta | null
  esCorrecta:             boolean | null
  justificacionCorrecta:  string | null
  referenciaNormativa:    string | null
}

export interface Simulacro {
  id:                   string
  concursoId:           string
  tipo:                 TipoSimulacro
  estado:               EstadoSimulacro
  tiempoLimiteSegundos: number | null
  tiempoUsadoSegundos:  number | null
  puntajeFinal:         number | null
  preguntas:            PreguntaSimulacro[]
}

export interface Desempeno {
  itemId:              string
  simulacroId:         string
  preguntasCorrectas:  number
  totalPreguntas:      number
  puntajePorcentual:   number
  nivelDominio:        NivelDominio
}

export interface TemaFallido {
  tema:         string
  descripcion:  string | null
}

export interface ReferenciaNormativa {
  norma:     string
  articulo:  string | null
  url:       string | null
}

export interface Recomendacion {
  id:                     string
  itemId:                 string
  simulacroId:            string
  resumenEjecutivo:       string | null
  temasFallidos:          TemaFallido[]
  referenciasNormativas:  ReferenciaNormativa[]
  tiempoEstudioHoras:     number | null
}

export interface ApiError {
  status:   number
  error:    string
  mensaje:  string
  detalles: string[]
}
