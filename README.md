# Tattoo Scheduler Backend 

## Project Motivation

This project was built to support a real tattoo artist’s booking workflow.

It provides a backend system for managing client registration, authentication, 
and booking scheduling based on business constraints.

The project was also used to apply production-oriented practices such as layered 
architecture, validation, and automated testing.

## Overview 

Backend application for managing bookings for a tattoo artist. 

Supports user registration, authentication via JWT, booking creation and generation 
of available session slots on chosen session type and date. 

Designed with layered architecture and tested with unit, integration and end-to-end tests. 

## Features 

- User registration and authentication (JWT) 
- Create and manage bookings 
- Availability calculation based on working hours, 
session types and artist's rest/maintenance time buffer 
- Validation (including custom age validation) 
- Global exception handling 
- Unit, integration and end-to-end testing 

## Tech stack 

- Java 21
- JJWT 
- Spring Boot 
- Spring Security 
- PostgreSQL 
- Hibernate / JPA 
- MapStruct 
- Lombok 
- Testcontainers 
- JUnit 5 / AssertJ / Mockito / MockMvs 

## Architecture 

Controller → Service → Repository 
- Controllers handle HTTP requests 
- Services contain business logic 
- Repositories interact with the database 
- Authentication is handled via JWT and Spring Security filter chain 

## API examples
 
### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
-H "Content-Type:application/json" \
-d '{"name":"Vasya","email":"vasya_huligan228@gmail.com", \
"password":"qwerty12345","phoneNumber":"88005553535","birthDate":"2000-02-02"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type:application/json" \
-d '{"email":"vasya_huligan228@gmail.com","password":"qwerty12345"}'
```

### Get available session slots for desired date

```bash
curl -X GET "http://localhost:8080/api/availability?date=2026-05-15&sessionType=MEDIUM"
```

### Create booking

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_token_here>" \
  -d '{
    "sessionType": "MEDIUM",
    "startTime": "2026-05-15T10:00:00",
    "notes": "First tattoo"
  }'
```

## How to run 

1. Clone repository 
2. Start PostgreSQL:
```bash
docker-compose up -d
```
3. Start application:
```bash 
docker-compose up -d 
./mvnw spring-boot:run 
```
4. Use provided curl examples 

## What I learned 

- Designing layered backend architecture 
- Implementing JWT authentication with Spring Security 
- Writing unit, integration, and end-to-end tests 
- Handling validation and custom constraints 
- Working with Hibernate and PostgreSQL
- Implemented and analyzed common OOP/design patterns in the context of the project, 
including Strategy, Template Method, Factory, and Facade. 
- Working with Bash, curl, jq, and Docker for local development and testing

## Future Improvements

### PostgreSQL Range Types and Overlap Constraints

Currently, booking availability is calculated using separate `startTime` and `endOfBufferTime` fields with application-level overlap checks.

A potential improvement is to leverage **PostgreSQL range types** (e.g., `tstzrange`) to represent booking intervals as a single value.

This allows using built-in range operators such as `&&` (overlaps), simplifying query logic and reducing the risk of edge-case errors.

**Example:**

```sql
SELECT *
FROM bookings
WHERE period && tstzrange(:start, :end);
```
Additionally, a **GiST index** can be applied to optimize overlap queries:

```sql
CREATE INDEX idx_booking_period ON bookings USING GiST (period);
```
**Enforcing Non-Overlapping Bookings**
- To guarantee that bookings for the same artist do not overlap, a database-level constraint can be introduced using an exclusion constraint:

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE bookings ADD CONSTRAINT no_overlap_booking
EXCLUDE USING gist (
    artist_id WITH =,
    tstzrange(start_time, end_of_buffer_time) WITH &&
);
```
This ensures that no two bookings for the same artist can have overlapping time intervals.
**Benefits:**
- ✅ Simplifies overlap detection logic
- ✅ Eliminates race conditions
- ✅ Enforces business rules at the database level
- ✅ Improves data integrity and reliability