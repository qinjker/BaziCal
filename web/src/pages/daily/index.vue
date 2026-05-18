<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { apiService } from '@/api';
import dayjs from 'dayjs';
import type { DailyData, DailyDetailResponse } from '@/types';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const date = ref(route.query.date as string || dayjs().format('YYYY-MM-DD'));
const dayData = ref<DailyData | null>(null);
const dailyDetail = ref<DailyDetailResponse | null>(null);
const loading = ref(false);

// 如果没有用户数据，跳转回首页
onMounted(() => {
  userStore.restore();
  if (!userStore.user) {
    router.push('/index');
    return;
  }
  fetchDailyData();
  fetchDailyDetail();
});

// 获取每日数据 (从日历API)
const fetchDailyData = async () => {
  if (!userStore.user) return;

  try {
    const currentDate = dayjs(date.value);
    const response = await apiService.getCalendar(
      userStore.user.userId,
      currentDate.year(),
      currentDate.month() + 1
    );
    if (response.code === 0 && response.data) {
      dayData.value = response.data.days.find(d => d.date === date.value) || null;
    }
  } catch (err) {
    console.error('Fetch daily data error:', err);
  }
};

// 获取每日详情
const fetchDailyDetail = async () => {
  if (!userStore.user) return;

  loading.value = true;
  try {
    const response = await apiService.getDailyDetail(userStore.user.userId, date.value);
    if (response.code === 0 && response.data) {
      dailyDetail.value = response.data;
    }
  } catch (err) {
    console.error('Fetch daily detail error:', err);
  } finally {
    loading.value = false;
  }
};

// 返回日历
const goBack = () => {
  router.push('/calendar');
};

// 跳转到日历
const goToCalendar = () => {
  router.push('/calendar');
};

// 日期格式化
const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY年M月D日');
};

// 星期几
const getWeekday = (dateStr: string) => {
  const weekdays = ['日', '一', '二', '三', '四', '五', '六'];
  return weekdays[dayjs(dateStr).day()];
};

const weekdays = ['日', '一', '二', '三', '四', '五', '六'];

// 获取农历信息
const getLunarInfo = computed(() => {
  if (!dayData.value) return '';
  return dayData.value.lunarDate;
});

// 获取干支信息
const ganzhi = computed(() => {
  return dayData.value?.ganzhi || dailyDetail.value?.ganzhi || '';
});

// 获取今日十神
const shishenList = computed(() => {
  if (!dailyDetail.value) return [];
  const result = [];
  if (dailyDetail.value.shishen) result.push(dailyDetail.value.shishen);
  if (dailyDetail.value.branchShishen) result.push(dailyDetail.value.branchShishen);
  return result;
});

// 获取十神颜色类
const getTenGodClass = (shishen: string): string => {
  const map: Record<string, string> = {
    '正官': 'gold', '七杀': 'gold',
    '正印': 'green', '偏印': 'green',
    '正财': 'gold', '偏财': 'gold',
    '比肩': 'gold', '劫财': 'red',
    '食神': 'green', '伤官': 'red'
  };
  return map[shishen] || 'gold';
};
</script>

