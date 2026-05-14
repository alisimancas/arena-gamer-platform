package com.example.arenainventory.repository;

import com.example.arenainventory.model.Producto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductoRepository {

    private final Map<Long, Producto> almacenamiento = new HashMap<>();
    private Long contadorId = 1L;

    public List<Producto> findAll() {
        return new ArrayList<>(almacenamiento.values());
    }

    public Optional<Producto> findById(Long id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }

    public Producto save(Producto producto) {
        if (producto.getId() == null) {
            producto.setId(contadorId++);
        }
        almacenamiento.put(producto.getId(), producto);
        return producto;
    }

    public boolean deleteById(Long id) {
        if (!almacenamiento.containsKey(id)) {
            return false;
        }
        almacenamiento.remove(id);
        return true;
    }

    // Lógica de negocio: filtrar por categoría
    public List<Producto> findByCategoria(Producto.Categoria categoria) {
        return almacenamiento.values().stream()
                .filter(p -> p.getCategoria().equals(categoria))
                .toList();
    }
}