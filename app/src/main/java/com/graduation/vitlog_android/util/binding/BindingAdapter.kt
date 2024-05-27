package com.graduation.vitlog_android.util.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import coil.transform.RoundedCornersTransformation
import com.graduation.vitlog_android.R

@BindingAdapter("setImageUrl")
fun ImageView.setImageUrl(imageUrl: String?) {
    if (imageUrl != null) {
        if (imageUrl.startsWith("http")) {
            // URL인 경우
            load(imageUrl) {
                transformations(RoundedCornersTransformation(200F))
            }
        } else {
            // 로컬 리소스인 경우
            val resourceId = context.resources.getIdentifier(
                imageUrl, "drawable", context.packageName
            )
            if (resourceId != 0) {
                load(resourceId) {
                    transformations(RoundedCornersTransformation(200F))
                }
            } else {
                load(R.drawable.img_mypage_profile)
            }
        }
    } else {
        load(R.drawable.img_mypage_profile)
    }
}