<template>
  <div class="phone-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <a href="#/calendar" class="nav-back" @click.prevent="goBack">←</a>
      <div class="nav-date">
        <div class="nav-date-main">{{ formatDate(date) }}</div>
        <div class="nav-date-lunar">{{ getLunarInfo }} · {{ weekdays[dayjs(date).day()] }}日</div>
      </div>
      <div class="nav-spacer"></div>
    </div>

    <!-- 顶部用户信息卡 -->
    <div class="user-card">
      <div class="user-info-left">
        <div class="user-avatar">十</div>
        <div class="user-text">
          <div class="user-name">日主：{{ userStore.bazi?.day.stem }}{{ userStore.bazi?.day.branch }}</div>
          <div class="user-desc">生于{{ userStore.user?.birthday }}</div>
        </div>
      </div>
      <div class="user-energy">
        <div class="user-energy-value">{{ dailyDetail?.shishen || dayData?.shishen || '正官' }}</div>
        <div class="user-energy-label">今日能量</div>
      </div>
    </div>

    <!-- 主分享按钮 (暂时隐藏，送礼功能未实现) -->
    <!--
    <a href="#" class="share-main-btn">
      <div class="share-main-btn-text">✨ 生成分享卡</div>
      <div class="share-main-btn-hint">分享今日能量给朋友</div>
    </a>
    -->

    <!-- 干支大字区域 -->
    <div class="ganzhi-section">
      <div class="ganzhi-text">{{ ganzhi }}</div>
      <div class="ganzhi-info">{{ date }} · {{ dayData?.lunarDate || '' }}</div>
      <div class="ten-god-container">
        <div class="ten-god-row">
          <div
            v-for="ss in shishenList.slice(0, 2)"
            :key="ss"
            class="ten-god-tag"
            :class="getTenGodClass(ss)"
          >
            {{ ss }}
          </div>
        </div>
        <div v-if="shishenList.length > 2" class="ten-god-row">
          <div
            v-for="ss in shishenList.slice(2)"
            :key="ss"
            class="ten-god-tag"
            :class="getTenGodClass(ss)"
          >
            {{ ss }}
          </div>
        </div>
      </div>
    </div>

    <!-- 寄语卡片 -->
    <div class="message-card">
      <div class="message-header">
        <span class="message-icon">💬</span>
        <span class="message-title">今日寄语</span>
      </div>
      <p class="message-main">「{{ dailyDetail?.energy?.description || '今日宜稳中求进，贵人运不错，适合谈判与推进项目。' }}」</p>
      <ul v-if="dailyDetail?.messages?.length" class="message-list">
        <li v-for="(msg, idx) in dailyDetail.messages" :key="idx">
          {{ msg }}
        </li>
      </ul>
    </div>

    <!-- 底部功能栏 -->
    <div class="action-bar">
      <a href="#" class="action-item primary">
        <div class="action-icon">✨</div>
        <div class="action-label">分享</div>
      </a>
      <a href="#" class="action-item">
        <div class="action-icon">💾</div>
        <div class="action-label">保存</div>
      </a>
      <a href="#/calendar" class="action-item" @click.prevent="goToCalendar">
        <div class="action-icon">📅</div>
        <div class="action-label">日历</div>
      </a>
    </div>

    <!-- 底部Tab栏 -->
    <div class="tab-bar">
      <a href="#/calendar" class="tab-item">
        <span class="tab-icon">📅</span>
        <span class="tab-label">月历</span>
      </a>
      <a href="#/daily" class="tab-item active">
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
  padding-bottom: 100px;
}

/* 导航栏 */
.navbar {
  display: flex;
  align-items: center;
  padding: 14px 18px 24px;
}

.nav-back {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: #2C1810;
  font-size: 26px;
  border-radius: 14px;
  transition: background 0.2s;
}

.nav-back:active {
  background: rgba(0,0,0,0.05);
}

.nav-date {
  flex: 1;
  text-align: center;
}

.nav-date-main {
  font-size: 18px;
  color: #2C1810;
  font-weight: 600;
}

.nav-date-lunar {
  font-size: 13px;
  color: #8B7355;
  margin-top: 3px;
}

.nav-spacer {
  width: 48px;
}

