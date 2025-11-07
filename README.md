# Project: Automated Expense Tracker from SMS

## Overview

This project is a complete microservices-based application designed to automate personal expense tracking. It intelligently processes incoming SMS messages from financial institutions, uses a Large Language Model (LLM) to extract transaction details, and securely stores this information for the user. The system provides a robust backend that handles user authentication, profile management, and expense data, all powered by a modern, event-driven architecture.

The core user workflow is as follows:
1.  A user registers and logs into the system.
2.  An application on the user's phone forwards bank transaction SMS messages to the system.
3.  The system automatically extracts the expense details (amount, merchant).
4.  The user can then view their categorized expenses on a user interface.

## High-Level Architecture

The system is composed of four primary microservices that are exposed to the outside world through a centralized Kong API Gateway. The services communicate with each other asynchronously via an Apache Kafka event bus. This decoupled design ensures security, scalability, resilience, and maintainability.

```
+------------------------+
|  Client App            |
|  (Web/Mobile)          |
+-----------+------------+
            |
            | (e.g., /auth/v1/login, /users/v1/getUser)
            |
+-----------v------------+      +--------------------+      +--------------------------+
|                        +----->| Authentication Svc +----->|       MySQL DB           |
|  Kong API Gateway      |      | (on Port 9898)     |      | (Users, Roles, Tokens)   |
|  (Single Entry Point)  |      +--------------------+      +--------------------------+
|  (Routing & Security)  |                      |
|                        |                      | Publishes UserInfoEvent
|                        |                      v
|                        |      +--------------------+      +--------------------------+
|                        +----->|   User Service     |<----|       Apache Kafka       |
|                        |      | (on Port 9810)     |     |       (Event Bus)        |
|                        |      +---------+----------+      +------------+-------------+
|                        |                |                               ^
|                        |      +---------v----------+                      | Produces ExpenseInfoEvent
|                        |      |    Redis Cache     |                      |
|                        |      |  (User Profiles)   |                      |
|                        |      +--------------------+      +---------------+----------+
|                        |                                  | Data Science Svc         |
|                        +--------------------------------->| (on Port 8000)           |
|                        |                                  +--------------------------+
|                        |
|                        |      +--------------------+      +--------------------------+
|                        +----->|  Expense Service   |<----|       MySQL DB            |
|                               | (on Port 9820)     |      |       (Expenses)         |
+-----------+------------+      +--------------------+      +--------------------------+
            ^
            | (e.g., /data-science/v1/message)
            |
+-----------+------------+
|  External App          |
|  (SMS Forwarder)       |
+------------------------+

```

## Core Workflow

1.  **User Registration:** A user signs up through the **Authentication Service**, which creates a new user identity and publishes a `UserInfoEvent` to a Kafka topic.
2.  **Profile Creation:** The **User Service** consumes this event and creates a detailed user profile in its own database, which is also cached in Redis for fast access.
3.  **SMS Ingestion:** An SMS message is forwarded to the **Data Science Service**.
4.  **Intelligent Extraction:** This service uses a Google Gemini LLM to analyze the text and extract structured expense data (e.g., `{ "amount": "15.75", "merchant": "Starbucks", "currency": "USD" }`).
5.  **Expense Event:** The **Data Science Service** publishes the structured data as an `ExpenseInfoEvent` to a different Kafka topic.
6.  **Expense Creation:** The **Expense Service** consumes this event and saves the new expense record to its database, linking it to the user.
7.  **Data Retrieval:** The authenticated client application can then make API calls to the **User Service** and **Expense Service** to retrieve profile and financial data to display on the UI.

## Microservices Overview

This project consists of four distinct microservices:

*   **Authentication Service:** A Java/Spring Boot service that manages user identity, registration, login (JWT), and token refreshing. It is the single source of truth for user authentication.
*   **User Service:** A Java/Spring Boot service for managing user profile data (name, email, etc.). It uses a Redis cache to ensure high-performance reads.
*   **Expense Service:** A Java/Spring Boot service that manages all financial transaction records. It can create expenses via its API or by consuming events from Kafka.
*   **Data Science Service:** A Python/Flask service that uses an LLM (Google Gemini) to perform NLP for extracting structured expense data from raw text.

## Technology Stack

| Category               | Technology                                                           |
| ---------------------- | -------------------------------------------------------------------- |
| **API Gateway**        | Kong (Centralized Routing & Security Enforcement)                                                              |
| **Backend Frameworks** | Spring Boot (Java), Flask (Python)                                   |
| **Languages**          | Java 21, Python                                                      |
| **AI / NLP**           | LangChain, Google Gemini                                             |
| **Databases**          | MySQL (for all services)                                             |
| **Caching**            | Redis (for User Service)                                             |
| **Messaging / Events** | Apache Kafka                                                         |
| **Security**           | Spring Security, JSON Web Tokens (JWT)                               |
| **Containerization**   | Docker, Docker Compose                                               |


## API Gateway (Kong) Configuration

