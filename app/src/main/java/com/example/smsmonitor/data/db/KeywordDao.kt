package com.example.smsmonitor.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smsmonitor.data.model.Keyword

/**
 * 关键词数据访问对象
 */
@Dao
interface KeywordDao {

    @Query("SELECT * FROM keywords ORDER BY createdAt DESC")
    fun getAllKeywords(): LiveData<List<Keyword>>

    @Query("SELECT * FROM keywords WHERE isEnabled = 1")
    suspend fun getEnabledKeywords(): List<Keyword>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keyword: Keyword): Long

    @Update
    suspend fun update(keyword: Keyword)

    @Delete
    suspend fun delete(keyword: Keyword)

    @Query("DELETE FROM keywords")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM keywords")
    suspend fun count(): Int
}
