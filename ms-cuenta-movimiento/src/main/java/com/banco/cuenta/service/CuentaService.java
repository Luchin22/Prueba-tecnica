package com.banco.cuenta.service;

import com.banco.cuenta.client.ClienteServiceClient;
import com.banco.cuenta.dto.ClienteDto;
import com.banco.cuenta.dto.CuentaRequestDto;
import com.banco.cuenta.dto.CuentaResponseDto;
import com.banco.cuenta.entity.Cuenta;
import com.banco.cuenta.mapper.CuentaMapper;
import com.banco.cuenta.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final CuentaMapper cuentaMapper;
    private final ClienteServiceClient clienteServiceClient;

    @Transactional(readOnly = true)
    public Page<CuentaResponseDto> getAllCuentas(Pageable pageable) {
        log.debug("Obteniendo cuentas con paginación: {}", pageable);
        Page<Cuenta> cuentas = cuentaRepository.findAll(pageable);
        return cuentas.map(cuentaMapper::toDto);
    }

    @Transactional(readOnly = true)
    public CuentaResponseDto getCuentaByNumeroCuenta(String numeroCuenta) {
        log.debug("Buscando cuenta con número: {}", numeroCuenta);
        Cuenta cuenta = cuentaRepository.findById(numeroCuenta)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + numeroCuenta));
        return cuentaMapper.toDto(cuenta);
    }

    @Transactional(readOnly = true)
    public Page<CuentaResponseDto> getCuentasByClienteId(String clienteId, Pageable pageable) {
        log.debug("Obteniendo cuentas del cliente {} con paginación: {}", clienteId, pageable);
        Page<Cuenta> cuentas = cuentaRepository.findByEstadoTrue(pageable);
        return cuentas.map(cuentaMapper::toDto);
    }

    @Transactional
    public CuentaResponseDto createCuenta(CuentaRequestDto cuentaRequestDto) {
        log.debug("Creando nueva cuenta: {}", cuentaRequestDto);

        // validar que el cliente existe y esta activo
        validateCliente(cuentaRequestDto.getClienteId());

        Cuenta cuenta = cuentaMapper.toEntity(cuentaRequestDto);
        cuenta = cuentaRepository.save(cuenta);

        log.info("Cuenta creada exitosamente con número: {}", cuenta.getNumeroCuenta());
        return cuentaMapper.toDto(cuenta);
    }

    @Transactional
    public CuentaResponseDto updateCuenta(String numeroCuenta, CuentaRequestDto cuentaRequestDto) {
        log.debug("Actualizando cuenta con número: {}", numeroCuenta);

        Cuenta cuenta = cuentaRepository.findById(numeroCuenta)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + numeroCuenta));

        // actualizar campos permitidos
        cuentaMapper.updateEntity(cuentaRequestDto, cuenta);
        cuenta = cuentaRepository.save(cuenta);

        log.info("Cuenta actualizada exitosamente: {}", cuenta.getNumeroCuenta());
        return cuentaMapper.toDto(cuenta);
    }

    @Transactional
    public void deleteCuenta(String numeroCuenta) {
        log.debug("Eliminando cuenta con número: {}", numeroCuenta);

        if (!cuentaRepository.existsById(numeroCuenta)) {
            throw new RuntimeException("Cuenta no encontrada: " + numeroCuenta);
        }

        cuentaRepository.deleteById(numeroCuenta);
        log.info("Cuenta eliminada exitosamente: {}", numeroCuenta);
    }

    private void validateCliente(String clienteId) {
        log.debug("Validando existencia del cliente con ID: {}", clienteId);

        try {
            ClienteDto cliente = clienteServiceClient.getClienteById(clienteId);

            if (cliente == null) {
                throw new RuntimeException("Cliente no encontrado con ID: " + clienteId);
            }

            if (!Boolean.TRUE.equals(cliente.getEstado())) {
                throw new RuntimeException("El cliente con ID " + clienteId + " no está activo");
            }

            log.debug("Cliente validado exitosamente: {}", clienteId);

        } catch (Exception e) {
            log.error("Error al validar cliente con ID: {}. Error: {}", clienteId, e.getMessage());
            throw new RuntimeException("No se pudo validar el cliente: " + e.getMessage(), e);
        }
    }
}
