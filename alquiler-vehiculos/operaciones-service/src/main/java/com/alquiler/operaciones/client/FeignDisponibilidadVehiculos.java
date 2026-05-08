package com.alquiler.operaciones.client;

import com.alquiler.operaciones.service.VehiculoNoDisponibleException;
import feign.FeignException;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Implementación de {@link DisponibilidadVehiculos} que consulta {@code vehiculos-service}
 * vía Feign (lote 5: URL fija; lote 6: descubrimiento Eureka).
 *
 * <p>Considera disponible <strong>únicamente</strong> el estado {@code DISPONIBLE}
 * del catálogo. Cualquier otro estado ({@code ALQUILADO}, {@code RESERVADO},
 * {@code MANTENIMIENTO}) o un 404 (vehículo inexistente) se traduce a
 * {@link VehiculoNoDisponibleException} y el controlador devuelve 409.</p>
 *
 * <p>Nota: el catálogo no expone aún disponibilidad por rango de fechas;
 * la detección de solapes de fechas entre solicitudes activas la realiza
 * {@code existsSolapamientoActivo} en el propio módulo de operaciones.</p>
 */
@Component
@Primary
@Profile("!test")
public class FeignDisponibilidadVehiculos implements DisponibilidadVehiculos {

    private final VehiculoCatalogoClient vehiculoCatalogoClient;

    public FeignDisponibilidadVehiculos(VehiculoCatalogoClient vehiculoCatalogoClient) {
        this.vehiculoCatalogoClient = vehiculoCatalogoClient;
    }

    @Override
    public boolean estaDisponibleParaAlquiler(Long vehiculoId, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            var respuesta = vehiculoCatalogoClient.obtener(vehiculoId);
            if (!"DISPONIBLE".equalsIgnoreCase(respuesta.estado())) {
                throw new VehiculoNoDisponibleException(
                        "El vehículo " + vehiculoId + " no está disponible (estado actual: " + respuesta.estado() + ")");
            }
            return true;
        } catch (FeignException.NotFound e) {
            throw new VehiculoNoDisponibleException(
                    "El vehículo " + vehiculoId + " no existe en el catálogo");
        }
    }
}
