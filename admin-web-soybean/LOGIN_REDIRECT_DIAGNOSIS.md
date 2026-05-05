# 登录跳转问题诊断报告

## 📋 问题描述
用户登录成功后，页面没有成功跳转到首页或其他页面。

## 🔍 已添加的诊断日志

为了准确定位问题，我已经在关键位置添加了详细的控制台日志：

### 1. **AuthStore 登录流程日志**
文件: `src/store/modules/auth/index.ts`

日志标签: `[AuthStore]`

**跟踪内容:**
- 🔐 登录开始
- 📥 后端响应数据
- ✅ Token 保存状态
- 👤 用户信息
- 🚀 重定向开始
- ❌ 错误信息

### 2. **路由守卫日志**
文件: `src/router/guard/route.ts`

日志标签: `[RouteGuard]`

**跟踪内容:**
- 🔍 导航信息（目标路由、来源路由、Token 状态）
- 🔄 路由初始化结果
- 🔐 权限检查结果
- ➡️ 重定向决策
- ✅ 放行决定

### 3. **路由器跳转日志**
文件: `src/hooks/common/router.ts`

日志标签: `[RouterPush]`

**跟踪内容:**
- 🎯 重定向参数
- ➡️ 跳转目标
- ✅ 跳转完成状态

## 🧪 如何诊断问题

### 步骤 1: 打开浏览器开发者工具
1. 在浏览器中按 `F12` 或 `Cmd+Option+I` (Mac)
2. 切换到 **Console** 标签

### 步骤 2: 执行登录
1. 输入用户名、密码和验证码
2. 点击"立即登录"

### 步骤 3: 观察控制台输出

#### 正常登录流程应该看到：

```
🔐 [AuthStore] Login started {userName: 'admin', redirect: true}
📥 [AuthStore] Login response: {hasData: true, hasError: false, tokenPreview: 'eyJhbGciOiJIUzI1NiIsIn...'}
✅ [AuthStore] LoginByToken result: true
💾 [AuthStore] Token stored: YES
👤 [AuthStore] UserInfo: {userId: '1', userName: 'admin', roles: ['R_SUPER'], ...}
🚀 [AuthStore] Starting redirect... {redirect: true}
🎯 [RouterPush] RedirectFromLogin: {needRedirect: true, hasRedirectQuery: false, ...}
🏠 [RouterPush] Redirecting to home (root)
🔍 [RouteGuard] Navigation: {to: 'root', from: 'login', hasToken: true}
🔐 [RouteGuard] Auth check: {isLogin: true, needLogin: true, hasAuth: true, ...}
✅ [RouteGuard] Allowing route switch
✅ [RouterPush] Redirect to home completed
✅ [AuthStore] Redirect completed
```

#### 问题场景及标识：

**场景 1: 后端返回错误**
```
📥 [AuthStore] Login response: {hasData: false, hasError: true, error: {...}}
❌ [AuthStore] Login error: ...
```
**原因:** 验证码错误、用户名密码错误、账号被禁用等

**场景 2: Token 未保存**
```
✅ [AuthStore] LoginByToken result: true
💾 [AuthStore] Token stored: NO  ← 问题在这里！
```
**原因:** localStorage 问题、代码执行异常

**场景 3: 路由守卫阻止**
```
🔍 [RouteGuard] Navigation: {to: 'root', from: 'login', hasToken: true}
🔐 [RouteGuard] Auth check: {isLogin: true, needLogin: true, hasAuth: false, ...}
➡️ [RouteGuard] No authorization, redirecting to 403  ← 问题在这里！
```
**原因:** 用户角色映射错误、权限配置问题

**场景 4: 路由初始化失败**
```
🔄 [RouteGuard] InitRoute returned location: {name: 'login', query: {...}}  ← 循环重定向
```
**原因:** 路由配置错误、路由 store 未正确初始化

**场景 5: 跳转函数异常**
```
🏠 [RouterPush] Redirecting to home (root)
(没有看到 "Redirect to home completed")  ← 跳转被中断
```
**原因:** 路由不存在、路由守卫拦截

## 🛠️ 常见解决方案

### 问题 1: 角色映射错误

