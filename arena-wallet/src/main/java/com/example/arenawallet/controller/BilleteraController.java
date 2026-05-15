package com.example.arenawallet.controller;

import com.example.arenawallet.model.Billetera;
import com.example.arenawallet.service.BilleteraService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/billeteras")
@Validated
public class BilleteraController {

    private final BilleteraService billeteraService;

    public BilleteraController(BilleteraService billeteraService) {
        this.billeteraService = billeteraService;
    }

    // GET /api/v1/billeteras
    @GetMapping
    public ResponseEntity<List<Billetera>> listar() {
        return ResponseEntity.ok(billeteraService.listarTodas());
    }

    // GET /api/v1/billeteras/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Billetera> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(billeteraService.obtenerPorId(id));
    }

    // POST /api/v1/billeteras
    @PostMapping
    public ResponseEntity<Billetera> crear(@Valid @RequestBody Billetera billetera) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billeteraService.crear(billetera));
    }

    // PUT /api/v1/billeteras/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Billetera> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Billetera billetera) {
        return ResponseEntity.ok(billeteraService.actualizar(id, billetera));
    }

    // PATCH /api/v1/billeteras/{id}/recargas
    @PatchMapping("/{id}/recargas")
    public ResponseEntity<Billetera> recargarSaldo(
            @PathVariable Long id,
            @RequestBody Map<String, Double> body) {

        Double monto = body.get("monto");
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a recargar debe ser positivo y obligatorio");
        }

        // Le pasamos el monto al service
        return ResponseEntity.ok(billeteraService.recargarSaldo(id, monto));
    }

    // --- REQUERIMIENTO LECCIÓN 13: ENDPOINT DE AUDITORÍA ---
    // GET /api/v1/billeteras/{id}/historial
    @GetMapping("/{id}/historial")
    public ResponseEntity<?> obtenerHistorial(@PathVariable Long id) {
        // Este método devolverá la lista de movimientos/cambios de la billetera
        return ResponseEntity.ok(billeteraService.obtenerHistorial(id));
    }
    // -------------------------------------------------------

    // DELETE /api/v1/billeteras/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        billeteraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}