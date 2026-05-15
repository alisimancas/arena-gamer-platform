package com.example.arenareservas.controller;

import com.example.arenareservas.model.Reserva;
import com.example.arenareservas.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public ResponseEntity<List<Reserva>> listar(
            @RequestParam(required = false) String estado) {
        List<Reserva> resultado = (estado != null && !estado.isBlank())
                ? reservaService.listarPorEstado(estado)
                : reservaService.listarTodas();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    // Lección 13: Consultar historial de cambios de la reserva
    @GetMapping("/{id}/history")
    public ResponseEntity<List<?>> obtenerHistorial(@PathVariable Long id) {
        // Suponiendo que implementaremos la entidad ReservaHistory
        return ResponseEntity.ok(reservaService.obtenerHistorial(id));
    }

    @PostMapping
    public ResponseEntity<Reserva> crear(@Valid @RequestBody Reserva reserva) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crear(reserva));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reserva> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Reserva reserva) {
        return ResponseEntity.ok(reservaService.actualizar(id, reserva));
    }

    // Requerimiento Lección 12/13: Cambio de estado (PATCH)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Reserva> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String nuevoEstado = body.get("estado");
        String comentario = body.get("comentario"); // Opcional para el historial

        return ResponseEntity.ok(reservaService.cambiarEstado(id, nuevoEstado, comentario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}