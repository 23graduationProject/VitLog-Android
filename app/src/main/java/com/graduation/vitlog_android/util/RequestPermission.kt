package com.graduation.vitlog_android.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class RequestPermission(private val activity: FragmentActivity) {
    private val permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2) {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    } else {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.CAMERA
        )
    }

    companion object {
        const val REQUEST_PERMISSION = 1 // 필요에 따라 요청 코드 변경 가능
    }

    fun checkAndRequestPermissions() {
        val requestList = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (requestList.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                requestList.toTypedArray(),
                REQUEST_PERMISSION
            )
        }
    }
}
