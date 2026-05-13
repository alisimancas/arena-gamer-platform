package com.example.arenawallet.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Billetera {

    private Long id;

    @NotNull(message = "El ID de usuario es obligatorio")
    @Min(value = 1, message = "El ID de usuario debe ser mayor a 0")
    private Long idUsuario;

    @NotNull(message = "El saldo es obligatorio")
    @PositiveOrZero(message = "El saldo no puede ser negativo")
    private Double saldo;

    @NotNull(message = "Los puntos de fidelización son obligatorios")
    @PositiveOrZero(message = "Los puntos no pueden ser negativos")
    private Integer puntosFidelizacion;
}