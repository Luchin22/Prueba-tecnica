package com.banco.cuenta.strategy;

import com.banco.cuenta.entity.Cuenta;
import com.banco.cuenta.entity.Movimiento;
import com.banco.cuenta.enums.TipoMovimiento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DepositoStrategy implements MovimientoStrategy {

    @Override
    public Movimiento procesarMovimiento(Cuenta cuenta, BigDecimal valor) {
        validarMovimiento(cuenta, valor);

        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        BigDecimal saldoDespues = saldoAnterior.add(valor);

        Movimiento movimiento = Movimiento.builder()
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoMovimiento(TipoMovimiento.DEPOSITO)
                .valor(valor)
                .saldoAnterior(saldoAnterior)
                .saldoDespues(saldoDespues)
                .build();

        cuenta.setSaldoActual(saldoDespues);

        return movimiento;
    }

    @Override
    public void validarMovimiento(Cuenta cuenta, BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor del depósito no puede ser nulo");
        }

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del depósito debe ser mayor a cero");
        }

        if (!cuenta.getEstado()) {
            throw new IllegalArgumentException("No se pueden realizar depósitos en una cuenta inactiva");
        }
    }
}
