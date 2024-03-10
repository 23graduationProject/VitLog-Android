package com.graduation.vitlog_android.presentation.edit

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.graduation.vitlog_android.databinding.ItemTimeLineImageBinding

class TimeLineAdapter(private val images: List<Int>): RecyclerView.Adapter<TimeLineAdapter.TimeLIneViewHolder>() {

    class TimeLIneViewHolder(private val binding: ItemTimeLineImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(img: Int) {
            binding.image.setImageResource(img)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLIneViewHolder {
        val binding =
            ItemTimeLineImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeLIneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeLIneViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size
}