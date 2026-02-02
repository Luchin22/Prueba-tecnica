package com.banco.cuenta.strategy;

import com.banco.cuenta.entity.Cuenta;
import com.banco.cuenta.entity.Movimiento;

import java.math.BigDecimal;

public interface MovimientoStrategy {

    Movimiento procesarMovimiento(Cuenta cuenta, BigDecimal valor);

    void validarMovimiento(Cuenta cuenta, BigDecimal valor);
}
