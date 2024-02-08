package com.graduation.vitlog_android.util.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import coil.transform.RoundedCornersTransformation

@BindingAdapter("setImageUrl")
fun ImageView.setImageUrl(imageUrl: String) {
    load(imageUrl) {
        transformations(RoundedCornersTransformation(20F))
    }
}
