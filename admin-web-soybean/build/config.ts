import type { ProxyOptions } from 'vite';
import { createServiceConfig } from '../src/utils/service';

export function getBuildTime() {
  return new Date().toISOString();
}

export function createViteProxy(env: Env.ImportMeta, enableProxy: boolean) {
  if (!enableProxy || env.VITE_HTTP_PROXY !== 'Y') {
    return undefined;
  }

  const { baseURL, other } = createServiceConfig(env);
  const proxy: Record<string, string | ProxyOptions> = {
    '/proxy-default': createProxyOptions(baseURL, '/proxy-default')
  };

  other.forEach(item => {
    proxy[item.proxyPattern] = createProxyOptions(item.baseURL, item.proxyPattern);
  });

  return proxy;
}

function createProxyOptions(target: string, prefix: string): ProxyOptions {
  return {
    target,
    changeOrigin: true,
    rewrite: path => path.replace(new RegExp(`^${prefix}`), '')
  };
}
