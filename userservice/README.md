# README: User Service

## Service Definition

The **User Service** is a high-performance microservice responsible for managing detailed user profile information. It acts as the central repository for non-security-related user data, such as names, contact information, and profile pictures. This service is designed for fast read access by employing a caching layer to deliver user data with minimal latency.

## Core Objectives

*   **Centralize User Profile Data:** To create a single source of truth for user profile information, separate from the core authentication data.
*   **Provide High-Performance Reads:** To deliver user profile data with very low latency by implementing a robust caching strategy with Redis.
*   **Ensure Data Synchronization:** To automatically create and update user profiles by listening to events published by the Authentication Service, ensuring data consistency across the ecosystem.
*   **Offer a Simple Management API:** To provide a clean, RESTful API, exposed via an API Gateway, for clients to retrieve and update user profile information.

## Architectural Overview

This service is a Spring Boot application that operates **behind a Kong API Gateway**. It follows a CQRS (Command Query Responsibility Segregation) pattern through its use of a cache. It exposes a REST API for data manipulation, consumes user creation events from an Apache Kafka topic, persists data in a MySQL database, and uses a Redis cache for all read operations to ensure high performance.

## Tech Stack and Tools

| Component           | Technology / Tool                                       |
| ------------------- | ------------------------------------------------------- |
| **Framework**       | Spring Boot                                             |
| **Language**        | Java                                                    |
| **Database**        | MySQL                                                   |
| **Caching**         | Redis                                                   |
| **Data Access**     | Spring Data JPA / Hibernate, Spring Data Redis          |
| **Event Streaming** | Apache Kafka (Consumer)                                 |
| **API**             | REST (Exposed via Kong API Gateway)                     |
| **Dependencies**    | Maven / Gradle                                          |
| **Deployment**      | Docker                                                  |

## System Architecture

The User Service has two primary entry points: synchronous API calls routed through the **Kong API Gateway** and asynchronous events consumed from **Apache Kafka**.

```
                               +-------------------------+
                               |  Authentication Service |
                               +------------+------------+
                                            |
                                            | (1) Publishes UserInfoEvent
                                            |     on new user signup
                                            v
                               +--------------------------+
                               |      Apache Kafka        |
                               | (Topic: testing_json)    |
                               +------------+-------------+
                                            |
           +-----------------+              | (2) Consumes event
           |   Client App    |              v
           |  (Web/Mobile)   |  +---------------------------+
           +--------+--------+  |    AuthServiceConsumer    |
                    |           +-------------+-------------+
(3) GET /users/v1/getUser?id=...              | (2a) Invokes upsert
                    |                         v
+-------------------v-------------------------v-----------------------------------------+
|                                  USER SERVICE                                         |
|                                                                                       |
|  +--------------------------+     +--------------------+     +----------------------+ |
|  |     REST Controller      +----->     UserService    <-----+       Redis Cache    | |
|  | (/user/v1/upsert)        |     |(Business Logic)    |     |(Key: "user:<userId>")| |
|  | (/users/v1/getUser)      |     +---------+----------+     +-----------+----------+ |
|  +--------------------------+               ^                      ^     |            |
|                                             |                      | (4b)|(4a) Cache  |
|                                             | DB Read/Write        | Cache      Miss  |
|                                             | (on miss or upsert)  | Write            |
|                                             v                      |                  |
|                                  +------------------------+                           |
|                                  |     MySQL Database     |                           |
|                                  |   (User Profile Data)  |                           |
|                                  +------------------------+                           |
+---------------------------------------------------------------------------------------+

```

**Workflow:**
1.  The **Authentication Service** publishes a `UserInfoEvent` to the `testing_json` Kafka topic when a new user registers.
2.  The `AuthServiceConsumer` in the User Service consumes this event and calls the `upsertUser` method.
3.  The `upsertUser` method saves or updates the user's profile in the MySQL database and then writes the result to the Redis cache to ensure the cache is up-to-date.
4.  When a client requests a user's profile via the `GET /users/v1/getUser` endpoint:
    a. The service first attempts to retrieve the data from the **Redis cache**.
    b. If the data is not in the cache (a cache miss), it queries the **MySQL database**, writes the result into Redis for subsequent requests, and then returns the data to the client.

## Code Repository Structure

```
com.chelv
├── config              # Configuration beans (RedisConfig)
├── controller          # REST API controller (UserController)
├── entities            # JPA entity for the 'user_info' table
├── model               # DTOs and event models (UserInfoDTO, UserInfoEvent)
├── pubsub              # Kafka consumer for authentication events
│   └── subscribe
├── repository          # Spring Data JPA repository for the UserInfo entity
├── serdes              # Custom Kafka deserializer
│   └── deserialization
├── service             # Core business logic with caching (UserService)
└── utility             # Helper classes for mapping (UserInfoMapper)
```

## Key Features

| Feature                       | Description                                                                                                                                                                             |
| ----------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Upsert User Profile**       | Provides a single `POST /user/v1/upsert` endpoint that handles both the creation of new user profiles and the updating of existing ones, ensuring data is saved to both the database and cache. |
| **High-Speed User Retrieval** | Implements a **cache-aside** pattern for the `GET /users/v1/getUser` endpoint. It checks Redis first for user data, drastically reducing response times and database load.                       |
| **Event-Driven Synchronization**| Automatically creates user profiles by consuming `UserInfoEvent` messages from a Kafka topic published by the Authentication Service, ensuring eventual consistency.                   |
| **Database-per-Service**      | Maintains its own dedicated database for user profile information, completely independent of the Authentication Service's user table, which is a core tenet of microservice design.          |
| **Data Consistency**          | On every user profile update (upsert), the service ensures the Redis cache is either updated or invalidated, maintaining a high degree of consistency between the cache and the database. |

## Design and Architectural Principles

*   **Cache-Aside Pattern:** This classic caching strategy is implemented to maximize read performance. The application code is responsible for checking the cache and loading data from the database only when necessary.
*   **Database-per-Service:** The User Service has full ownership of its own database schema and data, preventing other services from coupling to its data model and ensuring independent scalability and evolution.
*   **Eventual Consistency:** By consuming events from Kafka, the service accepts that its data will be "eventually consistent" with the Authentication Service. This is a common and powerful pattern in distributed systems that favors availability and performance.
*   **Single Responsibility Principle:** The service's responsibility is clearly defined: it manages user *profile* data. All other concerns, such as authentication or expense tracking, are handled by other dedicated services.

---
