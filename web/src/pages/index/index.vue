<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

// 年份选项
const currentYear = new Date().getFullYear();
const years = computed(() => {
  const arr = [];
  for (let y = currentYear; y >= 1900; y--) {
    arr.push(y);
  }
  return arr;
});

// 月份选项
const months = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'));

// 日期选项
const days = Array.from({ length: 31 }, (_, i) => String(i + 1).padStart(2, '0'));

// 当前选中的值
const selectedYear = ref('1990');
const selectedMonth = ref('01');
const selectedDay = ref('15');
const selectedTime = ref(''); // 空字符串表示未知

// 生日类型
const birthdayType = ref<'solar' | 'lunar'>('solar');

// 显示哪个提示
const showSolarTip = ref(true);

// 时辰选项
const timeOptions = [
  { label: '未知', value: '' },
  { label: '子时', value: '子时' },
  { label: '丑时', value: '丑时' },
  { label: '寅时', value: '寅时' },
  { label: '卯时', value: '卯时' },
  { label: '辰时', value: '辰时' },
  { label: '巳时', value: '巳时' },
  { label: '午时', value: '午时' },
  { label: '未时', value: '未时' },
  { label: '申时', value: '申时' },
  { label: '酉时', value: '酉时' },
  { label: '戌时', value: '戌时' },
  { label: '亥时', value: '亥时' },
];

// 时辰映射到小时
const timeToHour: Record<string, number> = {
  '': 0,
  '子时': 23,
  '丑时': 1,
  '寅时': 3,
  '卯时': 5,
  '辰时': 7,
  '巳时': 9,
  '午时': 11,
  '未时': 13,
  '申时': 15,
  '酉时': 17,
  '戌时': 19,
  '亥时': 21,
};

// 选择时辰
const selectTime = (timeLabel: string) => {
  selectedTime.value = timeLabel;
};

// 选择生日类型
const selectCalendarType = (type: 'solar' | 'lunar') => {
  birthdayType.value = type;
  showSolarTip.value = type === 'solar';
};

// 提交
const handleSubmit = async () => {
  const birthday = `${selectedYear.value}-${selectedMonth.value}-${selectedDay.value}`;
  const hour = timeToHour[selectedTime.value] || 0;

  userStore.loading = true;

  try {
    // 模拟加载
    await new Promise(resolve => setTimeout(resolve, 600));

    await userStore.calculate(
      '用户', // 默认名字
      birthday,
      birthdayType.value,
      hour,
      0,
      '男' // 默认性别
    );

    if (userStore.error) {
      alert(userStore.error);
    } else {
      router.push('/calendar');
    }
  } catch (err) {
    alert('提交失败');
    console.error('Submit error:', err);
  } finally {
    userStore.loading = false;
  }
};
</script>

