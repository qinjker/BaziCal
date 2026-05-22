<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { apiService } from '@/api';

const router = useRouter();

// 我的反馈列表
const myFeedbacks = ref<any[]>([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(20);
const hasMore = ref(true);

// Tab 状态
const activeTab = ref('list'); // 'list' | 'submit'

// 加载反馈列表
const loadFeedbacks = async () => {
  if (loading.value || !hasMore.value) return;

  loading.value = true;
  try {
    const res = await apiService.getMyFeedbacks(page.value, pageSize.value);
    if (res.code === 0) {
      if (page.value === 1) {
        myFeedbacks.value = res.data?.feedbacks || [];
      } else {
        myFeedbacks.value.push(...(res.data?.feedbacks || []));
      }
      hasMore.value = (res.data?.feedbacks || []).length === pageSize.value;
      page.value++;
    }
  } catch (err) {
    console.error('加载反馈列表失败', err);
  } finally {
    loading.value = false;
  }
};

// 跳转到反馈详情
const goToDetail = (feedbackId: string) => {
  router.push(`/feedback/messages/${feedbackId}`);
};

// 返回日历
const goBack = () => {
  router.push('/calendar');
};

// 跳转提交反馈
const goToSubmit = () => {
  router.push('/feedback');
};

// 获取状态标签
const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待处理',
    reviewed: '已查看',
    replied: '已回复',
    closed: '已关闭',
  };
  return map[status] || status;
};

// 获取状态颜色
const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    pending: '#C84A3E',
    reviewed: '#8B7355',
    replied: '#4CAF50',
    closed: '#999',
  };
  return map[status] || '#999';
};

// 获取反馈类型标签颜色
const getTypeTagColor = (type: string) => {
  const map: Record<string, string> = {
    '功能建议': '#4CAF50',
    '问题反馈': '#C84A3E',
    '体验优化': '#2196F3',
    '其他': '#8B7355',
  };
  return map[type] || '#8B7355';
};

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) return '今天';
  if (days === 1) return '昨天';
  if (days < 7) return `${days}天前`;
  return `${date.getMonth() + 1}/${date.getDate()}`;
};

// 滚动加载更多
const onScroll = (e: Event) => {
  const target = e.target as HTMLElement;
  if (target.scrollHeight - target.scrollTop - target.clientHeight < 50) {
    loadFeedbacks();
  }
};

onMounted(() => {
  loadFeedbacks();
});
</script>

<template>
  <div class="phone-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <a href="#" class="nav-back" @click.prevent="goBack">←</a>
      <h1 class="nav-title">我的反馈</h1>
      <button class="btn-new" @click="goToSubmit">+ 新建</button>
    </div>

    <!-- 反馈列表 -->
    <div class="feedback-list" @scroll="onScroll">
      <div v-if="myFeedbacks.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">📝</div>
        <p class="empty-text">暂无反馈记录</p>
        <button class="btn-submit-empty" @click="goToSubmit">立即反馈</button>
      </div>

      <div
        v-for="item in myFeedbacks"
        :key="item.id"
        class="feedback-card"
        @click="goToDetail(item.id)"
      >
        <div class="card-header">
          <span
            class="type-tag"
            :style="{ background: getTypeTagColor(item.type) + '22', color: getTypeTagColor(item.type) }"
          >
            {{ item.type }}
          </span>
          <span
            class="status-tag"
            :style="{ background: getStatusColor(item.status) + '22', color: getStatusColor(item.status) }"
          >
            {{ getStatusLabel(item.status) }}
          </span>
        </div>

        <div class="card-content">{{ item.content }}</div>

        <div class="card-footer">
          <span class="time">{{ formatTime(item.created_at) }}</span>
          <span v-if="item.replies && item.replies.length > 0" class="reply-count">
            {{ item.replies.length }} 条回复 →
          </span>
        </div>
      </div>

      <div v-if="loading" class="loading-more">
        <span>加载中...</span>
      </div>

      <div v-if="!hasMore && myFeedbacks.length > 0" class="no-more">
        <span>没有更多了</span>
      </div>
    </div>

    <!-- 底部Tab栏 -->
    <div class="tab-bar">
      <a href="#/calendar" class="tab-item">
        <span class="tab-icon">📅</span>
        <span class="tab-label">月历</span>
      </a>
      <a href="#/daily" class="tab-item">
        <span class="tab-icon">✨</span>
        <span class="tab-label">今日</span>
      </a>
      <a href="#/index" class="tab-item">
        <span class="tab-icon">📝</span>
        <span class="tab-label">生辰</span>
      </a>
      <a href="#/feedback" class="tab-item">
        <span class="tab-icon">💬</span>
        <span class="tab-label">反馈</span>
      </a>
    </div>
  </div>
</template>

<style scoped>
.phone-container {
  width: 100%;
  max-width: 390px;
  min-height: 100vh;
  margin: 0 auto;
  background: #F7F4EF;
  padding-bottom: 40px;
}

/* 导航栏 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid rgba(232, 224, 213, 0.8);
}

.nav-back {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: #2C1810;
  font-size: 24px;
  border-radius: 12px;
  transition: background 0.2s;
}

.nav-back:active {
  background: rgba(0,0,0,0.05);
}

.nav-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  color: #2C1810;
  margin: 0;
}

.btn-new {
  padding: 8px 16px;
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
}

/* 反馈列表 */
.feedback-list {
  padding: 16px 20px;
  height: calc(100vh - 140px);
  overflow-y: auto;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 15px;
  color: #8B7355;
  margin-bottom: 20px;
}

.btn-submit-empty {
  padding: 12px 32px;
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  cursor: pointer;
}

/* 反馈卡片 */
.feedback-card {
  background: white;
  border-radius: 16px;
  padding: 18px;
  margin-bottom: 14px;
  box-shadow: 0 4px 16px rgba(44, 24, 16, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.feedback-card:active {
  transform: scale(0.98);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.type-tag {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
}

.card-content {
  font-size: 14px;
  color: #2C1810;
  line-height: 1.5;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.time {
  font-size: 12px;
  color: #B8A892;
}

.reply-count {
  font-size: 12px;
  color: #C84A3E;
}

/* 加载更多 */
.loading-more {
  text-align: center;
  padding: 16px;
  color: #8B7355;
  font-size: 14px;
}

.no-more {
  text-align: center;
  padding: 16px;
  color: #B8A892;
  font-size: 13px;
}

/* 底部Tab栏 */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 390px;
  background: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
  padding-bottom: calc(8px + env(safe-area-inset-bottom, 0px));
  border-top: 1px solid rgba(232, 224, 213, 0.8);
  z-index: 100;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px 12px;
  cursor: pointer;
  text-decoration: none;
}

.tab-icon {
  font-size: 24px;
  margin-bottom: 3px;
}

.tab-label {
  font-size: 10px;
  color: #B8A892;
  font-weight: 500;
}

.tab-item.active .tab-icon,
.tab-item.active .tab-label {
  color: #C84A3E;
}
</style>