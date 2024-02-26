package com.graduation.vitlog_android.model.request

import kotlinx.serialization.SerialName

data class RequestLoginDto(
    @SerialName("userId")
    val user_id : String,
    @SerialName("password")
    val password : String
)
