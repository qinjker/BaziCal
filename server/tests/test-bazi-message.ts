/**
 * 八字 + 每日寄语 测试
 * 传入用户生日信息，计算八字后获取 MiniMax 正能量寄语
 *
 * 用法:
 *   npx ts-node tests/test-bazi-message.ts                    # 测试默认生日
 *   npx ts-node tests/test-bazi-message.ts 1990-05-15 10 30 男   # 传入生日
 */

import https from 'https';

// 加载环境变量
require('dotenv').config({ path: require('path').resolve(__dirname, '../.env.development') });

// 八字类型定义
interface Bazi {
  year: { stem: string; branch: string };
  month: { stem: string; branch: string };
  day: { stem: string; branch: string };
  hour: { stem: string; branch: string };
  shishen: {
    year: string;
    month: string;
    day: string;
    hour: string;
  };
}

// ============ 八字计算相关（简化版） ============

const GAN = ['甲', '乙', '丙', '丁', '戊', '己', '庚', '辛', '壬', '癸'];
const ZHI = ['子', '丑', '寅', '卯', '辰', '巳', '午', '未', '申', '酉', '戌', '亥'];

// 天干地支转换为天干地支索引
function getGanIndex(gan: string): number {
  return GAN.indexOf(gan);
}

function getZhiIndex(zhi: string): number {
  return ZHI.indexOf(zhi);
}

// 计算天干
function getYearGan(year: number): string {
  const offset = (year - 4) % 10;
  return GAN[offset < 0 ? offset + 10 : offset];
}

// 计算地支
function getYearZhi(year: number): string {
  const offset = (year - 4) % 12;
  return ZHI[offset < 0 ? offset + 12 : offset];
}

// 计算月干
function getMonthGan(yearGan: string, month: number): string {
  const monthGanBase = (getGanIndex(yearGan) * 2) % 10;
  return GAN[(monthGanBase + month - 1) % 10];
}

// 计算日干支
function getDayGanZhi(year: number, month: number, day: number): { gan: string; zhi: string } {
  // 使用蔡勒公式简化计算
  let y = year;
  let m = month;
  let d = day;

  if (m < 3) {
    y -= 1;
    m += 12;
  }

  const c = Math.floor(y / 100);
  const y2 = y % 100;
  const w = Math.floor(c / 4) - 2 * c + Math.floor(y2 / 4) + Math.floor(13 * (m + 1) / 5) + d - 1;

  const dayGanIndex = ((w % 10) + 10) % 10;
  const dayZhiIndex = ((w % 12) + 12) % 12;

  return { gan: GAN[dayGanIndex], zhi: ZHI[dayZhiIndex] };
}

// 计算时干
function getHourGan(dayGan: string, hour: number): string {
  const base = getGanIndex(dayGan) * 2 % 10;
  const hourIndex = Math.floor((hour + 1) / 2) % 12;
  return GAN[(base + hourIndex) % 10];
}

// 十神映射
const SHISHEN_MAP: { [key: string]: { [key: string]: string } } = {
  '甲': { '甲': '比肩', '乙': '劫财', '丙': '食神', '丁': '伤官', '戊': '偏财', '己': '正财', '庚': '七杀', '辛': '正官', '壬': '偏印', '癸': '正印' },
  '乙': { '甲': '劫财', '乙': '比肩', '丙': '伤官', '丁': '食神', '戊': '正财', '己': '偏财', '庚': '正官', '辛': '七杀', '壬': '正印', '癸': '偏印' },
  '丙': { '甲': '偏印', '乙': '正印', '丙': '比肩', '丁': '劫财', '戊': '食神', '己': '伤官', '庚': '偏财', '辛': '正财', '壬': '七杀', '癸': '正官' },
  '丁': { '甲': '正印', '乙': '偏印', '丙': '劫财', '丁': '比肩', '戊': '伤官', '己': '食神', '庚': '正财', '辛': '偏财', '壬': '正官', '癸': '七杀' },
  '戊': { '甲': '七杀', '乙': '正官', '丙': '偏印', '丁': '正印', '戊': '比肩', '己': '劫财', '庚': '食神', '辛': '伤官', '壬': '偏财', '癸': '正财' },
  '己': { '甲': '正官', '乙': '七杀', '丙': '正印', '丁': '偏印', '戊': '劫财', '己': '比肩', '庚': '伤官', '辛': '食神', '壬': '正财', '癸': '偏财' },
  '庚': { '甲': '偏财', '乙': '正财', '丙': '七杀', '丁': '正官', '戊': '偏印', '己': '正印', '庚': '比肩', '辛': '劫财', '壬': '食神', '癸': '伤官' },
  '辛': { '甲': '正财', '乙': '偏财', '丙': '正官', '丁': '七杀', '戊': '正印', '己': '偏印', '庚': '劫财', '辛': '比肩', '壬': '伤官', '癸': '食神' },
  '壬': { '甲': '食神', '乙': '伤官', '丙': '偏财', '丁': '正财', '戊': '七杀', '己': '正官', '庚': '偏印', '辛': '正印', '壬': '比肩', '癸': '劫财' },
  '癸': { '甲': '伤官', '乙': '食神', '丙': '正财', '丁': '偏财', '戊': '正官', '己': '七杀', '庚': '正印', '辛': '偏印', '壬': '劫财', '癸': '比肩' },
};

