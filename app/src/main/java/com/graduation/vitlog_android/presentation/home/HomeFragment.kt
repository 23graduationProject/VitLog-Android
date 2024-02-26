package com.graduation.vitlog_android

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.GridLayoutManager
import com.graduation.vitlog_android.databinding.FragmentHomeBinding
import com.graduation.vitlog_android.presentation.edit.EditActivity
import com.graduation.vitlog_android.presentation.home.GalleryAdapter
import com.graduation.vitlog_android.presentation.mypage.MyPageFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                // 권한 거부된 상태
                Toast.makeText(context, "갤러리 접근 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
            }
        }

    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // 갤러리에서 선택한 영상의 URI 처리
            val intent = Intent(context, EditActivity::class.java)
            intent.data = Uri.parse(uri.toString())
            Log.d("uri", uri.toString())
            startActivity(intent)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        binding.ivHomeEdit.setOnClickListener {
            checkPermission()
        }

        binding.ivHomeMypage.setOnClickListener {
            navigateTo<MyPageFragment>()
        }
        showRecentImages()
        return binding.root
    }

    // 갤러리 접근 권한을 확인하고 갤러리에 접근
    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
                    == PackageManager.PERMISSION_GRANTED -> {
                // 이미 권한이 허용된 경우
                openGallery()
            }

            else -> {
                // 권한이 없는 경우 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
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

    private fun showRecentImages() {
        val resolver = context?.contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.ImageColumns._ID)
        val sortOrder = "${MediaStore.Images.ImageColumns.DATE_ADDED} DESC"

        val cursor = resolver?.query(uri, projection, null, null, sortOrder)


        val imageUris = mutableListOf<Uri>()
        if (cursor != null) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageUris.add(contentUri)
            }
            cursor.close()
        }

        val recentImageUris = imageUris.take(9)

        binding.rvHomeGallery.layoutManager = GridLayoutManager(context, 3) // 3 columns
        binding.rvHomeGallery.adapter = GalleryAdapter(recentImageUris)

    }

}