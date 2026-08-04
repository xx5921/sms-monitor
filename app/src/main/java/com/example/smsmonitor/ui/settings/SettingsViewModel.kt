package com.example.smsmonitor.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.smsmonitor.util.PreferencesManager

/**
 * 设置页 ViewModel
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)

    val ringCount = MutableLiveData(prefs.getRingCount())
    val ringInterval = MutableLiveData(prefs.getRingIntervalSeconds())
    val isForceRing = MutableLiveData(prefs.isForceRingEnabled())
    val isVibration = MutableLiveData(prefs.isVibrationEnabled())
    val ringtoneUri = MutableLiveData(prefs.getRingtoneUri())

    fun setRingCount(count: Int) {
        prefs.setRingCount(count)
        ringCount.value = count
    }

    fun setRingInterval(seconds: Int) {
        prefs.setRingIntervalSeconds(seconds)
        ringInterval.value = seconds
    }

    fun setForceRing(enabled: Boolean) {
        prefs.setForceRingEnabled(enabled)
        isForceRing.value = enabled
    }

    fun setVibration(enabled: Boolean) {
        prefs.setVibrationEnabled(enabled)
        isVibration.value = enabled
    }

    fun setRingtoneUri(uri: String) {
        prefs.setRingtoneUri(uri)
        ringtoneUri.value = uri
    }
}
