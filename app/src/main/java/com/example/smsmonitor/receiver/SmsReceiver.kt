package com.example.smsmonitor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import com.example.smsmonitor.data.db.AppDatabase
import com.example.smsmonitor.data.model.SmsRecord
import com.example.smsmonitor.service.AlertService
import com.example.smsmonitor.util.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 短信接收器
 * 监听收到的新短信，匹配关键词后触发提醒
 */
class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // 获取短信消息
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        // 合并短信内容（长短信可能分多条）
        val sender = messages[0].displayOriginatingAddress ?: messages[0].originatingAddress ?: ""
        val content = messages.joinToString("") { it.displayMessageBody ?: it.messageBody ?: "" }

        Log.d(TAG, "收到短信: sender=$sender, content=$content")

        if (content.isBlank()) return

        // 异步处理关键词匹配
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                processSms(context, sender, content)
            } catch (e: Exception) {
                Log.e(TAG, "处理短信出错", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * 处理短信：检查关键词匹配
     */
    private suspend fun processSms(context: Context, sender: String, content: String) {
        val prefs = PreferencesManager(context)

        // 检查监控是否开启
        if (!prefs.isMonitoringEnabled()) return

        val db = AppDatabase.getDatabase(context)
        val keywords = db.keywordDao().getEnabledKeywords()

        // 匹配关键词
        val matched = keywords.filter { keyword ->
            content.contains(keyword.text, ignoreCase = true)
        }

        if (matched.isEmpty()) return

        val matchedText = matched.joinToString(",") { it.text }
        Log.d(TAG, "匹配到关键词: $matchedText")

        // 保存短信记录
        val record = SmsRecord(
            sender = sender,
            content = content,
            receivedAt = System.currentTimeMillis(),
            matchedKeywords = matchedText
        )
        db.smsRecordDao().insert(record)

        // 启动响铃提醒服务
        val serviceIntent = Intent(context, AlertService::class.java).apply {
            putExtra(AlertService.EXTRA_SENDER, sender)
            putExtra(AlertService.EXTRA_CONTENT, content)
            putExtra(AlertService.EXTRA_MATCHED_KEYWORDS, matchedText)
        }
        context.startForegroundService(serviceIntent)
    }
}
