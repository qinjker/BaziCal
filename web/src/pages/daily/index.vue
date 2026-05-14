<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { apiService } from '@/api';
import dayjs from 'dayjs';
import type { DailyData } from '@/types';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const date = ref(route.query.date as string || dayjs().format('YYYY-MM-DD'));
const dayData = ref<DailyData | null>(null);
const loading = ref(false);

// 如果没有用户数据，跳转回首页
onMounted(() => {
  userStore.restore();
  if (!userStore.user) {
    router.push('/');
    return;
  }
  fetchDailyData();
});

// 获取每日数据
const fetchDailyData = async () => {
  if (!userStore.user) return;

  loading.value = true;
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
  } finally {
    loading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.push('/calendar');
};

// 日期格式化
const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('MM月DD日');
};

// 星期几
const getWeekday = (dateStr: string) => {
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  return weekdays[dayjs(dateStr).day()];
};

// 农历日期 (简化)
const getLunarDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY年MM月DD日');
};
</script>

<template>
  <div class="page">
    <!-- 顶部导航 -->
    <div class="header">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">&lt;</span>
        返回
      </button>
      <div class="header-info">
        <span class="date">{{ formatDate(date) }}</span>
        <span class="weekday">{{ getWeekday(date) }}</span>
      </div>
      <div class="placeholder"></div>
    </div>

    <!-- 当日数据 -->
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="dayData" class="content">
      <!-- 主要信息卡片 -->
      <div class="main-card">
        <!-- 干支信息 -->
        <div class="ganzhi-row">
          <div class="ganzhi-item">
            <span class="label">干支</span>
            <span class="value ganzhi">{{ dayData.ganzhi }}</span>
          </div>
          <div class="ganzhi-item">
            <span class="label">五行</span>
            <span class="value wuxing">{{ dayData.wuxing }}</span>
          </div>
          <div class="ganzhi-item">
            <span class="label">星宿</span>
            <span class="value star">{{ dayData.star }}</span>
          </div>
        </div>

        <!-- 节日/节气/农历 -->
        <div class="holiday-row" :class="{ 'is-holiday': dayData.holiday, 'is-jieqi': dayData.jieqi && !dayData.holiday }">
          {{ dayData.holiday || dayData.jieqi || dayData.lunarDate }}
        </div>

        <!-- 天干地支详情 -->
        <div class="tiangan-row">
          <div class="tiangan-item">
            <span class="tg">{{ dayData.ganzhi[0] }}</span>
            <span class="ss">{{ dayData.shishen }}</span>
          </div>
          <div class="tiangan-item">
            <span class="tg">{{ dayData.ganzhi[1] }}</span>
            <span class="ss">{{ dayData.branchShishen }}</span>
          </div>
        </div>
      </div>

      <!-- 八字展示 -->
      <div v-if="userStore.bazi" class="bazi-card">
        <div class="card-title">您的八字</div>
        <div class="bazi-grid">
          <div class="bazi-item">
            <span class="bazi-label">年</span>
            <span class="bazi-value">{{ userStore.bazi.year.stem }}{{ userStore.bazi.year.branch }}</span>
          </div>
          <div class="bazi-item">
            <span class="bazi-label">月</span>
            <span class="bazi-value">{{ userStore.bazi.month.stem }}{{ userStore.bazi.month.branch }}</span>
          </div>
          <div class="bazi-item">
            <span class="bazi-label">日</span>
            <span class="bazi-value">{{ userStore.bazi.day.stem }}{{ userStore.bazi.day.branch }}</span>
          </div>
          <div class="bazi-item">
            <span class="bazi-label">时</span>
            <span class="bazi-value">{{ userStore.bazi.hour.stem }}{{ userStore.bazi.hour.branch }}</span>
          </div>
        </div>
      </div>

      <!-- 宜忌区域 -->
      <div class="yiji-section">
        <!-- 宜 -->
        <div class="yiji-card yi-card">
          <div class="card-header">
            <span class="icon">宜</span>
            <span class="title">宜做</span>
          </div>
          <div class="tags">
            <span v-for="item in dayData.yi" :key="item" class="tag yi-tag">{{ item }}</span>
          </div>
        </div>

        <!-- 忌 -->
        <div class="yiji-card ji-card">
          <div class="card-header">
            <span class="icon">忌</span>
            <span class="title">忌做</span>
          </div>
          <div class="tags">
            <span v-for="item in dayData.ji" :key="item" class="tag ji-tag">{{ item }}</span>
          </div>
        </div>
      </div>

      <!-- 五行分析 -->
      <div v-if="userStore.bazi" class="wuxing-card">
        <div class="card-title">五行分布</div>
        <div class="wuxing-bars">
          <div v-for="(value, key) in userStore.bazi.wuxing" :key="key" class="wuxing-row">
            <span class="wuxing-name">{{ key }}</span>
            <div class="bar-bg">
              <div class="bar-fill" :style="{ width: (value / 8 * 100) + '%' }"></div>
            </div>
            <span class="wuxing-count">{{ value }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #0f0f1a 100%);
  padding: 15px;
  color: #fff;
}

