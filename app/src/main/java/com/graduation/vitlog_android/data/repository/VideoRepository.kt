package com.graduation.vitlog_android.data.repository

import com.graduation.vitlog_android.data.api.VideoApi
import com.graduation.vitlog_android.model.request.RequestBlurDto
import com.graduation.vitlog_android.model.response.ResponseGetPresignedUrlDto
import com.graduation.vitlog_android.model.response.ResponsePostVideoDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject


class VideoRepository @Inject constructor(
    private val api: VideoApi
) {
    suspend fun postVideo(
        uid: Int,
        video: MultipartBody.Part?
    ): Result<ResponsePostVideoDto> = runCatching {
        api.postVideo(uid = uid, file = video)
    }

    suspend fun getPresignedUrl(
        uid: Int,
        fileName: String
    ): Result<ResponseGetPresignedUrlDto> = runCatching {
        api.getPresignedUrl(uid = uid, fileName = fileName)
    }

    suspend fun putVideoToPresignedUrl(
        url: String,
        file: RequestBody
    ): Result<ResponseBody> = runCatching {
        api.putVideoToPresignedUrl(url = url, file = file)
    }

    suspend fun getMosaicedVideo(
        uid: Int,
        fileName: String
    ): Result<ResponseBody> = runCatching {
        api.getMosaicedVideo(uid = uid, fileName = fileName)
    }
    
    suspend fun postManualBlur(
        uid: Int,
        vid: String,
        requestBlurDto: RequestBlurDto
    ): Result<ResponseBody> = runCatching {
        api.postManualBlur(uid = uid, vid = vid, requestBlurDto = requestBlurDto)
    }
}

