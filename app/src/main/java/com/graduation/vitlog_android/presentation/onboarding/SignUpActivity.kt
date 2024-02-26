package com.graduation.vitlog_android.presentation.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.graduation.vitlog_android.databinding.ActivitySignUpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpBackBtn.setOnClickListener {
            finish()
        }

        binding.signUpBtn.setOnClickListener {
            //TODO: 회원가입 api 연결
            finish()
        }
    }
}