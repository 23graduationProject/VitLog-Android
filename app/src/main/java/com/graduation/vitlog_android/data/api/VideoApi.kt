package com.graduation.vitlog_android.data.api

import com.graduation.vitlog_android.model.request.RequestBlurDto
import com.graduation.vitlog_android.model.request.RequestLoginDto
import com.graduation.vitlog_android.model.request.RequestPostEditedSubtitleDto
import com.graduation.vitlog_android.model.response.ResponseGetPresignedUrlDto
import com.graduation.vitlog_android.model.response.ResponseGetSubtitleDto
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
import retrofit2.http.Query
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
        @Path("uid") uid: Int,
        @Query("ext") ext: String
    ): ResponseGetPresignedUrlDto

    @PUT
    suspend fun putVideoToPresignedUrl(
        @Url url: String,
        @Body file: RequestBody
    ): ResponseBody

    @GET("api/getMosaicedVideo/{uid}/{fileName}")
    suspend fun getMosaicedVideo(
        @Path("uid") uid: Int,
        @Path("fileName") fileName: String
    ): ResponseBody

    @POST("api/coord/{uid}/{vid}")
    suspend fun postManualBlur(
        @Path("uid") uid: Int,
        @Path("vid") vid: Int,
        @Body requestBlurDto: MutableList<RequestBlurDto>
    ): ResponseBody

    @GET("api/subtitle/{uid}/{fileName}")
    suspend fun getSubtitle(
        @Path("uid") uid: Int,
        @Path("fileName") fileName: String
    ): ResponseGetSubtitleDto

    @POST("api/subtitle-edit/{uid}/{fileName}")
    suspend fun postEditedSubtitle(
        @Path("uid") uid: Int,
        @Path("fileName") fileName: String,
        @Body requestPostEditedSubtitleDto: RequestPostEditedSubtitleDto
    ): ResponseBody
}