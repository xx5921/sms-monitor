@echo off
REM 恢复 MIUI/HyperOS 拦截的短信广播权限
REM 每次通过 Android Studio 或 adb 安装/升级应用后运行一次
REM 原理：MIUI 对服务号短信(银行/交警/验证码)的 SMS_RECEIVED 广播做私有权限拦截，
REM       adb 安装升级会重置这些权限，需重新授权
REM 10018 = OP_READ_NOTIFICATION_SMS(读取通知类短信，关键项)
REM 10017 = 桌面快捷方式 / 10022 = 读取应用列表 / 10045 = 未命名(保险起见一并开启)

adb shell appops set com.example.smsmonitor 10018 allow
adb shell appops set com.example.smsmonitor 10017 allow
adb shell appops set com.example.smsmonitor 10022 allow
adb shell appops set com.example.smsmonitor 10045 allow

echo.
echo MIUI 短信广播权限已恢复，可发送测试短信验证
pause
