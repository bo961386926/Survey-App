# Survey-App Docker 部署指南

**文档版本**: V2.0  
**更新时间**: 2026-05-06  
**适用环境**: 本地开发/测试/生产环境

---

## 📋 前置要求

### 必需软件
- ✅ Docker 20.10+
- ✅ Docker Compose V2 (内置于Docker Desktop)
- ✅ Git

### 验证安装
```bash
docker --version
docker compose version
```

---

## 🚀 快速部署（推荐）

### 方式一：使用部署脚本

```bash
# 1. 进入项目目录
cd /path/to/Survey-App

# 2. 编辑 .env 文件（可选，修改默认密码等配置）
vim .env

# 3. 执行部署脚本
./deploy.sh
```

脚本会自动完成：
- ✅ 检查Docker环境
- ✅ 自动创建.env配置文件
- ✅ 构建Docker镜像
- ✅ 启动所有服务
- ✅ 执行健康检查

### 方式二：快速部署（有本地Maven环境）
```bash
# 利用本地Maven缓存快速构建（~30秒）
./deploy-fast.sh
```

### 方式三：手动部署

```bash
# 1. 复制环境变量文件
cp .env.example .env

# 2. （可选）编辑.env文件修改配置
vim .env

# 3. 构建并启动服务
docker compose up -d --build

# 4. 查看服务状态
docker compose ps

# 5. 查看日志
docker compose logs -f admin-backend
```

---

## 📊 服务说明

### 部署架构

```
┌─────────────────────────────────────┐
│         Docker Network              │
│                                     │
│  ┌──────────┐    ┌──────────┐      │
│  │  MySQL   │    │  Redis   │      │
│  │ :3306    │    │ :6379    │      │
│  └────┬─────┘    └────┬─────┘      │
│       │               │             │
│       └───────┬───────┘             │
│               │                      │
│       ┌───────▼───────┐             │
│       │ Admin Backend │             │
│       │    :8080      │             │
│       │  logs:/app/logs│            │
│       └───────────────┘             │
│                                     │
└─────────────────────────────────────┘
         ▲
         │ http://localhost:8081（默认宿主端口）
```

### 服务清单

| 服务 | 容器名 | 容器端口 | 默认宿主端口 | 说明 |
|------|--------|---------|------------|------|
| MySQL | survey-mysql | 3306 | 3306 | 数据库 |
| Redis | survey-redis | 6379 | 6379 | 缓存 |
| Backend | survey-admin-backend | 8080 | 8081 | 后端API |

---

## 🔧 配置说明

### 环境变量 (.env)

```bash
# 数据库配置
DB_ROOT_PASSWORD=root           # MySQL root密码
DB_NAME=survey_db               # 数据库名
DB_USERNAME=survey_user         # 应用数据库用户
DB_PASSWORD=survey_password     # 应用数据库密码
DB_MAX_CONNECTIONS=200          # 最大连接数
DB_BUFFER_POOL=256M             # InnoDB缓冲池大小

# Redis配置
REDIS_PASSWORD=redis_password   # Redis密码
REDIS_MAX_MEMORY=128mb          # Redis最大内存

# JWT配置
JWT_SECRET=your-secret-key      # JWT密钥（生产环境必须修改）
JWT_EXPIRATION=7200000          # Token过期时间（ms）
JWT_REFRESH_EXPIRATION=604800000  # RefreshToken过期时间（ms）

# 应用配置
APP_ENV=prod                    # 运行环境
CORS_ALLOWED_ORIGINS=...        # 允许的跨域来源

# 端口配置（可选，如需修改默认端口）
# MYSQL_PORT=3306
# REDIS_PORT=6379
# BACKEND_PORT=8081
```

---

## 📱 访问地址

部署成功后，可访问以下地址：

| 服务 | URL | 说明 |
|------|-----|------|
| 后端API | http://localhost:8081 | REST API |
| 健康检查 | http://localhost:8081/api/v1/health | 服务状态 |
| API文档 | http://localhost:8081/doc.html | Knife4j接口文档 |
| MySQL | localhost:3306 | 数据库连接 |
| Redis | localhost:6379 | 缓存连接 |

> **注意**: 后端容器内部端口为 8080，映射到宿主端口 8081。所有外部访问使用 8081 端口。

---

## 🏥 健康检查

### 检查所有服务状态
```bash
docker compose ps
```

### 检查后端服务
```bash
curl http://localhost:8081/api/v1/health
```

预期响应：
```json
{
  "status": "UP",
  "timestamp": "2026-05-06T12:00:00",
  "service": "survey-admin",
  "version": "1.0.0"
}
```

### 检查MySQL
```bash
docker compose exec mysql mysqladmin ping -h localhost -u root -p${DB_ROOT_PASSWORD:-root}
```

### 检查Redis
```bash
docker compose exec redis redis-cli -a ${REDIS_PASSWORD:-redis_password} ping
```

---

## 📋 常用命令

### 查看日志
```bash
# 查看所有服务日志
docker compose logs -f

# 查看后端日志
docker compose logs -f admin-backend

# 查看最近100行
docker compose logs --tail=100 admin-backend
```

### 停止服务
```bash
# 停止所有服务（保留数据卷）
docker compose down

# 或使用脚本
./stop.sh
```

