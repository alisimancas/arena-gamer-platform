package com.example.arenareservas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

// Se conecta al puerto 9001 donde está tu Inventory
@FeignClient(name = "inventory-service", url = "http://localhost:9001/api/v1/productos")
public interface InventoryClient {

    @PatchMapping("/{id}/stock")
    void actualizarStock(@PathVariable("id") Long id, @RequestBody Map<String, Integer> body);
}