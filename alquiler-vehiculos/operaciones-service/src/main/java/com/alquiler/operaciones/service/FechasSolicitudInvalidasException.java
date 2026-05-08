package com.alquiler.operaciones.service;

public class FechasSolicitudInvalidasException extends RuntimeException {

    public FechasSolicitudInvalidasException() {
        super("La fecha de inicio no puede ser posterior a la fecha de fin");
    }
}
