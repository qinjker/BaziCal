<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { apiService } from '@/api';

const route = useRoute();
const router = useRouter();

// ============ 状态 ============
const feedbackId = route.params.id as string;
const loading = ref(true);
const submitting = ref(false);
const replyContent = ref('');
const listRef = ref<HTMLElement | null>(null);

// 反馈数据
const feedback = ref<any>({
  id: '',
  type: '',
  content: '',
  status: '',
  created_at: '',
});
const replies = ref<any[]>([]);

// ============ 工具函数 ============

// 获取类型标签颜色
const getTypeTagColor = (type: string) => {
  const map: Record<string, { bg: string; color: string }> = {
    '功能建议': { bg: '#E8F5E9', color: '#4CAF50' },
    '问题反馈': { bg: '#FFEBEE', color: '#C84A3E' },
    '体验优化': { bg: '#E3F2FD', color: '#2196F3' },
    '其他': { bg: '#F5F5F5', color: '#8B7355' },
  };
  return map[type] || { bg: '#F5F5F5', color: '#8B7355' };
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

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}`;
};

// 获取回复头像
const getReplyAvatar = (authorType: string) => {
  return authorType === 'admin' ? '👨‍💼' : '👤';
};

// 获取回复者名称颜色
const getAuthorNameColor = (authorType: string) => {
  return authorType === 'admin' ? '#C84A3E' : '#4CAF50';
};

// ============ 方法 ============

// 加载反馈详情和回复
const loadFeedbackDetail = async () => {
  loading.value = true;
  try {
    // 调用新 API 获取完整反馈信息（含回复）
    const res = await apiService.getFeedbackDetail(feedbackId);
    if (res.code === 0 && res.data) {
      feedback.value = {
        id: res.data.id,
        type: res.data.type,
        content: res.data.content,
        status: res.data.status,
        created_at: res.data.created_at,
      };
      replies.value = res.data.replies || [];
    }
  } catch (err) {
    console.error('加载反馈详情失败', err);
  } finally {
    loading.value = false;
    // 滚动到底部
    nextTick(() => {
      scrollToBottom();
    });
  }
};

// 发送回复
const sendReply = async () => {
  if (!replyContent.value.trim() || submitting.value) return;

  submitting.value = true;
  try {
    await apiService.addFeedbackReply(feedbackId, replyContent.value.trim());
    replyContent.value = '';
    // 重新加载回复
    await loadFeedbackDetail();
  } catch (err) {
    console.error('发送回复失败', err);
    alert('发送失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 滚动到底部
const scrollToBottom = () => {
  if (listRef.value) {
    listRef.value.scrollTop = listRef.value.scrollHeight;
  }
};

// 返回上一页
const goBack = () => {
  router.push('/feedback');
};

// ============ 生命周期 ============
onMounted(() => {
  loadFeedbackDetail();
});
</script>

<template>
  <div class="phone-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <a href="#" class="nav-back" @click.prevent="goBack">←</a>
      <h1 class="nav-title">反馈详情</h1>
      <div class="nav-spacer"></div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <span>加载中...</span>
    </div>

    <!-- 反馈内容和回复列表 -->
    <div v-else ref="listRef" class="messages-list">
      <!-- 反馈卡片 -->
      <div class="feedback-card">
        <div class="card-header">
          <span
            class="type-badge"
            :style="{
              background: getTypeTagColor(feedback.type).bg,
              color: getTypeTagColor(feedback.type).color
            }"
          >
            {{ feedback.type }}
          </span>
          <span class="time">{{ formatTime(feedback.created_at) }}</span>
        </div>
        <div class="card-content">{{ feedback.content }}</div>
      </div>

      <!-- 回复列表 -->
      <div
        v-for="reply in replies"
        :key="reply.id"
        class="reply-item"
        :class="reply.author_type"
      >
        <div class="reply-avatar">{{ getReplyAvatar(reply.author_type) }}</div>
        <div class="reply-bubble">
          <div
            class="reply-name"
            :style="{ color: getAuthorNameColor(reply.author_type) }"
          >
            {{ reply.author_name }}
          </div>
          <div class="reply-content">{{ reply.content }}</div>
          <div class="reply-time">{{ formatTime(reply.created_at) }}</div>
        </div>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="input-area">
      <textarea
        v-model="replyContent"
        class="reply-input"
        placeholder="输入回复内容..."
        @keydown.enter.exact.prevent="sendReply"
      ></textarea>
      <button
        class="send-btn"
        :disabled="!replyContent.trim() || submitting"
        @click="sendReply"
      >
        {{ submitting ? '...' : '发送' }}
      </button>
    </div>

    <!-- 底部 Tab 栏 -->
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
      <a href="#/feedback" class="tab-item active">
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
  display: flex;
  flex-direction: column;
  padding-bottom: 70px;
}

/* 导航栏 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 8px;
  background: white;
  flex-shrink: 0;
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

.nav-spacer {
  width: 40px;
}

/* 加载状态 */
.loading-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8B7355;
}

/* 消息列表 */
.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

/* 反馈卡片 */
.feedback-card {
  background: white;
  border-radius: 16px;
  padding: 18px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(44, 24, 16, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.type-badge {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
}

.time {
  font-size: 12px;
  color: #B8A892;
}

.card-content {
  font-size: 14px;
  color: #2C1810;
  line-height: 1.6;
}

/* 回复项 */
.reply-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.reply-item.admin {
  flex-direction: row-reverse;
}

.reply-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  background: #4CAF50;
}

.reply-item.admin .reply-avatar {
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
}

.reply-bubble {
  max-width: 70%;
  background: white;
  border-radius: 16px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(44, 24, 16, 0.06);
}

.reply-item.admin .reply-bubble {
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
}

.reply-item.admin .reply-name,
.reply-item.admin .reply-content,
.reply-item.admin .reply-time {
  color: white;
}

.reply-name {
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 6px;
}

.reply-content {
  font-size: 14px;
  color: #2C1810;
  line-height: 1.5;
}

.reply-time {
  font-size: 11px;
  color: #B8A892;
  margin-top: 6px;
}

/* 输入区域 */
.input-area {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid rgba(232, 224, 213, 0.8);
  flex-shrink: 0;
}

.reply-input {
  flex: 1;
  height: 44px;
  background: #FAF6F0;
  border: 1.5px solid #E8E0D5;
  border-radius: 12px;
  padding: 0 14px;
  font-size: 14px;
  font-family: inherit;
  color: #2C1810;
  resize: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.reply-input:focus {
  outline: none;
  border-color: #C84A3E;
  box-shadow: 0 0 0 4px rgba(200, 74, 62, 0.1);
}

.reply-input::placeholder {
  color: #B8A892;
}

.send-btn {
  padding: 0 20px;
  height: 44px;
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn:active {
  transform: scale(0.98);
}

.send-btn:disabled {
  background: #E8E0D5;
  cursor: not-allowed;
}

/* 底部 Tab 栏 */
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