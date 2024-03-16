package com.graduation.vitlog_android.presentation.edit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.graduation.vitlog_android.databinding.ItemEditSubtitleBinding
import com.graduation.vitlog_android.model.entity.Subtitle
import com.graduation.vitlog_android.util.view.ItemDiffCallback

class SubtitleAdapter(
) : ListAdapter<Subtitle, SubtitleAdapter.SubtitleViewHolder>(diffUtil) {

    class SubtitleViewHolder(
        private val binding: ItemEditSubtitleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: Subtitle) {
            binding.apply {
                this.data = data

                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubtitleViewHolder {
        val binding =
            ItemEditSubtitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubtitleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubtitleViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    companion object {
        private val diffUtil = ItemDiffCallback<Subtitle>(
            onItemsTheSame = { old, new -> old.timeStamp == new.timeStamp },
            onContentsTheSame = { old, new -> old == new }
        )
    }
}