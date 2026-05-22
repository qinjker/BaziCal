/**
 * 八字计算服务
 * 使用 Tyme4TS 库进行八字排盘
 */

import {
  LunarHour,
  LunarDay,
  EightChar,
  SixtyCycle,
  Element,
  TenStar,
  HeavenStem,
  EarthBranch,
  LunarYear,
  LunarMonth,
  NineStar,
  Week,
  TwentyEightStar,
  SolarDay,
} from 'tyme4ts';

export interface BaziResult {
  year: { stem: string; branch: string };
  month: { stem: string; branch: string };
  day: { stem: string; branch: string };
  hour: { stem: string; branch: string };
  wuxing: Record<string, number>;
  shishen: Record<string, string>;
}

export interface DailyResult {
  date: string;
  ganzhi: string;
  wuxing: string;
  yi: string[];
  ji: string[];
  star: string;
  luck: string;
  shishen: string;
  jieqi: string;
  lunarDate: string;
  holiday: string;
  branchShishen: string;
}

/**
 * 根据出生时间计算八字
 * @param birthday 出生日期 (格式: YYYY-MM-DD)
 * @param hour 小时 (0-23)
 * @param minute 分钟 (0-59)
 * @param birthdayType 生日类型：solar=阳历，lunar=阴历（默认阴历）
 */
export const calculateBazi = (
  birthday: string,
  hour: number,
  minute: number = 0,
  birthdayType: 'solar' | 'lunar' = 'lunar'
): BaziResult => {
  let lunarBirthday = birthday;

  // 如果是阳历，先转换为阴历
  if (birthdayType === 'solar') {
    const lunar = solarToLunar(birthday);
    lunarBirthday = `${lunar.year}-${String(lunar.month).padStart(2, '0')}-${String(lunar.day).padStart(2, '0')}`;
  }

  // 解析阴历生日 (格式: YYYY-MM-DD)
  const [year, month, day] = lunarBirthday.split('-').map(Number);

  // 验证并调整日期（某些农历月份可能没有31天）
  const lunarMonth = LunarMonth.fromYm(year, month);
  const maxDayInMonth = lunarMonth.getDayCount();
  const validDay = Math.min(day, maxDayInMonth);

  // 使用 LunarDay 创建阴历日期
  const lunarDay = LunarDay.fromYmd(year, month, validDay);

  // 使用阴历年月日创建 LunarHour (注意：fromYmdHms 接收的是阴历日期)
  const lunarYear = lunarDay.getLunarMonth().getYear();
  const lunarMonthNum = lunarDay.getLunarMonth().getMonth();
  const lunarDayNum = lunarDay.getDay();

  const lunarHour = LunarHour.fromYmdHms(
    lunarYear,
    lunarMonthNum,
    lunarDayNum,
    hour,
    minute,
    0
  );
  const eightChar = lunarHour.getEightChar();

  // 获取四柱
  const yearPillar = eightChar.getYear();
  const monthPillar = eightChar.getMonth();
  const dayPillar = eightChar.getDay();
  const hourPillar = eightChar.getHour();

  // 计算五行
  const wuxing = calculateWuxing(eightChar);

  // 计算十神
  const shishen = calculateShishen(eightChar);

  return {
    year: {
      stem: yearPillar.getHeavenStem().getName(),
      branch: yearPillar.getEarthBranch().getName(),
    },
    month: {
      stem: monthPillar.getHeavenStem().getName(),
      branch: monthPillar.getEarthBranch().getName(),
    },
    day: {
      stem: dayPillar.getHeavenStem().getName(),
      branch: dayPillar.getEarthBranch().getName(),
    },
    hour: {
      stem: hourPillar.getHeavenStem().getName(),
      branch: hourPillar.getEarthBranch().getName(),
    },
    wuxing,
    shishen,
  };
};

/**
 * 阳历转阴历
 * @param solarDate 阳历日期 (格式: YYYY-MM-DD)
 */
