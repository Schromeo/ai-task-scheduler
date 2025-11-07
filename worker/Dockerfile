# --------------------
# 1. 构建阶段 (Build Stage)
# --------------------
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# 复制父 POM
COPY pom.xml .

# --- 【修复】 复制 Maven Wrapper ---
COPY .mvn/ ./.mvn
COPY mvnw .
# ---------------------------------

# 复制所有模块的 POMs
COPY gateway/pom.xml ./gateway/
COPY worker/pom.xml ./worker/

# 复制源代码 (只复制 worker 的)
COPY worker/src ./worker/src

# 运行 Maven 依赖下载
RUN ./mvnw dependency:go-offline -pl worker -am

# 复制源代码 (现在再复制)
# (上面已经复制过了，但为了保险再加一次)
COPY worker/src ./worker/src

# 运行 Maven 打包命令
RUN ./mvnw clean package -pl worker -am -DskipTests

# --------------------
# 2. 运行阶段 (Runtime Stage)
# --------------------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 从构建阶段(builder)复制编译好的 JAR
COPY --from=builder /app/worker/target/worker-*.jar ./app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]