package com.example.arenawallet.repository;

import com.example.arenawallet.model.Billetera;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class BilleteraRepository {

    private final Map<Long, Billetera> almacenamiento = new HashMap<>();
    private Long contadorId = 1L;

    public List<Billetera> findAll() {
        return new ArrayList<>(almacenamiento.values());
    }

    public Optional<Billetera> findById(Long id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }

    public Billetera save(Billetera billetera) {
        if (billetera.getId() == null) {
            billetera.setId(contadorId++);
        }
        almacenamiento.put(billetera.getId(), billetera);
        return billetera;
    }

    public boolean deleteById(Long id) {
        if (!almacenamiento.containsKey(id)) {
            return false;
        }
        almacenamiento.remove(id);
        return true;
    }

    public boolean existsByUsuarioId(Long idUsuario) {
        return almacenamiento.values().stream()
                .anyMatch(b -> b.getIdUsuario().equals(idUsuario));
    }
}