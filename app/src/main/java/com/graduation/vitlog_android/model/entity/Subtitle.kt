package com.graduation.vitlog_android.model.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Subtitle(
    @SerialName("timeStamp")
    val timeStamp: String,
    @SerialName("text")
    val text: String,
    @SerialName("startMill")
    val startMill: Int,
    @SerialName("endMill")
    val endMill: Int
)
