/**
 * 每日寄语服务
 * 优先级：MiniMax -> Qwen -> 静态寄语库
 */

import { pool } from '../config/database';
import { config } from '../config';
import { createLLMProviders, MiniMaxProvider, QwenProvider } from './providers';

// 静态寄语库
const STATIC_MESSAGES = [
  "今日宜保持平和心态，稳中求进必有收获",
  "今日贵人运不错，多与人交流会有意外惊喜",
  "今天适合处理积压事务，你的能力会被认可",
  "保持积极心态，好事即将发生在你身上",
  "今日思维清晰，适合做重要决定",
  "你今天的能量很强，敢于尝试新事物吧",
  "今日适合团队协作，发挥你的领导力",
  "保持耐心，你正在为好运积累能量",
  "今日运势上扬，把握机会展现自我",
  "稳扎稳打，今天的你值得信赖",
  "今日宜静心思考，容易找到问题的答案",
  "你身边的人会带来好运，多与他们交流",
  "今天适合学习新技能，脑子格外灵活",
  "保持微笑，你的气场会吸引正能量",
  "今日适合处理人际关系，沟通效果特别好",
  "有付出就有收获，今天的努力不会白费",
  "今日运势平稳，按计划行事即可",
  "相信自己直觉，今天的判断很准确",
  "今日宜大胆尝试新事物，可能有惊喜",
  "保持专注，你正在接近目标",
  "今日适合整理房间，清理旧的能量",
  "有贵人相助，事情会比预想的顺利",
  "今日思维活跃，适合创意性的工作",
  "保持谦逊，会有人愿意帮助你",
  "今日运势上升期，适合积极行动",
  "今天是反思的好日子，领悟会很多",
  "你比自己想象的更强大",
  "今日适合做决定，计划可以开始了",
  "保持真诚，人脉会给你带来机会",
  "今日好事连连，注意留心身边的小幸运",
  "适合给自己设定新目标",
  "今日贵人运特别旺，要善于借助他人力量",
  "保持好奇心的同时也要谨慎决策",
  "今日适合协作，独乐乐不如众乐乐",
  "你正在积累的经验会派上用场",
  "今日适合主动出击，机会不等人",
  "保持内心的平静，好运会自然到来",
  "今天要相信自己的实力",
  "今日宜循序渐进，不要急于求成",
  "有想法就说出来，会得到认可",
  "今日适合整理思路，写下来会更清晰",
  "保持开放的的心态，好事会来",
  "今日运势非常好，大胆往前走",
  "适合犒劳自己，让自己开心一下",
  "今日会遇到志同道合的人",
  "保持热忱，你的工作会被认可",
  "今日适合犒劳自己，好事即将发生",
  "今天运气不错，适合尝试新事物",
  "保持内心的平静，好运会自然到来",
  "今日贵人运不错，多与人交流会有收获",
  "今天适合处理积压事务，能力会被认可",
  "保持积极心态，好事即将发生在你身上",
  "今日思维清晰，适合做重要决定",
  "你今天的能量很强，敢于尝试新事物吧",
  "今日适合团队协作，发挥你的领导力",
  "保持耐心，你正在为好运积累能量",
  "今日运势上扬，把握机会展现自我",
  "稳扎稳打，今天的你值得信赖",
  "今日宜保持平和心态，稳中求进必有收获",
  "今日贵人运不错，多与人交流会有意外惊喜",
  "今天适合处理积压事务，你的能力会被认可",
  "保持积极心态，好事即将发生在你身上",
  "今日思维清晰，适合做重要决定",
  "你今天的能量很强，敢于尝试新事物吧",
  "今日适合团队协作，发挥你的领导力",
  "保持耐心，你正在为好运积累能量",
  "今日运势上扬，把握机会展现自我",
  "稳扎稳打，今天的你值得信赖",
  "今日宜保持平和心态，稳中求进必有收获",
  "今日贵人运不错，多与人交流会有意外惊喜",
  "今天适合处理积压事务，你的能力会被认可",
  "保持积极心态，好事即将发生在你身上",
  "今日思维清晰，适合做重要决定",
  "你今天的能量很强，敢于尝试新事物吧",
  "今日适合团队协作，发挥你的领导力",
  "保持耐心，你正在为好运积累能量",
  "今日运势上扬，把握机会展现自我",
  "稳扎稳打，今天的你值得信赖",
  "今日宜保持平和心态，稳中求进必有收获",
  "今日贵人运不错，多与人交流会有意外惊喜",
  "今天适合处理积压事务，你的能力会被认可",
  "保持积极心态，好事即将发生在你身上",
  "今日思维清晰，适合做重要决定",
  "你今天的能量很强，敢于尝试新事物吧",
  "今日适合团队协作，发挥你的领导力",
  "保持耐心，你正在为好运积累能量",
  "今日运势上扬，把握机会展现自我",
  "稳扎稳打，今天的你值得信赖",
];

export interface DailyMessageResult {
  birthday: string;
  date: string;
  messages: string[];
  source: 'ai' | 'static';
  model?: string;
  cached: boolean;
}

// 创建 LLM providers
let providers: { minimax: MiniMaxProvider; qwen: QwenProvider } | null = null;

function getProviders() {
  if (!providers) {
    providers = createLLMProviders({
      minimax: {
        apiKey: config.llm.minimax.apiKey,
        model: config.llm.minimax.model,
        temperature: config.llm.minimax.temperature,
        maxTokens: config.llm.minimax.maxTokens,
        dailyLimit: config.llm.minimax.dailyLimit
      },
      qwen: {
        apiKey: config.llm.qwen.apiKey,
        model: config.llm.qwen.model,
        temperature: config.llm.qwen.temperature,
        maxTokens: config.llm.qwen.maxTokens,
        enabled: config.llm.qwen.enabled
      }
    });
  }
  return providers;
}

