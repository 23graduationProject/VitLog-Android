package com.graduation.vitlog_android.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponsePostFaceDto(

    @SerialName("status")
    val status: Int,
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: FaceData

)

@Serializable
data class FaceData(
    @SerialName("fileName")
    val fileName: String,
    @SerialName("pictureName")
    val pictureName: String,
    @SerialName("imgUrl")
    val imgUrl: String
)
