package com.banco.cuenta.strategy;

import com.banco.cuenta.entity.Cuenta;
import com.banco.cuenta.entity.Movimiento;
import com.banco.cuenta.enums.TipoMovimiento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RetiroStrategy implements MovimientoStrategy {

    @Override
    public Movimiento procesarMovimiento(Cuenta cuenta, BigDecimal valor) {
        validarMovimiento(cuenta, valor);

        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        BigDecimal valorRetiro = valor.abs();
        BigDecimal saldoDespues = saldoAnterior.subtract(valorRetiro);

        Movimiento movimiento = Movimiento.builder()
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoMovimiento(TipoMovimiento.RETIRO)
                .valor(valorRetiro.negate())
                .saldoAnterior(saldoAnterior)
                .saldoDespues(saldoDespues)
                .build();

        cuenta.setSaldoActual(saldoDespues);

        return movimiento;
    }

    @Override
    public void validarMovimiento(Cuenta cuenta, BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor del retiro no puede ser nulo");
        }

        BigDecimal valorRetiro = valor.abs();

        if (valorRetiro.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del retiro debe ser mayor a cero");
        }

        if (!cuenta.getEstado()) {
            throw new IllegalArgumentException("No se pueden realizar retiros en una cuenta inactiva");
        }

        if (cuenta.getSaldoActual().compareTo(valorRetiro) < 0) {
            throw new IllegalArgumentException(
                String.format("Saldo no disponible. Saldo actual: %s, Retiro solicitado: %s",
                    cuenta.getSaldoActual(), valorRetiro)
            );
        }
    }
}
