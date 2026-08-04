package com.example.smsmonitor

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.smsmonitor.databinding.ActivityAlertBinding
import com.example.smsmonitor.service.AlertService

/**
 * 响铃提醒弹窗 Activity。
 */
class AlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureWindow()

        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sender = intent.getStringExtra(EXTRA_SENDER).orEmpty()
        val content = intent.getStringExtra(EXTRA_CONTENT).orEmpty()
        val matchedKeywords = intent.getStringExtra(EXTRA_MATCHED_KEYWORDS).orEmpty()

        binding.textAlertSender.text = sender.ifEmpty { "未知号码" }
        binding.textAlertKeywords.text = matchedKeywords.ifEmpty { "关键词匹配" }
        binding.textAlertContent.text = content
        binding.btnStopAlert.setOnClickListener {
            stopAlertService()
            finish()
        }
        binding.btnOpenApp.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    /**
     * 配置弹窗在锁屏和亮屏场景下的展示行为。
     */
    private fun configureWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    /**
     * 停止当前响铃提醒服务。
     */
    private fun stopAlertService() {
        val intent = Intent(this, AlertService::class.java).apply {
            action = AlertService.ACTION_STOP_ALERT
        }
        startService(intent)
    }

    companion object {
        private const val EXTRA_SENDER = "extra_sender"
        private const val EXTRA_CONTENT = "extra_content"
        private const val EXTRA_MATCHED_KEYWORDS = "extra_matched_keywords"

        /**
         * 创建响铃提醒弹窗启动 Intent。
         *
         * Args:
         *     context: 上下文对象。
         *     sender: 短信发送方。
         *     content: 短信内容。
         *     matchedKeywords: 命中的关键词。
         *
         * Returns:
         *     用于启动提醒弹窗的 Intent。
         */
        fun createIntent(
            context: Context,
            sender: String,
            content: String,
            matchedKeywords: String
        ): Intent {
            return Intent(context, AlertActivity::class.java).apply {
                putExtra(EXTRA_SENDER, sender)
                putExtra(EXTRA_CONTENT, content)
                putExtra(EXTRA_MATCHED_KEYWORDS, matchedKeywords)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }
}
