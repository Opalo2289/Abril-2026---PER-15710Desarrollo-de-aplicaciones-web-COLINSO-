import httpClient, { parseApiError } from './httpClient.js'

export { parseApiError }

function buildParams(filtros = {}) {
  const params = {}
  if (filtros.marca?.trim()) params.marca = filtros.marca.trim()
  if (filtros.modelo?.trim()) params.modelo = filtros.modelo.trim()
  if (filtros.estado) params.estado = filtros.estado
  return params
}

export function vehiculoToPayload(vehiculo) {
  return {
    marca: vehiculo.marca,
    modelo: vehiculo.modelo,
    matricula: vehiculo.matricula,
    estado: vehiculo.estado,
    precioPorDia: vehiculo.precioPorDia,
  }
}

export async function listarVehiculos(filtros = {}, config = {}) {
  const { data } = await httpClient.get('/vehiculos', {
    params: buildParams(filtros),
    ...config,
  })
  return data
}

export async function obtenerVehiculo(id, config = {}) {
  const { data } = await httpClient.get(`/vehiculos/${id}`, config)
  return data
}

export async function crearVehiculo(payload) {
  const { data } = await httpClient.post('/vehiculos', payload)
  return data
}

export async function actualizarVehiculo(id, payload) {
  const { data } = await httpClient.put(`/vehiculos/${id}`, payload)
  return data
}

export async function eliminarVehiculo(id) {
  await httpClient.delete(`/vehiculos/${id}`)
}

export async function syncEstadoTrasConfirmar(vehiculoId, nuevoEstado = 'RESERVADO') {
  const vehiculo = await obtenerVehiculo(vehiculoId)
  return actualizarVehiculo(vehiculoId, {
    ...vehiculoToPayload(vehiculo),
    estado: nuevoEstado,
  })
}

export async function syncEstadoTrasCancelar(vehiculoId) {
  const vehiculo = await obtenerVehiculo(vehiculoId)
  if (vehiculo.estado === 'RESERVADO' || vehiculo.estado === 'ALQUILADO') {
    return actualizarVehiculo(vehiculoId, {
      ...vehiculoToPayload(vehiculo),
      estado: 'DISPONIBLE',
    })
  }
  return vehiculo
}
