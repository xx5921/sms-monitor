package com.example.smsmonitor.data.repository

import androidx.lifecycle.LiveData
import com.example.smsmonitor.data.db.SmsRecordDao
import com.example.smsmonitor.data.model.SmsRecord

/**
 * 短信记录仓库
 */
class SmsRepository(private val smsRecordDao: SmsRecordDao) {

    val allRecords: LiveData<List<SmsRecord>> = smsRecordDao.getAllRecords()

    fun getRecentRecords(limit: Int = 5): LiveData<List<SmsRecord>> =
        smsRecordDao.getRecentRecords(limit)

    suspend fun insert(record: SmsRecord): Long = smsRecordDao.insert(record)

    suspend fun deleteAll() = smsRecordDao.deleteAll()
}
