# 🤖 AI-Enhanced Distributed Task Scheduler

This is a personal, hands-on project to build an industry-grade, distributed task scheduling platform from scratch. The primary goal is to master and demonstrate advanced backend architecture concepts, including microservices, message queues, and database integration.

## 📍 Tech Stack

* **Backend:** Spring Boot, Java 17, Maven
* **Message Broker:** Apache Kafka
* **Database:** MySQL 8.0
* **Containerization:** Docker & Docker Compose
* **AI Module:** (Upcoming) Python FastAPI

## 🚀 Milestones

* **[✅ COMPLETED] M1: E2E Message Flow (Dockerized)**
    * All services (MySQL, Kafka, Gateway, Worker) run via a single `docker-compose up` command.
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

## How to Run (M1 - E2E Dockerized)

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
    * (This may take a few minutes on the first run as it downloads dependencies).
    ```bash
    docker-compose up --build
    ```

4.  **Test the API**
    * Wait for the logs to show that both `gateway-service` and `worker-service` have `Started ...Application...`
    * Open the `tests/M1-test-gateway.http` file in VS Code (requires the "REST Client" extension).
    * Click the **"Send Request"** link above the `POST` line.

5.  **Observe the Result**
    * You will see an `HTTP/1.1 200 OK` response on the right.
    * Check your `docker-compose` log terminal. You will see two things happen:
        1.  `gateway-service | 🎉 [Gateway] 收到了一个新 Job 请求...`
        2.  `worker-service  | ✅✅✅ [Worker] “球”接到了！Job 内容是...`