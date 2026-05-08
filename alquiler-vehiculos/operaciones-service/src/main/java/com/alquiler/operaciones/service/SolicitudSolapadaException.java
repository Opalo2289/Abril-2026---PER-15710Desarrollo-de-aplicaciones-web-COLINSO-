package com.alquiler.operaciones.service;

public class SolicitudSolapadaException extends RuntimeException {

    public SolicitudSolapadaException() {
        super("Ya existe una solicitud pendiente o confirmada para ese vehículo en el rango de fechas indicado");
    }
}
