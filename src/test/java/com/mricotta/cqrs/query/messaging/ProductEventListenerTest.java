package com.mricotta.cqrs.query.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import com.mricotta.cqrs.query.event.ProductEvent;
import com.mricotta.cqrs.query.service.ProductProjectionService;
import java.time.Instant;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class ProductEventListenerTest {

    private static final String VALID_EVENT = """
            {"eventId":"6f1c1d2e-8f7a-4a51-9c63-2b0d5e7c9a10","eventType":"ProductUpdated",
             "occurredAt":"2026-09-14T10:00:00Z","aggregateId":1,"aggregateVersion":2,"addedLater":"ignored",
             "payload":{"id":1,"name":"Mouse","description":"Wireless mouse","price":19.99,"stock":10,
                        "createdAt":"2026-09-14T09:00:00Z","updatedAt":"2026-09-14T10:00:00Z"}}
            """;

    @Mock
    private ProductProjectionService productProjectionService;

    @Captor
    private ArgumentCaptor<ProductEvent> eventCaptor;

    private ProductEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new ProductEventListener(JsonMapper.builder().build(), productProjectionService);
    }

    @Test
    void onProductEvent_withValidEvent_delegatesParsedEventToProjection() {
        listener.onProductEvent(consumerRecord(VALID_EVENT));

        then(productProjectionService).should().apply(eventCaptor.capture());
        var event = eventCaptor.getValue();
        assertThat(event.eventId()).isEqualTo(UUID.fromString("6f1c1d2e-8f7a-4a51-9c63-2b0d5e7c9a10"));
        assertThat(event.aggregateId()).isEqualTo(1L);
        assertThat(event.aggregateVersion()).isEqualTo(2L);
        assertThat(event.payload().name()).isEqualTo("Mouse");
        assertThat(event.payload().price()).isEqualByComparingTo("19.99");
        assertThat(event.payload().updatedAt()).isEqualTo(Instant.parse("2026-09-14T10:00:00Z"));
    }

    @Test
    void onProductEvent_withMalformedJson_throwsSoTheErrorHandlerCanDeadLetterIt() {
        assertThatThrownBy(() -> listener.onProductEvent(consumerRecord("{not-json")))
                .isInstanceOf(JacksonException.class);

        verifyNoInteractions(productProjectionService);
    }

    @Test
    void onProductEvent_withUnknownEventType_skipsIt() {
        listener.onProductEvent(consumerRecord(VALID_EVENT.replace("ProductUpdated", "ProductArchived")));

        verifyNoInteractions(productProjectionService);
    }

    @Test
    void onProductEvent_withTombstone_skipsIt() {
        listener.onProductEvent(consumerRecord(null));

        verifyNoInteractions(productProjectionService);
    }

    private static ConsumerRecord<String, String> consumerRecord(String value) {
        return new ConsumerRecord<>("catalog.product.events", 0, 0L, "1", value);
    }
}
