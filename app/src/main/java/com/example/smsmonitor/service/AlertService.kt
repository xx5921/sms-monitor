package com.example.smsmonitor.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.smsmonitor.AlertActivity
import com.example.smsmonitor.util.NotificationHelper
import com.example.smsmonitor.util.PreferencesManager

/**
 * 多次响铃提醒服务
 * 匹配到关键词后，响铃多次并显示通知
 */
class AlertService : Service() {

    companion object {
        const val ACTION_STOP_ALERT = "com.example.smsmonitor.action.STOP_ALERT"
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_MATCHED_KEYWORDS = "extra_matched_keywords"
        private const val TAG = "AlertService"
        private const val NOTIFICATION_ID = 1001
    }

    private lateinit var prefs: PreferencesManager
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var currentRingCount = 0
    private var timer: CountDownTimer? = null

    // 唤醒锁，防止设备休眠中断响铃
    private var wakeLock: PowerManager.WakeLock? = null

    // 保存原始音量，用于恢复
    private var originalVolume = 0
    private var isVolumeBoosted = false
    private var audioManager: AudioManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALERT) {
            Log.d(TAG, "用户停止响铃提醒")
            stopAndCleanup()
            return START_NOT_STICKY
        }

        val sender = intent?.getStringExtra(EXTRA_SENDER) ?: "未知号码"
        val content = intent?.getStringExtra(EXTRA_CONTENT) ?: ""
        val matchedKeywords = intent?.getStringExtra(EXTRA_MATCHED_KEYWORDS) ?: ""

        Log.d(TAG, "启动响铃提醒: sender=$sender, keywords=$matchedKeywords")

        // 启动前台通知
        val notification = NotificationHelper.createForegroundNotification(
            this, sender, content
        )
        startForeground(NOTIFICATION_ID, notification)

        // 发送高优先级通知
        NotificationHelper.sendAlertNotification(this, sender, content, matchedKeywords)

        // 尝试显示带停止按钮的提醒弹窗
        showAlertPopup(sender, content, matchedKeywords)

        // 开始多次响铃
        startRepeatedRinging()

        return START_NOT_STICKY
    }

    /**
     * 开始多次响铃
     */
    private fun startRepeatedRinging() {
        timer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        currentRingCount = 0
        val totalRings = prefs.getRingCount()
        val intervalMs = prefs.getRingIntervalSeconds() * 1000L

        // 获取唤醒锁，确保设备休眠期间也能正常响铃
        acquireWakeLock()

        playRing()

        // 定时响铃
        timer = object : CountDownTimer(intervalMs * totalRings, intervalMs) {
            override fun onTick(millisUntilFinished: Long) {
                currentRingCount++
                if (currentRingCount < totalRings) {
                    playRing()
                }
            }

            override fun onFinish() {
                Log.d(TAG, "响铃完成")
                stopAndCleanup()
            }
        }.start()
    }

    /**
     * 尝试显示响铃提醒弹窗。
     *
     * Args:
     *     sender: 短信发送方。
     *     content: 短信内容。
     *     matchedKeywords: 命中的关键词。
     */
    private fun showAlertPopup(sender: String, content: String, matchedKeywords: String) {
        try {
            startActivity(
                AlertActivity.createIntent(this, sender, content, matchedKeywords)
            )
        } catch (e: Exception) {
            Log.w(TAG, "显示响铃弹窗失败，保留通知按钮作为兜底", e)
        }
    }

    /**
     * 播放一次铃声
     */
    private fun playRing() {
        Log.d(TAG, "播放铃声 #${currentRingCount + 1}")

        // 如果开启强制响铃，调大音量
        if (prefs.isForceRingEnabled()) {
            audioManager?.let { am ->
                if (!isVolumeBoosted) {
                    originalVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                    isVolumeBoosted = true
                }
                am.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0
                )
            }
        }

        // 播放铃声
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                val ringtoneUri = prefs.getRingtoneUri()
                val uri = if (ringtoneUri.isNotEmpty()) {
                    Uri.parse(ringtoneUri)
                } else {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }
                setDataSource(this@AlertService, uri)
                setWakeMode(this@AlertService, PowerManager.PARTIAL_WAKE_LOCK)

                if (prefs.isForceRingEnabled()) {
                    // 强制响铃：绕过静音模式
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                } else {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }

                isLooping = false
                setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                    // 恢复音量
                    if (prefs.isForceRingEnabled()) {
                        restoreVolume()
                    }
                }
                prepare()
                start()
            } catch (e: Exception) {
                Log.e(TAG, "播放铃声失败", e)
                release()
                mediaPlayer = null
            }
        }

        // 振动
        if (prefs.isVibrationEnabled()) {
            vibrate()
        }
    }

    /**
     * 振动提醒
     */
    private fun vibrate() {
        val pattern = longArrayOf(0, 500, 200, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        }
    }

    /**
     * 停止并清理
     */
    private fun stopAndCleanup() {
        timer?.cancel()
        timer = null
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        restoreVolume()
        releaseWakeLock()
        NotificationManagerCompat.from(this).cancel(NotificationHelper.NOTIFICATION_ID_ALERT)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * 恢复强制响铃前的媒体音量。
     */
    private fun restoreVolume() {
        if (!isVolumeBoosted) return

        audioManager?.setStreamVolume(
            AudioManager.STREAM_MUSIC, originalVolume, 0
        )
        isVolumeBoosted = false
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        restoreVolume()
        releaseWakeLock()
    }

    /**
     * 获取 CPU 唤醒锁，确保设备休眠期间提醒服务正常运行。
     */
    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sms-monitor:AlertWakeLock").apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 最长持有 10 分钟
            }
        }
    }

    /**
     * 释放唤醒锁。
     */
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "释放唤醒锁失败", e)
        }
        wakeLock = null
    }
}
