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
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.smsmonitor.util.NotificationHelper
import com.example.smsmonitor.util.PreferencesManager

/**
 * 多次响铃提醒服务
 * 匹配到关键词后，响铃多次并显示通知
 */
class AlertService : Service() {

    companion object {
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

    // 保存原始音量，用于恢复
    private var originalVolume = 0
    private var audioManager: AudioManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

        // 开始多次响铃
        startRepeatedRinging()

        return START_NOT_STICKY
    }

    /**
     * 开始多次响铃
     */
    private fun startRepeatedRinging() {
        currentRingCount = 0
        val totalRings = prefs.getRingCount()
        val intervalMs = prefs.getRingIntervalSeconds() * 1000L

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
     * 播放一次铃声
     */
    private fun playRing() {
        Log.d(TAG, "播放铃声 #${currentRingCount + 1}")

        // 如果开启强制响铃，调大音量
        if (prefs.isForceRingEnabled()) {
            audioManager?.let { am ->
                originalVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
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
                        audioManager?.setStreamVolume(
                            AudioManager.STREAM_MUSIC, originalVolume, 0
                        )
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
        // 恢复音量
        if (prefs.isForceRingEnabled()) {
            audioManager?.setStreamVolume(
                AudioManager.STREAM_MUSIC, originalVolume, 0
            )
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
