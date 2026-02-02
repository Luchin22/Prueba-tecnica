package com.banco.cuenta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResponseDto {
    private ClienteDto cliente;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<CuentaConMovimientosDto> cuentas;
}
