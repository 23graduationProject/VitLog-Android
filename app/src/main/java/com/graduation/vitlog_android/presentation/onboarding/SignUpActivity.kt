package com.graduation.vitlog_android.presentation.onboarding

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.ActivitySignUpBinding
import com.graduation.vitlog_android.util.binding.BindingActivity
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class SignUpActivity : BindingActivity<ActivitySignUpBinding>(R.layout.activity_sign_up){

    private val signUpViewModel by viewModels<SignUpViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addListener()
        addObserver()
    }

    private fun addListener(){
        binding.signUpBackBtn.setOnClickListener {
            finish()
        }

        binding.signUpBtn.setOnClickListener {
            //TODO: 회원가입 api 연결
            finish()
        }
    }

    private fun addObserver(){
        setPostSingUpStateObserver()
    }


    private fun setPostSingUpStateObserver() {
        signUpViewModel.postSignUpState.flowWithLifecycle(lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        Log.d("Success", state.data.toString())
                    }

                    is UiState.Failure -> {
                        Log.d("Failure", state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                    else -> {}
                }
            }.launchIn(lifecycleScope)
    }
}