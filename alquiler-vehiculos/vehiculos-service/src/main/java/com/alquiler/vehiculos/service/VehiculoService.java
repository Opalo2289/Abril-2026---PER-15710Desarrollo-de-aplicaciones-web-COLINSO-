package com.alquiler.vehiculos.service;

import com.alquiler.vehiculos.client.SolicitudesOperaciones;
import com.alquiler.vehiculos.domain.EstadoVehiculo;
import com.alquiler.vehiculos.domain.Vehiculo;
import com.alquiler.vehiculos.repository.VehiculoRepository;
import com.alquiler.vehiculos.repository.VehiculoSpecifications;
import com.alquiler.vehiculos.web.dto.VehiculoRequest;
import com.alquiler.vehiculos.web.dto.VehiculoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final SolicitudesOperaciones solicitudesOperaciones;

    public VehiculoService(VehiculoRepository vehiculoRepository, SolicitudesOperaciones solicitudesOperaciones) {
        this.vehiculoRepository = vehiculoRepository;
        this.solicitudesOperaciones = solicitudesOperaciones;
    }

    @Transactional(readOnly = true)
    public List<VehiculoResponse> buscar(String marca, String modelo, EstadoVehiculo estado) {
        var spec = VehiculoSpecifications.conFiltrosOpcionales(marca, modelo, estado);
        return vehiculoRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VehiculoResponse obtenerPorId(Long id) {
        return vehiculoRepository.findById(id).map(this::toResponse).orElseThrow(() -> new VehiculoNotFoundException(id));
    }

    @Transactional
    public VehiculoResponse crear(VehiculoRequest request) {
        asegurarMatriculaLibre(request.getMatricula(), null);
        Vehiculo v = new Vehiculo();
        aplicar(v, request);
        return toResponse(vehiculoRepository.save(v));
    }

    @Transactional
    public VehiculoResponse actualizar(Long id, VehiculoRequest request) {
        Vehiculo v = vehiculoRepository.findById(id).orElseThrow(() -> new VehiculoNotFoundException(id));
        asegurarMatriculaLibre(request.getMatricula(), id);
        aplicar(v, request);
        return toResponse(vehiculoRepository.save(v));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new VehiculoNotFoundException(id);
        }
        solicitudesOperaciones.eliminarPorVehiculoId(id);
        vehiculoRepository.deleteById(id);
    }

    private void asegurarMatriculaLibre(String matricula, Long idExcluido) {
        if (matricula == null || matricula.isBlank()) {
            return;
        }
        if (idExcluido == null) {
            vehiculoRepository.findByMatriculaIgnoreCase(matricula.trim()).ifPresent(v -> {
                throw new MatriculaDuplicadaException(matricula.trim());
            });
        } else if (vehiculoRepository.existsByMatriculaIgnoreCaseAndIdNot(matricula.trim(), idExcluido)) {
            throw new MatriculaDuplicadaException(matricula.trim());
        }
    }

    private void aplicar(Vehiculo destino, VehiculoRequest origen) {
        destino.setMarca(origen.getMarca().trim());
        destino.setModelo(origen.getModelo().trim());
        destino.setMatricula(origen.getMatricula().trim());
        destino.setEstado(origen.getEstado());
        destino.setPrecioPorDia(origen.getPrecioPorDia());
    }

    private VehiculoResponse toResponse(Vehiculo v) {
        return new VehiculoResponse(v.getId(), v.getMarca(), v.getModelo(), v.getMatricula(), v.getEstado(), v.getPrecioPorDia());
    }
}
