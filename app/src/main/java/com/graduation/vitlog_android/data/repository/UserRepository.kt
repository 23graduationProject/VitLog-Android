package com.graduation.vitlog_android.data.repository

import com.graduation.vitlog_android.data.api.UserApi
import com.graduation.vitlog_android.model.request.RequestLoginDto
import com.graduation.vitlog_android.model.request.RequestSignUpDto
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun postSignUp(
        requestSignUpDto: RequestSignUpDto
    ): Result<String> = runCatching {
        api.postSignUp(requestSignUpDto)
    }

    suspend fun postLogin(
        requestLoginDto: RequestLoginDto
    ): Result<String> = runCatching {
        api.postLogin(requestLoginDto)
    }
}