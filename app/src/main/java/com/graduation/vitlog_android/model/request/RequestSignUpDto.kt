package com.graduation.vitlog_android.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestSignUpDto (
    @SerialName("userId")
    val user_id : String,
    @SerialName("password")
    val password : String
)