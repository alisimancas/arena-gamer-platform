package com.example.arenainventory.service;

import com.example.arenainventory.exception.ProductoNotFoundException;
import com.example.arenainventory.model.Producto;
import com.example.arenainventory.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarPorCategoria(String categoria) {
        try {
            Producto.Categoria cat = Producto.Categoria.valueOf(categoria.toUpperCase());
            return productoRepository.findByCategoria(cat);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Categoría inválida. Las opciones son: CONSOLA, PERIFERICO, JUEGO");
        }
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(
                        "Producto con ID " + id + " no encontrado"));
    }

    public Producto crear(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto productoActualizado) {
        Producto existente = obtenerPorId(id);
        productoActualizado.setId(existente.getId());
        return productoRepository.save(productoActualizado);
    }

    public Producto actualizarStock(Long id, Integer cantidad) {
        if (cantidad == null) {
            throw new IllegalArgumentException("La cantidad no puede ser nula");
        }
        Producto producto = obtenerPorId(id);
        int nuevoStock = producto.getStock() + cantidad;

        if (nuevoStock < 0) {
            throw new IllegalArgumentException(
                    "Stock insuficiente. Stock actual: " + producto.getStock()
                            + ". No se pueden restar " + Math.abs(cantidad) + " unidades.");
        }

        producto.setStock(nuevoStock);
        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ProductoNotFoundException(
                    "Producto con ID " + id + " no encontrado");
        }
        productoRepository.deleteById(id);
    }
}