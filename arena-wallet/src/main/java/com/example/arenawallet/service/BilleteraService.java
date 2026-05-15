package com.example.arenawallet.service;

import com.example.arenawallet.exception.BilleteraNotFoundException;
import com.example.arenawallet.model.Billetera;
import com.example.arenawallet.model.BilleteraHistory;
import com.example.arenawallet.repository.BilleteraHistoryRepository;
import com.example.arenawallet.repository.BilleteraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BilleteraService {

    private final BilleteraRepository billeteraRepository;
    private final BilleteraHistoryRepository historialRepository; // Auditoría L13

    // Inyectamos ambos repositorios
    public BilleteraService(BilleteraRepository billeteraRepository,
                            BilleteraHistoryRepository historialRepository) {
        this.billeteraRepository = billeteraRepository;
        this.historialRepository = historialRepository;
    }

    public List<Billetera> listarTodas() {
        return billeteraRepository.findAll();
    }

    public Billetera obtenerPorId(Long id) {
        return billeteraRepository.findById(id)
                .orElseThrow(() -> new BilleteraNotFoundException(
                        "Billetera con ID " + id + " no encontrada"));
    }

    @Transactional
    public Billetera crear(Billetera billetera) {
        if (billeteraRepository.existsByIdUsuario(billetera.getIdUsuario())) {
            throw new IllegalArgumentException(
                    "Ya existe una billetera para el usuario con ID "
                            + billetera.getIdUsuario());
        }
        if (billetera.getPuntosFidelizacion() == null) {
            billetera.setPuntosFidelizacion(0);
        }
        if (billetera.getSaldo() == null) {
            billetera.setSaldo(0.0);
        }

        Billetera guardada = billeteraRepository.save(billetera);

        // Guardar historial inicial
        registrarHistorial(guardada, "CREACION", guardada.getSaldo(), 0.0, guardada.getSaldo());

        return guardada;
    }

    @Transactional
    public Billetera actualizar(Long id, Billetera billeteraActualizada) {
        Billetera existente = obtenerPorId(id);

        if (!existente.getIdUsuario().equals(billeteraActualizada.getIdUsuario())
                && billeteraRepository.existsByIdUsuarioAndIdNot(
                billeteraActualizada.getIdUsuario(), id)) {
            throw new IllegalArgumentException(
                    "Ya existe una billetera para el usuario con ID "
                            + billeteraActualizada.getIdUsuario());
        }

        billeteraActualizada.setId(existente.getId());
        return billeteraRepository.save(billeteraActualizada);
    }

    // --- LÓGICA DE AUDITORÍA (LECCIÓN 13) ---
    @Transactional
    public Billetera recargarSaldo(Long id, Double monto) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException(
                    "El monto de recarga debe ser un valor positivo");
        }

        Billetera billetera = obtenerPorId(id);
        Double saldoAnterior = billetera.getSaldo();

        billetera.setSaldo(saldoAnterior + monto);

        int puntosGanados = (int) (monto / 1000);
        billetera.setPuntosFidelizacion(
                billetera.getPuntosFidelizacion() + puntosGanados);

        Billetera guardada = billeteraRepository.save(billetera);

        // Registrar el movimiento en el historial
        registrarHistorial(guardada, "RECARGA", monto, saldoAnterior, guardada.getSaldo());

        return guardada;
    }

    public List<BilleteraHistory> obtenerHistorial(Long id) {
        if (!billeteraRepository.existsById(id)) {
            throw new BilleteraNotFoundException("Billetera con ID " + id + " no encontrada");
        }
        return historialRepository.findByBilleteraIdOrderByFechaOperacionDesc(id);
    }

    // Método auxiliar para no repetir código al guardar historial
    private void registrarHistorial(Billetera billetera, String tipo, Double monto, Double anterior, Double nuevo) {
        BilleteraHistory historial = new BilleteraHistory();
        historial.setBilletera(billetera);
        historial.setTipoOperacion(tipo);
        historial.setMontoInvolucrado(monto);
        historial.setSaldoAnterior(anterior);
        historial.setSaldoNuevo(nuevo);
        historial.setFechaOperacion(LocalDateTime.now());
        historialRepository.save(historial);
    }
    // ----------------------------------------

    public void eliminar(Long id) {
        if (!billeteraRepository.existsById(id)) {
            throw new BilleteraNotFoundException(
                    "Billetera con ID " + id + " no encontrada");
        }
        billeteraRepository.deleteById(id);
    }
}