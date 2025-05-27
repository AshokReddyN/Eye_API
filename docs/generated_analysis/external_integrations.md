# External System Integrations

This document details the external system integrations used by the application, based on the analysis of `pom.xml` and application configuration files.

## 1. MongoDB Integration

MongoDB is confirmed as the primary database for this application.

-   **Confirmation:**
    *   The `pom.xml` includes the `spring-boot-starter-data-mongodb` dependency, which is the standard Spring Boot starter for MongoDB integration.
    *   All entity classes (e.g., `Hospital`, `Patient`, `User`) are annotated with `@Document`, specifying MongoDB collection names.
    *   Application configuration files (`application.yml` and its variants) contain explicit `spring.data.mongodb` properties for connection details.

-   **Connection Configuration by Environment:**

    *   **Default (`application.yml`):**
        *   `spring.data.mongodb.uri`: `mongodb+srv://nayonikaeyecare:b7yjInwQUxCCghBT@nayonikacluster.fqrcfog.mongodb.net/nayonikaDev`
        *   `spring.data.mongodb.database`: `nayonikaDev`
        *   **Note:** This configuration contains hardcoded credentials and points to the `nayonikaDev` database. This serves as a fallback if no specific profile is active or if environment variables are not set for other profiles.

    *   **Development (`application-dev.yml`):**
        *   `spring.data.mongodb.uri`: `${MONGODB_URI}`
        *   `spring.data.mongodb.database`: `${MONGODB_DATABASE}`
        *   **Note:** Connection details are expected to be provided via environment variables (`MONGODB_URI` and `MONGODB_DATABASE`). This is a good practice for development environments, allowing flexibility.

    *   **QA (`application-qa.yml`):**
        *   `spring.data.mongodb.uri`: `${MONGODB_URI}`
        *   `spring.data.mongodb.database`: `${MONGODB_DATABASE}`
        *   **Note:** Similar to the development environment, QA uses environment variables for database configuration, allowing for a dedicated QA database instance.

    *   **Production (`application-prod.yml`):**
        *   `spring.data.mongodb.uri`: `mongodb+srv://nayonikaeyecare:b7yjInwQUxCCghBT@nayonikacluster.fqrcfog.mongodb.net/nayonikaDev`
        *   `spring.data.mongodb.database`: `nayonikaDev`
        *   **Critical Security Note:** This profile **hardcodes credentials** and, alarmingly, points to the `nayonikaDev` database. **Hardcoding production credentials directly in configuration files is a significant security risk.** Production environments should always use secure methods for managing sensitive information, such as environment variables sourced from a secure vault or secrets management system (e.g., HashiCorp Vault, AWS Secrets Manager, Azure Key Vault). Furthermore, the production profile should connect to a dedicated production database, not a development database.

-   **Security Considerations for MongoDB:**
    *   The use of placeholders (`${...}`) for URI and database name in `dev` and `qa` profiles is a good practice, promoting secure and flexible configuration.
    *   The hardcoded credentials in `application.yml` (default) and especially in `application-prod.yml` are a major security concern. These credentials could be inadvertently exposed if the codebase is compromised or if the files are not handled with strict access controls. **It is strongly recommended to remove hardcoded credentials from all configuration files and use environment variables or a dedicated secrets management solution for all environments, especially production.**

## 2. Other Potential Integrations

-   Based on the `pom.xml` and the analyzed application configuration files (`application.yml`, `application-dev.yml`, `application-prod.yml`, `application-qa.yml`):
    *   There are **no other explicit external system integrations** configured or clearly indicated.
    *   The dependencies and configurations primarily revolve around:
        *   Spring Boot framework (web, security, data-mongodb, validation).
        *   Lombok for code generation.
        *   JWT libraries for token-based authentication.
        *   Testing libraries (including for MongoDB).
    *   No dependencies or configuration properties were found that would suggest direct integration with:
        *   Message Queues (e.g., Kafka, RabbitMQ).
        *   Caching Services (e.g., Redis, Memcached).
        *   Email Services (e.g., SMTP configurations, SendGrid, AWS SES).
        *   Specific third-party APIs for services like SMS (e.g., Twilio, Fast2SMS), payment processing, or other specialized functions.
        *   Other cloud provider services beyond database hosting (e.g., specific AWS S3, Azure Blob Storage configurations, etc., were not present).

-   While some service class names might imply functionality (e.g., sending OTPs which might eventually use an SMS gateway), the actual integration points for such services are not defined in the provided configuration files or `pom.xml`. Any such integrations would be implemented directly in the Java code, potentially with hardcoded client details if not using Spring's configuration system.

In summary, MongoDB is the only clearly defined and configured external system integration evident from the provided files.I have created the `external_integrations.md` document.

It covers:
1.  **MongoDB Integration:**
    *   Confirms MongoDB as the primary database using `pom.xml` and configuration files.
    *   Details connection configurations for default, dev, qa, and prod environments.
    *   Highlights the use of placeholders (`${MONGODB_URI}`, `${MONGODB_DATABASE}`) in `dev` and `qa` profiles.
    *   Points out the hardcoded credentials in `application.yml` (default) and `application-prod.yml`.
    *   Includes a critical security note regarding the hardcoded production credentials and the `prod` profile pointing to a `dev` database.
2.  **Other Potential Integrations:**
    *   States that no other explicit external integrations (message queues, caching, specific third-party APIs like SMS gateways, other cloud services) were found in the `pom.xml` or the application configuration files.
    *   Acknowledges that while service functionalities might imply such integrations (like OTP sending), the configuration for these is not present in the analyzed files.

The document emphasizes the security risks associated with hardcoded credentials, especially in the production profile.
