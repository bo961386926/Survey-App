/**
 * AMap (高德地图) Loader Utility
 * Dynamically loads AMap JavaScript API with security key
 */

const AMAP_JSAPI_VERSION = '1.4.15';
const AMAP_SECURITY_CONFIG = {
  key: '6b6697c339d48f245660d0e79ecc0945',
  securityJsCode: '' // 如果需要安全密钥，请在这里添加
};

let isLoading = false;
let isLoaded = false;
let loadPromise: Promise<void> | null = null;

/**
 * Load AMap JavaScript API
 * Returns a promise that resolves when AMap is ready
 */
export function loadAMap(): Promise<void> {
  if (isLoaded) {
    return Promise.resolve();
  }

  if (loadPromise) {
    return loadPromise;
  }

  loadPromise = new Promise((resolve, reject) => {
    if (isLoading) {
      // Wait for existing load to complete
      const checkInterval = setInterval(() => {
        if (isLoaded) {
          clearInterval(checkInterval);
          resolve();
        }
      }, 100);
      return;
    }

    isLoading = true;

    // Set security configuration
    window._AMapSecurityConfig = AMAP_SECURITY_CONFIG;

    console.log('[AMap] Starting to load AMap API...');
    console.log('[AMap] API Key:', AMAP_SECURITY_CONFIG.key);

    // Create script element
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = `https://webapi.amap.com/maps?v=${AMAP_JSAPI_VERSION}&key=${AMAP_SECURITY_CONFIG.key}`;
    script.async = true;

    script.onload = () => {
      console.log('[AMap] AMap API loaded successfully');
      console.log('[AMap] window.AMap available:', typeof window.AMap !== 'undefined');
      isLoaded = true;
      isLoading = false;
      resolve();
    };

    script.onerror = (error) => {
      console.error('[AMap] Failed to load AMap API:', error);
      isLoading = false;
      loadPromise = null;
      reject(new Error('高德地图API加载失败，请检查网络连接或API Key'));
    };

    document.head.appendChild(script);
    console.log('[AMap] Script tag added to document head');
  });

  return loadPromise;
}

/**
 * Check if AMap is loaded
 */
export function isAMapLoaded(): boolean {
  return isLoaded && typeof window.AMap !== 'undefined';
}

/**
 * Reset AMap load state (for development hot reload)
 */
export function resetAMapState() {
  isLoaded = false;
  isLoading = false;
  loadPromise = null;
}

// Type declarations for window
declare global {
  interface Window {
    _AMapSecurityConfig: {
      key: string;
      securityJsCode?: string;
    };
    AMap: any;
  }
}
