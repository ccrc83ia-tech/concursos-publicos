import axios from 'axios'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

// Agrega el token JWT en cada petición
client.interceptors.request.use((config) => {
  const raw = localStorage.getItem('dian_auth')
  if (raw) {
    const state = JSON.parse(raw)
    const token = state?.state?.token
    if (token) config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Redirige al login si el token expiró
client.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('dian_auth')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default client
