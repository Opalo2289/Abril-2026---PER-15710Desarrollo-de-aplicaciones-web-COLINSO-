package com.alquiler.vehiculos.repository;

import com.alquiler.vehiculos.domain.EstadoVehiculo;
import com.alquiler.vehiculos.domain.Vehiculo;
import org.springframework.data.jpa.domain.Specification;

public final class VehiculoSpecifications {

    private VehiculoSpecifications() {
    }

    public static Specification<Vehiculo> conFiltrosOpcionales(String marca, String modelo, EstadoVehiculo estado) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (marca != null && !marca.isBlank()) {
                predicates = cb.and(predicates, cb.equal(cb.lower(root.get("marca")), marca.trim().toLowerCase()));
            }
            if (modelo != null && !modelo.isBlank()) {
                predicates = cb.and(predicates, cb.equal(cb.lower(root.get("modelo")), modelo.trim().toLowerCase()));
            }
            if (estado != null) {
                predicates = cb.and(predicates, cb.equal(root.get("estado"), estado));
            }
            return predicates;
        };
    }
}
