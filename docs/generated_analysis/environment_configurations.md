# Environment-Specific Configurations

This document outlines the application configurations specific to different environments (default, dev, qa, prod), based on the analysis of `application.yml`, `application-dev.yml`, `application-qa.yml`, and `application-prod.yml`.

## 1. MongoDB Connection Details

Configuration for `spring.data.mongodb.uri` and `spring.data.mongodb.database`:

-   **Default (`application.yml`):**
    *   `uri`: `mongodb+srv://nayonikaeyecare:b7yjInwQUxCCghBT@nayonikacluster.fqrcfog.mongodb.net/nayonikaDev` (Hardcoded value)
    *   `database`: `nayonikaDev` (Hardcoded value)
    *   *Note:* Contains hardcoded credentials, apparently for a development database.

-   **Development (`application-dev.yml`):**
    *   `uri`: `${MONGODB_URI}` (Placeholder, expects environment variable)
    *   `database`: `${MONGODB_DATABASE}` (Placeholder, expects environment variable)
    *   *Note:* Promotes better security and flexibility by using environment variables.

-   **QA (`application-qa.yml`):**
    *   `uri`: `${MONGODB_URI}` (Placeholder, expects environment variable)
    *   `database`: `${MONGODB_DATABASE}` (Placeholder, expects environment variable)
    *   *Note:* Similar to 'dev', uses environment variables for QA-specific database setup.

-   **Production (`application-prod.yml`):**
    *   `uri`: `mongodb+srv://nayonikaeyecare:b7yjInwQUxCCghBT@nayonikacluster.fqrcfog.mongodb.net/nayonikaDev` (Hardcoded value)
    *   `database`: `nayonikaDev` (Hardcoded value)
    *   **Security Concern:** This profile contains **hardcoded production credentials**, which is a significant security risk. Furthermore, it points to the `nayonikaDev` database, which is inappropriate for a production environment. Production should use unique, strong credentials and a dedicated production database, configured via secure means like environment variables or a secrets management system.

## 2. Server Port

Configuration for `server.port`:

-   **Default (`application.yml`):**
    *   `server.port`: `8081`

-   **Development (`application-dev.yml`):**
    *   `server.port`: `8080`

-   **QA (`application-qa.yml`):**
    *   `server.port`: `8080`

-   **Production (`application-prod.yml`):**
    *   `server.port`: `8084`

*Summary: Each environment (or default) is configured to run on a distinct server port, which is useful for avoiding conflicts when multiple environments might be running on the same host or for routing purposes.*

## 3. Logging Levels

Configuration for `logging.level.root` and `logging.level.com.nayonikaeyecare`:

-   **Default (`application.yml`):**
    *   No explicit logging levels are set, so Spring Boot's default logging level (usually INFO for root) would apply.

-   **Development (`application-dev.yml`):**
    *   `logging.level.root`: `DEBUG`
    *   `logging.level.com.nayonikaeyecare`: `DEBUG`
    *   *Note:* Enables verbose logging for development, which is helpful for debugging.

-   **QA (`application-qa.yml`):**
    *   `logging.level.root`: `DEBUG`
    *   `logging.level.com.nayonikaeyecare`: `DEBUG`
    *   *Note:* Similar to 'dev', enables verbose logging for QA testing.

-   **Production (`application-prod.yml`):**
    *   No explicit logging levels are set. Spring Boot's default (usually INFO) will apply. It's common to set production logging to `INFO` or `WARN` to reduce log volume while still capturing important events.

## 4. JWT Configuration

Configuration for `jwt.secret` and `jwt.expiration`:

-   **Default (`application.yml`):**
    *   `jwt.secret`: `"thisisasecretkeythatis32byteslongButNotReallyAndItShouldBeKeptSecret"` (Hardcoded value)
    *   `jwt.expiration`: `3600` (1 hour)

-   **Development (`application-dev.yml`):**
    *   `jwt.secret`: `${JWT_SECRET}` (Placeholder, expects environment variable)
    *   `jwt.expiration`: `3600` (1 hour)

-   **QA (`application-qa.yml`):**
    *   `jwt.secret`: `${JWT_SECRET}` (Placeholder, expects environment variable)
    *   `jwt.expiration`: `3600` (1 hour)

-   **Production (`application-prod.yml`):**
    *   `jwt.secret`: `"thisisasecretkeythatis32byteslongButNotReallyAndItShouldBeKeptSecret"` (Hardcoded value)
    *   `jwt.expiration`: `3600` (1 hour)
    *   **Security Concern:** The JWT secret is **hardcoded in the production profile**. This is a significant security risk. If this secret is compromised, attackers can forge valid JWTs. The production JWT secret should be unique, strong, and managed securely via environment variables or a secrets management system.

## 5. Default Profile Activation

-   The `application.yml` file contains the following setting:
    `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:dev}`
-   **Explanation:**
    *   This line sets the default active Spring profile to `dev`.
    *   If the environment variable `SPRING_PROFILES_ACTIVE` is set when the application starts, its value will override `dev` and activate the specified profile(s).
    *   If `SPRING_PROFILES_ACTIVE` is not set, the application will start with the `dev` profile active by default, thus loading `application-dev.yml` on top of `application.yml`.

## 6. Common Configurations

Several configurations are common across all or most profile files:

-   **`spring.application.name`:**
    *   Set to `api` in `application.yml`. This name is inherited by other profiles unless overridden.

-   **`auth.excluded.path`:**
    *   Set to `/auth,/v3/api-docs,/swagger-ui,/swagger-resources` in all files (`application.yml`, `application-dev.yml`, `application-qa.yml`, `application-prod.yml`).
    *   This indicates that these paths are consistently excluded from JWT authentication filtering across all defined environments, allowing public access for authentication endpoints and API documentation.

-   **`jwt.expiration`:**
    *   Set to `3600` (1 hour) consistently across all profiles where it's defined (`application.yml`, `application-dev.yml`, `application-qa.yml`, `application-prod.yml`). This standardizes token lifetime.

This document summarizes the key configuration differences and commonalities, highlighting important security considerations for managing sensitive data like database credentials and JWT secrets.I have created the `environment_configurations.md` document.

It details:
1.  **MongoDB Connection Details:** How `spring.data.mongodb.uri` and `spring.data.mongodb.database` are configured in default, dev, qa, and prod profiles, noting the use of placeholders versus hardcoded values and reiterating security concerns for hardcoded production credentials.
2.  **Server Port:** Differences in `server.port` across profiles.
3.  **Logging Levels:** Variations in `logging.level.root` and specific package logging for each profile.
4.  **JWT Configuration:** How `jwt.secret` and `jwt.expiration` are set, highlighting placeholder use for secrets in dev/qa and hardcoded secrets in default/prod, with a security warning for production.
5.  **Default Profile Activation:** Explanation of `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:dev}` in `application.yml`.
6.  **Common Configurations:** Mentions settings like `spring.application.name` and `auth.excluded.path` that are consistent across profiles.

The document emphasizes security concerns related to hardcoded sensitive information in production configurations.
