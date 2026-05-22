<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { apiService } from '@/api';
import { getDeviceId } from '@/utils/crypto';

const router = useRouter();

// ============ 状态 ============
const activeTab = ref<'list' | 'submit'>('list');
const loading = ref(false);
const submitting = ref(false);
const showSuccess = ref(false);

// 反馈列表数据
const feedbacks = ref<any[]>([]);
const page = ref(1);
const pageSize = ref(20);
const hasMore = ref(true);
const total = ref(0);

// 提交表单
const selectedType = ref('功能建议');
const feedbackContent = ref('');
const charCount = ref(0);
const contact = ref('');

// ============ 计算属性 ============
const canSubmit = computed(() => {
  return feedbackContent.value.trim().length >= 10;
});

const feedbackTypes = ['功能建议', '问题反馈', '体验优化', '其他'];

// ============ 方法 ============

// 切换 Tab
const switchTab = (tab: 'list' | 'submit') => {
  activeTab.value = tab;
};

// 选择反馈类型
const selectType = (type: string) => {
  selectedType.value = type;
};

// 更新字符计数
const updateCharCount = () => {
  charCount.value = feedbackContent.value.length;
};

// 加载反馈列表
const loadFeedbacks = async (reset = false) => {
  if (loading.value || (!reset && !hasMore.value)) return;

  if (reset) {
    page.value = 1;
    feedbacks.value = [];
    hasMore.value = true;
  }

  loading.value = true;
  try {
    const deviceId = getDeviceId();
    const res = await apiService.getMyFeedbacks(page.value, pageSize.value, deviceId);
    if (res.code === 0) {
      const list = res.data?.feedbacks || [];
      if (page.value === 1) {
        feedbacks.value = list;
      } else {
        feedbacks.value.push(...list);
      }
      total.value = res.data?.total || 0;
      hasMore.value = list.length === pageSize.value;
      page.value++;
    }
  } catch (err) {
    console.error('加载反馈列表失败', err);
  } finally {
    loading.value = false;
  }
};

