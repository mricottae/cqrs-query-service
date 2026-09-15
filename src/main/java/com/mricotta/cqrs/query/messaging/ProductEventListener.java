package com.mricotta.cqrs.query.messaging;

import com.mricotta.cqrs.query.event.ProductEvent;
import com.mricotta.cqrs.query.event.ProductEventType;
import com.mricotta.cqrs.query.service.ProductProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final JsonMapper jsonMapper;
    private final ProductProjectionService productProjectionService;

    /**
     * Malformed payloads throw, so the error handler sends them to the DLT instead of blocking the
     * partition.
     */
    @KafkaListener(topics = "${app.kafka.topics.product-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void onProductEvent(ConsumerRecord<String, String> consumerRecord) {
        if (consumerRecord.value() == null) {
            log.warn("Skipping tombstone for key {} at {}-{}@{}; deletes are not supported yet",
                    consumerRecord.key(), consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
            return;
        }

        var event = jsonMapper.readValue(consumerRecord.value(), ProductEvent.class);
        ProductEventType.fromWireName(event.eventType()).ifPresentOrElse(
                type -> productProjectionService.apply(event),
                () -> log.warn("Skipping unknown event type '{}' (eventId={}, key={})",
                        event.eventType(), event.eventId(), consumerRecord.key()));
    }
}
