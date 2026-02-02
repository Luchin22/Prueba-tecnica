package com.banco.cliente.service;

import com.banco.cliente.event.ClienteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key.cliente}")
    private String routingKey;

    public void publishClienteEvent(ClienteEvent event) {
        try {
            log.info("Publicando evento de cliente: {} - {}", event.getEventType(), event.getClienteId());
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Evento publicado exitosamente");
        } catch (Exception e) {
            log.error("Error al publicar evento de cliente: {}", e.getMessage(), e);
        }
    }
}
