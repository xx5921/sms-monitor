package com.example.smsmonitor.ui.settings

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.smsmonitor.R
import com.example.smsmonitor.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider

/**
 * 设置页 Fragment
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    // 铃声选择 launcher
    private val ringtonePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = getPickedRingtoneUri(result.data)
            if (uri != null) {
                val uriString = if (uri == Settings.System.DEFAULT_RINGTONE_URI) {
                    ""
                } else {
                    uri.toString()
                }
                viewModel.setRingtoneUri(uriString)
                Toast.makeText(requireContext(), "铃声已保存", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRingSettings()
        setupToggles()
        setupRingtoneSelection()
        setupBatteryOptimization()
        setupVersionInfo()
    }

    /**
     * 在设置页底部展示当前应用版本号
     */
    private fun setupVersionInfo() {
        val versionName = try {
            requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
        } catch (e: Exception) {
            null
        }
        binding.textVersion.text = "${getString(R.string.app_name)} v${versionName ?: "未知"}"
    }

    /**
     * 响铃次数和间隔设置
     */
    private fun setupRingSettings() {
        // 响铃次数 Slider
        viewModel.ringCount.observe(viewLifecycleOwner) { count ->
            binding.sliderRingCount.value = count.toFloat()
            binding.textRingCountValue.text = "$count 次"
        }
        binding.sliderRingCount.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setRingCount(value.toInt())
            }
        }

        // 响铃间隔 Slider
        viewModel.ringInterval.observe(viewLifecycleOwner) { interval ->
            binding.sliderRingInterval.value = interval.toFloat()
            binding.textRingIntervalValue.text = "$interval 秒"
        }
        binding.sliderRingInterval.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setRingInterval(value.toInt())
            }
        }
    }

    /**
     * 开关设置
     */
    private fun setupToggles() {
        // 强制响铃
        viewModel.isForceRing.observe(viewLifecycleOwner) { enabled ->
            binding.switchForceRing.isChecked = enabled
        }
        binding.switchForceRing.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setForceRing(isChecked)
        }

        // 振动
        viewModel.isVibration.observe(viewLifecycleOwner) { enabled ->
            binding.switchVibration.isChecked = enabled
        }
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setVibration(isChecked)
        }
    }

    /**
     * 铃声选择
     */
    private fun setupRingtoneSelection() {
        viewModel.ringtoneUri.observe(viewLifecycleOwner) { uri ->
            binding.textRingtoneValue.text = getRingtoneTitle(uri)
        }

        binding.layoutRingtone.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "选择提醒铃声")
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                viewModel.ringtoneUri.value?.let { uri ->
                    if (uri.isNotEmpty()) {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uri))
                    }
                }
            }
            ringtonePicker.launch(intent)
        }

        binding.btnResetRingtone.setOnClickListener {
            viewModel.setRingtoneUri("")
            Toast.makeText(requireContext(), "已恢复系统默认铃声", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 从铃声选择结果中读取铃声 URI。
     *
     * Args:
     *     intent: 铃声选择器返回的结果 Intent。
     *
     * Returns:
     *     用户选择的铃声 URI；未选择时返回 null。
     */
    private fun getPickedRingtoneUri(intent: Intent?): Uri? {
        if (intent == null) return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                Uri::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        }
    }

    /**
     * 获取铃声显示名称。
     *
     * Args:
     *     uri: 铃声 URI 字符串。
     *
     * Returns:
     *     用于设置页展示的铃声名称。
     */
    private fun getRingtoneTitle(uri: String): String {
        if (uri.isEmpty()) return "系统默认"

        return try {
            RingtoneManager.getRingtone(requireContext(), Uri.parse(uri))
                ?.getTitle(requireContext())
                ?: "已选择铃声"
        } catch (e: Exception) {
            "已选择铃声"
        }
    }

    /**
     * 电池优化白名单引导
     */
    private fun setupBatteryOptimization() {
        binding.layoutBatteryOptimization.setOnClickListener {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
