# cqrs-query-service

Read side of the Product CQRS setup. Serves products from a MongoDB read model that is kept up to date by consuming the `catalog.product.events` topic published by `cqrs-command-service`.

## How it works
```
catalog.product.events ─► ProductEventListener ─► ProductProjectionServiceImpl ─► MongoDB (products)
                                   │
                                   └─ malformed, or still failing after 3 retries ─► catalog.product.events.DLT
```

- **Idempotent.** An event is applied only if its `aggregateVersion` is newer than the stored `version`, so duplicates and replays do nothing.
- **Ordered per product.** The message key is the product id, so all events for a product land on one partition and are processed in order.
- **Error handling.** Failing records are retried 3 times, 1 s apart, and then sent to the same partition of `catalog.product.events.DLT`. Unparseable JSON goes to the DLT immediately. Unknown event types and tombstones are logged and skipped.
- **Eventual consistency.** A write on the command service usually shows up here within about a second.

## Stack
Java 21 · Spring Boot 4 · Spring Data MongoDB · Spring for Apache Kafka · MapStruct · Lombok

## Run
Start Kafka ([`cqrs-infra`](../cqrs-infra)) and `cqrs-command-service` first. The command service creates `catalog.product.events`.
```bash
docker compose up -d
./mvnw spring-boot:run
```
The service listens on `http://localhost:8080`. MongoDB runs on host port `27018`. On startup the service creates `catalog.product.events.DLT` if it doesn't exist.

CORS allows `GET` from `http://localhost:5173`, configurable via `app.cors.allowed-origins`.

## Endpoints
| Method | Path                 | Description       | Success |
|--------|----------------------|-------------------|---------|
| GET    | `/v1/products`       | List all products | 200     |
| GET    | `/v1/products/{id}`  | Get product by id | 200     |

## Rebuild the read model
Because the topic is compacted and events carry full state, MongoDB can be rebuilt from Kafka at any time:
```bash
# 1. Stop this service
# 2. Drop the collection
docker exec cqrs-query-mongo mongosh products_query --quiet --eval 'db.products.drop()'
# 3. Rewind the consumer group
docker exec cqrs-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group cqrs-query-service --topic catalog.product.events --reset-offsets --to-earliest --execute
# 4. Start this service again
```

## Tests
```bash
./mvnw clean verify
```
