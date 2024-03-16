package com.graduation.vitlog_android.presentation.edit

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graduation.vitlog_android.data.repository.VideoRepository
import com.graduation.vitlog_android.model.request.RequestBlurDto
import com.graduation.vitlog_android.model.entity.Subtitle
import com.graduation.vitlog_android.model.response.ResponseGetPresignedUrlDto
import com.graduation.vitlog_android.model.response.ResponseGetSubtitleDto
import com.graduation.vitlog_android.model.response.ResponsePostVideoDto
import com.graduation.vitlog_android.util.multipart.ContentUriRequestBody
import com.graduation.vitlog_android.util.preference.SharedPrefManager.uid
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    val frames = MutableLiveData<List<Bitmap>>()

    private val _putVideoToPresignedUrlState =
        MutableStateFlow<UiState<ResponseBody>>(UiState.Empty)
    val putVideoToPresignedUrlState: StateFlow<UiState<ResponseBody>> =
        _putVideoToPresignedUrlState.asStateFlow()

    private val _getPresignedUrlState =
        MutableStateFlow<UiState<ResponseGetPresignedUrlDto>>(UiState.Empty)
    val getPresignedUrlState: StateFlow<UiState<ResponseGetPresignedUrlDto>> =
        _getPresignedUrlState.asStateFlow()

    private val _getMosaicedVideoState = MutableStateFlow<UiState<ResponseBody>>(UiState.Empty)
    val getMosaicedVideoState: StateFlow<UiState<ResponseBody>> =
        _getMosaicedVideoState.asStateFlow()

    var _getSubtitleState = MutableStateFlow<UiState<List<Subtitle>>>(UiState.Empty)
    val getSubtitleState: StateFlow<UiState<List<Subtitle>>> = _getSubtitleState.asStateFlow()
    val timeLineImages = mutableListOf<Bitmap>()

    var _subtitleList = listOf<Subtitle>()
    val subtitleList: List<Subtitle> = _subtitleList

    fun saveSubtitleList(subtitle: List<Subtitle>){
        _subtitleList = subtitle
    }
    private val _postManualBlurState = MutableStateFlow<UiState<ResponseBody>>(UiState.Loading)
    val postManualBlurState: StateFlow<UiState<ResponseBody>> = _postManualBlurState.asStateFlow()

    fun loadFrames(context: Context, uri: Uri, videoLength: Long) {
        val metaDataSource = MediaMetadataRetriever()
        metaDataSource.setDataSource(context, uri)

        val durationString = metaDataSource.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val duration = durationString?.toLong() ?: 0

        val frameRate = 1 // 프레임 속도 (1초 당 프레임 수)
        val numFrames = (duration / 1000) * frameRate // 영상의 총 프레임 수

        for (i in 0 until numFrames) {
            val timeUs = i * 1000000 / frameRate // 프레임의 시간(마이크로초)을 계산합니다.
            val bitmap = metaDataSource.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            // 비트맵(bitmap)을 사용하여 작업을 수행합니다.
            // 예를 들어, 프레임을 화면에 표시하거나 파일로 저장할 수 있습니다.
            bitmap?.let { timeLineImages.add(it) }
        }

        metaDataSource.release()

        // LiveData 객체를 업데이트합니다.
//        frames.value = frameBitmaps
    }


    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> get() = _imageUri

    init {
        _imageUri.value = null
    }


    fun getPresignedUrl() {
        viewModelScope.launch {
            _getPresignedUrlState.value = UiState.Loading
            videoRepository.getPresignedUrl(uid, "mp4")
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


    fun getSubtitle(
        uid: Int,
        fileName: String
    ) {
        viewModelScope.launch {
            _getSubtitleState.value = UiState.Loading
            videoRepository.getSubtitle(uid, fileName)
                .onSuccess { response ->
                    _getSubtitleState.value = UiState.Success(response)
                    Timber.e("성공 $response")
                }.onFailure { t ->
                    if (t is HttpException) {
                        val errorResponse = t.response()?.errorBody()?.string()
                        Timber.e("HTTP 실패: $errorResponse")
                    }
                    _getSubtitleState.value = UiState.Failure("${t.message}")
                }
        }
    }

    fun postManualBlur(
        uid: Int,
        vid: String,
        requestBlurDto: RequestBlurDto
    ) {
        viewModelScope.launch {
            _postManualBlurState.value = UiState.Loading
            videoRepository.postManualBlur(uid, vid, requestBlurDto)
                .onSuccess { response ->
                    _postManualBlurState.value = UiState.Success(response)
                    Timber.e("성공 $response")
                }.onFailure { t ->
                    if (t is HttpException) {
                        val errorResponse = t.response()?.errorBody()?.string()
                        Timber.e("HTTP 실패: $errorResponse")
                    }
                    _postManualBlurState.value = UiState.Failure("${t.message}")
                }
        }
    }

    suspend fun uriToRequestBody(context: Context, uri: Uri) {
        val requestBody = withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val byteArray = inputStream?.readBytes()
            inputStream?.close()
            byteArray?.toRequestBody()
                ?: throw IOException("Cannot create RequestBody from the provided Uri.")
        }
        putVideoToPresignedUrl(url = presignedUrl.value, requestBody = requestBody)
    }

    val presignedUrl: StateFlow<String> get() = _presignedUrl
    private val _presignedUrl = MutableStateFlow("")

    fun setPresignedUrl(url: String) {
        _presignedUrl.value = url
    }

    private val _videoFileName = MutableStateFlow<String?>("")
    val videoFileName: StateFlow<String?> get() = _videoFileName

    fun setVideoFileName(fileName: String) {
        _videoFileName.value = fileName
    }

    init {
        _imageUri.value = null
    }

    private fun putVideoToPresignedUrl(
        url: String,
        requestBody: RequestBody
    ) {
        viewModelScope.launch {
            _putVideoToPresignedUrlState.value = UiState.Loading
            videoRepository.putVideoToPresignedUrl(url, requestBody)
                .onSuccess { response ->
                    _putVideoToPresignedUrlState.value = UiState.Success(response)
                    Timber.e("성공 $response")
                }.onFailure { t ->
                    if (t is HttpException) {
                        val errorResponse = t.response()?.errorBody()?.string()
                        Timber.e("HTTP 실패: $errorResponse")
                    }
                    _putVideoToPresignedUrlState.value = UiState.Failure("${t.message}")
                }
        }
    }

    fun getMosaicedVideo(
        uid: Int,
        fileName: String
    ) {
        viewModelScope.launch {
            _getMosaicedVideoState.value = UiState.Loading
            videoRepository.getMosaicedVideo(uid, fileName)
                .onSuccess { response ->
                    _getMosaicedVideoState.value = UiState.Success(response)
                    Timber.e("성공 $response")
                }.onFailure { t ->
                    if (t is HttpException) {
                        val errorResponse = t.response()?.errorBody()?.string()
                        Timber.e("HTTP 실패: $errorResponse")
                    }
                    _getMosaicedVideoState.value = UiState.Failure("${t.message}")
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


    fun saveFile(context: Context, body: ResponseBody?) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)
            val fileSize = body?.contentLength()
            var fileSizeDownloaded: Long = 0
            inputStream = body?.byteStream()
            outputStream = FileOutputStream(File("path/to/your/file"))

            while (true) {
                val read = inputStream?.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read!!)
                fileSizeDownloaded += read.toLong()
            }

            outputStream.flush()

        } catch (e: IOException) {
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
        writeResponseBodyToDisk(context, body)
    }

}


private fun writeResponseBodyToDisk(context: Context, body: ResponseBody?): Boolean {
    return try {
        // 저장할 파일의 경로 지정
        val filePath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/your_video_name.mp4"
        val videoFile = File(filePath)

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)
            val fileSize = body?.contentLength()
            var fileSizeDownloaded: Long = 0
            inputStream = body?.byteStream()
            outputStream = FileOutputStream(videoFile)

            while (true) {
                val read = inputStream?.read(fileReader) ?: -1

                if (read == -1) {
                    break
                }

                outputStream.write(fileReader, 0, read)
                fileSizeDownloaded += read.toLong()
            }

            outputStream.flush()

            // 미디어 스캔 진행
            MediaScannerConnection.scanFile(
                context,
                arrayOf(videoFile.absolutePath),
                arrayOf("video/mp4")
            ) { path, uri ->
                Timber.e("갤러리 저장 성공")
            }

            true
        } catch (e: IOException) {
            false
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    } catch (e: IOException) {
        false
    }
}
