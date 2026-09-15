package com.mricotta.cqrs.query.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

@Configuration
public class KafkaConsumerConfig {

    private static final int DLT_PARTITIONS = 3;
    private static final long RETRY_INTERVAL_MS = 1_000L;
    private static final long MAX_RETRIES = 3L;

    @Bean
    public NewTopic productEventsDeadLetterTopic(
            @Value("${app.kafka.topics.product-events-dlt}") String dltTopic,
            @Value("${app.kafka.topics.replicas:1}") int replicas) {
        return TopicBuilder.name(dltTopic).partitions(DLT_PARTITIONS).replicas(replicas).build();
    }

    /**
     * Retries transient failures (e.g. Mongo unavailable), then dead-letters the record to the same
     * partition of the DLT. Unparseable payloads can never succeed, so they skip the retries.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topics.product-events-dlt}") String dltTopic) {
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate, (consumerRecord, ex) -> new TopicPartition(dltTopic, consumerRecord.partition()));
        var errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES));
        errorHandler.addNotRetryableExceptions(JacksonException.class);
        return errorHandler;
    }
}
