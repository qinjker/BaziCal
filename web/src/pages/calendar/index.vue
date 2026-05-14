<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { apiService } from '@/api';
import dayjs from 'dayjs';
import type { DailyData } from '@/types';

const router = useRouter();
const userStore = useUserStore();

const currentYear = ref(dayjs().year());
const currentMonth = ref(dayjs().month() + 1);
const days = ref<DailyData[]>([]);
const loading = ref(false);

// 如果没有用户数据，跳转回首页
onMounted(() => {
  userStore.restore();
  if (!userStore.user) {
    router.push('/');
    return;
  }
  fetchCalendar();
});

// 获取日历数据
const fetchCalendar = async () => {
  if (!userStore.user) return;

  loading.value = true;
  try {
    const response = await apiService.getCalendar(
      userStore.user.userId,
      currentYear.value,
      currentMonth.value
    );
    if (response.code === 0 && response.data) {
      days.value = response.data.days;
    }
  } catch (err) {
    console.error('Fetch calendar error:', err);
  } finally {
    loading.value = false;
  }
};

// 月份变化
const changeMonth = (delta: number) => {
  const date = dayjs(`${currentYear.value}-${currentMonth.value}-01`);
  const newDate = date.add(delta, 'month');
  currentYear.value = newDate.year();
  currentMonth.value = newDate.month() + 1;
  fetchCalendar();
};

// 星期标题
const weekDays = ['日', '一', '二', '三', '四', '五', '六'];

// 计算本月第一天是星期几
const firstDayOfMonth = computed(() => {
  return dayjs(`${currentYear.value}-${currentMonth.value}-01`).day();
});

// 获取当月天数
const daysInMonth = computed(() => {
  return dayjs(`${currentYear.value}-${currentMonth.value}-01`).daysInMonth();
});

// 判断日期是否今天
const isToday = (date: string) => {
  return date === dayjs().format('YYYY-MM-DD');
};

// 获取今天的日期用于显示
const todayStr = dayjs().format('YYYY年MM月DD日');

// 查看每日详情
const viewDaily = (day: DailyData) => {
  router.push(`/daily?date=${day.date}`);
};

// 跳转到首页重新计算
const goToIndex = () => {
  router.push('/');
};

// 跳转到今日
const goToToday = () => {
  currentYear.value = dayjs().year();
  currentMonth.value = dayjs().month() + 1;
  fetchCalendar();
};

// 天干五行配色
const stemColors: Record<string, { color: string; gradient: string }> = {
  '甲': { color: '#4ade80', gradient: 'linear-gradient(135deg, #4ade80, #22c55e)' }, // 木 - 绿色
  '乙': { color: '#86efac', gradient: 'linear-gradient(135deg, #86efac, #4ade80)' }, // 木 - 浅绿
  '丙': { color: '#f87171', gradient: 'linear-gradient(135deg, #f87171, #ef4444)' }, // 火 - 红色
  '丁': { color: '#fca5a5', gradient: 'linear-gradient(135deg, #fca5a5, #f87171)' }, // 火 - 浅红
  '戊': { color: '#a78bfa', gradient: 'linear-gradient(135deg, #a78bfa, #8b5cf6)' }, // 土 - 紫色
  '己': { color: '#c4b5fd', gradient: 'linear-gradient(135deg, #c4b5fd, #a78bfa)' }, // 土 - 浅紫
  '庚': { color: '#fbbf24', gradient: 'linear-gradient(135deg, #fbbf24, #f59e0b)' }, // 金 - 金色
  '辛': { color: '#fde047', gradient: 'linear-gradient(135deg, #fde047, #fbbf24)' }, // 金 - 浅金
  '壬': { color: '#60a5fa', gradient: 'linear-gradient(135deg, #60a5fa, #3b82f6)' }, // 水 - 蓝色
  '癸': { color: '#93c5fd', gradient: 'linear-gradient(135deg, #93c5fd, #60a5fa)' }, // 水 - 浅蓝
};

// 获取天干颜色
const getStemColor = (stem: string) => {
  return stemColors[stem] || { color: '#e8c97a', gradient: 'linear-gradient(135deg, #e8c97a, #d4a853)' };
};

