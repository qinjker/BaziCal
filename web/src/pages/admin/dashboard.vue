<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { apiService } from '@/api';
import AdminLayout from './components/AdminLayout.vue';
import dayjs from 'dayjs';

interface User {
  id: string;
  userId: string;
  name: string;
  birthday: string;
  gender: string;
  hour: number;
  minute: number;
  bazi: {
    year: { stem: string; branch: string };
    month: { stem: string; branch: string };
    day: { stem: string; branch: string };
    hour: { stem: string; branch: string };
  } | null;
  createdAt: string;
}

const router = useRouter();
const users = ref<User[]>([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);

onMounted(() => {
  const token = localStorage.getItem('admin_token');
  if (!token) {
    router.push('/admin/login');
    return;
  }
  fetchUsers();
});

const fetchUsers = async () => {
  loading.value = true;
  try {
    const response = await apiService.getAdminUsers(page.value, pageSize.value);
    if (response.code === 0) {
      users.value = response.data.users;
      total.value = response.data.total;
    }
  } catch (err) {
    console.error('Fetch users error:', err);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (newPage: number) => {
  page.value = newPage;
  fetchUsers();
};

const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm');
};

const formatBirthday = (birthday: string) => {
  if (birthday.includes('T')) {
    return dayjs(birthday).format('YYYY-MM-DD');
  }
  return birthday;
};

const getBaziString = (user: User) => {
  if (!user.bazi) return '-';
  return `${user.bazi.year.stem}${user.bazi.year.branch} ${user.bazi.month.stem}${user.bazi.month.branch} ${user.bazi.day.stem}${user.bazi.day.branch} ${user.bazi.hour.stem}${user.bazi.hour.branch}`;
};

const getHourName = (hour: number, minute: number) => {
  const h = hour.toString().padStart(2, '0');
  const m = minute.toString().padStart(2, '0');
  return `${h}:${m}`;
};
</script>

<template>
  <AdminLayout>
    <div class="admin-page">
      <div class="page-header">
        <h2 class="page-title">用户管理</h2>
        <span class="user-count">共 {{ total }} 位用户</span>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else class="user-list">
        <div class="list-header">
          <span>用户名</span>
          <span>生日</span>
          <span>时辰</span>
          <span>八字</span>
          <span>注册时间</span>
        </div>

        <div v-for="user in users" :key="user.id" class="user-item">
          <span class="user-name">{{ user.name }}</span>
          <span class="user-birthday">{{ formatBirthday(user.birthday) }}</span>
          <span class="user-hour">{{ getHourName(user.hour, user.minute) }}</span>
          <span class="user-bazi">{{ getBaziString(user) }}</span>
          <span class="user-date">{{ formatDate(user.createdAt) }}</span>
        </div>

        <div v-if="users.length === 0" class="empty">暂无用户数据</div>
      </div>

      <div v-if="total > pageSize" class="pagination">
        <button
          class="page-btn"
          :disabled="page === 1"
          @click="handlePageChange(page - 1)"
        >
          上一页
        </button>
        <span class="page-info">{{ page }} / {{ Math.ceil(total / pageSize) }}</span>
        <button
          class="page-btn"
          :disabled="page >= Math.ceil(total / pageSize)"
          @click="handlePageChange(page + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </AdminLayout>
</template>

<style scoped>
.admin-page {
  padding: 30px;
  color: #fff;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 25px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.user-count {
  font-size: 14px;
  color: #888;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #666;
}

.user-list {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  overflow: hidden;
}

.list-header {
  display: grid;
  grid-template-columns: 80px 100px 60px 1fr 120px;
  gap: 10px;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.08);
  font-size: 12px;
  color: #888;
  font-weight: 500;
}

.user-item {
  display: grid;
  grid-template-columns: 80px 100px 60px 1fr 120px;
  gap: 10px;
  padding: 18px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  font-size: 14px;
  transition: background 0.2s;
}

.user-item:hover {
  background: rgba(255, 255, 255, 0.03);
}

.user-item:last-child {
  border-bottom: none;
}

.user-name {
  color: #f6d365;
  font-weight: 500;
}

.user-birthday {
  color: #ccc;
}

.user-hour {
  color: #ccc;
}

.user-bazi {
  font-size: 12px;
  color: #888;
}

.user-date {
  font-size: 12px;
  color: #666;
}

.empty {
  text-align: center;
  padding: 60px;
  color: #666;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-top: 25px;
}

.page-btn {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.08);
  border: none;
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.15);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #888;
}
</style>