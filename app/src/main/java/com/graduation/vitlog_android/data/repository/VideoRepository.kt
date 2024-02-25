package com.graduation.vitlog_android.data.repository

import com.graduation.vitlog_android.data.api.VideoApi
import com.graduation.vitlog_android.model.response.ResponsePostVideoDto
import okhttp3.MultipartBody
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

}
