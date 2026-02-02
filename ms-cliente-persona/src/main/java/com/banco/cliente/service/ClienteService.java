package com.banco.cliente.service;

import com.banco.cliente.dto.ClienteRequestDto;
import com.banco.cliente.dto.ClienteResponseDto;
import com.banco.cliente.entity.Cliente;
import com.banco.cliente.event.ClienteEvent;
import com.banco.cliente.exception.ClienteDuplicadoException;
import com.banco.cliente.exception.ClienteNotFoundException;
import com.banco.cliente.mapper.ClienteMapper;
import com.banco.cliente.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;
    private final RabbitMQPublisher rabbitMQPublisher;

    @Transactional
    public ClienteResponseDto crear(ClienteRequestDto dto) {
        log.info("Creando cliente con identificación: {}", dto.getIdentificacion());

        if (clienteRepository.existsByIdentificacion(dto.getIdentificacion())) {
            throw new ClienteDuplicadoException(
                "Ya existe un cliente con la identificación: " + dto.getIdentificacion()
            );
        }

        Cliente cliente = clienteMapper.toEntity(dto);
        cliente.setContrasena(passwordEncoder.encode(dto.getContrasena()));

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente creado exitosamente con ID: {}", clienteGuardado.getClienteId());

        ClienteEvent event = ClienteEvent.builder()
                .clienteId(clienteGuardado.getClienteId())
                .nombre(clienteGuardado.getNombre())
                .identificacion(clienteGuardado.getIdentificacion())
                .estado(clienteGuardado.getEstado())
                .eventType("CREATED")
                .build();
        rabbitMQPublisher.publishClienteEvent(event);

        return clienteMapper.toDto(clienteGuardado);
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDto> listar(Pageable pageable) {
        log.info("Listando clientes activos - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Cliente> clientes = clienteRepository.findByEstadoTrue(pageable);
        return clientes.map(clienteMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerPorId(String clienteId) {
        log.info("Buscando cliente por ID: {}", clienteId);
        Cliente cliente = clienteRepository.findByClienteIdAndEstadoTrue(clienteId)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + clienteId));
        return clienteMapper.toDto(cliente);
    }

    @Transactional
    public ClienteResponseDto actualizar(String clienteId, ClienteRequestDto dto) {
        log.info("Actualizando cliente ID: {}", clienteId);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + clienteId));

        if (!cliente.getIdentificacion().equals(dto.getIdentificacion()) &&
            clienteRepository.existsByIdentificacion(dto.getIdentificacion())) {
            throw new ClienteDuplicadoException(
                "Ya existe un cliente con la identificación: " + dto.getIdentificacion()
            );
        }

        clienteMapper.updateEntity(dto, cliente);

        if (dto.getContrasena() != null && !dto.getContrasena().isBlank()) {
            cliente.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        }

        Cliente clienteActualizado = clienteRepository.save(cliente);
        log.info("Cliente actualizado exitosamente: {}", clienteId);

        ClienteEvent event = ClienteEvent.builder()
                .clienteId(clienteActualizado.getClienteId())
                .nombre(clienteActualizado.getNombre())
                .identificacion(clienteActualizado.getIdentificacion())
                .estado(clienteActualizado.getEstado())
                .eventType("UPDATED")
                .build();
        rabbitMQPublisher.publishClienteEvent(event);

        return clienteMapper.toDto(clienteActualizado);
    }

    @Transactional
    public void eliminar(String clienteId) {
        log.info("Eliminando cliente ID: {}", clienteId);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + clienteId));

        cliente.setEstado(false);
        clienteRepository.save(cliente);
        log.info("Cliente eliminado exitosamente: {}", clienteId);

        ClienteEvent event = ClienteEvent.builder()
                .clienteId(cliente.getClienteId())
                .nombre(cliente.getNombre())
                .identificacion(cliente.getIdentificacion())
                .estado(false)
                .eventType("DELETED")
                .build();
        rabbitMQPublisher.publishClienteEvent(event);
    }
}
