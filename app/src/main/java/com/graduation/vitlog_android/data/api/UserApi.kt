package com.graduation.vitlog_android.data.api

import com.graduation.vitlog_android.model.request.RequestLoginDto
import com.graduation.vitlog_android.model.request.RequestSignUpDto
import com.graduation.vitlog_android.model.response.ResponseGetUserDto
import com.graduation.vitlog_android.model.response.ResponseLoginDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {
    @POST("api/user/signup")
    suspend fun postSignUp(
        @Body requestSignUpDto: RequestSignUpDto
    )

    @POST("api/user/login")
    suspend fun postLogin(
        @Body requestLoginDto: RequestLoginDto
    ): ResponseLoginDto

    @GET("api/user/registered-pictures/{uid}")
    suspend fun getUser(
        @Path("uid") uid: Int
    ): ResponseGetUserDto
}