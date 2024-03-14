package com.graduation.vitlog_android.presentation.edit

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.graduation.vitlog_android.databinding.ItemTimeLineImageBinding

class TimeLineAdapter(private val images: List<Bitmap>): RecyclerView.Adapter<TimeLineAdapter.TimeLIneViewHolder>() {

    class TimeLIneViewHolder(private val binding: ItemTimeLineImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(img: Bitmap) {
            binding.image.setImageBitmap(img)
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