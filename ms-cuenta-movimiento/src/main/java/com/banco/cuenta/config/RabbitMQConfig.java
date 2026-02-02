package com.banco.cuenta.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.cliente}")
    private String clienteExchange;

    @Value("${rabbitmq.queue.cliente-creado}")
    private String clienteCreadoQueue;

    @Value("${rabbitmq.queue.cliente-actualizado}")
    private String clienteActualizadoQueue;

    @Value("${rabbitmq.routing-key.cliente-creado}")
    private String clienteCreadoRoutingKey;

    @Value("${rabbitmq.routing-key.cliente-actualizado}")
    private String clienteActualizadoRoutingKey;

   
    @Bean
    public TopicExchange clienteExchange() {
        return new TopicExchange(clienteExchange);
    }

    @Bean
    public Queue clienteCreadoQueue() {
        return new Queue(clienteCreadoQueue, true); 
    }

   
    @Bean
    public Queue clienteActualizadoQueue() {
        return new Queue(clienteActualizadoQueue, true); 
    }

   
    @Bean
    public Binding clienteCreadoBinding(Queue clienteCreadoQueue, TopicExchange clienteExchange) {
        return BindingBuilder
                .bind(clienteCreadoQueue)
                .to(clienteExchange)
                .with(clienteCreadoRoutingKey);
    }

 
    @Bean
    public Binding clienteActualizadoBinding(Queue clienteActualizadoQueue, TopicExchange clienteExchange) {
        return BindingBuilder
                .bind(clienteActualizadoQueue)
                .to(clienteExchange)
                .with(clienteActualizadoRoutingKey);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

 
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

   
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}
