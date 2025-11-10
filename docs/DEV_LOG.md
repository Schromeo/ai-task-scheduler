# Development Log

## Day 1: M1 - Project Setup & E2E Kafka Flow

**Objective:** Build the foundational structure, ensuring a message can be sent from a Gateway, through Kafka, and consumed by a Worker service, all running locally.

### Key Accomplishments:
* Set up Git repo, `docker-compose.yaml` (for infra), and `.env` for secrets.
* Established a multi-module Maven project (parent, gateway, worker).
* **[M1 COMPLETE (Local)]** Successfully built the E2E flow:
    1.  `REST Client` -> `POST /jobs` (HTTP)
    2.  `Gateway` (Local 8080) -> `Kafka (Docker 9092)`
    3.  `Worker` (Local 8081) -> `✅✅✅ [Worker] "球"接到了!` (Success!)

### Key Challenges & Solutions (Local):

* **Challenge: The "JDK War"**
    * **Problem:** VS Code JDT-LS (Java plugin) failed to initialize.
    * **Diagnosis:** Local `java -version` was 23, but `pom.xml` required 17 (LTS).
    * **Solution:** Installed JDK 17 (MSI) and manually configured VS Code's `settings.json` (`java.jdt.ls.java.home`) to force the plugin to use the correct JDK.

* **Challenge: The "Silent" Worker**
    * **Problem:** `worker` started but never consumed Kafka messages.
    * **Diagnosis (Self-Resolved):** The `consumer` package was placed in the wrong directory (`project-root/worker` instead of `project-root/worker/src/main/java/com/app/worker/`).
    * **Solution:** Moved the `consumer` package to the correct location (`com.app.worker.consumer`). This allowed Spring Boot's Component Scan to find and register the `@KafkaListener`.

---

## Day 2: M2 - DB Persistence, Robustness & Dockerization

**Objective:** Replace the volatile Kafka queue (M1) with a persistent MySQL database (M2), add a "lease" mechanism for robustness (M2.5), and containerize the entire application for a "one-click start".

### What I Accomplished:
* **Created `common` module:** Added `Job.java` (JPA @Entity) and `JobStatus.java` (enum).
* **Refactored `gateway`:**
    * Removed `spring-kafka` dependency.
    * Added `common` and `spring-data-jpa` dependencies.
    * Modified `JobsController` to save new jobs to MySQL with `PENDING` status.
* **Refactored `worker`:**
    * Removed `@KafkaListener` and added `@Scheduled` to poll the database.
    * Added `common` and `spring-data-jpa` dependencies.
    * Implemented a "lease" mechanism:
        1.  `Job` entity updated with `leaseExpiresAt` timestamp.
        2.  Repository query (`findNextAvailableJob`) now fetches `PENDING` jobs OR `RUNNING` jobs where `leaseExpiresAt < NOW()`.
        3.  `JobProcessor` now sets a 5-minute lease when starting a job, and updates status to `COMPLETED` or `FAILED` upon finishing. This handles worker crashes and ensures at-least-once execution.
* **[M2 & M2.5 COMPLETE (Docker)]** Successfully ran the entire M2 flow within Docker Compose.

### Key Challenges & Solutions (Dockerizing):

* **Challenge: Docker Build Fail 1 (`mvnw: not found`)**
    * **Problem:** `docker-compose up --build` failed because `RUN ./mvnw ...` couldn't find the `mvnw` executable.
    * **Solution:** Added `COPY .mvn/ ./.mvn` and `COPY mvnw .` to both `Dockerfile`s (in the `builder` stage).

* **Challenge: Docker Build Fail 2 (`Child module /app/common... not found`)**
    * **Problem:** Maven's reactor failed because when building `gateway` or `worker`, it couldn't find the `common` module's files.
    * **Solution:** Updated both `Dockerfile`s to copy *all* module `pom.xml` files (`common`, `gateway`, `worker`) and the `src` for *all* required dependencies (e.g., `gateway`'s build copies both `gateway/src` and `common/src`).

* **Challenge: Docker Build Fail 3 (`no main manifest attribute`)**
    * **Problem:** The `.jar` files were built but were not "executable".
    * **Solution:** Added a `<build><pluginManagement>...</pluginManagement>` block to the **parent `pom.xml`** to ensure the `spring-boot-maven-plugin` (with the `repackage` goal) was correctly configured and versioned for all child modules.

* **Challenge: Docker Runtime Fail 4 (`Connection refused`)**
    * **Problem:** `gateway` and `worker` services crashed on startup, unable to connect to MySQL.
    * **Diagnosis:** A "race condition". The Java apps started *faster* than the `mysql` container was ready to accept connections.
    * **Solution:** Added a `healthcheck` to the `mysql` service in `docker-compose.yaml` and changed the `gateway` / `worker` `depends_on` to use `condition: service_healthy`. This forces them to wait until MySQL is *actually* ready.