// 地支五行配色
const branchColors: Record<string, string> = {
  '子': '#60a5fa', // 水 - 蓝色
  '丑': '#a78bfa', // 土 - 紫色
  '寅': '#4ade80', // 木 - 绿色
  '卯': '#86efac', // 木 - 浅绿
  '辰': '#a78bfa', // 土 - 紫色
  '巳': '#f87171', // 火 - 红色
  '午': '#fca5a5', // 火 - 浅红
  '未': '#a78bfa', // 土 - 紫色
  '申': '#fbbf24', // 金 - 金色
  '酉': '#fde047', // 金 - 浅金
  '戌': '#a78bfa', // 土 - 紫色
  '亥': '#60a5fa', // 水 - 蓝色
};

// 获取地支颜色
const getBranchColor = (branch: string) => {
  return branchColors[branch] || '#e8c97a';
};
</script>

<template>
  <div class="page">
    <!-- 顶部导航 -->
    <div class="header">
      <button class="nav-btn" @click="goToIndex">首页</button>
      <button class="today-btn" @click="goToToday">今天</button>
      <div class="month-selector">
        <button class="change-btn" @click="changeMonth(-1)">&lt;</button>
        <span class="current-month">{{ currentYear }}年{{ currentMonth }}月</span>
        <button class="change-btn" @click="changeMonth(1)">&gt;</button>
      </div>
    </div>

    <!-- 八字展示区 -->
    <div v-if="userStore.bazi" class="bazi-section">
      <div class="section-title">今日八字</div>
      <div class="bazi-pillars">
        <div class="pillar-item">
          <span class="pillar-label">年柱</span>
          <span class="pillar-value">{{ userStore.bazi.year.stem }}{{ userStore.bazi.year.branch }}</span>
          <span class="shishen">{{ userStore.bazi.shishen.year }}</span>
        </div>
        <div class="pillar-item">
          <span class="pillar-label">月柱</span>
          <span class="pillar-value">{{ userStore.bazi.month.stem }}{{ userStore.bazi.month.branch }}</span>
          <span class="shishen">{{ userStore.bazi.shishen.month }}</span>
        </div>
        <div class="pillar-item">
          <span class="pillar-label">日柱</span>
          <span class="pillar-value">{{ userStore.bazi.day.stem }}{{ userStore.bazi.day.branch }}</span>
          <span class="shishen">{{ userStore.bazi.shishen.day }}</span>
        </div>
        <div class="pillar-item">
          <span class="pillar-label">时柱</span>
          <span class="pillar-value">{{ userStore.bazi.hour.stem }}{{ userStore.bazi.hour.branch }}</span>
          <span class="shishen">{{ userStore.bazi.shishen.hour }}</span>
        </div>
      </div>
    </div>

    <!-- 五行分布 -->
    <div v-if="userStore.bazi" class="wuxing-section">
      <div class="wuxing-bars">
        <div v-for="(value, key) in userStore.bazi.wuxing" :key="key" class="wuxing-item">
          <span class="wuxing-name">{{ key }}</span>
          <div class="wuxing-bar-bg">
            <div class="wuxing-bar-fill" :style="{ width: (value / 8 * 100) + '%' }"></div>
          </div>
          <span class="wuxing-count">{{ value }}</span>
        </div>
      </div>
    </div>

    <!-- 当日宜忌 -->
    <div v-if="days.find(d => isToday(d.date))" class="today-yiji">
      <div class="yi-section">
        <span class="label">宜</span>
        <span class="tags">
          <span v-for="item in days.find(d => isToday(d.date))?.yi.slice(0, 3)" :key="item" class="tag yi-tag">{{ item }}</span>
        </span>
      </div>
      <div class="ji-section">
        <span class="label">忌</span>
        <span class="tags">
          <span v-for="item in days.find(d => isToday(d.date))?.ji.slice(0, 2)" :key="item" class="tag ji-tag">{{ item }}</span>
        </span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 日历网格 -->
    <div v-else class="calendar-grid">
      <!-- 星期标题 -->
      <div class="week-header">
        <span v-for="day in weekDays" :key="day" :class="{ weekend: day === '日' || day === '六' }">
          {{ day }}
        </span>
      </div>

      <!-- 日期格子 -->
      <div class="days-container">
        <!-- 空白格子 -->
        <div v-for="i in firstDayOfMonth" :key="'empty-' + i" class="day-cell empty"></div>

        <!-- 日期格子 -->
        <div
          v-for="day in days"
          :key="day.date"
          class="day-cell"
          :class="{
            today: isToday(day.date)
          }"
          @click="viewDaily(day)"
        >
          <!-- 第一行：日期 (居中) -->
          <div class="day-date">{{ dayjs(day.date).date() }}</div>

          <!-- 第二行：节日/节气/农历 (居中) -->
          <div class="day-lunar" :class="{ 'is-holiday': day.holiday, 'is-jieqi': day.jieqi && !day.holiday }">
            {{ day.holiday || day.jieqi || day.lunarDate }}
          </div>

          <!-- 第三行：天干 + 十神 -->
          <div class="day-row">
            <span class="stem" :style="{ color: getStemColor(day.ganzhi[0]).color }">{{ day.ganzhi[0] }}</span>
            <span class="shisen">{{ day.shishen }}</span>
          </div>

          <!-- 第四行：地支 + 十神 -->
          <div class="day-row">
            <span class="branch" :style="{ color: getBranchColor(day.ganzhi[1]) }">{{ day.ganzhi[1] }}</span>
            <span class="shisen">{{ day.branchShishen }}</span>
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
  padding: 12px;
  color: #fff;
}

