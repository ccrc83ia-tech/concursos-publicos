export interface Usuario {
  id:     string
  nombre: string
  email:  string
  rol:    'ESTUDIANTE' | 'ADMIN'
  token:  string
}

export type TipoPrueba    = 'FUNCIONAL' | 'COMPORTAMENTAL' | 'INTEGRIDAD'
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
  /** Dominio REAL del tema derivado del último simulacro (0 si nunca se practicó). */
  dominioPorcentual:    number
  /** Semáforo del último desempeño; null si el tema aún no se ha practicado. */
  nivelDominio:         NivelDominio | null
  /** true si el usuario ya hizo al menos un simulacro con este tema. */
  practicado:           boolean
  umbralBrecha:         number
  orden:                number
  createdAt:            string
}

export type EstadoGeneracion = 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADO' | 'ERROR'
export type OpcionRespuesta  = 'A' | 'B' | 'C' | 'D'
export type NivelDificultad  = 'BASICO' | 'INTERMEDIO' | 'AVANZADO'
export type ProcesoCognitivo = 'EVOCACION' | 'COMPRENSION' | 'APLICACION'

/** Estado de calidad de una pregunta dentro del banco (doble IA). */
export type EstadoCalidad = 'PENDIENTE_VALIDACION' | 'VALIDADA' | 'RECHAZADA' | 'AGOTADA'
/** Modelo que redactó la pregunta: PREMIUM (Gemini) o PROVISIONAL (Groq). */
export type OrigenModelo  = 'PREMIUM' | 'PROVISIONAL'

/** Foto del banco de preguntas de un ítem (GET /items/{id}/banco/estado). */
export interface BancoEstado {
  total:          number
  validadas:      number
  pendientes:     number
  rechazadas:     number
  agotadas:       number
  provisionales:  number
  premium:        number
}

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
  procesoCognitivo:          ProcesoCognitivo
  generadaPorIa:             boolean
  estadoCalidad:             EstadoCalidad | null
  origenModelo:              OrigenModelo | null
}

export type TipoSimulacro   = 'POR_ITEM' | 'COMPLETO' | 'BRECHAS'
export type EstadoSimulacro = 'EN_CURSO' | 'COMPLETADO' | 'ABANDONADO'

/** Motivo por el que un aspirante reporta una pregunta como problemática. */
export type TipoFeedback =
  | 'CLAVE_INCORRECTA'
  | 'ENUNCIADO_CONFUSO'
  | 'VARIAS_CORRECTAS'
  | 'FUERA_DE_TEMA'
  | 'ERROR_REDACCION'
  | 'OTRO'

/** Resultado de reportar una pregunta. */
export interface FeedbackResultado {
  reportesAcumulados: number
  preguntaRetirada:   boolean
}

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
