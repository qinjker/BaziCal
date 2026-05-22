/**
 * 路由配置
 */

import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

// 路由守卫 - 检查管理员登录状态
const checkAdminAuth = (to: any, from: any, next: any) => {
  const token = localStorage.getItem('admin_token');
  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    if (!token) {
      next('/admin/login');
    } else {
      next();
    }
  } else {
    next();
  }
};

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
    path: '/admin',
    redirect: '/admin/dashboard',
  },
  {
    path: '/admin/dashboard',
    name: 'admin-dashboard',
    component: () => import('@/pages/admin/dashboard.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/admin/feedbacks',
    name: 'admin-feedbacks',
    component: () => import('@/pages/admin/feedback-manager.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/admin/messages',
    name: 'admin-messages',
    component: () => import('@/pages/admin/message-manager.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/feedback',
    name: 'feedback',
    component: () => import('@/pages/feedback/feedback-center.vue'),
  },
  {
    path: '/feedback/messages/:id',
    name: 'feedback-messages',
    component: () => import('@/pages/feedback/feedback-messages.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 全局导航守卫
router.beforeEach((to, from, next) => {
  // 检查管理员路由
  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    const token = localStorage.getItem('admin_token');
    if (!token) {
      next('/admin/login');
      return;
    }
  }
  next();
});

export default router;