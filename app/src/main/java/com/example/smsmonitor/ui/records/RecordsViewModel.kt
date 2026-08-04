package com.example.smsmonitor.ui.records

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.smsmonitor.SmsMonitorApp
import com.example.smsmonitor.data.model.SmsRecord
import kotlinx.coroutines.launch

/**
 * 记录页 ViewModel
 */
class RecordsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SmsMonitorApp

    val records: LiveData<List<SmsRecord>> = app.smsRepository.allRecords

    fun deleteAll() {
        viewModelScope.launch {
            app.smsRepository.deleteAll()
        }
    }
}
