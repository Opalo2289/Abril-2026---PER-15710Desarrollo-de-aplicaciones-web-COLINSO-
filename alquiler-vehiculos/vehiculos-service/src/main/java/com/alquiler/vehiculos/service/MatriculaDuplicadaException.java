package com.alquiler.vehiculos.service;

public class MatriculaDuplicadaException extends RuntimeException {

    public MatriculaDuplicadaException(String matricula) {
        super("Ya existe un vehículo con matrícula " + matricula);
    }
}
