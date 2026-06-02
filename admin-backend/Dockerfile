# ========================================
# 企业级多阶段构建 Dockerfile
# 阶段1: 编译构建
# ========================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# 配置 Maven 镜像加速（使用阿里云镜像避免国内网络超时）
RUN mkdir -p /root/.m2 && \
    echo '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><name>Aliyun Maven Mirror</name><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# 先复制 pom.xml 利用 Docker 缓存层
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# 复制源代码并编译
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ========================================
# 阶段2: 运行环境
# ========================================
# 使用支持多平台的镜像
FROM eclipse-temurin:17-jre

# 创建非root用户（Debian系）
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# 创建日志目录并设置权限
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

# 安装 wget（用于健康检查）和 curl（用于调试）
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# 从构建阶段复制jar包
COPY --from=builder /build/target/*.jar app.jar

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
  "-Dfile.encoding=UTF-8", \
  "-Duser.timezone=Asia/Shanghai", \
  "-jar", "app.jar", \
  "--spring.profiles.active=prod"]