// 计算八字
function calculateBazi(birthday: string, hour: number, minute: number, gender: string): Bazi {
  const [yearStr, monthStr, dayStr] = birthday.split('-');
  const year = parseInt(yearStr, 10);
  const month = parseInt(monthStr, 10);
  const day = parseInt(dayStr, 10);

  const yearGan = getYearGan(year);
  const yearZhi = getYearZhi(year);
  const monthGan = getMonthGan(yearGan, month);
  const monthZhi = ZHI[month - 1];

  const dayGanZhi = getDayGanZhi(year, month, day);
  const dayGan = dayGanZhi.gan;
  const dayZhi = dayGanZhi.zhi;

  const hourGan = getHourGan(dayGan, hour);
  const hourZhi = ZHI[Math.floor((hour + 1) / 2) % 12];

  // 获取十神
  const yearShishen = SHISHEN_MAP[dayGan]?.[yearGan] || '';
  const monthShishen = SHISHEN_MAP[dayGan]?.[monthGan] || '';
  const dayShishen = '日主'; // 日干为日主
  const hourShishen = SHISHEN_MAP[dayGan]?.[hourGan] || '';

  return {
    year: { stem: yearGan, branch: yearZhi },
    month: { stem: monthGan, branch: monthZhi },
    day: { stem: dayGan, branch: dayZhi },
    hour: { stem: hourGan, branch: hourZhi },
    shishen: {
      year: yearShishen,
      month: monthShishen,
      day: dayShishen,
      hour: hourShishen
    }
  };
}

// ============ MiniMax API 调用 ============

async function getMiniMaxMessages(birthday: string, bazi: any): Promise<{ success: boolean; messages?: string[]; message?: string }> {
  const apiKey = process.env.MINIMAX_API_KEY;
  const model = process.env.MINIMAX_MODEL || 'MiniMax-M2.7';

  if (!apiKey) {
    return { success: false, message: 'MINIMAX_API_KEY not configured' };
  }

  // 构建十神信息
  const baziInfo = bazi;
  const shishenText = `年干${bazi.year.stem}为${bazi.shishen.year}，月干${bazi.month.stem}为${bazi.shishen.month}，日主${bazi.day.stem}，时干${bazi.hour.stem}为${bazi.shishen.hour}`;

  return new Promise((resolve) => {
    const postData = JSON.stringify({
      model: model,
      tokens_to_generate: 150,
      temperature: 0.7,
      messages: [
        { role: 'system', content: '你是命理老师，用温暖简短的话给人力量。结合用户的八字十神信息，生成3条15-20字正能量寄语，用"你"称呼，每条单独一行。' },
        { role: 'user', content: `用户八字：${bazi.year.stem}${bazi.year.branch}年 ${bazi.month.stem}${bazi.month.branch}月 ${bazi.day.stem}${bazi.day.branch}日 ${bazi.hour.stem}${bazi.hour.branch}时。十神信息：${shishenText}。请生成3条正能量寄语。` }
      ]
    });

    const options = {
      hostname: 'api.minimax.chat',
      port: 443,
      path: '/v1/text/chatcompletion_v2',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + apiKey
      }
    };

    const req = https.request(options, (res) => {
      let body = '';
      res.on('data', (chunk) => { body += chunk; });
      res.on('end', () => {
        try {
          const response = JSON.parse(body);
          if (response.base_resp && response.base_resp.status_code === 0 && response.choices && response.choices[0]) {
            const content = response.choices[0].message.content;
            const messages = content.split('\n').filter((m: string) => m.trim());
            resolve({ success: true, messages });
          } else {
            const msg = response.base_resp ? response.base_resp.status_msg : response.msg;
            resolve({ success: false, message: 'MiniMax API error: ' + msg });
          }
        } catch (err) {
          resolve({ success: false, message: 'Parse error: ' + (err as Error).message });
        }
      });
    });

    req.on('error', (e) => {
      resolve({ success: false, message: 'Request error: ' + e.message });
    });

    req.setTimeout(30000, () => {
      req.destroy();
      resolve({ success: false, message: 'Timeout' });
    });

    req.write(postData);
    req.end();
  });
}

// ============ 主函数 ============

async function main() {
  console.log('\n========== 八字 + 每日寄语 测试 ==========\n');

  // 解析命令行参数
  const args = process.argv.slice(2);
  let birthday = '1990-05-15';
  let hour = 10;
  let minute = 30;
  let gender = '男';

  if (args.length >= 1) birthday = args[0];
  if (args.length >= 2) hour = parseInt(args[1], 10);
  if (args.length >= 3) minute = parseInt(args[2], 10);
  if (args.length >= 4) gender = args[3];

  console.log('输入参数:');
  console.log(`  生日: ${birthday}`);
  console.log(`  时间: ${hour}时${minute}分`);
  console.log(`  性别: ${gender}`);
  console.log('');

  // 计算八字
  console.log('[1] 计算八字...');
  const bazi = calculateBazi(birthday, hour, minute, gender);
  console.log('  年: ' + bazi.year.stem + bazi.year.branch + ' (' + bazi.shishen.year + ')');
  console.log('  月: ' + bazi.month.stem + bazi.month.branch + ' (' + bazi.shishen.month + ')');
  console.log('  日: ' + bazi.day.stem + bazi.day.branch + ' (' + bazi.shishen.day + ')');
  console.log('  时: ' + bazi.hour.stem + bazi.hour.branch + ' (' + bazi.shishen.hour + ')');
  console.log('');

  // 获取 MiniMax 寄语
  console.log('[2] 获取 MiniMax 正能量寄语...');
  const result = await getMiniMaxMessages(birthday, bazi);

  if (result.success) {
    console.log('  ✅ 成功获取寄语:');
    result.messages?.forEach((m, i) => {
      console.log(`     ${i + 1}. ${m}`);
    });
  } else {
    console.log('  ❌ ' + result.message);
  }

  console.log('\n========== 测试完成 ==========\n');
}

main();