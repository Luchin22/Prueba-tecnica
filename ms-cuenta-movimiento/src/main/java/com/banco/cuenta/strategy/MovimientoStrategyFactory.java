package com.banco.cuenta.strategy;

import com.banco.cuenta.enums.TipoMovimiento;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MovimientoStrategyFactory {

    private final Map<TipoMovimiento, MovimientoStrategy> strategies;

    public MovimientoStrategyFactory(
            DepositoStrategy depositoStrategy,
            RetiroStrategy retiroStrategy) {
        this.strategies = new HashMap<>();
        this.strategies.put(TipoMovimiento.DEPOSITO, depositoStrategy);
        this.strategies.put(TipoMovimiento.RETIRO, retiroStrategy);
    }

    public MovimientoStrategy getStrategy(TipoMovimiento tipoMovimiento) {
        if (tipoMovimiento == null) {
            throw new IllegalArgumentException("El tipo de movimiento no puede ser nulo");
        }

        MovimientoStrategy strategy = strategies.get(tipoMovimiento);
        if (strategy == null) {
            throw new IllegalArgumentException("Tipo de movimiento no soportado: " + tipoMovimiento);
        }

        return strategy;
    }
}
