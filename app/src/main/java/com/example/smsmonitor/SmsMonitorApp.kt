package com.example.smsmonitor

import android.app.Application
import com.example.smsmonitor.data.db.AppDatabase
import com.example.smsmonitor.data.repository.KeywordRepository
import com.example.smsmonitor.data.repository.SmsRepository
import com.example.smsmonitor.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application 类
 * 初始化全局组件
 */
class SmsMonitorApp : Application() {

    // 通过 lazy 延迟初始化
    val database by lazy { AppDatabase.getDatabase(this) }
    val keywordRepository by lazy { KeywordRepository(database.keywordDao()) }
    val smsRepository by lazy { SmsRepository(database.smsRecordDao()) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // 创建通知渠道
        NotificationHelper.createNotificationChannels(this)

        // 初始化默认关键词
        applicationScope.launch {
            keywordRepository.initDefaultKeywords()
        }
    }
}
