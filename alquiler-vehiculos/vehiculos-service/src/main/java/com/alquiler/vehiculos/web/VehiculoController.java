package com.alquiler.vehiculos.web;

import com.alquiler.vehiculos.domain.EstadoVehiculo;
import com.alquiler.vehiculos.service.VehiculoService;
import com.alquiler.vehiculos.web.dto.VehiculoRequest;
import com.alquiler.vehiculos.web.dto.VehiculoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@Tag(name = "Vehículos", description = "CRUD y búsquedas del catálogo")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    @Operation(summary = "Listar o buscar vehículos", description = "Sin filtros devuelve todo el catálogo; con filtros aplica marca, modelo y/o estado (AND).")
    public List<VehiculoResponse> listar(
            @Parameter(description = "Filtrar por marca (coincidencia exacta, sin distinguir mayúsculas)")
            @RequestParam(required = false) String marca,
            @Parameter(description = "Filtrar por modelo (coincidencia exacta, sin distinguir mayúsculas)")
            @RequestParam(required = false) String modelo,
            @Parameter(description = "Filtrar por estado")
            @RequestParam(required = false) EstadoVehiculo estado) {
        return vehiculoService.buscar(marca, modelo, estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener vehículo por id")
    public VehiculoResponse obtener(@PathVariable Long id) {
        return vehiculoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear vehículo")
    public VehiculoResponse crear(@Valid @RequestBody VehiculoRequest request) {
        return vehiculoService.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar vehículo")
    public VehiculoResponse actualizar(@PathVariable Long id, @Valid @RequestBody VehiculoRequest request) {
        return vehiculoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar vehículo")
    public void eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
    }
}
