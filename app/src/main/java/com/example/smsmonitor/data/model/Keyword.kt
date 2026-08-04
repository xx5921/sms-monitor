package com.example.smsmonitor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 关键词实体
 * 用于短信内容匹配过滤
 */
@Entity(tableName = "keywords")
data class Keyword(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 关键词文本 */
    val text: String,
    /** 是否启用 */
    val isEnabled: Boolean = true,
    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis()
)
