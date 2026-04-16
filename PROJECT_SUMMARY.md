# 青泓项目勘查信息采集与审核系统 - 项目总结

## 已完成工作

### 后端系统 (Spring Boot)
- ✅ 项目结构搭建
- ✅ 数据库设计 (MySQL 8.0)
- ✅ 实体类创建
- ✅ Mapper 接口配置
- ✅ 项目管理接口
- ✅ 模板管理接口
- ✅ Docker 配置 (MySQL + Redis)

### PC 管理后台 (Vue3 + Element Plus)
- ✅ 登录页面
- ✅ 首页布局
- ✅ 项目管理页面
- ✅ 模板管理页面
- ✅ 路由配置
- ✅ 状态管理

### 移动端 (uni-app)
- ✅ 登录页面
- ✅ 首页
- ✅ 点位列表
- ✅ 点位详情
- ✅ 个人中心
- ✅ 开发服务器运行中

## 待开发功能

### 后端
1. 点位管理接口
2. 勘查结果提交接口
3. 审核反馈接口
4. 用户认证接口
5. OSS 图片上传接口

### 移动端
1. 动态表单渲染组件
2. 离线数据存储
3. 相机拍照功能
4. 地图纠偏功能
5. 接口对接

## 运行说明

### 数据库
```bash
docker compose up -d
```

### 后端
```bash
cd admin-backend
mvn spring-boot:run
```

### PC 管理后台
```bash
cd admin-web
npm run dev
```

### 移动端
```bash
cd mobile-app
npm run dev:h5
```

访问: http://localhost:8082/
