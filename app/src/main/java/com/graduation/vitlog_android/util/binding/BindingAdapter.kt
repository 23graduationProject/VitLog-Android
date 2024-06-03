package com.graduation.vitlog_android.util.binding

import android.net.Uri
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


@BindingAdapter("setUploadImageUri")
fun ImageView.setUploadImageUri(imageUri: Uri?) {
    if (imageUri == null) {
        load(R.drawable.ic_addface_gallery)
    } else {

        load(imageUri) {
            placeholder(R.drawable.ic_addface_gallery)
            transformations(RoundedCornersTransformation(20F))
        }

    }
}

