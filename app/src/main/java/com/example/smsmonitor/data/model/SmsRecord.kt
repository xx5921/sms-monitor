package com.example.smsmonitor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 短信记录实体
 * 记录匹配到关键词的短信
 */
@Entity(tableName = "sms_records")
data class SmsRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 发送者号码 */
    val sender: String,
    /** 短信内容 */
    val content: String,
    /** 接收时间戳 */
    val receivedAt: Long,
    /** 匹配到的关键词（逗号分隔） */
    val matchedKeywords: String
)
