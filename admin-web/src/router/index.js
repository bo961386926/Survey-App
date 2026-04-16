import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/layout',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: '/project',
        name: 'Project',
        component: () => import('@/views/project/ProjectList.vue')
      },
      {
        path: '/template',
        name: 'Template',
        component: () => import('@/views/template/TemplateList.vue')
      },
      {
        path: '/point',
        name: 'Point',
        component: () => import('@/views/point/PointList.vue')
      },
      {
        path: '/audit',
        name: 'Audit',
        component: () => import('@/views/audit/AuditList.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router
