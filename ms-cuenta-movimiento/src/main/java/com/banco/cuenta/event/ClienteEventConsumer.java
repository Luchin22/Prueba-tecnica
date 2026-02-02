package com.banco.cuenta.event;

import com.banco.cuenta.dto.ClienteEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteEventConsumer {

 
    @RabbitListener(queues = "${rabbitmq.queue.cliente-creado}")
    public void handleClienteCreado(ClienteEventDTO event) {
        try {
            log.info("Evento recibido - Cliente creado: ID={}, Nombre={}, Identificacion={}",
                    event.getClienteId(),
                    event.getNombre(),
                    event.getIdentificacion());

            // aqui se puede implementar logica adicional si es necesario
            // por ejemplo: enviar notificaciones, actualizar cache, etc.

            log.debug("Detalles del evento: {}", event);
            log.info("Evento de cliente creado procesado exitosamente");

        } catch (Exception e) {
            log.error("Error al procesar evento de cliente creado: {}", event, e);
            throw e; // re-lanzar para activar retry de rabbitmq
        }
    }

  
    @RabbitListener(queues = "${rabbitmq.queue.cliente-actualizado}")
    public void handleClienteActualizado(ClienteEventDTO event) {
        try {
            log.info("Evento recibido - Cliente actualizado: ID={}, Nombre={}, Estado={}",
                    event.getClienteId(),
                    event.getNombre(),
                    event.getEstado());

            // aqui se puede implementar logica adicional
            // por ejemplo: si el cliente fue desactivado, podria desactivar sus cuentas
            if (Boolean.FALSE.equals(event.getEstado())) {
                log.warn("Cliente {} ha sido desactivado. Considerar desactivar cuentas asociadas.",
                        event.getClienteId());
                // implementar logica de desactivacion de cuentas si es necesario
            }

            log.debug("Detalles del evento: {}", event);
            log.info("Evento de cliente actualizado procesado exitosamente");

        } catch (Exception e) {
            log.error("Error al procesar evento de cliente actualizado: {}", event, e);
            throw e; // re-lanzar para activar retry de rabbitmq
        }
    }
}
