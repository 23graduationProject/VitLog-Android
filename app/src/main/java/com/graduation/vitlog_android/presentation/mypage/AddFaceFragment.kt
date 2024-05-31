package com.graduation.vitlog_android.presentation.mypage

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.FragmentAddfaceBinding
import com.graduation.vitlog_android.util.binding.BindingFragment
import com.graduation.vitlog_android.util.binding.setUploadImageUri
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AddFaceFragment : BindingFragment<FragmentAddfaceBinding>(R.layout.fragment_addface) {
    private val myPageViewModel: MyPageViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListener()
        editFaceName()
        manageFaceName()
        setPostFaceObserver()
        observeFaceUri()
    }

    private fun initClickListener() {
        binding.ivAddface.setOnClickListener {
            openGallery()
        }
        registerFace()
    }

    private fun openGallery() {
        getContent.launch("image/*")
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                myPageViewModel.updateFaceUri(uri)

            }
        }

    private fun observeFaceUri() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myPageViewModel.faceUri.collect { uri ->
                    val imageView = view?.findViewById<ImageView>(R.id.iv_addface)
                    imageView?.setUploadImageUri(uri)
                }
            }
        }
    }

    private fun registerFace() {
        binding.btnAddfaceRegister.setOnClickListener {
            myPageViewModel.registerFace(requireContext())
        }
    }

    private fun editFaceName() {
        binding.tvAddfaceName.setOnClickListener {
            it.visibility = View.INVISIBLE
            binding.etAddfaceName.setText("")
            binding.etAddfaceName.visibility = View.VISIBLE
            binding.etAddfaceName.requestFocus()
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etAddfaceName, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun saveFaceName() {
        val faceName = binding.etAddfaceName.text
        binding.tvAddfaceName.text = faceName
        myPageViewModel.updateFaceName(faceName.toString())
        binding.etAddfaceName.visibility = View.INVISIBLE
        binding.tvAddfaceName.visibility = View.VISIBLE
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etAddfaceName.windowToken, 0)
    }

    private fun manageFaceName() {
        binding.root.setOnClickListener {
            if (binding.etAddfaceName.visibility == View.VISIBLE) {
                saveFaceName()
            }
        }

        binding.etAddfaceName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveFaceName()
            }
        }
    }

    private fun setPostFaceObserver() {
        myPageViewModel.postFaceState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        if (state.data.status == 400) {
                            Toast.makeText(
                                requireContext(),
                                "얼굴을 등록할 수 없습니다 /n 이목구비가 정면으로 나온 사진을 선택해주세요",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@onEach
                        }
                        Toast.makeText(requireContext(), "얼굴을 성공적으로 등록했습니다", Toast.LENGTH_SHORT)
                            .show()
                        myPageViewModel._postFaceState.value = UiState.Empty
                        myPageViewModel._faceName.value = ""
                        myPageViewModel._faceUri.value = null
                        parentFragmentManager.popBackStack()
                        Timber.tag("Success").d(state.data.toString())
                    }

                    is UiState.Failure -> {o
                        Timber.tag("Failure").e(state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private inline fun <reified T : Fragment> navigateTo() {
        parentFragmentManager.commit {
            replace<T>(R.id.home_fragment_view, T::class.simpleName)
        }
    }
}