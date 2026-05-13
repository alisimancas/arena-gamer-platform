package com.example.arenareservas.controller;

import com.example.arenareservas.model.Reserva;
import com.example.arenareservas.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // GET /api/v1/reservas  →  Lista todas las reservas
    // GET /api/v1/reservas?estado=CONFIRMADA  →  Filtra por estado
    @GetMapping
    public ResponseEntity<List<Reserva>> listar(
            @RequestParam(required = false) String estado) {

        List<Reserva> resultado = (estado != null && !estado.isBlank())
                ? reservaService.listarPorEstado(estado)
                : reservaService.listarTodas();

        return ResponseEntity.ok(resultado);
    }

    // GET /api/v1/reservas/{id}  →  Obtiene una reserva por ID
    @GetMapping("/{id}")
    public ResponseEntity<Reserva> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    // POST /api/v1/reservas  →  Crea una nueva reserva
    @PostMapping
    public ResponseEntity<Reserva> crear(@Valid @RequestBody Reserva reserva) {
        Reserva nueva = reservaService.crear(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // PUT /api/v1/reservas/{id}  →  Actualiza completamente una reserva existente
    @PutMapping("/{id}")
    public ResponseEntity<Reserva> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Reserva reserva) {

        return ResponseEntity.ok(reservaService.actualizar(id, reserva));
    }

    // DELETE /api/v1/reservas/{id}  →  Elimina una reserva
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}