// 提交反馈
const submitFeedbackHandler = async () => {
  if (!canSubmit.value || submitting.value) return;

  submitting.value = true;
  try {
    await apiService.submitFeedback({
      type: selectedType.value,
      content: feedbackContent.value.trim(),
      contact: contact.value.trim() || undefined,
    });

    showSuccess.value = true;
    setTimeout(() => {
      showSuccess.value = false;
      // 重置表单
      selectedType.value = '功能建议';
      feedbackContent.value = '';
      charCount.value = 0;
      contact.value = '';
      // 切换到列表
      switchTab('list');
      // 刷新列表
      loadFeedbacks(true);
    }, 1500);
  } catch (err) {
    console.error('提交反馈失败', err);
    alert('提交失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 跳转到反馈详情
const goToDetail = (feedbackId: string) => {
  router.push(`/feedback/messages/${feedbackId}`);
};

// 返回上一页
const goBack = () => {
  router.push('/calendar');
};

// 滚动加载更多
const onScroll = (e: Event) => {
  const target = e.target as HTMLElement;
  if (target.scrollHeight - target.scrollTop - target.clientHeight < 50) {
    loadFeedbacks();
  }
};

// ============ 工具函数 ============

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
  const map: Record<string, { bg: string; color: string }> = {
    pending: { bg: '#FFF3E0', color: '#E65100' },
    reviewed: { bg: '#E3F2FD', color: '#2196F3' },
    replied: { bg: '#E8F5E9', color: '#4CAF50' },
    closed: { bg: '#F5F5F5', color: '#999' },
  };
  return map[status] || { bg: '#F5F5F5', color: '#999' };
};

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

// ============ 生命周期 ============
onMounted(() => {
  loadFeedbacks(true);
});
</script>

<template>
  <div class="phone-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <a href="#" class="nav-back" @click.prevent="goBack">←</a>
      <h1 class="nav-title">反馈中心</h1>
      <div class="nav-spacer"></div>
    </div>

    <!-- 顶部 Tab 切换 -->
    <div class="top-tabs">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'list' }"
        @click="switchTab('list')"
      >
        📋 我的反馈
      </button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'submit' }"
        @click="switchTab('submit')"
      >
        ✏️ 提交反馈
      </button>
    </div>

    <!-- ============ 反馈列表 ============ -->
    <div v-show="activeTab === 'list'" class="feedback-list" @scroll="onScroll">
      <!-- 空状态 -->
      <div v-if="feedbacks.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">💬</div>
        <p class="empty-text">还没有提交过反馈</p>
        <button class="btn-empty-submit" @click="switchTab('submit')">立即反馈</button>
      </div>

      <!-- 反馈卡片列表 -->
      <div
        v-for="item in feedbacks"
        :key="item.id"
        class="feedback-item"
        @click="goToDetail(item.id)"
      >
        <!-- 卡片头部：类型 + 时间 -->
        <div class="item-header">
          <span
            class="type-badge"
            :style="{
              background: getTypeTagColor(item.type).bg,
              color: getTypeTagColor(item.type).color
            }"
          >
            {{ item.type }}
          </span>
          <span class="item-time">{{ formatTime(item.created_at) }}</span>
        </div>

        <!-- 反馈内容预览 -->
        <div class="item-content">{{ item.content }}</div>

        <!-- 最新回复预览 -->
        <div v-if="item.latestReply" class="reply-preview">
          <div
            class="reply-avatar"
            :class="item.latestReply.author_type"
          >
            {{ item.latestReply.author_type === 'admin' ? '👨‍💼' : '👤' }}
          </div>
          <div class="reply-info">
            <div class="reply-name">{{ item.latestReply.author_name }}</div>
            <div class="reply-text">{{ item.latestReply.content }}</div>
          </div>
          <!-- 未读标记 -->
          <div v-if="item.status === 'replied' && !item.read" class="unread-dot"></div>
        </div>

        <!-- 卡片底部：回复数 + 状态 -->
        <div class="item-footer">
          <span class="reply-count">💬 {{ item.replyCount || 0 }}条回复</span>
          <span
            class="status-badge"
            :style="{
              background: getStatusColor(item.status).bg,
              color: getStatusColor(item.status).color
            }"
          >
            {{ getStatusLabel(item.status) }}
          </span>
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="loading" class="loading-more">
        <span>加载中...</span>
      </div>
      <div v-if="!hasMore && feedbacks.length > 0" class="no-more">
        <span>没有更多了</span>
      </div>
    </div>

    <!-- ============ 提交反馈表单 ============ -->
    <div v-show="activeTab === 'submit'" class="submit-form">
      <div class="form-card">
        <!-- 类型选择 - 单行4个 -->
        <div class="type-row">
          <div
            v-for="type in feedbackTypes"
            :key="type"
            class="type-btn"
            :class="{ selected: selectedType === type }"
            @click="selectType(type)"
          >
            <span class="type-icon">{{ type === '功能建议' ? '💡' : type === '问题反馈' ? '🐛' : type === '体验优化' ? '✨' : '📌' }}</span>
            <span class="type-label">{{ type }}</span>
          </div>
        </div>

        <!-- 内容输入 -->
        <div class="content-row">
          <textarea
            v-model="feedbackContent"
            class="content-textarea"
            placeholder="请描述您的建议或遇到的问题..."
            @input="updateCharCount"
          ></textarea>
          <div class="char-counter">{{ charCount }}/500</div>
        </div>

        <!-- 联系方式 - 单行 -->
        <div class="contact-row">
          <span class="contact-label">联系方式</span>
          <input
            v-model="contact"
            type="text"
            class="contact-input"
            placeholder="选填"
          />
        </div>
      </div>

      <!-- 提交按钮 -->
      <button
        class="submit-btn"
        :disabled="!canSubmit || submitting"
        @click="submitFeedbackHandler"
      >
        {{ submitting ? '提交中...' : '提交反馈' }}
      </button>
    </div>

    <!-- 成功提示 -->
    <div class="success-toast" :class="{ show: showSuccess }">
      <span class="success-icon">✓</span>
      <span>提交成功！</span>
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
  padding-bottom: 70px;
}

/* 导航栏 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 8px;
  background: white;
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

/* 顶部 Tab 切换 */
.top-tabs {
  display: flex;
  background: white;
  padding: 0 20px;
  border-bottom: 1px solid rgba(232, 224, 213, 0.8);
}

.tab-btn {
  flex: 1;
  padding: 14px 0;
  background: none;
  border: none;
  font-size: 14px;
  color: #8B7355;
  cursor: pointer;
  position: relative;
  transition: color 0.2s;
}

.tab-btn.active {
  color: #2C1810;
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  border-radius: 2px;
}

