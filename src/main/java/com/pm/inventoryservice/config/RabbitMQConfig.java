package com.pm.inventoryservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    /*
     * Existing queue.
     *
     * Product Service already publishes variant events
     * to this queue through product.exchange.
     */
    public static final String VARIANT_QUEUE =
            "inventory.variant.queue";

    /*
     * Retry infrastructure.
     */
    public static final String RETRY_EXCHANGE =
            "inventory.retry.exchange";

    public static final String RETRY_QUEUE =
            "inventory.variant.retry.queue";

    public static final String RETRY_ROUTING_KEY =
            "variant.retry";

    /*
     * Final failed-message infrastructure.
     *
     * This is effectively our DLQ.
     */
    public static final String DLQ_EXCHANGE =
            "inventory.dlq.exchange";

    public static final String DLQ_QUEUE =
            "inventory.variant.dlq";

    public static final String DLQ_ROUTING_KEY =
            "variant.dlq";

    /*
     * Existing Product Service exchange.
     *
     * Retry messages will eventually be sent back here
     * after the retry queue TTL expires.
     */
    public static final String PRODUCT_EXCHANGE =
            "product.exchange";

    public static final String PRODUCT_ROUTING_KEY =
            "variant.event";

    /*
     * Existing normal queue.
     *
     * IMPORTANT:
     * Do not add queue arguments here.
     *
     * Product Service also declares this queue, so both
     * services must declare it with the same configuration.
     */
    @Bean
    public Queue variantQueue() {
        return new Queue(VARIANT_QUEUE, true);
    }

    /*
     * Retry exchange.
     */
    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(RETRY_EXCHANGE);
    }

    /*
     * Retry queue.
     *
     * Message stays here for 5 seconds.
     *
     * After 5 seconds RabbitMQ sends it back to:
     *
     * product.exchange
     *       |
     *       v
     * inventory.variant.queue
     */
    @Bean
    public Queue retryQueue() {

        Map<String, Object> arguments =
                new HashMap<>();

        arguments.put(
                "x-message-ttl",
                5000
        );

        arguments.put(
                "x-dead-letter-exchange",
                PRODUCT_EXCHANGE
        );

        arguments.put(
                "x-dead-letter-routing-key",
                PRODUCT_ROUTING_KEY
        );

        return new Queue(
                RETRY_QUEUE,
                true,
                false,
                false,
                arguments
        );
    }

    /*
     * Retry exchange -> Retry queue.
     */
    @Bean
    public Binding retryBinding(
            Queue retryQueue,
            DirectExchange retryExchange) {

        return BindingBuilder
                .bind(retryQueue)
                .to(retryExchange)
                .with(RETRY_ROUTING_KEY);
    }

    /*
     * Final DLQ exchange.
     */
    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    /*
     * Final failed-message queue.
     */
    @Bean
    public Queue dlqQueue() {
        return new Queue(DLQ_QUEUE, true);
    }

    /*
     * DLQ exchange -> DLQ queue.
     */
    @Bean
    public Binding dlqBinding(
            Queue dlqQueue,
            DirectExchange dlqExchange) {

        return BindingBuilder
                .bind(dlqQueue)
                .to(dlqExchange)
                .with(DLQ_ROUTING_KEY);
    }
}
