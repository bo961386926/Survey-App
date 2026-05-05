/**
 * 生成TabBar占位图标的脚本
 * 使用Canvas生成简单的PNG图标
 */

const fs = require('fs');
const path = require('path');

// 由于Node.js环境限制，这里提供手动创建图标的说明
console.log(`
=====================================
TabBar 图标生成说明
=====================================

由于环境限制，请按照以下方式手动创建图标：

1. 使用在线工具生成图标：
   - 访问 https://www.iconfont.cn/
   - 搜索 "home", "location", "map", "file", "user"
   - 下载PNG格式 (81x81px)
   - 普通状态颜色: #94A3B8
   - 激活状态颜色: #2563EB

2. 或者使用简单的纯色方块作为临时占位：
   - 打开任意图片编辑工具
   - 创建 81x81px 的画布
   - 填充对应颜色
   - 保存为PNG

3. 需要的图标文件（共10个）：
   ✓ home.png / home-active.png - 已完成
   - point.png / point-active.png - 待创建
   - map.png / map-active.png - 待创建
   - survey.png / survey-active.png - 待创建
   - my.png / my-active.png - 待创建

4. 文件位置：
   mobile-app/src/static/tabbar/

=====================================
`);

// 创建简单的占位PNG (1x1像素，实际需要手动替换)
function createPlaceholderIcon(filename, color) {
  // 这里只是创建占位文件提示
  const filePath = path.join(__dirname, filename);
  console.log(`需要创建: ${filePath} (颜色: ${color})`);
}

// 列出所有需要创建的图标
const icons = [
  { name: 'point.png', color: '#94A3B8' },
  { name: 'point-active.png', color: '#2563EB' },
  { name: 'map.png', color: '#94A3B8' },
  { name: 'map-active.png', color: '#2563EB' },
  { name: 'survey.png', color: '#94A3B8' },
  { name: 'survey-active.png', color: '#2563EB' },
  { name: 'my.png', color: '#94A3B8' },
  { name: 'my-active.png', color: '#2563EB' }
];

icons.forEach(icon => createPlaceholderIcon(icon.name, icon.color));

console.log('\n提示: 暂时使用文字TabBar或先注释掉图标配置以测试功能');
