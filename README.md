# 🤖 AI-Enhanced Distributed Task Scheduler

This is a personal, hands-on project to build an industry-grade, distributed task scheduling platform from scratch. The primary goal is to master and demonstrate advanced backend architecture concepts.

## 📍 Tech Stack

* **Backend:** Spring Boot, Java 17, Maven
* **Message Broker:** Apache Kafka
* **Database:** MySQL 8.0
* **Environment:** Docker Compose
* **AI Module:** (Upcoming) Python FastAPI

## 🚀 M1 Milestone (In Progress): API Gateway & Kafka Producer

### How to Run (M1 - Gateway Service)

1.  **Clone the Repository**
    ```bash
    git clone [YOUR_REPOSITORY_URL]
    cd ai-task-scheduler
    ```

2.  **Create Environment File**
    * Copy the template: `cp .env.example .env` (or manually copy)
    * Fill in the `MYSQL_USER` and `MYSQL_PASSWORD` values in `.env` (to match your `docker-compose.yaml`).

3.  **Start Infrastructure**
    * Ensure Docker Desktop is running.
    * ```bash
        docker-compose up -d
        ```
    * This will launch MySQL, Kafka, and Zookeeper in the background.

4.  **Launch the `gateway` Service**
    * Open the project in VS Code.
    * Wait for the Java Language Server to initialize.
    * Open `gateway/src/main/java/com/app/gateway/GatewayApplication.java`.
    * Click the **"Run"** button that appears above the `main` method.
    * Wait for the terminal to show `...Started GatewayApplication...`.

5.  **Test the API!**
    * Open the `tests/M1-test-gateway.http` file.
    * Click the **"Send Request"** link above the `POST` line.
    * You will see a `200 OK` response on the right-hand panel, confirming the service is live and has sent the message to Kafka.