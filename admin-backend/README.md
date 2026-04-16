# Spring Boot 后端项目

## 技术栈
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Druid 连接池
- JWT 认证

## 开发环境准备

### 启动数据库
```bash
docker compose up -d
```

### 数据库配置
- 数据库: `survey_db`
- 用户名: `root`
- 密码: `root`
- 端口: `3306`

### 启动项目
```bash
cd admin-backend
mvn clean install
mvn spring-boot:run
```

## 接口文档
项目启动后访问: `http://localhost:8080`

### 项目管理接口
- `GET /api/project/list` - 获取项目列表
- `POST /api/project/create` - 创建项目
- `PUT /api/project/update` - 更新项目
- `DELETE /api/project/delete/{id}` - 删除项目

### 模板管理接口
- `GET /api/template/list` - 获取模板列表
- `GET /api/template/detail/{id}` - 获取模板详情
- `POST /api/template/create` - 创建模板
- `PUT /api/template/update` - 更新模板
- `DELETE /api/template/delete/{id}` - 删除模板
