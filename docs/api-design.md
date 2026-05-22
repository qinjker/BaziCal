# BaziCal API 接口设计

> 文档更新时间：2026-05-18
> 基础路径：`/api/v1`

---

## 一、现有 API 接口（已实现）

### 1. 八字计算

**POST** `/api/v1/bazi/calculate`

计算用户八字，需要签名验证。

**Request:**
```json
{
  "name": "张三",
  "birthday": "1990-05-15",
  "birthday_type": "solar",
  "hour": 10,
  "minute": 30,
  "gender": "男",
  "device_id": "optional-device-id"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | ✅ | 姓名 |
| birthday | string | ✅ | 生日日期 (YYYY-MM-DD) |
| birthday_type | string | ✅ | 生日类型：solar=阳历，lunar=阴历 |
| hour | int | ✅ | 小时 (0-23) |
| minute | int | ✅ | 分钟 (0-59) |
| gender | string | ✅ | 性别 (男/女) |
| device_id | string | ❌ | 设备ID |

**说明：**
- 如果 `birthday_type` 为 `solar`（阳历），后端会自动转换为阴历后再计算八字
- 如果 `birthday_type` 为 `lunar`（阴历），直接使用该日期计算八字
- 默认值为 `solar`

**Response:**
```json
{
  "code": 0,
  "data": {
    "userId": "uuid-xxx",
    "bazi": {
      "year": { "stem": "庚", "branch": "午" },
      "month": { "stem": "壬", "branch": "午" },
      "day": { "stem": "戊", "branch": "子" },
      "hour": { "stem": "壬", "branch": "午" },
      "wuxing": { "木": 0, "火": 2, "土": 1, "金": 1, "水": 2 },
      "shishen": {
        "year": "正官",
        "month": "偏印",
        "day": "日主",
        "hour": "偏印"
      }
    }
  }
}
```

---

### 2. 获取月历

**GET** `/api/v1/bazi/calendar`

获取指定月份的日历数据，需要签名验证。

**Request Query:**
```
?userId=xxx&year=2026&month=5
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | string | ✅ | 用户ID |
| year | int | ✅ | 年份 |
| month | int | ✅ | 月份 (1-12) |

**Response:**
```json
{
  "code": 0,
  "data": {
    "days": [
      {
        "date": "2026-05-01",
        "ganzhi": "丙戌",
        "wuxing": "土",
        "yi": ["祭祀", "开业"],
        "ji": ["动土"],
        "star": "角宿",
        "luck": "吉",
        "shishen": "食神",
        "jieqi": "",
        "lunarDate": "初一",
        "holiday": "劳动节",
        "branchShishen": "伤官"
      }
      // ... 当月所有天
    ]
  }
}
```

**返回字段说明:**
| 字段 | 类型 | 说明 |
|------|------|------|
| date | string | 阳历日期 (YYYY-MM-DD) |
| ganzhi | string | 当日干支 (如：丙戌) |
| wuxing | string | 五行属性 |
| yi | string[] | 宜做的事 |
| ji | string[] | 忌做的事 |
| star | string | 二十八星宿 |
| luck | string | 吉/凶 |
| shishen | string | 天干十神 |
| jieqi | string | 节气 (无节气则为空) |
| lunarDate | string | 农历日期 (如：初一) |
| holiday | string | 节日 (无则为空) |
| branchShishen | string | 地支十神 |

---

### 3. 阳历转阴历

**GET** `/api/v1/bazi/solar-to-lunar`

阳历日期转阴历，需要签名验证。

