# cqrs-query-service

Read side of the Product CQRS setup. Serves product read models from MongoDB.

> The read model is not synced with `cqrs-command-service` yet; that will be done via Kafka events in a later step.

## Stack
Java 21 · Spring Boot · Spring Data MongoDB · MapStruct · Lombok

## Run
```bash
docker compose up -d
./mvnw spring-boot:run
```
Service listens on `http://localhost:8080`. CORS allows `http://localhost:5173` (product-fe), configurable via `app.cors.allowed-origins`.

## Endpoints
| Method | Path                 | Description         | Success |
|--------|----------------------|---------------------|---------|
| GET    | `/v1/products`       | List all products   | 200     |
| GET    | `/v1/products/{id}`  | Get product by id   | 200     |

### Seed data manually (until Kafka sync exists)
```bash
docker exec -it cqrs-query-mongo mongosh products_query --eval \
  'db.products.insertOne({_id: NumberLong(1), name: "Mouse", description: "Wireless mouse", price: NumberDecimal("19.99"), stock: 10})'
curl localhost:8080/v1/products
```

## Tests
```bash
./mvnw clean verify
```
