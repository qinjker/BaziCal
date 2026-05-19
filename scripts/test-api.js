/**
 * API 验证脚本
 * 验证 API 接口可用性
 */

const http = require('http');
const crypto = require('crypto');

// 配置
const HOST = 'rili.jingyan99.com';
const PORT = process.env.PORT || '80';
const APP_KEY = 'apkey20260519';

/**
 * 发送 HTTP 请求
 */
function request(options, body = null) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, data: JSON.parse(data) });
        } catch {
          resolve({ status: res.statusCode, data: data });
        }
      });
    });

    req.on('error', reject);
    req.setTimeout(5000, () => {
      req.destroy();
      reject(new Error('Request timeout'));
    });

    if (body) {
      req.write(JSON.stringify(body));
    }
    req.end();
  });
}

/**
 * 生成签名
 * signature = SHA256(appKey + timestamp + body)
 */
function generateSignature(appKey, timestamp, body) {
  const bodyStr = JSON.stringify(body || {});
  const data = appKey + timestamp + bodyStr;
  return crypto.createHash('sha256').update(data).digest('hex');
}

/**
 * 测试健康检查端点
 */
async function testHealth() {
  console.log('\n📡 测试 GET /health...');
  try {
    const result = await request({
      hostname: HOST,
      port: PORT,
      path: '/health',
      method: 'GET'
    });
    console.log(`   状态: ${result.status}`);
    console.log(`   响应: ${JSON.stringify(result.data)}`);
    return result.status === 200;
  } catch (err) {
    console.log(`   ❌ 失败: ${err.message}`);
    return false;
  }
}

/**
 * 测试八字计算端点
 */
async function testCalculate() {
  console.log('\n📡 测试 POST /api/v1/bazi/calculate...');

  const timestamp = Date.now().toString();
  const body = {
    name: '用户',
    birthday: '1990-01-15',
    birthday_type: 'solar',
    hour: 10,
    minute: 30,
    gender: '男',
    device_id:"device_foi835qmzktmpbdsabr"
  };
  const signature = generateSignature(APP_KEY, timestamp, body);

  try {
    const result = await request({
      hostname: HOST,
      port: PORT,
      path: '/api/v1/bazi/calculate',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-App-Key': APP_KEY,
        'X-Timestamp': timestamp,
        'X-Signature': signature
      }
    }, body);

    console.log(`   状态: ${result.status}`);
    console.log(`   响应: ${JSON.stringify(result.data)}`);
    return result.status === 200;
  } catch (err) {
    console.log(`   ❌ 失败: ${err.message}`);
    return false;
  }
}

/**
 * 主函数
 */
async function main() {
  console.log('🔍 API 验证测试开始');
  console.log(`   服务器: ${HOST}:${PORT}`);

  const healthOk = await testHealth();
  const calcOk = await testCalculate();

  console.log('\n========== 测试结果 ==========');
  console.log(`健康检查: ${healthOk ? '✅ 通过' : '❌ 失败'}`);
  console.log(`八字计算: ${calcOk ? '✅ 通过' : '❌ 失败'}`);
  console.log('==============================\n');

  process.exit(healthOk && calcOk ? 0 : 1);
}

main();