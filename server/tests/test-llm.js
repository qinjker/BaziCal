const https = require('https');
require('dotenv').config({ path: require('path').resolve(__dirname, '../.env.development') });

const STATIC_MESSAGES = [
  "msg1",
  "msg2",
  "msg3",
  "msg4",
  "msg5",
];

async function testMiniMax() {
  const apiKey = process.env.MINIMAX_API_KEY;
  const model = process.env.MINIMAX_MODEL || 'abab6.5s-chat';

  if (!apiKey) {
    return { success: false, message: 'MINIMAX_API_KEY not configured' };
  }

  return new Promise((resolve) => {
    const postData = JSON.stringify({
      model: model,
      tokens_to_generate: 100,
      temperature: 0.7,
      messages: [
        { role: 'system', content: 'You are a fortune teller.' },
        { role: 'user', content: 'Generate 3 positive messages.' }
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
          // MiniMax uses base_resp.status_code
          if (response.base_resp && response.base_resp.status_code === 0) {
            resolve({ success: true, message: 'MiniMax OK', response });
          } else {
            const msg = response.base_resp ? response.base_resp.status_msg : response.msg;
            resolve({ success: false, message: 'MiniMax API error: ' + msg, response });
          }
        } catch (err) {
          resolve({ success: false, message: 'MiniMax parse error: ' + err.message, response: body });
        }
      });
    });

    req.on('error', (e) => {
      resolve({ success: false, message: 'MiniMax request error: ' + e.message });
    });

    req.setTimeout(10000, () => {
      req.destroy();
      resolve({ success: false, message: 'MiniMax timeout' });
    });

    req.write(postData);
    req.end();
  });
}

async function testQwen() {
  const apiKey = process.env.QWEN_API_KEY;
  if (!apiKey) {
    return { success: false, message: 'QWEN_API_KEY not configured (skip)' };
  }

  return new Promise((resolve) => {
    const postData = JSON.stringify({
      model: 'qwen-turbo',
      input: {
        messages: [
          { role: 'system', content: 'You are a fortune teller.' },
          { role: 'user', content: 'Generate 3 positive messages.' }
        ]
      },
      parameters: { temperature: 0.7, max_tokens: 150 }
    });

    const options = {
      hostname: 'dashscope.aliyuncs.com',
      port: 443,
      path: '/api/v1/services/aigc/text-generation/generation',
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
          if (response.code === 'success') {
            resolve({ success: true, message: 'Qwen OK', response });
          } else {
            resolve({ success: false, message: 'Qwen API error: ' + response.message, response });
          }
        } catch (err) {
          resolve({ success: false, message: 'Qwen parse error: ' + err.message, response: body });
        }
      });
    });

    req.on('error', (e) => {
      resolve({ success: false, message: 'Qwen request error: ' + e.message });
    });

    req.setTimeout(10000, () => {
      req.destroy();
      resolve({ success: false, message: 'Qwen timeout' });
    });

    req.write(postData);
    req.end();
  });
}

async function testStatic() {
  const date = new Date().toISOString().split('T')[0];
  const birthday = '1990-01-15';

  const hashString = (str) => {
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

  const messages = [
    STATIC_MESSAGES[baseIndex],
    STATIC_MESSAGES[(baseIndex + 1) % STATIC_MESSAGES.length],
    STATIC_MESSAGES[(baseIndex + 2) % STATIC_MESSAGES.length],
  ];

  return { success: true, message: 'Static messages OK', messages };
}

async function main() {
  console.log('\n========== LLM Test ==========\n');

  const args = process.argv.slice(2);
  const testTarget = args[0] || '';

  const tests = [];

  if (testTarget === '' || testTarget === 'minimax') tests.push({ name: 'MiniMax', fn: testMiniMax });
  if (testTarget === '' || testTarget === 'qwen') tests.push({ name: 'Qwen', fn: testQwen });
  if (testTarget === '' || testTarget === 'static') tests.push({ name: 'Static', fn: testStatic });

  let allSuccess = true;

  for (const test of tests) {
    console.log('[Test] ' + test.name + '...');
    try {
      const result = await test.fn();
      if (result.success) {
        console.log('  OK: ' + result.message);
        if (result.messages) {
          result.messages.forEach((m, i) => console.log('    ' + (i+1) + '. ' + m));
        }
      } else {
        console.log('  FAIL: ' + result.message);
        if (!result.message.includes('(skip)')) {
          allSuccess = false;
        }
      }
    } catch (err) {
      console.log('  ERROR: ' + err.message);
      allSuccess = false;
    }
  }

  console.log('\n========== Done ==========\n');
  process.exit(allSuccess ? 0 : 1);
}

main();