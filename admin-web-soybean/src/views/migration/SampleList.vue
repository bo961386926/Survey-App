<template>
  <div style="padding:16px">
    <a-list
      bordered
      :data-source="samples"
      :renderItem="renderItem"
    />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { samples } from './index'

const router = useRouter()
function open(item) {
  router.push({ name: 'MigrationSample', params: { name: item.name, title: item.title }, query: { src: item.file } })
}

function renderItem(item) {
  return (
    <a-list-item>
      <a onClick={(e) => { e.preventDefault(); open(item) }}>{item.title}</a>
    </a-list-item>
  )
}
</script>

