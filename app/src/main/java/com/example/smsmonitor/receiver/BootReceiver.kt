package com.example.smsmonitor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.smsmonitor.util.NotificationHelper
import com.example.smsmonitor.util.PreferencesManager

/**
 * 开机自启接收器
 * 开机后创建持久通知，提示用户短信监控正在运行
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = PreferencesManager(context)
            if (prefs.isMonitoringEnabled()) {
                // 显示持久运行通知
                NotificationHelper.showRunningNotification(context)
            }
        }
    }
}
