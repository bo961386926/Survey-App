import type { App } from 'vue';
import {
  type RouterHistory,
  createMemoryHistory,
  createRouter,
  createWebHashHistory,
  createWebHistory
} from 'vue-router';
import { createBuiltinVueRoutes } from './routes/builtin';
import { createRouterGuard } from './guard';
import type { RouteRecordRaw } from 'vue-router';

const { VITE_ROUTER_HISTORY_MODE = 'history', VITE_BASE_URL } = import.meta.env;

const historyCreatorMap: Record<Env.RouterHistoryMode, (base?: string) => RouterHistory> = {
  hash: createWebHashHistory,
  history: createWebHistory,
  memory: createMemoryHistory
};

const builtinRoutes = createBuiltinVueRoutes();

// legacy routes to help migrate pages from the old admin-web
const legacyRoutes: RouteRecordRaw[] = [
  {
    path: '/legacy-login',
    name: 'LegacyLogin',
    component: () => import('@/views/migration/LoginMigration.vue')
  },
  {
    path: '/legacy',
    name: 'LegacyLayout',
    component: () => import('@/legacy/views/Layout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'LegacyDashboard',
        component: () => import('@/legacy/views/Dashboard.vue')
      },
      {
        path: 'project',
        name: 'LegacyProject',
        component: () => import('@/legacy/views/project/ProjectList.vue')
      },
      {
        path: 'template',
        name: 'LegacyTemplate',
        component: () => import('@/legacy/views/template/TemplateList.vue')
      }
    ]
  }
];

export const router = createRouter({
  history: historyCreatorMap[VITE_ROUTER_HISTORY_MODE](VITE_BASE_URL),
  routes: [...builtinRoutes, ...legacyRoutes,
    // migration sample preview routes
    {
      path: '/migration/sample/:name',
      name: 'MigrationSample',
      component: () => import('@/views/migration/SampleViewer.vue'),
      props: true
    }
  ]
});

// real migration implementations routes
router.addRoute({
  path: '/migration/real/collection-mobile',
  name: 'MigrationCollectionMobile',
  component: () => import('@/views/migration/CollectionMobile.vue')
})

router.addRoute({
  path: '/migration/real/dashboard-analytics',
  name: 'MigrationDashboardAnalytics',
  component: () => import('@/views/migration/DashboardAnalytics.vue')
})

router.addRoute({ path: '/migration/real/dashboard-v2', name: 'MigrationDashboardV2', component: () => import('@/views/migration/DashboardV2.vue') })
router.addRoute({ path: '/migration/real/point-list', name: 'MigrationPointList', component: () => import('@/views/migration/PointList.vue') })
router.addRoute({ path: '/migration/real/project-management', name: 'MigrationProjectManagement', component: () => import('@/views/migration/ProjectManagement.vue') })
router.addRoute({ path: '/migration/real/review-detail', name: 'MigrationReviewDetail', component: () => import('@/views/migration/ReviewDetail.vue') })
router.addRoute({ path: '/migration/real/user-management', name: 'MigrationUserManagement', component: () => import('@/views/migration/UserManagement.vue') })

/** Setup Vue Router */
export async function setupRouter(app: App) {
  app.use(router);
  createRouterGuard(router);
  await router.isReady();
}
