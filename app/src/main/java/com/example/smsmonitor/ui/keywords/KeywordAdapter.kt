package com.example.smsmonitor.ui.keywords

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smsmonitor.data.model.Keyword
import com.example.smsmonitor.databinding.ItemKeywordBinding

/**
 * 关键词列表适配器
 */
class KeywordAdapter(
    private val onToggleClick: (Keyword) -> Unit,
    private val onDeleteClick: (Keyword) -> Unit
) : ListAdapter<Keyword, KeywordAdapter.KeywordViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val binding = ItemKeywordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return KeywordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class KeywordViewHolder(
        private val binding: ItemKeywordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(keyword: Keyword) {
            binding.textKeyword.text = keyword.text
            // 先置空 listener 再设置 checked 状态，避免 setChecked 触发回调导致不必要的数据库写入
            binding.switchEnabled.setOnCheckedChangeListener(null)
            binding.switchEnabled.isChecked = keyword.isEnabled
            binding.switchEnabled.setOnCheckedChangeListener { _, _ ->
                onToggleClick(keyword)
            }
            binding.btnDelete.setOnClickListener {
                onDeleteClick(keyword)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Keyword>() {
            override fun areItemsTheSame(oldItem: Keyword, newItem: Keyword): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Keyword, newItem: Keyword): Boolean =
                oldItem == newItem
        }
    }
}
