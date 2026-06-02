import path from 'node:path';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import UnoCSS from '@unocss/vite';
import Icons from 'unplugin-icons/vite';
import Components from 'unplugin-vue-components/vite';
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers';
import ElegantRouter from '@elegant-router/vue/vite';
import progress from 'vite-plugin-progress';
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons';
import VueDevTools from 'vite-plugin-vue-devtools';
import type { PluginOption } from 'vite';

export function setupVitePlugins(_env: Env.ImportMeta, _buildTime: string) {
  const plugins: PluginOption[] = [
    vue(),
    vueJsx(),
    UnoCSS(),
    ElegantRouter(),
    Icons({
      compiler: 'vue3',
      autoInstall: false
    }),
    Components({
      dts: 'typings/components.d.ts',
      resolvers: [
        AntDesignVueResolver({
          importStyle: false
        })
      ]
    }),
    createSvgIconsPlugin({
      iconDirs: [path.resolve(process.cwd(), 'src/assets/svg-icon')],
      symbolId: 'local-icon-[name]',
      inject: 'body-last',
      customDomId: '__SVG_ICON_LOCAL__'
    }),
    progress()
  ];

  if (process.env.NODE_ENV !== 'production') {
    plugins.push(VueDevTools());
  }

  return plugins;
}
