package com.example.arenainventory.controller;

import com.example.arenainventory.model.Producto;
import com.example.arenainventory.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listar(
            @RequestParam(required = false) String categoria) {

        List<Producto> resultado = (categoria != null && !categoria.isBlank())
                ? productoService.listarPorCategoria(categoria)
                : productoService.listarTodos();

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        // El service lanzará una excepción personalizada si no existe,
        // la cual será capturada por el GlobalExceptionHandler
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoService.crear(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizar(id, producto));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Producto> actualizarStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {

        // Validación de seguridad para el body
        if (body == null || !body.containsKey("cantidad")) {
            return ResponseEntity.badRequest().build();
        }

        Integer cantidad = body.get("cantidad");
        return ResponseEntity.ok(productoService.actualizarStock(id, cantidad));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}