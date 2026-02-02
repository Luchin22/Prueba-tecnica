package com.banco.cuenta.dto;

import com.banco.cuenta.enums.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponseDto {
    private String movimientoId;
    private Instant fecha;
    private TipoMovimiento tipoMovimiento;
    private BigDecimal valor;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoDespues;
    private String numeroCuenta;
    private String descripcion;
}
