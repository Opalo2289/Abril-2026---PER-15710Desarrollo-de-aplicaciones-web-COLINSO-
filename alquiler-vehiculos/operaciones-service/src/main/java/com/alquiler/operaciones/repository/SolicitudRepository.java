package com.alquiler.operaciones.repository;

import com.alquiler.operaciones.domain.EstadoSolicitud;
import com.alquiler.operaciones.domain.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByEstado(EstadoSolicitud estado);

    void deleteByVehiculoId(Long vehiculoId);

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM Solicitud s
            WHERE s.vehiculoId = :vehiculoId
              AND s.estado IN :activos
              AND s.fechaInicio <= :hasta
              AND s.fechaFin >= :desde
            """)
    boolean existsSolapamientoActivo(
            @Param("vehiculoId") Long vehiculoId,
            @Param("activos") Collection<EstadoSolicitud> activos,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
