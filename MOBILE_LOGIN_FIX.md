# 移动端登录和跳转问题修复说明

## 问题原因

1. **TabBar图标缺失** - 新增的地图和勘查页面缺少对应的TabBar图标文件
2. **认证页面列表未更新** - 新增的 `survey-list` 页面未添加到需要登录的页面列表中
3. **登录跳转容错不足** - 登录成功后跳转失败时没有备用方案

## 已修复内容

### 1. 更新认证页面列表 ✅
**文件**: `mobile-app/src/utils/auth.js`

添加了 `pages/survey/survey-list` 到需要登录的页面列表中,确保该页面在未登录时会被拦截。

### 2. 优化登录逻辑 ✅
**文件**: `mobile-app/src/pages/login/login.vue`

改进内容:
- 兼容不同的API响应格式 (`result.token` 或 `result.accessToken`)
- 兼容不同的用户信息字段 (`result.user` 或 `result.userInfo`)
- 添加跳转失败的备用方案 (使用 `reLaunch`)
- 添加详细的错误日志输出
- 登录页面加载时自动检测登录状态

### 3. 临时移除图标配置 ✅
**文件**: `mobile-app/src/pages.json`

由于图标文件缺失,暂时移除了TabBar的图标配置,使用纯文字TabBar确保功能正常运行。

**当前TabBar配置**:
- 工作台
- 点位
- 地图
- 勘查
- 我的

## 测试步骤

### 1. 启动应用
```bash
cd mobile-app
npm run dev:h5
```

### 2. 测试登录流程

**预期行为**:
1. 访问应用 → 显示登录页面
2. 输入用户名和密码 → 点击登录
3. 登录成功 → 显示"登录成功"提示
4. 自动跳转到"工作台"页面
5. TabBar显示5个标签

**如果登录失败**:
- 检查控制台错误日志
- 确认后端API是否正常运行
- 检查网络连接

### 3. 测试页面跳转

**测试项**:
- ✅ 登录后自动跳转到工作台
- ✅ 点击TabBar切换页面
- ✅ 从工作台点击"点位"跳转
- ✅ 从工作台点击"地图"跳转
- ✅ 从工作台点击"勘查"跳转
- ✅ 从工作台点击"我的"跳转

### 4. 测试未登录拦截

**测试步骤**:
1. 清除浏览器localStorage
2. 直接访问 `http://localhost:5173/#/pages/home/home`
3. 应该自动重定向到登录页面

## 后续工作

### 1. 准备TabBar图标 (重要)

需要创建10个图标文件(5组):

**图标规格**:
- 尺寸: 81x81px
- 格式: PNG(透明背景)
- 普通状态颜色: #94A3B8 (灰色)
- 激活状态颜色: #2563EB (蓝色)

**文件列表**:
```
mobile-app/src/static/tabbar/
├── home.png              (灰色)
├── home-active.png       (蓝色)
├── point.png             (灰色)
├── point-active.png      (蓝色)
├── map.png               (灰色)
├── map-active.png        (蓝色)
├── survey.png            (灰色)
├── survey-active.png     (蓝色)
├── my.png                (灰色)
└── my-active.png         (蓝色)
```

**快速获取方式**:
1. 访问 https://www.iconfont.cn/
2. 搜索关键词: home, location, map, file, user
3. 下载PNG格式
4. 使用图片编辑工具调整颜色

### 2. 恢复图标配置

图标准备好后,修改 `pages.json`:

```json
{
  "pagePath": "pages/home/home",
  "text": "工作台",
  "iconPath": "static/tabbar/home.png",
  "selectedIconPath": "static/tabbar/home-active.png"
}
```

### 3. 高德地图Key配置

已在 `manifest.json` 中配置:
```json
{
  "distribute": {
    "sdkConfigs": {
      "maps": {
        "amap": {
          "appkey_ios": "6b6697c339d48f245660d0e79ecc0945",
          "appkey_android": "6b6697c339d48f245660d0e79ecc0945"
        }
      }
    }
  }
}
```

## 常见问题排查

### Q1: 登录后不跳转
**检查**:
1. 浏览器控制台是否有错误
2. Token是否正确保存 (F12 → Application → Local Storage)
3. 后端API是否正常返回数据

**解决**:
- 查看控制台日志 `console.log('登录结果:', result)`
- 确认API响应格式

### Q2: 页面显示空白
**检查**:
1. 浏览器控制台是否有错误
2. TabBar图标文件是否存在
3. 路由配置是否正确

**解决**:
- 当前已临时移除图标配置
- 检查 `pages.json` 路由配置

### Q3: TabBar不显示
**检查**:
1. `pages.json` 中tabBar配置是否正确
2. 页面路径是否正确

**解决**:
- 当前使用纯文字TabBar
- 确认5个页面路径都在tabBar.list中

### Q4: 地图无法加载
**检查**:
1. 高德地图Key是否配置
2. 浏览器是否允许定位权限
3. 控制台是否有地图SDK错误

**解决**:
- Key已配置在 `manifest.json`
- H5环境需要引入高德地图JS API
- 检查网络请求是否被拦截

## 技术细节

### 登录流程
```
用户输入 → 调用API → 保存Token → 跳转首页
                ↓
          失败: 显示错误提示
```

### 页面跳转方式
- **switchTab**: 用于TabBar页面切换
- **navigateTo**: 用于普通页面跳转
- **reLaunch**: 重启应用(备用方案)

### 认证拦截
```javascript
// 在页面onLoad时检查
if (!isLogin()) {
  uni.redirectTo({ url: '/pages/login/login' })
}
```

## 更新日志

**2026-05-05**:
- ✅ 修复登录跳转问题
- ✅ 更新认证页面列表
- ✅ 优化登录错误处理
- ✅ 临时移除TabBar图标配置
- ✅ 添加自动登录检测

---

**提示**: 当前版本功能完整可用,TabBar为纯文字模式。准备好图标后可恢复图标显示。
