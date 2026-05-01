import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'

// 导入页面组件
import Login from './pages/login/login.vue'
import Home from './pages/home/home.vue'
import PointList from './pages/point-list/point-list.vue'
import PointDetail from './pages/point-detail/point-detail.vue'
import Survey from './pages/survey/survey.vue'
import AuditList from './pages/audit/audit-list.vue'
import AuditDetail from './pages/audit/audit-detail.vue'
import My from './pages/my/my.vue'

// 创建路由
const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login },
  { path: '/home', name: 'Home', component: Home },
  { path: '/points', name: 'PointList', component: PointList },
  { path: '/point/:id', name: 'PointDetail', component: PointDetail },
  { path: '/survey/:id?', name: 'Survey', component: Survey },
  { path: '/audit', name: 'AuditList', component: AuditList },
  { path: '/audit/:id', name: 'AuditDetail', component: AuditDetail },
  { path: '/my', name: 'My', component: My }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('access_token')
  const publicPages = ['/login']
  
  if (!token && !publicPages.includes(to.path)) {
    next('/login')
  } else {
    next()
  }
})

// 创建并挂载应用
const app = createApp(App)
app.use(router)
app.mount('#app')
