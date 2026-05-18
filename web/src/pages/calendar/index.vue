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
    router.push('/index');
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

// 上个月的天数
const daysInPrevMonth = computed(() => {
  return dayjs(`${currentYear.value}-${currentMonth.value}-01`).subtract(1, 'month').daysInMonth();
});

// 判断日期是否今天
const isToday = (date: string) => {
  return date === dayjs().format('YYYY-MM-DD');
};

// 判断是否是周末
const isWeekend = (index: number) => {
  return index === 0 || index === 6;
};

// 查看每日详情
const viewDaily = (day: DailyData) => {
  router.push(`/daily?date=${day.date}`);
};

// 跳转到今日详情
const goToDaily = () => {
  router.push(`/daily?date=${dayjs().format('YYYY-MM-DD')}`);
};

// 天干颜色
const stemColors: Record<string, string> = {
  '甲': 'green', '乙': 'light-green',
  '丙': 'red', '丁': 'light-red',
  '戊': 'purple', '己': 'light-purple',
  '庚': 'gold', '辛': 'light-gold',
  '壬': 'blue', '癸': 'light-blue',
};

// 获取天干颜色类
const getStemColorClass = (stem: string) => {
  return stemColors[stem] || '';
};

// 地支颜色 (内联样式)
const branchColors: Record<string, string> = {
  '子': '#60a5fa', '丑': '#a78bfa', '寅': '#4ade80', '卯': '#86efac',
  '辰': '#a78bfa', '巳': '#f87171', '午': '#fca5a5', '未': '#a78bfa',
  '申': '#fbbf24', '酉': '#fde047', '戌': '#a78bfa', '亥': '#60a5fa',
};

const getBranchColor = (branch: string) => {
  return branchColors[branch] || '#2C1810';
};
</script>

<template>
  <div class="phone-container">
    <!-- 顶部日期切换 -->
    <div class="date-header">
      <div class="date-nav-btn" @click="changeMonth(-1)">‹ 上一月</div>
      <div class="date-display">
        <div class="date-display-main">{{ currentYear }}年{{ currentMonth }}月</div>
        <div class="date-display-sub">癸巳月 · 丙午马年</div>
      </div>
      <div class="date-nav-btn" @click="changeMonth(1)">下一月 ›</div>
    </div>

    <!-- 用户信息卡 -->
    <div class="user-card">
      <div class="user-info-row">
        <div class="user-info-left">
          <div class="user-avatar">十</div>
          <div class="user-info-text">
            <div class="user-name">日主：戊土</div>
            <div class="user-desc">生于{{ userStore.user?.birthday }}</div>
          </div>
        </div>
        <div class="user-info-right">
          <div class="user-energy">{{ userStore.bazi?.shishen.month }}</div>
          <div class="user-energy-label">本月能量</div>
        </div>
      </div>
    </div>

    <!-- 日历网格 -->
    <div class="calendar-section">
      <!-- 星期标题 -->
      <div class="calendar-header-row">
        <div v-for="day in weekDays" :key="day" class="calendar-weekday" :class="{ weekend: day === '日' || day === '六' }">
          {{ day }}
        </div>
      </div>

      <!-- 日期格子 -->
      <div class="calendar-grid">
        <!-- 上个月的数据 -->
        <div
          v-for="i in firstDayOfMonth"
          :key="'prev-' + i"
          class="calendar-day other-month"
        >
          <span class="day-number">{{ daysInPrevMonth - firstDayOfMonth + i }}</span>
        </div>

        <!-- 当月日期 -->
        <div
          v-for="day in daysInMonth"
          :key="'day-' + day"
          class="calendar-day"
          :class="{
            today: isToday(`${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`),
            weekend: isWeekend((firstDayOfMonth + day - 1) % 7)
          }"
          @click="viewDaily(days[day - 1] || { date: `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}` } as DailyData)"
        >
          <span class="day-number">{{ day }}</span>
          <span class="day-lunar" :class="{ 'is-holiday': days[day - 1]?.holiday, 'is-jieqi': days[day - 1]?.jieqi && !days[day - 1]?.holiday }">
            {{ days[day - 1]?.holiday || days[day - 1]?.jieqi || days[day - 1]?.lunarDate || '' }}
          </span>
          <div class="day-row">
            <span class="stem" :class="getStemColorClass(days[day - 1]?.ganzhi?.[0] || '')">
              {{ days[day - 1]?.ganzhi?.[0] || '' }}
            </span>
            <span class="shisen">{{ days[day - 1]?.shishen || '' }}</span>
          </div>
          <div class="day-row">
            <span class="branch" :style="{ color: getBranchColor(days[day - 1]?.ganzhi?.[1] || '') }">
              {{ days[day - 1]?.ganzhi?.[1] || '' }}
            </span>
            <span class="shisen">{{ days[day - 1]?.branchShishen || '' }}</span>
          </div>
        </div>

        <!-- 下个月的数据 -->
        <div
          v-for="i in (42 - firstDayOfMonth - daysInMonth)"
          :key="'next-' + i"
          class="calendar-day other-month"
        >
          <span class="day-number">{{ i }}</span>
        </div>
      </div>
    </div>

    <!-- 底部Tab栏 -->
    <div class="tab-bar">
      <a href="#/calendar" class="tab-item active">
        <span class="tab-icon">📅</span>
        <span class="tab-label">月历</span>
      </a>
      <a href="#/daily" class="tab-item" @click.prevent="goToDaily">
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
  padding-bottom: 90px;
  color: #2C1810;
}

