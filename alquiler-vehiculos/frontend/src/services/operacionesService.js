import httpClient, { parseApiError } from './httpClient.js'
import { syncEstadoTrasCancelar, syncEstadoTrasConfirmar } from './vehiculosService.js'

export { parseApiError }

export async function listarSolicitudes(filtros = {}, config = {}) {
  const params = {}
  if (filtros.estado) params.estado = filtros.estado
  const { data } = await httpClient.get('/operaciones/solicitudes', {
    params,
    ...config,
  })
  return data
}

export async function obtenerSolicitud(id, config = {}) {
  const { data } = await httpClient.get(`/operaciones/solicitudes/${id}`, config)
  return data
}

export async function crearSolicitud(payload) {
  const { data } = await httpClient.post('/operaciones/solicitudes', payload)
  return data
}

export async function confirmarSolicitud(id, opciones = {}) {
  const { data } = await httpClient.post(`/operaciones/solicitudes/${id}/confirmar`)
  const estadoVehiculo = opciones.estadoVehiculo || 'RESERVADO'
  await syncEstadoTrasConfirmar(data.vehiculoId, estadoVehiculo)
  return data
}

export async function cancelarSolicitud(id) {
  const { data } = await httpClient.post(`/operaciones/solicitudes/${id}/cancelar`)
  await syncEstadoTrasCancelar(data.vehiculoId)
  return data
}
