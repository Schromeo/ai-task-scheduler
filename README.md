# 🤖 AI-Enhanced Distributed Task Scheduler

This is a personal, hands-on project to build an industry-grade, distributed task scheduling platform from scratch. The primary goal is to master and demonstrate advanced backend architecture concepts.

## 📍 Tech Stack

* **Backend:** Spring Boot, Java 17, Maven
* **Message Broker:** Apache Kafka
* **Database:** MySQL 8.0
* **Environment:** Docker Compose
* **AI Module:** (Upcoming) Python FastAPI

## 🚀 Milestones

* **[✅ COMPLETED] M1: E2E Message Flow:**
    * `Gateway` service accepts `POST /jobs` requests.
    * `Gateway` publishes job message to a Kafka topic.
    * `Worker` service consumes the message from the topic and logs it.

* **[◻️ PENDING] M2: Robustness & Database**
    * Implement database persistence for jobs (Outbox Pattern).
    * Ensure "at-least-once" delivery.
    * Implement idempotency in the worker.
    * Add retry logic and a Dead-Letter Queue (DLQ).

* **[◻️ PENDING] M3: AI & Monitoring**
* **[◻️ PENDING] M4: Deployment**

## How to Run (M1)

1.  **Clone the Repository**
    ```bash
    git clone [YOUR_REPOSITORY_URL]
    cd ai-task-scheduler
    ```

2.  **Create Environment File**
    * Copy the template: `cp .env.example .env` (or manually copy).
    * Fill in the `MYSQL_USER` and `MYSQL_PASSWORD` values in `.env`.

3.  **Start Infrastructure**
    * Ensure your Docker Desktop is running.
    * ```bash
        docker-compose up -d
        ```
    * This will launch MySQL, Kafka, and Zookeeper.

4.  **Run the Services** (in two separate terminals)

    * **Terminal 1: Start the Gateway**
        ```bash
        # From the project root directory
        ./mvnw -f gateway/pom.xml spring-boot:run
        ```
        *(Wait for `...Started GatewayApplication...` on port 8080)*

    * **Terminal 2: Start the Worker**
        ```bash
        # From the project root directory
        ./mvnw -f worker/pom.xml spring-boot:run
        ```
        *(Wait for `...Started WorkerApplication...` on port 8081)*

5.  **Test the API!**
    * Open the `tests/M1-test-gateway.http` file in VS Code (with the REST Client extension).
    * Click the **"Send Request"** link above the `POST` line.
    * You will see a `200 OK` response in the right-hand panel.
    * Check your **Worker terminal** (Terminal 2) — you will see the `✅✅✅ [Worker] "球"接到了!...` log output!