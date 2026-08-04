package com.example.smsmonitor.data.repository

import androidx.lifecycle.LiveData
import com.example.smsmonitor.data.db.KeywordDao
import com.example.smsmonitor.data.model.Keyword

/**
 * 关键词仓库
 */
class KeywordRepository(private val keywordDao: KeywordDao) {

    val allKeywords: LiveData<List<Keyword>> = keywordDao.getAllKeywords()

    suspend fun getEnabledKeywords(): List<Keyword> = keywordDao.getEnabledKeywords()

    suspend fun insert(keyword: Keyword): Long = keywordDao.insert(keyword)

    suspend fun update(keyword: Keyword) = keywordDao.update(keyword)

    suspend fun delete(keyword: Keyword) = keywordDao.delete(keyword)

    suspend fun deleteAll() = keywordDao.deleteAll()

    /**
     * 初始化默认关键词
     */
    suspend fun initDefaultKeywords() {
        if (keywordDao.count() > 0) return
        val defaults = listOf("违章", "停车", "处罚", "交警")
        defaults.forEach { text ->
            keywordDao.insert(Keyword(text = text))
        }
    }
}
