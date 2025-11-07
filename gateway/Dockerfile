# --------------------
# 1. 构建阶段 (Build Stage)
# --------------------
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# 复制父 POM
COPY pom.xml .

# --- 【修复】 复制 Maven Wrapper ---
# 这两个是运行 ./mvnw 命令所必需的！
COPY .mvn/ ./.mvn
COPY mvnw .
# ---------------------------------

# 复制所有模块的 POMs (防止 Maven 找不到子模块)
COPY gateway/pom.xml ./gateway/
COPY worker/pom.xml ./worker/

# (可选) 复制源代码 (我们可以在下一步做)
# COPY gateway/src ./gateway/src

# 运行 Maven 依赖下载 (利用 Docker 缓存)
RUN ./mvnw dependency:go-offline -pl gateway -am

# 复制源代码 (现在再复制)
COPY gateway/src ./gateway/src

# 运行 Maven 打包命令
RUN ./mvnw clean package -pl gateway -am -DskipTests

# --------------------
# 2. 运行阶段 (Runtime Stage)
# --------------------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 从构建阶段(builder)复制编译好的 JAR
# 使用通配符 * 来避免版本号问题
COPY --from=builder /app/gateway/target/gateway-*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]