package com.example.smsmonitor.ui.settings

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
            val uriString: String? = result.data?.getStringExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uriString != null) {
                viewModel.setRingtoneUri(uriString)
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
        binding.sliderRingCount.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                viewModel.setRingCount(value.toInt())
            }
        }

        // 响铃间隔 Slider
        viewModel.ringInterval.observe(viewLifecycleOwner) { interval ->
            binding.sliderRingInterval.value = interval.toFloat()
            binding.textRingIntervalValue.text = "$interval 秒"
        }
        binding.sliderRingInterval.addOnChangeListener { slider, value, fromUser ->
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
