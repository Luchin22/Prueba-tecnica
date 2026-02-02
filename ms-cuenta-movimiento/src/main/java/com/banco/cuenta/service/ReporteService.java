package com.banco.cuenta.service;

import com.banco.cuenta.client.ClienteServiceClient;
import com.banco.cuenta.dto.ClienteDto;
import com.banco.cuenta.dto.ReporteEstadoCuentaDTO;
import com.banco.cuenta.entity.Cuenta;
import com.banco.cuenta.entity.Movimiento;
import com.banco.cuenta.repository.CuentaRepository;
import com.banco.cuenta.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Guayaquil");

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ClienteServiceClient clienteServiceClient;

    @Transactional(readOnly = true)
    public ReporteEstadoCuentaDTO generarReporteEstadoCuenta(
            String clienteId,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        log.info("Generando reporte de estado de cuenta para cliente {} desde {} hasta {}",
                clienteId, fechaInicio, fechaFin);

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        ClienteDto cliente = obtenerInformacionCliente(clienteId);
        List<Cuenta> cuentas = cuentaRepository.findByClienteIdAndEstadoTrue(clienteId);

        if (cuentas.isEmpty()) {
            log.warn("El cliente {} no tiene cuentas asociadas", clienteId);
        }

        Instant fechaInicioTime = fechaInicio.atStartOfDay(ZONA_HORARIA).toInstant();
        Instant fechaFinTime = fechaFin.atTime(LocalTime.MAX).atZone(ZONA_HORARIA).toInstant();

        List<String> numerosCuenta = cuentas.stream()
                .map(Cuenta::getNumeroCuenta)
                .collect(Collectors.toList());

        List<Movimiento> todosMovimientos = numerosCuenta.isEmpty()
                ? new ArrayList<>()
                : movimientoRepository.findMovimientosParaReporte(numerosCuenta, fechaInicioTime, fechaFinTime);

        ReporteEstadoCuentaDTO reporte = new ReporteEstadoCuentaDTO();
        reporte.setClienteId(clienteId);
        reporte.setNombreCliente(cliente.getNombre());
        reporte.setIdentificacion(cliente.getIdentificacion());
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setFechaGeneracion(Instant.now());

        List<ReporteEstadoCuentaDTO.DetalleCuenta> detallesCuentas = new ArrayList<>();
        BigDecimal saldoTotalGeneral = BigDecimal.ZERO;

        for (Cuenta cuenta : cuentas) {
            List<Movimiento> movimientosCuenta = todosMovimientos.stream()
                    .filter(m -> m.getNumeroCuenta().equals(cuenta.getNumeroCuenta()))
                    .collect(Collectors.toList());

            ReporteEstadoCuentaDTO.DetalleCuenta detalleCuenta = procesarCuenta(cuenta, movimientosCuenta);
            detallesCuentas.add(detalleCuenta);
            saldoTotalGeneral = saldoTotalGeneral.add(cuenta.getSaldoActual());
        }

        reporte.setCuentas(detallesCuentas);
        reporte.setSaldoTotalGeneral(saldoTotalGeneral);
        reporte.setTotalCuentas(cuentas.size());

        log.info("Reporte generado exitosamente para cliente {}. Total cuentas: {}, Saldo total: {}",
                clienteId, reporte.getTotalCuentas(), reporte.getSaldoTotalGeneral());

        return reporte;
    }

    @Transactional(readOnly = true)
    public ReporteEstadoCuentaDTO generarReportePorCuenta(
            String numeroCuenta,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        log.info("Generando reporte para cuenta {} desde {} hasta {}",
                numeroCuenta, fechaInicio, fechaFin);

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        Cuenta cuenta = cuentaRepository.findById(numeroCuenta)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + numeroCuenta));

        ClienteDto cliente = obtenerInformacionCliente(cuenta.getClienteId());

        Instant fechaInicioTime = fechaInicio.atStartOfDay(ZONA_HORARIA).toInstant();
        Instant fechaFinTime = fechaFin.atTime(LocalTime.MAX).atZone(ZONA_HORARIA).toInstant();

        List<Movimiento> movimientos = movimientoRepository.findMovimientosParaReporte(
                List.of(numeroCuenta), fechaInicioTime, fechaFinTime);

        ReporteEstadoCuentaDTO reporte = new ReporteEstadoCuentaDTO();
        reporte.setClienteId(cuenta.getClienteId());
        reporte.setNombreCliente(cliente.getNombre());
        reporte.setIdentificacion(cliente.getIdentificacion());
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setFechaGeneracion(Instant.now());

        ReporteEstadoCuentaDTO.DetalleCuenta detalleCuenta = procesarCuenta(cuenta, movimientos);

        List<ReporteEstadoCuentaDTO.DetalleCuenta> cuentas = new ArrayList<>();
        cuentas.add(detalleCuenta);

        reporte.setCuentas(cuentas);
        reporte.setSaldoTotalGeneral(cuenta.getSaldoActual());
        reporte.setTotalCuentas(1);

        log.info("Reporte generado exitosamente para cuenta {}", numeroCuenta);
        return reporte;
    }

    private ClienteDto obtenerInformacionCliente(String clienteId) {
        log.debug("Consultando información del cliente {} desde ms-cliente-persona", clienteId);

        try {
            ClienteDto cliente = clienteServiceClient.getClienteById(clienteId);

            if (cliente == null) {
                throw new RuntimeException("Cliente no encontrado con ID: " + clienteId);
            }

            return cliente;

        } catch (Exception e) {
            log.error("Error al consultar información del cliente {}: {}", clienteId, e.getMessage());
            throw new RuntimeException("No se pudo obtener la información del cliente: " + e.getMessage(), e);
        }
    }

    private ReporteEstadoCuentaDTO.DetalleCuenta procesarCuenta(
            Cuenta cuenta,
            List<Movimiento> movimientos) {

        log.debug("Procesando cuenta {} para el reporte", cuenta.getNumeroCuenta());

        BigDecimal totalDepositos = BigDecimal.ZERO;
        BigDecimal totalRetiros = BigDecimal.ZERO;

        List<ReporteEstadoCuentaDTO.DetalleMovimiento> detallesMovimientos = new ArrayList<>();

        for (Movimiento movimiento : movimientos) {
            ReporteEstadoCuentaDTO.DetalleMovimiento detalleMovimiento =
                    ReporteEstadoCuentaDTO.DetalleMovimiento.builder()
                            .fecha(movimiento.getFecha())
                            .tipoMovimiento(movimiento.getTipoMovimiento())
                            .valor(movimiento.getValor())
                            .saldo(movimiento.getSaldoDespues())
                            .build();

            detallesMovimientos.add(detalleMovimiento);

            if (movimiento.getValor().compareTo(BigDecimal.ZERO) > 0) {
                totalDepositos = totalDepositos.add(movimiento.getValor());
            } else {
                totalRetiros = totalRetiros.add(movimiento.getValor().abs());
            }
        }

        ReporteEstadoCuentaDTO.DetalleCuenta detalleCuenta =
                ReporteEstadoCuentaDTO.DetalleCuenta.builder()
                        .numeroCuenta(cuenta.getNumeroCuenta())
                        .tipoCuenta(cuenta.getTipoCuenta())
                        .saldoInicial(cuenta.getSaldoInicial())
                        .saldoActual(cuenta.getSaldoActual())
                        .estado(cuenta.getEstado())
                        .totalDepositos(totalDepositos)
                        .totalRetiros(totalRetiros)
                        .movimientos(detallesMovimientos)
                        .totalMovimientos(movimientos.size())
                        .build();

        log.debug("Cuenta {} procesada. Movimientos: {}, Depósitos: {}, Retiros: {}",
                cuenta.getNumeroCuenta(), movimientos.size(), totalDepositos, totalRetiros);

        return detalleCuenta;
    }
}