All external traffic is routed through the Kong API Gateway, which provides a single entry point (`http://<kong_host>:8000`) and directs requests to the appropriate backend service based on the URL path.

*   **Routing:**
    *   `/auth/*` -> `authentication-service:9898`
    *   `/users/*` -> `user-service:9810`
    *   `/expenses/*` -> `expense-service:9820`
    *   `/data-science/*` -> `datascience-service:8000`
*   **Security (Future Enhancement):** The gateway is positioned to be the primary security enforcement point. The next step is to enable the **JWT plugin** on the `/users` and `/expenses` routes. This will offload token validation from the individual services, centralizing security logic.
*   **Request Transformation (Future Enhancement):** After JWT validation, a plugin will be configured to extract the user's ID from the token payload and inject it into the `X-User-Id` header for downstream services.


## Getting Started

### Prerequisites

*   Docker and Docker Compose
*   Java 21+ and Maven (to build the Java projects)
*   Python 3.10+ and pip (for the Data Science service)
*   A running MySQL, Kafka, and Redis instance. (The provided `docker-compose.yml` will set these up for you).
*   A Google API Key for the Gemini model.

### Running the Entire System

The most convenient way to run the entire application stack is by using the provided Dockerfiles and a `docker-compose.yml` file.

**1. Build the Java Applications:**
Navigate to the root directory of the `authentication-service`, `expense-service`, and `user-service` and run the following command in each to build the JAR files:
```bash
mvn clean package
```

**2. Create a `docker-compose.yml` file:**
Create a file named `docker-compose.yml` in the root of your project directory and paste the following content. This file defines all the services and their dependencies.

```yaml
version: '3.8'

services:
  # -- INFRASTRUCTURE --
   kong-db:
    image: postgres:9.6
    container_name: kong_db
    environment:
      - POSTGRES_USER=kong
      - POSTGRES_DB=kong
      - POSTGRES_PASSWORD=kong
    volumes:
      - kong-db-data:/var/lib/postgresql/data

  kong-migration:
    image: kong:latest
    container_name: kong_migration
    depends_on:
      - kong-db
    environment:
      - KONG_DATABASE=postgres
      - KONG_PG_HOST=kong-db
      - KONG_PG_PASSWORD=kong
      - KONG_PG_USER=kong
    command: "kong migrations bootstrap"

  kong:
    image: kong:latest
    container_name: kong_gateway
    depends_on:
      - kong-migration
    environment:
      - KONG_DATABASE=postgres
      - KONG_PG_HOST=kong-db
      - KONG_PG_PASSWORD=kong
      - KONG_PG_USER=kong
      - KONG_PROXY_ACCESS_LOG=/dev/stdout
      - KONG_ADMIN_ACCESS_LOG=/dev/stdout
      - KONG_PROXY_ERROR_LOG=/dev/stderr
      - KONG_ADMIN_ERROR_LOG=/dev/stderr
      - KONG_ADMIN_LISTEN=0.0.0.0:8001, 0.0.0.0:8444 ssl
    ports:
      - "8000:8000" # Proxy Port for client traffic
      - "8001:8001" # Admin API Port for configuration
  mysql:
    image: mysql:8.0
    container_name: mysql_db
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: userservice # Will be used by default, others created by scripts or services
      MYSQL_USER: springuser
      MYSQL_PASSWORD: password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  redis:
    image: redis:6.2-alpine
    container_name: redis_cache
    ports:
      - "6379:6379"
    command: redis-server --save 20 1 --loglevel warning --requirepass redis_gold123
    volumes:
      - redis-data:/data

  zookeeper:
    image: confluentinc/cp-zookeeper:7.0.1
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.0.1
    container_name: kafka_broker
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:29092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1

  # -- APPLICATION SERVICES --
  auth-service:
    build: ./authentication-service
    container_name: auth_service
    ports:
      - "9898:9898"
    depends_on:
      - mysql
      - kafka
      - redis
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092
      - REDIS_HOST=redis

  user-service:
    build: ./user-service
    container_name: user_service
    ports:
      - "9810:9810"
    depends_on:
      - mysql
      - kafka
      - redis
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092
      - REDIS_HOST=redis

  expense-service:
    build: ./expense-service
    container_name: expense_service
    ports:
      - "9820:9820"
    depends_on:
      - mysql
      - kafka
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092

  datascience-service:
    build: ./datascience-service # Assuming a Dockerfile exists here
    container_name: ds_service
    ports:
      - "8000:8000"
    depends_on:
      - kafka
    environment:
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092
      - GOOGLE_API_KEY=your_google_api_key_here # Replace this

volumes:
  kong-db-data:
  mysql-data:
  redis-data:
```

***Note:*** *You will need to create a Dockerfile for the `datascience-service` and replace `your_google_api_key_here` with your actual API key.*

**3. Run the system and Configure Kong:**
First, start all services:
```bash
docker-compose up --build -d
```
After a minute, configure Kong by running the `curl` commands you provided against the Admin API port (`8001`).

```

This command will build the images for all your services and start them, along with the required infrastructure. Your entire backend system is now running.
