package com.graduation.vitlog_android.presentation.onboarding

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.ActivitySignUpBinding
import com.graduation.vitlog_android.util.binding.BindingActivity
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : BindingActivity<ActivitySignUpBinding>(R.layout.activity_sign_up) {

    private val signUpViewModel by viewModels<SignUpViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addListener()
        addObserver()
    }

    private fun addListener() {
        initTextChangeListener()
        setBackButtonClickListener()
        setSignUpButtonClickListener()

    }

    private fun addObserver() {
        setPostSignUpStateObserver()
    }

    private fun setSignUpButtonClickListener() {
        binding.signUpBtn.setOnClickListener {
            signUpViewModel.postSignUp(
                id = signUpViewModel.id.value, password = signUpViewModel.password.value
            )
        }
    }

    private fun setBackButtonClickListener() {
        binding.signUpBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun initTextChangeListener() {
        binding.etSignupId.doAfterTextChanged { text ->
            signUpViewModel.updateUserId(text.toString())
            updateRegisterButtonState()
        }
        binding.etSignupPw.doAfterTextChanged { text ->
            signUpViewModel.updateUserPassword(text.toString())
            updateRegisterButtonState()
        }
        binding.etSignupPwCheck.doAfterTextChanged { text ->
            signUpViewModel.isPasswordSame(text.toString())
            updateRegisterButtonState()
        }
    }

    private fun updateRegisterButtonState() {
        lifecycleScope.launch {
            signUpViewModel.isInputValid.collectLatest {
                if (it) {
                    binding.signUpBtn.background = getDrawable(R.drawable.background_pink_radius_5)
                    binding.signUpBtn.isEnabled = true
                } else {
                    binding.signUpBtn.background = getDrawable(R.drawable.background_gray_radius_5)
                    binding.signUpBtn.isEnabled = false
                }
            }
        }
    }

    private fun setPostSignUpStateObserver() {
        signUpViewModel.postSignUpState.flowWithLifecycle(lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        Log.d("Success", state.data.toString())
                        finish()
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