import { createApp } from 'vue';
import './plugins/assets';
import { setupAppVersionNotification, setupDayjs, setupIconifyOffline, setupLoading, setupNProgress } from './plugins';
import { setupStore } from './store';
import { setupRouter } from './router';
import { setupI18n } from './locales';
import App from './App.vue';

/**
 * 带错误边界的应用启动函数
 * 防止初始化过程中抛出异常导致页面白屏/黑屏
 */
async function setupApp() {
  try {
    setupLoading();
  } catch (loadingErr) {
    console.error('Loading 动画初始化失败:', loadingErr);
    // 确保 #app 至少有一个可见的占位内容，防止黑屏
    const appEl = document.getElementById('app');
    if (appEl && !appEl.innerHTML.trim()) {
      appEl.innerHTML = '<div id="app-fallback" style="display:flex;align-items:center;justify-content:center;height:100vh;font-family:sans-serif;color:#333;background:#f5f5f5;">应用加载中...</div>';
    }
  }

  try {
    setupNProgress();
    setupIconifyOffline();
    setupDayjs();

    const app = createApp(App);

    setupStore(app);

    await setupRouter(app);

    setupI18n(app);

    setupAppVersionNotification();

    app.mount('#app');
  } catch (err) {
    console.error('应用启动失败:', err);
    // 显示错误信息，替代黑屏
    const appEl = document.getElementById('app');
    if (appEl) {
      appEl.innerHTML = `
        <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;padding:40px;font-family:sans-serif;background:#f5f5f5;">
          <h2 style="color:#e74c3c;margin-bottom:12px;">应用启动失败</h2>
          <p style="color:#666;margin-bottom:20px;max-width:600px;text-align:center;">
            遇到错误，请尝试以下操作：
          </p>
          <div style="background:#fff;border:1px solid #e0e0e0;border-radius:8px;padding:16px 24px;max-width:600px;width:100%;margin-bottom:20px;overflow:auto;">
            <pre style="margin:0;font-size:13px;color:#c00;white-space:pre-wrap;word-break:break-all;">${err instanceof Error ? err.message : String(err)}</pre>
          </div>
          <ol style="color:#666;line-height:1.8;max-width:600px;">
            <li>打开浏览器开发者工具 (F12) → Console 查看完整错误</li>
            <li>检查 <code>.env</code> 或 <code>.env.test</code> 配置文件是否完整</li>
            <li>执行 <code>pnpm install</code> 重新安装依赖</li>
            <li>删除 <code>node_modules</code> 后重新 <code>pnpm install</code></li>
          </ol>
          <button onclick="location.reload()" style="margin-top:20px;padding:10px 32px;background:#1677ff;color:#fff;border:none;border-radius:6px;font-size:14px;cursor:pointer;">重新加载</button>
        </div>`;
    }
  }
}

setupApp();
