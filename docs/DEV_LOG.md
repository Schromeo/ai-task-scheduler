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

## Day 2: Nov 06, 2025 (M1 - Worker & Milestone Complete)

### What I Accomplished:
* Created the `worker` module from scratch, adhering to the multi-module Maven structure.
* Implemented the `JobListener` service using `@KafkaListener` to consume messages from the `topic.jobs`.
* **[MILESTONE 1 COMPLETE]** Successfully created a full, end-to-end (E2E) message pipeline:
    1.  `REST Client` -> `POST /jobs` (HTTP)
    2.  `Gateway` (8080) -> `Kafka (topic.jobs)`
    3.  `Worker` (8081) -> `✅✅✅ [Worker] Log` (Success!)

### Key Challenges & Solutions:

**1. Challenge: The "Zombie" Worker Process**
* **Problem:** The initial `worker` (without `spring-boot-starter-web`) was a "background" process. It "died" immediately after `main` finished, **but** its "zombie" process (`java.exe`) **still held onto port 8081**.
* **Error:** `Port 8081 was already in use.`
* **Solution:**
    1.  Upgraded the `worker` to be a "real" web service by adding `spring-boot-starter-web` and `spring-boot-starter-actuator`. This forces it to "stay alive" (blocking the main thread) to serve health checks.
    2.  Used `netstat -aon | findstr "8081"` (PowerShell) to find the "zombie" PID.
    3.  Used `taskkill /PID <zombie_pid> /F` to kill the process and free the port.

**2. Challenge: The "Silent" Worker (The Final Boss)**
* **Problem:** The `worker` started correctly on 8081, `gateway` sent the message, but the `worker`'s `@KafkaListener` **never fired**. No `✅✅✅` logs appeared.
* **Diagnosis (My "A-Ha!" Moment):** I realized I had created the `consumer` package (`JobListener.java`) in the **wrong directory**.
* **Solution:** I moved the `consumer` package from `project-root/consumer` to **inside** the correct Spring Boot scan path: `worker/src/main/java/com/app/worker/consumer`.
* **Result:** Spring Boot's "Component Scan" finally "found" the `@Service` (`JobListener`), activated the `@KafkaListener`, and the messages immediately came flooding in!

### Key Learnings:
* **"Port in use"** is often caused by "zombie" processes from previous failed runs. `netstat` + `taskkill` is the essential fix.
* A "background-only" Spring Boot app (like a Kafka listener) **must** also be a "web" app (`starter-web`) to prevent the main thread from exiting prematurely.
* Spring Boot's **Component Scan** is "silent but deadly". If you put a file in the wrong package (outside the `main` application's sub-packages), it **will not** be "seen", and it **will not** error—it will just "do nothing".