/* 顶部导航 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.back-icon {
  font-size: 16px;
}

.header-info {
  text-align: center;
}

.header-info .date {
  display: block;
  font-size: 18px;
  font-weight: bold;
}

.header-info .weekday {
  font-size: 12px;
  color: #888;
}

.placeholder {
  width: 80px;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #666;
}

/* 主要卡片 */
.main-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  padding: 25px;
  margin-bottom: 15px;
}

.ganzhi-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.ganzhi-item {
  text-align: center;
  padding: 15px 10px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
}

.ganzhi-item .label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.ganzhi-item .value {
  display: block;
  font-size: 20px;
  font-weight: bold;
}

.ganzhi-item .ganzhi {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.ganzhi-item .wuxing {
  color: #888;
}

.ganzhi-item .star {
  color: #888;
}

/* 节日/节气行 */
.holiday-row {
  text-align: center;
  font-size: 14px;
  color: #999;
  padding: 12px;
  margin-bottom: 15px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.03);
}

.holiday-row.is-holiday {
  color: #ff6b6b;
  font-weight: 600;
}

.holiday-row.is-jieqi {
  color: #4ecdc4;
  font-weight: 600;
}

/* 天干地支详情 */
.tiangan-row {
  display: flex;
  justify-content: center;
  gap: 40px;
}

.tiangan-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.tiangan-item .tg {
  font-size: 24px;
  font-weight: 600;
  color: #e8c97a;
}

.tiangan-item .ss {
  font-size: 12px;
  color: #888;
}

/* 八字卡片 */
.bazi-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 15px;
}

.card-title {
  font-size: 14px;
  color: #888;
  margin-bottom: 15px;
  text-align: center;
}

.bazi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.bazi-item {
  text-align: center;
  padding: 12px 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
}

.bazi-label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.bazi-value {
  display: block;
  font-size: 16px;
  font-weight: bold;
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 宜忌区域 */
.yiji-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 15px;
}

.yiji-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.card-header .icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 14px;
  font-weight: bold;
}

.yi-card .card-header .icon {
  background: rgba(246, 211, 101, 0.2);
  color: #f6d365;
}

.ji-card .card-header .icon {
  background: rgba(255, 100, 100, 0.2);
  color: #ff6464;
}

.card-header .title {
  font-size: 14px;
  color: #888;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 14px;
}

.yi-tag {
  background: rgba(246, 211, 101, 0.2);
  color: #f6d365;
}

.ji-tag {
  background: rgba(255, 100, 100, 0.2);
  color: #ff6464;
}

/* 五行卡片 */
.wuxing-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 20px;
}

.wuxing-bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.wuxing-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.wuxing-name {
  font-size: 14px;
  width: 20px;
  color: #888;
}

.bar-bg {
  flex: 1;
  height: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 5px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #f6d365 0%, #fda085 100%);
  border-radius: 5px;
}

.wuxing-count {
  font-size: 12px;
  color: #666;
  width: 16px;
  text-align: right;
}
</style>