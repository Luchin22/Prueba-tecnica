package com.banco.cuenta.dto;

import com.banco.cuenta.enums.TipoMovimiento;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoRequestDto {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(regexp = "^CTA-[A-F0-9]{8}$", message = "Formato de cuenta inválido")
    private String numeroCuenta;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor debe ser mayor a cero")
    @DecimalMax(value = "999999.99", message = "El valor excede el máximo permitido")
    private BigDecimal valor;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
}
