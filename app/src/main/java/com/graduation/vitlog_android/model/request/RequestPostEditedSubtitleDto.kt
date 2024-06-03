package com.graduation.vitlog_android.model.request

import com.graduation.vitlog_android.model.entity.Subtitle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestPostEditedSubtitleDto(
    @SerialName("font")
    val font : String,
    @SerialName("color")
    val color : String,
    @SerialName("subtitle")
    val subtitle : List<Subtitle>,
)
