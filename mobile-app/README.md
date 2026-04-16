# uni-app 移动端勘查系统

## 技术栈
- uni-app (最新版本)
- Vue 3.3
- Vite 5.0

## 功能模块
- ✅ 用户登录
- ✅ 首页 (数据概览)
- ✅ 点位列表
- ✅ 点位详情 (含地图纠偏入口)
- ✅ 个人中心
- ⏳ 勘查填报 (表单渲染、离线存储、照片上传)

## 已完成页面
- `/pages/login/login` - 登录页
- `/pages/home/home` - 首页
- `/pages/point-list/point-list` - 点位列表
- `/pages/point-detail/point-detail` - 点位详情
- `/pages/my/my` - 个人中心

## 运行项目

### 开发环境
```bash
npm run dev:h5
```

### 构建生产
```bash
npm run build:h5
```

## 配置说明
- `pages.json` - 页面路由配置
- `manifest.json` - 应用配置
- 支持微信小程序、H5、App 三端

## 待完善功能
1. 勘查填报表单 (动态渲染)
2. 离线数据存储
3. 相机拍照功能
4. 地图手动纠偏
5. 后端接口对接
