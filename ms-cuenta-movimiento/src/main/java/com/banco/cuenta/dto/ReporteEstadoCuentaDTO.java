package com.banco.cuenta.dto;

import com.banco.cuenta.enums.TipoCuenta;
import com.banco.cuenta.enums.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteEstadoCuentaDTO {
    private String clienteId;
    private String nombreCliente;
    private String identificacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Instant fechaGeneracion;
    private List<DetalleCuenta> cuentas;
    private BigDecimal saldoTotalGeneral;
    private Integer totalCuentas;

   
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleCuenta {
        private String numeroCuenta;
        private TipoCuenta tipoCuenta;
        private BigDecimal saldoInicial;
        private BigDecimal saldoActual;
        private Boolean estado;
        private BigDecimal totalDepositos;
        private BigDecimal totalRetiros;
        private Integer totalMovimientos;
        private List<DetalleMovimiento> movimientos;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleMovimiento {
        private Instant fecha;
        private TipoMovimiento tipoMovimiento;
        private BigDecimal valor;
        private BigDecimal saldo;
    }
}
