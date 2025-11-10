# Development Log

## Day 1: M1 - Project Setup & E2E Kafka Flow (Local)

**Objective:** Build the foundational structure, ensuring a message can be sent from a Gateway, through Kafka, and consumed by a Worker service, all running locally.

### Key Accomplishments:
* Set up Git repo, `docker-compose.yaml` (for infra), and `.env` for secrets.
* Established a multi-module Maven project (parent, gateway, worker).
* **[M1 COMPLETE (Local)]** Successfully built the E2E flow:
    1.  `REST Client` -> `POST /jobs` (HTTP)
    2.  `Gateway` (Local 8080) -> `Kafka (Docker 9092)`
    3.  `Worker` (Local 8081) -> `✅✅✅ [Worker] Log` (Success!)

### Key Challenges & Solutions (Local):

* **Challenge: The "JDK War"**
    * **Problem:** VS Code JDT-LS (Java plugin) failed to start, reporting errors on basic imports.
    * **Diagnosis:** Local `java -version` was 23, but `pom.xml` required 17 (LTS).
    * **Solution:** Installed JDK 17 (MSI) and manually configured VS Code's `settings.json` (`java.jdt.ls.java.home`) to force the plugin to use the correct JDK.

* **Challenge: The "Zombie" Worker**
    * **Problem:** The `worker` service started and immediately exited.
    * **Diagnosis:** A Spring Boot app *without* `spring-boot-starter-web` (or another "blocking" task) will exit when the `main` method finishes, as the `@KafkaListener` runs on a background thread.
    * **Solution:** Added `spring-boot-starter-web` and `spring-boot-starter-actuator` to the `worker` POM and set `server.port=8081` to prevent conflicts. This keeps the JVM alive.

* **Challenge: The "Silent" Worker**
    * **Problem:** The `worker` started, stayed alive, but never consumed Kafka messages.
    * **Diagnosis (Self-Resolved):** The `consumer` package was in the wrong directory, outside the `com.app.worker` base package.
    * **Solution:** Moved the `consumer` package to `worker/src/main/java/com/app/worker/consumer`. This allowed Spring Boot's Component Scan to find and register the `@Service` and `@KafkaListener`.

---

## Day 2: M2 - DB Persistence & "One-Click Start" (Docker)

**Objective:** Replace the volatile Kafka queue (M1) with a persistent MySQL database (M2), and containerize the entire application for a one-click start.

### What I Accomplished:
* **Created `common` module:** Added `Job.java` (JPA @Entity) and `JobStatus.java` (enum) to be shared.
* **Refactored `gateway`:**
    * Removed `spring-kafka` dependency.
    * Added `common` and `spring-data-jpa` dependencies.
    * Created `JobRepository` (JPA).
    * Modified `JobsController` to save new jobs to MySQL with `PENDING` status.
    * Added `@EntityScan("com.app.common.model")` to `GatewayApplication.java` to find the `Job` entity.
* **Refactored `worker`:**
    * Removed `@KafkaListener` (`JobListener.java` -> `JobProcessor.java`).
    * Added `spring-data-jpa`, `mysql-driver`, and `common` dependencies.
    * Added `@Scheduled(fixedRate = 5000)` to poll the database.
    * Implemented `JobRepository` with a custom query (`findNextPendingJob`) to find and lock pending jobs.
    * Job logic now updates job status from `PENDING` -> `RUNNING` -> `COMPLETED`.
* **[M2 COMPLETE (Docker)]** Successfully ran the entire M2 flow within Docker Compose.

### Key Challenges & Solutions (Dockerizing):

* **Challenge: Docker Build Fail 1 (`mvnw: not found`)**
    * **Problem:** `docker-compose up --build` failed because `RUN ./mvnw ...` couldn't find the `mvnw` executable.
    * **Solution:** Added `COPY .mvn/ ./.mvn` and `COPY mvnw .` to both `Dockerfile`s (in the `builder` stage).

* **Challenge: Docker Build Fail 2 (`Child module /app/common... not found`)**
    * **Problem:** Maven's reactor failed because when building `gateway`, it couldn't find the `common` module's files.
    * **Solution:** Updated both `Dockerfile`s to copy *all* module `pom.xml` files (`common/pom.xml`, `gateway/pom.xml`, `worker/pom.xml`) and the `src` for *all* required dependencies (e.g., `gateway`'s build copies both `gateway/src` and `common/src`).

* **Challenge: Docker Build Fail 3 (`no main manifest attribute`)**
    * **Problem:** The `.jar` files were built but were not "executable".
    * **Solution:** Added a `<build><pluginManagement>...</pluginManagement>` block to the **parent `pom.xml`** to ensure the `spring-boot-maven-plugin` (with the `repackage` goal) was correctly configured and versioned for all child modules.

* **Challenge: Docker Runtime Fail 4 (`Connection refused`)**
    * **Problem:** `gateway` and `worker` services crashed on startup, unable to connect to MySQL.
    * **Diagnosis:** A "race condition". The Java apps started *faster* than the `mysql` container was ready to accept connections.
    * **Solution:** Added a `healthcheck` to the `mysql` service in `docker-compose.yaml` and changed the `gateway` / `worker` `depends_on` to use `condition: service_healthy`. This forces them to wait until MySQL is *actually* ready.