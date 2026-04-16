import { defineConfig } from 'vite'
import uniPlugin from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uniPlugin.default || uniPlugin],
  server: {
    port: 8081
  }
})
