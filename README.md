# Payment Transfer Service (BPTS)

A secure and robust RESTful API service for transferring funds between accounts within a digital banking platform. Built with Spring Boot, MySQL, Docker, and comprehensive unit testing.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Build Instructions](#build-instructions)
- [Running the Application](#running-the-application)
  - [Using Docker (Recommended)](#using-docker-recommended)
  - [Running Locally](#running-locally)
- [API Endpoints Documentation](#api-endpoints-documentation)
- [Testing Instructions](#testing-instructions)
- [Database Schema Overview](#database-schema-overview)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

## Project Overview

The Payment Transfer Service is a microservice that enables secure fund transfers between accounts in a digital banking platform. It provides:

- **RESTful API** for initiating fund transfers
- **Atomic transactions** ensuring data consistency
- **Comprehensive validation** for accounts and balances
- **Transaction audit trail** for compliance and tracking
- **Error handling** with detailed error responses
- **Concurrency safety** using pessimistic locking

The service is built using Spring Boot 3.2.0 with Java 17, MySQL 8.0 for data persistence, and Docker for containerized deployment.

## Features

✅ **Secure Fund Transfers** - Transfer funds between accounts with validation and atomic transactions  
✅ **Account Management** - Create accounts, retrieve account information, and check account existence  
✅ **Account Listing** - Paginated list of all accounts with sorting support  
✅ **Account Validation** - Ensures source and destination accounts exist before processing  
✅ **Balance Verification** - Validates sufficient funds before executing transfers  
✅ **Transaction History** - Records all transfers for audit and tracking purposes  
✅ **Comprehensive Error Handling** - Detailed error responses with appropriate HTTP status codes  
✅ **Concurrency Safety** - Pessimistic locking prevents race conditions during transfers  
✅ **RESTful API** - Well-documented REST endpoints following best practices  
✅ **API Documentation** - Interactive Swagger/OpenAPI documentation with complete examples  
✅ **Docker Support** - Containerized deployment with Docker and Docker Compose  
✅ **Unit & Integration Tests** - Comprehensive test coverage with JUnit 5 and Mockito  

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java**: JDK 17 or higher
  ```bash
  java -version  # Should show version 17 or higher
  ```

- **Maven**: 3.6 or higher
  ```bash
  mvn -version  # Should show version 3.6 or higher
  ```

- **Docker**: 20.10 or higher (for Docker setup)
  ```bash
  docker --version  # Should show version 20.10 or higher
  ```

- **Docker Compose**: 1.25.0 or higher (for Docker Compose setup)
  ```bash
  docker-compose --version  # Should show version 1.25.0 or higher
  ```

- **MySQL**: 8.0 or higher (for local development only, not required for Docker setup)

## Project Structure

```
bpts/
├── src/
│   ├── main/
│   │   ├── java/com/bpts/
│   │   │   ├── config/              # Configuration classes
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/          # REST controllers
│   │   │   │   ├── TransferController.java
│   │   │   │   └── AccountController.java
│   │   │   ├── exception/           # Custom exceptions and handlers
│   │   │   │   ├── AccountNotFoundException.java
│   │   │   │   ├── InsufficientFundsException.java
│   │   │   │   ├── InvalidTransferException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/                # Entity models and DTOs
│   │   │   │   ├── Account.java
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── TransferRequest.java
│   │   │   │   ├── TransferResponse.java
│   │   │   │   ├── AccountResponse.java
│   │   │   │   └── CreateAccountRequest.java
│   │   │   ├── repository/          # Data access layer
│   │   │   │   ├── AccountRepository.java
│   │   │   │   └── TransactionRepository.java
│   │   │   ├── service/              # Business logic layer
│   │   │   │   ├── AccountService.java
│   │   │   │   └── TransferService.java
│   │   │   └── PaymentTransferServiceApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-docker.properties
│   │       └── db/migration/         # Flyway migrations
│   │           ├── V1__create_accounts_table.sql
│   │           └── V2__create_transactions_table.sql
│   └── test/                         # Test files
│       └── java/com/bpts/
│           ├── controller/
│           ├── repository/
│           └── service/
├── docker-compose.yml                # Docker Compose configuration
├── Dockerfile                         # Docker image definition
├── pom.xml                           # Maven dependencies
└── README.md                         # This file
```

## Build Instructions

### Clone the Repository

```bash
git clone <repository-url>
cd bpts
```

### Build the Project

#### Using Maven

```bash
# Clean and compile
mvn clean compile

# Build JAR file
mvn clean package

# Skip tests during build (if needed)
mvn clean package -DskipTests
```

The JAR file will be created at: `target/payment-transfer-service-1.0.0.jar`

#### Verify Build

```bash
# Check if JAR file exists
ls -lh target/*.jar

# Run JAR file directly
java -jar target/payment-transfer-service-1.0.0.jar
```

## Running the Application

### Using Docker (Recommended)

Docker setup is the easiest way to run the application as it includes both MySQL database and the Spring Boot application.

#### Step 1: Build and Start Services

```bash
# Build Docker images and start all services (MySQL + Application)
docker-compose up -d --build
```

This command will:
- Pull MySQL 8.0 image
- Build the Spring Boot application Docker image
- Start MySQL container
- Start the application container
- Wait for MySQL to be healthy before starting the app

#### Step 2: Verify Services are Running

```bash
# Check running containers
docker-compose ps

# View application logs
docker-compose logs -f app

# View MySQL logs
docker-compose logs -f mysql
```

Expected output should show both services as "Up" and healthy.

#### Step 3: Verify Application is Accessible

```bash
# Health check
curl http://localhost:8080/api/transfers/health

# Expected response:
# {"status":"UP","service":"Payment Transfer Service"}
```

#### Step 4: Access Swagger UI

Open your browser and navigate to:
```
http://localhost:8080/swagger-ui.html
```

#### Useful Docker Commands

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clears database data)
docker-compose down -v

# Restart services
docker-compose restart

# View logs for a specific service
docker-compose logs -f app
docker-compose logs -f mysql

# Execute commands inside a container
docker-compose exec app sh
docker-compose exec mysql mysql -u root -prootpassword
```

### Running Locally

For local development without Docker:

#### Step 1: Set Up MySQL Database

1. **Install MySQL** (if not already installed):

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql

# macOS (using Homebrew)
brew install mysql
brew services start mysql

# Windows
# Download and install from https://dev.mysql.com/downloads/
```

2. **Create Database and User**:

```bash
mysql -u root -p

CREATE DATABASE payment_transfer_db;
CREATE USER 'root'@'localhost' IDENTIFIED BY 'rootpassword';
GRANT ALL PRIVILEGES ON payment_transfer_db.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

3. **Update Configuration** (if needed):

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/payment_transfer_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=rootpassword
```

#### Step 2: Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or using the JAR file
java -jar target/payment-transfer-service-1.0.0.jar
```

The application will start on `http://localhost:8080`.

#### Step 3: Verify the Application

```bash
# Health check
curl http://localhost:8080/api/transfers/health

# Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

## API Endpoints Documentation

### Base URLs

The API provides endpoints under two main paths:

```
http://localhost:8080/api/transfers  # Transfer operations
http://localhost:8080/api/accounts   # Account management
```

### Interactive API Documentation

Access the interactive Swagger UI for complete API documentation:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

### Endpoints

#### 1. Health Check

Check if the service is operational.

**Endpoint:** `GET /api/transfers/health`

**Response:**
```json
{
  "status": "UP",
  "service": "Payment Transfer Service"
}
```

**Example Request:**
```bash
curl http://localhost:8080/api/transfers/health
```

---

#### 2. Transfer Funds

Transfer funds from one account to another.

**Endpoint:** `POST /api/transfers`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "fromAccountId": "550e8400-e29b-41d4-a716-446655440000",
  "toAccountId": "660e8400-e29b-41d4-a716-446655440001",
  "amount": 250.75,
  "description": "Payment for invoice #12345"
}
```

**Field Descriptions:**
- `fromAccountId` (required): Source account ID (UUID format, 36 characters)
- `toAccountId` (required): Destination account ID (UUID format, 36 characters)
- `amount` (required): Transfer amount (must be > 0.01, decimal)
- `description` (optional): Optional description for the transfer

**Success Response (201 Created):**
```json
{
  "transactionId": "770e8400-e29b-41d4-a716-446655440002",
  "status": "SUCCESS",
  "message": "Transfer completed successfully",
  "timestamp": "2024-01-15T10:30:00",
  "fromAccountId": "550e8400-e29b-41d4-a716-446655440000",
  "toAccountId": "660e8400-e29b-41d4-a716-446655440001",
  "amount": "250.75"
}
```

**Error Responses:**

| Status Code | Description | Example |
|-------------|-------------|---------|
| 400 Bad Request | Invalid request format or validation errors | Missing required fields, invalid UUID format |
| 404 Not Found | Account does not exist | Source or destination account not found |
| 422 Unprocessable Entity | Business rule violation | Insufficient funds, currency mismatch, same account |

**Error Response Example (422 - Insufficient Funds):**
```json
{
  "error": "Insufficient Funds",
  "message": "Insufficient funds in account 550e8400-e29b-41d4-a716-446655440000. Available: 100.00, Requested: 250.75",
  "timestamp": "2024-01-15T10:30:00",
  "status": 422,
  "details": {
    "accountId": "550e8400-e29b-41d4-a716-446655440000",
    "availableBalance": "100.00",
    "requestedAmount": "250.75"
  }
}
```

**Error Response Example (400 - Validation Error):**
```json
{
  "error": "Validation Failed",
  "message": "Invalid input parameters",
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "details": {
    "fromAccountId": "Source account ID is required",
    "amount": "Transfer amount must be greater than zero"
  }
}
```

**Error Response Example (404 - Account Not Found):**
```json
{
  "error": "Account Not Found",
  "message": "Account 550e8400-e29b-41d4-a716-446655440000: Account not found",
  "timestamp": "2024-01-15T10:30:00",
  "status": 404
}
```

**Example cURL Request:**
```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "550e8400-e29b-41d4-a716-446655440000",
    "toAccountId": "660e8400-e29b-41d4-a716-446655440001",
    "amount": 100.00,
    "description": "Test transfer"
  }'
```

**Example Using Swagger UI:**

1. Navigate to http://localhost:8080/swagger-ui.html
2. Click on the `/api/transfers` POST endpoint
3. Click "Try it out"
4. Fill in the request body with example values
5. Click "Execute"
6. View the response

---

### Account Management Endpoints

#### 3. Get All Accounts

Retrieve a paginated list of all accounts.

**Endpoint:** `GET /api/accounts`

**Query Parameters:**
- `page` (optional): Page number (zero-based, default: 0)
- `size` (optional): Page size (default: 20, max: 100)
- `sort` (optional): Sorting criteria (format: `property,direction`). Example: `createdAt,desc` or `balance,asc`

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "accountId": "550e8400-e29b-41d4-a716-446655440000",
      "balance": 1000.00,
      "currency": "USD",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    },
    {
      "accountId": "660e8400-e29b-41d4-a716-446655440001",
      "balance": 2500.50,
      "currency": "USD",
      "createdAt": "2024-01-15T11:00:00",
      "updatedAt": "2024-01-15T11:00:00"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 2
}
```

**Example Requests:**
```bash
# Get all accounts (default pagination)
curl http://localhost:8080/api/accounts

# Get specific page
curl "http://localhost:8080/api/accounts?page=0&size=10"

# Get with sorting by balance (descending)
curl "http://localhost:8080/api/accounts?sort=balance,desc"

# Combined: page, size, and sort
curl "http://localhost:8080/api/accounts?page=0&size=10&sort=createdAt,desc"
```

---

#### 4. Get Account by ID

Retrieve account information by account ID.

**Endpoint:** `GET /api/accounts/{accountId}`

**Path Parameters:**
- `accountId` (required): Account identifier (UUID format, 36 characters)

**Success Response (200 OK):**
```json
{
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1000.00,
  "currency": "USD",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Error Responses:**

| Status Code | Description | Example |
|-------------|-------------|---------|
| 404 Not Found | Account does not exist | Account ID not found |

**Example Request:**
```bash
curl http://localhost:8080/api/accounts/550e8400-e29b-41d4-a716-446655440000
```

---

#### 5. Create Account

Create a new account with optional initial balance and currency.

**Endpoint:** `POST /api/accounts`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "initialBalance": 1000.00,
  "currency": "USD"
}
```

**Field Descriptions:**
- `initialBalance` (optional): Initial balance for the account (must be >= 0, default: 0.00)
- `currency` (optional): Currency code (ISO 4217 format, 3 uppercase letters, default: "USD")

**Success Response (201 Created):**
```json
{
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1000.00,
  "currency": "USD",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Error Responses:**

| Status Code | Description | Example |
|-------------|-------------|---------|
| 400 Bad Request | Invalid input (e.g., negative balance, invalid currency format) | Validation errors |

**Error Response Example (400 - Validation Error):**
```json
{
  "error": "Validation Failed",
  "message": "Invalid input parameters",
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "details": {
    "initialBalance": "Initial balance cannot be negative",
    "currency": "Currency must be a 3-letter uppercase code (ISO 4217)"
  }
}
```

**Example Requests:**
```bash
# Create account with initial balance
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "initialBalance": 1000.00,
    "currency": "USD"
  }'

# Create account with minimal request (defaults: balance=0.00, currency=USD)
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

#### 6. Check Account Exists

Check if an account with the given ID exists.

**Endpoint:** `GET /api/accounts/{accountId}/exists`

**Path Parameters:**
- `accountId` (required): Account identifier (UUID format)

**Success Response (200 OK):**
```json
{
  "exists": true
}
```

Or:
```json
{
  "exists": false
}
```

**Example Request:**
```bash
curl http://localhost:8080/api/accounts/550e8400-e29b-41d4-a716-446655440000/exists
```

---

## Testing Instructions

### Prerequisites for Testing

- Java 17+ installed
- Maven 3.6+ installed
- Docker (optional, for repository integration tests)

### Running Tests

#### Run All Tests

```bash
# Run all unit and integration tests
mvn test

# Run with test output
mvn test -Dtest=*Test
```

#### Run Specific Test Classes

```bash
# Run TransferService tests
mvn test -Dtest=TransferServiceTest

# Run Controller tests
mvn test -Dtest=TransferControllerTest

# Run AccountService tests
mvn test -Dtest=AccountServiceTest
```

#### Run Tests with Coverage

```bash
# Run tests and generate coverage report (if JaCoCo is configured)
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Structure

The project includes comprehensive test coverage:

#### Unit Tests

- **TransferServiceTest**: Tests business logic for fund transfers
  - Successful transfers
  - Insufficient funds scenarios
  - Account not found scenarios
  - Invalid transfer scenarios
  - Currency mismatch scenarios
  - Failed transaction handling

- **AccountServiceTest**: Tests account management
  - Account retrieval
  - Balance updates
  - Account existence checks

#### Integration Tests

- **TransferControllerTest**: Tests REST API endpoints
  - HTTP request/response handling
  - Request validation
  - Error handling

- **AccountRepositoryTest**: Database integration tests (requires Docker)
  - Account persistence
  - Account retrieval
  - Balance updates

### Test Output

Test results are generated in:
```
target/surefire-reports/
```

View test reports:
```bash
# List test reports
ls target/surefire-reports/

# View specific test report
cat target/surefire-reports/TEST-com.bpts.service.TransferServiceTest.txt
```

### Note on Repository Tests

The `AccountRepositoryTest` requires Docker to run as it uses Testcontainers. If Docker is not available, the tests will be skipped automatically.

To run repository tests:
```bash
# Ensure Docker is running
docker ps

# Run repository tests
mvn test -Dtest=AccountRepositoryTest
```

## Database Schema Overview

The application uses MySQL 8.0 with Flyway for database migrations. The schema consists of two main tables:

### Accounts Table

Stores account information and balances.

**Table Name:** `accounts`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| account_id | VARCHAR(36) | PRIMARY KEY | Unique account identifier (UUID) |
| balance | DECIMAL(19,2) | NOT NULL, DEFAULT 0.00, CHECK >= 0 | Account balance (non-negative) |
| currency | VARCHAR(3) | NOT NULL, DEFAULT 'USD' | Currency code (ISO 4217) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Account creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Constraints:**
- `balance >= 0` (CHECK constraint ensures balance cannot be negative)
- `account_id` is unique (PRIMARY KEY)
- `created_at` is set automatically on creation
- `updated_at` is updated automatically on modification

**Indexes:**
- Primary index on `account_id`
- Index on `created_at` for querying recent accounts

### Transactions Table

Stores all transfer transaction records for audit and tracking.

**Table Name:** `transactions`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| transaction_id | VARCHAR(36) | PRIMARY KEY | Unique transaction identifier (UUID) |
| from_account_id | VARCHAR(36) | NOT NULL, FK to accounts | Source account ID |
| to_account_id | VARCHAR(36) | NOT NULL, FK to accounts | Destination account ID |
| amount | DECIMAL(19,2) | NOT NULL, CHECK > 0 | Transfer amount (must be positive) |
| status | ENUM | NOT NULL, DEFAULT 'PENDING' | Transaction status (SUCCESS, FAILED, PENDING) |
| transaction_date | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Transaction timestamp |
| description | VARCHAR(500) | NULL | Optional transaction description |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |

**Constraints:**
- `amount > 0` (CHECK constraint ensures transfer amount is positive)
- `from_account_id != to_account_id` (CHECK constraint prevents transfers to same account)
- Foreign key to `accounts(account_id)` with RESTRICT on delete
- `status` must be one of: SUCCESS, FAILED, PENDING

**Indexes:**
- Primary index on `transaction_id`
- Index on `from_account_id` for querying by source account
- Index on `to_account_id` for querying by destination account
- Index on `transaction_date` for querying by date range
- Index on `status` for querying by transaction status

### Database Migration

Database schema is managed using Flyway migrations located in:
```
src/main/resources/db/migration/
```

Migration files:
- `V1__create_accounts_table.sql` - Creates accounts table
- `V2__create_transactions_table.sql` - Creates transactions table

Migrations run automatically when the application starts.

### Schema Relationships

```
accounts (1) ──< transactions (many)
```

- One account can have many outgoing transactions (`from_account_id`)
- One account can have many incoming transactions (`to_account_id`)
- Foreign keys ensure referential integrity
- RESTRICT on delete prevents accidental account deletion if transactions exist

## Configuration

### Application Properties

#### Local Development (`application.properties`)

```properties
# Server
server.port=8080

# Database (Local MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/payment_transfer_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=rootpassword

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# SpringDoc OpenAPI (Swagger)
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
```

#### Docker Deployment (`application-docker.properties`)

Uses Docker service names for database connection:
```properties
# Database (Docker MySQL)
spring.datasource.url=jdbc:mysql://mysql:3306/payment_transfer_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=rootpassword
```

### Environment Variables

You can override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/payment_transfer_db
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=yourpassword
export SERVER_PORT=8080
```

### Docker Compose Configuration

The `docker-compose.yml` file configures:
- **MySQL Service**: Port 3306, database `payment_transfer_db`
- **Application Service**: Port 8080, depends on MySQL
- **Networks**: Bridge network for service communication
- **Volumes**: Persistent data storage for MySQL
