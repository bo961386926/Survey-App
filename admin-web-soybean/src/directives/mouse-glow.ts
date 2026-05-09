import type { Directive, DirectiveBinding } from 'vue';

interface GlowOptions {
  color?: string;
  size?: number;
  intensity?: number;
}

interface BorderGlowOptions {
  color?: string;
  width?: number;
}

// Use WeakMap to store cleanup functions (no property pollution)
const mouseGlowCleanups = new WeakMap<HTMLElement, () => void>();
const glowBorderCleanups = new WeakMap<HTMLElement, () => void>();

/**
 * 鼠标跟随光效指令
 * 当鼠标移动到元素上时，在鼠标位置产生柔和的光晕效果
 *
 * 用法:
 * <div v-mouse-glow>默认光效</div>
 * <div v-mouse-glow="{ color: '22,119,255', size: 300, intensity: 0.15 }">自定义参数</div>
 */
export const vMouseGlow: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<GlowOptions>) {
    const options: Required<GlowOptions> = {
      color: binding.value?.color || '22,119,255',
      size: binding.value?.size || 300,
      intensity: binding.value?.intensity || 0.1,
    };

    if (!el.style.position) {
      el.style.position = 'relative';
    }
    el.style.overflow = 'hidden';

    const glowLayer = document.createElement('div');
    glowLayer.className = 'mouse-glow-layer';
    glowLayer.style.cssText = `
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      pointer-events: none;
      z-index: 1;
      opacity: 0;
      transition: opacity 0.3s ease;
    `;
    el.appendChild(glowLayer);

    const handleMouseMove = (e: MouseEvent) => {
      const rect = el.getBoundingClientRect();
      const posX = e.clientX - rect.left;
      const posY = e.clientY - rect.top;

      glowLayer.style.opacity = '1';
      glowLayer.style.background = `
        radial-gradient(
          circle ${options.size / 2}px at ${posX}px ${posY}px,
          rgba(${options.color}, ${options.intensity}) 0%,
          rgba(${options.color}, ${options.intensity * 0.5}) 30%,
          transparent 70%
        )
      `;
    };

    const handleMouseLeave = () => {
      glowLayer.style.opacity = '0';
    };

    el.addEventListener('mousemove', handleMouseMove);
    el.addEventListener('mouseleave', handleMouseLeave);

    const cleanup = () => {
      el.removeEventListener('mousemove', handleMouseMove);
      el.removeEventListener('mouseleave', handleMouseLeave);
      glowLayer.remove();
    };

    mouseGlowCleanups.set(el, cleanup);
  },
  unmounted(el: HTMLElement) {
    const cleanup = mouseGlowCleanups.get(el);
    cleanup?.();
    mouseGlowCleanups.delete(el);
  },
};

/**
 * 鼠标跟随边框光效指令
 * 当鼠标移动到元素上时，边框会产生跟随鼠标的光效
 *
 * 用法:
 * <div v-glow-border>默认光效</div>
 * <div v-glow-border="{ color: '#1677FF', width: 2 }">自定义参数</div>
 */
export const vGlowBorder: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<BorderGlowOptions>) {
    const options: Required<BorderGlowOptions> = {
      color: binding.value?.color || 'rgba(22,119,255,0.5)',
      width: binding.value?.width || 2,
    };

    if (!el.style.position) {
      el.style.position = 'relative';
    }
    el.style.overflow = 'hidden';

    const borderGlowLayer = document.createElement('div');
    borderGlowLayer.className = 'glow-border-layer';
    borderGlowLayer.style.cssText = `
      position: absolute;
      top: -${options.width}px;
      left: -${options.width}px;
      right: -${options.width}px;
      bottom: -${options.width}px;
      pointer-events: none;
      z-index: 2;
      opacity: 0;
      transition: opacity 0.3s ease;
      border-radius: inherit;
    `;
    el.appendChild(borderGlowLayer);

    const handleMouseMove = () => {
      borderGlowLayer.style.opacity = '1';
      borderGlowLayer.style.boxShadow = `
        inset 0 0 0 ${options.width}px ${options.color},
        0 0 ${options.width * 10}px ${options.color.replace(/[\d.]+\)$/, '0.2)')}
      `;
    };

    const handleMouseLeave = () => {
      borderGlowLayer.style.opacity = '0';
      borderGlowLayer.style.boxShadow = 'none';
    };

    el.addEventListener('mousemove', handleMouseMove);
    el.addEventListener('mouseleave', handleMouseLeave);

    const cleanup = () => {
      el.removeEventListener('mousemove', handleMouseMove);
      el.removeEventListener('mouseleave', handleMouseLeave);
      borderGlowLayer.remove();
    };

    glowBorderCleanups.set(el, cleanup);
  },
  unmounted(el: HTMLElement) {
    const cleanup = glowBorderCleanups.get(el);
    cleanup?.();
    glowBorderCleanups.delete(el);
  },
};

export default { vMouseGlow, vGlowBorder };
