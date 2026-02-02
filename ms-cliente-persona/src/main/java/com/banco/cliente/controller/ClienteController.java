package com.banco.cliente.controller;

import com.banco.cliente.dto.ClienteRequestDto;
import com.banco.cliente.dto.ClienteResponseDto;
import com.banco.cliente.service.ClienteService;
import com.banco.common.dto.ApiResponse;
import com.banco.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponseDto>> crear(
            @Valid @RequestBody ClienteRequestDto dto) {
        log.info("POST /api/clientes - Crear cliente");
        ClienteResponseDto cliente = clienteService.crear(dto);

        ApiResponse<ClienteResponseDto> response = ApiResponse.<ClienteResponseDto>builder()
                .success(true)
                .message("Cliente creado exitosamente")
                .data(cliente)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponseDto>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaCreacion,desc") String sort) {
        log.info("GET /api/clientes - page: {}, size: {}, sort: {}", page, size, sort);

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<ClienteResponseDto> pageResult = clienteService.listar(pageable);

        PageResponse<ClienteResponseDto> pageResponse = PageResponse.<ClienteResponseDto>builder()
                .content(pageResult.getContent())
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .empty(pageResult.isEmpty())
                .build();

        ApiResponse<PageResponse<ClienteResponseDto>> response = ApiResponse.<PageResponse<ClienteResponseDto>>builder()
                .success(true)
                .message("Clientes obtenidos exitosamente")
                .data(pageResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponseDto>> obtenerPorId(@PathVariable String id) {
        log.info("GET /api/clientes/{}", id);
        ClienteResponseDto cliente = clienteService.obtenerPorId(id);

        ApiResponse<ClienteResponseDto> response = ApiResponse.<ClienteResponseDto>builder()
                .success(true)
                .message("Cliente encontrado")
                .data(cliente)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponseDto>> actualizar(
            @PathVariable String id,
            @Valid @RequestBody ClienteRequestDto dto) {
        log.info("PUT /api/clientes/{}", id);
        ClienteResponseDto cliente = clienteService.actualizar(id, dto);

        ApiResponse<ClienteResponseDto> response = ApiResponse.<ClienteResponseDto>builder()
                .success(true)
                .message("Cliente actualizado exitosamente")
                .data(cliente)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String id) {
        log.info("DELETE /api/clientes/{}", id);
        clienteService.eliminar(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Cliente eliminado exitosamente")
                .build();

        return ResponseEntity.ok(response);
    }
}
