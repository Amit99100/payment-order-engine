# High-Concurrency Order & Payment Engine

Java 17 · Spring Boot · PostgreSQL · Redis · Apache Kafka · Docker Compose

A backend engineering project demonstrating inventory concurrency, payment idempotency,
transactional outbox messaging, Kafka consumers, and failure handling.

## Architecture responsibilities

| Technology | Actual responsibility in this project |
|---|---|
| Java 17 / Spring Boot | REST API, domain rules, transactions, scheduled workers |
| PostgreSQL | Durable orders, inventory, payments, idempotency records, outbox |
| Redis | 24-hour fast cache for previously processed idempotency keys |
| Kafka | Durable asynchronous event transport |
| Docker Compose | Starts PostgreSQL, Redis and Kafka locally |
| JUnit 5 | Unit tests |

## Requirements
- JDK 17
- Maven 3.8+
- Docker Desktop (or Docker Engine + Compose plugin)

## Start infrastructure
From the project root:

```bash
docker compose up -d
docker compose ps
```

Wait until PostgreSQL, Redis and Kafka are healthy.

## Run application
```bash
mvn clean test
mvn spring-boot:run
```

The application listens on `http://localhost:8080`.

Stop infrastructure:
```bash
docker compose down
```

Delete local database/cache/broker data (destructive):
```bash
docker compose down -v
```

## API walkthrough

### 1. Create product
`POST /api/products`
```json
{"sku":"KB-001","name":"Mechanical Keyboard","price":2499.00,"availableQuantity":5}
```

### 2. Create order
`POST /api/orders`
```json
{"customerEmail":"buyer@example.com","items":[{"productId":1,"quantity":2}]}
```
The service locks inventory rows, reserves stock and creates the order in one transaction.
An `OrderCreated` event is written to the outbox in the same DB transaction.

### 3. Pay
`POST /api/orders/1/payments`
Header: `Idempotency-Key: checkout-unique-001`

The first request calls the mock gateway and stores the durable result in PostgreSQL.
After commit, the result is cached in Redis for 24 hours. Repeating the same key returns
the original payment instead of making another gateway call.

### 4. Read order
`GET /api/orders/1`

### 5. Restock
`POST /api/products/1/restock?quantity=3`

## Verify Kafka
After creating an order or successful payment, the scheduled outbox publisher sends
events to `payment-order-events`. The `OrderEventConsumer` listens and logs received events.

View logs:
```bash
docker logs -f payment-engine-kafka
```
Application logs show `Published outbox event` and `KAFKA_EVENT_RECEIVED`.

You can also open a Kafka console consumer inside the broker:
```bash
docker exec -it payment-engine-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic payment-order-events --from-beginning
```

## Verify Redis
After a payment, inspect the idempotency key:
```bash
docker exec -it payment-engine-redis redis-cli
KEYS payment:idempotency:*
GET payment:idempotency:checkout-unique-001
TTL payment:idempotency:checkout-unique-001
```
Avoid `KEYS` on large production Redis instances; use `SCAN` there.

## Important engineering details

1. **Inventory locking**: PostgreSQL row-level `PESSIMISTIC_WRITE` locks coordinate concurrent
   app instances. Product IDs are locked in sorted order to reduce deadlock risk.
2. **Redis is a cache, not the authority**: PostgreSQL unique constraints and records provide
   durable idempotency. Redis may evict keys or restart.
3. **Cache-after-commit**: Redis is populated only after the DB transaction commits, preventing
   cache entries that point to rolled-back DB writes.
4. **Transactional Outbox**: business state and event row commit together in PostgreSQL.
5. **Kafka delivery**: publisher waits for broker acknowledgement before marking an outbox row
   published. This is at-least-once delivery. A crash between Kafka acknowledgement and DB commit
   can duplicate an event; consumers must deduplicate by event ID.
6. **Circuit breaker**: the included small Java implementation is process-local and educational.
   For production multi-instance use, adopt a mature library such as Resilience4j.
7. **Payment uncertainty**: a real gateway can accept a charge while the caller times out.
   Production must use provider-side idempotency and reconciliation; never blindly retry.
8. **External calls and transactions**: this learning implementation calls a mock gateway in a
   transaction. A production flow should use a payment-attempt state machine, short DB transactions,
   asynchronous provider calls and reconciliation.
9. **Inventory compensation**: stock is reserved when the order is created. A production workflow
   must release stock after payment failure/cancellation through an idempotent compensation operation.
10. **Schema management**: `ddl-auto: update` is for local learning. Use Flyway/Liquibase and
    `ddl-auto: validate` for a production deployment.

## Interview exercises
- Write an integration test where 20 buyers compete for the last 3 units.
- Stop Kafka, create an order, restart Kafka, and observe outbox retry.
- Stop Redis and verify PostgreSQL fallback still prevents duplicate charges.
- Deliver the same Kafka event twice and implement consumer-side deduplication.
- Implement inventory release after failed payment.
- Move payment gateway calls outside DB transactions.
- Add Flyway migrations, metrics, tracing and authentication.
