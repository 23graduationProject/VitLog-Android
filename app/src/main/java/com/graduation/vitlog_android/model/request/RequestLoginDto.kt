package com.graduation.vitlog_android.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestLoginDto(
    @SerialName("user_id")
    val user_id : String,
    @SerialName("password")
    val password : String
)
