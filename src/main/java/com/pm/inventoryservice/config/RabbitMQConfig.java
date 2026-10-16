package com.pm.inventoryservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    @Bean
    public Queue variantQueue(RabbitMQProperties properties) {
        return new Queue(properties.variantQueue(), true);
    }

    @Bean
    public DirectExchange retryExchange(RabbitMQProperties properties) {
        return new DirectExchange(properties.retryExchange());
    }

    @Bean
    public Queue retryQueue(RabbitMQProperties properties) {
        Map<String, Object> arguments = new HashMap<>();

        arguments.put("x-message-ttl", properties.retryTtl());
        arguments.put("x-dead-letter-exchange", properties.productExchange());
        arguments.put(
                "x-dead-letter-routing-key",
                properties.productRoutingKey()
        );

        return new Queue(
                properties.retryQueue(),
                true,
                false,
                false,
                arguments
        );
    }

    @Bean
    public Binding retryBinding(
            Queue retryQueue,
            DirectExchange retryExchange,
            RabbitMQProperties properties) {

        return BindingBuilder
                .bind(retryQueue)
                .to(retryExchange)
                .with(properties.retryRoutingKey());
    }

    @Bean
    public DirectExchange dlqExchange(RabbitMQProperties properties) {
        return new DirectExchange(properties.dlqExchange());
    }

    @Bean
    public Queue dlqQueue(RabbitMQProperties properties) {
        return new Queue(properties.dlqQueue(), true);
    }

    @Bean
    public Binding dlqBinding(
            Queue dlqQueue,
            DirectExchange dlqExchange,
            RabbitMQProperties properties) {

        return BindingBuilder
                .bind(dlqQueue)
                .to(dlqExchange)
                .with(properties.dlqRoutingKey());
    }
}
