# Android 短信监控 App - 开发需求

## 项目概述
开发一个 Android 短信监控 App，能够根据用户自定义的关键词过滤收到的短信，并在匹配到关键词时进行多次响铃提醒。

## 核心功能

### 1. 短信监控
- 通过 Android SmsManager/SMS BroadcastReceiver 监听接收到的短信
- 实时拦截和分析短信内容

### 2. 关键词管理
- 用户可以添加、编辑、删除自定义关键词（如"停车违章"、"违章"、"处罚"等）
- 支持多个关键词，任意一个匹配即触发提醒
- 关键词持久化存储（SharedPreferences 或 Room 数据库）
- 默认预置一些常见关键词：违章、停车、处罚、交警

### 3. 多次响铃提醒
- 匹配到关键词时，播放自定义铃声
- **多次响铃**：默认响铃3次，每次间隔5秒（用户可配置次数和间隔）
- 即使手机处于静音模式，也要强制响铃（如果用户开启了这个选项）
- 使用 MediaPlayer + AudioManager 实现，支持通过 Volume 键停止

### 4. 通知通知
- 同时发送一条高优先级通知（Notification），显示短信内容和发送者
- 点击通知可以打开 App 查看详情

### 5. 短信记录
- 记录所有匹配到的短信（发送者、内容、时间、匹配的关键词）
- 提供历史记录列表查看
- 支持清空记录

### 6. 权限处理
- 正确处理 Android 运行时权限：RECEIVE_SMS、READ_SMS、NOTIFICATION
- 引导用户授予必要权限
- 适配 Android 14+ 的权限要求

## 技术要求
- **语言**: Kotlin
- **最低 SDK**: Android 8.0 (API 26)
- **目标 SDK**: Android 14 (API 34)
- **UI**: Material Design 3 风格
- **架构**: MVVM + Repository 模式
- **数据库**: Room
- **构建工具**: Gradle (Kotlin DSL)

## UI 页面

### 主页面（首页）
- 显示监控状态（开启/关闭开关）
- 快速查看最近匹配的短信
- 底部导航：首页、关键词、记录、设置

### 关键词管理页
- 关键词列表（可增删改）
- 添加关键词的输入框和按钮
- 每个关键词可以单独启用/禁用

### 记录页
- 匹配短信的历史记录列表
- 每条记录显示：发送者、内容、时间、匹配关键词
- 支持清空记录

### 设置页
- 响铃次数设置（1-10次）
- 响铃间隔设置（1-30秒）
- 铃声选择（系统铃声或自定义）
- 强制响铃开关（静音模式下也响铃）
- 振动开关

## 项目结构
```
app/src/main/java/com/example/smsmonitor/
├── MainActivity.kt
├── data/
│   ├── model/ (SmsRecord, Keyword)
│   ├── db/ (AppDatabase, SmsRecordDao, KeywordDao)
│   └── repository/ (SmsRepository, KeywordRepository)
├── receiver/
│   └── SmsReceiver.kt
├── service/
│   └── AlertService.kt (多次响铃服务)
├── ui/
│   ├── home/ (首页)
│   ├── keywords/ (关键词管理)
│   ├── records/ (记录页)
│   └── settings/ (设置页)
└── util/
    ├── NotificationHelper.kt
    └── PreferencesManager.kt
```

## 注意事项
1. 使用 Material Design 3 组件
2. 深色模式支持
3. 确保 SmsReceiver 在开机后自动注册（BOOT_COMPLETED）
4. 前台服务（ForegroundService）保证持续监控
5. 电量优化白名单引导
6. 代码注释用中文

## 完成后
完成所有代码后，运行以下命令通知我：
openclaw system event --text "Done: Android短信监控App开发完成，包含短信监控、关键词管理、多次响铃、记录历史等功能" --mode now
