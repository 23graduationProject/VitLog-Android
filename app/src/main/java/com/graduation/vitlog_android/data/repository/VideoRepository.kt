package com.graduation.vitlog_android.data.repository

import com.graduation.vitlog_android.data.api.VideoApi
import com.graduation.vitlog_android.model.request.RequestBlurDto
import com.graduation.vitlog_android.model.request.RequestGetSubtitleDto
import com.graduation.vitlog_android.model.entity.Subtitle
import com.graduation.vitlog_android.model.request.RequestPostEditedSubtitleDto
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
        ext: String
    ): Result<ResponseGetPresignedUrlDto> = runCatching {
        api.getPresignedUrl(uid = uid, ext = ext)
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
        vid: Int,
        requestBlurDto: MutableList<RequestBlurDto>
    ): Result<ResponseBody> = runCatching {
        api.postManualBlur(uid = uid, vid = vid, requestBlurDto = requestBlurDto)
    }

    suspend fun getSubtitle(
        uid: Int,
        fileName: String
    ): Result<List<Subtitle>> = runCatching {
        api.getSubtitle(uid = uid, fileName = fileName).convertToSubtitle()
    }

    suspend fun postEditedSubtitle(
        uid: Int,
        fileName: String,
        requestPostEditedSubtitleDto : RequestPostEditedSubtitleDto
    ): Result<ResponseBody> = runCatching {
        api.postEditedSubtitle(uid = uid, fileName = fileName, requestPostEditedSubtitleDto = requestPostEditedSubtitleDto)
    }
}

