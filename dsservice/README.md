# README: Data Science Service

## Service Definition

The **Data Science Service** is an intelligent, Python-based microservice designed to perform Natural Language Processing (NLP). Its primary function is to receive unstructured text messages (such as bank transaction SMS), extract structured financial data from them using a powerful Large Language Model (LLM), and publish this structured data for consumption by other backend services.

## Core Objectives

*   **Automate Data Entry:** To automatically convert unstructured text from sources like SMS alerts into structured, actionable expense data, eliminating the need for manual user input.
*   **Bridge Unstructured and Structured Data:** To act as an intelligent pipeline that ingests raw text and outputs clean, validated, and structured JSON objects.
*   **Leverage State-of-the-Art AI:** To utilize the advanced capabilities of Google's Gemini Pro model for high-accuracy information extraction.
*   **Enable Event-Driven Expense Creation:** To produce events that can be consumed by other services (like the Expense Service) to create new records in an asynchronous, decoupled manner.

## Architectural Overview

This service is built using Python and the Flask web framework. It exposes a single REST API endpoint to accept text messages. The core logic is powered by the LangChain framework, which orchestrates calls to Google's Gemini LLM to perform structured data extraction. Upon successful extraction, the service acts as a Kafka producer, sending the structured expense data to a dedicated topic.

## Tech Stack and Tools

| Component           | Technology / Tool                                     |
| ------------------- | ----------------------------------------------------- |
| **Framework**       | Flask                                                 |
| **Language**        | Python                                                |
| **AI/ML Framework** | LangChain                                             |
| **LLM Provider**    | Google Gemini Pro                                     |
| **Data Validation** | Pydantic                                              |
| **Event Streaming** | Apache Kafka (Producer)                               |
| **API**             | REST                                                  |

## System Architecture

```
+--------------------------+
|  User's Mobile Device    |
| (with SMS forwarding app)|
+------------+-------------+
             |
             | (1) Forwards bank transaction SMS
             |     as a POST request
             v
+------------------------------------------------------------------------------------------------------------------+
|                                  DATA SCIENCE SERVICE                                                            |
|                                                                                                                  |
| +---------------------+  (2) Receives  +--------------------+  (3) Filters &  +----------+---------+             |
| |     Flask API       +-------------->|   MessageService   +----------------> |   LLMService       |             |
| | (/v1/ds/message)    |              | (Orchestration)    |                   | (LangChain/Gemini) |             |
| +---------------------+              +--------------------+                   +--------+-----------+             |
|                                                                                        | (4) Extracts data       |
|                                                                                        | into Pydantic model     |
|                                                                                        v                         |
| +---------------------+  (6) Publishes +--------------------+                 +--------------------+             |
| |   Kafka Producer    <--------------+ |     JSON Data      <---------------+ |    Expense         |             |
| +---------------------+   structured   | (Serialized from   |                 | (Pydantic Model)   |             |
|                                        |  Pydantic model)   |                 +--------------------+             |
|                                        +--------------------+                                                    |
+------------------------------------------------------------------------------------------------------------------+
             |
             | (7) Message sent to Kafka topic
             v
+--------------------------+      +---------------------------+
|      Apache Kafka        |      |      Expense Service      |
| (Topic: expense_service) +----->| (Consumes the message     |
+--------------------------+      |  to create a new expense) |
                                  +---------------------------+

```

**Workflow:**
1.  An external service (e.g., an app on a user's phone) forwards an SMS message to the `/v1/ds/message` API endpoint.
2.  The `MessageService` receives the raw text.
3.  A utility filters the message to confirm it is likely a bank transaction.
4.  The `LLMService` sends the text to the Google Gemini model via LangChain, instructing it to return a structured JSON object matching the `Expense` Pydantic model.
5.  The LLM returns the extracted data (amount, merchant, currency).
6.  The Flask application serializes this structured data into a JSON payload.
7.  The Kafka producer publishes this JSON message to the `expense_service` topic, where the Expense Service is listening.

## Code Repository Structure

```
.
├── service/
│   ├── Expense.py          # Pydantic model defining the data schema for an expense.
│   ├── llmService.py       # Core logic for interacting with the LangChain and Gemini model.
│   └── messageService.py   # Orchestrates the message filtering and processing workflow.
├── utils/
│   └── messageUtil.py      # Utility for filtering relevant SMS messages.
├── app.py                  # Main Flask application, API endpoint definitions, and Kafka producer logic.
└── Dockerfile              # (To be added) Instructions for containerizing the service.
```

## Key Features

| Feature                       | Description                                                                                                                                                                             |
| ----------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **LLM-Powered Extraction**    | Leverages Google's Gemini Pro, a powerful LLM, to accurately extract expense details (amount, merchant, currency) from unstructured text.                                               |
| **Structured Output**         | Utilizes LangChain's `with_structured_output` feature, which forces the LLM to return a clean, predictable, and validated JSON object based on a Pydantic schema.                       |
| **Event Producer**            | Acts as a producer in the event-driven architecture. Once data is extracted, it is published to a Kafka topic, seamlessly integrating with other backend services.                      |
| **Intelligent Filtering**     | Includes a pre-processing step to filter out irrelevant messages, ensuring that only potential transaction texts are sent to the LLM, optimizing for cost and performance.              |
| **Robust Payload Handling**   | The Flask API includes logic to handle and repair potentially malformed JSON payloads, making the service more resilient to varied client implementations.                              |

## Design and Architectural Principles

*   **AI as a Service:** The entire service is designed as a specialized function—turning text into data. It encapsulates the complexity of interacting with an LLM and provides a simple, clean interface to the rest of the system.
*   **Schema Enforcement:** By using Pydantic as the schema for the LLM's output, the service ensures that the data it produces is always well-structured and validated before being published.
*   **Decoupling:** The service is completely decoupled from its consumers. It publishes data to a Kafka topic without any knowledge of which service (or how many services) will consume it.
*   **Scalability:** As a stateless Python application, the service can be easily containerized and scaled horizontally to handle a high volume of incoming messages.
