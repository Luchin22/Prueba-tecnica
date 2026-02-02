package com.banco.cuenta.controller;

import com.banco.cuenta.dto.ReporteEstadoCuentaDTO;
import com.banco.cuenta.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/estado-cuenta/cliente/{clienteId}")
    public ResponseEntity<ReporteEstadoCuentaDTO> generarReporteEstadoCuentaCliente(
            @PathVariable String clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        log.info("GET /api/reportes/estado-cuenta/cliente/{} - Desde: {} Hasta: {}",
                clienteId, fechaInicio, fechaFin);

        try {
            ReporteEstadoCuentaDTO reporte = reporteService.generarReporteEstadoCuenta(
                    clienteId, fechaInicio, fechaFin);
            return ResponseEntity.ok(reporte);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al generar reporte: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (RuntimeException e) {
            log.error("Error al generar reporte para cliente {}: {}", clienteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/estado-cuenta/cuenta/{numeroCuenta}")
    public ResponseEntity<ReporteEstadoCuentaDTO> generarReporteEstadoCuentaPorCuenta(
            @PathVariable String numeroCuenta,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        log.info("GET /api/reportes/estado-cuenta/cuenta/{} - Desde: {} Hasta: {}",
                numeroCuenta, fechaInicio, fechaFin);

        try {
            ReporteEstadoCuentaDTO reporte = reporteService.generarReportePorCuenta(
                    numeroCuenta, fechaInicio, fechaFin);
            return ResponseEntity.ok(reporte);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al generar reporte: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (RuntimeException e) {
            log.error("Error al generar reporte para cuenta {}: {}", numeroCuenta, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/estado-cuenta")
    public ResponseEntity<ReporteEstadoCuentaDTO> generarReporteEstadoCuenta(
            @RequestParam String clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        log.info("GET /api/reportes/estado-cuenta?clienteId={}&fechaInicio={}&fechaFin={}",
                clienteId, fechaInicio, fechaFin);

        try {
            ReporteEstadoCuentaDTO reporte = reporteService.generarReporteEstadoCuenta(
                    clienteId, fechaInicio, fechaFin);
            return ResponseEntity.ok(reporte);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al generar reporte: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (RuntimeException e) {
            log.error("Error al generar reporte para cliente {}: {}", clienteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Error de validación: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error("Error en operación de reporte: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al generar el reporte: " + e.getMessage());
    }
}
