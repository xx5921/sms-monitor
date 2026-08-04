package com.example.smsmonitor.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.smsmonitor.SmsMonitorApp
import com.example.smsmonitor.data.model.SmsRecord
import com.example.smsmonitor.util.PreferencesManager

/**
 * 首页 ViewModel
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SmsMonitorApp
    private val prefs = PreferencesManager(application)

    val recentRecords: LiveData<List<SmsRecord>> = app.smsRepository.getRecentRecords(5)

    private val _monitoringState = MutableLiveData<Boolean>()
    val isMonitoringEnabled: LiveData<Boolean> = _monitoringState

    init {
        _monitoringState.value = prefs.isMonitoringEnabled()
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        prefs.setMonitoringEnabled(enabled)
        _monitoringState.value = enabled
    }
}
