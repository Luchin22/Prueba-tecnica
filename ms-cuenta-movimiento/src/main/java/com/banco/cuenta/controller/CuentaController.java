package com.banco.cuenta.controller;

import com.banco.cuenta.dto.CuentaRequestDto;
import com.banco.cuenta.dto.CuentaResponseDto;
import com.banco.cuenta.service.CuentaService;
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
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @GetMapping
    public ResponseEntity<Page<CuentaResponseDto>> getAllCuentas(
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("GET /api/cuentas - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<CuentaResponseDto> cuentas = cuentaService.getAllCuentas(pageable);
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDto> getCuentaByNumeroCuenta(@PathVariable String numeroCuenta) {
        log.debug("GET /api/cuentas/{}", numeroCuenta);
        try {
            CuentaResponseDto cuenta = cuentaService.getCuentaByNumeroCuenta(numeroCuenta);
            return ResponseEntity.ok(cuenta);
        } catch (RuntimeException e) {
            log.error("Error al obtener cuenta {}: {}", numeroCuenta, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Page<CuentaResponseDto>> getCuentasByClienteId(
            @PathVariable String clienteId,
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("GET /api/cuentas/cliente/{} - Página: {}, Tamaño: {}",
                clienteId, pageable.getPageNumber(), pageable.getPageSize());
        Page<CuentaResponseDto> cuentas = cuentaService.getCuentasByClienteId(clienteId, pageable);
        return ResponseEntity.ok(cuentas);
    }

    @PostMapping
    public ResponseEntity<CuentaResponseDto> createCuenta(@Valid @RequestBody CuentaRequestDto cuentaRequestDto) {
        log.info("POST /api/cuentas - Creando cuenta para cliente: {}", cuentaRequestDto.getClienteId());
        try {
            CuentaResponseDto cuentaCreada = cuentaService.createCuenta(cuentaRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(cuentaCreada);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear cuenta: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al crear cuenta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDto> updateCuenta(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody CuentaRequestDto cuentaRequestDto) {
        log.info("PUT /api/cuentas/{} - Actualizando cuenta", numeroCuenta);
        try {
            CuentaResponseDto cuentaActualizada = cuentaService.updateCuenta(numeroCuenta, cuentaRequestDto);
            return ResponseEntity.ok(cuentaActualizada);
        } catch (RuntimeException e) {
            log.error("Error al actualizar cuenta {}: {}", numeroCuenta, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<Void> deleteCuenta(@PathVariable String numeroCuenta) {
        log.info("DELETE /api/cuentas/{} - Eliminando cuenta", numeroCuenta);
        try {
            cuentaService.deleteCuenta(numeroCuenta);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar cuenta {}: {}", numeroCuenta, e.getMessage());
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
