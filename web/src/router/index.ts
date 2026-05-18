/**
 * 路由配置
 */

import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'splash',
    component: () => import('@/pages/splash/index.vue'),
  },
  {
    path: '/index',
    name: 'index',
    component: () => import('@/pages/index/index.vue'),
  },
  {
    path: '/calendar',
    name: 'calendar',
    component: () => import('@/pages/calendar/index.vue'),
  },
  {
    path: '/daily',
    name: 'daily',
    component: () => import('@/pages/daily/index.vue'),
  },
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/pages/admin/login.vue'),
  },
  {
    path: '/admin/dashboard',
    name: 'admin-dashboard',
    component: () => import('@/pages/admin/dashboard.vue'),
  },
  {
    path: '/feedback',
    name: 'feedback',
    component: () => import('@/pages/feedback/index.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;