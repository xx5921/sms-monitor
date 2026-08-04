package com.example.smsmonitor.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.smsmonitor.data.model.SmsRecord

/**
 * 短信记录数据访问对象
 */
@Dao
interface SmsRecordDao {

    @Query("SELECT * FROM sms_records ORDER BY receivedAt DESC")
    fun getAllRecords(): LiveData<List<SmsRecord>>

    @Query("SELECT * FROM sms_records ORDER BY receivedAt DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 5): LiveData<List<SmsRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SmsRecord): Long

    @Query("DELETE FROM sms_records")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM sms_records")
    suspend fun count(): Int
}
