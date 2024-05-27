package com.graduation.vitlog_android.presentation.mypage

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.graduation.vitlog_android.HomeFragment
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.FragmentMypageBinding
import com.graduation.vitlog_android.model.entity.Face
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@AndroidEntryPoint
class MyPageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null

    private val binding get() = _binding!!
    private val myPageViewModel by viewModels<MyPageViewModel>()
    private lateinit var myPageAdapter: MyPageAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(layoutInflater, container, false)
        myPageViewModel.getUser()
        initAdapter()
        initClickListener()
        initObserver()
        return binding.root
    }

    private fun initAdapter() {
        myPageAdapter = MyPageAdapter(
            onAddButtonClick = { openGallery() }
        )
        binding.rvMypageFaces.adapter = myPageAdapter
    }

    private fun initClickListener() {
        binding.ivMypageBack.setOnClickListener {
            navigateTo<HomeFragment>()
        }
    }

    private fun openGallery() {
        getContent.launch("image/*")
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                myPageViewModel.updateFaceUri(uri, requireContext())
            }
        }


    private fun initObserver() {
        setGetUserStateObserver()
        setPostFaceObserver()
    }

    private fun setGetUserStateObserver() {
        myPageViewModel.geUserState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        binding.data = state.data
                        val originalList = state.data.registeredFace.toMutableList()
                        originalList.add(Face(picName = "", picPath = "ic_mypage_add"))
                        myPageAdapter.submitList(originalList)
                        Timber.tag("Success").d(state.data.toString())
                    }

                    is UiState.Failure -> {
                        Timber.tag("Failure").e(state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setPostFaceObserver() {
        myPageViewModel.postFaceState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        Timber.tag("Success").d(state.data.toString())
                    }

                    is UiState.Failure -> {
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