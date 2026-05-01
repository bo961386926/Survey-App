# 青泓项目勘察系统 - 后端部署指南

## 环境要求

- Docker Desktop for Mac (已安装)
- Docker CLI 路径: `/Applications/Docker.app/Contents/Resources/bin/`

## 快速部署

### 1. 启动所有服务

```bash
export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
cd /Users/wuxb/DevSpace/Survey-App
docker compose up -d --build
```

### 2. 查看服务状态

```bash
docker compose ps
```

### 3. 查看日志

```bash
# 查看所有服务日志
docker compose logs -f

# 仅查看后端日志
docker compose logs -f admin-backend
```

### 4. 停止服务

```bash
docker compose down
```

### 5. 停止并删除数据卷

```bash
docker compose down -v
```

## 服务端口

| 服务 | 容器名称 | 端口 | 说明 |
|------|---------|------|------|
| MySQL | survey-mysql | 3306 | 数据库 |
| Redis | survey-redis | 6379 | 缓存 |
| Admin Backend | survey-admin-backend | 8080 | 后端API |

## 访问地址

- 后端API: http://localhost:8080
- API文档: http://localhost:8080/doc.html

## 数据库连接

- 主机: localhost
- 端口: 3306
- 数据库: survey_db
- 用户名: root
- 密码: root

## 前端联调配置

前端项目需要配置API基础地址为: `http://localhost:8080`

## 常见问题

### 1. Docker命令找不到

```bash
export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
```

### 2. 端口被占用

修改 `docker-compose.yml` 中的端口映射，如:
```yaml
ports:
  - "8081:8080"  # 将8080改为8081
```

### 3. 数据库初始化失败

检查 `init-sql/init.sql` 文件是否存在且格式正确。