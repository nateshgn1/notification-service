# Notification Service

A scalable and extensible notification service built using Spring Boot.  
The system supports priority-based scheduling, configurable retry strategies, bulk ingestion, recurring notifications, and multi-channel delivery (Email, SMS, Push).

---

## 1. Overview

This service is designed to handle notification workflows reliably at scale.  
It separates ingestion from dispatch logic and provides configurable retry policies along with user-level channel preferences.

The architecture emphasizes modularity, extensibility, and clean separation of concerns.

---

## 2. Core Features

- Priority-based processing (HIGH, MEDIUM, LOW)
- Scheduled notifications
- Configurable retry mechanism per channel
- Dead letter handling after retry exhaustion
- Bulk notification ingestion with partial success handling
- Multi-channel delivery (Email, SMS, Push)
- User channel enable/disable preferences
- Optional recurring notifications
- Paginated tracking API
- Unit test coverage ~77% (50+ test cases and 100% of service layer)

---

## 3. Architecture

The system follows a layered architecture:

Controller  
→ Service  
→ Repository  
→ Database

Dispatch flow:

NotificationPoller  
→ NotificationDispatcherService  
→ NotificationChannelFactory  
→ Channel Implementation (Email / SMS / Push)

### Key Components

**NotificationPoller**  
Periodically fetches eligible notifications based on status and scheduled time.

**NotificationDispatcherService**  
Handles dispatch execution, retry logic, dead letter transition, and recurrence scheduling.

**NotificationChannelFactory**  
Resolves channel implementation dynamically based on channel type.

**Channel Implementations**  
Encapsulate channel-specific logic including endpoint validation and provider invocation.

**UserPreferenceService**  
Validates whether a notification channel is enabled for a user.

**RetryProperties**  
Allows configurable retry counts per channel via application configuration.

---

## 4. Priority Handling

Eligible notifications are selected using:

- Status filter (CREATED / FAILED)
- Scheduled time eligibility
- Sorted by:
    - priorityWeight DESC
    - createdAt ASC

This ensures higher priority notifications are processed first within the ready set.

---

## 5. Retry and Dead Letter Strategy

Each notification maintains:

- retryCount
- maxRetries
- nextRetryAt

On dispatch failure:
- retryCount increments
- nextRetryAt is scheduled

If retryCount exceeds maxRetries:
- Status transitions to DEAD_LETTER

Retry counts are configurable per channel.

---

## 6. Bulk Ingestion

The bulk API:

- Accepts multiple notifications in a single request
- Enforces configurable maximum bulk size
- Supports partial success
- Returns accepted and rejected items separately

---

## 7. Recurring Notifications

If `recurrenceIntervalMinutes` is provided:

- After successful dispatch, the notification is rescheduled
- retryCount is reset
- Status remains CREATED

---

## 8. Technology Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- MySQL 8.0
- Flyway 9.x
- Maven 3.x
- Docker 24.x
- JUnit 5
- Mockito

---

## 9. Prerequisites

Before running the application, ensure the following are installed:

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

---

## 10. Running the Application

### Step 1: Start MySQL (Docker)
docker-compose up -d

This starts a MySQL 8 container with the configured database.

### Step 2: Build the Project
mvn clean install

### Step 3: Run the Application
mvn spring-boot:run

Application runs at: http://localhost:8080
Default active profile: `dev`

---

## 11. API Documentation (Swagger)

When running with the `dev` profile:
http://localhost:8080/swagger-ui.html
---

## 12. Testing

Run all unit tests: 

mvn test
Includes tests for:

- Service layer
- Dispatcher logic
- Poller
- Channel implementations
- Bulk validation
- Retry behavior
- Dead letter handling

---

## 13. Design Considerations

- Extensible channel design using factory pattern
- Retry logic isolated from controller layer
- Separation of ingestion and dispatch concerns
- Optimized database indexing for polling
- Configurable retry strategy per channel
- Designed to be horizontally scalable with distributed locking (future enhancement)

---

## 14. Future Improvements

- Distributed locking for multi-instance deployments
- Event-driven dispatch using message queues
- Rate limiting per channel
- Metrics and observability integration
- Dead letter queue monitoring
- High-Level Design (HLD) documentation
- Class diagrams
- Database schema documentation

---

## Author

Natesh G N

