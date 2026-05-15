package com.example.arenainventory.service;

import com.example.arenainventory.exception.ProductoNotFoundException;
import com.example.arenainventory.model.Producto;
import com.example.arenainventory.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    // Aquí iría el NotificationClient si usaras Feign como el README 15

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarPorCategoria(String categoria) {
        try {
            // El uso de enum es excelente, previene errores de tipeo
            Producto.Categoria cat = Producto.Categoria.valueOf(categoria.toUpperCase());
            return productoRepository.findByCategoria(cat);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Categoría inválida. Use: CONSOLA, PERIFERICO o JUEGO");
        }
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(
                        "Producto con ID " + id + " no encontrado"));
    }

    public Producto crear(Producto producto) {
        // Regla: No permitir crear productos con stock negativo inicial
        if (producto.getStock() != null && producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto nuevo) {
        Producto existente = obtenerPorId(id);

        // Actualizamos campos manteniendo el ID original
        existente.setNombre(nuevo.getNombre());
        existente.setCategoria(nuevo.getCategoria());
        existente.setPrecioAlquiler(nuevo.getPrecioAlquiler());
        existente.setStock(nuevo.getStock());

        return productoRepository.save(existente);
    }

    public Producto actualizarStock(Long id, Integer cantidad) {
        if (cantidad == null || cantidad == 0) {
            throw new IllegalArgumentException("La cantidad debe ser distinta de cero");
        }

        Producto producto = obtenerPorId(id);
        int nuevoStock = producto.getStock() + cantidad;

        if (nuevoStock < 0) {
            throw new RuntimeException(
                    "Operación cancelada: Stock insuficiente. Disponible: " + producto.getStock());
        }

        producto.setStock(nuevoStock);

        // El README 14/15 sugiere que aquí se debería notificar el cambio
        // System.out.println("Notificando cambio de stock para producto: " + id);

        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        // Verificamos existencia antes de borrar para lanzar el 404 correcto
        if (!productoRepository.existsById(id)) {
            throw new ProductoNotFoundException("No se puede eliminar: ID " + id + " no existe");
        }
        productoRepository.deleteById(id);
    }
}