<template>
  <div class="phone-container">
    <!-- 进度条 (隐藏) -->
    <div class="progress-bar" style="display: none;">
      <div class="progress-dot active"></div>
      <div class="progress-dot"></div>
    </div>

    <!-- 导航栏 -->
    <div class="navbar">
      <a href="#/" class="nav-back">←</a>
      <span class="nav-title">输入生日</span>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">你的生日</h1>
      <p class="page-desc">我们将基于此为你生成专属能量日历</p>
    </div>

    <!-- 表单卡片 -->
    <div class="form-card">
      <div class="input-label">
        <span class="label-text">出生年月日</span>
      </div>

      <!-- 日期选择器 -->
      <div class="date-pickers">
        <div class="picker">
          <select v-model="selectedYear">
            <option v-for="year in years" :key="year" :value="String(year)">
              {{ year }}
            </option>
          </select>
        </div>
        <div class="picker">
          <select v-model="selectedMonth">
            <option v-for="month in months" :key="month" :value="month">
              {{ month }}
            </option>
          </select>
        </div>
        <div class="picker">
          <select v-model="selectedDay">
            <option v-for="day in days" :key="day" :value="day">
              {{ day }}
            </option>
          </select>
        </div>
      </div>

      <!-- 时辰选择 -->
      <div class="time-section">
        <div class="time-label">
          <span class="label-text">出生时辰（选填）</span>
          <span class="label-hint">选填可让结果更精准</span>
        </div>
        <div class="time-tags">
          <div
            v-for="time in timeOptions"
            :key="time.value"
            class="time-tag"
            :class="{ selected: selectedTime === time.value }"
            @click="selectTime(time.value)"
          >
            {{ time.label }}
          </div>
        </div>
      </div>

      <!-- 阴历阳历选择 -->
      <div class="calendar-type-section">
        <div class="calendar-type-label">
          <span class="label-text">生日类型</span>
        </div>
        <div class="calendar-type-tags">
          <div
            class="calendar-type-tag"
            :class="{ selected: birthdayType === 'solar' }"
            @click="selectCalendarType('solar')"
          >
            <span class="type-name">阳历</span>
            <span class="type-hint">公历生日</span>
          </div>
          <div
            class="calendar-type-tag"
            :class="{ selected: birthdayType === 'lunar' }"
            @click="selectCalendarType('lunar')"
          >
            <span class="type-name">阴历</span>
            <span class="type-hint">农历生日</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 什么是阳历提示 -->
    <div class="what-is-solar-box" v-if="showSolarTip">
      <span class="tip-icon">📖</span>
      <div>
        <div class="tip-title">什么是阳历生日？</div>
        <div class="tip-content">阳历也叫公历，是国际通用的日历。比如1月1日、10月1日就是阳历日期。大部分身份证上的生日都是阳历。</div>
      </div>
    </div>

    <!-- 什么是阴历提示 -->
    <div class="what-is-solar-box" v-if="!showSolarTip">
      <span class="tip-icon">📖</span>
      <div>
        <div class="tip-title">什么是阴历生日？</div>
        <div class="tip-content">阴历也叫农历，是中国的传统历法。比如春节是正月初一、端午是五月初五、中秋是八月十五。有些地区习惯过农历生日。</div>
      </div>
    </div>

    <!-- 提示 -->
    <div class="tip-box">
      <span class="tip-icon">💡</span>
      <span class="tip-text">不填时辰也没关系，我们依然能为你生成专属日历</span>
    </div>

    <!-- 底部按钮 -->
    <div class="bottom-area">
      <button
        class="btn-submit"
        :class="{ loading: userStore.loading }"
        @click="handleSubmit"
      >
        <span class="btn-text">开启我的日历</span>
        <span class="btn-loading"></span>
      </button>
      <p class="btn-hint">点击即表示你同意我们的服务条款</p>
    </div>

    <!-- 成功提示 -->
    <div class="success-toast" :class="{ show: userStore.loading }">✓ 生成成功</div>

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
      <a href="#/index" class="tab-item active">
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

/* 顶部进度条 */
.progress-bar {
  display: flex;
  gap: 8px;
  padding: 20px 24px 0;
}

.progress-dot {
  width: 40px;
  height: 4px;
  border-radius: 2px;
  background: #E8E0D5;
}

.progress-dot.active {
  background: #C84A3E;
}

/* 导航栏 */
.navbar {
  display: flex;
  align-items: center;
  padding: 20px 20px 24px;
}

.nav-back {
  width: 44px;
  height: 44px;
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
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 600;
  color: #2C1810;
  padding-right: 44px;
}

/* 页面标题 */
.page-header {
  padding: 0 24px 32px;
  text-align: center;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 26px;
  color: #2C1810;
  margin-bottom: 10px;
}

.page-desc {
  font-size: 14px;
  color: #8B7355;
}

/* 表单卡片 */
.form-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  margin: 0 24px;
  box-shadow: 0 4px 20px rgba(44, 24, 16, 0.06);
}

/* 输入标签 */
.input-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.label-text {
  font-size: 15px;
  color: #2C1810;
  font-weight: 500;
}

.label-hint {
  font-size: 12px;
  color: #B8A892;
}

