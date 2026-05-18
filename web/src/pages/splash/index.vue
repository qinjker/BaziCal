<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

const startCalendar = () => {
  router.push('/index');
};
</script>

<template>
  <div class="page">
    <!-- 背景装饰 -->
    <div class="bg-decoration"></div>

    <!-- 内容区 -->
    <div class="content">
      <!-- Logo区域 -->
      <div class="logo-area">
        <div class="logo">
          <span class="logo-text">十</span>
        </div>
        <h1 class="app-name">十神能量日历</h1>
        <p class="app-tagline">每一种能量，都是你的优势</p>
      </div>

      <!-- 今日能量预览 -->
      <div class="today-preview">
        <div class="preview-label">今日能量</div>
        <div class="preview-ganzhi">
          <span class="preview-char">壬</span>
          <span class="preview-char">午</span>
          <div class="preview-divider"></div>
          <div class="preview-info">
            <div class="preview-info-main">正官 · 比肩</div>
            <div class="preview-info-sub">日主戊土 · 今日运势良好</div>
          </div>
        </div>
        <div class="preview-tags">
          <span class="preview-tag gold">贵人运</span>
          <span class="preview-tag green">事业进步</span>
          <span class="preview-tag gold">财运</span>
        </div>
      </div>

      <!-- 主按钮 -->
      <button class="btn-primary" @click="startCalendar">
        开启我的日历
      </button>

      <!-- 底部提示 -->
      <p class="hint">基于你的生日，定制专属能量日历</p>

      <!-- 功能说明 -->
      <div class="features">
        <div class="feature-item">
          <div class="feature-icon">📅</div>
          <div class="feature-text">每日十神</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">💬</div>
          <div class="feature-text">正向寄语</div>
        </div>
        <!-- 送礼功能暂不做 -->
        <!--
        <div class="feature-item">
          <div class="feature-icon">🎁</div>
          <div class="feature-text">送礼祝福</div>
        </div>
        -->
        <div class="feature-item" @click="router.push('/feedback')">
          <div class="feature-icon">📝</div>
          <div class="feature-text">意见反馈</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #F7F4EF;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(180deg, #FDF9F5 0%, #F7F4EF 100%);
  z-index: 0;
}

/* 内容区 */
.content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 80px 32px 60px;
}

/* Logo区域 */
.logo-area {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 100px;
  height: 100px;
  background: linear-gradient(145deg, #C84A3E 0%, #A33D33 100%);
  border-radius: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 16px 48px rgba(200, 74, 62, 0.35);
}

.logo-text {
  color: white;
  font-size: 44px;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
}

.app-name {
  font-family: 'Noto Serif SC', serif;
  font-size: 28px;
  color: #2C1810;
  letter-spacing: 3px;
  margin-bottom: 8px;
}

.app-tagline {
  font-size: 14px;
  color: #8B7355;
  letter-spacing: 1px;
}

/* 今日能量预览 */
.today-preview {
  background: white;
  border-radius: 24px;
  padding: 28px 24px;
  margin-bottom: 32px;
  box-shadow: 0 8px 32px rgba(44, 24, 16, 0.08);
}

.preview-label {
  font-size: 12px;
  color: #B8A892;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: 16px;
}

.preview-ganzhi {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}

.preview-char {
  font-family: 'Noto Serif SC', serif;
  font-size: 56px;
  color: #C84A3E;
  font-weight: 700;
}

.preview-divider {
  width: 2px;
  height: 50px;
  background: linear-gradient(180deg, #E8E0D5 0%, #F0EBE3 100%);
  border-radius: 1px;
}

.preview-info {
  text-align: center;
}

.preview-info-main {
  font-size: 18px;
  color: #2C1810;
  font-weight: 600;
  margin-bottom: 4px;
}

.preview-info-sub {
  font-size: 13px;
  color: #8B7355;
}

.preview-tags {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.preview-tag {
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  color: white;
  font-weight: 500;
}

.preview-tag.gold {
  background: linear-gradient(145deg, #D4A843 0%, #C4983A 100%);
}

.preview-tag.green {
  background: linear-gradient(145deg, #5A8A6A 0%, #4A7A5A 100%);
}

/* 主按钮 */
.btn-primary {
  display: block;
  width: 100%;
  height: 58px;
  background: linear-gradient(145deg, #C84A3E 0%, #A33D33 100%);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  box-shadow: 0 10px 28px rgba(200, 74, 62, 0.35);
  transition: all 0.2s ease;
}

.btn-primary:active {
  transform: scale(0.97);
  box-shadow: 0 6px 18px rgba(200, 74, 62, 0.3);
}

/* 底部提示 */
.hint {
  text-align: center;
  margin-top: 32px;
  font-size: 13px;
  color: #B8A892;
}

/* 功能入口 */
.features {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: auto;
  padding-top: 32px;
}

.feature-item {
  text-align: center;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  -webkit-tap-highlight-color: transparent;
}

.feature-item:active {
  transform: scale(0.95);
}

.feature-icon {
  width: 48px;
  height: 48px;
  background: white;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin: 0 auto 8px;
  box-shadow: 0 4px 12px rgba(44, 24, 16, 0.06);
}

.feature-text {
  font-size: 12px;
  color: #8B7355;
}
</style>