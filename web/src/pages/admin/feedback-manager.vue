<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { apiService } from '@/api';
import AdminLayout from './components/AdminLayout.vue';
import dayjs from 'dayjs';

interface FeedbackReply {
  id: string;
  content: string;
  author_type: 'user' | 'admin';
  author_name: string;
  created_at: string;
}

interface Feedback {
  id: string;
  type: '功能建议' | '问题反馈' | '体验优化' | '其他';
  content: string;
  contact?: string;
  status: 'pending' | 'reviewed' | 'replied' | 'closed';
  user_id?: string;
  device_id?: string;
  created_at: string;
  replies?: FeedbackReply[];
}

const router = useRouter();

// List state
const feedbacks = ref<Feedback[]>([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);

// Filter state
const statusFilter = ref('');
const typeFilter = ref('');

// Modal state
const showModal = ref(false);
const selectedFeedback = ref<Feedback | null>(null);
const replyContent = ref('');
const submitting = ref(false);

// Status and type options
const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '待处理', value: 'pending' },
  { label: '已查看', value: 'reviewed' },
  { label: '已回复', value: 'replied' },
  { label: '已关闭', value: 'closed' },
];

const typeOptions = [
  { label: '全部类型', value: '' },
  { label: '功能建议', value: '功能建议' },
  { label: '问题反馈', value: '问题反馈' },
  { label: '体验优化', value: '体验优化' },
  { label: '其他', value: '其他' },
];

const statusMap: Record<string, { label: string; bg: string; color: string }> = {
  pending: { label: '待处理', bg: '#FFF3E0', color: '#E65100' },
  reviewed: { label: '已查看', bg: '#E3F2FD', color: '#2196F3' },
  replied: { label: '已回复', bg: '#E8F5E9', color: '#4CAF50' },
  closed: { label: '已关闭', bg: '#F5F5F5', color: '#999' },
};

const typeMap: Record<string, { bg: string; color: string }> = {
  '功能建议': { bg: '#E8F5E9', color: '#4CAF50' },
  '问题反馈': { bg: '#FFEBEE', color: '#C84A3E' },
  '体验优化': { bg: '#E3F2FD', color: '#2196F3' },
  '其他': { bg: '#F5F5F5', color: '#8B7355' },
};

onMounted(() => {
  const token = localStorage.getItem('admin_token');
  if (!token) {
    router.push('/admin/login');
    return;
  }
  fetchFeedbacks();
});

const fetchFeedbacks = async () => {
  loading.value = true;
  try {
    const params: any = { page: page.value, pageSize: pageSize.value };
    if (statusFilter.value) params.status = statusFilter.value;
    if (typeFilter.value) params.type = typeFilter.value;

    const response = await apiService.getAdminFeedbacks(params);
    if (response.code === 0) {
      feedbacks.value = response.data.feedbacks;
      total.value = response.data.total;
    }
  } catch (err) {
    console.error('Fetch feedbacks error:', err);
  } finally {
    loading.value = false;
  }
};

const handleFilterChange = () => {
  page.value = 1;
  fetchFeedbacks();
};

const handlePageChange = (newPage: number) => {
  page.value = newPage;
  fetchFeedbacks();
};

const openFeedbackDetail = async (feedback: Feedback) => {
  selectedFeedback.value = { ...feedback, replies: [] };
  showModal.value = true;
  await fetchReplies(feedback.id);
};

