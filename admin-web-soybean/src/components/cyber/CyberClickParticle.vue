<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

interface Particle {
  id: number;
  x: number;
  y: number;
  angle: number;
  speed: number;
  size: number;
  color: string;
  opacity: number;
}

const particles = ref<Particle[]>([]);
let particleId = 0;

const colors = ['#4A9EFF', '#B266FF', '#39FF14', '#FFD700', '#FF3860'];

const createParticles = (x: number, y: number) => {
  const count = 8 + Math.floor(Math.random() * 5);
  const newParticles: Particle[] = [];

  for (let i = 0; i < count; i += 1) {
    newParticles.push({
      id: particleId,
      x,
      y,
      angle: (Math.PI * 2 * i) / count,
      speed: 30 + Math.random() * 50,
      size: 2 + Math.random() * 4,
      color: colors[Math.floor(Math.random() * colors.length)],
      opacity: 1
    });
    particleId += 1;
  }

  particles.value = [...particles.value, ...newParticles];

  // 清理已完成的粒子
  setTimeout(() => {
    particles.value = particles.value.filter(p => !newParticles.includes(p));
  }, 600);
};

const handleClick = (e: MouseEvent) => {
  createParticles(e.clientX, e.clientY);
};

onMounted(() => {
  document.addEventListener('click', handleClick);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClick);
});
</script>

<template>
  <Teleport to="body">
    <div class="cyber-particle-container">
      <div
        v-for="particle in particles"
        :key="particle.id"
        class="cyber-particle-item"
        :style="{
          left: `${particle.x}px`,
          top: `${particle.y}px`,
          width: `${particle.size}px`,
          height: `${particle.size}px`,
          backgroundColor: particle.color,
          '--angle': `${particle.angle}rad`,
          '--speed': `${particle.speed}px`
        }"
      />
    </div>
  </Teleport>
</template>

<style scoped>
.cyber-particle-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9999;
}

.cyber-particle-item {
  position: absolute;
  border-radius: 50%;
  animation: particle-fly 0.6s ease-out forwards;
}

@keyframes particle-fly {
  0% {
    transform: translate(0, 0) scale(1);
    opacity: 1;
  }
  100% {
    transform: translate(calc(cos(var(--angle)) * var(--speed)), calc(sin(var(--angle)) * var(--speed))) scale(0);
    opacity: 0;
  }
}

@supports (transform: translate(calc(cos(1rad)), calc(sin(1rad)))) {
  @keyframes particle-fly {
    0% {
      transform: translate(0, 0) scale(1);
      opacity: 1;
    }
    100% {
      transform: translate(calc(cos(var(--angle)) * var(--speed)), calc(sin(var(--angle)) * var(--speed))) scale(0);
      opacity: 0;
    }
  }
}

/* 降级方案：使用固定方向 */
@keyframes particle-fly-fallback {
  0% {
    transform: translate(0, 0) scale(1);
    opacity: 1;
  }
  100% {
    transform: translate(var(--dx), var(--dy)) scale(0);
    opacity: 0;
  }
}
</style>
