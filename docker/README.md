# AppService Infrastructure Plane (Docker)

This directory houses the containerized infrastructure dependencies required for local development and integration testing:

- **PostgreSQL 16**: Multi-tenant relational store (Shared DB, Schema-per-tenant, Dedicated DB).
- **Redis 7**: Distributed caching, token-bucket rate limiting, and Pub/Sub translation cache invalidation.
- **Apache Kafka 3.7**: Clustered event bus in KRaft mode (KRaft controller, no ZooKeeper needed).
- **Kafka UI**: Web console at `http://localhost:8085` to monitor topics, consumers, and lag.
- **Mailpit**: SMTP server (`localhost:1025`) and web mailbox (`http://localhost:8025`) for email verification.

---

## Quick Start

### 1. Start all infrastructure services:
From repository root:
```bash
docker compose -f docker/docker-compose.yml --env-file docker/.env up -d
```
Or directly from the `docker` directory:
```bash
cd docker
docker compose up -d
```

### 2. Check running services & health:
```bash
docker compose -f docker/docker-compose.yml ps
```

### 3. Stop services:
```bash
docker compose -f docker/docker-compose.yml down
```

### 4. Reset all persistent volumes:
```bash
docker compose -f docker/docker-compose.yml down -v
```
