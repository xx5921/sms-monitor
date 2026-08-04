package com.example.smsmonitor.ui.keywords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smsmonitor.databinding.FragmentKeywordsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 关键词管理页 Fragment
 */
class KeywordsFragment : Fragment() {

    private var _binding: FragmentKeywordsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: KeywordsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeywordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = KeywordAdapter(
            onToggleClick = { keyword -> viewModel.toggleKeyword(keyword) },
            onDeleteClick = { keyword -> viewModel.deleteKeyword(keyword) }
        )

        binding.recyclerKeywords.layoutManager = LinearLayoutManager(context)
        binding.recyclerKeywords.adapter = adapter

        // 添加关键词按钮
        binding.btnAddKeyword.setOnClickListener {
            val text = binding.editKeyword.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.addKeyword(text)
                binding.editKeyword.text?.clear()
            }
        }

        // 清空按钮
        binding.btnDeleteAll.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认")
                .setMessage("确定要清空所有关键词吗？")
                .setNegativeButton("取消") { _, _ -> }
                .setPositiveButton("清空") { _, _ -> viewModel.deleteAllKeywords() }
                .show()
        }

        // 观察关键词列表
        viewModel.keywords.observe(viewLifecycleOwner) { keywords ->
            adapter.submitList(keywords)
            binding.textKeywordCount.text = "共 ${keywords.size} 个关键词"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
