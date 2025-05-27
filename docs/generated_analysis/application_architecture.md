# Application Architecture and Feature Summary

This document provides a summary of the application's core features and an overview of its architectural layers. The application appears to be a backend system for Nayonika Eye Care, managing hospitals, patients, referrals, vision ambassadors, and user authentication, likely for an eye care management platform.

## Core Features

The application supports the following core functionalities:

### 1. Hospital Management
- **CRUD Operations:** Create, Read (single, all, paginated), Update, and Delete hospitals.
- **Filtering & Pagination:** Search and retrieve hospitals based on criteria like state, city, status, name, and services offered, with paginated results.
- **Bulk Upload:** Import multiple hospital records from a data source (e.g., JSON).

### 2. Patient Management
- **CRUD Operations:** Create, Read (single, all, paginated), Update, and Delete patient records.
- **Filtering & Pagination:** Search and retrieve patients based on criteria like state, city, name, and associated Vision Ambassador, with paginated results.
- **Association with Vision Ambassador:** Link patients to specific Vision Ambassadors.
- **Patient Reports/Referrals:** Retrieve reports or referrals associated with a patient, optionally filtered by hospital.

### 3. Referral Management
- **CRUD Operations:** Create, Read (single, all, paginated), Update, and Delete referrals.
- **Association:** Link referrals to patients, hospitals, and Vision Ambassadors.
- **Targeted Retrieval:** Fetch referrals based on Vision Ambassador ID, hospital ID (with pagination), or patient ID.
- **Filtering & Pagination:** Search and retrieve referrals based on criteria like ambassador ID, state, city, status, and name, with paginated results.

### 4. Vision Ambassador Management
- **CRUD Operations:** Create, Read (single, all, paginated), Update, and Delete Vision Ambassador records.
- **Filtering & Pagination:** Search and retrieve Vision Ambassadors by state and city, with paginated results.
- **User Association:** Link Vision Ambassadors to system user accounts (retrieve ambassador by User ID).

### 5. User Authentication & Management
- **OTP-Based Authentication:**
    - Request OTP for login (separate flows for general users and Vision Ambassadors).
    - Verify OTP to authenticate and obtain a session token (likely JWT).
    - Resend OTP functionality.
- **User Management:**
    - Update user details.
    - Retrieve user information by User ID.

### 6. Patient Report Management
- **Bulk Import:** Import patient report data from JSON, potentially updating existing records.
- **Status Updates:** Update the status of multiple patient reports simultaneously (e.g., SCREENED, REFERRED, COMPLETED).

## Architectural Layers

The application follows a layered architecture, common in Spring Boot applications, to separate concerns and improve maintainability.

### 1. Controllers (`com.nayonikaeyecare.api.controllers`)
- **Purpose:** Handle incoming HTTP requests from clients. They are the entry point to the application's API.
- **Responsibilities:**
    - Map HTTP request paths and methods (e.g., GET, POST, PUT, DELETE) to specific handler methods.
    - Receive and validate request data (path variables, query parameters, request bodies).
    - Delegate business logic processing to the appropriate Service layer components.
    - Transform results from the Service layer into HTTP responses (often using DTOs) with appropriate status codes.
- **Key Annotations:** `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, `@Valid`.

### 2. Services (`com.nayonikaeyecare.api.services`)
- **Purpose:** Contain the core business logic of the application. They act as an intermediary between Controllers and Repositories.
- **Responsibilities:**
    - Implement the application's use cases and business rules.
    - Orchestrate operations, which may involve calling multiple repositories or other services.
    - Handle data validation and transformation that is specific to business operations.
    - Manage transactions where necessary.
    - Convert between DTOs and Entities (often with the help of Mappers).
- **Examples:** `HospitalService`, `PatientService`, `UserService`, `ReportJsonImportService`.

### 3. Repositories (`com.nayonikaeyecare.api.repositories`)
- **Purpose:** Provide an abstraction layer for data access. They are responsible for all interactions with the database (MongoDB in this case, inferred from `ObjectId` usage).
- **Responsibilities:**
    - Define methods for CRUD (Create, Read, Update, Delete) operations on entities.
    - Implement custom queries for more complex data retrieval needs (often in `Impl` classes using `MongoTemplate`).
    - Encapsulate database-specific logic.
- **Key Technologies/Patterns:** Spring Data MongoDB (extending interfaces like `MongoRepository`), custom repository implementations for advanced queries.

### 4. Entities/Models (`com.nayonikaeyecare.api.entities`)
- **Purpose:** Represent the persistent data models of the application. They are Java objects that map directly to database collections (MongoDB documents).
- **Responsibilities:**
    - Define the structure and attributes of the data being managed by the application.
    - May include relationships between different entities (e.g., a `Patient` having an `Address`).
    - Often annotated with JPA or Spring Data MongoDB annotations (e.g., `@Document`, `@Id`).
- **Examples:** `Hospital`, `Patient`, `User`, `Referral`, `VisionAmbassador`.

### 5. DTOs (Data Transfer Objects) (`com.nayonikaeyecare.api.dto`)
- **Purpose:** Act as simple data carriers for transferring information between layers, especially between Controllers and Services, and in API requests/responses.
- **Responsibilities:**
    - Define the structure of data being sent to or returned from the API, providing a clear contract.
    - Help prevent over-exposure or under-exposure of entity fields by tailoring data to specific use cases.
    - Can be used for request validation.
- **Examples:** `HospitalRequest`, `HospitalResponse`, `AuthenticationRequest`, `PatientReportRequestDto`.

### 6. Mappers (`com.nayonikaeyecare.api.mappers`)
- **Purpose:** Convert data between different object types, primarily between Entities and DTOs.
- **Responsibilities:**
    - Encapsulate the logic for transforming an Entity object into a DTO, and vice-versa.
    - Promote separation of concerns by keeping mapping logic out of services, entities, and DTOs.
    - Improve code readability and maintainability.
- **Examples:** `HospitalMapper`, `PatientMapper`, `ReferralMapper`. (Frameworks like MapStruct might be used, or manual implementations).

### 7. Security
- **Purpose:** Handle authentication, authorization, and other security concerns.
- **Components & Responsibilities:**
    - **`AuthenticationController` (`com.nayonikaeyecare.api.controllers.user`):** Manages OTP-based authentication flows (request, verify, resend OTP).
    - **`UserService` & `VisionAmbassadorService`:** Contain logic for user/ambassador authentication and OTP generation/validation.
    - **`ApplicationUserDetailsService` (`com.nayonikaeyecare.api.services`):** Integrates with Spring Security to load user-specific data for authentication and authorization.
    - **Spring Security Framework:** (Inferred) Provides the underlying mechanisms for security, including `AuthenticationManager`.
    - **Entities:** `User`, `UserCredential`, `UserSession`, `Role`, `Permission` define the data models for users, credentials, sessions, and access control.
    - **Token-based Authorization:** Successful OTP verification likely results in a token (e.g., JWT) which is then used to authorize subsequent API requests. (Inferred from `OTPVerificationResponse` containing a token).
- **Mechanism:** The primary authentication mechanism is OTP (One-Time Password) sent to the user's registered contact (likely phone number).

This layered architecture helps in creating a modular, scalable, and maintainable application.
