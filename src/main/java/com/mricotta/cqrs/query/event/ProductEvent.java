package com.mricotta.cqrs.query.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

/**
 * Consumer-side copy of the product event contract published by cqrs-command-service. Unknown fields
 * are ignored so the producer can add fields without breaking this service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long aggregateId,
        Long aggregateVersion,
        ProductPayload payload) {
}