const fetchReplies = async (feedbackId: string) => {
  try {
    const token = localStorage.getItem('admin_token');
    const response = await fetch(`/api/v1/admin/feedbacks/${feedbackId}/replies`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const data = await response.json();
    if (data.code === 0 && selectedFeedback.value) {
      selectedFeedback.value.replies = data.data.replies || [];
    }
  } catch (err) {
    console.error('Fetch replies error:', err);
  }
};

const submitReply = async () => {
  if (!replyContent.value.trim() || !selectedFeedback.value || submitting.value) return;

  submitting.value = true;
  try {
    const response = await apiService.replyFeedback(selectedFeedback.value.id, replyContent.value.trim());
    if (response.code === 0) {
      replyContent.value = '';
      await fetchReplies(selectedFeedback.value.id);
      // Update feedback status in list
      const idx = feedbacks.value.findIndex(f => f.id === selectedFeedback.value?.id);
      if (idx !== -1) {
        feedbacks.value[idx].status = 'replied';
      }
      if (selectedFeedback.value) {
        selectedFeedback.value.status = 'replied';
      }
    }
  } catch (err) {
    console.error('Submit reply error:', err);
    alert('回复失败，请重试');
  } finally {
    submitting.value = false;
  }
};

const updateStatus = async (feedbackId: string, newStatus: string) => {
  try {
    const response = await apiService.updateFeedbackStatus(feedbackId, newStatus);
    if (response.code === 0) {
      const idx = feedbacks.value.findIndex(f => f.id === feedbackId);
      if (idx !== -1) {
        feedbacks.value[idx].status = newStatus as any;
      }
      if (selectedFeedback.value?.id === feedbackId) {
        selectedFeedback.value.status = newStatus as any;
      }
    }
  } catch (err) {
    console.error('Update status error:', err);
    alert('更新状态失败，请重试');
  }
};

const formatTime = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm');
};

const formatRelativeTime = (dateStr: string) => {
  const diff = Date.now() - new Date(dateStr).getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  if (days === 0) return '今天';
  if (days === 1) return '昨天';
  if (days < 7) return `${days}天前`;
  return dayjs(dateStr).format('MM/DD');
};

const getAuthorIcon = (authorType: string) => {
  return authorType === 'admin' ? '👨‍💼' : '👤';
};

const getAuthorColor = (authorType: string) => {
  return authorType === 'admin' ? '#C84A3E' : '#4CAF50';
};

const closeModal = () => {
  showModal.value = false;
  selectedFeedback.value = null;
};

const totalPages = computed(() => Math.ceil(total.value / pageSize.value));
</script>

