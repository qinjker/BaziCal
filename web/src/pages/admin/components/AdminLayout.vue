<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const collapsed = ref(false);

const menuItems = [
  { path: '/admin/dashboard', icon: '👥', label: '用户管理' },
  { path: '/admin/feedbacks', icon: '💬', label: '反馈管理' },
  { path: '/admin/messages', icon: '💡', label: '寄语管理' },
];

const isActive = (path: string) => route.path === path;

const toggleCollapse = () => {
  collapsed.value = !collapsed.value;
};

const logout = () => {
  localStorage.removeItem('admin_token');
  router.push('/admin/login');
};
</script>

<template>
  <div class="admin-layout">
    <!-- 左侧菜单 -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-header">
        <button class="collapse-btn" @click="toggleCollapse">
          {{ collapsed ? '→' : '←' }}
        </button>
        <div v-if="!collapsed" class="header-text">
          <h1 class="app-title">十神能历</h1>
          <p class="app-subtitle">管理后台</p>
        </div>
      </div>

      <nav class="menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
          :title="collapsed ? item.label : ''"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span v-if="!collapsed" class="menu-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button class="logout-btn" :title="collapsed ? '退出登录' : ''" @click="logout">
          <span class="logout-icon">🚪</span>
          <span v-if="!collapsed">退出登录</span>
        </button>
      </div>
    </aside>

    <!-- 右侧内容 -->
    <main class="content">
      <slot></slot>
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 200px;
  background: linear-gradient(180deg, #1a1a2e 0%, #0f0f1a 100%);
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  transition: width 0.3s ease;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 60px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.collapse-btn {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 8px;
  color: #fff;
  width: 28px;
  height: 28px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.header-text {
  overflow: hidden;
}

.app-title {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  margin: 0;
  white-space: nowrap;
}

.app-subtitle {
  font-size: 12px;
  color: #666;
  margin: 5px 0 0 0;
}

.menu {
  flex: 1;
  padding: 15px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  color: #888;
  text-decoration: none;
  transition: all 0.2s;
  border-left: 3px solid transparent;
  white-space: nowrap;
}

.sidebar.collapsed .menu-item {
  justify-content: center;
  padding: 14px;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.menu-item.active {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border-left-color: #f6d365;
}

.menu-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.menu-label {
  font-size: 14px;
  overflow: hidden;
}

.sidebar-footer {
  padding: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: none;
  border-radius: 10px;
  color: #888;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.sidebar.collapsed .logout-btn {
  padding: 12px 8px;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.logout-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.content {
  flex: 1;
  background: #0f0f1a;
  overflow-y: auto;
}
</style>