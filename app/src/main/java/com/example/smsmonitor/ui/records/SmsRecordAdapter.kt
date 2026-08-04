package com.example.smsmonitor.ui.records

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smsmonitor.data.model.SmsRecord
import com.example.smsmonitor.databinding.ItemSmsRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 短信记录列表适配器
 */
class SmsRecordAdapter :
    ListAdapter<SmsRecord, SmsRecordAdapter.SmsRecordViewHolder>(DIFF_CALLBACK) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsRecordViewHolder {
        val binding = ItemSmsRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SmsRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SmsRecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SmsRecordViewHolder(
        private val binding: ItemSmsRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: SmsRecord) {
            binding.textSender.text = record.sender
            binding.textContent.text = record.content
            binding.textTime.text = dateFormat.format(Date(record.receivedAt))
            binding.textMatchedKeywords.text = "关键词: ${record.matchedKeywords}"
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SmsRecord>() {
            override fun areItemsTheSame(oldItem: SmsRecord, newItem: SmsRecord): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SmsRecord, newItem: SmsRecord): Boolean =
                oldItem == newItem
        }
    }
}
