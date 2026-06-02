# 移动端API配置说明

## ✅ 已修复的问题

**API路径不匹配**: 后端使用 `/api/v1/`,前端已更新为 `/api/v1/`

## 当前配置

**开发环境**: `http://localhost:8081/api/v1`
**生产环境**: 需要修改 `.env.production`

## 后端服务检查

### 1. 确认后端是否运行

```bash
# 检查后端服务状态
curl http://localhost:8081/api/v1/auth/login
```

如果返回连接拒绝,说明后端未启动。

### 2. 启动后端服务

```bash
cd admin-backend
# 使用Maven启动
mvn spring-boot:run

# 或使用JAR包
java -jar target/survey-app.jar
```

### 3. 测试后端API

```bash
# 测试登录接口
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGc...",
    "user": {
      "id": 1,
      "username": "admin",
      "name": "管理员",
      "roles": ["ADMIN"]
    }
  }
}
```

## 修改API地址

### 方式1: 修改环境变量文件

编辑 `mobile-app/.env.development`:
```
VITE_API_BASE_URL=http://你的后端地址:端口/api/v1
```

### 方式2: 直接修改api.js

编辑 `mobile-app/src/utils/api.js` 第7行:
```javascript
const BASE_URL = 'http://你的后端地址:端口/api/v1'
```

## 跨域问题

如果前后端不在同一域名,需要配置CORS:

### 后端CORS配置

确保后端 `WebMvcConfig.java` 中有:

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")  // 前端地址
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

### Vite代理配置 (开发环境)

编辑 `mobile-app/vite.config.js`:

```javascript
export default {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        // 不要rewrite,保持/api/v1路径
      }
    }
  }
}
```

然后修改 `.env.development`:
```
VITE_API_BASE_URL=/api/v1
```

## API路径映射

| 前端路径 | 后端Controller | 实际路径 |
|---------|---------------|----------|
| `/auth/login` | AuthController | `/api/v1/auth/login` |
| `/auth/sms-login` | AuthController | `/api/v1/auth/sms-login` |
| `/user/info` | SysUserController | `/api/v1/user/info` |
| `/project/list` | ProjectController | `/api/v1/project/list` |
| `/point/list` | SurveyPointController | `/api/v1/point/list` |
| `/point/my` | SurveyPointController | `/api/v1/point/my` |
| `/result/my` | SurveyResultController | `/api/v1/result/my` |
| `/message/list` | MessageCenterController | `/api/v1/message/list` |

## 常见问题

### Q1: 网络请求失败
**原因**: 后端未启动或地址错误
**解决**: 
1. 检查后端是否运行
2. 确认API地址配置正确
3. 查看浏览器控制台Network面板
4. **确认路径包含 `/v1`**

### Q2: 404错误
**原因**: API路径不匹配
**解决**:
1. 检查后端Controller的 `@RequestMapping`
2. 确认前端API路径包含 `/v1`
3. 对比上表的映射关系

## 测试清单

- [ ] 后端服务已启动 (端口8080)
- [ ] API地址配置正确 (包含 `/v1`)
- [ ] 跨域问题已解决
- [ ] 登录接口可访问: `POST /api/v1/auth/login`
- [ ] 浏览器控制台无CORS错误
- [ ] Network面板显示请求成功
- [ ] 登录成功后Token已保存
- [ ] 可以正常访问受保护的API

## 快速测试

### 1. 启动后端
```bash
cd admin-backend
mvn spring-boot:run
```

### 2. 测试后端API
```bash
# 测试登录
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 3. 启动前端
```bash
cd mobile-app
npm run dev:h5
```

### 4. 打开浏览器
访问: `http://localhost:5173`

### 5. 检查Network面板
- 登录请求应发送到: `http://localhost:8081/api/v1/auth/login`
- 状态码应为: `200`
- 响应应包含: `token` 和 `user` 信息

### 6. 尝试登录
- 输入用户名和密码
- 查看控制台日志
- 确认跳转成功
