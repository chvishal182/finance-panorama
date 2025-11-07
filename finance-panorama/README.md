# README: Authentication Service

## Service Definition

The **Authentication Service** is a centralized, standalone microservice responsible for managing user identity and securing access to the application ecosystem. It handles user registration, login, and token management, providing robust, token-based authentication (JWT) for all other microservices.


## Core Objectives

*   **Centralize User Authentication:** To act as the single source of truth for user identity, providing a secure and scalable authentication point for the entire platform.
*   **Secure Microservice Endpoints:** To issue short-lived JWT access tokens and long-lived refresh tokens that clients can use to securely access other protected services.
*   **Enable Asynchronous Communication:** To notify other services of user-related events (e.g., new user registration) in a decoupled manner using an event-driven approach with Apache Kafka.
*   **Provide a Seamless User Experience:** To allow users to maintain their session using refresh tokens, avoiding the need for frequent logins.

## Architectural Overview

This service is a stateless Spring Boot application that operates **behind a Kong API Gateway**. It exposes a REST API for authentication operations, integrates with Spring Security for its core logic, uses a MySQL database for persistence, and leverages Apache Kafka to publish events to other microservices.

## Tech Stack and Tools

| Component             | Technology / Tool                                     |
| --------------------- | ----------------------------------------------------- |
| **Framework**         | Spring Boot                                           |
| **API Gateway**       | **Kong** (Routes `/auth/*` traffic to this service)   |                                        
| **Language**          | Java                                                  |
| **Security**          | Spring Security                                       |
| **Authentication**    | JSON Web Tokens (JWT) & Refresh Tokens                |
| **Database**          | MySQL                                                 |
| **Data Access**       | Spring Data JPA / Hibernate                           |
| **Event Streaming**   | Apache Kafka                                          |
| **API**               | REST                                                  |
| **Dependencies**      | Maven / Gradle                                        |
| **Deployment**        | Docker                                                |


## System Architecture

```
                               +--------------------------+
                               |      Client App          |
                               | (Web/Mobile)             |
                               +-------------+------------+
                                             |
                  (1) Register/Login         | (4) Send JWT with requests
                    (HTTPS)                  |   to other services
                                             |
       +-------------------------------------v---------------------------------------+
       |                                                                             |
       |  +-------------------------+      +------------------+      +---------------+
       |  | Expense Service         |<-----+  API Gateway     +------>| User Service |
       |  +-------------------------+      +------------------+      +---------------+
       |                                          ^                                  |
       |                                          | (3) Receives &                   |
       |                                          |     validates event              |
       +------------------------------------------+----------------------------------+
                                                  |
                                                  |
       +------------------------------------------v-------------------------------------+
       |                        AUTHENTICATION SERVICE                                  |
       |                                                                                |
       |  +----------------------------+   (2) Issues    +-------------------------+    |
       |  |      REST Controllers      +---------------> |   JWT & Refresh Tokens  |    |
       |  | (/signup, /login, /refresh)|                 +-------------------------+    |
       |  +-------------+--------------+                                                |
       |                |                                                               |
       |                | Uses                                                          |
       |                v                                                               |
       |  +----------------------------+      +------------------+      +-------------+ |
       |  |     Spring Security &      |      |  MySQL Database  |      | Apache Kafka| |
       |  |      User Services         +<---->| (Users, Roles,   |+----->|   (Topic:  | |
       |  | (Password Hash, JWT Gen)   |      |    Tokens)       |      | user-events)| |
       |  +----------------------------+      +------------------+      +-------------+ |
       |                                                                                |
       +--------------------------------------------------------------------------------+

```

**Workflow:**
1.  A user sends a registration or login request to the **Kong API Gateway** (e.g., `POST /auth/v1/login`).
2.  Kong identifies the `/auth` path and **routes the request** to the Authentication Service.
3.  The service's `REST Controller` processes the request:
    *   It validates the user's credentials against the **MySQL database**.
    *   It generates a JWT access token and a refresh token.
    *   Upon successful new registration, it publishes a `UserInfoEvent` to a **Kafka topic**.
4.  The service returns the tokens to the client via the API Gateway.
5.  The client can then use the JWT in the `Authorization` header for subsequent requests to other protected services (e.g., User or Expense Service), which will be validated at the gateway level.

## Code Repository Structure

```
com.chelv
├── auth                    # Spring Security configuration (JWT Filter, SecurityConfig)
├── authentication_service  # Main application entry point
├── controller              # REST API controllers (AuthController, TokenController)
├── entities                # JPA entities (UserInfo, UserRole, RefreshToken)
├── model                   # DTOs and event models (UserInfoDTO, UserInfoEvent)
├── pubsub                  # Kafka producer for publishing events
├── repository              # Spring Data JPA repositories
├── request                 # DTOs for incoming API requests
├── response                # DTOs for API responses
└── service                 # Core business logic
    ├── token               # JWT and Refresh Token services
    └── user                # User details service and custom UserDetails implementation
```

## Key Features

| Feature                  | Description                                                                                                                                                             |
| ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **User Registration**    | Securely signs up new users, hashes their passwords using BCrypt, and publishes a user creation event to Kafka.                                                           |
| **User Login**           | Authenticates users with a username and password and returns a JWT access token and a refresh token upon success.                                                       |
| **Token-Based Auth (JWT)** | Implements stateless authentication using JSON Web Tokens. A custom filter validates the JWT on every request to a protected endpoint.                                    |
| **Token Refresh**        | Provides an endpoint (`/auth/v1/refreshToken`) to issue a new access token using a valid, non-expired refresh token, enhancing security and user experience.               |
| **Event-Driven Integration** | On new user registration, it publishes an event to a Kafka topic, allowing other microservices to react to the new user in a decoupled fashion.                            |
| **Centralized Security Config** | Utilizes Spring Security to manage security policies, including defining public vs. private endpoints and configuring the authentication provider.               |

## Design and Architectural Principles

*   **Single Responsibility Principle:** The service is solely focused on authentication and identity management, delegating all other concerns (e.g., user profiles, application logic) to other services.
*   **Statelessness:** The service itself is stateless. All necessary information for authenticating a request is contained within the JWT, making it highly scalable.
*   **Loose Coupling:** By using Kafka for inter-service communication, the Authentication Service is decoupled from its downstream consumers. It does not need to know which services exist or how they will use the user data.
*   **Security First:** Passwords are never stored in plain text and are always hashed using a strong algorithm (BCrypt). Short-lived access tokens and a secure refresh mechanism are used to minimize the risk of token hijacking.
*   **API Contract:** The use of Data Transfer Objects (DTOs) for all API requests and responses ensures a stable and well-defined contract for clients.

---