**Request Query:**
```
?date=2026-05-15
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | string | ✅ | 阳历日期 (YYYY-MM-DD) |

**Response:**
```json
{
  "code": 0,
  "data": {
    "year": 2026,
    "month": 4,
    "day": 10
  }
}
```

---

### 4. 管理员登录

**POST** `/api/v1/admin/login`

管理员登录，无需认证。

**Request:**
```json
{
  "username": "admin",
  "password": "xxx"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

---

### 5. 获取用户列表

**GET** `/api/v1/admin/users`

获取所有用户列表，需要管理员认证。

**Request Query:**
```
?page=1&pageSize=20
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | ❌ | 页码 (默认1) |
| pageSize | int | ❌ | 每页数量 (默认20) |

**Response:**
```json
{
  "code": 0,
  "data": {
    "users": [
      {
        "user_id": "uuid-xxx",
        "name": "张三",
        "birthday": "1990-05-15",
        "gender": "男",
        "created_at": "2026-05-13T12:00:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

---

## 二、缺失的 API 接口

### 1. 获取用户信息

**GET** `/api/v1/bazi/user/:userId`

获取用户详细信息。

**Response:**
```json
{
  "code": 0,
  "data": {
    "userId": "uuid-xxx",
    "name": "张三",
    "birthday": "1990-05-15",
    "hour": 10,
    "minute": 30,
    "gender": "男",
    "bazi": { ... }
  }
}
```

---

### 2. 获取每日详情

**GET** `/api/v1/bazi/daily/:date`

获取指定日期的详细信息。

**Request Query:**
```
?userId=xxx
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "date": "2026-05-15",
    "ganzhi": { ... },
    "shishen": { ... },
    "energy": {
      "level": 4,
      "description": "贵人运不错，适合推进项目"
    },
    "lucky": {
      "direction": "东",
      "time": "卯时(5-7点)",
      "color": "绿色",
      "number": 3
    },
    "messages": ["今日宜稳中求进", "谈判顺利"],
    "tags": ["贵人运", "事业进步"]
  }
}
```

---

### 3. 生成送礼日历

**POST** `/api/v1/gift/generate`

生成送礼日历卡。

**Request:**
```json
{
  "birthYear": 1995,
  "birthMonth": 8,
  "birthDay": 15,
  "birthday_type": "solar",
  "blessing": "生日快乐！"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| birthYear | int | ✅ | 出生年份 |
| birthMonth | int | ✅ | 出生月份 |
| birthDay | int | ✅ | 出生日期 |
| birthday_type | string | ✅ | 生日类型：solar=阳历，lunar=阴历 |
| blessing | string | ❌ | 祝福语 |

**Response:**
```json
{
  "code": 0,
  "data": {
    "shareCode": "ABC12345",
    "previewUrl": "https://api.bazical.com/gift/ABC12345"
  }
}
```

---

### 4. 查看送礼日历

**GET** `/api/v1/gift/:shareCode`

通过分享码查看送礼日历。

**Response:**
```json
{
  "code": 0,
  "data": {
    "shareCode": "ABC12345",
    "friendBirthday": "1995-08-15",
    "friendGanzhi": "乙亥",
    "friendRizhu": "辛未",
    "friendShishen": "偏印",
    "blessing": "生日快乐！",
    "currentGanzhi": { ... }
  }
}
```

---

### 5. 提交反馈

**POST** `/api/v1/feedback`

提交用户反馈，需要签名验证。

**Request:**
```json
{
  "type": "功能建议",
  "content": "希望增加春节提醒功能",
  "contact": "微信号：xxx",
  "device_id": "optional-device-id"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | ✅ | 反馈类型：功能建议/问题反馈/体验优化/其他 |
| content | string | ✅ | 反馈内容，最多500字符 |
| contact | string | ❌ | 联系方式（微信号/邮箱/手机号） |
| device_id | string | ❌ | 设备ID，用于关联用户 |

**Response:**
```json
{
  "code": 0,
  "message": "反馈已提交，感谢您的意见！"
}
```

**错误码：**
| code | 说明 |
|------|------|
| 400 | 参数错误（type不在范围内，content为空或超过500字符） |

---

### 6. 获取我的反馈列表

**GET** `/api/v1/feedbacks`

获取当前用户的反馈列表，需要签名验证。

**Request Query:**
```
?page=1&pageSize=20
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | ❌ | 页码 (默认1) |
| pageSize | int | ❌ | 每页数量 (默认20) |

**Response:**
```json
{
  "code": 0,
  "data": {
    "feedbacks": [
      {
        "id": "uuid-xxx",
        "type": "功能建议",
        "content": "希望增加春节提醒功能",
        "contact": "微信号：xxx",
        "status": "replied",
        "latestReply": {
          "author_type": "admin",
          "author_name": "客服小八",
          "content": "感谢反馈，已记录..."
        },
        "replyCount": 3,
        "created_at": "2026-05-20T12:00:00Z"
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 20
  }
}
```

**Response 字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 反馈ID |
| type | string | 反馈类型 |
| content | string | 反馈内容 |
| contact | string | 联系方式 |
| status | string | 状态 (pending/reviewed/replied/closed) |
| latestReply | object | 最新一条回复（用于列表展示预览） |
| latestReply.author_type | string | 回复者类型 (user/admin) |
| latestReply.author_name | string | 回复者名称 |
| latestReply.content | string | 回复内容预览 |
| replyCount | int | 回复总数 |
| created_at | string | 提交时间 |

---

### 7. 获取反馈列表（管理后台）

**GET** `/api/v1/admin/feedbacks`

获取用户反馈列表，需要管理员认证。

**Request Query:**
```
?page=1&pageSize=20&status=pending&type=功能建议
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | ❌ | 页码 (默认1) |
| pageSize | int | ❌ | 每页数量 (默认20) |
| status | string | ❌ | 状态筛选：pending/reviewed/replied/closed |
| type | string | ❌ | 类型筛选：功能建议/问题反馈/体验优化/其他 |

**Response:**
```json
{
  "code": 0,
  "data": {
    "feedbacks": [
      {
        "id": "uuid-xxx",
        "type": "功能建议",
        "content": "希望增加春节提醒功能",
        "contact": "微信号：xxx",
        "user_id": "xxx",
        "device_id": "xxx",
        "status": "pending",
        "replies": [
          {
            "id": "reply-uuid",
            "content": "感谢您的反馈，我们已记录",
            "author_type": "admin",
            "author_id": "admin1",
            "author_name": "客服小八",
            "created_at": "2026-05-21T10:00:00Z"
          }
        ],
        "created_at": "2026-05-20T12:00:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

**Response 字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| replies | array | 回复列表，按时间升序排列形成盖楼效果 |

---

### 8. 获取反馈的回复列表

**GET** `/api/v1/feedbacks/:id/replies`

获取指定反馈的所有回复（支持盖楼展示），需要签名验证。

**Response:**
```json
{
  "code": 0,
  "data": {
    "replies": [
      {
        "id": "reply-uuid-1",
        "content": "希望增加春节提醒功能",
        "author_type": "user",
        "author_id": "user-xxx",
        "author_name": "用户",
        "created_at": "2026-05-20T12:00:00Z"
      },
      {
        "id": "reply-uuid-2",
        "content": "感谢您的反馈，我们已记录",
        "author_type": "admin",
        "author_id": "admin1",
        "author_name": "客服小八",
        "created_at": "2026-05-21T10:00:00Z"
      }
    ]
  }
}
```

---

### 9. 添加反馈回复（支持盖楼）

**POST** `/api/v1/feedbacks/:id/replies`

用户或管理员添加回复，需要签名验证（用户）或管理员认证（管理员）。

**用户提交 Request:**
```json
{
  "content": "追问：能具体说说计划吗？"
}
```

**管理员提交 Request:**
```json
{
  "content": "感谢您的反馈，我们已记录并在后续版本中考虑。"
}
```

**Response:**
```json
{
  "code": 0,
  "message": "回复成功"
}
```

---

### 10. 回复反馈（管理后台 - 兼容旧版）

**POST** `/api/v1/admin/feedbacks/:id/reply`

回复用户反馈（内部调用添加回复接口），需要管理员认证。

**Request:**
```json
{
  "content": "感谢您的反馈，我们已记录并在后续版本中考虑。"
}
```

**Response:**
```json
{
  "code": 0,
  "message": "回复成功"
}
```

---

### 11. 更新反馈状态（管理后台）

**PATCH** `/api/v1/admin/feedbacks/:id/status`

更新反馈状态，需要管理员认证。

**Request:**
```json
{
  "status": "reviewed"
}
```

**Response:**
```json
{
  "code": 0,
  "message": "状态已更新"
}
```

---

### 12. 获取每日寄语

**POST** `/api/v1/bazi/daily-message`

获取指定生日和日期的正能量寄语，需要签名验证。

**Request:**
```json
{
  "birthday": "1990-01-15",
  "date": "2026-05-21"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| birthday | string | ✅ | 生日日期 (YYYY-MM-DD) |
| date | string | ✅ | 当日日期 (YYYY-MM-DD) |

**Response:**
```json
{
  "code": 0,
  "data": {
    "birthday": "1990-01-15",
    "date": "2026-05-21",
    "messages": [
      "今天的你思维清晰，稳重的特质让你处理问题格外得心应手",
      "正官星旺，今日贵人运强，多与人交流会有意想不到的收获",
      "保持平和心态，你的踏实和耐心正在为你积累好运"
    ],
    "source": "ai",
    "model": "abab6.5s-chat",
    "cached": false
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| birthday | string | 生日日期 |
| date | string | 当日日期 |
| messages | string[] | 寄语数组（3条） |
| source | string | 来源：ai=LLM生成，static=静态寄语库 |
| model | string | 使用的模型名称 |
| cached | boolean | 是否命中缓存 |

**说明：**
- 优先从缓存获取，未命中则调用 LLM 生成
- LLM 降级顺序：MiniMax → Qwen → 静态寄语库
- 缓存过期时间：当天 23:59:59

---

## 三、错误码

| code | HTTP状态码 | 说明 |
|------|-----------|------|
| 0 | 200 | 成功 |
| 400 | 400 | 参数错误 |
| 401 | 401 | 未授权 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 服务器内部错误 |

---

## 四、认证方式

- **用户 API**：需要签名验证 (通过 `signatureMiddleware`)
- **管理后台 API**：需要 JWT Token 认证
- **送礼分享码**：无需认证，公开访问