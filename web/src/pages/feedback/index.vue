<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 反馈类型
const feedbackTypes = ['功能建议', '问题反馈', '体验优化', '其他'];
const selectedType = ref('功能建议');

// 常见问题快捷选择
const quickQuestions = [
  '日历显示不准',
  '想增加XX功能',
  '界面交互问题',
  '数据同步问题',
  '广告/付费问题',
  '其他问题'
];

// 反馈内容
const feedbackContent = ref('');
const charCount = ref(0);

// 联系方式
const contact = ref('');

// 是否提交中
const submitting = ref(false);

// 是否显示成功提示
const showSuccess = ref(false);

// 选择反馈类型
const selectType = (type: string) => {
  selectedType.value = type;
};

// 快速填写
const quickFill = (question: string) => {
  const existing = feedbackContent.value;
  if (existing) {
    feedbackContent.value = existing + '\n' + question;
  } else {
    feedbackContent.value = question;
  }
  updateCharCount();
};

// 更新字符计数
const updateCharCount = () => {
  charCount.value = feedbackContent.value.length;
};

// 返回日历
const goBack = () => {
  router.push('/calendar');
};

// 提交反馈
const submitFeedback = async () => {
  if (feedbackContent.value.trim().length < 10) {
    alert('请输入至少10个字符的反馈内容');
    return;
  }

  submitting.value = true;

  try {
    // 模拟提交
    await new Promise(resolve => setTimeout(resolve, 800));
    showSuccess.value = true;

    setTimeout(() => {
      router.push('/calendar');
    }, 1500);
  } catch (err) {
    alert('提交失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 检查是否可以提交
const canSubmit = () => {
  return feedbackContent.value.trim().length >= 10;
};
</script>

<template>
  <div class="phone-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <a href="#/calendar" class="nav-back" @click.prevent="goBack">←</a>
      <button
        class="btn-submit"
        :disabled="!canSubmit() || submitting"
        @click="submitFeedback"
      >
        {{ submitting ? '...' : '✓' }}
      </button>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-icon">💬</div>
      <h1 class="page-title">您的声音</h1>
      <p class="page-desc">帮助我们做得更好</p>
      <p class="submit-hint">填写完成后点击右上角 ✓ 提交</p>
    </div>

    <!-- 反馈类型 -->
    <div class="feedback-type-card">
      <div class="type-label">反馈类型</div>
      <div class="type-tags">
        <div
          v-for="type in feedbackTypes"
          :key="type"
          class="type-tag"
          :class="{ selected: selectedType === type }"
          @click="selectType(type)"
        >
          {{ type }}
        </div>
      </div>
    </div>

    <!-- 常见问题快捷选择 -->
    <div class="quick-questions-card">
      <div class="quick-label">常见问题（点击快速填写）</div>
      <div class="quick-tags">
        <div
          v-for="question in quickQuestions"
          :key="question"
          class="quick-tag"
          @click="quickFill(question)"
        >
          {{ question }}
        </div>
      </div>
    </div>

    <!-- 反馈内容 -->
    <div class="feedback-content-card">
      <div class="content-label">详细描述</div>
      <textarea
        v-model="feedbackContent"
        class="content-textarea"
        placeholder="请描述您的建议或遇到的问题..."
        @input="updateCharCount"
      ></textarea>
      <div class="char-count">{{ charCount }}/500</div>
    </div>

    <!-- 联系方式 -->
    <div class="contact-card">
      <div class="contact-label">联系方式（选填）</div>
      <input
        v-model="contact"
        type="text"
        class="contact-input"
        placeholder="微信号 / 邮箱 / 手机号"
      />
    </div>

    <!-- 提示 -->
    <div class="tip-box">
      <span class="tip-icon">💡</span>
      <span class="tip-text">我们会认真阅读每一条反馈，并在3个工作日内回复您</span>
    </div>

    <!-- 成功提示 -->
    <div class="success-toast" :class="{ show: showSuccess }">
      <span class="success-icon">✓</span>
      <span>感谢您的反馈！</span>
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
  padding-bottom: 40px;
}

/* 导航栏 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
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

/* 页面标题 */
.page-header {
  padding: 0 24px 20px;
  text-align: center;
}

.page-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin: 0 auto 12px;
  box-shadow: 0 6px 20px rgba(200, 74, 62, 0.25);
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 22px;
  color: #2C1810;
  margin-bottom: 6px;
}

.page-desc {
  font-size: 13px;
  color: #8B7355;
}

.submit-hint {
  font-size: 12px;
  color: #C84A3E;
  margin-top: 8px;
  font-weight: 500;
}

/* 提交按钮 - 右上角 */
.btn-submit {
  width: 44px;
  height: 44px;
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 20px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(200, 74, 62, 0.3);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-submit:active {
  transform: scale(0.95);
}

.btn-submit:disabled {
  background: #E8E0D5;
  box-shadow: none;
  cursor: not-allowed;
}

/* 反馈类型 */
.feedback-type-card {
  margin: 0 20px 16px;
  background: white;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(44, 24, 16, 0.06);
}

.type-label {
  font-size: 13px;
  color: #8B7355;
  margin-bottom: 14px;
}

.type-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.type-tag {
  padding: 10px 18px;
  background: #FAF6F0;
  border: 1.5px solid #E8E0D5;
  border-radius: 12px;
  font-size: 14px;
  color: #5A4A3A;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.type-tag:active {
  transform: scale(0.95);
}

.type-tag.selected {
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
  color: white;
  border-color: #C84A3E;
  box-shadow: 0 4px 14px rgba(200, 74, 62, 0.3);
}

/* 常见问题快捷选择 */
.quick-questions-card {
  margin: 0 20px 16px;
  background: white;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(44, 24, 16, 0.06);
}

.quick-label {
  font-size: 13px;
  color: #8B7355;
  margin-bottom: 14px;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-tag {
  padding: 8px 14px;
  background: #FFF9F5;
  border: 1.5px solid #F0E8E0;
  border-radius: 10px;
  font-size: 13px;
  color: #8B7355;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.quick-tag:active {
  transform: scale(0.95);
  background: #FAF0EB;
}

/* 反馈内容 */
.feedback-content-card {
  margin: 0 20px 16px;
  background: white;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(44, 24, 16, 0.06);
}

.content-label {
  font-size: 13px;
  color: #8B7355;
  margin-bottom: 14px;
}

.content-textarea {
  width: 100%;
  height: 140px;
  background: #FAF6F0;
  border: 1.5px solid #E8E0D5;
  border-radius: 14px;
  padding: 16px;
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

.char-count {
  text-align: right;
  font-size: 12px;
  color: #B8A892;
  margin-top: 8px;
}

/* 联系方式 */
.contact-card {
  margin: 0 20px 20px;
  background: white;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(44, 24, 16, 0.06);
}

.contact-label {
  font-size: 13px;
  color: #8B7355;
  margin-bottom: 14px;
}

.contact-input {
  width: 100%;
  height: 50px;
  background: #FAF6F0;
  border: 1.5px solid #E8E0D5;
  border-radius: 12px;
  padding: 0 16px;
  font-size: 15px;
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

/* 提示 */
.tip-box {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #FFF9F5;
  border-radius: 12px;
  padding: 14px 16px;
  margin: 0 20px 24px;
}

.tip-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.tip-text {
  font-size: 13px;
  color: #8B7355;
  line-height: 1.5;
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