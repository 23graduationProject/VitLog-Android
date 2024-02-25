package com.graduation.vitlog_android.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetPresignedUrlDto(

    @SerialName("status")
    val status: Int,
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: Data

)

@Serializable
data class Data(
    @SerialName("url")
    val url: String
)
