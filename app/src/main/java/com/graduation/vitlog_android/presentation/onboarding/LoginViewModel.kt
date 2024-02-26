package com.graduation.vitlog_android.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graduation.vitlog_android.data.repository.UserRepository
import com.graduation.vitlog_android.model.request.RequestLoginDto
import com.graduation.vitlog_android.model.response.ResponseLoginDto
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _postLoginState = MutableStateFlow<UiState<ResponseLoginDto>>(UiState.Loading)
    val postLoginState: StateFlow<UiState<ResponseLoginDto>> = _postLoginState.asStateFlow()

    val id: MutableStateFlow<String> get() = _id
    private var _id = MutableStateFlow<String>("")

    val password: MutableStateFlow<String> get() = _password
    private var _password = MutableStateFlow<String>("")

    fun updateIdInput(
        id: String
    ){
        _id.value = id
        isInputValid()
    }

    fun updatePasswordInput(
        pw: String
    ){
        _password.value = pw
        isInputValid()
    }

    val isInputValid: MutableStateFlow<Boolean> get() = _isInputValid
    private var _isInputValid = MutableStateFlow<Boolean>(false)

    fun isInputValid(){
        _isInputValid.value = _id.value.isNotBlank() && _password.value.isNotBlank()
    }

    fun postLogin(
        id: String,
        password: String
    ) {
        viewModelScope.launch {
            _postLoginState.value = UiState.Loading
            userRepository.postLogin(
                RequestLoginDto(
                    user_id = id,
                    password = password
                )
            )
                .onSuccess { response ->
                    _postLoginState.value = UiState.Success(response)
                    Timber.e("성공 $response")
                }.onFailure { t ->
                    if (t is HttpException) {
                        val errorResponse = t.response()?.errorBody()?.string()
                        Timber.e("HTTP 실패: $errorResponse")
                    }
                    _postLoginState.value = UiState.Failure("${t.message}")
                }
        }
    }
}