/**
 * 获取每日寄语
 * 1. 先查缓存
 * 2. 缓存未命中则调用 LLM（MiniMax -> Qwen -> 静态寄语）
 * 3. 结果存入缓存
 */
export async function getDailyMessages(birthday: string, date: string): Promise<DailyMessageResult> {
  // 1. 查缓存
  const cached = await getCachedMessages(birthday, date);
  if (cached) {
    return {
      birthday,
      date,
      messages: cached.messages,
      source: 'ai',
      cached: true
    };
  }

  // 2. 尝试生成新消息
  const messages = await generateMessages(birthday, date);

  // 3. 存入缓存
  await cacheMessages(birthday, date, messages);

  // 4. 返回结果
  const result: DailyMessageResult = {
    birthday,
    date,
    messages,
    source: 'ai',
    cached: false
  };

  return result;
}

/**
 * 从缓存获取寄语
 */
async function getCachedMessages(birthday: string, date: string): Promise<{ messages: string[]; source?: string; model?: string } | null> {
  try {
    const result = await pool.query(
      `SELECT messages, generated_at FROM daily_messages
       WHERE birthday = $1 AND date = $2
       AND expires_at > NOW()`,
      [birthday, date]
    );

    if (result.rows.length > 0) {
      return {
        messages: result.rows[0].messages,
        source: 'cached'
      };
    }
  } catch (err) {
    console.error('Failed to get cached messages:', err);
  }
  return null;
}

/**
 * 缓存寄语
 */
async function cacheMessages(birthday: string, date: string, messages: string[]): Promise<void> {
  try {
    // 计算过期时间：当天 23:59:59
    const expiresAt = new Date(date + ' 23:59:59');

    await pool.query(
      `INSERT INTO daily_messages (birthday, date, messages, expires_at)
       VALUES ($1, $2, $3, $4)
       ON CONFLICT (birthday, date) DO UPDATE SET
         messages = EXCLUDED.messages,
         generated_at = NOW()`,
      [birthday, date, messages, expiresAt]
    );
  } catch (err) {
    console.error('Failed to cache messages:', err);
  }
}

/**
 * 生成寄语（使用 LLM 或静态库）
 */
async function generateMessages(birthday: string, date: string): Promise<string[]> {
  const prompt = `生成3条15-20字正能量寄语，用"你"称呼，分隔。每条寄语单独一行。`;

  // 如果 LLM 未启用，直接使用静态寄语
  if (!config.llm.enabled) {
    return getStaticMessages(birthday, date);
  }

  // 尝试 MiniMax
  const { minimax, qwen } = getProviders();
  if (minimax.enabled) {
    try {
      const messages = await minimax.generate(prompt, birthday);
      return messages;
    } catch (err) {
      console.error('MiniMax generation failed:', err);
    }
  }

  // 尝试 Qwen
  if (qwen.enabled) {
    try {
      const messages = await qwen.generate(prompt, birthday);
      return messages;
    } catch (err) {
      console.error('Qwen generation failed:', err);
    }
  }

  // 都失败了，使用静态寄语
  console.log('All LLM providers failed, using static messages');
  return getStaticMessages(birthday, date);
}

/**
 * 获取静态寄语（根据日期和生日哈希）
 */
function getStaticMessages(birthday: string, date: string): string[] {
  const hashString = (str: string): number => {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) - hash) + str.charCodeAt(i);
      hash = hash & hash;
    }
    return Math.abs(hash);
  };

  const dateHash = hashString(date);
  const birthdayHash = hashString(birthday);
  const baseIndex = (dateHash + birthdayHash) % STATIC_MESSAGES.length;

  return [
    STATIC_MESSAGES[baseIndex],
    STATIC_MESSAGES[(baseIndex + 1) % STATIC_MESSAGES.length],
    STATIC_MESSAGES[(baseIndex + 2) % STATIC_MESSAGES.length],
  ];
}

/**
 * 初始化静态寄语库到数据库
 * 如果数据库中静态寄语不足100条，从静态库补充
 */
export async function initStaticMessages(): Promise<void> {
  try {
    // 检查现有数量
    const result = await pool.query('SELECT COUNT(*) as count FROM static_messages');
    const existingCount = parseInt(result.rows[0].count, 10);

    console.log(`静态寄语库当前数量: ${existingCount}`);

    if (existingCount < 100) {
      // 插入静态寄语到数据库
      const values: string[] = [];
      const params: string[] = [];
      let paramIndex = 1;

      for (const message of STATIC_MESSAGES) {
        values.push(`($${paramIndex}, 'general')`);
        params.push(message);
        paramIndex++;
      }

      if (values.length > 0) {
        await pool.query(
          `INSERT INTO static_messages (content, category) VALUES ${values.join(', ')} ON CONFLICT DO NOTHING`,
          params
        );
        console.log(`已补充 ${STATIC_MESSAGES.length} 条静态寄语到数据库`);
      }
    }
  } catch (err) {
    console.error('初始化静态寄语库失败:', err);
  }
}

/**
 * 清理过期缓存
 */
export async function cleanExpiredCache(): Promise<void> {
  try {
    const result = await pool.query('DELETE FROM daily_messages WHERE expires_at < NOW()');
    if (result.rowCount && result.rowCount > 0) {
      console.log(`已清理 ${result.rowCount} 条过期寄语缓存`);
    }
  } catch (err) {
    console.error('清理过期缓存失败:', err);
  }
}