/* 反馈列表 */
.feedback-list {
  padding: 16px;
  height: calc(100vh - 180px);
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
  font-size: 14px;
  color: #8B7355;
  margin-bottom: 20px;
}

.btn-empty-submit {
  padding: 12px 32px;
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
}

/* 反馈卡片 */
.feedback-item {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 12px rgba(44, 24, 16, 0.06);
  cursor: pointer;
  transition: transform 0.2s;
}

.feedback-item:active {
  transform: scale(0.98);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.type-badge {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
}

.item-time {
  font-size: 12px;
  color: #B8A892;
}

.item-content {
  font-size: 14px;
  color: #2C1810;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 回复预览 */
.reply-preview {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  background: #FAF6F0;
  border-radius: 12px;
  margin-bottom: 12px;
}

.reply-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.reply-avatar.admin {
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
}

.reply-avatar.user {
  background: #4CAF50;
}

.reply-info {
  flex: 1;
  min-width: 0;
}

.reply-name {
  font-size: 12px;
  color: #8B7355;
  margin-bottom: 4px;
}

.reply-text {
  font-size: 13px;
  color: #5A4A3A;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #C84A3E;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reply-count {
  font-size: 12px;
  color: #B8A892;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
}

/* 提交表单 */
.submit-form {
  padding: 16px;
}

.form-card {
  background: white;
  border-radius: 16px;
  padding: 18px;
  margin-bottom: 14px;
}

/* 类型选择 */
.type-row {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.type-btn {
  flex: 1;
  padding: 10px 6px;
  background: #FAF6F0;
  border: 1.5px solid #E8E0D5;
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.type-btn:active {
  transform: scale(0.97);
}

.type-btn.selected {
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  color: white;
  border-color: #C84A3E;
}

.type-icon {
  display: block;
  font-size: 18px;
  margin-bottom: 4px;
}

.type-label {
  display: block;
  font-size: 11px;
}

.type-btn:not(.selected) .type-icon,
.type-btn:not(.selected) .type-label {
  color: #5A4A3A;
}

.type-btn.selected .type-icon,
.type-btn.selected .type-label {
  color: white;
}

/* 内容输入 */
.content-row {
  margin-bottom: 14px;
}

.content-textarea {
  width: 100%;
  height: 100px;
  background: #FAF6F0;
  border: 1.5px solid #E8E0D5;
  border-radius: 12px;
  padding: 14px;
  font-size: 15px;
  font-family: inherit;
  color: #2C1810;
  resize: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.content-textarea:focus {
  outline: none;
  border-color: #C84A3E;
  box-shadow: 0 0 0 4px rgba(200, 74, 62, 0.1);
}

.content-textarea::placeholder {
  color: #B8A892;
}

.char-counter {
  text-align: right;
  font-size: 11px;
  color: #B8A892;
  margin-top: 6px;
}

/* 联系方式 */
.contact-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.contact-label {
  font-size: 12px;
  color: #8B7355;
  white-space: nowrap;
}

.contact-input {
  flex: 1;
  height: 44px;
  background: #FAF6F0;
  border: 1.5px solid #E8E0D5;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 14px;
  font-family: inherit;
  color: #2C1810;
  transition: all 0.2s;
  box-sizing: border-box;
}

.contact-input:focus {
  outline: none;
  border-color: #C84A3E;
  box-shadow: 0 0 0 4px rgba(200, 74, 62, 0.1);
}

.contact-input::placeholder {
  color: #B8A892;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(200, 74, 62, 0.3);
  transition: all 0.2s;
}

.submit-btn:active {
  transform: scale(0.98);
}

.submit-btn:disabled {
  background: #E8E0D5;
  box-shadow: none;
  cursor: not-allowed;
}

/* 成功提示 */
.success-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0.8);
  background: rgba(44, 24, 16, 0.92);
  color: white;
  padding: 20px 40px;
  border-radius: 18px;
  font-size: 16px;
  font-weight: 500;
  opacity: 0;
  pointer-events: none;
  transition: all 0.3s ease;
  z-index: 200;
  display: flex;
  align-items: center;
  gap: 12px;
}

.success-toast.show {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1);
}

.success-icon {
  font-size: 24px;
}

/* 加载更多 */
.loading-more,
.no-more {
  text-align: center;
  padding: 16px;
  color: #8B7355;
  font-size: 14px;
}

.no-more {
  color: #B8A892;
  font-size: 13px;
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