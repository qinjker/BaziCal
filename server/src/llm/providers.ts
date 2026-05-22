/**
 * LLM Provider 接口定义
 * 支持 MiniMax 和 Qwen 两个提供商
 */

import https from 'https';

// LLM Provider 接口
export interface LLMProvider {
  name: string;
  enabled: boolean;
  generate(prompt: string, birthday: string): Promise<string[]>;
  logUsage(promptTokens: number, completionTokens: number, responseTime: number, success: boolean, errorMessage?: string): Promise<void>;
}

// Base response types
interface MiniMaxResponse {
  id: string;
  choices?: Array<{
    finish_reason: string;
    index: number;
    message: { role: string; content: string; name: string };
  }>;
  model: string;
  object: string;
  usage?: {
    total_tokens: number;
    total_characters: number;
    prompt_tokens: number;
    completion_tokens: number;
  };
  base_resp: {
    status_code: number;
    status_msg: string;
  };
}

interface QwenResponse {
  code: string;
  message?: string;
  output?: {
    choices?: Array<{
      message: { role: string; content: string };
    }>;
  };
}

// MiniMax Provider
export class MiniMaxProvider implements LLMProvider {
  name = 'MiniMax';
  private apiKey: string;
  private model: string;
  private temperature: number;
  private maxTokens: number;
  private dailyLimit: number;

  constructor(apiKey: string, model: string, temperature: number, maxTokens: number, dailyLimit: number) {
    this.apiKey = apiKey;
    this.model = model;
    this.temperature = temperature;
    this.maxTokens = maxTokens;
    this.dailyLimit = dailyLimit;
  }

  get enabled(): boolean {
    return !!this.apiKey;
  }

