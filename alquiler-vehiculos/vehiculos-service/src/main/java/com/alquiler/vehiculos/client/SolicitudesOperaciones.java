package com.alquiler.vehiculos.client;

/**
 * Limpieza de solicitudes en operaciones-service cuando se elimina un vehículo del catálogo.
 */
public interface SolicitudesOperaciones {

    void eliminarPorVehiculoId(Long vehiculoId);
}
