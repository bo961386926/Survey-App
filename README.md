# 青泓项目勘查信息采集与审核系统

## 项目结构

```
.
├── admin-backend/          # Spring Boot 后端
├── admin-web/             # Vue3 PC 管理后台
└── mobile-app/            # uni-app 移动端
```

## 技术栈

- **后端**: Spring Boot + MyBatis-Plus + MySQL 8.0
- **PC 管理后台**: Vue3 + Element Plus
- **移动端**: uni-app + uView Plus

## 功能概述

1. **标准化采集**: 实现入河排口现场勘查信息的标准化、电子化录入
2. **动态适配**: 支持后台自定义表单模板
3. **高可靠填报**: 离线暂存机制
4. **高精度定位**: 地图人工纠偏
5. **闭环审核**: 提交-审核-通过/驳回流程
6. **跨端协同**: Web、小程序、App 三端数据打通
