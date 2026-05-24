import axios from 'axios'

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

export function parseApiError(error) {
  if (axios.isCancel(error)) {
    return null
  }

  if (!error.response) {
    return 'No se pudo conectar con el servidor. Comprueba que el API Gateway esté activo en el puerto 8080.'
  }

  const { data, status } = error.response

  if (data && typeof data === 'object') {
    if (data.message) return data.message
    if (data.detail) return data.detail
    if (Array.isArray(data.errors) && data.errors.length > 0) {
      return data.errors.map((e) => e.defaultMessage || e.message).join('. ')
    }
  }

  if (typeof data === 'string' && data.trim()) {
    return data
  }

  return `Error del servidor (${status})`
}

export default httpClient
