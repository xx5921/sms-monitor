package com.example.smsmonitor.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smsmonitor.databinding.FragmentRecordsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 记录页 Fragment
 */
class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecordsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SmsRecordAdapter()
        binding.recyclerRecords.layoutManager = LinearLayoutManager(context)
        binding.recyclerRecords.adapter = adapter

        // 清空记录按钮
        binding.btnClearRecords.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认")
                .setMessage("确定要清空所有记录吗？")
                .setNegativeButton("取消") { _, _ -> }
                .setPositiveButton("清空") { _, _ -> viewModel.deleteAll() }
                .show()
        }

        // 观察记录列表
        viewModel.records.observe(viewLifecycleOwner) { records ->
            if (records.isNullOrEmpty()) {
                binding.textNoRecords.visibility = View.VISIBLE
                binding.recyclerRecords.visibility = View.GONE
                binding.btnClearRecords.visibility = View.GONE
            } else {
                binding.textNoRecords.visibility = View.GONE
                binding.recyclerRecords.visibility = View.VISIBLE
                binding.btnClearRecords.visibility = View.VISIBLE
                adapter.submitList(records)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
