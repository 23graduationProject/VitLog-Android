package com.graduation.vitlog_android.presentation.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.graduation.vitlog_android.databinding.ItemGalleryImageBinding

class GalleryAdapter(private val imageUris: List<Uri>) :
    RecyclerView.Adapter<GalleryAdapter.GalleryImageViewHolder>() {

    class GalleryImageViewHolder(private val binding: ItemGalleryImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            binding.image.setImageURI(uri)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryImageViewHolder {
        val binding =
            ItemGalleryImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryImageViewHolder, position: Int) {
        holder.bind(imageUris[position])
    }

    override fun getItemCount(): Int = imageUris.size
}
