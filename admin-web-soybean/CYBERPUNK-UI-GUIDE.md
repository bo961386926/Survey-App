# 赛博朋克科技风 UI 系统 - 实施指南

## 📋 目录

1. [系统概述](#系统概述)
2. [快速开始](#快速开始)
3. [组件使用](#组件使用)
4. [样式定制](#样式定制)
5. [动效说明](#动效说明)

---

## 系统概述

### 设计理念
- **核心风格**：科技未来风，赛博朋克低饱和变体
- **主色调**：深灰 (#0D1117) + 科技蓝 (#4A9EFF) + 荧光紫 (#B266FF)
- **核心元素**：玻璃拟态、发光边框、全息投影、粒子动效

### 文件结构
```
src/
├── styles/css/
│   └── cyberpunk-theme.css    # 全局主题样式
├── components/cyber/
│   └── CyberClickParticle.vue # 点击粒子效果组件
├── directives/
│   └── parallax.ts            # 滚动视差指令
```

---

## 快速开始

### 1. 引入主题样式

在 `main.ts` 或入口文件中引入：

```ts
import './styles/css/cyberpunk-theme.css';
```

### 2. 注册组件和指令

```ts
// main.ts
import { CyberClickParticle } from './components/cyber/CyberClickParticle.vue';
import { vParallax } from './directives/parallax';

app.component('CyberClickParticle', CyberClickParticle);
app.directive('parallax', vParallax);
```

### 3. 应用背景效果

在根组件或布局组件中添加背景：

```vue
<template>
  <div class="cyber-bg">
    <CyberClickParticle />
    <!-- 页面内容 -->
  </div>
</template>
```

---

## 组件使用

### 玻璃拟态卡片

```vue
<template>
  <div class="cyber-card">
    <div class="cyber-card-header">
      <h2>卡片标题</h2>
    </div>
    <div class="cyber-card-body">
      卡片内容
    </div>
  </div>
</template>
```

### 按钮系统

```vue
<template>
  <!-- 默认按钮 -->
  <button class="cyber-btn">默认按钮</button>

  <!-- 主按钮 -->
  <button class="cyber-btn cyber-btn-primary">主要操作</button>

  <!-- 点缀按钮 -->
  <button class="cyber-btn cyber-btn-accent">特色操作</button>

  <!-- 危险按钮 -->
  <button class="cyber-btn cyber-btn-danger">删除</button>
</template>
```

### 输入框

```vue
<template>
  <input class="cyber-input" placeholder="请输入内容..." />
</template>
```

### 下拉选择

```vue
<template>
  <select class="cyber-select">
    <option>选项 1</option>
    <option>选项 2</option>
  </select>
</template>
```

### 标签/徽章

```vue
<template>
  <span class="cyber-tag">默认标签</span>
  <span class="cyber-tag cyber-tag-success">成功</span>
  <span class="cyber-tag cyber-tag-warning">警告</span>
  <span class="cyber-tag cyber-tag-danger">危险</span>
</template>
```

### 加载动画

```vue
<template>
  <!-- 线性进度条 -->
  <div class="cyber-loader"></div>

  <!-- 圆形加载器 -->
  <div class="cyber-spinner"></div>
</template>
```

### 弹窗效果

```vue
<template>
  <div class="cyber-modal-overlay" @click="close"></div>
  <div class="cyber-modal">
    <button class="cyber-modal-close" @click="close">×</button>
    <div class="cyber-card-body">
      弹窗内容
    </div>
  </div>
</template>
```

### 表格

```vue
<template>
  <table class="cyber-table">
    <thead>
      <tr>
        <th>列 1</th>
        <th>列 2</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>数据 1</td>
        <td>数据 2</td>
      </tr>
    </tbody>
  </table>
</template>
```

---

## 样式定制

### CSS 变量覆盖

在组件中覆盖主题变量：

```vue
<style scoped>
.my-component {
  --cp-primary: #FF6B6B;
  --cp-accent: #4ECDC4;
}
</style>
```

### 工具类

| 类名 | 效果 |
|------|------|
| `.text-glow` | 文字发光（蓝色） |
| `.text-glow-accent` | 文字发光（紫色） |
| `.glow-border` | 边框发光（蓝色） |
| `.glow-border-accent` | 边框发光（紫色） |
| `.gradient-text` | 渐变文字 |
| `.hover-lift` | 悬浮上浮 |
| `.cyber-border-pulse` | 边框脉冲闪烁 |

---

## 动效说明

### 1. 点击粒子效果

全局点击时触发粒子爆炸效果，粒子颜色随机从主题色中选取。

```vue
<!-- 自动生效，无需配置 -->
<CyberClickParticle />
```

### 2. 滚动视差

```vue
<template>
  <div v-parallax="0.5">视差层 1</div>
  <div v-parallax="0.3">视差层 2</div>
</template>
```

### 3. 按钮呼吸灯

按钮默认带有呼吸灯动画，2秒循环。

### 4. 卡片悬浮效果

- 悬浮时上浮 3px
- 边框发光增强
- 渐变色边框动画

### 5. 输入框聚焦

- 边框变为科技蓝
- 外发光效果
- 光标颜色变为科技蓝，1.5秒闪烁频率

### 6. 弹窗入场

- 淡入动画
- 轻微 3D 旋转效果
- 缩放弹性动画

---

## 注意事项

1. **性能优化**：粒子效果和视差滚动会消耗一定性能，在低端设备上建议关闭
2. **浏览器兼容**：`backdrop-filter` 在部分旧版浏览器中不支持，已添加 `-webkit-` 前缀
3. **可访问性**：动画效果可能影响部分用户，建议提供关闭动画的选项
4. **响应式**：所有组件均支持响应式设计，在高分辨率屏幕上效果更佳

---

## 示例页面

创建一个完整的赛博朋克风格页面：

```vue
<script setup lang="ts">
import { ref } from 'vue';
import { CyberClickParticle } from '@/components/cyber/CyberClickParticle.vue';
import { vParallax } from '@/directives/parallax';
</script>

<template>
  <div class="cyber-bg min-h-screen p-8">
    <CyberClickParticle />

    <!-- 标题 -->
    <h1 class="text-4xl font-bold gradient-text text-glow mb-8">
      赛博朋克管理系统
    </h1>

    <!-- 卡片网格 -->
    <div class="grid grid-cols-3 gap-6">
      <div class="cyber-card" v-parallax="0.3">
        <div class="cyber-card-header">
          <h2 class="text-xl">数据概览</h2>
        </div>
        <div class="cyber-card-body">
          <p class="text-2xl font-bold text-glow">1,234</p>
          <p class="text-secondary">总用户数</p>
        </div>
      </div>

      <!-- 更多卡片... -->
    </div>

    <!-- 按钮组 -->
    <div class="flex gap-4 mt-8">
      <button class="cyber-btn cyber-btn-primary">确认</button>
      <button class="cyber-btn">取消</button>
      <button class="cyber-btn cyber-btn-danger">删除</button>
    </div>

    <!-- 输入框 -->
    <div class="mt-8 max-w-md">
      <input class="cyber-input" placeholder="搜索..." />
    </div>
  </div>
</template>
```

---

*最后更新：2026-05-09*
