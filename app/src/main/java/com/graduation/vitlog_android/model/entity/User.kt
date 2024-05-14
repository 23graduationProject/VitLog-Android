package com.graduation.vitlog_android.model.entity

data class User(
    val uid: String,
    val registeredFace: List<Face>
) {
    fun getFirstPicPath(): String? {
        return if (registeredFace.isNotEmpty()) registeredFace[0].picPath else null
    }
}

data class Face(
    val picPath: String,
    val picName: String
)
