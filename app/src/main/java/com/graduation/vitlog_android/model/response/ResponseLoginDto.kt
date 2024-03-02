package com.graduation.vitlog_android.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseLoginDto(
    @SerialName("success")
    val success : String,
    @SerialName("uid")
    val uid : Int
)