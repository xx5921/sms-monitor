# 短信监控 (SMS Monitor)

Android 短信监控应用，根据自定义关键词过滤收到的短信，匹配时触发多次响铃提醒。

## 功能

- **短信监控**：实时监听收到的短信，匹配用户自定义关键词
- **关键词管理**：增删改、启用/禁用关键词，默认预置违章、停车、处罚、交警
- **多次响铃**：可配置响铃次数（1-10 次）和间隔（1-30 秒）
- **强制响铃**：可穿透静音模式，锁屏时弹出全屏提醒
- **振动提醒**：可独立开关
- **铃声选择**：支持系统铃声自定义
- **历史记录**：所有匹配短信持久化存储，可查看和清空
- **深色模式**：自适应系统深色主题

## 技术栈

- Kotlin + MVVM + Room
- Material Design 3
- 最低 Android 8.0 (API 26)，目标 Android 14 (API 34)

## 构建

```bash
./gradlew assembleDebug
```

## 安装

构建后将 `app/build/outputs/apk/debug/app-debug.apk` 传到手机安装，首次启动需授予短信和通知权限。

## MIUI/HyperOS 特别说明

小米系统会对**服务号短信**（银行、交警、验证码等，广播标记 `SERVICE_NUMBER=true`）的 `SMS_RECEIVED` 广播做私有权限拦截，系统日志表现为：

```
reason: skipped by policy at enqueue: MIUI Permission Skip
```

第三方应用默认收不到这类广播（普通手机号的短信不受影响），需要通过 adb 开启 MIUI 私有权限：

```bash
adb shell appops set com.example.smsmonitor 10018 allow   # 读取通知类短信（关键项）
adb shell appops set com.example.smsmonitor 10017 allow   # 桌面快捷方式
adb shell appops set com.example.smsmonitor 10022 allow   # 读取应用列表
adb shell appops set com.example.smsmonitor 10045 allow   # 未命名权限（保险起见一并开启）
```

或者直接双击项目根目录的 `fix-miui-sms-permission.bat`。

注意：

- 该设置**日常重启不会丢失**，但**每次安装/升级应用后会被重置**，需重新执行
- 权限码对照参考社区逆向的 [XiaomiUtilities](https://gist.github.com/0awawa0/65bf88e43159750f596da194ed923522)，10018 = `OP_READ_NOTIFICATION_SMS`

## License

MIT
