package com.graduation.vitlog_android.presentation.edit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.ActivityEditBinding
import com.graduation.vitlog_android.util.binding.BindingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditActivity : BindingActivity<ActivityEditBinding>(R.layout.activity_edit) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val getUri = intent.data
        val editFragment = EditFragment()
        val bundle = Bundle()
        bundle.putString("videoUri", getUri.toString())
        editFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(binding.editFragmentView.id, editFragment)
            .commit()
    }
}