<template>
  <AdminLayout>
    <div class="admin-page">
      <div class="page-header">
        <h2 class="page-title">反馈管理</h2>
        <span class="feedback-count">共 {{ total }} 条反馈</span>
      </div>
    <div class="filters">
      <select v-model="statusFilter" @change="handleFilterChange" class="filter-select">
        <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
      </select>
      <select v-model="typeFilter" @change="handleFilterChange" class="filter-select">
        <option v-for="opt in typeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
      </select>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 反馈列表 -->
    <div v-else class="feedback-list">
      <div class="list-header">
        <span class="col-type">类型</span>
        <span class="col-content">内容</span>
        <span class="col-status">状态</span>
        <span class="col-time">时间</span>
      </div>

      <div
        v-for="feedback in feedbacks"
        :key="feedback.id"
        class="feedback-item"
        @click="openFeedbackDetail(feedback)"
      >
        <span class="col-type">
          <span
            class="type-badge"
            :style="{ background: typeMap[feedback.type]?.bg, color: typeMap[feedback.type]?.color }"
          >
            {{ feedback.type }}
          </span>
        </span>
        <span class="col-content">{{ feedback.content.slice(0, 30) }}{{ feedback.content.length > 30 ? '...' : '' }}</span>
        <span class="col-status">
          <span
            class="status-badge"
            :style="{ background: statusMap[feedback.status]?.bg, color: statusMap[feedback.status]?.color }"
          >
            {{ statusMap[feedback.status]?.label }}
          </span>
        </span>
        <span class="col-time">{{ formatRelativeTime(feedback.created_at) }}</span>
      </div>

      <div v-if="feedbacks.length === 0" class="empty">暂无反馈数据</div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <button class="page-btn" :disabled="page === 1" @click="handlePageChange(page - 1)">上一页</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button class="page-btn" :disabled="page >= totalPages" @click="handlePageChange(page + 1)">下一页</button>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>反馈详情</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>

        <div v-if="selectedFeedback" class="modal-body">
          <!-- 反馈信息 -->
          <div class="feedback-info">
            <div class="info-header">
              <span
                class="type-badge"
                :style="{ background: typeMap[selectedFeedback.type]?.bg, color: typeMap[selectedFeedback.type]?.color }"
              >
                {{ selectedFeedback.type }}
              </span>
              <select
                class="status-select"
                :value="selectedFeedback.status"
                @change="updateStatus(selectedFeedback.id, ($event.target as HTMLSelectElement).value)"
              >
                <option v-for="opt in statusOptions.slice(1)" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </div>
            <p class="feedback-content">{{ selectedFeedback.content }}</p>
            <div class="feedback-meta">
              <span v-if="selectedFeedback.contact">联系方式: {{ selectedFeedback.contact }}</span>
              <span>{{ formatTime(selectedFeedback.created_at) }}</span>
            </div>
          </div>

          <!-- 回复列表 -->
          <div class="replies-section">
            <h4>对话</h4>
            <div class="replies-list">
              <div
                v-for="reply in selectedFeedback.replies"
                :key="reply.id"
                class="reply-item"
              >
                <span class="reply-avatar">{{ getAuthorIcon(reply.author_type) }}</span>
                <div class="reply-bubble">
                  <span class="reply-name" :style="{ color: getAuthorColor(reply.author_type) }">{{ reply.author_name }}</span>
                  <p class="reply-content">{{ reply.content }}</p>
                  <span class="reply-time">{{ formatTime(reply.created_at) }}</span>
                </div>
              </div>
              <div v-if="!selectedFeedback.replies?.length" class="no-replies">暂无回复</div>
            </div>
          </div>

          <!-- 回复输入 -->
          <div class="reply-input-area">
            <textarea
              v-model="replyContent"
              class="reply-textarea"
              placeholder="输入回复内容..."
              rows="3"
            ></textarea>
            <button
              class="submit-btn"
              :disabled="!replyContent.trim() || submitting"
              @click="submitReply"
            >
              {{ submitting ? '发送中...' : '发送回复' }}
            </button>
          </div>
        </div>
      </div>
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

.feedback-count {
  font-size: 14px;
  color: #888;
}

.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.filter-select {
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
}

.filter-select option {
  background: #1a1a2e;
  color: #fff;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #666;
}

.feedback-list {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  overflow: hidden;
}

.list-header {
  display: grid;
  grid-template-columns: 80px 1fr 80px 60px;
  gap: 10px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 12px;
  color: #888;
}

.feedback-item {
  display: grid;
  grid-template-columns: 80px 1fr 80px 60px;
  gap: 10px;
  padding: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.feedback-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.feedback-item:last-child {
  border-bottom: none;
}

.type-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.col-content {
  color: #ccc;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-time {
  color: #666;
  font-size: 12px;
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
  margin-top: 20px;
}

.page-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #888;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  background: #1a1a2e;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.05);
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
}

.close-btn {
  background: none;
  border: none;
  color: #888;
  font-size: 24px;
  cursor: pointer;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.feedback-info {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 20px;
}

.info-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.status-select {
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 12px;
}

.status-select option {
  background: #1a1a2e;
}

.feedback-content {
  margin: 10px 0;
  line-height: 1.5;
}

.feedback-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #666;
}

.replies-section h4 {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #888;
}

.replies-list {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 15px;
}

.reply-item {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.reply-avatar {
  font-size: 20px;
}

.reply-bubble {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 10px 15px;
}

.reply-name {
  font-size: 12px;
  font-weight: bold;
}

.reply-content {
  margin: 8px 0;
  line-height: 1.4;
}

.reply-time {
  font-size: 11px;
  color: #666;
}

.no-replies {
  text-align: center;
  padding: 30px;
  color: #666;
}

.reply-input-area {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 15px;
}

.reply-textarea {
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  resize: none;
  margin-bottom: 10px;
}

.reply-textarea::placeholder {
  color: #666;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  border: none;
  border-radius: 12px;
  color: #1a1a2e;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>