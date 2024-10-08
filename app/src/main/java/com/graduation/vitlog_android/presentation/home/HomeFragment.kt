package com.graduation.vitlog_android.presentation.home

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.GridLayoutManager
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.FragmentHomeBinding
import com.graduation.vitlog_android.presentation.edit.EditActivity
import com.graduation.vitlog_android.presentation.mypage.MyPageFragment
import com.graduation.vitlog_android.util.binding.BindingFragment

class HomeFragment : BindingFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // 갤러리에서 선택한 영상의 URI 가져와 편집 화면으로 넘기기
            if (uri != null) {
                val intent = Intent(context, EditActivity::class.java)
                intent.data = uri
                startActivity(intent)
            } else {
                // URI가 null인 경우, 즉 아무것도 안골랐을때
                Toast.makeText(context, "영상 선택이 취소되었습니다", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivHomeEdit.setOnClickListener {
            openGallery()
        }

        binding.ivHomeMypage.setOnClickListener {
            navigateTo<MyPageFragment>()
        }
        showRecentVideos()
    }

    private fun navigateToEditWithUri(uri: Uri) {
        val intent = Intent(context, EditActivity::class.java)
        intent.data = uri
        startActivity(intent)
    }

    private fun openGallery() {
        getContentLauncher.launch("video/*")
    }

    private inline fun <reified T : Fragment> navigateTo() {
        parentFragmentManager.commit {
            replace<T>(R.id.home_fragment_view, T::class.simpleName)
                .addToBackStack("home")
        }
    }

    private fun showRecentVideos() {
        val resolver = context?.contentResolver
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.VideoColumns._ID)
        val sortOrder = "${MediaStore.Video.VideoColumns.DATE_ADDED} DESC"

        val cursor = resolver?.query(uri, projection, null, null, sortOrder)

        val videoUris = mutableListOf<Uri>()
        if (cursor != null) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri =
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                videoUris.add(contentUri)
            }
            cursor.close()
        }

        val recentVideoUris = videoUris.take(9)

        binding.rvHomeGallery.layoutManager = GridLayoutManager(context, 3)
        binding.rvHomeGallery.adapter = GalleryAdapter(requireContext(), recentVideoUris) { uri ->
            navigateToEditWithUri(uri)
        }
    }

}