/* 顶部日期切换 */
.date-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid rgba(232, 224, 213, 0.5);
}

.date-nav-btn {
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #8B7355;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.date-display {
  text-align: center;
}

.date-display-main {
  font-size: 18px;
  color: #2C1810;
  font-weight: 600;
}

.date-display-sub {
  font-size: 13px;
  color: #8B7355;
  margin-top: 2px;
}

/* 用户信息卡 */
.user-card {
  margin: 16px 20px;
  background: white;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(44, 24, 16, 0.06);
}

.user-info-row {
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
  width: 48px;
  height: 48px;
  background: linear-gradient(145deg, #C84A3E 0%, #A33D33 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.user-info-text {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 16px;
  color: #2C1810;
  font-weight: 600;
}

.user-desc {
  font-size: 12px;
  color: #8B7355;
  margin-top: 2px;
}

.user-info-right {
  text-align: right;
}

.user-energy {
  font-size: 14px;
  color: #D4A843;
  font-weight: 600;
}

.user-energy-label {
  font-size: 11px;
  color: #B8A892;
}

/* 日历网格 */
.calendar-section {
  margin: 0 20px;
  background: white;
  border-radius: 20px;
  padding: 16px;
  box-shadow: 0 4px 16px rgba(44, 24, 16, 0.06);
}

.calendar-header-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(232, 224, 213, 0.5);
}

.calendar-weekday {
  text-align: center;
  font-size: 12px;
  color: #B8A892;
  padding: 6px 0;
}

.calendar-weekday.weekend {
  color: #C84A3E;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-day {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
  background: #FAFAF8;
  min-height: 75px;
  padding: 5px 3px;
}

.calendar-day:active {
  background: #F0EBE3;
}

.calendar-day.other-month {
  opacity: 0.25;
}

.calendar-day.today {
  background: linear-gradient(145deg, #C84A3E 0%, #A33D33 100%);
}

.calendar-day.today .day-number {
  color: white;
  font-weight: 600;
}

.calendar-day.today .day-lunar {
  color: rgba(255,255,255,0.7);
}

.calendar-day.today .stem,
.calendar-day.today .branch {
  color: white;
}

.calendar-day.today .shisen {
  color: rgba(255,255,255,0.7);
}

.calendar-day.weekend .day-number {
  color: #C84A3E;
}

.calendar-day.today.weekend .day-number {
  color: white;
}

.day-number {
  font-size: 15px;
  color: #2C1810;
  font-weight: 600;
  margin-bottom: 1px;
}

.day-lunar {
  font-size: 9px;
  color: #8B7355;
  margin-bottom: 3px;
  text-align: center;
  min-height: 11px;
}

.day-lunar.is-holiday {
  color: #E74C3C;
  font-weight: 600;
}

.day-lunar.is-jieqi {
  color: #5A8A6A;
  font-weight: 600;
}

.day-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  width: 100%;
  line-height: 1.2;
}

.stem {
  font-size: 12px;
  font-weight: 600;
}

.stem.green { color: #4ade80; }
.stem.light-green { color: #86efac; }
.stem.red { color: #f87171; }
.stem.light-red { color: #fca5a5; }
.stem.purple { color: #a78bfa; }
.stem.light-purple { color: #c4b5fd; }
.stem.gold { color: #fbbf24; }
.stem.light-gold { color: #fde047; }
.stem.blue { color: #60a5fa; }
.stem.light-blue { color: #93c5fd; }

.branch {
  font-size: 12px;
  font-weight: 600;
  color: #2C1810;
}

.shisen {
  font-size: 9px;
  color: #8B7355;
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