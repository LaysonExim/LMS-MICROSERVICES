# NextGen Loan Management Platform (NGLMP)

<a href="https://github.com/LaysonExim/LMS-MICROSERVICES"><img src="https://img.shields.io/badge/GitHub-Repo-blue?logo=github" alt="GitHub Repo"/></a>
<img src="https://img.shields.io/badge/Java-21-orange?logo=java" alt="Java 21"/>
<img src="https://img.shields.io/badge/Spring%20Boot-3.3.0-green?logo=spring" alt="Spring Boot 3.3.0"/>
<img src="https://img.shields.io/badge/Spring%20Cloud-2023.0.2-brightgreen?logo=spring" alt="Spring Cloud"/>
<img src="https://img.shields.io/badge/Kafka-7.3.2-orange?logo=apache-kafka" alt="Apache Kafka"/>

A production-grade, event-driven microservices platform for loan management, built for banking-scale requirements. The platform implements a complete loan lifecycle from customer onboarding through loan origination, disbursement, repayment, and closure — all while maintaining a full audit trail for regulatory compliance.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Services Reference](#services-reference)
5. [API Endpoints](#api-endpoints)
6. [Swagger UI & OpenAPI](#swagger-ui--openapi)
7. [Event-Driven Communication](#event-driven-communication)
8. [Loan Lifecycle](#loan-lifecycle)
9. [Quick Start](#quick-start)
10. [Environment Variables](#environment-variables)
11. [Testing](#testing)
12. [Design Principles](#design-principles)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT APPLICATIONS                                │
│                  (Web, Mobile, Third-party integrations)                     │
└──────────────────────────────┬──────────────────────────────────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │     API Gateway      │  Port: 8080
                    │  Spring Cloud Gateway│
                    │  ── Swagger UI       │
                    │  ── CORS            │
                    │  ── Rate Limiting   │
                    │  ── Resilience4j    │
                    └──────────┬──────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
┌─────────▼─────────┐  ┌───────▼───────┐  ┌─────────▼─────────┐
│  Eureka Server    │  │ Config Server │  │   Kafka Broker    │
│  (Discovery)      │  │  (Centralize) │  │  (Messaging)      │
│  Port: 8761       │  │  Port: 8888   │  │  Port: 9092       │
└─────────┬─────────┘  └───────┬───────┘  └─────────┬─────────┘
          │                    │                    │
          │              ┌─────▼─────┐              │
          │              │  Config   │              │
          │              │  Repo     │              │
          │              │(file-based)│              │
          │              └─────┬─────┘              │
          │                    │                    │
          │  Service Registration & Discovery      │
          │                    │                    │
┌─────────┴─────────┐  ┌───────▼───────┐  ┌─────────▼─────────┐
│ Customer Service  │  │   Loan Service │  │   Kafka Event     │
│ Port: 8081        │  │  Port: 8082    │  │   Consumers:      │
│                   │  │                │  │  Audit (8087)    │
│ REST + Eureka     │  │ REST + Kafka   │  │  Reporting (8085) │
│ + Swagger         │  │ + Swagger      │  │  Monitoring (8088)│
└─────────┬─────────┘  └───────┬───────┘  │  Notification(8086)│
          │                    │          └─────────┬─────────┘
          │          ┌──────────┘                    │
          │          │                               │
┌─────────▼────┐  ┌──▼────────────┐  ┌───────────────▼────────┐
│Limit Service │  │Collateral Svc │  │    Databases (MSSQL)     │
│ Port: 8083   │  │ Port: 8084    │  │  customer-db (1433)      │
└──────────────┘  └───────────────┘  │  loan-db (1434)          │
                                      │  limit-db (1435)         │
                                      │  collateral-db (1436)   │
                                      │  audit-db (1437)        │
                                      │  notification-db (1438)  │
                                      └─────────────────────────┘
```

### Key Patterns

| Pattern | Where | Purpose |
|---------|-------|---------|
| **API Gateway** | `api-gateway:8080` | Single entry point, routing, CORS, resilience |
| **Service Discovery** | `discovery-server:8761` | Eureka registry, dynamic service location |
| **Centralized Config** | `config-server:8888` | Git-backed configuration, externalized |
| **Event-Driven** | `kafka:9092` | Async communication, audit trail, reporting |
| **Circuit Breaker** | `loan-service` | Resilience4j — circuit breaker, retry, timeout |
| **Saga Pattern** | `loan-service` | Distributed transactions with compensating actions |
| **DTO Separation** | All services | Request/Response/Internal DTOs per service boundary |
| **Soft Delete** | `customer-service` | Customer deactivation instead of hard delete |

---

## Technology Stack

| Layer | Technology |
|-------|------------|
| **Java Version** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.3.0 |
| **Cloud** | Spring Cloud 2023.0.2 |
| **Service Discovery** | Netflix Eureka Server / Client |
| **Config Server** | Spring Cloud Config Server |
| **API Gateway** | Spring Cloud Gateway (reactive) |
| **Messaging** | Apache Kafka 7.3.2 (Confluent Platform) |
| **Database** | Microsoft SQL Server 2022 (Developer Edition) |
| **ORM** | Spring Data JPA / Hibernate |
| **Migrations** | Flyway (with `clean-disabled` in production) |
| **Resilience** | Resilience4j (circuit breaker, retry, timeout, bulkhead) |
| **API Docs** | SpringDoc OpenAPI 2.5.0 (Swagger UI) |
| **Build** | Maven (multi-module) |
| **Containerization** | Docker, Docker Compose |
| **Testing** | JUnit 5, Testcontainers, Mockito |
| **Observability** | Micrometer, Spring Boot Actuator |
| **Code Quality** | Lombok (boilerplate), MapStruct (type-safe mapping) |

---

## Project Structure

```
LMS-MICROSERVICES/
├── pom.xml                              # Parent POM (nglmp-parent)
├── docker-compose.yml                   # Full platform orchestration
├── Dockerfile.template                  # Multi-stage Dockerfile template
├── tests/
│   └── end-to-end-test.sh               # Complete E2E integration test
├── config-repo/                         # Centralized configuration (Config Server)
│   ├── application.yml                  # Default config for all services
│   ├── application-dev.yml              # Development overrides
│   ├── application-production.yml       # Production overrides
│   └── customer-service.yml             # Customer Service specific config
├── discovery-server/                    # Eureka Service Registry
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/java/.../ConfigServerApplication.java
│   └── src/main/resources/application.yaml
├── config-server/                       # Spring Cloud Config Server
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/java/.../ConfigServerApplication.java
│   └── src/main/resources/application.yaml
├── API-Gateway/                         # Spring Cloud Gateway
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/java/.../ApiGatewayApplication.java
│   ├── src/main/java/.../controller/OpenApiAggregatorController.java
│   └── src/main/resources/{application.yaml, bootstrap.yml}
├── customer-service/                    # Customer Management
│   ├── pom.xml, README.md
│   ├── Dockerfile
│   ├── src/main/java/.../controller/    # CustomerController, ConfigController, HealthController
│   ├── src/main/java/.../service/       # CustomerService
│   ├── src/main/java/.../entity/        # Customer, Address
│   ├── src/main/java/.../dto/           # Request/Response/Summary/Internal DTOs
│   ├── src/main/java/.../mapper/        # CustomerMapper, AddressMapper
│   ├── src/main/java/.../config/        # CustomerProperties
│   ├── src/main/resources/{application.yaml, bootstrap.yml}
│   ├── src/main/resources/db/migration/ # V1__customer_tables.sql, V2__indexes.sql
│   └── src/test/                        # Integration tests with Testcontainers
├── loan-service/                        # Loan Origination & Management
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/java/.../controller/    # LoanController, CompleteLoanController, SagaController
│   ├── src/main/java/.../service/       # LoanService (full loan lifecycle)
│   ├── src/main/java/.../entity/        # LoanApplication, LoanSchedule, LoanRepayment
│   ├── src/main/java/.../model/         # LoanState (state machine)
│   ├── src/main/java/.../saga/          # LoanApplicationSaga
│   ├── src/main/java/.../event/         # LoanEvent, LoanEventPublisher
│   ├── src/main/java/.../client/        # CustomerServiceClient, CreditLimitClient, CollateralClient
│   ├── src/main/java/.../config/        # KafkaProducerConfig, WebClientConfig
│   ├── src/main/resources/{application.yaml, bootstrap.yml}
│   ├── src/main/resources/db/migration/
│   └── src/test/
├── limit-service/                       # Credit Limit Management
│   ├── pom.xml, Dockerfile
│   ├── src/main/java/.../controller/    # CreditLimitController
│   ├── src/main/java/.../service/       # CreditLimitService
│   ├── src/main/java/.../entity/        # CustomerCreditLimit, LimitReservation, ProductCreditLimit
│   ├── src/main/resources/{application.yaml}
│   ├── src/main/resources/db/migration/
│   └── src/test/
├── collateral-service/                  # Collateral Management
│   ├── pom.xml, Dockerfile
│   ├── src/main/java/.../controller/    # CollateralController
│   ├── src/main/java/.../service/       # CollateralService
│   ├── src/main/java/.../entity/        # CollateralAsset, CollateralValuation
│   ├── src/main/resources/{application.yaml}
│   ├── src/main/resources/db/migration/
│   └── src/test/
├── reporting-service/                   # Business Reports
│   ├── pom.xml, Dockerfile
│   ├── src/main/java/.../consumer/      # LoanEventConsumer (Kafka)
│   ├── src/main/java/.../controller/    # ReportingController
│   ├── src/main/java/.../model/         # ReportSummary, LoanReportData
│   └── src/main/resources/{application.yaml}
├── notification-service/                # Email/SMS Notifications
│   ├── pom.xml, Dockerfile
│   ├── src/main/java/.../consumer/      # LoanEventConsumer (Kafka)
│   ├── src/main/java/.../service/       # EmailService, SmsService
│   ├── src/main/java/.../dto/           # NotificationRequest
│   ├── src/main/resources/{application.yaml}
│   └── src/main/resources/db/migration/
├── audit-service/                       # Audit Trail & Analytics
│   ├── pom.xml, Dockerfile
│   ├── src/main/java/.../consumer/      # LoanEventConsumer (Kafka)
│   ├── src/main/java/.../controller/    # AuditController, AuditAnalyticsController
│   ├── src/main/java/.../entity/        # AuditEvent
│   ├── src/main/java/.../service/       # AuditAnalyticsService
│   ├── src/main/resources/{application.yaml}
│   └── src/main/resources/db/migration/
└── monitoring-service/                  # Event Flow Monitoring
    ├── pom.xml, Dockerfile
    ├── src/main/java/.../controller/    # MonitoringController
    ├── src/main/java/.../service/       # EventMetricsService (Micrometer + Kafka)
    └── src/main/resources/{application.yaml}
```

---

## Services Reference

| Service | Port | Docker Container | Database | DB Port | Description |
|---------|------|-----------------|----------|---------|-------------|
| **Discovery Server** | `8761` | `discovery-server` | — | — | Netflix Eureka service registry |
| **Config Server** | `8888` | `config-server` | — | — | Spring Cloud Config Server (file-based Git) |
| **API Gateway** | `8080` | `api-gateway` | — | — | Spring Cloud Gateway — single entry point |
| **Customer Service** | `8081` | `customer-service` | `customer_db` | `1433` | Customer registration, profiles, address mgmt |
| **Loan Service** | `8082` | `loan-service` | `loan_db` | `1434` | Loan origination, state management, amortization |
| **Limit Service** | `8083` | `limit-service` | `limit_db` | `1435` | Credit limit checks, reservations |
| **Collateral Service** | `8084` | `collateral-service` | `collateral_db` | `1436` | Collateral asset registration, validation |
| **Reporting Service** | `8085` | `reporting-service` | — (in-memory) | — | Real-time loan reporting from Kafka events |
| **Notification Service** | `8086` | `notification-service` | `notification_db` | `1438` | Email/SMS notifications via Kafka |
| **Audit Service** | `8087` | `audit-service` | `audit_db` | `1437` | Immutable audit trail for compliance |
| **Monitoring Service** | `8088` | `monitoring-service` | — (in-memory) | — | Event flow metrics via Micrometer + Kafka |
| **Kafka Broker** | `9092` | `kafka` | — | — | Event streaming (topic: `loan-events`) |
| **Kafka UI** | `8090` | `kafka-ui` | — | — | Web UI for Kafka monitoring |
| **Zookeeper** | `2181` | `zookeeper` | — | — | Kafka dependency |

### Service URLs Summary

| Service | Base URL |
|---------|----------|
| API Gateway | `http://localhost:8080` |
| Discovery Server | `http://localhost:8761` |
| Config Server | `http://localhost:8888` |
| Customer Service | `http://localhost:8081` |
| Loan Service | `http://localhost:8082` |
| Limit Service | `http://localhost:8083` |
| Collateral Service | `http://localhost:8084` |
| Reporting Service | `http://localhost:8085` |
| Notification Service | `http://localhost:8086` |
| Audit Service | `http://localhost:8087` |
| Monitoring Service | `http://localhost:8088` |
| Kafka | `localhost:9092` |
| Kafka UI | `http://localhost:8090` |

---

## API Endpoints

All customer-facing APIs are accessible through the **API Gateway** at `http://localhost:8080`.

### Customer Service — `/api/v1/customers/**`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/customers` | Register a new customer |
| `GET` | `/api/v1/customers` | List customers (pagination, filtering, sorting) |
| `GET` | `/api/v1/customers/{customerNumber}` | Get customer by number |
| `GET` | `/api/v1/customers/internal/{customerNumber}` | Get customer for internal service use |
| `PUT` | `/api/v1/customers/{customerNumber}` | Update customer (full) |
| `DELETE` | `/api/v1/customers/{customerNumber}` | Deactivate customer (soft delete) |
| `PATCH` | `/api/v1/customers/{customerNumber}/status` | Update customer status |
| `POST` | `/api/v1/customers/search` | Advanced search (JSON criteria) |
| `GET` | `/api/v1/customers/{customerNumber}/addresses` | Get customer addresses |
| `POST` | `/api/v1/customers/{customerNumber}/addresses` | Add address |
| `PUT` | `/api/v1/customers/{customerNumber}/addresses/{addressId}` | Update address |
| `DELETE` | `/api/v1/customers/{customerNumber}/addresses/{addressId}` | Delete address |

### Customer Service — Config & Health

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/config` | View current configuration |
| `POST` | `/api/v1/config/refresh` | Refresh config from Config Server |
| `GET` | `/api/v1/health` | Service health check |
| `GET` | `/actuator/health` | Spring Boot Actuator health |
| `GET` | `/actuator/metrics` | Service metrics |

### Loan Service — `/api/v1/loans/**`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/loans` | Apply for a new loan |
| `GET` | `/api/v1/loans/{loanNumber}` | Get loan by number |
| `GET` | `/api/v1/loans/customer/{customerNumber}` | Get loans by customer |
| `GET` | `/api/v1/loans/customer/{customerNumber}/page` | Get loans by customer (paginated) |
| `GET` | `/api/v1/loans` | Get all loans (paginated) |
| `GET` | `/api/v1/loans/status/{status}` | Get loans by status |
| `PATCH` | `/api/v1/loans/{loanNumber}/status` | Transition loan status |
| `GET` | `/api/v1/loans/{loanNumber}/schedule` | Get amortization schedule |
| `POST` | `/api/v1/loans/{loanNumber}/repayments` | Record a repayment |
| `GET` | `/api/v1/loans/{loanNumber}/repayments` | Get all repayments |

### Loan Service — Complete Flow & Saga

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/loans/complete` | Apply for loan with full integration (customer + credit + collateral) |
| `POST` | `/api/v1/saga/loans` | Apply for loan via saga pattern (202 Accepted, async) |
| `GET` | `/api/v1/saga/loans/{sagaId}` | Get saga state (monitoring) |

### Limit Service — `/api/v1/credit-limits/**`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/credit-limits/check` | Check and reserve credit |
| `POST` | `/api/v1/credit-limits/reservations/{reservationId}/confirm` | Confirm reservation (permanent) |
| `POST` | `/api/v1/credit-limits/reservations/{reservationId}/release` | Release reservation |
| `GET` | `/api/v1/credit-limits/reservations/{reservationId}` | Get reservation details |
| `GET` | `/api/v1/credit-limits/customer/{customerNumber}` | Get customer's credit limit |

### Collateral Service — `/api/v1/collateral/**`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/collateral` | Register a collateral asset |
| `POST` | `/api/v1/collateral/{collateralReference}/link` | Link collateral to a loan |
| `POST` | `/api/v1/collateral/{collateralReference}/release` | Release collateral from a loan |
| `GET` | `/api/v1/collateral/validate` | Validate collateral for a loan |
| `GET` | `/api/v1/collateral/{collateralReference}` | Get collateral by reference |
| `GET` | `/api/v1/collateral/customer/{customerNumber}` | Get all collateral for a customer |
| `GET` | `/api/v1/collateral/loan/{loanNumber}` | Get collateral linked to a loan |
| `POST` | `/api/v1/collateral/loan/{loanNumber}/link` | Link collateral to loan |
| `POST` | `/api/v1/collateral/link-all/{loanNumber}` | Link all active collateral to loan |

### Audit Service — `/api/v1/audit/**`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/audit/loan/{loanNumber}` | Get audit events for a loan |
| `GET` | `/api/v1/audit/customer/{customerNumber}` | Get audit events for a customer |
| `GET` | `/api/v1/audit/type/{eventType}` | Get audit events by type |
| `GET` | `/api/v1/audit/date-range` | Get audit events by date range |

### Audit Service — Analytics

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/audit/analytics/statistics` | Get audit statistics for a date range |

### Reporting Service — `/api/v1/reporting/**`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/reporting/summary` | Get aggregate report (total loans, amount, by type/status) |

### Notification Service

| Component | Details |
|-----------|---------|
| **Kafka Listener** | Topic: `loan-events`, Group: `notification-service-group` |
| **Features** | Email (primary + welcome templates), SMS (fallback), retry logic (max 3), dead letter handling |
| **HTTP API** | No REST endpoints — event-driven only |

### Monitoring Service — `/api/v1/monitoring/**`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/monitoring/events` | Get event statistics (counts, errors, health, throughput) |

| Component | Details |
|-----------|---------|
| **Kafka Listener** | Topic: `loan-events`, Group: `monitoring-service-group` |
| **Metrics** | Micrometer counters for `events.processed.total` and `events.errored.total` |

---

## Swagger UI & OpenAPI

### Aggregated Swagger UI (Recommended)

All microservice APIs are aggregated into a **single unified Swagger UI** via the API Gateway:

> **http://localhost:8080/swagger-ui.html**

This provides a single interface to explore and test every API endpoint across all microservices without needing to know individual service ports.

The aggregated OpenAPI spec is available at:
> **http://localhost:8080/v3/api-docs/merged**

### Per-Service Swagger UI

Each service also exposes its own Swagger UI for direct access:

| Service | Swagger UI URL | OpenAPI Spec URL |
|---------|---------------|-----------------|
| Customer Service | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |
| Loan Service | `http://localhost:8082/swagger-ui.html` | `http://localhost:8082/v3/api-docs` |
| Limit Service | `http://localhost:8083/swagger-ui.html` | `http://localhost:8083/v3/api-docs` |
| Collateral Service | `http://localhost:8084/swagger-ui.html` | `http://localhost:8084/v3/api-docs` |
| Reporting Service | `http://localhost:8085/swagger-ui.html` | `http://localhost:8085/v3/api-docs` |
| Notification Service | `http://localhost:8086/swagger-ui.html` | `http://localhost:8086/v3/api-docs` |
| Audit Service | `http://localhost:8087/swagger-ui.html` | `http://localhost:8087/v3/api-docs` |
| Monitoring Service | `http://localhost:8088/swagger-ui.html` | `http://localhost:8088/v3/api-docs` |

### Gateway API Docs Routes

The Gateway also proxies individual service OpenAPI specs:

| Route | Target Service |
|-------|---------------|
| `http://localhost:8080/services/customer-service/v3/api-docs/**` | Customer Service |
| `http://localhost:8080/services/loan-service/v3/api-docs/**` | Loan Service |
| `http://localhost:8080/services/limit-service/v3/api-docs/**` | Limit Service |
| `http://localhost:8080/services/collateral-service/v3/api-docs/**` | Collateral Service |
| `http://localhost:8080/services/reporting-service/v3/api-docs/**` | Reporting Service |
| `http://localhost:8080/services/notification-service/v3/api-docs/**` | Notification Service |
| `http://localhost:8080/services/audit-service/v3/api-docs/**` | Audit Service |
| `http://localhost:8080/services/monitoring-service/v3/api-docs/**` | Monitoring Service |

### Actuator Endpoints

All services expose Spring Boot Actuator endpoints:

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Service health (shows DB, Kafka, Eureka connectivity) |
| `/actuator/info` | Service info and build details |
| `/actuator/metrics` | Application metrics |
| `/actuator/gateway` | Gateway route info (API Gateway only) |
| `/actuator/circuitbreaker` | Circuit breaker status (API Gateway only) |
| `/actuator/prometheus` | Prometheus metrics format (API Gateway only) |

---

## Event-Driven Communication

The platform uses Apache Kafka for asynchronous, event-driven communication between services. The **Loan Service** is the sole event publisher; **Audit**, **Reporting**, **Notification**, and **Monitoring** services are consumers.

### Kafka Topic

| Topic | Key | Producer | Consumers |
|-------|-----|----------|-----------|
| `loan-events` | `loanNumber` | `loan-service` | `audit-service`, `reporting-service`, `notification-service`, `monitoring-service` |

### Event Types

| Event | Description | Triggered By |
|-------|-------------|-------------|
| `LOAN_CREATED` | New loan application submitted | Loan Service — loan creation |
| `LOAN_STATUS_CHANGED` | Loan status transition (e.g., PENDING→VERIFIED) | Loan Service — `transitionState()` |
| `LOAN_DISBURSED` | Funds disbursed, loan activated | Loan Service — status → `ACTIVE` |
| `LOAN_REPAID` | Partial/full repayment recorded | Loan Service — `recordRepayment()` |

### Event Schema (v2.0)

```json
{
  "eventVersion": "2.0",
  "eventType": "LOAN_STATUS_CHANGED",
  "correlationId": "uuid",
  "eventTimestamp": "2026-01-01T12:00:00",
  "eventSource": "loan-service",
  "loanNumber": "LN-20260101-1234",
  "customerNumber": "CUST-000001",
  "amount": 300000.00,
  "loanType": "MORTGAGE",
  "loanPurpose": "Home purchase",
  "interestRate": 4.5000,
  "termMonths": 360,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "additionalData": "oldStatus=PENDING,newStatus=VERIFIED"
}
```

### Consumer Groups

| Service | Consumer Group | Responsibility |
|---------|---------------|----------------|
| `audit-service` | `audit-service-group` | Store immutable audit trail in SQL Server |
| `reporting-service` | `reporting-service-group` | Maintain real-time reporting aggregates |
| `notification-service` | `notification-service-group` | Send email/SMS notifications (3 retries, fallback) |
| `monitoring-service` | `monitoring-service-group` | Collect event metrics via Micrometer |

---

## Loan Lifecycle

The loan state machine enforces strict transition rules:

```
┌─────────┐     ┌───────────┐     ┌──────────┐     ┌────────┐     ┌────────┐
│ PENDING │────▶│  VERIFIED │────▶│ APPROVED │────▶│ ACTIVE │────▶│ CLOSED │
└─────────┘     └───────────┘     └──────────┘     └────────┘     └────────┘
      │               │                │              │
      ▼               │                │              │
  ┌─────────┐         │                │              │
  │REJECTED │         │                │              │
  └─────────┘         │                │              │
                     (can also         │              │
                      reject at        │              │
                      any stage)       │              │
                                       │              │
                                  ┌────▼──────────────┘
                                  │  (can reject)
                                  ▼
                              ┌─────────┐
                              │ REJECTED│
                              └─────────┘
```

| State | Code | Description | Allowed Transitions |
|-------|------|-------------|-------------------|
| `PENDING` | `PENDING` | Initial state after application | → `VERIFIED`, → `REJECTED` |
| `VERIFIED` | `VERIFIED` | Customer/KYC verified | → `APPROVED`, → `REJECTED` |
| `APPROVED` | `APPROVED` | Approved, pending disbursement | → `ACTIVE`, → `REJECTED` |
| `ACTIVE` | `ACTIVE` | Disbursed, accepting repayments | → `CLOSED` |
| `CLOSED` | `CLOSED` | Fully repaid | (terminal) |
| `REJECTED` | `REJECTED` | Application rejected | (terminal) |

---

## Quick Start

### Prerequisites

- **Java 21** (JDK)
- **Docker & Docker Compose**

### 1. Start All Services with Docker Compose

```bash
docker-compose up --build
```

All services, databases, Kafka, and Zookeeper will start automatically with health checks ensuring proper startup order.

### 2. Verify Services Are Running

Check the Discovery Server dashboard: [http://localhost:8761](http://localhost:8761)

### 3. Run the End-to-End Test

```bash
.\tests\end-to-end-test.sh
```

This script:
- Verifies all 10 services are healthy
- Creates a customer (implicit via seed data: `CUST-000001`)
- Creates collateral for the customer
- Applies for a loan (complete flow with credit check + collateral validation)
- Transitions the loan through all states (VERIFIED → APPROVED → ACTIVE → CLOSED)
- Verifies audit events were recorded
- Verifies reporting summary is updated
- Verifies monitoring metrics are collected

### 4. Build from Source

```bash
# Build all modules
mvn clean install -DskipTests

# Run individual services (requires Docker infrastructure)
mvn spring-boot:run -pl customer-service
mvn spring-boot:run -pl loan-service
mvn spring-boot:run -pl API-Gateway
```

---

## Environment Variables

### Common (All Services)

| Variable | Default | Description |
|----------|---------|-------------|
| `EUREKA_HOST` | `discovery-server` | Eureka server hostname |
| `CONFIG_SERVER_HOST` | `config-server` | Config Server hostname |
| `SPRING_CLOUD_CONFIG_FAIL_FAST` | `false` (gateway) / `true` (services) | Fail service startup if Config Server unavailable |

### Database (per-service)

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `<service>-db` | Database hostname |
| `DB_USERNAME` | `sa` | SQL Server username |
| `DB_PASSWORD` | (set in docker-compose) | SQL Server password |

### Kafka (per-service)

| Variable | Default | Description |
|----------|---------|-------------|
| `KAFKA_HOST` | `kafka` | Kafka broker hostname |

### SQL Server Credentials

| Database | SA Password | Container Port |
|----------|-------------|----------------|
| customer-db | `Customer@123` | `1433` |
| loan-db | `Loan@123` | `1434` |
| limit-db | `Limit@123` | `1435` |
| collateral-db | `Collateral@123` | `1436` |
| audit-db | `Audit@123` | `1437` |
| notification-db | `Notification@123` | `1438` |

---

## Testing

### Unit & Integration Tests

```bash
# Run tests for all modules
mvn test

# Run tests for a specific service
mvn test -pl customer-service
```

Tests use **Testcontainers** with real SQL Server containers (not in-memory H2), ensuring database compatibility issues are caught early.

### End-to-End Test

```bash
./tests/end-to-end-test.sh
```

The E2E test script validates the complete loan flow:
1. Service health checks
2. Customer → Collateral → Loan application (complete flow)
3. Loan status transitions through the full state machine
4. Audit trail verification
5. Reporting data verification
6. Monitoring metrics verification

### Kafka UI

Monitor Kafka topics and consumer groups at: [http://localhost:8090](http://localhost:8090)

---

## Design Principles

1. **Service Boundaries** — Each service owns its data and exposes a well-defined API. Cross-service calls use REST (synchronous) or Kafka events (asynchronous).

2. **Data Ownership** — No service directly accesses another service's database. Customer data is owned by Customer Service; Loan data by Loan Service. Data is duplicated (denormalized) where needed for performance.

3. **Resilience** — Resilience4j circuit breakers, retry logic, and timeouts protect against cascading failures. Each service has a fallback that returns a degraded response rather than failing.

4. **Event Versioning** — Kafka events are versioned (v2.0) to support backward compatibility as the schema evolves.

5. **Observability** — Spring Boot Actuator, Micrometer metrics, and structured logging provide visibility into service health and performance.

6. **Configuration Externalization** — All service configuration is managed by Spring Cloud Config Server, backed by a Git-style repository (`config-repo/`), enabling configuration changes without code changes.

7. **Soft Deletes** — Customer records are never hard-deleted; they are deactivated for compliance and auditing.

8. **Optimistic Locking** — JPA `@Version` fields prevent lost updates in concurrent scenarios.

9. **Database Migrations** — Flyway manages all schema changes with checksum validation. Production uses `clean-disabled: true` to prevent accidental data loss.

10. **DTO Separation** — Request, Response, Summary, and Internal DTOs enforce clear API contracts. Internal fields (database IDs, version numbers) are never exposed to external clients.

---

## License

This project is proprietary software. All rights reserved.
