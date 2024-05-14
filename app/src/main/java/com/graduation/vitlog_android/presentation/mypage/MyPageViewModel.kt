package com.graduation.vitlog_android.presentation.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graduation.vitlog_android.data.repository.UserRepository
import com.graduation.vitlog_android.model.entity.User
import com.graduation.vitlog_android.util.preference.SharedPrefManager.uid
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
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _getUserState =
        MutableStateFlow<UiState<User>>(UiState.Empty)
    val geUserState: StateFlow<UiState<User>> =
        _getUserState.asStateFlow()


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