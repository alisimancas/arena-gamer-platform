package com.example.arenareservas.service;

import com.example.arenareservas.exception.ReservaNotFoundException;
import com.example.arenareservas.model.Reserva;
import com.example.arenareservas.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    // Lógica de negocio: filtrar reservas por estado
    public List<Reserva> listarPorEstado(String estado) {
        return reservaRepository.findAll().stream()
                .filter(r -> r.getEstado().equalsIgnoreCase(estado))
                .collect(Collectors.toList());
    }

    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(
                        "Reserva con ID " + id + " no encontrada"));
    }

    public Reserva crear(Reserva reserva) {
        validarConflictoHorario(reserva.getEstacionId(),
                reserva.getFecha().toString(),
                reserva.getBloqueHorario(),
                -1L); // -1L indica que no hay ID a excluir (es nueva)
        reserva.setEstado(reserva.getEstado().toUpperCase());
        return reservaRepository.save(reserva);
    }

    public Reserva actualizar(Long id, Reserva reservaActualizada) {
        // Verifica que la reserva exista antes de actualizar
        Reserva existente = obtenerPorId(id);

        // Valida conflicto excluyendo la reserva actual (para permitir actualizar su propio horario)
        validarConflictoHorario(
                reservaActualizada.getEstacionId(),
                reservaActualizada.getFecha().toString(),
                reservaActualizada.getBloqueHorario(),
                id);

        reservaActualizada.setId(existente.getId());
        reservaActualizada.setEstado(reservaActualizada.getEstado().toUpperCase());
        return reservaRepository.save(reservaActualizada);
    }

    public void eliminar(Long id) {
        if (!reservaRepository.deleteById(id)) {
            throw new ReservaNotFoundException(
                    "Reserva con ID " + id + " no encontrada");
        }
    }

    private void validarConflictoHorario(Long estacionId, String fecha,
                                         String bloqueHorario, Long idExcluido) {
        if (reservaRepository.existeConflicto(estacionId, fecha, bloqueHorario, idExcluido)) {
            throw new IllegalArgumentException(
                    "Ya existe una reserva para la estación " + estacionId +
                            " en la fecha " + fecha +
                            " durante el bloque " + bloqueHorario);
        }
    }
}