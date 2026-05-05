<template>
  <div class="sample-viewer">
    <a-card title="{{ title }}" style="margin:16px">
      <iframe :src="src" style="width:100%;height:80vh;border:0"></iframe>
    </a-card>
  </div>
</template>

<script setup>
import { toRef } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const src = toRef(route, 'query').value?.src || route.params.src || ''
const title = route.params.title || 'Design Sample'
</script>

<style scoped>
.sample-viewer {
  padding: 12px;
}
</style>
