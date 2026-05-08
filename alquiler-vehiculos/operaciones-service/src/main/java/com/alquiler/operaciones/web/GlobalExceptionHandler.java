package com.alquiler.operaciones.web;

import com.alquiler.operaciones.service.FechasSolicitudInvalidasException;
import com.alquiler.operaciones.service.SolicitudNotFoundException;
import com.alquiler.operaciones.service.SolicitudSolapadaException;
import com.alquiler.operaciones.service.TransicionEstadoInvalidaException;
import com.alquiler.operaciones.service.VehiculoNoDisponibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SolicitudNotFoundException.class)
    public ProblemDetail noEncontrado(SolicitudNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Recurso no encontrado");
        pd.setType(URI.create("about:blank"));
        return pd;
    }

    @ExceptionHandler({
            VehiculoNoDisponibleException.class,
            SolicitudSolapadaException.class,
            TransicionEstadoInvalidaException.class
    })
    public ProblemDetail conflicto(RuntimeException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflicto de negocio");
        pd.setType(URI.create("about:blank"));
        return pd;
    }

    @ExceptionHandler(FechasSolicitudInvalidasException.class)
    public ProblemDetail fechasInvalidas(FechasSolicitudInvalidasException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Solicitud inválida");
        pd.setType(URI.create("about:blank"));
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Errores de validación");
        pd.setTitle("Solicitud inválida");
        pd.setType(URI.create("about:blank"));
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errores.put(fe.getField(), fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "inválido"));
        pd.setProperty("errors", errores);
        return pd;
    }
}
