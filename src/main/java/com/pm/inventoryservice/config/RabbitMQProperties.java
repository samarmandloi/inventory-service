package com.pm.inventoryservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inventory.rabbitmq")
public record RabbitMQProperties(
        String variantQueue,
        String retryExchange,
        String retryQueue,
        String retryRoutingKey,
        String dlqExchange,
        String dlqQueue,
        String dlqRoutingKey,
        String productExchange,
        String productRoutingKey,
        int retryTtl
) {
}