export const solarToLunar = (solarDate: string): { year: number; month: number; day: number } => {
  const [year, month, day] = solarDate.split('-').map(Number);
  const solarDay = SolarDay.fromYmd(year, month, day);
  const lunarDay = solarDay.getLunarDay();

  return {
    year: lunarDay.getLunarMonth().getYear(),
    month: lunarDay.getLunarMonth().getMonth(),
    day: lunarDay.getDay(),
  };
};

/**
 * 计算五行分布
 */
const calculateWuxing = (eightChar: EightChar): Record<string, number> => {
  const elements: Record<string, number> = {
    木: 0,
    火: 0,
    土: 0,
    金: 0,
    水: 0,
  };

  // 统计四柱天干的五行
  const stems = [
    eightChar.getYear().getHeavenStem(),
    eightChar.getMonth().getHeavenStem(),
    eightChar.getDay().getHeavenStem(),
    eightChar.getHour().getHeavenStem(),
  ];

  stems.forEach((stem) => {
    const element = stem.getElement();
    elements[element.getName()]++;
  });

  // 统计四柱地支的五行
  const branches = [
    eightChar.getYear().getEarthBranch(),
    eightChar.getMonth().getEarthBranch(),
    eightChar.getDay().getEarthBranch(),
    eightChar.getHour().getEarthBranch(),
  ];

  branches.forEach((branch) => {
    const element = branch.getElement();
    elements[element.getName()]++;
  });

  return elements;
};

/**
 * 计算十神
 * 十神是通过日干与其他天干的五行关系得出
 */
const calculateShishen = (eightChar: EightChar): Record<string, string> => {
  const dayStem = eightChar.getDay().getHeavenStem();
  const dayElement = dayStem.getElement();

  // 获取四柱天干
  const yearStem = eightChar.getYear().getHeavenStem();
  const monthStem = eightChar.getMonth().getHeavenStem();
  const hourStem = eightChar.getHour().getHeavenStem();

  return {
    year: getTenStarName(dayElement, yearStem.getElement()),
    month: getTenStarName(dayElement, monthStem.getElement()),
    day: '日主',
    hour: getTenStarName(dayElement, hourStem.getElement()),
  };
};

/**
 * 根据日干天干索引和目标天干索引计算传统十神
 * 考虑五行生克关系 + 阴阳异性
 *
 * 十神规则口诀：
 * 关系    同性(阴阳相同)    异性(阴阳不同)
 * 克我者    七杀            正官
 * 生我者    偏印            正印
 * 同我者    比肩            劫财
 * 我生者    食神            伤官
 * 我克者    偏财            正财
 */
const getTraditionalTenStar = (dayStemIdx: number, targetStemIdx: number): string => {
  // 天干索引对应的五行索引: 0,1→木(0), 2,3→火(1), 4,5→土(2), 6,7→金(3), 8,9→水(4)
  const dayElementIdx = Math.floor(dayStemIdx / 2) % 5;
  const targetElementIdx = Math.floor(targetStemIdx / 2) % 5;

  // 计算五行生克关系 (循环)
  // 同我:0, 我生:1, 我克:2, 克我:3, 生我:4
  let relation = targetElementIdx - dayElementIdx;
  if (relation < 0) relation += 5;

  // 判断阴阳同性/异性
  // 天干索引: 0,2,4,6,8 为阳; 1,3,5,7,9 为阴
  const dayIsYang = dayStemIdx % 2 === 0;
  const targetIsYang = targetStemIdx % 2 === 0;
  const isSameYinYang = dayIsYang === targetIsYang;

  // 根据关系和阴阳得出十神
  // 同我者
  if (relation === 0) {
    return isSameYinYang ? '比肩' : '劫财';
  }
  // 我生者
  if (relation === 1) {
    return isSameYinYang ? '食神' : '伤官';
  }
  // 我克者
  if (relation === 2) {
    return isSameYinYang ? '偏财' : '正财';
  }
  // 克我者
  if (relation === 3) {
    return isSameYinYang ? '七杀' : '正官';
  }
  // 生我者
  if (relation === 4) {
    return isSameYinYang ? '偏印' : '正印';
  }

  return '比肩'; // 默认
};

