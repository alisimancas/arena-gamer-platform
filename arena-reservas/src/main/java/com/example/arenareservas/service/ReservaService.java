package com.example.arenareservas.service;

import com.example.arenareservas.exception.ReservaNotFoundException;
import com.example.arenareservas.model.Reserva;
import com.example.arenareservas.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    public List<Reserva> listarPorEstado(String estado) {
        return reservaRepository.findByEstadoIgnoreCase(estado);
    }

    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(
                        "Reserva con ID " + id + " no encontrada"));
    }

    public Reserva crear(Reserva reserva) {
        validarConflictoHorario(
                reserva.getEstacionId(),
                reserva.getFecha(),
                reserva.getBloqueHorario(),
                -1L);
        reserva.setEstado(reserva.getEstado().toUpperCase());
        return reservaRepository.save(reserva);
    }

    public Reserva actualizar(Long id, Reserva reservaActualizada) {
        Reserva existente = obtenerPorId(id);

        validarConflictoHorario(
                reservaActualizada.getEstacionId(),
                reservaActualizada.getFecha(),
                reservaActualizada.getBloqueHorario(),
                id);

        reservaActualizada.setId(existente.getId());
        reservaActualizada.setEstado(reservaActualizada.getEstado().toUpperCase());
        return reservaRepository.save(reservaActualizada);
    }

    public void eliminar(Long id) {
        if (!reservaRepository.existsById(id)) {
            throw new ReservaNotFoundException(
                    "Reserva con ID " + id + " no encontrada");
        }
        reservaRepository.deleteById(id);
    }

    private void validarConflictoHorario(Long estacionId, java.time.LocalDate fecha,
                                         String bloqueHorario, Long idExcluido) {
        boolean conflicto = reservaRepository
                .existsByEstacionIdAndFechaAndBloqueHorarioAndEstadoNotAndIdNot(
                        estacionId, fecha, bloqueHorario, "CANCELADA", idExcluido);
        if (conflicto) {
            throw new IllegalArgumentException(
                    "Ya existe una reserva para la estación " + estacionId +
                            " en la fecha " + fecha +
                            " durante el bloque " + bloqueHorario);
        }
    }
}