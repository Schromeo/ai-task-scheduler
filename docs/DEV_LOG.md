# Development Log

## Day 1: Nov 06, 2025 (Project Setup & M1 - Gateway)

### What I Accomplished:
* Established the complete project foundation: Git repository, Docker Compose (MySQL, Kafka), and security (`.env`, `.gitignore`).
* Successfully structured the project as a multi-module Maven build (Parent POM + `gateway` module).
* Implemented the `gateway` service's `POST /jobs` API endpoint using Spring Boot (`@RestController`).
* Configured the `gateway` service to connect to Kafka (`KafkaTemplate`) and MySQL (`Spring Data JPA`).
* **[SUCCESS]** The `POST /jobs` endpoint successfully receives requests, logs them, and publishes the payload to the `topic.jobs` Kafka topic.

### Key Challenges & Solutions:

**1. Challenge: The "JDK War"**
* **Problem:** My local environment had Java 23 installed, but the project `pom.xml` requires Java 17 (LTS). This caused the VS Code Java Language Server (JDT-LS) to fail initialization.
* **Pitfall:** I first attempted a manual download and mistakenly downloaded Java 8.
* **Solution:**
    1.  Installed a clean **JDK 17 (MSI)** to ensure proper system registration.
    2.  "Hard-coded" the VS Code `settings.json` to force the language server to use the correct JDK path, bypassing the "stuck" auto-detection:
        ```json
        "java.jdt.ls.java.home": "C:\\Program Files\\...\\jdk-17.0.17"
        ```
    3.  This immediately stabilized the IDE.

**2. Challenge: MySQL `Access Denied`**
* **Problem:** The `gateway` service failed to start, throwing a `java.sql.SQLException: Access denied for user '${DB_USER}'`. The environment variables from `launch.json` were not being injected correctly during startup.
* **Solution:**
    1.  Refactored `application.properties` to use Spring's **default value syntax**:
        ```properties
        spring.datasource.username=${DB_USER:user}
        spring.datasource.password=${DB_PASS:password}
        ```
    2.  This provides a fallback for local development (which fixed the issue) while still allowing environment variables to override it in production.

### Key Learnings:
* Never underestimate environment configuration. Manually verifying the Java Language Server's JDK (`java.jdt.ls.java.home`) is a critical debugging step for VS Code.
* The `REST Client` (`.http` file) extension in VS Code is a highly efficient workflow for API testing, removing the need to leave the IDE.
* The `${VAR:default_value}` syntax is a powerful and safe pattern for local development properties.