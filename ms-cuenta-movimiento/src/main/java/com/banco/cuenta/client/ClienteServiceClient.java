package com.banco.cuenta.client;

import com.banco.cuenta.dto.ApiResponseWrapper;
import com.banco.cuenta.dto.ClienteDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;


@Slf4j
@Component
public class ClienteServiceClient {

    private final WebClient webClient;
    private final long timeout;

    public ClienteServiceClient(
            WebClient webClient,
            @Value("${cliente-service.timeout:5000}") long timeout) {
        this.webClient = webClient;
        this.timeout = timeout;
    }

  
    @CircuitBreaker(name = "clienteService", fallbackMethod = "getClienteFallback")
    @Retry(name = "clienteService")
    public ClienteDto getClienteById(String clienteId) {
        log.debug("Consultando cliente con ID: {}", clienteId);

        try {
            ApiResponseWrapper<ClienteDto> response = webClient.get()
                    .uri("/api/clientes/{id}", clienteId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseWrapper<ClienteDto>>() {})
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response == null || response.getData() == null) {
                throw new RuntimeException("Cliente no encontrado con ID: " + clienteId);
            }

            log.debug("Cliente obtenido exitosamente: {}", clienteId);
            return response.getData();

        } catch (WebClientResponseException.NotFound e) {
            log.error("Cliente no encontrado con ID: {}", clienteId);
            throw new RuntimeException("Cliente no encontrado con ID: " + clienteId);

        } catch (Exception e) {
            log.error("Error al consultar cliente con ID: {}. Error: {}", clienteId, e.getMessage());
            throw new RuntimeException("Error al consultar el servicio de clientes", e);
        }
    }

   
    @CircuitBreaker(name = "clienteService", fallbackMethod = "existsClienteFallback")
    @Retry(name = "clienteService")
    public boolean existsCliente(String clienteId) {
        log.debug("Verificando existencia de cliente con ID: {}", clienteId);

        try {
            ApiResponseWrapper<ClienteDto> response = webClient.get()
                    .uri("/api/clientes/{id}", clienteId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseWrapper<ClienteDto>>() {})
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                    .block();

            ClienteDto cliente = response != null ? response.getData() : null;
            boolean exists = cliente != null && Boolean.TRUE.equals(cliente.getEstado());
            log.debug("Cliente {} existe: {}", clienteId, exists);
            return exists;

        } catch (Exception e) {
            log.error("Error al verificar existencia de cliente con ID: {}. Error: {}", clienteId, e.getMessage());
            return false;
        }
    }

    private ClienteDto getClienteFallback(String clienteId, Throwable throwable) {
        log.warn("Fallback activado para getClienteById. Cliente ID: {}. Razón: {}",
                clienteId, throwable.getMessage());
        throw new RuntimeException("Servicio de clientes no disponible temporalmente. Por favor, intente más tarde.");
    }

 
    private boolean existsClienteFallback(String clienteId, Throwable throwable) {
        log.warn("Fallback activado para existsCliente. Cliente ID: {}. Razón: {}",
                clienteId, throwable.getMessage());
        return false;
    }
}
