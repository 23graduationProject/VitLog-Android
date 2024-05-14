package com.graduation.vitlog_android.util.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import coil.transform.RoundedCornersTransformation
import com.graduation.vitlog_android.R

@BindingAdapter("setImageUrl")
fun ImageView.setImageUrl(imageUrl: String?) {
    if (imageUrl != null) {
        load(imageUrl) {
            transformations(RoundedCornersTransformation(200F))
        }
    } else {
        load(R.drawable.img_mypage_profile)
    }
}

