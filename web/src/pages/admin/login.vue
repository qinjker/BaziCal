<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { apiService } from '@/api';

const router = useRouter();
const username = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');

const handleLogin = async () => {
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码';
    return;
  }

  loading.value = true;
  error.value = '';

  try {
    const response = await apiService.adminLogin(username.value, password.value);
    if (response.code === 0 && response.data?.token) {
      localStorage.setItem('admin_token', response.data.token);
      router.push('/admin/dashboard');
    } else {
      error.value = response.message || '登录失败';
    }
  } catch (err) {
    error.value = '网络错误，请重试';
    console.error('Login error:', err);
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="title">管理员登录</div>

      <div class="form">
        <div class="input-group">
          <label>用户名</label>
          <input v-model="username" type="text" placeholder="请输入用户名" />
        </div>

        <div class="input-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" />
        </div>

        <div v-if="error" class="error">{{ error }}</div>

        <button class="login-btn" @click="handleLogin" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #1a1a2e 0%, #0f0f1a 100%);
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  padding: 40px 30px;
  width: 100%;
  max-width: 360px;
}

.title {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: #f6d365;
  margin-bottom: 30px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  font-size: 14px;
  color: #888;
}

.input-group input {
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
  outline: none;
  transition: border-color 0.2s;
}

.input-group input:focus {
  border-color: #f6d365;
}

.input-group input::placeholder {
  color: #666;
}

.error {
  color: #ff6464;
  font-size: 14px;
  text-align: center;
}

.login-btn {
  padding: 14px;
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  border: none;
  border-radius: 12px;
  color: #1a1a2e;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.2s;
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-btn:not(:disabled):hover {
  opacity: 0.9;
}
</style>