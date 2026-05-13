package com.example.arenareservas.repository;

import com.example.arenareservas.model.Reserva;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReservaRepository {

    private final Map<Long, Reserva> almacenamiento = new HashMap<>();
    private Long contadorId = 1L;

    public List<Reserva> findAll() {
        return new ArrayList<>(almacenamiento.values());
    }

    public Optional<Reserva> findById(Long id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }

    public Reserva save(Reserva reserva) {
        if (reserva.getId() == null) {
            reserva.setId(contadorId++);
        }
        almacenamiento.put(reserva.getId(), reserva);
        return reserva;
    }

    public boolean deleteById(Long id) {
        if (!almacenamiento.containsKey(id)) {
            return false;
        }
        almacenamiento.remove(id);
        return true;
    }

    public boolean existsById(Long id) {
        return almacenamiento.containsKey(id);
    }

    // Lógica de negocio: verificar conflicto de horario en la misma estación
    public boolean existeConflicto(Long estacionId, String fecha, String bloqueHorario, Long idExcluido) {
        return almacenamiento.values().stream()
                .filter(r -> !r.getId().equals(idExcluido))
                .filter(r -> r.getEstacionId().equals(estacionId))
                .filter(r -> r.getFecha().toString().equals(fecha))
                .filter(r -> r.getBloqueHorario().equals(bloqueHorario))
                .filter(r -> !r.getEstado().equalsIgnoreCase("CANCELADA"))
                .anyMatch(r -> true);
    }
}