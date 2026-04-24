package com.alquiler.vehiculos.web.dto;

import com.alquiler.vehiculos.domain.EstadoVehiculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Datos para crear o actualizar un vehículo")
public class VehiculoRequest {

    @NotBlank
    @Size(max = 80)
    private String marca;

    @NotBlank
    @Size(max = 80)
    private String modelo;

    @NotBlank
    @Size(max = 20)
    private String matricula;

    @NotNull
    private EstadoVehiculo estado;

    @Positive
    @Schema(description = "Precio diario de alquiler (opcional)", example = "45.50")
    private BigDecimal precioPorDia;

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public BigDecimal getPrecioPorDia() {
        return precioPorDia;
    }

    public void setPrecioPorDia(BigDecimal precioPorDia) {
        this.precioPorDia = precioPorDia;
    }
}