  async generate(prompt: string, birthday: string): Promise<string[]> {
    if (!this.enabled) {
      throw new Error('MiniMax API key not configured');
    }

    // Check daily limit
    const today = new Date().toISOString().split('T')[0];
    const count = await this.getTodayUsageCount();
    if (count >= this.dailyLimit) {
      throw new Error(`MiniMax daily limit exceeded (${count}/${this.dailyLimit})`);
    }

    return new Promise((resolve, reject) => {
      const postData = JSON.stringify({
        model: this.model,
        tokens_to_generate: this.maxTokens,
        temperature: this.temperature,
        messages: [
          { role: 'system', content: '你是命理老师，用温暖简短的话给人力量。' },
          { role: 'user', content: prompt }
        ]
      });

      const options = {
        hostname: 'api.minimax.chat',
        port: 443,
        path: '/v1/text/chatcompletion_v2',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.apiKey}`
        }
      };

      const startTime = Date.now();
      const req = https.request(options, (res) => {
        let body = '';
        res.on('data', (chunk) => { body += chunk; });
        res.on('end', async () => {
          const responseTime = Date.now() - startTime;
          try {
            const response: MiniMaxResponse = JSON.parse(body);
            if (response.base_resp.status_code === 0 && response.choices && response.choices[0]) {
              const content = response.choices[0].message.content;
              const messages = this.parseMessages(content);
              await this.logUsage(0, 0, responseTime, true);
              resolve(messages);
            } else {
              await this.logUsage(0, 0, responseTime, false, response.base_resp.status_msg);
              reject(new Error(`MiniMax API error: ${response.base_resp.status_msg}`));
            }
          } catch (err) {
            await this.logUsage(0, 0, responseTime, false, (err as Error).message);
            reject(err);
          }
        });
      });

      req.on('error', async (e) => {
        const responseTime = Date.now() - startTime;
        await this.logUsage(0, 0, responseTime, false, e.message);
        reject(e);
      });

      req.setTimeout(10000, () => {
        req.destroy();
        reject(new Error('MiniMax request timeout'));
      });

      req.write(postData);
      req.end();
    });
  }

  private parseMessages(content: string): string[] {
    // 解析 LLM 返回的内容，提取3条寄语
    // 格式可能是：1. 消息1\n2. 消息2\n3. 消息3
    const messages: string[] = [];
    const lines = content.split('\n').filter(line => line.trim());

    for (const line of lines) {
      // 匹配 "1. xxx" 或 "1、xxx" 格式
      const match = line.match(/^\d+[、.]\s*(.+)/);
      if (match) {
        messages.push(match[1].trim());
      }
    }

    // 如果解析失败，尝试按换行符分割
    if (messages.length < 3) {
      return lines.slice(0, 3).map(m => m.replace(/^\d+[、.]\s*/, '').trim());
    }

    return messages.slice(0, 3);
  }

  private async getTodayUsageCount(): Promise<number> {
    const { pool } = await import('../config/database');
    const today = new Date().toISOString().split('T')[0];
    try {
      const result = await pool.query(
        `SELECT COUNT(*) as count FROM llm_usage_logs
         WHERE provider = 'MiniMax' AND success = true
         AND created_at >= $1 AND created_at < $1::date + interval '1 day'`,
        [today]
      );
      return parseInt(result.rows[0].count, 10);
    } catch {
      return 0;
    }
  }

  async logUsage(promptTokens: number, completionTokens: number, responseTime: number, success: boolean, errorMessage?: string): Promise<void> {
    const { pool } = await import('../config/database');
    try {
      await pool.query(
        `INSERT INTO llm_usage_logs (provider, model, prompt_tokens, completion_tokens, response_time, success, error_message)
         VALUES ('MiniMax', $1, $2, $3, $4, $5, $6)`,
        [this.model, promptTokens, completionTokens, responseTime, success, errorMessage || null]
      );
    } catch (err) {
      console.error('Failed to log MiniMax usage:', err);
    }
  }
}

// Qwen Provider
export class QwenProvider implements LLMProvider {
  name = 'Qwen';
  private apiKey: string;
  private model: string;
  private temperature: number;
  private maxTokens: number;
  private _enabled: boolean;

  constructor(apiKey: string, model: string, temperature: number, maxTokens: number, enabled: boolean) {
    this.apiKey = apiKey;
    this.model = model;
    this.temperature = temperature;
    this.maxTokens = maxTokens;
    this._enabled = enabled;
  }

  get enabled(): boolean {
    return this._enabled && !!this.apiKey;
  }

  async generate(prompt: string, birthday: string): Promise<string[]> {
    if (!this.enabled) {
      throw new Error('Qwen not enabled or API key not configured');
    }

    return new Promise((resolve, reject) => {
      const postData = JSON.stringify({
        model: this.model,
        input: {
          messages: [
            { role: 'system', content: '你是命理老师，用温暖简短的话给人力量。' },
            { role: 'user', content: prompt }
          ]
        },
        parameters: {
          temperature: this.temperature,
          max_tokens: this.maxTokens
        }
      });

      const options = {
        hostname: 'dashscope.aliyuncs.com',
        port: 443,
        path: '/api/v1/services/aigc/text-generation/generation',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.apiKey}`
        }
      };

      const startTime = Date.now();
      const req = https.request(options, (res) => {
        let body = '';
        res.on('data', (chunk) => { body += chunk; });
        res.on('end', async () => {
          const responseTime = Date.now() - startTime;
          try {
            const response: QwenResponse = JSON.parse(body);
            if (response.code === 'success' && response.output?.choices?.[0]) {
              const content = response.output.choices[0].message.content;
              const messages = this.parseMessages(content);
              await this.logUsage(0, 0, responseTime, true);
              resolve(messages);
            } else {
              await this.logUsage(0, 0, responseTime, false, response.message);
              reject(new Error(`Qwen API error: ${response.message}`));
            }
          } catch (err) {
            await this.logUsage(0, 0, responseTime, false, (err as Error).message);
            reject(err);
          }
        });
      });

      req.on('error', async (e) => {
        const responseTime = Date.now() - startTime;
        await this.logUsage(0, 0, responseTime, false, e.message);
        reject(e);
      });

      req.setTimeout(10000, () => {
        req.destroy();
        reject(new Error('Qwen request timeout'));
      });

      req.write(postData);
      req.end();
    });
  }

  private parseMessages(content: string): string[] {
    const messages: string[] = [];
    const lines = content.split('\n').filter(line => line.trim());

    for (const line of lines) {
      const match = line.match(/^\d+[、.]\s*(.+)/);
      if (match) {
        messages.push(match[1].trim());
      }
    }

    if (messages.length < 3) {
      return lines.slice(0, 3).map(m => m.replace(/^\d+[、.]\s*/, '').trim());
    }

    return messages.slice(0, 3);
  }

  async logUsage(promptTokens: number, completionTokens: number, responseTime: number, success: boolean, errorMessage?: string): Promise<void> {
    const { pool } = await import('../config/database');
    try {
      await pool.query(
        `INSERT INTO llm_usage_logs (provider, model, prompt_tokens, completion_tokens, response_time, success, error_message)
         VALUES ('Qwen', $1, $2, $3, $4, $5, $6)`,
        [this.model, promptTokens, completionTokens, responseTime, success, errorMessage || null]
      );
    } catch (err) {
      console.error('Failed to log Qwen usage:', err);
    }
  }
}

// Provider 工厂函数
export function createLLMProviders(config: {
  minimax: { apiKey: string; model: string; temperature: number; maxTokens: number; dailyLimit: number };
  qwen: { apiKey: string; model: string; temperature: number; maxTokens: number; enabled: boolean };
}): { minimax: MiniMaxProvider; qwen: QwenProvider } {
  return {
    minimax: new MiniMaxProvider(
      config.minimax.apiKey,
      config.minimax.model,
      config.minimax.temperature,
      config.minimax.maxTokens,
      config.minimax.dailyLimit
    ),
    qwen: new QwenProvider(
      config.qwen.apiKey,
      config.qwen.model,
      config.qwen.temperature,
      config.qwen.maxTokens,
      config.qwen.enabled
    )
  };
}