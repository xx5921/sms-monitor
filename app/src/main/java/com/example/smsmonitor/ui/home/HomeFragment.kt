package com.example.smsmonitor.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smsmonitor.databinding.FragmentHomeBinding
import com.example.smsmonitor.ui.records.SmsRecordAdapter

/**
 * 首页 Fragment
 * 显示监控开关和最近匹配的短信
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 监控开关
        binding.switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMonitoringEnabled(isChecked)
            if (isChecked) {
                binding.textStatus.text = "监控运行中"
                binding.textStatus.setTextColor(
                    resources.getColor(android.R.color.holo_green_dark, null)
                )
            } else {
                binding.textStatus.text = "监控已停止"
                binding.textStatus.setTextColor(
                    resources.getColor(android.R.color.darker_gray, null)
                )
            }
        }

        // 观察监控状态
        viewModel.isMonitoringEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.switchMonitoring.isChecked = enabled
        }

        // 最近记录列表
        val adapter = SmsRecordAdapter()
        binding.recyclerRecentRecords.layoutManager = LinearLayoutManager(context)
        binding.recyclerRecentRecords.adapter = adapter

        viewModel.recentRecords.observe(viewLifecycleOwner) { records ->
            if (records.isNullOrEmpty()) {
                binding.textNoRecords.visibility = View.VISIBLE
                binding.recyclerRecentRecords.visibility = View.GONE
            } else {
                binding.textNoRecords.visibility = View.GONE
                binding.recyclerRecentRecords.visibility = View.VISIBLE
                adapter.submitList(records)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