/* 日期选择器 */
.date-pickers {
  display: flex;
  gap: 10px;
}

.picker {
  flex: 1;
  height: 54px;
  background: #FAF6F0;
  border: 1.5px solid #E8E0D5;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.2s;
}

.picker:focus-within {
  border-color: #C84A3E;
  background: white;
}

.picker select {
  width: 100%;
  height: 100%;
  border: none;
  background: transparent;
  font-size: 16px;
  color: #2C1810;
  cursor: pointer;
  text-align: center;
  text-align-last: center;
  font-family: inherit;
  -webkit-appearance: none;
}

/* 时辰选择 */
.time-section {
  margin-top: 24px;
}

.time-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.time-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.time-tag {
  padding: 10px 14px;
  background: #FAF6F0;
  border: 1.5px solid #E8E0D5;
  border-radius: 10px;
  font-size: 14px;
  color: #5A4A3A;
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
}

.time-tag:active {
  transform: scale(0.95);
}

.time-tag.selected {
  background: #C84A3E;
  color: white;
  border-color: #C84A3E;
}

/* 阴历阳历选择 */
.calendar-type-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #E8E0D5;
}

.calendar-type-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.calendar-type-tags {
  display: flex;
  gap: 10px;
}

.calendar-type-tag {
  flex: 1;
  height: 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #FAF6F0;
  border: 1.5px solid #E8E0D5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
}

.calendar-type-tag:active {
  transform: scale(0.98);
}

.calendar-type-tag.selected {
  background: #FFF5F4;
  border-color: #C84A3E;
}

.calendar-type-tag .type-name {
  font-size: 15px;
  font-weight: 500;
  color: #2C1810;
}

.calendar-type-tag.selected .type-name {
  color: #C84A3E;
}

.calendar-type-tag .type-hint {
  font-size: 11px;
  color: #B8A892;
  margin-top: 2px;
}

.calendar-type-tag.selected .type-hint {
  color: #C84A3E;
  opacity: 0.7;
}

/* 什么是阳历提示 */
.what-is-solar-box {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #F0F7FF;
  border-radius: 12px;
  padding: 14px 16px;
  margin: 16px 24px 0;
}

.what-is-solar-box .tip-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.what-is-solar-box .tip-title {
  font-size: 13px;
  font-weight: 500;
  color: #2C1810;
  margin-bottom: 4px;
}

.what-is-solar-box .tip-content {
  font-size: 12px;
  color: #5A6A7A;
  line-height: 1.5;
}

/* 提示文字 */
.tip-box {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #FFF9F5;
  border-radius: 12px;
  padding: 14px 16px;
  margin: 20px 24px 0;
}

.tip-box .tip-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.tip-box .tip-text {
  font-size: 13px;
  color: #8B7355;
  line-height: 1.5;
}

/* 底部按钮 */
.bottom-area {
  padding: 32px 24px 40px;
}

.btn-submit {
  display: block;
  width: 100%;
  height: 56px;
  background: linear-gradient(145deg, #C84A3E 0%, #A33D33 100%);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 28px rgba(200, 74, 62, 0.3);
  transition: all 0.2s;
}

.btn-submit:active {
  transform: scale(0.98);
}

.btn-submit.loading {
  pointer-events: none;
  opacity: 0.9;
}

.btn-submit.loading .btn-text {
  display: none;
}

.btn-submit.loading .btn-loading {
  display: block;
}

.btn-loading {
  display: none;
  text-align: center;
}

.btn-loading::after {
  content: '';
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.btn-hint {
  text-align: center;
  font-size: 12px;
  color: #B8A892;
  margin-top: 14px;
}

/* 成功提示 */
.success-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0.8);
  background: rgba(44, 24, 16, 0.9);
  color: white;
  padding: 16px 32px;
  border-radius: 16px;
  font-size: 15px;
  font-weight: 500;
  opacity: 0;
  pointer-events: none;
  transition: all 0.3s ease;
  z-index: 200;
}

.success-toast.show {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1);
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
</style>