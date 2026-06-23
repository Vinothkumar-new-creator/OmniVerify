package com.omniverify

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omniverify.databinding.ItemScanHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class ScanHistoryAdapter : ListAdapter<ScanHistory, ScanHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScanHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemScanHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

        fun bind(item: ScanHistory) {
            binding.tvRawContent.text = item.rawContent
            binding.tvTimestamp.text = sdf.format(Date(item.timestamp))
            binding.tvVerdict.text = item.verdict
            
            val context = binding.root.context
            val color = when (item.verdict) {
                "HUMAN", "SAFE" -> ContextCompat.getColor(context, R.color.active_green)
                "PARTIAL_AI", "LIKELY_AI" -> ContextCompat.getColor(context, R.color.result_yellow)
                "AI", "DANGEROUS", "DANGEROUS_SCRIPT" -> ContextCompat.getColor(context, R.color.result_red)
                else -> ContextCompat.getColor(context, R.color.inactive_blue)
            }
            binding.tvVerdict.setTextColor(color)
            
            // Map the verdict text if it's one of our core states
            binding.tvVerdict.text = when (item.verdict) {
                "HUMAN" -> context.getString(R.string.human_made)
                "PARTIAL_AI" -> context.getString(R.string.partial_ai)
                "AI" -> context.getString(R.string.ai_generated)
                else -> item.verdict
            }

            val icon = when (item.scanType) {
                "IMAGE" -> R.drawable.ic_eye
                "QR" -> R.drawable.ic_bolt
                "LINK" -> R.drawable.ic_lock
                else -> R.drawable.ic_shield
            }
            binding.ivScanType.setImageResource(icon)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ScanHistory>() {
        override fun areItemsTheSame(oldItem: ScanHistory, newItem: ScanHistory) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ScanHistory, newItem: ScanHistory) = oldItem == newItem
    }
}