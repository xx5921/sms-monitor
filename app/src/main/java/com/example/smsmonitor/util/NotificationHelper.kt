package com.example.smsmonitor.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smsmonitor.AlertActivity
import com.example.smsmonitor.R
import com.example.smsmonitor.service.AlertService

/**
 * 通知工具类
 * 管理所有通知的创建和发送
 */
object NotificationHelper {

    // 通知渠道 ID
    const val CHANNEL_ALERT = "channel_alert"        // 高优先级提醒
    const val CHANNEL_RUNNING = "channel_running"    // 运行状态

    // 通知 ID
    const val NOTIFICATION_ID_ALERT = 2001
    const val NOTIFICATION_ID_RUNNING = 2002

    /**
     * 创建通知渠道（需在 Application onCreate 中调用）
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            // 高优先级提醒渠道
            val alertChannel = NotificationChannel(
                CHANNEL_ALERT,
                "短信提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "匹配关键词的短信提醒"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // 运行状态渠道（低优先级）
            val runningChannel = NotificationChannel(
                CHANNEL_RUNNING,
                "运行状态",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "短信监控运行状态"
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }

            manager.createNotificationChannels(listOf(alertChannel, runningChannel))
        }
    }

    /**
     * 创建前台服务通知
     */
    fun createForegroundNotification(
        context: Context,
        sender: String,
        content: String
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            AlertActivity.createIntent(context, sender, content, ""),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = createStopAlertPendingIntent(context)

        return NotificationCompat.Builder(context, CHANNEL_ALERT)
            .setSmallIcon(R.drawable.ic_sms_notification)
            .setContentTitle("收到关键词短信: $sender")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .addAction(
                R.drawable.ic_sms_notification,
                "停止响铃",
                stopIntent
            )
            .setAutoCancel(true)
            .build()
    }

    /**
     * 发送高优先级提醒通知
     */
    fun sendAlertNotification(
        context: Context,
        sender: String,
        content: String,
        matchedKeywords: String
    ) {
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            AlertActivity.createIntent(context, sender, content, matchedKeywords),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = createStopAlertPendingIntent(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERT)
            .setSmallIcon(R.drawable.ic_sms_notification)
            .setContentTitle("短信提醒: $sender")
            .setContentText("匹配关键词: $matchedKeywords")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$content\n\n匹配关键词: $matchedKeywords"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .addAction(
                R.drawable.ic_sms_notification,
                "停止响铃",
                stopIntent
            )
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID_ALERT, notification)
    }

    /**
     * 显示运行状态通知（持久通知）
     */
    fun showRunningNotification(context: Context) {
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, com.example.smsmonitor.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_RUNNING)
            .setSmallIcon(R.drawable.ic_sms_notification)
            .setContentTitle("短信监控运行中")
            .setContentText("正在监控包含关键词的短信")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID_RUNNING, notification)
    }

    /**
     * 取消运行状态通知
     */
    fun cancelRunningNotification(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(NOTIFICATION_ID_RUNNING)
    }

    /**
     * 创建停止提醒服务的 PendingIntent。
     *
     * Args:
     *     context: 上下文对象。
     *
     * Returns:
     *     点击通知动作时发送给提醒服务的 PendingIntent。
     */
    private fun createStopAlertPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlertService::class.java).apply {
            action = AlertService.ACTION_STOP_ALERT
        }

        return PendingIntent.getService(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
