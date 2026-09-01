# Customer Service

## Overview
The Customer Service is a microservice that manages customer data in the NextGen Loan Management Platform (NGLMP). It provides REST APIs for customer registration, profile management, address management, and status management.

## Architecture
- **Framework**: Spring Boot 3.3.0
- **Database**: PostgreSQL 16
- **Migration**: Flyway
- **Service Discovery**: Eureka Client
- **Configuration**: Config Server Client
- **API Documentation**: OpenAPI 3 / Swagger UI

## Features
- ✅ Customer Registration
- ✅ Customer CRUD Operations
- ✅ Address Management
- ✅ Status Management
- ✅ Pagination, Filtering, Sorting
- ✅ Advanced Search
- ✅ Validation
- ✅ Exception Handling
- ✅ API Documentation
- ✅ Service Discovery Integration
- ✅ Configuration Management

## API Endpoints

### Customer Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/customers | Register a new customer |
| GET | /api/v1/customers | List customers (paginated) |
| GET | /api/v1/customers/{customerNumber} | Get customer by number |
| PUT | /api/v1/customers/{customerNumber} | Update customer |
| PATCH | /api/v1/customers/{customerNumber}/status | Update status |
| DELETE | /api/v1/customers/{customerNumber} | Deactivate customer |
| POST | /api/v1/customers/search | Advanced search |

### Address Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/customers/{customerNumber}/addresses | Get addresses |
| POST | /api/v1/customers/{customerNumber}/addresses | Add address |
| PUT | /api/v1/customers/{customerNumber}/addresses/{addressId} | Update address |
| DELETE | /api/v1/customers/{customerNumber}/addresses/{addressId} | Delete address |

### Configuration Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/config | View current configuration |
| POST | /api/v1/config/refresh | Refresh configuration |

### Health
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /actuator/health | Health check |

## Configuration

### Required Environment Variables
- `DB_HOST`: PostgreSQL host (default: localhost)
- `DB_PORT`: PostgreSQL port (default: 5432)
- `DB_NAME`: Database name (default: customer_db)
- `DB_USERNAME`: Database username (default: customer_user)
- `DB_PASSWORD`: Database password (default: customer_pass)
- `CONFIG_SERVER_URL`: Config Server URL (default: http://localhost:8888)
- `EUREKA_SERVER_URL`: Eureka Server URL (default: http://localhost:8761/eureka/)

## Running Locally

1. Start PostgreSQL:
   ```bash
   docker-compose up -d customer-db