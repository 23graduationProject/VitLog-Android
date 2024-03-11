package com.graduation.vitlog_android.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetSubtitleDto(
    @SerialName("data")
    val data: SubData,
    @SerialName("status")
    val status: Int,
    @SerialName("success")
    val success: Boolean
)

@Serializable
data class SubData(
    @SerialName("subtitle")
    val subtitle: List<Subtitle>
)

@Serializable
data class Subtitle(
    @SerialName("end")
    val end: Double,
    @SerialName("start")
    val start: Double,
    @SerialName("text")
    val text: String
)