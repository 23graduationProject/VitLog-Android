package com.graduation.vitlog_android.model.response

import com.graduation.vitlog_android.model.entity.Face
import com.graduation.vitlog_android.model.entity.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetUserDto(
    @SerialName("data")
    val data: Data,
    @SerialName("status")
    val status: Int,
    @SerialName("success")
    val success: Boolean
) {
    @Serializable
    data class Data(
        @SerialName("uid")
        val uid: String,
        @SerialName("registeredFaceList")
        val registeredFaceList: List<Face>
    )

    @Serializable
    data class Face(
        @SerialName("picName")
        val picName: String,
        @SerialName("picPath")
        val picPath: String
    )

    fun convertToUser() : User {
       return  User(
            uid = data.uid,
            registeredFace = data.registeredFaceList.map {
                com.graduation.vitlog_android.model.entity.Face(
                    picName = it.picName,
                    picPath = it.picPath
                )
            }
        )
    }
}
