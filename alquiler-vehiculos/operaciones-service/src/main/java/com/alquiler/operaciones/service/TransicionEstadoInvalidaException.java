package com.alquiler.operaciones.service;

public class TransicionEstadoInvalidaException extends RuntimeException {

    public TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje);
    }
}