**症状:** 看到 "No authorization, redirecting to 403"

**解决:**
检查后端返回的 `role` 字段和前端映射：

```typescript
// src/store/modules/auth/index.ts - loginByToken 函数
const roleCodeMap: Record<number, string> = {
  1: 'R_SUPER',      // 超级管理员
  2: 'R_ADMIN',      // 项目负责人
  3: 'R_AUDITOR',    // 审核员
  4: 'R_COLLECTOR',  // 采集员
  5: 'R_VIEWER'      // 查看者
};
```

### 问题 2: Token 字段不匹配

**症状:** Token stored: NO

**解决:**
后端返回的 token 字段名可能是 `token` 而不是 `accessToken`：

```typescript
// src/store/modules/auth/index.ts - loginByToken 函数
const accessToken = loginToken.accessToken || loginToken.token || '';
localStg.set('token', accessToken);
```

### 问题 3: 路由未初始化

**症状:** 登录后跳转到空白页或 404

**解决:**
确认环境变量正确：
```env
VITE_AUTH_ROUTE_MODE=static
VITE_ROUTE_HOME=home
```

### 问题 4: 验证码问题

**症状:** Login error: "验证码错误" 或 "验证码已过期"

**解决:**
1. 检查 Redis 是否运行
2. 确认开发环境正确返回验证码
3. 检查验证码 key 是否正确传递

## 📊 需要提供的诊断信息

如果问题依然存在，请提供以下信息：

### 1. 控制台完整日志
复制从点击登录按钮到页面停止跳转期间的所有日志（包括 emoji 标签）

### 2. Network 请求详情
在 Network 标签中找到 `/api/auth/login` 请求，提供：
- **Request Payload:**
  ```json
  {
    "username": "admin",
    "password": "******",
    "captcha": "1234",
    "captchaKey": "..."
  }
  ```
- **Response:**
  ```json
  {
    "code": 200,
    "data": { ... },
    "msg": "操作成功"
  }
  ```

### 3. Local Storage 状态
在 Application/Storage 标签中查看：
- `SOY_token`: 是否存在？值是什么？
- `SOY_refreshToken`: 是否存在？

### 4. 路由状态
如果有 Vue DevTools，提供：
- 当前路由名称
- 路由参数
- Route Store 状态（isInitConstantRoute, isInitAuthRoute）

## 🚀 快速测试方案

### 测试 1: 强制跳转
临时修改 `src/store/modules/auth/index.ts`:

```typescript
if (pass) {
  // 临时强制跳转
  window.location.href = '/home';
  return;
  
  // await redirectFromLogin(redirect);
  // ... 其余代码
}
```

如果强制跳转成功，问题出在路由系统。

### 测试 2: 直接访问
登录成功后，手动在地址栏输入 `http://localhost:端口/home`，看是否能访问。

- ✅ 能访问 → 跳转逻辑问题
- ❌ 不能访问 → 路由守卫或权限问题

### 测试 3: 清除缓存
```javascript
// 在控制台执行
localStorage.clear()
sessionStorage.clear()
location.reload()
```

然后重新登录。

## 📝 下一步

1. **运行应用并观察日志**
   ```bash
   cd admin-web-soybean
   pnpm dev
   ```

2. **记录控制台输出**
   - 复制所有带 emoji 标签的日志
   - 注意任何红色错误信息

3. **提供诊断信息**
   根据上面的"需要提供的诊断信息"章节收集数据

4. **分析结果**
   根据日志判断是哪个场景的问题，然后应用对应的解决方案

## 🔗 相关文件

- [Auth Store](src/store/modules/auth/index.ts) - 登录逻辑和状态管理
- [Route Guard](src/router/guard/route.ts) - 路由守卫和权限检查
- [Router Hook](src/hooks/common/router.ts) - 路由跳转函数
- [Login Component](src/views/_builtin/login/modules/pwd-login.vue) - 登录表单
- [Auth API](src/service/api/auth.ts) - 登录 API 调用
- [Request Handler](src/service/request/index.ts) - HTTP 请求拦截器

---

**注意:** 这些日志仅用于开发调试，问题解决后应该移除或改为条件输出。
