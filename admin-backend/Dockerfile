# ========================================
# 企业级多阶段构建 Dockerfile
# 阶段1: 编译构建
# ========================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# 先复制pom.xml利用Docker缓存层
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并编译
COPY src ./src
RUN mvn clean package -DskipTests -B

# ========================================
# 阶段2: 运行环境
# ========================================
# 使用支持多平台的镜像
FROM eclipse-temurin:17-jre

# 创建非root用户（Debian系）
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# 从构建阶段复制jar包
COPY --from=builder /build/target/survey-admin-1.0.0.jar app.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/v1/health || exit 1

# 切换到非root用户
USER appuser

# JVM优化参数
ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx512m", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=/app/logs/heap_dump.hprof", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar", \
  "--spring.profiles.active=prod"]
