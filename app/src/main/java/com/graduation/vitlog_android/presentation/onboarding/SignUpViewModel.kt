package com.graduation.vitlog_android.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graduation.vitlog_android.data.repository.UserRepository
import com.graduation.vitlog_android.model.request.RequestSignUpDto
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
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _postSignUpState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val postSignUpState: StateFlow<UiState<Unit>> = _postSignUpState.asStateFlow()

    private val id: MutableStateFlow<String> get() = _id
    private var _id = MutableStateFlow<String>("")

    private val password: MutableStateFlow<String> get() = _password
    private var _password = MutableStateFlow<String>("")

    private val isPasswordSame: MutableStateFlow<Boolean> get() = _isPasswordSame
    private var _isPasswordSame = MutableStateFlow<Boolean>(false)

    val isInputValid: MutableStateFlow<Boolean> get() = _isInputValid
    private var _isInputValid = MutableStateFlow<Boolean>(false)
    fun updateUserId(
        id: String
    ){
        _id.value = id
        isInputValid()
    }

    fun updateUserPassword(
        pw: String
    ){
        _password.value = pw
        isInputValid()
    }

    fun isPasswordSame(
        recheckPw : String
    ){
        _isPasswordSame.value =  (recheckPw == _password.value)
        isInputValid()
    }

    private fun isInputValid(){
        _isInputValid.value = _id.value.length <= 8 && _password.value.length <=8 && _isPasswordSame.value
    }

    fun postSignUp(
        id: String,
        password: String
    ) {
        viewModelScope.launch {
            _postSignUpState.value = UiState.Loading
            userRepository.postSignUp(
                RequestSignUpDto(
                    user_id = id,
                    password = password
                )
            )
                .onSuccess { response ->
                    _postSignUpState.value = UiState.Success(response)
                    Timber.e("성공 $response")
                }.onFailure { t ->
                    if (t is HttpException) {
                        val errorResponse = t.response()?.errorBody()?.string()
                        Timber.e("HTTP 실패: $errorResponse")
                    }
                    _postSignUpState.value = UiState.Failure("${t.message}")
                }
        }
    }
}