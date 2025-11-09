# --------------------
# 1. 构建阶段 (Build Stage)
# --------------------
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# 复制父 POM
COPY pom.xml .

# 复制 Maven Wrapper
COPY .mvn/ ./.mvn
COPY mvnw .

# --- 【M2 修复！】 复制““所有””模块的 POMs ---
COPY gateway/pom.xml ./gateway/
COPY worker/pom.xml ./worker/
COPY common/pom.xml ./common/  

# --- 【M2 修复！】 复制““所有””模块的源代码 ---
# 我们需要 worker 和 common 的代码
# (虽然 worker 暂未依赖 common，但很快就会了)
COPY worker/src ./worker/src
COPY common/src ./common/src   
# 运行 Maven 打包命令 (只打包 worker)
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