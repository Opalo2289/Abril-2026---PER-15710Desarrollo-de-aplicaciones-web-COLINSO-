package com.alquiler.operaciones.service;

import com.alquiler.operaciones.client.DisponibilidadVehiculos;
import com.alquiler.operaciones.domain.EstadoSolicitud;
import com.alquiler.operaciones.domain.Solicitud;
import com.alquiler.operaciones.repository.SolicitudRepository;
import com.alquiler.operaciones.web.dto.SolicitudRequest;
import com.alquiler.operaciones.web.dto.SolicitudResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SolicitudService {

    private static final List<EstadoSolicitud> ACTIVOS = List.of(EstadoSolicitud.PENDIENTE, EstadoSolicitud.CONFIRMADA);

    private final SolicitudRepository solicitudRepository;
    private final DisponibilidadVehiculos disponibilidadVehiculos;

    public SolicitudService(SolicitudRepository solicitudRepository, DisponibilidadVehiculos disponibilidadVehiculos) {
        this.solicitudRepository = solicitudRepository;
        this.disponibilidadVehiculos = disponibilidadVehiculos;
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar(EstadoSolicitud estado) {
        if (estado == null) {
            return solicitudRepository.findAll().stream().map(this::toResponse).toList();
        }
        return solicitudRepository.findByEstado(estado).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtener(Long id) {
        return toResponse(solicitudRepository.findById(id).orElseThrow(() -> new SolicitudNotFoundException(id)));
    }

    @Transactional
    public SolicitudResponse registrar(SolicitudRequest request) {
        if (request.fechaInicio().isAfter(request.fechaFin())) {
            throw new FechasSolicitudInvalidasException();
        }
        if (!disponibilidadVehiculos.estaDisponibleParaAlquiler(
                request.vehiculoId(), request.fechaInicio(), request.fechaFin())) {
            throw new VehiculoNoDisponibleException(
                    "El vehículo no está disponible para alquiler en el rango de fechas indicado");
        }
        if (solicitudRepository.existsSolapamientoActivo(
                request.vehiculoId(), ACTIVOS, request.fechaInicio(), request.fechaFin())) {
            throw new SolicitudSolapadaException();
        }
        Solicitud s = new Solicitud();
        s.setVehiculoId(request.vehiculoId());
        s.setFechaInicio(request.fechaInicio());
        s.setFechaFin(request.fechaFin());
        s.setEstado(EstadoSolicitud.PENDIENTE);
        return toResponse(solicitudRepository.save(s));
    }

    @Transactional
    public SolicitudResponse confirmar(Long id) {
        Solicitud s = solicitudRepository.findById(id).orElseThrow(() -> new SolicitudNotFoundException(id));
        if (s.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new TransicionEstadoInvalidaException(
                    "Solo se pueden confirmar solicitudes en estado PENDIENTE (estado actual: " + s.getEstado() + ")");
        }
        s.setEstado(EstadoSolicitud.CONFIRMADA);
        return toResponse(solicitudRepository.save(s));
    }

    @Transactional
    public SolicitudResponse cancelar(Long id) {
        Solicitud s = solicitudRepository.findById(id).orElseThrow(() -> new SolicitudNotFoundException(id));
        if (s.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new TransicionEstadoInvalidaException(
                    "Solo se pueden cancelar solicitudes en estado PENDIENTE (estado actual: " + s.getEstado() + ")");
        }
        s.setEstado(EstadoSolicitud.CANCELADA);
        return toResponse(solicitudRepository.save(s));
    }

    private SolicitudResponse toResponse(Solicitud s) {
        return new SolicitudResponse(s.getId(), s.getVehiculoId(), s.getFechaInicio(), s.getFechaFin(), s.getEstado());
    }
}
