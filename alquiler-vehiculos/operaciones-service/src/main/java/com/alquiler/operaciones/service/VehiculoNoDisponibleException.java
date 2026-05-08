package com.alquiler.operaciones.service;

public class VehiculoNoDisponibleException extends RuntimeException {

    public VehiculoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
