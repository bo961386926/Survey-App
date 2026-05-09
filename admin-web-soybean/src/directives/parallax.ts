import type { Directive, DirectiveBinding } from 'vue';

interface ParallaxElement {
  el: HTMLElement;
  speed: number;
  observer: IntersectionObserver;
}

const elements = new Map<HTMLElement, ParallaxElement>();

function handleScroll() {
  const scrollY = window.scrollY || document.documentElement.scrollTop;

  elements.forEach(({ el, speed }) => {
    const rect = el.getBoundingClientRect();
    if (rect.top < window.innerHeight && rect.bottom > 0) {
      const translateY = (scrollY * speed) * 0.1;
      el.style.transform = `translateY(${translateY}px)`;
    }
  });
}

let scrollHandlerAttached = false;

const setupScrollHandler = () => {
  if (!scrollHandlerAttached) {
    window.addEventListener('scroll', handleScroll, { passive: true });
    scrollHandlerAttached = true;
  }
};

export const vParallax: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<number>) {
    const speed = binding.value || 0.5;

    elements.set(el, {
      el,
      speed,
      observer: new IntersectionObserver(() => {})
    });

    el.style.willChange = 'transform';
    el.style.transition = 'transform 0.1s ease-out';

    setupScrollHandler();
  },

  unmounted(el: HTMLElement) {
    elements.delete(el);
    el.style.willChange = 'auto';
    el.style.transform = '';
  }
};

export default vParallax;
