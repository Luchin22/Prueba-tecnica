package com.banco.cuenta.dto;

import com.banco.cuenta.enums.TipoCuenta;
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
public class CuentaRequestDto {

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private TipoCuenta tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.0", message = "El saldo inicial no puede ser negativo")
    private BigDecimal saldoInicial;

    @Builder.Default
    private Boolean estado = true;

    @NotBlank(message = "El ID del cliente es obligatorio")
    @Pattern(regexp = "^CLI-[A-F0-9]{8}$", message = "Formato de cliente ID inválido")
    private String clienteId;
}
