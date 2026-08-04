package com.example.smsmonitor.util

import android.content.Context
import android.content.SharedPreferences

/**
 * 偏好设置管理器
 * 管理用户配置项的读取和保存
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** 监控是否开启 */
    fun isMonitoringEnabled(): Boolean = prefs.getBoolean(KEY_MONITORING_ENABLED, true)

    fun setMonitoringEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MONITORING_ENABLED, enabled).apply()
    }

    /** 响铃次数（默认3次） */
    fun getRingCount(): Int = prefs.getInt(KEY_RING_COUNT, 3)

    fun setRingCount(count: Int) {
        prefs.edit().putInt(KEY_RING_COUNT, count.coerceIn(1, 10)).apply()
    }

    /** 响铃间隔秒数（默认5秒） */
    fun getRingIntervalSeconds(): Int = prefs.getInt(KEY_RING_INTERVAL, 5)

    fun setRingIntervalSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_RING_INTERVAL, seconds.coerceIn(1, 30)).apply()
    }

    /** 强制响铃（静音模式下也响铃） */
    fun isForceRingEnabled(): Boolean = prefs.getBoolean(KEY_FORCE_RING, false)

    fun setForceRingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FORCE_RING, enabled).apply()
    }

    /** 振动开关 */
    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION, true)

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }

    /** 铃声 URI */
    fun getRingtoneUri(): String = prefs.getString(KEY_RINGTONE_URI, "") ?: ""

    fun setRingtoneUri(uri: String) {
        prefs.edit().putString(KEY_RINGTONE_URI, uri).apply()
    }

    companion object {
        private const val PREFS_NAME = "sms_monitor_prefs"
        private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
        private const val KEY_RING_COUNT = "ring_count"
        private const val KEY_RING_INTERVAL = "ring_interval"
        private const val KEY_FORCE_RING = "force_ring"
        private const val KEY_VIBRATION = "vibration"
        private const val KEY_RINGTONE_URI = "ringtone_uri"
    }
}
