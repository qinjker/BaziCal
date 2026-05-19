/**
 * 加密工具
 * 用于生成请求签名
 */

const APP_KEY = import.meta.env.VITE_APP_KEY || 'dev-app-key-12345';

/**
 * SHA256 哈希
 * 优先使用 Web Crypto API，备选纯 JavaScript 实现
 */
export const sha256 = async (data: string): Promise<string> => {
  // 方法1: Web Crypto API (浏览器 HTTPS 环境)
  if (typeof crypto !== 'undefined' && crypto.subtle) {
    try {
      const msgBuffer = new TextEncoder().encode(data);
      const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      return hashArray.map((b) => b.toString(16).padStart(2, '0')).join('');
    } catch (e) {
      // Web Crypto 失败，fallback 到备选方案
    }
  }

  // 方法2: 备选实现 (纯 JavaScript，不依赖 Web Crypto)
  return sha256Fallback(data);
};

/**
 * SHA256 纯 JavaScript 实现 (备选方案)
 */
function sha256Fallback(str: string): string {
  // 使用更可靠的纯 JavaScript 实现
  const utf8 = unescape(encodeURIComponent(str));
  const msgLen = utf8.length;

  const words: number[] = [];
  for (let i = 0; i < msgLen; i++) {
    words[i >> 2] |= utf8.charCodeAt(i) << (24 - (i % 4) * 8);
  }
  words[msgLen >> 2] |= 0x80 << (24 - (msgLen % 4) * 8);
  words[words.length - 1] = msgLen * 8;

  const k = [0x428a2b98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
             0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
             0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
             0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
             0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
             0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
             0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
             0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2];

  let h0 = 0x6a09e667, h1 = 0xbb67ae85, h2 = 0x3c6ef372, h3 = 0xa54ff53a;
  let h4 = 0x510e527f, h5 = 0x9b05688c, h6 = 0x1f83d9ab, h7 = 0x5be0cd19;

  const rotl = (x: number, n: number) => (x << n) | (x >>> (32 - n));

  const ch = (x: number, y: number, z: number) => (x & y) ^ (~x & z);
  const maj = (x: number, y: number, z: number) => (x & y) ^ (x & z) ^ (y & z);

  for (let i = 0; i < words.length; i += 16) {
    const w = words.slice(i, i + 16);

    let a = h0, b = h1, c = h2, d = h3, e = h4, f = h5, g = h6, h = h7;

    for (let j = 0; j < 64; j++) {
      let S1: number, s0: number;
      if (j < 16) {
        s0 = w[j];
      } else {
        const s0Raw = rotl(w[j - 15], 7) ^ rotl(w[j - 2], 18) ^ (w[j - 15] >>> 3);
        const s1Raw = rotl(w[j - 7], 12) ^ rotl(w[j - 16], 19) ^ (w[j - 7] >>> 10);
        w[j] = (w[j - 16] + s0Raw + w[j - 7] + s1Raw) >>> 0;
        s0 = w[j];
      }
      S1 = rotl(e, 6) ^ rotl(e, 11) ^ rotl(e, 25);
      const chVal = ch(e, f, g);
      const temp1 = (h + S1 + chVal + k[j] + s0) >>> 0;
      const S2 = rotl(a, 2) ^ rotl(a, 13) ^ rotl(a, 22);
      const majVal = maj(a, b, c);
      const temp2 = (S2 + majVal) >>> 0;

      h = g;
      g = f;
      f = e;
      e = (d + temp1) >>> 0;
      d = c;
      c = b;
      b = a;
      a = (temp1 + temp2) >>> 0;
    }

    h0 = (h0 + a) >>> 0;
    h1 = (h1 + b) >>> 0;
    h2 = (h2 + c) >>> 0;
    h3 = (h3 + d) >>> 0;
    h4 = (h4 + e) >>> 0;
    h5 = (h5 + f) >>> 0;
    h6 = (h6 + g) >>> 0;
    h7 = (h7 + h) >>> 0;
  }

  const toHex = (n: number) => {
    let result = '';
    for (let j = 0; j < 4; j++) {
      result += ((n >>> (j * 8)) & 0xff).toString(16).padStart(2, '0');
    }
    return result;
  };

  return toHex(h0) + toHex(h1) + toHex(h2) + toHex(h3) + toHex(h4) + toHex(h5) + toHex(h6) + toHex(h7);
}

/**
 * 生成签名和时间戳
 */
export const generateSignature = async (body: string): Promise<{ signature: string; timestamp: string }> => {
  const timestamp = Date.now().toString();
  const data = APP_KEY + timestamp + body;
  const signature = await sha256(data);
  return { signature, timestamp };
};

/**
 * 获取时间戳
 */
export const getTimestamp = (): string => {
  return Date.now().toString();
};

/**
 * 获取设备ID
 */
export const getDeviceId = (): string => {
  let deviceId = localStorage.getItem('device_id');
  if (!deviceId) {
    deviceId = 'device_' + Math.random().toString(36).substring(2) + Date.now().toString(36);
    localStorage.setItem('device_id', deviceId);
  }
  return deviceId;
};