/**
 * 根据日干和其他天干的五行关系获取十神名称 (用于四柱十神计算)
 */
const getTenStarName = (dayElement: Element, otherElement: Element): string => {
  const elements = ['木', '火', '土', '金', '水'];
  const dayIdx = elements.indexOf(dayElement.getName());
  const otherIdx = elements.indexOf(otherElement.getName());

  // 计算相差的索引 (循环)
  let diff = otherIdx - dayIdx;
  if (diff < 0) diff += 5;

  // 十神对应关系 (传统算法)
  // 同我者: 0=比肩, 1=劫财
  // 我生者: 2=食神, 3=伤官
  // 我克者: 4=偏财, 5=正财
  // 克我者: 6=七杀, 7=正官
  // 生我者: 8=偏印, 9=正印

  const tenStarNames = ['比肩', '劫财', '食神', '伤官', '偏财', '正财', '七杀', '正官', '偏印', '正印'];

  return tenStarNames[diff];
};

/**
 * 获取某月的日历数据
 */
export const getMonthlyCalendar = (
  userBazi: BaziResult,
  year: number,
  month: number
): DailyResult[] => {
  const days: DailyResult[] = [];
  const daysInMonth = new Date(year, month, 0).getDate();

  // 获取日干的索引 (用于计算十神)
  const dayStemNames = ['甲', '乙', '丙', '丁', '戊', '己', '庚', '辛', '壬', '癸'];
  const dayStemIndex = dayStemNames.indexOf(userBazi.day.stem);

  // 获取当年的节气数据
  const solarTerms = getYearSolarTerms(year);

  // 获取当年节日
  const holidays = getYearHolidays(year);

  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`;

    // 获取当天的干支 (使用 SolarDay 转农历)
    const solarDay = SolarDay.fromYmd(year, month, d);
    const lunarDay = solarDay.getLunarDay();

    // 使用用户日柱的天干（第一个字）作为干支显示
    const userDayStemStr = userBazi.day.stem;
    const userDayBranch = userBazi.day.branch;
    // 干支显示当天实际的，十神计算基于用户日干
    const todayGanzhi = lunarDay.getSixtyCycle().toString();
    const ganzhi = todayGanzhi;

    // 获取五行 (使用用户日柱地支的元素)
    const dayElement = userBazi.day.branch;

    // 获取宜忌 (简化版，实际需要更复杂的逻辑)
    const { yi, ji } = getYiji(ganzhi);

    // 获取星宿 (二十八星宿)
    const star = lunarDay.getTwentyEightStar().getName();

    // 获取吉凶 (基于九星)
    const nineStarIndex = lunarDay.getNineStar().getIndex();
    const luckyStars = [0, 5, 7, 8];
    const luck = luckyStars.includes(nineStarIndex) ? '吉' : '凶';

    // 获取当天的日干索引 (用于计算十神)
    const dayStem = lunarDay.getSixtyCycle().getHeavenStem();
    const dayStemIdx = dayStem.getIndex();
    const userDayStemIdx = dayStemNames.indexOf(userBazi.day.stem);

    // 计算天干十神 (使用传统算法)
    const shishen = getTraditionalTenStar(userDayStemIdx, dayStemIdx);

    // 获取地支的十神 (使用传统算法，根据地支藏干主气计算)
    const earthBranch = lunarDay.getSixtyCycle().getEarthBranch();
    const branchStem = earthBranch.getHideHeavenStemMain();
    const branchStemIdx = branchStem.getIndex();
    const branchShishen = getTraditionalTenStar(userDayStemIdx, branchStemIdx);

    // 获取节气
    const jieqi = getSolarTermName(solarTerms, year, month, d);

    // 获取农历日期
    const lunarDate = lunarDay.getName();

    // 获取节日
    const holiday = holidays.get(dateStr) || '';

    days.push({
      date: dateStr,
      ganzhi,
      wuxing: dayElement,
      yi,
      ji,
      star,
      luck,
      shishen,
      jieqi,
      lunarDate,
      holiday,
      branchShishen,
    });
  }

  return days;
};

/**
 * 计算日干的十神
 */
const calculateDayShishen = (dayStemIdx: number, userDayStemIdx: number): string => {
  const elements = ['木', '火', '土', '金', '水'];
  const dayStemElement = elements[Math.floor(dayStemIdx / 2) % 5];
  const userDayStemElement = elements[Math.floor(userDayStemIdx / 2) % 5];

  const dayIdx = elements.indexOf(dayStemElement);
  const userIdx = elements.indexOf(userDayStemElement);

  let diff = dayIdx - userIdx;
  if (diff < 0) diff += 5;

  // 十神对应关系
  // 同我者: 0=劫财, 1=比肩
  // 我克者: 2=偏财, 3=正财
  // 克我者: 4=七杀, 5=正官
  // 我生者: 6=食神, 7=伤官
  // 生我者: 8=偏印, 9=正印
  const tenStarNames = ['比肩', '劫财', '食神', '伤官', '偏财', '正财', '七杀', '正官', '偏印', '正印'];

  return tenStarNames[diff];
};

/**
 * 计算地支的十神 (根据地支藏干与日干的关系)
 */
const calculateBranchShishen = (branch: string, userDayBranch: string): string => {
  // 地支到五行的映射
  const branchToElement: Record<string, string> = {
    '子': '水', '丑': '土', '寅': '木', '卯': '木', '辰': '土',
    '巳': '火', '午': '火', '未': '土', '申': '金', '酉': '金',
    '戌': '土', '亥': '水'
  };

  const elements = ['木', '火', '土', '金', '水'];
  const branchElement = branchToElement[branch] || '土';
  const userDayBranchElement = branchToElement[userDayBranch] || '水';

  const branchIdx = elements.indexOf(branchElement);
  const userIdx = elements.indexOf(userDayBranchElement);

  let diff = branchIdx - userIdx;
  if (diff < 0) diff += 5;

  const tenStarNames = ['比肩', '劫财', '食神', '伤官', '偏财', '正财', '七杀', '正官', '偏印', '正印'];

  return tenStarNames[diff];
};

/**
 * 获取农历日期名称 (初一、初二等)
 */
const getLunarDateName = (month: number, day: number): string => {
  const dayNames = ['初一', '初二', '初三', '初四', '初五', '初六', '初七', '初八', '初九', '初十',
    '十一', '十二', '十三', '十四', '十五', '十六', '十七', '十八', '十九', '二十',
    '廿一', '廿二', '廿三', '廿四', '廿五', '廿六', '廿七', '廿八', '廿九', '三十'];
  return dayNames[day - 1] || '';
};

/**
 * 获取年份节日
 */
const getYearHolidays = (year: number): Map<string, string> => {
  const holidays = new Map<string, string>();
  const fixedHolidays: Record<string, string> = {
    '01-01': '元旦',
    '02-14': '情人节',
    '03-08': '妇女节',
    '03-12': '植树节',
    '04-01': '愚人节',
    '05-01': '劳动节',
    '05-04': '青年节',
    '06-01': '儿童节',
    '07-01': '建党节',
    '08-01': '建军节',
    '09-10': '教师节',
    '10-01': '国庆节',
    '12-25': '圣诞节',
  };

  for (const [date, name] of Object.entries(fixedHolidays)) {
    holidays.set(`${year}-${date}`, name);
  }

  const mothersDay = getMothersDay(year);
  if (mothersDay) holidays.set(mothersDay, '母亲节');

  return holidays;
};

/**
 * 计算母亲节日期 (5月第二个周日)
 */
const getMothersDay = (year: number): string | null => {
  const mayFirst = new Date(year, 4, 1);
  const dayOfWeek = mayFirst.getDay();
  const sunday = dayOfWeek === 0 ? 1 : 8 - dayOfWeek;
  return `${year}-05-${String(sunday + 7).padStart(2, '0')}`;
};

/**
 * 获取当年的节气数据
 */
const getYearSolarTerms = (year: number): Map<string, string> => {
  const terms = new Map<string, string>();
  // 节气日期 (月-日格式)
  const solarTermDates: Record<string, string[]> = {
    小寒: ['01', '06'],
    大寒: ['01', '20'],
    立春: ['02', '04'],
    雨水: ['02', '19'],
    惊蛰: ['03', '06'],
    春分: ['03', '21'],
    清明: ['04', '05'],
    谷雨: ['04', '20'],
    立夏: ['05', '06'],
    小满: ['05', '21'],
    芒种: ['06', '06'],
    夏至: ['06', '21'],
    小暑: ['07', '07'],
    大暑: ['07', '23'],
    立秋: ['08', '08'],
    处暑: ['08', '23'],
    白露: ['09', '08'],
    秋分: ['09', '23'],
    寒露: ['10', '08'],
    霜降: ['10', '24'],
    立冬: ['11', '07'],
    小雪: ['11', '22'],
    大雪: ['12', '07'],
    冬至: ['12', '22'],
  };

  for (const [jieqi, dates] of Object.entries(solarTermDates)) {
    // 节气可能出现在两个日期，取距指定日期最近的
    for (const date of dates) {
      const key = `${year}-${date}`;
      terms.set(key, jieqi);
    }
  }

  return terms;
};

/**
 * 获取指定日期的节气名称
 */
const getSolarTermName = (solarTerms: Map<string, string>, year: number, month: number, day: number): string => {
  const key = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  return solarTerms.get(key) || '';
};

/**
 * 获取宜忌 (简化版)
 */
const getYiji = (ganzhi: string): { yi: string[]; ji: string[] } => {
  // 简化逻辑，实际需要基于建除十二神等综合判断
  const yiList = ['祭祀', '开业', '出行', '搬家', '结婚', '动土'];
  const jiList = ['动土', '破屋', '安葬', '行丧'];

  return {
    yi: yiList.slice(0, Math.floor(Math.random() * 3) + 2),
    ji: jiList.slice(0, Math.floor(Math.random() * 2) + 1),
  };
};

/**
 * 获取星宿 (二十八星宿)
 */
const getStar = (day: number): string => {
  const stars = [
    '角宿', '亢宿', '氐宿', '房宿', '心宿', '尾宿', '箕宿',
    '斗宿', '牛宿', '女宿', '虚宿', '危宿', '室宿', '壁宿',
    '奎宿', '娄宿', '胃宿', '昴宿', '毕宿', '觜宿', '参宿',
    '井宿', '鬼宿', '柳宿', '星宿', '张宿', '翼宿', '轸宿',
  ];
  return stars[(day - 1) % 28];
};

/**
 * 获取指定日期的详细信息
 */
export const getDailyDetail = (
  userBazi: BaziResult,
  date: string
): {
  date: string;
  ganzhi: string;
  shishen: string;
  branchShishen: string;
  energy: { level: number; description: string };
  lucky: { direction: string; time: string; color: string; number: number };
  messages: string[];
  tags: string[];
} => {
  const [year, month, day] = date.split('-').map(Number);

  // 获取当天的干支
  const solarDay = SolarDay.fromYmd(year, month, day);
  const lunarDay = solarDay.getLunarDay();
  const todayGanzhi = lunarDay.getSixtyCycle().toString();

  // 获取当天的日干索引 (用于计算十神)
  const dayStemNames = ['甲', '乙', '丙', '丁', '戊', '己', '庚', '辛', '壬', '癸'];
  const dayStem = lunarDay.getSixtyCycle().getHeavenStem();
  const dayStemIdx = dayStem.getIndex();
  const userDayStemIdx = dayStemNames.indexOf(userBazi.day.stem);

  // 计算天干十神
  const shishen = getTraditionalTenStar(userDayStemIdx, dayStemIdx);

  // 获取地支的十神
  const earthBranch = lunarDay.getSixtyCycle().getEarthBranch();
  const branchStem = earthBranch.getHideHeavenStemMain();
  const branchStemIdx = branchStem.getIndex();
  const branchShishen = getTraditionalTenStar(userDayStemIdx, branchStemIdx);

  // 计算能量等级 (简化版)
  const nineStarIndex = lunarDay.getNineStar().getIndex();
  const energyLevel = [0, 5, 7, 8].includes(nineStarIndex) ? 4 : 3;

  // 获取宜忌
  const { yi, ji } = getYiji(todayGanzhi);

  // 生成寄语
  const messages = generateDailyMessages(shishen, energyLevel);

  // 生成标签
  const tags = generateTags(shishen, branchShishen, energyLevel);

  return {
    date,
    ganzhi: todayGanzhi,
    shishen,
    branchShishen,
    energy: {
      level: energyLevel,
      description: getEnergyDescription(shishen, energyLevel),
    },
    lucky: {
      direction: getLuckyDirection(dayStemIdx),
      time: getLuckyTime(dayStemIdx),
      color: getLuckyColor(dayStemIdx),
      number: getLuckyNumber(dayStemIdx),
    },
    messages,
    tags,
  };
};

/**
 * 获取能量描述
 */
const getEnergyDescription = (shishen: string, level: number): string => {
  if (level >= 4) {
    return '贵人运不错，适合推进项目';
  }
  return '今日宜稳中求进，保守行事';
};

/**
 * 获取幸运方向
 */
const getLuckyDirection = (dayStemIdx: number): string => {
  const directions = ['东', '南', '南', '西', '西', '北', '北', '东', '东', '南'];
  return directions[dayStemIdx % 10];
};

/**
 * 获取幸运时间
 */
const getLuckyTime = (dayStemIdx: number): string => {
  const times = [
    '卯时(5-7点)',
    '辰时(7-9点)',
    '巳时(9-11点)',
    '午时(11-13点)',
    '未时(13-15点)',
    '申时(15-17点)',
    '酉时(17-19点)',
    '戌时(19-21点)',
    '亥时(21-23点)',
    '子时(23-1点)',
  ];
  return times[dayStemIdx % 10];
};

/**
 * 获取幸运颜色
 */
const getLuckyColor = (dayStemIdx: number): string => {
  const colors = ['绿色', '红色', '黄色', '白色', '黑色', '金色', '银色', '粉色', '紫色', '蓝色'];
  return colors[dayStemIdx % 10];
};

/**
 * 获取幸运数字
 */
const getLuckyNumber = (dayStemIdx: number): number => {
  return (dayStemIdx * 3 + 1) % 9 + 1;
};

/**
 * 生成每日寄语
 */
const generateDailyMessages = (shishen: string, level: number): string[] => {
  const baseMessages = [
    '今日宜稳中求进',
    '贵人运不错，适合推进项目',
    '保持平和心态，好事即将发生',
  ];

  if (level >= 4) {
    return baseMessages;
  }

  return [
    '今日宜保守行事',
    '注意细节，避免粗心',
    '保持专注，工作顺利',
  ];
};

/**
 * 生成标签
 */
const generateTags = (shishen: string, branchShishen: string, level: number): string[] => {
  const tags: string[] = [];

  // 基于十神添加标签
  if (shishen.includes('官') || shishen.includes('杀')) {
    tags.push('事业运');
  }
  if (shishen.includes('印')) {
    tags.push('学习运');
  }
  if (shishen.includes('食') || shishen.includes('伤')) {
    tags.push('表达运');
  }
  if (shishen.includes('财')) {
    tags.push('财运');
  }
  if (shishen.includes('比') || shishen.includes('劫')) {
    tags.push('人际运');
  }

  // 基于能量等级添加标签
  if (level >= 4) {
    tags.push('贵人运');
  } else {
    tags.push('平稳运');
  }

  return tags.slice(0, 3);
};

/**
 * 获取吉凶 (简化版，基于阴阳)
 */
const getLuck = (lunarDay: LunarDay): string => {
  // 获取六十甲子的索引，偶数为吉，奇数为凶
  // 这只是简化逻辑，实际需要更复杂的判断
  const sixtyCycle = lunarDay.getSixtyCycle();
  const index = sixtyCycle.getIndex();
  return index % 2 === 0 ? '吉' : '凶';
};