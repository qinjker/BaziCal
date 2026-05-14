<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { SolarDay } from 'tyme4ts';

const router = useRouter();
const userStore = useUserStore();

// 表单数据
const name = ref('');
const birthday = ref('1990-01-15');
const hour = ref(12);
const minute = ref(0);
const gender = ref<'男' | '女'>('男');
const lunarDateDisplay = ref('');

// 阳历转阴历 (使用 tyme4ts)
const solarToLunar = (solarDate: string) => {
  const [year, month, day] = solarDate.split('-').map(Number);
  const solar = SolarDay.fromYmd(year, month, day);
  const lunar = solar.getLunarDay();
  return {
    year: lunar.getLunarMonth().getYear(),
    month: lunar.getLunarMonth().getMonth(),
    day: lunar.getDay()
  };
};

// 计算阴历日期
const updateLunarDate = () => {
  if (!birthday.value) {
    lunarDateDisplay.value = '';
    return;
  }

  try {
    const lunar = solarToLunar(birthday.value);
    lunarDateDisplay.value = `${lunar.year}年${lunar.month}月${lunar.day}日`;
  } catch (err) {
    console.error('Failed to convert solar to lunar:', err);
    lunarDateDisplay.value = '转换失败';
  }
};

// 初始化时计算一次
updateLunarDate();

// 监听 birthday 变化
watch(birthday, updateLunarDate);

// 提交
const handleSubmit = async () => {
  if (!name.value || !birthday.value) {
    alert('请填写完整信息');
    return;
  }

  try {
    const lunar = solarToLunar(birthday.value);
    const lunarBirthday = `${lunar.year}-${String(lunar.month).padStart(2, '0')}-${String(lunar.day).padStart(2, '0')}`;

    await userStore.calculate(name.value, lunarBirthday, hour.value, minute.value, gender.value);

    if (userStore.error) {
      alert(userStore.error);
    } else {
      router.push('/calendar');
    }
  } catch (err) {
    alert('阴历转换失败');
    console.error('Submit error:', err);
  }
};

// 跳转到日历页面
const goToCalendar = () => {
  router.push('/calendar');
};
</script>

<template>
  <div class="page">
    <div class="header">
      <h1>八字历</h1>
      <p class="slogan">每一天，都算数</p>
    </div>

    <div class="form-card">
      <h2>输入您的出生信息</h2>

      <div class="form-group">
        <label>姓名</label>
        <input v-model="name" type="text" placeholder="请输入姓名" />
      </div>

      <div class="form-group">
        <label>出生日期（阳历）</label>
        <input v-model="birthday" type="date" />
        <span class="hint">阳历：也称公历，即通常使用的日历日期（如 1990-01-15）</span>
      </div>

      <div class="form-group">
        <label>出生日期（阴历）</label>
        <div class="lunar-display">{{ lunarDateDisplay || '选择阳历日期后自动计算' }}</div>
        <span class="hint">阴历：也称农历/月历，基于月亮运行周期（八字计算使用阴历）</span>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>出生时辰</label>
          <select v-model="hour">
            <option v-for="h in 24" :key="h-1" :value="h-1">
              {{ (h-1).toString().padStart(2, '0') }}时
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>分钟</label>
          <select v-model="minute">
            <option v-for="m in 60" :key="m-1" :value="m-1">
              {{ (m-1).toString().padStart(2, '0') }}分
            </option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label>性别</label>
        <div class="gender-select">
          <button
            :class="{ active: gender === '男' }"
            @click="gender = '男'"
          >
            男
          </button>
          <button
            :class="{ active: gender === '女' }"
            @click="gender = '女'"
          >
            女
          </button>
        </div>
      </div>

      <button
        class="submit-btn"
        :disabled="userStore.loading"
        @click="handleSubmit"
      >
        {{ userStore.loading ? '计算中...' : '查看我的八字日历' }}
      </button>

      <button
        v-if="userStore.user"
        class="secondary-btn"
        @click="goToCalendar"
      >
        查看已保存的八字日历
      </button>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 40px 20px;
  color: #fff;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 48px;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.slogan {
  color: #888;
  font-size: 16px;
}

.form-card {
  max-width: 400px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 30px;
  backdrop-filter: blur(10px);
}

.form-card h2 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #aaa;
  font-size: 14px;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 16px;
  box-sizing: border-box;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
}

.lunar-display {
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.05);
  color: #f6d365;
  font-size: 16px;
  text-align: center;
}

.hint {
  display: block;
  margin-top: 6px;
  color: #666;
  font-size: 12px;
}

.gender-select {
  display: flex;
  gap: 10px;
}

.gender-select button {
  flex: 1;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.gender-select button.active {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  color: #1a1a2e;
  border-color: transparent;
}

.submit-btn {
  width: 100%;
  padding: 15px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  color: #1a1a2e;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 20px;
}

.submit-btn:disabled {
  opacity: 0.6;
}

.secondary-btn {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  background: transparent;
  color: #aaa;
  font-size: 14px;
  cursor: pointer;
  margin-top: 15px;
}
</style>