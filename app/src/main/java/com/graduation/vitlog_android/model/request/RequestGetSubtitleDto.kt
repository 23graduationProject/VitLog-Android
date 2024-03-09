package com.graduation.vitlog_android.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestGetSubtitleDto(
    @SerialName("uid")
    val uid : Int,
    @SerialName("fileName")
    val fileName : String
)
