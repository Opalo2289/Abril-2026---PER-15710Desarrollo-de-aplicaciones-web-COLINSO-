package com.alquiler.vehiculos.service;

public class VehiculoNotFoundException extends RuntimeException {

    public VehiculoNotFoundException(Long id) {
        super("No existe vehículo con id " + id);
    }
}
