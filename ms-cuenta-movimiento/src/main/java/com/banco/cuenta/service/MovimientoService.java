package com.banco.cuenta.service;

import com.banco.cuenta.dto.MovimientoRequestDto;
import com.banco.cuenta.dto.MovimientoResponseDto;
import com.banco.cuenta.entity.Cuenta;
import com.banco.cuenta.entity.Movimiento;
import com.banco.cuenta.mapper.MovimientoMapper;
import com.banco.cuenta.repository.CuentaRepository;
import com.banco.cuenta.repository.MovimientoRepository;
import com.banco.cuenta.strategy.MovimientoStrategy;
import com.banco.cuenta.strategy.MovimientoStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoMapper movimientoMapper;
    private final MovimientoStrategyFactory strategyFactory;

    @Transactional(readOnly = true)
    public Page<MovimientoResponseDto> getAllMovimientos(Pageable pageable) {
        log.debug("Obteniendo movimientos con paginación: {}", pageable);
        Page<Movimiento> movimientos = movimientoRepository.findAll(pageable);
        return movimientos.map(movimientoMapper::toDto);
    }

    @Transactional(readOnly = true)
    public MovimientoResponseDto getMovimientoById(String movimientoId) {
        log.debug("Buscando movimiento con ID: {}", movimientoId);
        Movimiento movimiento = movimientoRepository.findById(movimientoId)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado: " + movimientoId));
        return movimientoMapper.toDto(movimiento);
    }

    @Transactional(readOnly = true)
    public Page<MovimientoResponseDto> getMovimientosByCuenta(String numeroCuenta, Pageable pageable) {
        log.debug("Obteniendo movimientos de la cuenta {} con paginación: {}", numeroCuenta, pageable);
        Page<Movimiento> movimientos = movimientoRepository.findByNumeroCuenta(numeroCuenta, pageable);
        return movimientos.map(movimientoMapper::toDto);
    }

    @Transactional
    public MovimientoResponseDto createMovimiento(MovimientoRequestDto movimientoRequestDto) {
        log.debug("Procesando nuevo movimiento: {}", movimientoRequestDto);

        Cuenta cuenta = cuentaRepository.findById(movimientoRequestDto.getNumeroCuenta())
                .orElseThrow(() -> new RuntimeException(
                        "Cuenta no encontrada: " + movimientoRequestDto.getNumeroCuenta()));

        if (!cuenta.getEstado()) {
            throw new IllegalArgumentException("La cuenta está inactiva y no puede realizar movimientos");
        }

        MovimientoStrategy strategy = strategyFactory.getStrategy(movimientoRequestDto.getTipoMovimiento());

        Movimiento movimiento = strategy.procesarMovimiento(cuenta, movimientoRequestDto.getValor());
        movimiento.setDescripcion(movimientoRequestDto.getDescripcion());

        movimiento = movimientoRepository.save(movimiento);
        cuentaRepository.save(cuenta);

        log.info("Movimiento procesado exitosamente. ID: {}, Tipo: {}, Nuevo saldo: {}",
                movimiento.getMovimientoId(), movimiento.getTipoMovimiento(), movimiento.getSaldoDespues());

        return movimientoMapper.toDto(movimiento);
    }

    @Transactional
    public void deleteMovimiento(String movimientoId) {
        log.warn("Eliminando movimiento: {}", movimientoId);

        if (!movimientoRepository.existsById(movimientoId)) {
            throw new RuntimeException("Movimiento no encontrado: " + movimientoId);
        }

        movimientoRepository.deleteById(movimientoId);
        log.warn("Movimiento eliminado con ID: {}", movimientoId);
    }
}
