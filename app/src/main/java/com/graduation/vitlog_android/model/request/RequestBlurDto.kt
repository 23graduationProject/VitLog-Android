package com.graduation.vitlog_android.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestBlurDto(
    @SerialName("start_time")
    val startTime : String,
    @SerialName("end_time")
    val endTime : String,
    @SerialName("x1")
    val x1 : Float,
    @SerialName("y1")
    val y1 : Float,
    @SerialName("x2")
    val x2 : Float,
    @SerialName("y2")
    val y2 : Float
)