### 重启服务
```bash
# 重启所有服务
docker compose restart

# 重启单个服务
docker compose restart admin-backend
```

### 重新构建
```bash
# 清理并重新构建（从零构建）
docker compose down
docker compose up -d --build

# 或使用重新部署脚本
./redeploy.sh
```

### 进入容器
```bash
# 进入后端容器
docker compose exec admin-backend sh

# 进入MySQL容器
docker compose exec mysql bash

# 进入Redis容器
docker compose exec redis sh
```

---

## 🗄️ 数据库操作

### 连接MySQL
```bash
# 从宿主机连接
mysql -h 127.0.0.1 -P 3306 -u survey_user -psurvey_password survey_db

# 从容器内连接
docker compose exec mysql mysql -u survey_user -psurvey_password survey_db
```

### 导入数据
```bash
# SQL文件会自动在首次启动时导入
# 位置: ./init-sql/*.sql
```

### 备份数据库
```bash
docker compose exec mysql mysqldump -u survey_user -psurvey_password survey_db > backup.sql
```

### 恢复数据库
```bash
docker compose exec -T mysql mysql -u survey_user -psurvey_password survey_db < backup.sql
```

---

## 🔍 故障排查

### 问题1：服务启动失败

**检查日志**：
```bash
docker compose logs admin-backend
```

**常见原因**：
- MySQL未就绪：等待healthcheck通过
- Redis未就绪：等待healthcheck通过
- 端口被占用：修改.env中的 BACKEND_PORT/MYSQL_PORT/REDIS_PORT

### 问题2：无法连接数据库

**检查网络**：
```bash
docker compose exec admin-backend ping mysql
```

**检查环境变量**：
```bash
docker compose exec admin-backend env | grep DB_
```

### 问题3：端口冲突

**查看占用端口的进程**：
```bash
# Windows
netstat -ano | findstr :8081

# macOS/Linux
lsof -i :8081
```

**解决方案**：修改 .env 文件中的端口配置
```bash
BACKEND_PORT=9090
```

### 问题4：内存不足

**查看资源使用**：
```bash
docker stats
```

**调整资源配置**（.env）：
```bash
BACKEND_MEM_LIMIT=2048M
BACKEND_CPU_LIMIT=4.0
```

### 问题5：Apple Silicon (M1/M2/M3) 兼容性

如果在 Apple Silicon 上遇到 MySQL 或 Redis 启动问题，请在 docker-compose.yml 中取消对应服务的 platform 注释：
```yaml
mysql:
  # platform: linux/amd64   ← 取消注释
```

---

## 🧹 清理操作

### 清理未使用的镜像
```bash
docker system prune -f
```

### 完全清理（包括数据卷）
```bash
# ⚠️ 警告：会删除所有数据！
docker compose down -v
docker system prune -af
```

---

## 📈 性能优化

### JVM参数调整

编辑 `admin-backend/Dockerfile`：
```dockerfile
ENTRYPOINT ["java", \
  "-Xms512m", \      # 初始堆内存
  "-Xmx1024m", \     # 最大堆内存
  "-XX:+UseG1GC", \  # 使用G1垃圾回收器
  ...]
```

### MySQL优化

编辑 `.env`：
```bash
DB_BUFFER_POOL=512M    # 增加缓冲池
DB_MAX_CONNECTIONS=500 # 增加连接数
```

### Redis优化

编辑 `.env`：
```bash
REDIS_MAX_MEMORY=256mb # 增加最大内存
```

---

## 🔐 生产环境部署

### 安全加固

1. **修改默认密码**
```bash
# .env文件中修改所有密码
DB_ROOT_PASSWORD=<strong-password>
DB_PASSWORD=<strong-password>
REDIS_PASSWORD=<strong-password>
JWT_SECRET=<strong-secret-key>
```

2. **关闭端口暴露**
```yaml
# docker-compose.yml
# 注释掉ports，仅内部访问
# ports:
#   - "3306:3306"
#   - "6379:6379"
```

3. **使用Docker Secrets**
```yaml
secrets:
  db_password:
    file: ./secrets/db_password.txt
  redis_password:
    file: ./secrets/redis_password.txt
```

4. **配置HTTPS**
- 使用Nginx反向代理
- 配置SSL证书

---

## 📦 文件清单

### 配置文件
| 文件 | 说明 |
|------|------|
| docker-compose.yml | Docker服务编排 |
| .env.example | 环境变量模板 |
| .env | 实际环境配置（需从模板复制） |
| .dockerignore | Docker构建忽略规则 |

### Docker构建
| 文件 | 说明 |
|------|------|
| admin-backend/Dockerfile | 后端完整构建（多阶段） |
| admin-backend/Dockerfile.fast | 后端快速打包（需预编译） |

### 部署脚本
| 脚本 | 说明 |
|------|------|
| deploy.sh | 一键完整部署 |
| deploy-fast.sh | 快速部署（利用Maven缓存） |
| redeploy.sh | 重新部署 |
| stop.sh | 停止服务 |
| scripts/pre-deploy-check.sh | 部署前检查 |

---

## 📞 技术支持

如遇问题：
1. 查看日志: `docker compose logs -f`
2. 检查健康: `curl http://localhost:8081/api/v1/health`
3. 查看完整项目文档

---

**文档维护**: System Architecture Team  
**最后更新**: 2026-05-06
