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
# Maven 构建时需要知道““整个””项目结构
COPY gateway/pom.xml ./gateway/
COPY worker/pom.xml ./worker/
COPY common/pom.xml ./common/  

# --- 【M2 修复！】 复制““所有””模块的源代码 ---
# 因为 gateway 依赖 common，所以两者都必须被复制
COPY gateway/src ./gateway/src
COPY common/src ./common/src   

# 运行 Maven 打包命令
# -pl gateway 意思是“只打包 gateway”
# -am 意思是“同时构建 gateway 依赖的模块”(也就是 common)
RUN ./mvnw clean package -pl gateway -am -DskipTests

# --------------------
# 2. 运行阶段 (Runtime Stage)
# --------------------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 从构建阶段(builder)复制编译好的 JAR
COPY --from=builder /app/gateway/target/gateway-*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]