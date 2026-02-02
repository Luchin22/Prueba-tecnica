package com.banco.cuenta.controller;

import com.banco.cuenta.dto.MovimientoRequestDto;
import com.banco.cuenta.dto.MovimientoResponseDto;
import com.banco.cuenta.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @GetMapping
    public ResponseEntity<Page<MovimientoResponseDto>> getAllMovimientos(
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("GET /api/movimientos - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<MovimientoResponseDto> movimientos = movimientoService.getAllMovimientos(pageable);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/{movimientoId}")
    public ResponseEntity<MovimientoResponseDto> getMovimientoById(@PathVariable String movimientoId) {
        log.debug("GET /api/movimientos/{}", movimientoId);
        try {
            MovimientoResponseDto movimiento = movimientoService.getMovimientoById(movimientoId);
            return ResponseEntity.ok(movimiento);
        } catch (RuntimeException e) {
            log.error("Error al obtener movimiento {}: {}", movimientoId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cuenta/{numeroCuenta}")
    public ResponseEntity<Page<MovimientoResponseDto>> getMovimientosByCuenta(
            @PathVariable String numeroCuenta,
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("GET /api/movimientos/cuenta/{} - Página: {}, Tamaño: {}",
                numeroCuenta, pageable.getPageNumber(), pageable.getPageSize());
        Page<MovimientoResponseDto> movimientos = movimientoService.getMovimientosByCuenta(numeroCuenta, pageable);
        return ResponseEntity.ok(movimientos);
    }

    @PostMapping
    public ResponseEntity<MovimientoResponseDto> createMovimiento(
            @Valid @RequestBody MovimientoRequestDto movimientoRequestDto) {
        log.info("POST /api/movimientos - Creando movimiento tipo {} en cuenta {}",
                movimientoRequestDto.getTipoMovimiento(), movimientoRequestDto.getNumeroCuenta());
        try {
            MovimientoResponseDto movimientoCreado = movimientoService.createMovimiento(movimientoRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(movimientoCreado);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear movimiento: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al crear movimiento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{movimientoId}")
    public ResponseEntity<Void> deleteMovimiento(@PathVariable String movimientoId) {
        log.warn("DELETE /api/movimientos/{}", movimientoId);
        try {
            movimientoService.deleteMovimiento(movimientoId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar movimiento {}: {}", movimientoId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Error de validación: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error("Error en operación: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al procesar la solicitud: " + e.getMessage());
    }
}
