# Login Redirect Issue - Diagnostic Guide

## 快速诊断步骤

### 1. 检查浏览器控制台

打开浏览器 DevTools (F12)，在 Console 中查找以下信息：

```javascript
// 登录成功后，检查 token 是否正确保存
localStorage.getItem('SOY_token')
// 应该返回一个 JWT token 字符串
```

### 2. 检查网络请求

在 Network 标签中查看 `/api/auth/login` 请求：

**期望的响应格式：**
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "userId": 1,
    "username": "admin",
    "realName": "管理员",
    "role": 1,
    "isFirstLogin": false
  },
  "msg": "操作成功"
}
```

### 3. 检查路由守卫日志

在 `src/router/guard/route.ts` 中添加调试日志：

```typescript
router.beforeEach(async (to, from, next) => {
  console.log('🔍 Route Guard:', {
    to: to.name,
    from: from.name,
    hasToken: Boolean(localStg.get('token')),
    isInitConstantRoute: routeStore.isInitConstantRoute,
    isInitAuthRoute: routeStore.isInitAuthRoute
  });
  
  // ... 其余代码
});
```

### 4. 检查登录流程日志

在 `src/store/modules/auth/index.ts` 的 `login` 函数中添加：

```typescript
async function login(userName: string, password: string, captcha: string, captchaKey: string, redirect = true) {
  console.log('🔐 Login started');
  startLoading();

  const { data: loginToken, error } = await fetchLogin(userName, password, captcha, captchaKey);
  
  console.log('📥 Login response:', { data: loginToken, error });

  if (!error) {
    const pass = await loginByToken(loginToken);
    console.log('✅ Token stored:', pass);

    if (pass) {
      console.log('🚀 Redirecting...', { redirect });
      await redirectFromLogin(redirect);
      console.log('✅ Redirect completed');
      
      window.$notification?.success({
        message: $t('page.login.common.loginSuccess'),
        description: $t('page.login.common.welcomeBack', { userName: userInfo.userName })
      });
    }
  } else {
    console.error('❌ Login error:', error);
    resetStore();
  }

  endLoading();
}
```

## 常见问题及解决方案

### 问题 1: 验证码错误

**症状：** 登录失败，提示"验证码错误"或"验证码已过期"

**解决方案：**
- 检查后端 Redis 是否正常运行
- 确认前端请求的验证码 key 与提交的一致
- 检查开发环境是否正确返回验证码（用于调试）

### 问题 2: Token 未保存

**症状：** 登录成功但页面不跳转，刷新后回到登录页

**检查：**
```javascript
// 在控制台执行
localStorage.getItem('SOY_token')
```

**解决方案：**
- 检查 `loginByToken()` 函数是否正确执行
- 确认 `localStg.set('token', accessToken)` 被调用
- 检查浏览器是否禁用了 localStorage

### 问题 3: 路由守卫阻止跳转

**症状：** Token 已保存，但页面不跳转或跳转到 403

**解决方案：**
- 检查用户角色是否正确映射（后端 role: 1 → 前端 'R_SUPER'）
- 确认 `.env` 中 `VITE_STATIC_SUPER_ROLE=R_SUPER`
- 检查路由是否正确初始化

### 问题 4: 后端返回数据格式不匹配

**症状：** 登录成功但前端报错或无法解析响应

**期望的后端响应（LoginResponse.java）：**
```java
{
  "accessToken": "...",
  "refreshToken": "...",
  "userId": 1,
  "username": "admin",
  "realName": "管理员",
  "role": 1,
  "isFirstLogin": false,
  "loginWarning": null
}
```

**前端期望的类型（api.d.ts）：**
```typescript
interface LoginToken {
  accessToken?: string;
  token?: string;  // 兼容性字段
  refreshToken: string;
  userId: number;
  username: string;
  realName: string;
  role: number;
  isFirstLogin: boolean;
  loginWarning?: string;
}
```

### 问题 5: 路由未正确初始化

**症状：** 登录后跳转到空白页或 404

**解决方案：**
检查路由初始化流程：
1. `initConstantRoute()` - 初始化常量路由（登录页等）
2. `initAuthRoute()` - 初始化认证路由（需要登录的页面）
3. 确认 `VITE_AUTH_ROUTE_MODE=static` 使用静态路由模式

## 快速修复方案

如果问题依然存在，尝试以下步骤：

### 1. 清除缓存并重新登录

```javascript
// 在控制台执行
localStorage.clear()
sessionStorage.clear()
location.reload()
```

### 2. 检查环境变量

确认 `.env` 文件配置正确：
```env
VITE_SERVICE_BASE_URL=http://localhost:8080
VITE_HTTP_PROXY=Y
VITE_AUTH_ROUTE_MODE=static
VITE_ROUTE_HOME=home
VITE_SERVICE_SUCCESS_CODE=200
VITE_STATIC_SUPER_ROLE=R_SUPER
```

### 3. 强制跳转测试

临时修改 `src/store/modules/auth/index.ts`：

```typescript
async function login(userName: string, password: string, captcha: string, captchaKey: string, redirect = true) {
  startLoading();

  const { data: loginToken, error } = await fetchLogin(userName, password, captcha, captchaKey);

  if (!error) {
    const pass = await loginByToken(loginToken);

    if (pass) {
      // 临时强制跳转到首页
      window.location.href = '/home';
      
      // 原来的代码
      // await redirectFromLogin(redirect);
    }
  }
  
  endLoading();
}
```

如果强制跳转成功，说明问题出在路由守卫或路由初始化。

## 需要进一步帮助？

请提供以下信息：
1. 浏览器控制台的完整错误信息
2. Network 标签中 `/api/auth/login` 的请求和响应
3. Local Storage 中的 `SOY_token` 值（如果存在）
4. 登录前后的路由变化（Vue DevTools → Routes）
