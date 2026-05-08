package com.alquiler.operaciones.web;

import com.alquiler.operaciones.domain.EstadoSolicitud;
import com.alquiler.operaciones.service.SolicitudService;
import com.alquiler.operaciones.web.dto.SolicitudRequest;
import com.alquiler.operaciones.web.dto.SolicitudResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operaciones/solicitudes")
@Tag(name = "Solicitudes", description = "Registro y ciclo de vida de solicitudes de alquiler (BD operaciones)")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    @Operation(summary = "Listar solicitudes", description = "Sin filtro devuelve todas; con estado filtra por PENDIENTE, CONFIRMADA o CANCELADA.")
    public List<SolicitudResponse> listar(
            @Parameter(description = "Filtrar por estado")
            @RequestParam(required = false) EstadoSolicitud estado) {
        return solicitudService.listar(estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener solicitud por id")
    public SolicitudResponse obtener(@PathVariable Long id) {
        return solicitudService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar solicitud", description = "Crea en PENDIENTE. Valida solapes con otras solicitudes activas del mismo vehículo. "
            + "La disponibilidad del catálogo de vehículos es simulada en el lote 4 (stub).")
    public SolicitudResponse registrar(@Valid @RequestBody SolicitudRequest request) {
        return solicitudService.registrar(request);
    }

    @PostMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar solicitud", description = "Pasa de PENDIENTE a CONFIRMADA.")
    public SolicitudResponse confirmar(@PathVariable Long id) {
        return solicitudService.confirmar(id);
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar solicitud", description = "Pasa de PENDIENTE a CANCELADA.")
    public SolicitudResponse cancelar(@PathVariable Long id) {
        return solicitudService.cancelar(id);
    }
}
