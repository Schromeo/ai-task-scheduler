# 🤖 AI-Enhanced Distributed Task Scheduler

A hands-on project to build an industry-grade, distributed task scheduling platform from scratch, demonstrating advanced backend architecture concepts.

## 📍 Tech Stack

* **Backend:** Spring Boot, Java 17, Maven (Multi-Module)
* **Database:** MySQL 8.0
* **Message Broker:** Apache Kafka (Used in M1, replaced by DB in M2)
* **Containerization:** Docker & Docker Compose
* **AI Module:** (Upcoming) Python FastAPI

## 🚀 Milestones

* **[✅ COMPLETED] M1: E2E Message Flow (Local)**
    * `Gateway` service accepts `POST /jobs` requests.
    * `Gateway` publishes job message to a Kafka topic.
    * `Worker` service consumes the message and logs it.

* **[✅ COMPLETED] M2: Database Persistence & Dockerization**
    * Refactored `Gateway` to save jobs to MySQL (Status: `PENDING`).
    * Created `common` module for shared entities (`Job.java`).
    * Refactored `Worker` to poll the database for `PENDING` jobs using `@Scheduled`.
    * `Worker` updates job status to `RUNNING` and then `COMPLETED`.
    * **Result:** Achieved a "One-Click-Start" environment using `docker-compose up --build` for all services.

* **[◻️ PENDING] M2.5: Robustness**
    * Implement idempotency in the worker.
    * Add retry logic and a Dead-Letter Queue (DLQ) mechanism.
    * Handle "stuck" jobs (worker crash detection).

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
    * This command will build the `gateway` and `worker` images, then start all 5 containers (zookeeper, kafka, mysql, gateway, worker).
    * (This may take several minutes on the first run).
    ```bash
    docker-compose up --build
    ```

4.  **Test the Full M2 Flow**
    * Wait for the logs to show that both `gateway-service` and `worker-service` have `Started ...Application...`
    * Open the `tests/M1-test-gateway.http` file in VS Code (requires the "REST Client" extension).
    * Click the **"Send Request"** link above the `POST` line.

5.  **Observe the Result in Docker Logs:**
    1.  `gateway-service | 🎉 [Gateway] M2: 收到新 Job 请求...`
    2.  `gateway-service | Hibernate: insert into jobs ...`
    3.  *(Wait ~5 seconds for the next poll)*
    4.  `worker-service  | Hibernate: SELECT * FROM jobs WHERE status = 'PENDING'...`
    5.  `worker-service  | 🔥🔥🔥 [Worker] M2: “开始处理” Job ID: 1`
    6.  `worker-service  | ✅✅✅ [Worker] M2: “完成” Job ID: 1`