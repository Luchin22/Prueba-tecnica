package com.banco.cliente;

import com.banco.cliente.dto.ClienteRequestDto;
import com.banco.cliente.dto.ClienteResponseDto;
import com.banco.cliente.entity.Cliente;
import com.banco.cliente.exception.ClienteDuplicadoException;
import com.banco.cliente.exception.ClienteNotFoundException;
import com.banco.cliente.mapper.ClienteMapper;
import com.banco.cliente.repository.ClienteRepository;
import com.banco.cliente.service.ClienteService;
import com.banco.cliente.service.RabbitMQPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RabbitMQPublisher rabbitMQPublisher;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequestDto requestDto;
    private Cliente cliente;
    private ClienteResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = ClienteRequestDto.builder()
                .nombre("Jose Lema")
                .genero("M")
                .edad(35)
                .identificacion("1234567890")
                .direccion("Otavalo sn y principal")
                .telefono("098254785")
                .contrasena("1234")
                .estado(true)
                .build();

        cliente = Cliente.builder()
                .clienteId("CLI-A1B2C3D4")
                .nombre("Jose Lema")
                .genero("M")
                .edad(35)
                .identificacion("1234567890")
                .direccion("Otavalo sn y principal")
                .telefono("098254785")
                .contrasena("$2a$10$encrypted")
                .estado(true)
                .build();

        responseDto = ClienteResponseDto.builder()
                .clienteId("CLI-A1B2C3D4")
                .nombre("Jose Lema")
                .genero("M")
                .edad(35)
                .identificacion("1234567890")
                .direccion("Otavalo sn y principal")
                .telefono("098254785")
                .estado(true)
                .build();
    }

    @Test
    void cuandoCrearCliente_debeGenerarClienteIdYGuardar() {
        when(clienteRepository.existsByIdentificacion(anyString())).thenReturn(false);
        when(clienteMapper.toEntity(any(ClienteRequestDto.class))).thenReturn(cliente);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encrypted");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(responseDto);
        doNothing().when(rabbitMQPublisher).publishClienteEvent(any());

        ClienteResponseDto resultado = clienteService.crear(requestDto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getClienteId()).isEqualTo("CLI-A1B2C3D4");
        assertThat(resultado.getNombre()).isEqualTo("Jose Lema");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
        verify(rabbitMQPublisher, times(1)).publishClienteEvent(any());
    }

    @Test
    void cuandoCrearClienteConIdentificacionDuplicada_debeLanzarExcepcion() {
        when(clienteRepository.existsByIdentificacion(anyString())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crear(requestDto))
                .isInstanceOf(ClienteDuplicadoException.class)
                .hasMessageContaining("Ya existe un cliente con la identificación");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void cuandoObtenerClientePorIdInexistente_debeLanzarExcepcion() {
        String clienteId = "CLI-NOEXISTE";
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPorId(clienteId))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado");

        verify(clienteRepository, times(1)).findById(clienteId);
    }

    @Test
    void cuandoEliminarCliente_debeRealizarSoftDelete() {
        String clienteId = "CLI-A1B2C3D4";
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        doNothing().when(rabbitMQPublisher).publishClienteEvent(any());

        clienteService.eliminar(clienteId);

        assertThat(cliente.getEstado()).isFalse();
        verify(clienteRepository, times(1)).save(cliente);
        verify(rabbitMQPublisher, times(1)).publishClienteEvent(any());
    }
}
