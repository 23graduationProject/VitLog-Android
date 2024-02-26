package com.graduation.vitlog_android.data.api

import com.graduation.vitlog_android.model.request.RequestLoginDto
import com.graduation.vitlog_android.model.request.RequestSignUpDto
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("api/user/signup")
    suspend fun postSignUp(
        @Body requestSignUpDto : RequestSignUpDto
    ): String

    @POST("api/user/login")
    suspend fun postLogin(
        @Body requestLoginDto : RequestLoginDto
    ): String
}