/* 响应式适配 */
@media (max-width: 400px) {
  .page {
    padding: 8px;
  }

  .days-container {
    gap: 4px;
  }

  .day-cell {
    min-height: 80px;
    padding: 4px 2px;
  }

  .day-date {
    font-size: 14px;
  }

  .day-lunar {
    font-size: 10px;
  }

  .stem, .branch {
    font-size: 14px;
  }

  .shisen {
    font-size: 10px;
  }
}

/* 顶部导航 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  margin-bottom: 15px;
}

.nav-btn, .today-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.today-btn {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  color: #1a1a2e;
}

.month-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.change-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.current-month {
  font-size: 18px;
  font-weight: bold;
  min-width: 120px;
  text-align: center;
}

/* 八字展示区 */
.bazi-section {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 15px;
}

.section-title {
  font-size: 14px;
  color: #888;
  margin-bottom: 15px;
  text-align: center;
}

.bazi-pillars {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.pillar-item {
  text-align: center;
  padding: 12px 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
}

.pillar-label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}

.pillar-value {
  display: block;
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.shishen {
  display: block;
  font-size: 12px;
  color: #f6d365;
  margin-top: 4px;
}

/* 五行分布 */
.wuxing-section {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 15px;
  margin-bottom: 15px;
}

.wuxing-bars {
  display: flex;
  gap: 10px;
  align-items: center;
}

.wuxing-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
}

.wuxing-name {
  font-size: 14px;
  width: 20px;
  color: #888;
}

.wuxing-bar-bg {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.wuxing-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #f6d365 0%, #fda085 100%);
  border-radius: 4px;
}

.wuxing-count {
  font-size: 12px;
  color: #666;
  width: 16px;
  text-align: right;
}

/* 今日宜忌 */
.today-yiji {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
}

.yi-section, .ji-section {
  flex: 1;
}

.yi-section .label, .ji-section .label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.yi-tag {
  background: rgba(246, 211, 101, 0.2);
  color: #f6d365;
}

.ji-tag {
  background: rgba(255, 100, 100, 0.2);
  color: #ff6464;
}

/* 加载状态 */
.loading {
  text-align: center;
  padding: 60px;
  color: #666;
}

/* 日历网格 */
.calendar-grid {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 15px;
}

.week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 10px;
}

.week-header span {
  font-size: 12px;
  color: #666;
}

.week-header span.weekend {
  color: #f6d365;
}

.days-container {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.day-cell {
  min-height: 90px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6px 3px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.day-cell:not(.empty):hover {
  background: rgba(255, 255, 255, 0.1);
}

.day-cell.empty {
  background: transparent;
  cursor: default;
}

.day-cell.today {
  background: rgba(246, 211, 101, 0.2);
  border: 2px solid #f6d365;
}

.day-cell.today .day-date {
  color: #f6d365;
  font-size: 18px;
}

.day-date {
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 3px;
}

.day-lunar {
  font-size: 9px;
  color: #999;
  text-align: center;
  margin-bottom: 4px;
  min-height: 12px;
}

.day-lunar.is-holiday {
  color: #ff6b6b;
  font-weight: 600;
}

.day-lunar.is-jieqi {
  color: #4ecdc4;
  font-weight: 600;
}

.day-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0 2px;
  line-height: 1.3;
}

.day-row.single {
  justify-content: center;
  gap: 8px;
}

.day-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stem {
  font-size: 14px;
  font-weight: 600;
}

.shisen {
  font-size: 10px;
  color: #888;
}
</style>