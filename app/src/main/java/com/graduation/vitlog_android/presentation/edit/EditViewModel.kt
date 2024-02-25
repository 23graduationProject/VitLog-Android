package com.graduation.vitlog_android.presentation.edit

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graduation.vitlog_android.data.repository.VideoRepository
import com.graduation.vitlog_android.model.response.ResponseGetPresignedUrlDto
import com.graduation.vitlog_android.model.response.ResponsePostVideoDto
import com.graduation.vitlog_android.util.multipart.ContentUriRequestBody
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    val frames = MutableLiveData<List<Bitmap>>()

    private val _postVideoState = MutableStateFlow<UiState<ResponsePostVideoDto>>(UiState.Loading)
    val postVideoState: StateFlow<UiState<ResponsePostVideoDto>> = _postVideoState.asStateFlow()


    private val _getPresignedUrlState =
        MutableStateFlow<UiState<ResponseGetPresignedUrlDto>>(UiState.Loading)
    val getPresignedUrlState: StateFlow<UiState<ResponseGetPresignedUrlDto>> =
        _getPresignedUrlState.asStateFlow()


    fun loadFrames(context: Context, uri: Uri, videoLength: Long) {
        val metaDataSource = MediaMetadataRetriever()
        metaDataSource.setDataSource(context, uri)

        val thumbnailCount = 7
        val interval = videoLength / thumbnailCount

        // 비트맵을 저장할 리스트를 선언합니다.
        val frameBitmaps = ArrayList<Bitmap>()

        for (i in 0 until thumbnailCount - 1) {
            val frameTime = i * interval
            var bitmap =
                metaDataSource.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            // 프레임을 추출하고 비트맵 리스트에 추가합니다.
            bitmap?.let { frameBitmaps.add(it) }
        }
        metaDataSource.release()

        // LiveData 객체를 업데이트합니다.
        frames.value = frameBitmaps
    }


    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> get() = _imageUri

    init {
        _imageUri.value = null
    }

    fun postVideo() {
        viewModelScope.launch {
            _postVideoState.value = UiState.Loading
            val video = createRequestBody()
            videoRepository.postVideo(1, video)
                .onSuccess { response ->
                    _postVideoState.value = UiState.Success(response)
                    Timber.e("성공 $response")
                }.onFailure { t ->
                    if (t is HttpException) {
                        val errorResponse = t.response()?.errorBody()?.string()
                        Timber.e("HTTP 실패: $errorResponse")
                    }
                    _postVideoState.value = UiState.Failure("${t.message}")
                }
        }
    }

    fun getPresignedUrl() {
        viewModelScope.launch {
            _getPresignedUrlState.value = UiState.Loading
            videoRepository.getPresignedUrl(1)
                .onSuccess { response ->
                    _getPresignedUrlState.value = UiState.Success(response)
                    Timber.e("성공 $response")
                }.onFailure { t ->
                    if (t is HttpException) {
                        val errorResponse = t.response()?.errorBody()?.string()
                        Timber.e("HTTP 실패: $errorResponse")
                    }
                    _getPresignedUrlState.value = UiState.Failure("${t.message}")
                }
        }
    }

    private var videoRequestBody: ContentUriRequestBody? = null

    fun setVideoRequestBody(videoRequestBody: ContentUriRequestBody) {
        this.videoRequestBody = videoRequestBody
    }

    private fun createRequestBody(): MultipartBody.Part? {
        return videoRequestBody?.toFormData()
    }


}
