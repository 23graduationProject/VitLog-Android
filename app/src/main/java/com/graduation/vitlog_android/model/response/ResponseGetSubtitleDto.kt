package com.graduation.vitlog_android.model.response

import com.graduation.vitlog_android.util.number.TimeUtil.formatTimeStamp
import com.graduation.vitlog_android.util.number.TimeUtil.toMillis
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
) {
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

    fun convertToSubtitle() = this.data.subtitle.map {
        com.graduation.vitlog_android.model.entity.Subtitle(
            timeStamp = formatTimeStamp(it.start, it.end),
            text = it.text,
            startMill = it.start.toMillis(),
            endMill = it.end.toMillis()
        )
    }
}