/* 顶部用户信息卡 */
.user-card {
  margin: 0 20px 20px;
  background: white;
  border-radius: 18px;
  padding: 16px 20px;
  box-shadow: 0 4px 16px rgba(44, 24, 16, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 44px;
  height: 44px;
  background: linear-gradient(145deg, #C84A3E 0%, #A33D33 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.user-text {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 15px;
  color: #2C1810;
  font-weight: 600;
}

.user-desc {
  font-size: 12px;
  color: #8B7355;
  margin-top: 2px;
}

.user-energy {
  text-align: right;
}

.user-energy-value {
  font-size: 16px;
  color: #D4A843;
  font-weight: 600;
}

.user-energy-label {
  font-size: 11px;
  color: #B8A892;
}

/* 干支大字区域 */
.ganzhi-section {
  text-align: center;
  padding: 24px 24px 28px;
  background: linear-gradient(180deg, #FFFFFF 0%, #FAF8F4 100%);
  margin: 0 20px;
  border-radius: 20px;
  box-shadow: 0 4px 16px rgba(44, 24, 16, 0.06);
  margin-bottom: 20px;
}

.ganzhi-text {
  font-family: 'Noto Serif SC', serif;
  font-size: 72px;
  color: #C84A3E;
  line-height: 1;
  margin-bottom: 16px;
  letter-spacing: 10px;
  text-shadow: 0 4px 20px rgba(200, 74, 62, 0.25);
}

.ganzhi-info {
  font-size: 14px;
  color: #8B7355;
  margin-bottom: 18px;
}

/* 十神标签 */
.ten-god-container {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

.ten-god-row {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.ten-god-row:first-child {
  margin-bottom: 8px;
}

.ten-god-tag {
  padding: 8px 16px;
  border-radius: 18px;
  font-size: 14px;
  color: white;
  font-weight: 500;
  box-shadow: 0 3px 10px rgba(0,0,0,0.12);
}

.ten-god-tag.gold {
  background: linear-gradient(145deg, #E0B850 0%, #C49A3A 100%);
}

.ten-god-tag.green {
  background: linear-gradient(145deg, #689A78 0%, #4A7A5A 100%);
}

.ten-god-tag.red {
  background: linear-gradient(145deg, #D65A4E 0%, #B8443A 100%);
}

/* 寄语卡片 */
.message-card {
  background: #FFFFFF;
  border-radius: 22px;
  padding: 26px 24px;
  margin: 0 20px 20px;
  box-shadow: 0 4px 20px rgba(44, 24, 16, 0.08);
  border: 1px solid rgba(232, 224, 213, 0.6);
}

.message-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.message-icon {
  font-size: 20px;
}

.message-title {
  font-size: 13px;
  color: #B8A892;
  font-weight: 500;
  letter-spacing: 2px;
}

.message-main {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  color: #2C1810;
  line-height: 1.7;
  margin-bottom: 20px;
  font-weight: 600;
}

.message-list {
  list-style: none;
}

.message-list li {
  font-size: 14px;
  color: #5A4A3A;
  padding: 12px 0;
  padding-left: 20px;
  position: relative;
  border-bottom: 1px solid rgba(232, 224, 213, 0.5);
}

.message-list li:last-child {
  border-bottom: none;
}

.message-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  background: #D4A843;
  border-radius: 50%;
}

/* 底部功能栏 */
.action-bar {
  display: flex;
  justify-content: space-around;
  margin: 0 20px;
  background: #FFFFFF;
  border-radius: 20px;
  padding: 16px 8px;
  box-shadow: 0 6px 24px rgba(44, 24, 16, 0.08);
  border: 1px solid rgba(232, 224, 213, 0.6);
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 12px;
  transition: all 0.2s;
  text-decoration: none;
}

.action-item:active {
  background: #FAF6F0;
  transform: scale(0.95);
}

.action-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(145deg, #FAF6F0 0%, #F0EBE3 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.action-item.primary .action-icon {
  background: linear-gradient(145deg, #C84A3E 0%, #A33D33 100%);
  box-shadow: 0 4px 12px rgba(200, 74, 62, 0.3);
}

.action-label {
  font-size: 12px;
  color: #5A4A3A;
  font-weight: 500;
}

.action-item.primary .action-label {
  color: #C84A3E;
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
  padding: 10px 0;
  padding-bottom: calc(10px + env(safe-area-inset-bottom, 0px));
  border-top: 1px solid rgba(232, 224, 213, 0.8);
  z-index: 100;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px 20px;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.2s;
}

.tab-icon {
  font-size: 26px;
  margin-bottom: 4px;
}

.tab-label {
  font-size: 11px;
  color: #B8A892;
  font-weight: 500;
}

.tab-item.active .tab-icon,
.tab-item.active .tab-label {
  color: #C84A3E;
}

.tab-item.active .tab-icon {
  transform: scale(1.1);
}
</style>