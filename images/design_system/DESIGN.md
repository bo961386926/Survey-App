---
name: Design System
colors:
  surface: '#f9f9ff'
  surface-dim: '#cfdaf2'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f3ff'
  surface-container: '#e7eeff'
  surface-container-high: '#dee8ff'
  surface-container-highest: '#d8e3fb'
  on-surface: '#111c2d'
  on-surface-variant: '#3f484e'
  inverse-surface: '#263143'
  inverse-on-surface: '#ecf1ff'
  outline: '#6f787e'
  outline-variant: '#bec8ce'
  surface-tint: '#006686'
  primary: '#006686'
  on-primary: '#ffffff'
  primary-container: '#7dd3fc'
  on-primary-container: '#005b78'
  inverse-primary: '#7bd1fa'
  secondary: '#50616b'
  on-secondary: '#ffffff'
  secondary-container: '#d3e5f1'
  on-secondary-container: '#566771'
  tertiary: '#576065'
  on-tertiary: '#ffffff'
  tertiary-container: '#c1cad0'
  on-tertiary-container: '#4c555a'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#c0e8ff'
  primary-fixed-dim: '#7bd1fa'
  on-primary-fixed: '#001e2b'
  on-primary-fixed-variant: '#004d66'
  secondary-fixed: '#d3e5f1'
  secondary-fixed-dim: '#b7c9d5'
  on-secondary-fixed: '#0c1e26'
  on-secondary-fixed-variant: '#384953'
  tertiary-fixed: '#dbe4ea'
  tertiary-fixed-dim: '#bfc8ce'
  on-tertiary-fixed: '#141d21'
  on-tertiary-fixed-variant: '#3f484d'
  background: '#f9f9ff'
  on-background: '#111c2d'
  surface-variant: '#d8e3fb'
typography:
  display-lg:
    fontFamily: Manrope
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Manrope
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.4'
    letterSpacing: -0.01em
  body-main:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
    letterSpacing: '0'
  label-sm:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1'
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 40px
  container-margin: 32px
  gutter: 20px
---

## Brand & Style

本设计系统的核心理念是“呼吸感”与“冰感透明”。它在承袭 Apple 极致细腻美学的基础上，进一步剔除了视觉重量，追求一种如空气般轻盈、如冰晶般清透的交互体验。

设计风格定位于 **超轻量极简主义 (Ultra-light Minimalism)** 结合微量的 **玻璃拟态 (Glassmorphism)**。通过大面积的留白、极高透明度的层级关系以及细腻的微动效，为用户营造一个纯净、专注且充满现代科技人文感的数字环境。目标受众是追求高效率、审美挑剔的专业用户，旨在唤起冷静、理智且愉悦的情感共鸣。

## Colors

色彩方案以纯白色为基调，强调“光”的介入而非“色”的堆砌。

*   **冰蓝色 (Ice Blue)**：作为主色调，相比传统的科技蓝，它大幅降低了饱和度并提升了明度，呈现出清透、冰凉的视觉感官。
*   **功能性色彩**：仅在必要处使用，确保界面整体的清冷调性不被破坏。
*   **模块分隔**：彻底摒弃实色分割线，改用极淡的渐变蓝（从 `#F0F9FF` 到 `transparent`）进行视觉引导，使不同功能区之间的过渡如同冰面消融般自然。
*   **中性色**：采用深灰蓝色而非纯黑，以保持文字在高亮背景下的呼吸感和阅读舒适度。

## Typography

字体系统选用 **Manrope**，其几何特征与人文细节的平衡完美匹配本系统的纯净感。针对中文环境，需配合系统默认的屏显黑体（如 PingFang SC），通过精确的字重分配建立层级。

排版原则强调“空气感”，通过增加段落行高（1.6x - 1.8x）和加大字间距来提升界面的通透度。标题采用略窄的字距以增强专业感与张力，而正文则保持自然的间距以确保长文阅读的流畅性。

## Layout & Spacing

采用基于 4px 的严谨栅格系统。布局哲学从“固定容器”转向“流式空间”。

*   **布局模型**：推荐使用 12 列响应式流式网格，但在模块内部采用更为松散的内边距。
*   **节奏感**：通过大比例的 `xl` 间距来划分主要功能模块，创造视觉上的停顿与节奏，避免信息过载。
*   **对齐**：坚持硬性左对齐原则，通过精确的留白而非物理线条来隐喻结构。

## Elevation & Depth

深度感的传达不依赖于阴影，而是依赖于**光影折射与层次叠加**。

*   **背板模糊 (Backdrop Blur)**：悬浮层和导航栏使用 20px - 40px 的高强度毛玻璃效果，背景色彩在透射过程中被柔化。
*   **低对比度轮廓**：容器边界不使用阴影，而是使用 1px 的超淡冰蓝边框（Opacity 10%-20%），在纯白背景上形成若隐若现的深度感。
*   **色彩层叠**：通过不同透明度的冰蓝色块堆叠，建立 Z 轴上的逻辑顺序，而非单纯的明暗变化。

## Shapes

形状语言追求圆润与理性的统一。

*   **圆角设定**：标准组件采用 `0.5rem` (8px) 圆角，大型容器或卡片采用 `1.5rem` (24px) 圆角。
*   **曲率连续性**：借鉴 Apple 的 iOS 连续曲率（Squircle），确保转角处视觉过渡极度平滑，无任何生硬的折角。
*   **交互形态**：在 Hover 或 Active 状态下，形状通过轻微的比例缩放或圆角半径的微调来反馈交互，而非改变颜色饱和度。

## Components

*   **按钮 (Buttons)**：主按钮采用冰蓝色渐变填充，文字为深色以确保清晰度；次级按钮仅保留超淡轮廓或完全透明背景，辅以图标引导。
*   **输入字段 (Input Fields)**：默认状态仅显示底部的渐变分隔线，激活时通过冰蓝色光晕包裹整个容器，保持界面的极度整洁。
*   **卡片 (Cards)**：背景保持 `#FFFFFF`，通过 1px 的 `#E0F2FE` 边框或模块间的渐变淡蓝分割。卡片不设阴影，通过间距建立独立感。
*   **选择控件 (Selection Controls)**：复选框与单选框在选中时呈现冰蓝色，未选中时仅展示极细的灰色轮廓，弱化视觉噪音。
*   **进度指示 (Progress)**：使用流光效果的冰蓝梯度，模拟液体流动的质感，增加界面的动态生命力。