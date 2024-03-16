package com.graduation.vitlog_android.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.ActivityLoginBinding
import com.graduation.vitlog_android.presentation.MainActivity
import com.graduation.vitlog_android.util.binding.BindingActivity
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : BindingActivity<ActivityLoginBinding>(R.layout.activity_login) {

    private val loginViewModel by viewModels<LoginViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addListener()
        addObserver()
    }


    private fun addListener() {
        checkIsInputValid()
        updateRegisterButtonState()
        setLoginButtonClickListener()
        setSignUpButtonClickListener()
    }

    private fun addObserver() {
        setPostLoginStateObserver()

    }

    private fun checkIsInputValid() {
        binding.etLoginId.doAfterTextChanged { text ->
            loginViewModel.updateIdInput(text.toString())
            loginViewModel.isInputValid()
        }
        binding.etLoginPw.doAfterTextChanged { text ->
            loginViewModel.updatePasswordInput(text.toString())
            loginViewModel.isInputValid()
        }
    }

    private fun updateRegisterButtonState() {
        lifecycleScope.launch {
            loginViewModel.isInputValid.collectLatest {
                if (it) {
                    binding.loginBtn.background = getDrawable(R.drawable.background_pink_radius_5)
                    binding.loginBtn.isEnabled = true
                } else {
                    binding.loginBtn.background = getDrawable(R.drawable.background_gray_radius_5)
                    binding.loginBtn.isEnabled = false
                }
            }
        }
    }

    private fun setLoginButtonClickListener() {
        binding.loginBtn.setOnClickListener {
            loginViewModel.postLogin(
                loginViewModel.id.value,
                loginViewModel.password.value
            )
        }
    }


    private fun setSignUpButtonClickListener() {
        binding.signupBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }


    private fun setPostLoginStateObserver() {
        loginViewModel.postLoginState.flowWithLifecycle(lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        Log.d("Success", state.data.toString())
                        navigateToMain()
                        finish()
                    }

                    is UiState.Failure -> {
                        Snackbar.make(binding.root, "아이디와 비밀번호를 확인해주세요", 1000).show()
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                }
            }.launchIn(lifecycleScope)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}