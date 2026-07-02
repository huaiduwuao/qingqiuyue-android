# qingqiuyue-android

清秋月 Android 客户端 — Kotlin + Jetpack Compose

## 技术栈

| 层 | 选型 |
|----|------|
| 语言 | Kotlin 2.0 |
| UI | Jetpack Compose (Material3) |
| 架构 | MVVM + Hilt + Repository |
| 网络 | Retrofit + OkHttp + Moshi |
| 流式 | okhttp-sse |
| 媒体 | AndroidX Media3 (ExoPlayer) |
| Token | EncryptedSharedPreferences (Android Keystore) |
| 图片 | Coil |
| 日志 | Timber |
| 最低 SDK | 26 (Android 8.0) |
| 目标 SDK | 34 |

## 目录结构

```
qingqiuyue-android/
├── build.gradle.kts              # 顶层
├── settings.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml    # Version catalog(集中管理依赖版本)
├── gradlew + gradlew.bat
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/qingqiuyue/app/
        │   │   ├── QingqiuyueApplication.kt   # @HiltAndroidApp 入口
        │   │   ├── MainActivity.kt             # 单 Activity + Compose Nav
        │   │   ├── di/
        │   │   │   ├── NetworkModule.kt        # Retrofit + OkHttp + Moshi
        │   │   │   └── AppModule.kt            # TokenStore 注入
        │   │   ├── data/
        │   │   │   ├── api/
        │   │   │   │   ├── APIService.kt       # Retrofit interface(20+ 端点)
        │   │   │   │   └── dto/Dtos.kt          # 所有响应/请求 DTO
        │   │   │   └── store/TokenStore.kt     # EncryptedSharedPreferences
        │   │   └── ui/
        │   │       ├── theme/Theme.kt           # MaterialTheme + BrandColor
        │   │       ├── auth/LoginScreen.kt      # 登录
        │   │       ├── main/MainScreen.kt       # BottomNav 容器
        │   │       ├── home/HomeFeedScreen.kt   # 推荐 Feed
        │   │       ├── player/                  # (待补)VideoPlayerView + ExoPlayer
        │   │       ├── chat/
        │   │       │   ├── ChatScreen.kt        # 流式聊天 UI
        │   │       │   └── ChatAPIClient.kt     # OkHttp SSE 流式
        │   │       ├── wallet/WalletScreen.kt   # 余额 + 套餐 + mock-pay
        │   │       ├── profile/ProfileScreen.kt # 退出登录
        │   │       └── components/AIGCBadge.kt  # 「AI 生成」角标
        │   └── res/
        │       ├── values/{strings,colors,themes}.xml
        │       ├── xml/{backup_rules,data_extraction_rules}.xml
        │       └── mipmap-*/                    # 应用图标(待补)
        └── test/java/com/qingqiuyue/app/
            └── APIServiceTest.kt
```

## 前置条件

- Android Studio Hedgehog (2023.1.1) 或更新
- JDK 17
- Android SDK 34 + build-tools 34.0.0
- 安装时勾选:Android SDK Platform 34、Android SDK Build-Tools 34、Android Emulator(可选)

## 启动开发

```bash
# 1. 用 Android Studio 打开整个 qingqiuyue-android/ 目录

# 2. 等待 Gradle Sync(会自动下载 libs.versions.toml 里的所有依赖)

# 3. 配置 API_BASE(开发期):
#    编辑 app/build.gradle.kts,把 buildConfigField "API_BASE" 改为:
#      "http://10.0.2.2:9080"        # Android 模拟器访问宿主机
#      或 "http://192.168.x.x:9080" # 真机调试(Wi-Fi 同网段)

# 4. 选设备:模拟器(API 30+) 或真机 → Run
```

## 打包

```bash
# Debug APK
./gradlew assembleDebug
# 产出:app/build/outputs/apk/debug/app-debug.apk

# Release APK(需签名)
./gradlew assembleRelease
# 产出:app/build/outputs/apk/release/app-release.apk
```

签名配置:在 `app/build.gradle.kts` 的 `signingConfigs` 块添加(密钥库不要提交仓库):

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../keystore/release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = "qingqiuyue"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
```

## 与后端联调

- 默认 `API_BASE = http://10.0.2.2:9080`(Android 模拟器 → 宿主机 APISIX)
- 真机:改 `app/build.gradle.kts` 的 `API_BASE` 为同网段 IP
- Token 自动注入:`NetworkModule.provideOkHttpClient` 在拦截器里读取 `TokenStore.token`
- AIGC「AI 生成」角标组件 `AIGCBadgeInline` / `AIGCBadgeOverlay` 与前端、iOS 视觉对齐

## 已实现端点覆盖

| 前端 page | Android 对应 | 端点 |
|-----------|------------|------|
| 登录 | `LoginScreen` | `POST /api/core/auth/login` |
| 首页 Feed | `HomeFeedScreen` | `GET /api/content/recommend/feed` |
| 数字人聊天 | `ChatScreen` + `ChatAPIClient` | `POST /api/avatar/chat` (SSE) |
| 钱包 + 套餐 | `WalletScreen` | `GET /api/core/wallet/balance` + `/api/core/payment/diamond-packages` |
| 购买(mock-pay) | `WalletViewModel.purchase()` | `POST /api/core/payment/orders` + `/mock-pay` |

## 待补(按前端 page 列表)

- 视频播放器(ExoPlayer) + 全屏 + AIGC 角标 overlay
- 创作者中心(Apply / Works / Monetize / Activity)
- 评论列表 + 发布
- 视频生成流程(`/api/ai/generate/workflows` + `POST /api/ai/generate/video` SSE)
- 系统推送 / 通知
- 应用图标(各 DPI mipmap)

按 `ViewModel → Screen → View` 模式追加,`APIService` 已统一封装,直接 `api.xxx()` 即可。