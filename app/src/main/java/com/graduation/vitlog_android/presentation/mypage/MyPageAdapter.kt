package com.graduation.vitlog_android.presentation.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.graduation.vitlog_android.databinding.ItemMypageRegisteredFaceBinding
import com.graduation.vitlog_android.model.entity.Face
import com.graduation.vitlog_android.util.view.ItemDiffCallback

class MyPageAdapter() : ListAdapter<Face, MyPageAdapter.RegisteredFaceViewHolder>(diffUtil) {


    class RegisteredFaceViewHolder(private val binding: ItemMypageRegisteredFaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Face) {
            binding.data = data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegisteredFaceViewHolder {
        val binding =
            ItemMypageRegisteredFaceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return RegisteredFaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RegisteredFaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val diffUtil = ItemDiffCallback<Face>(
            onItemsTheSame = { old, new -> old.picName == new.picName },
            onContentsTheSame = { old, new -> old == new }
        )
    }
}
