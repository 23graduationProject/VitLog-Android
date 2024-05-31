package com.graduation.vitlog_android.presentation.mypage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.FragmentMypageBinding
import com.graduation.vitlog_android.model.entity.Face
import com.graduation.vitlog_android.presentation.home.HomeFragment
import com.graduation.vitlog_android.util.binding.BindingFragment
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@AndroidEntryPoint
class MyPageFragment : BindingFragment<FragmentMypageBinding>(R.layout.fragment_mypage) {
    private val myPageViewModel by viewModels<MyPageViewModel>()
    private lateinit var myPageAdapter: MyPageAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myPageViewModel.getUser()
        initAdapter()
        initClickListener()
        initObserver()
    }

    private fun initAdapter() {
        myPageAdapter = MyPageAdapter(
            onAddButtonClick = { navigateToAddFace() }
        )
        binding.rvMypageFaces.adapter = myPageAdapter
    }

    private fun initClickListener() {
        binding.ivMypageBack.setOnClickListener {
            navigateTo<HomeFragment>()
        }
    }

    private fun navigateToAddFace() {
        navigateTo<AddFaceFragment>()
    }


    private fun initObserver() {
        setGetUserStateObserver()
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


    private inline fun <reified T : Fragment> navigateTo() {
        parentFragmentManager.commit {
            replace<T>(R.id.home_fragment_view, T::class.simpleName)
                .addToBackStack("MyPageFragment")
        }
    }
}