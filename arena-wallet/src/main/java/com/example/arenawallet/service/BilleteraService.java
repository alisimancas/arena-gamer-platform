package com.example.arenawallet.service;

import com.example.arenawallet.exception.BilleteraNotFoundException;
import com.example.arenawallet.model.Billetera;
import com.example.arenawallet.repository.BilleteraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BilleteraService {

    private final BilleteraRepository billeteraRepository;

    public BilleteraService(BilleteraRepository billeteraRepository) {
        this.billeteraRepository = billeteraRepository;
    }

    public List<Billetera> listarTodas() {
        return billeteraRepository.findAll();
    }

    public Billetera obtenerPorId(Long id) {
        return billeteraRepository.findById(id)
                .orElseThrow(() -> new BilleteraNotFoundException(
                        "Billetera con ID " + id + " no encontrada"));
    }

    public Billetera crear(Billetera billetera) {
        if (billeteraRepository.existsByUsuarioId(billetera.getIdUsuario())) {
            throw new IllegalArgumentException(
                    "Ya existe una billetera para el usuario con ID "
                            + billetera.getIdUsuario());
        }
        if (billetera.getPuntosFidelizacion() == null) {
            billetera.setPuntosFidelizacion(0);
        }
        return billeteraRepository.save(billetera);
    }

    public Billetera actualizar(Long id, Billetera billeteraActualizada) {
        Billetera existente = obtenerPorId(id);

        if (!existente.getIdUsuario().equals(billeteraActualizada.getIdUsuario())
                && billeteraRepository.existsByUsuarioId(billeteraActualizada.getIdUsuario())) {
            throw new IllegalArgumentException(
                    "Ya existe una billetera para el usuario con ID "
                            + billeteraActualizada.getIdUsuario());
        }

        billeteraActualizada.setId(existente.getId());
        return billeteraRepository.save(billeteraActualizada);
    }

    public Billetera recargarSaldo(Long id, Double monto) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException(
                    "El monto de recarga debe ser un valor positivo");
        }
        Billetera billetera = obtenerPorId(id);
        billetera.setSaldo(billetera.getSaldo() + monto);

        // Por cada $1.000 recargados se suma 1 punto de fidelización
        int puntosGanados = (int) (monto / 1000);
        billetera.setPuntosFidelizacion(
                billetera.getPuntosFidelizacion() + puntosGanados);

        return billeteraRepository.save(billetera);
    }

    public void eliminar(Long id) {
        if (!billeteraRepository.deleteById(id)) {
            throw new BilleteraNotFoundException(
                    "Billetera con ID " + id + " no encontrada");
        }
    }
}