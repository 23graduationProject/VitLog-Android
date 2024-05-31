package com.graduation.vitlog_android.presentation.home

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.graduation.vitlog_android.databinding.ItemGalleryImageBinding

class GalleryAdapter(
    private val context: Context,
    private val imageUris: List<Uri>,
    private val onItemClicked: (Uri) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.GalleryImageViewHolder>() {

    class GalleryImageViewHolder(private val binding: ItemGalleryImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, uri: Uri, onItemClicked: (Uri) -> Unit) {
            // 썸네일 불러오기
            val bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                context.contentResolver,
                ContentUris.parseId(uri),
                MediaStore.Video.Thumbnails.MINI_KIND,
                null
            )
            binding.image.setImageBitmap(bitmap)

            // 클릭 이벤트 처리
            itemView.setOnClickListener {
                onItemClicked(uri)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryImageViewHolder {
        val binding =
            ItemGalleryImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryImageViewHolder, position: Int) {
        holder.bind(context, imageUris[position], onItemClicked)
    }

    override fun getItemCount(): Int = imageUris.size
}

