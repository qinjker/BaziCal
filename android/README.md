# BaziCal 安卓端

八字历 Android 客户端，基于 Jetpack Compose 开发。

## 项目概述

- **Slogan**: 每一天，都算数
- **API**: 与 Web 端共用 `api.bazical.com`
- **最低支持**: Android 6.0 (API 23)
- **目标支持**: Android 14 (API 34)

## 技术栈

| 类别 | 技术 |
|------|------|
| UI | Jetpack Compose |
| 架构 | MVVM + Clean Architecture |
| DI | Hilt 2.50 |
| 网络 | Retrofit 2.9.0 + OkHttp 4.12.0 |
| 导航 | Navigation Compose 2.7.7 |
| 本地存储 | DataStore Preferences |
| 异步 | Kotlin Coroutines |
| 开机画面 | SplashScreen API 1.0.1 |

## 项目结构

```
android/
├── app/src/main/
│   ├── java/com/bazical/app/
│   │   ├── BaziCalApp.kt              # Application 类
│   │   ├── MainActivity.kt             # 入口 Activity
│   │   ├── data/
│   │   │   ├── remote/
│   │   │   │   ├── api/BaziApi.kt     # Retrofit API 接口
│   │   │   │   ├── dto/               # 数据传输对象
│   │   │   │   └── interceptor/       # 签名拦截器
│   │   │   ├── local/
│   │   │   │   └── UserDataStore.kt  # 本地数据存储
│   │   │   └── repository/           # Repository 实现
│   │   ├── domain/
│   │   │   ├── model/                # 领域模型
│   │   │   ├── repository/           # Repository 接口
│   │   │   └── usecase/              # Use Cases
│   │   ├── ui/
│   │   │   ├── theme/                # Compose 主题
│   │   │   ├── navigation/           # 导航图
│   │   │   ├── splash/               # 开机画面
│   │   │   ├── home/                 # 首页
│   │   │   ├── calendar/             # 日历页
│   │   │   └── daily/                # 每日详情
│   │   └── di/                        # Hilt 模块
│   └── res/
│       └── AndroidManifest.xml
├── build.gradle.kts
└── settings.gradle.kts
```

## 页面流转

```
Splash Screen
    │
    ├── 有用户数据 → Calendar Screen
    │
    └── 无用户数据 → Home Screen → 计算八字 → Calendar Screen
                                                        │
                                                    Daily Screen
```

## 功能页面

### 1. Splash Screen (开机画面)
- 检查本地存储是否有用户数据
- 根据数据状态决定跳转到首页或日历页

### 2. Home Screen (首页)
- 姓名输入
- 阳历出生日期选择（DatePicker）
- 出生时辰（小时/分钟）
- 性别选择
- 实时显示阴历转换结果
- 提交计算八字

### 3. Calendar Screen (日历页)
- 四柱八字展示（年柱、月柱、日柱、时柱）
- 月份日历网格
- 当日吉凶标注
- 月份切换（上月/下月）
- 点击日期进入每日详情

### 4. Daily Screen (每日详情)
- 干支、五行、星宿信息
- 宜忌标签详情
- 吉凶指示

## API 接口

与 Web 端共用同一套 API：

| 接口 | 方法 | 路径 |
|------|------|------|
| 计算八字 | POST | `/api/v1/bazi/calculate` |
| 获取日历 | GET | `/api/v1/bazi/calendar` |
| 阳历转阴历 | GET | `/api/v1/bazi/solar-to-lunar` |

### 签名机制

所有 `/api/v1/bazi/*` 请求需携带签名 Header：

```
X-App-Key: bazical-app-key-2024
X-Timestamp: <毫秒时间戳>
X-Signature: SHA256(appKey + timestamp + requestBody)
```

## 构建 APK

### 前提条件
- JDK 17+
- Android SDK

### 构建命令

```bash
cd android

# 下载 Gradle Wrapper 依赖（首次）
./gradlew --version

# 调试构建
./gradlew assembleDebug

# 发布构建
./gradlew assembleRelease
```

### 输出目录
- 调试 APK: `app/build/outputs/apk/debug/app-debug.apk`
- 发布 APK: `app/build/outputs/apk/release/app-release.apk`

## 开发规范

### 代码风格
- 使用 Kotlin 协程处理异步操作
- ViewModel + StateFlow 管理 UI 状态
- 遵循 Material 3 设计规范

### 命名规范
- 变量/函数: camelCase
- 类名/接口: PascalCase
- Compose 组件: PascalCase

### 架构分层
- **UI Layer**: Compose 页面、ViewModel
- **Domain Layer**: Use Cases、业务模型
- **Data Layer**: Repository、API、DataStore

## 兼容性

测试覆盖以下厂商系统：
- 小米 (MIUI)
- OPPO (ColorOS)
- vivo (FuntouchOS)
- 华为 (HarmonyOS)
- 三星 (OneUI)
- 一加 (OxygenOS)