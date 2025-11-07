# Development Log

## Day 1-2: (M1) Project Setup & E2E Dockerized Flow

### What I Accomplished:
* **[MILESTONE 1 COMPLETE]** Successfully built and ran the entire multi-service application (Gateway, Worker, Kafka, MySQL) using a single `docker-compose up --build` command.
* Established a full, end-to-end (E2E) message pipeline:
    1.  `REST Client` -> `POST /jobs` (HTTP)
    2.  `gateway-service` (Docker) -> `kafka` (Docker)
    3.  `worker-service` (Docker) -> `✅✅✅ Log Output` (Success!)
* Set up a professional multi-module Maven project (`parent`, `gateway`, `worker`).
* Created a secure `.env` file for credentials, used by both `docker-compose.yaml` and local Spring Boot `application.properties` (using `${VAR:default_value}` syntax).

### Key Challenges & Solutions:

**1. Challenge: The "JDK War" (Local Debugging)**
* **Problem:** VS Code's Java Language Server (JDT-LS) failed to initialize, showing errors on basic Java/Spring imports.
* **Diagnosis:** The local `java -version` was `23`, but the `pom.xml` required `17`. The plugin couldn't find a valid JDK.
* **Solution:** Installed a dedicated JDK 17 (MSI) and manually configured VS Code's `settings.json` to force the plugin to use it. This immediately stabilized the IDE.
    ```json
    "java.jdt.ls.java.home": "C:\\Program Files\\...\\jdk-17.0.17"
    ```

**2. Challenge: The "Silent Worker" (Local Debugging)**
* **Problem:** The `worker` service started but did not consume any Kafka messages.
* **Diagnosis (Self-Resolved):** The `consumer` package (containing the `@KafkaListener`) was placed in the wrong directory (`project-root/worker` instead of `project-root/worker/src/main/java/com/app/worker/`).
* **Solution:** Moved the `consumer` package to the correct location. Spring Boot's Component Scan was then able to find and register the listener.

**3. Challenge: The "Zombie" Worker (Local Debugging)**
* **Problem:** The `worker` service would start and then immediately exit, returning to the command prompt.
* **Diagnosis:** A Spring Boot application with *only* background tasks (like `@KafkaListener`) and no "foreground" task (like `starter-web`) will exit when the `main` method finishes.
* **Solution:** Added `spring-boot-starter-web` and `spring-boot-starter-actuator` to the `worker`'s `pom.xml` and set `server.port=8081` to prevent conflicts. This keeps the process alive and makes it health-checkable (a production best practice).

**4. Challenge: Docker Build Fail 1 (`mvnw: not found`)**
* **Problem:** `docker-compose up --build` failed with `/bin/sh: 1: ./mvnw: not found`.
* **Diagnosis:** The `Dockerfile` was trying to execute `./mvnw`, but this file only existed locally; it had not been `COPY`'d into the Docker build context.
* **Solution:** Added `COPY .mvn/ ./.mvn` and `COPY mvnw .` to both `gateway/Dockerfile` and `worker/Dockerfile` **before** the `RUN ./mvnw...` command.

**5. Challenge: Docker Build Fail 2 (`Child module... not found`)**
* **Problem:** The `gateway` build failed because Maven couldn't find the `worker` module (and vice-versa).
* **Diagnosis:** A multi-module Maven build needs *all* `pom.xml` files to understand the project structure, even when building only one module.
* **Solution:** Added `COPY gateway/pom.xml ./gateway/` AND `COPY worker/pom.xml ./worker/` to **both** Dockerfiles.

**6. Challenge: Docker Build Fail 3 (`no main manifest attribute`)**
* **Problem:** The `.jar` files were built but were not "executable" (`java -jar app.jar` failed).
* **Diagnosis:** The child modules (`gateway`, `worker`) did not inherit the `spring-boot-maven-plugin` configuration from the parent POM.
* **Solution:** Added a `<build><pluginManagement>...</pluginManagement>` section to the **parent `pom.xml`** to explicitly define the `spring-boot-maven-plugin` version and `repackage` goal for all child modules.

### Key Learnings:
* A "one-click-start" with `docker-compose` is the goal.
* Multi-module Docker builds require careful handling of the build context (copying all POMs, but only the `src` of the target module).
* Parent POMs are critical for managing not just `dependencies` but also `plugins`.
* Spring Boot's Component Scan (`@ComponentScan`) is limited to the main class's package and its sub-packages.