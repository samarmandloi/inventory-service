package com.pm.inventoryservice.consumer;

import com.pm.inventoryservice.config.RabbitMQConfig;
import com.pm.inventoryservice.dto.VariantEventDto;
import com.pm.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class VariantEventConsumer {

    private static final String RETRY_COUNT_HEADER =
            "x-inventory-retry-count";

    /*
     * Maximum number of retries after the initial attempt.
     */
    private static final int MAX_RETRIES = 3;

    /*
     * Retry delay is configured in RabbitMQConfig:
     * 5 seconds.
     */

    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.VARIANT_QUEUE)
    public void consume(Message message) {

        try {

            VariantEventDto event =
                    objectMapper.readValue(
                            message.getBody(),
                            VariantEventDto.class
                    );

            log.info(
                    "Received variant event: eventType={}, variantId={}, sku={}, quantity={}",
                    event.eventType(),
                    event.variantId(),
                    event.sku(),
                    event.quantity()
            );

            inventoryService.processVariantEvent(event);

            log.info(
                    "Inventory processed successfully for variantId={}",
                    event.variantId()
            );

        } catch (Exception e) {

            handleFailure(message, e);
        }
    }

    private void handleFailure(
            Message message,
            Exception exception) {

        int retryCount = getRetryCount(message);

        if (retryCount < MAX_RETRIES) {

            int nextRetryCount = retryCount + 1;

            Message retryMessage =
                    MessageBuilder
                            .fromMessage(message)
                            .setHeader(
                                    RETRY_COUNT_HEADER,
                                    nextRetryCount
                            )
                            .build();

            rabbitTemplate.send(
                    RabbitMQConfig.RETRY_EXCHANGE,
                    RabbitMQConfig.RETRY_ROUTING_KEY,
                    retryMessage
            );

            log.warn(
                    "Variant event processing failed. " +
                            "Sending message to retry queue. " +
                            "retry={}/{}",
                    nextRetryCount,
                    MAX_RETRIES,
                    exception
            );

        } else {

            rabbitTemplate.send(
                    RabbitMQConfig.DLQ_EXCHANGE,
                    RabbitMQConfig.DLQ_ROUTING_KEY,
                    message
            );

            log.error(
                    "Variant event failed after {} retries. " +
                            "Moving message to DLQ.",
                    MAX_RETRIES,
                    exception
            );
        }
    }

    private int getRetryCount(Message message) {

        Object value =
                message.getMessageProperties()
                        .getHeaders()
                        .get(RETRY_COUNT_HEADER);

        if (value == null) {
            return 0;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(
                    value.toString()
            );
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
