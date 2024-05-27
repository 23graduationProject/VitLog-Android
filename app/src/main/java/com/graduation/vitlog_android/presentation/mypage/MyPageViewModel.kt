package com.graduation.vitlog_android.presentation.mypage

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graduation.vitlog_android.data.repository.UserRepository
import com.graduation.vitlog_android.model.entity.User
import com.graduation.vitlog_android.util.multipart.ContentUriRequestBody
import com.graduation.vitlog_android.util.preference.SharedPrefManager.uid
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
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _getUserState =
        MutableStateFlow<UiState<User>>(UiState.Empty)
    val geUserState: StateFlow<UiState<User>> =
        _getUserState.asStateFlow()

    private val _postFaceState = MutableStateFlow<UiState<Unit>>(UiState.Empty)

    val postFaceState: StateFlow<UiState<Unit>> get() = _postFaceState.asStateFlow()

    private var imageRequestBody: ContentUriRequestBody? = null

    private fun createRequestBody(): MultipartBody.Part? {
        return imageRequestBody?.toFormData()
    }

    private fun updateRequestBody(requestBody: ContentUriRequestBody) {
        this.imageRequestBody = requestBody
    }

    private val _faceUri = MutableStateFlow<Uri?>(null)
    val faceUri: StateFlow<Uri?> = _faceUri.asStateFlow()

    fun updateFaceUri(uri: Uri, context: Context) = viewModelScope.launch {
        _faceUri.value = uri
        val uri = faceUri.value ?: return@launch
        val requestBody = ContentUriRequestBody(context, uri)
        updateRequestBody(requestBody)
        postFace()
    }

    private fun postFace() {
        viewModelScope.launch {
            _postFaceState.value = UiState.Loading
            val image = createRequestBody()
            val uid = uid
            val pName = "hyeseon"
            userRepository.postFace(uid = uid, pName = pName, file = image).onSuccess { response ->
                _postFaceState.value = UiState.Success(response)
                getUser()
                Timber.d("성공 $response")
            }.onFailure { t ->
                if (t is HttpException) {
                    val errorResponse = t.response()?.errorBody()?.string()
                    Timber.e("HTTP 실패: $errorResponse")
                }
                _postFaceState.value = UiState.Failure("${t.message}")
            }
        }
    }

    fun getUser() {
        viewModelScope.launch {
            _getUserState.value = UiState.Loading
            userRepository.getUser(uid = uid)
                .onSuccess { response ->
                    _getUserState.value = UiState.Success(response)
                    Timber.d("성공 $response")
                }.onFailure { t ->
                    if (t is HttpException) {
                        val errorResponse = t.response()?.errorBody()?.string()

                        Timber.e("HTTP 실패: $errorResponse")
                    }
                    _getUserState.value = UiState.Failure("${t.message}")
                }
        }
    }
}