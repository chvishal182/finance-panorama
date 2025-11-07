# README: Expense Service

## Service Definition

The **Expense Service** is a dedicated microservice designed to manage all aspects of user expenses. It provides a robust and scalable solution for creating, retrieving, and updating expense records, acting as the central repository for all trsansactional expense data within the application.

## Core Objectives

*   **Centralize Expense Management:** To provide a single, reliable source of truth for all user expense data, ensuring consistency and data integrity.
*   **Decouple Business Logic:** To isolate expense-related functionalities from other domains like user management or data analytics, promoting a clean and maintainable microservice architecture.
*   **Support Multiple Data Ingestion Methods:** To allow expenses to be created both synchronously via a direct REST API call and asynchronously by consuming events from a Kafka topic.
*   **Provide a Clear API for Clients:** To offer a simple and well-defined RESTful API for client applications to fetch and manage user expenses securely.

## Architectural Overview

This service is a self-contained Spring Boot application that exposes a REST API for managing expense data and includes a Kafka consumer to ingest expense information asynchronously. It uses a MySQL database for persistence, ensuring that all expense records are stored reliably.

## Tech Stack and Tools

| Component           | Technology / Tool                                       |
| ------------------- | ------------------------------------------------------- |
| **Framework**       | Spring Boot                                             |
| **API Gateway**     | **Kong** (Routes `/expense/*` traffic to this service)  |
| **Language**        | Java                                                    |
| **Database**        | MySQL                                                   |
| **Data Access**     | Spring Data JPA / Hibernate                             |
| **Event Streaming** | Apache Kafka (Consumer)                                 |
| **API**             | REST                                                    |
| **Dependencies**    | Maven / Gradle                                          |
| **Deployment**      | Docker                                                  |

## System Architecture

```
                      +----------------------+      +-------------------------+
                      |      Client App      |      | Other Backend Services  |
                      |    (Web/Mobile)      |      | (e.g., Data Ingestion)  |
                      +-----------+----------+      +------------+------------+
                                  |                               |
        (1a) Add/Get Expense      |                               | (1b) Publish Expense Event
               (HTTPS)            |                               |
                                  v                               v
       +-----------------------------------------------------------------------------+
       |                         API Gateway / Service Mesh                          |
       |  (Validates JWT from Auth Service, gets User ID, adds to X-User-Id header)  |
       +--------------------------+--------------------------------------------------+
                                  |
                                  v
+---------------------------------v----------------------------------------------------+
|                              EXPENSE SERVICE                                         |
|                                                                                      |
|  +---------------------------+      +--------------------+      +-----------------+  |
|  |     REST Controller       |      |   Kafka Consumer   |      |  MySQL Database |  |
|  | (/expense/v1/addExpense)  |      |  (Topic:           |      |  (Expense Table)|  |
|  | (/expense/v1/getExpense)  +------>|   expense_service) |      +--------+--------+ |
|  +------------+--------------+      +---------+----------+               ^           |
|               |                               |                          |           |
|               | (2a) Invokes                  | (2b) Invokes             | Stores/   |
|               |                               |                          | Retrieves |
|               +-------------------------------+--------------------------+           |
|                                               |                                      |
|                                               v                                      |
|                                 +--------------------------+                         |
|                                 |      ExpenseService      |                         |
|                                 |   (Core Business Logic)  |                         |
|                                 +--------------------------+                         |
|                                                                                      |
+--------------------------------------------------------------------------------------+
```

**Workflow:**

There are two primary ways to create an expense:
1.  **Synchronous (API Call):**
    a. A client application sends a `POST` request to the `/expense/v1/addExpense` endpoint, passing through an API Gateway which adds the authenticated user's ID to the `X-User-Id` header.
    b. The `ExpenseController` receives the request and calls the `ExpenseService` to save the new expense directly to the MySQL database.
2.  **Asynchronous (Event-Driven):**
    a. Another microservice or data pipeline publishes an `ExpenseInfoEvent` message to the `expense_service` Kafka topic.
    b. The `ExpenseServiceConsumer` within the Expense Service listens to this topic, consumes the message, and calls the `ExpenseService` to save the new expense to the database.

## Code Repository Structure

```
com.chelv
├── controller          # REST API controller (ExpenseController)
├── entities            # JPA entity for the 'expense' table
├── expense_service     # Main application entry point
├── model               # DTOs and event models (ExpenseInfoDTO, ExpenseInfoEvent)
├── pubsub              # Kafka consumer for expense events
│   └── subscribe
├── repository          # Spring Data JPA repository for the Expense entity
├── serdes              # Custom Kafka deserializer
│   └── deserialization
├── service             # Core business logic (ExpenseService)
└── utility             # Helper classes (ExpenseInfoMapper)
```

## Key Features

| Feature                          | Description                                                                                                                                               |
| -------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Create Expense**               | Allows for the creation of new expense records for a user via a `POST` request to the `/expense/v1/addExpense` endpoint.                                  |
| **Retrieve Expenses**            | Fetches and returns a list of all expenses associated with a given `user_id` via a `GET` request to the `/expense/v1/getExpense` endpoint.                |
| **Update Expense**               | The service layer contains logic to safely find an existing expense by its user ID and external ID and update its details.                                |
| **Asynchronous Expense Creation**| Subscribes to a Kafka topic to listen for and process `ExpenseInfoEvent` messages, allowing for a fully event-driven way to create expenses.              |
| **Data Integrity**               | Automatically generates a unique external ID (UUID) and a creation timestamp for each new expense record using JPA's `@PrePersist` annotation.            |
| **Clear API and Data Contracts** | Uses DTOs (`ExpenseInfoDTO`) for API interactions and separate Event models (`ExpenseInfoEvent`) for Kafka messages, ensuring stable and clear contracts. |

## Design and Architectural Principles

*   **Single Responsibility Principle:** The service is tightly focused on one business domain: managing expenses. It does not handle user profiles, authentication, or any other unrelated concerns.
*   **Loose Coupling:** By consuming events from Kafka, the service is decoupled from the producers of expense data. It does not need to know which service created the expense event, only that it needs to be processed.
*   **Domain-Driven Design:** The `Expense` entity and related services are modeled closely on the business domain of an expense, with clear attributes like amount, merchant, and currency.
*   **Scalability:** As a stateless microservice, multiple instances can be run in parallel to handle increased load from either the API or the Kafka topic.

---
