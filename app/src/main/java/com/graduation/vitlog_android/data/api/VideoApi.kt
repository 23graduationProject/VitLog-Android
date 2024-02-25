package com.graduation.vitlog_android.data.api

import com.graduation.vitlog_android.model.response.ResponseGetPresignedUrlDto
import com.graduation.vitlog_android.model.response.ResponsePostVideoDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Url

interface VideoApi {
    @Multipart
    @POST("api/upload/{uid}")
    suspend fun postVideo(
        @Path("uid") uid: Int,
        @Part file: MultipartBody.Part?
    ): ResponsePostVideoDto

    @GET("api/getPreSignedUrl/{uid}")
    suspend fun getPresignedUrl(
        @Path("uid") uid: Int
    ): ResponseGetPresignedUrlDto

    @PUT
    suspend fun putVideoToPresignedUrl(
        @Url url: String,
        @Body file: RequestBody
    ): ResponseBody

}