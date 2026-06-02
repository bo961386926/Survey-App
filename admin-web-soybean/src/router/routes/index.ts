import type { CustomRoute, ElegantConstRoute, ElegantRoute } from '@elegant-router/types';
import { generatedRoutes } from '../elegant/routes';
import { layouts, views } from '../elegant/imports';
import { transformElegantRoutesToVueRoutes } from '../elegant/transform';

/**
 * custom routes
 *
 * @link https://github.com/soybeanjs/elegant-router?tab=readme-ov-file#custom-route
 */
const customRoutes: CustomRoute[] = [
  {
    name: 'map',
    path: '/point/map',
    component: 'layout.base$view.point_map',
    meta: {
      title: 'point_map',
      i18nKey: 'route.point_map',
      icon: 'material-symbols:map-outline-rounded',
      order: 2
    }
  },
  {
    name: 'project',
    path: '/project',
    component: 'layout.base',
    redirect: '/project/list',
    meta: {
      title: 'project',
      i18nKey: 'route.project',
      icon: 'material-symbols:inventory-2-outline-rounded',
      order: 2
    },
    children: [
      {
        name: 'project_list_view',
        path: '/project/list',
        component: 'view.project_list',
        meta: {
          title: 'project_list',
          i18nKey: 'route.project_list',
          hideInMenu: true
        }
      },
      {
        name: 'project_detail_view',
        path: '/project/detail/:id',
        component: 'view.project_detail',
        meta: {
          title: 'project_detail',
          i18nKey: 'route.project_detail',
          hideInMenu: true,
          activeMenu: 'project'
        }
      }
    ]
  },
  {
    name: 'system',
    path: '/system',
    component: 'layout.base',
    redirect: '/system/user',
    meta: {
      title: 'system',
      i18nKey: 'route.system',
      icon: 'material-symbols:settings-outline-rounded',
      order: 7
    },
    children: [
      {
        name: 'system_user',
        path: '/system/user',
        component: 'view.system_user',
        meta: {
          title: 'system_user',
          i18nKey: 'route.system_user',
          icon: 'material-symbols:group-outline-rounded',
          order: 1
        }
      },
      {
        name: 'system_role',
        path: '/system/role',
        component: 'view.system_role',
        meta: {
          title: 'system_role',
          i18nKey: 'route.system_role',
          icon: 'material-symbols:manage-accounts-outline-rounded',
          order: 2
        }
      },
      {
        name: 'system_log',
        path: '/system/log',
        component: 'view.system_log',
        meta: {
          title: 'system_log',
          i18nKey: 'route.system_log',
          icon: 'material-symbols:history-rounded',
          order: 3
        }
      },
      {
        name: 'system_message',
        path: '/system/message',
        component: 'view.system_message',
        meta: {
          title: 'system_message',
          i18nKey: 'route.system_message',
          icon: 'material-symbols:notifications-outline-rounded',
          order: 4
        }
      },
      {
        name: 'system_help',
        path: '/system/help',
        component: 'view.system_help',
        meta: {
          title: 'system_help',
          i18nKey: 'route.system_help',
          icon: 'material-symbols:help-outline-rounded',
          order: 5
        }
      },
      {
        name: 'system_dict',
        path: '/system/dict',
        component: 'view.system_dict',
        meta: {
          title: 'system_dict',
          i18nKey: 'route.system_dict',
          icon: 'material-symbols:dictionary',
          order: 6
        }
      }
    ]
  },
  {
    name: 'template',
    path: '/template',
    component: 'layout.base',
    redirect: '/template/list',
    meta: {
      title: 'template',
      i18nKey: 'route.template',
      icon: 'material-symbols:schema-outline-rounded',
      order: 5
    },
    children: [
      {
        name: 'template_list_view',
        path: '/template/list',
        component: 'view.template_list',
        meta: {
          title: 'template_list',
          i18nKey: 'route.template_list',
          hideInMenu: true
        }
      },
      {
        name: 'template_design_view',
        path: '/template/design/:id',
        component: 'view.template_design',
        meta: {
          title: 'template_design',
          i18nKey: 'route.template_design',
          hideInMenu: true,
          activeMenu: 'template'
        }
      }
    ]
  },
  {
    name: 'audit',
    path: '/audit',
    component: 'layout.base',
    redirect: '/audit/list',
    meta: {
      title: 'audit',
      i18nKey: 'route.audit',
      icon: 'material-symbols:verified-user-outline-rounded',
      order: 6
    },
    children: [
      {
        name: 'audit_list_view',
        path: '/audit/list',
        component: 'view.audit_list',
        meta: {
          title: 'audit_list',
          i18nKey: 'route.audit_list',
          hideInMenu: true
        }
      },
      {
        name: 'audit_detail_view',
        path: '/audit/detail/:id',
        component: 'view.audit_detail',
        meta: {
          title: 'audit_detail',
          i18nKey: 'route.audit_detail',
          hideInMenu: true,
          activeMenu: 'audit'
        }
      }
    ]
  },
  { name: 'point', path: '/point', meta: { hideInMenu: true } },
  { name: 'export', path: '/export', meta: { hideInMenu: true } },
  { name: 'exception', path: '/exception', meta: { hideInMenu: true } },
  {
    name: 'dashboard',
    path: '/dashboard',
    component: 'layout.base$view.dashboard',
    meta: {
      title: 'dashboard',
      i18nKey: 'route.dashboard',
      icon: 'material-symbols:dashboard-outline-rounded',
      order: 1
    }
  }
];

/** create routes when the auth route mode is static */
export function createStaticRoutes() {
  const constantRoutes: ElegantRoute[] = [];

  const authRoutes: ElegantRoute[] = [];

  const customRouteNames = customRoutes.map(r => r.name);
  const filteredGeneratedRoutes = generatedRoutes.filter(r => !customRouteNames.includes(r.name));

  [...customRoutes, ...filteredGeneratedRoutes].forEach(item => {
    if (item.meta?.constant) {
      constantRoutes.push(item);
    } else {
      authRoutes.push(item);
    }
  });

  return {
    constantRoutes,
    authRoutes
  };
}

/**
 * Get auth vue routes
 *
 * @param routes Elegant routes
 */
export function getAuthVueRoutes(routes: ElegantConstRoute[]) {
  return transformElegantRoutesToVueRoutes(routes, layouts, views);
}
