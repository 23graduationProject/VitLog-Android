package com.graduation.vitlog_android.presentation.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.graduation.vitlog_android.HomeFragment
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.FragmentMypageBinding

class MyPageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(layoutInflater, container, false)
        initClickListener()
        return binding.root
    }

    private fun initClickListener() {
        binding.ivMypageBack.setOnClickListener {
            navigateTo<HomeFragment>()
        }
    }

    private inline fun <reified T : Fragment> navigateTo() {
        parentFragmentManager.commit {
            replace<T>(R.id.home_fragment_view, T::class.simpleName)
        }
    }
}