package com.graduation.vitlog_android.data.repository

import com.graduation.vitlog_android.data.api.UserApi
import com.graduation.vitlog_android.model.entity.User
import com.graduation.vitlog_android.model.request.RequestLoginDto
import com.graduation.vitlog_android.model.request.RequestSignUpDto
import com.graduation.vitlog_android.model.response.ResponseLoginDto
import okhttp3.MultipartBody
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun postSignUp(
        requestSignUpDto: RequestSignUpDto
    ): Result<Unit> = runCatching {
        api.postSignUp(requestSignUpDto)
    }

    suspend fun postLogin(
        requestLoginDto: RequestLoginDto
    ): Result<ResponseLoginDto> = runCatching {
        api.postLogin(requestLoginDto)
    }

    suspend fun getUser(
        uid: Int
    ): Result<User> = runCatching {
        api.getUser(uid).convertToUser()
    }

    suspend fun postFace(
        uid: Int,
        file: MultipartBody.Part?,
        pName: String
    ): Result<Unit> = runCatching {
        api.postFace(uid, pName = pName, file = file)
    }
}