# 🤖 AI-Enhanced Distributed Task Scheduler

A personal project to build an industry-grade, distributed task scheduling platform from scratch. This project demonstrates core concepts in distributed systems, including microservices, message queues, database persistence, and fault tolerance.

## 📍 Tech Stack

* **Backend:** Spring Boot (Java 17), Maven
* **Database:** MySQL 8.0
* **Message Broker:** Apache Kafka
* **Containerization:** Docker & Docker Compose
* **AI Module:** (Upcoming) Python FastAPI

## 🚀 Milestones

* **[✅ COMPLETED] M1: E2E Message Flow (Kafka)**
    * `Gateway` service accepts `POST /jobs` requests.
    * `Gateway` publishes job message to a Kafka topic.
    * `Worker` service consumes the message from the topic and logs it.

* **[✅ COMPLETED] M2: Database Persistence & Robustness**
    * Created `common` module for shared entities (`Job.java`, `JobStatus.java`).
    * Refactored `Gateway`: No longer uses Kafka. Instead, it saves new jobs to MySQL with a `PENDING` status.
    * Refactored `Worker`: No longer listens to Kafka. It now polls the database using `@Scheduled`.
    * **Robustness:** Implemented a "lease" mechanism (`leaseExpiresAt` timestamp) to handle worker failures. The `worker` now polls for both `PENDING` jobs and "stuck" `RUNNING` jobs whose lease has expired, ensuring at-least-once processing.

* **[✅ COMPLETED] M2.5: "One-Click Start" (Docker)**
    * Containerized both `gateway` and `worker` services using multi-stage `Dockerfile`s.
    * Configured `docker-compose.yaml` to manage the full stack (MySQL, Kafka, Zookeeper, Gateway, Worker).
    * Solved container networking (e.g., `mysql` instead of `localhost`).
    * Solved service start-up "race conditions" using Docker `healthcheck` and `depends_on: service_healthy`.

* **[◻️ PENDING] M3: AI & Monitoring**

## How to Run (M2 - E2E Dockerized)

This project is fully containerized. You only need **Docker Desktop** installed and running.

1.  **Clone the Repository**
    ```bash
    git clone [YOUR_REPOSITORY_URL]
    cd ai-task-scheduler
    ```

2.  **Create Environment File**
    * Copy the template: `cp .env.example .env` (or manually copy).
    * Fill in your desired `MYSQL_USER` and `MYSQL_PASSWORD` in the `.env` file.

3.  **Run the "One-Click Start"**
    * This command will build the `gateway` and `worker` images, then start all 5 containers.
    * (This may take several minutes on the first run).
    ```bash
    docker-compose up --build
    ```

4.  **Test the Full M2 Flow**
    * Wait for the logs to show that both `gateway-service` and `worker-service` have `Started ...Application...`
    * Open the `tests/M1-test-gateway.http` file in VS Code (requires the "REST Client" extension).
    * Click the **"Send Request"** link above the `POST` line.

5.  **Observe the Result in Docker Logs:**
    * The `gateway-service` log will show `🎉 [Gateway] M2: 收到新 Job 请求...` and an `Hibernate: insert into jobs ...` SQL statement.
    * Within 5 seconds, the `worker-service` log will show `🔥🔥🔥 [Worker] M2.5: “开始处理” Job ID: ...` and then `✅✅✅ [Worker] M2.5: “完成” Job ID: ...`.