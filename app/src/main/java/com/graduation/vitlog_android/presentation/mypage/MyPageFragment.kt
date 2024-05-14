package com.graduation.vitlog_android.presentation.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.graduation.vitlog_android.HomeFragment
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.FragmentMypageBinding
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
        myPageAdapter = MyPageAdapter()
        binding.rvMypageFaces.adapter = myPageAdapter
        initClickListener()
        initObserver()
        return binding.root
    }

    private fun initClickListener() {
        binding.ivMypageBack.setOnClickListener {
            navigateTo<HomeFragment>()
        }
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
                        myPageAdapter.submitList(state.data.registeredFace)
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