package com.example.arenareservas.service;

import com.example.arenareservas.client.InventoryClient;
import com.example.arenareservas.exception.ReservaNotFoundException;
import com.example.arenareservas.model.Reserva;
import com.example.arenareservas.model.ReservaHistory;
import com.example.arenareservas.repository.ReservaHistoryRepository;
import com.example.arenareservas.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ReservaHistoryRepository historialRepository;
    private final InventoryClient inventoryClient;

    // Constructor con inyección de todas las dependencias (Repositorios y Cliente Feign)
    public ReservaService(ReservaRepository reservaRepository,
                          ReservaHistoryRepository historialRepository,
                          InventoryClient inventoryClient) {
        this.reservaRepository = reservaRepository;
        this.historialRepository = historialRepository;
        this.inventoryClient = inventoryClient;
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

    @Transactional
    public Reserva crear(Reserva reserva) {
        // Validación de fecha (no permitir fechas pasadas)
        if (reserva.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden crear reservas en fechas pasadas");
        }

        validarConflictoHorario(
                reserva.getEstacionId(),
                reserva.getFecha(),
                reserva.getBloqueHorario(),
                -1L);

        reserva.setEstado("NUEVA");
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva actualizar(Long id, Reserva reservaActualizada) {
        Reserva existente = obtenerPorId(id);

        validarConflictoHorario(
                reservaActualizada.getEstacionId(),
                reservaActualizada.getFecha(),
                reservaActualizada.getBloqueHorario(),
                id);

        existente.setFecha(reservaActualizada.getFecha());
        // Corrección del error de escritura:
        existente.setBloqueHorario(reservaActualizada.getBloqueHorario());
        existente.setEstacionId(reservaActualizada.getEstacionId());

        return reservaRepository.save(existente);
    }

    // --- LÓGICA DE AUDITORÍA (L13) Y COMUNICACIÓN (L15) ---
    @Transactional
    public Reserva cambiarEstado(Long id, String nuevoEstado, String comentario) {
        Reserva reserva = obtenerPorId(id);
        String estadoAnterior = reserva.getEstado();

        // 1. Guardar el nuevo estado localmente
        reserva.setEstado(nuevoEstado.toUpperCase());
        Reserva guardada = reservaRepository.save(reserva);

        // 2. Registrar el cambio en el historial (Auditoría Lección 13)
        ReservaHistory historial = new ReservaHistory();
        historial.setReserva(guardada);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(nuevoEstado.toUpperCase());
        historial.setFechaCambio(LocalDateTime.now());
        historial.setComentario(comentario);
        historialRepository.save(historial);

        // 3. Comunicación Inter-Service (Lección 15)
        // Solo enviamos la orden a Inventory si la reserva se CONFIRMA
        if ("CONFIRMADA".equalsIgnoreCase(nuevoEstado)) {
            try {
                // Preparamos el mapa para el cuerpo de la petición PATCH
                Map<String, Integer> body = Map.of("cantidad", -1);

                // Usamos el cliente Feign para llamar al MS de Inventory
                // Se asume que reserva.getEstacionId() corresponde al ID del producto
                inventoryClient.actualizarStock(reserva.getEstacionId(), body);

                System.out.println("Comunicación L15: Stock descontado exitosamente en Inventory.");
            } catch (Exception e) {
                // Registro de error si el microservicio externo no responde
                System.err.println("Error al comunicar con Inventory MS: " + e.getMessage());
            }
        }

        return guardada;
    }

    public List<ReservaHistory> obtenerHistorial(Long id) {
        if (!reservaRepository.existsById(id)) {
            throw new ReservaNotFoundException("Reserva con ID " + id + " no encontrada");
        }
        return historialRepository.findByReservaIdOrderByFechaCambioDesc(id);
    }

    public void eliminar(Long id) {
        if (!reservaRepository.existsById(id)) {
            throw new ReservaNotFoundException("Reserva con ID " + id + " no encontrada");
        }
        reservaRepository.deleteById(id);
    }

    private void validarConflictoHorario(Long estacionId, LocalDate fecha,
                                         String bloqueHorario, Long idExcluido) {
        boolean conflicto = reservaRepository
                .existsByEstacionIdAndFechaAndBloqueHorarioAndEstadoNotAndIdNot(
                        estacionId, fecha, bloqueHorario, "CANCELADA", idExcluido);
        if (conflicto) {
            throw new IllegalArgumentException(
                    "Ya existe una reserva activa para la estación " + estacionId +
                            " en la fecha " + fecha +
                            " durante el bloque " + bloqueHorario);
        }
    }
}