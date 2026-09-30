package com.pm.inventoryservice.consumer;

import com.pm.inventoryservice.config.RabbitMQConfig;
import com.pm.inventoryservice.dto.VariantEventDto;
import com.pm.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VariantEventConsumerTest {

    private static final String RETRY_COUNT_HEADER =
            "x-inventory-retry-count";

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private VariantEventConsumer variantEventConsumer;


    @Test
    void consume_shouldProcessEventSuccessfully() throws Exception {

        VariantEventDto event =
                new VariantEventDto(
                        "VARIANT_CREATED",
                        java.util.UUID.randomUUID(),
                        "NIKE-AIR-M-RED-001",
                        50
                );

        Message message =
                new Message(
                        "{\"eventType\":\"VARIANT_CREATED\"}"
                                .getBytes()
                );

        when(objectMapper.readValue(
                message.getBody(),
                VariantEventDto.class
        )).thenReturn(event);

        variantEventConsumer.consume(message);

        verify(inventoryService)
                .processVariantEvent(event);

        verifyNoInteractions(rabbitTemplate);
    }


    @Test
    void consume_shouldSendMessageToRetryQueue_whenProcessingFails() throws Exception {

        VariantEventDto event =
                new VariantEventDto(
                        "VARIANT_CREATED",
                        java.util.UUID.randomUUID(),
                        "NIKE-AIR-M-RED-001",
                        50
                );

        Message message =
                new Message(
                        "{\"eventType\":\"VARIANT_CREATED\"}"
                                .getBytes()
                );

        when(objectMapper.readValue(
                message.getBody(),
                VariantEventDto.class
        )).thenReturn(event);

        doThrow(new RuntimeException("Processing failed"))
                .when(inventoryService)
                .processVariantEvent(event);

        variantEventConsumer.consume(message);

        verify(inventoryService)
                .processVariantEvent(event);

        verify(rabbitTemplate)
                .send(
                        eq(RabbitMQConfig.RETRY_EXCHANGE),
                        eq(RabbitMQConfig.RETRY_ROUTING_KEY),
                        any(Message.class)
                );
    }


    @Test
    void consume_shouldSetRetryCountToOne_onFirstFailure()
            throws Exception {

        VariantEventDto event =
                new VariantEventDto(
                        "VARIANT_CREATED",
                        java.util.UUID.randomUUID(),
                        "NIKE-AIR-M-RED-001",
                        50
                );

        Message message =
                new Message(
                        "{\"eventType\":\"VARIANT_CREATED\"}"
                                .getBytes()
                );

        when(objectMapper.readValue(
                message.getBody(),
                VariantEventDto.class
        )).thenReturn(event);

        doThrow(new RuntimeException("Processing failed"))
                .when(inventoryService)
                .processVariantEvent(event);

        variantEventConsumer.consume(message);

        ArgumentCaptor<Message> messageCaptor =
                ArgumentCaptor.forClass(Message.class);

        verify(rabbitTemplate)
                .send(
                        eq(RabbitMQConfig.RETRY_EXCHANGE),
                        eq(RabbitMQConfig.RETRY_ROUTING_KEY),
                        messageCaptor.capture()
                );

        Message retryMessage =
                messageCaptor.getValue();

        Object retryCount =
                retryMessage
                        .getMessageProperties()
                        .getHeaders()
                        .get(RETRY_COUNT_HEADER);

        assertEquals(1, retryCount);
    }


    @Test
    void consume_shouldIncrementRetryCount_whenRetrying()
            throws Exception {

        VariantEventDto event =
                new VariantEventDto(
                        "VARIANT_UPDATED",
                        java.util.UUID.randomUUID(),
                        "NIKE-AIR-M-RED-001",
                        75
                );

        Message message =
                MessageBuilder
                        .withBody(
                                "{\"eventType\":\"VARIANT_UPDATED\"}"
                                        .getBytes()
                        )
                        .setHeader(
                                RETRY_COUNT_HEADER,
                                2
                        )
                        .build();

        when(objectMapper.readValue(
                message.getBody(),
                VariantEventDto.class
        )).thenReturn(event);

        doThrow(new RuntimeException("Processing failed"))
                .when(inventoryService)
                .processVariantEvent(event);

        variantEventConsumer.consume(message);

        ArgumentCaptor<Message> messageCaptor =
                ArgumentCaptor.forClass(Message.class);

        verify(rabbitTemplate)
                .send(
                        eq(RabbitMQConfig.RETRY_EXCHANGE),
                        eq(RabbitMQConfig.RETRY_ROUTING_KEY),
                        messageCaptor.capture()
                );

        Message retryMessage =
                messageCaptor.getValue();

        Object retryCount =
                retryMessage
                        .getMessageProperties()
                        .getHeaders()
                        .get(RETRY_COUNT_HEADER);

        assertEquals(3, retryCount);
    }


    @Test
    void consume_shouldSendMessageToDlq_whenMaxRetriesExceeded()
            throws Exception {

        VariantEventDto event =
                new VariantEventDto(
                        "VARIANT_UPDATED",
                        java.util.UUID.randomUUID(),
                        "NIKE-AIR-M-RED-001",
                        75
                );

        Message message =
                MessageBuilder
                        .withBody(
                                "{\"eventType\":\"VARIANT_UPDATED\"}"
                                        .getBytes()
                        )
                        .setHeader(
                                RETRY_COUNT_HEADER,
                                3
                        )
                        .build();

        when(objectMapper.readValue(
                message.getBody(),
                VariantEventDto.class
        )).thenReturn(event);

        doThrow(new RuntimeException("Processing failed"))
                .when(inventoryService)
                .processVariantEvent(event);

        variantEventConsumer.consume(message);

        verify(rabbitTemplate)
                .send(
                        eq(RabbitMQConfig.DLQ_EXCHANGE),
                        eq(RabbitMQConfig.DLQ_ROUTING_KEY),
                        eq(message)
                );

        verify(rabbitTemplate, never())
                .send(
                        eq(RabbitMQConfig.RETRY_EXCHANGE),
                        eq(RabbitMQConfig.RETRY_ROUTING_KEY),
                        any(Message.class)
                );
    }


    @Test
    void consume_shouldTreatInvalidRetryHeaderAsZero()
            throws Exception {

        VariantEventDto event =
                new VariantEventDto(
                        "VARIANT_CREATED",
                        java.util.UUID.randomUUID(),
                        "NIKE-AIR-M-RED-001",
                        50
                );

        Message message =
                MessageBuilder
                        .withBody(
                                "{\"eventType\":\"VARIANT_CREATED\"}"
                                        .getBytes()
                        )
                        .setHeader(
                                RETRY_COUNT_HEADER,
                                "invalid"
                        )
                        .build();

        when(objectMapper.readValue(
                message.getBody(),
                VariantEventDto.class
        )).thenReturn(event);

        doThrow(new RuntimeException("Processing failed"))
                .when(inventoryService)
                .processVariantEvent(event);

        variantEventConsumer.consume(message);

        ArgumentCaptor<Message> messageCaptor =
                ArgumentCaptor.forClass(Message.class);

        verify(rabbitTemplate)
                .send(
                        eq(RabbitMQConfig.RETRY_EXCHANGE),
                        eq(RabbitMQConfig.RETRY_ROUTING_KEY),
                        messageCaptor.capture()
                );

        Message retryMessage =
                messageCaptor.getValue();

        Object retryCount =
                retryMessage
                        .getMessageProperties()
                        .getHeaders()
                        .get(RETRY_COUNT_HEADER);

        assertEquals(1, retryCount);
    }
}
