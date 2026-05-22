<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { apiService } from '@/api';
import AdminLayout from './components/AdminLayout.vue';
import dayjs from 'dayjs';

interface Message {
  id: string;
  content: string;
  category: string;
  created_at: string;
}

const router = useRouter();

// List state
const messages = ref<Message[]>([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);

// Modal state
const showModal = ref(false);
const editingMessage = ref<Message | null>(null);
const formContent = ref('');
const formCategory = ref('general');
const submitting = ref(false);

const categoryOptions = [
  { label: '通用', value: 'general' },
  { label: '情感', value: 'emotion' },
  { label: '事业', value: 'career' },
  { label: '财运', value: 'fortune' },
];

onMounted(() => {
  const token = localStorage.getItem('admin_token');
  if (!token) {
    router.push('/admin/login');
    return;
  }
  fetchMessages();
});

const fetchMessages = async () => {
  loading.value = true;
  try {
    const response = await apiService.getAdminMessages({
      page: page.value,
      pageSize: pageSize.value,
    });
    if (response.code === 0) {
      messages.value = response.data.messages;
      total.value = response.data.total;
    }
  } catch (err) {
    console.error('Fetch messages error:', err);
  } finally {
    loading.value = false;
  }
};

const openAddModal = () => {
  editingMessage.value = null;
  formContent.value = '';
  formCategory.value = 'general';
  showModal.value = true;
};

const openEditModal = (msg: Message) => {
  editingMessage.value = msg;
  formContent.value = msg.content;
  formCategory.value = msg.category;
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  editingMessage.value = null;
  formContent.value = '';
  formCategory.value = 'general';
};

const handleSubmit = async () => {
  if (!formContent.value.trim()) return;

  submitting.value = true;
  try {
    if (editingMessage.value) {
      // Update
      await apiService.updateMessage(editingMessage.value.id, {
        content: formContent.value.trim(),
        category: formCategory.value,
      });
    } else {
      // Create
      await apiService.createMessage({
        content: formContent.value.trim(),
        category: formCategory.value,
      });
    }
    closeModal();
    fetchMessages();
  } catch (err) {
    console.error('Submit error:', err);
    alert('操作失败，请重试');
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (id: string) => {
  if (!confirm('确定要删除这条寄语吗？')) return;

  try {
    await apiService.deleteMessage(id);
    fetchMessages();
  } catch (err) {
    console.error('Delete error:', err);
    alert('删除失败，请重试');
  }
};

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm');
};

const getCategoryLabel = (category: string) => {
  const found = categoryOptions.find(c => c.value === category);
  return found ? found.label : category;
};
</script>

<template>
  <AdminLayout>
    <div class="message-manager">
      <div class="header">
        <h1>寄语管理</h1>
        <button class="add-btn" @click="openAddModal">添加寄语</button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading">加载中...</div>

      <!-- Table -->
      <div v-else class="table-container">
        <table class="message-table">
          <thead>
            <tr>
              <th>寄语内容</th>
              <th>分类</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="msg in messages" :key="msg.id">
              <td class="content-cell">{{ msg.content }}</td>
              <td>
                <span class="category-badge">{{ getCategoryLabel(msg.category) }}</span>
              </td>
              <td class="time-cell">{{ formatDate(msg.created_at) }}</td>
              <td class="action-cell">
                <button class="edit-btn" @click="openEditModal(msg)">编辑</button>
                <button class="delete-btn" @click="handleDelete(msg.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Empty -->
        <div v-if="messages.length === 0" class="empty">
          暂无寄语，点击上方按钮添加
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="total > pageSize" class="pagination">
        <button
          :disabled="page === 1"
          @click="page--; fetchMessages()"
        >
          上一页
        </button>
        <span>{{ page }} / {{ Math.ceil(total / pageSize) }}</span>
        <button
          :disabled="page * pageSize >= total"
          @click="page++; fetchMessages()"
        >
          下一页
        </button>
      </div>

      <!-- Modal -->
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal">
          <div class="modal-header">
            <h2>{{ editingMessage ? '编辑寄语' : '添加寄语' }}</h2>
            <button class="close-btn" @click="closeModal">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>寄语内容</label>
              <textarea
                v-model="formContent"
                rows="4"
                maxlength="200"
                placeholder="请输入寄语内容（最多200字）"
              ></textarea>
              <span class="char-count">{{ formContent.length }}/200</span>
            </div>
            <div class="form-group">
              <label>分类</label>
              <select v-model="formCategory">
                <option v-for="opt in categoryOptions" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="cancel-btn" @click="closeModal">取消</button>
            <button
              class="submit-btn"
              :disabled="!formContent.trim() || submitting"
              @click="handleSubmit"
            >
              {{ submitting ? '提交中...' : '确定' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<style scoped>
.message-manager {
  padding: 24px;
  color: #fff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h1 {
  font-size: 20px;
  margin: 0;
}

.add-btn {
  padding: 10px 20px;
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.add-btn:hover {
  opacity: 0.9;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.table-container {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
}

.message-table {
  width: 100%;
  border-collapse: collapse;
}

.message-table th,
.message-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.message-table th {
  background: rgba(255, 255, 255, 0.03);
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.message-table td {
  font-size: 14px;
}

.content-cell {
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-cell {
  color: #666;
  font-size: 12px;
}

.category-badge {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(214, 90, 78, 0.2);
  color: #D65A4E;
  border-radius: 6px;
  font-size: 12px;
}

.action-cell {
  white-space: nowrap;
}

.edit-btn,
.delete-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 8px;
}

.edit-btn {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.delete-btn {
  background: rgba(200, 74, 62, 0.2);
  color: #C84A3E;
}

.edit-btn:hover,
.delete-btn:hover {
  opacity: 0.8;
}

.empty {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.pagination button {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination span {
  color: #666;
  font-size: 14px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #1a1a2e;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #888;
}

.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  font-family: inherit;
  box-sizing: border-box;
}

.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #D65A4E;
}

.form-group select {
  cursor: pointer;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.cancel-btn,
.submit-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.cancel-btn {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.submit-btn {
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  color: white;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>