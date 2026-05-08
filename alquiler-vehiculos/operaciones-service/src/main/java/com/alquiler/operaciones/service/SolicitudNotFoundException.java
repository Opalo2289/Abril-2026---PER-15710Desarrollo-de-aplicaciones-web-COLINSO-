package com.alquiler.operaciones.service;

public class SolicitudNotFoundException extends RuntimeException {

    public SolicitudNotFoundException(Long id) {
        super("Solicitud no encontrada: " + id);
    }
}
