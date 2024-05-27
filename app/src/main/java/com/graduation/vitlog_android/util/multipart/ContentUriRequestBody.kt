package com.graduation.vitlog_android.util.multipart

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

class ContentUriRequestBody(
    context: Context,
    private val uri: Uri
) : RequestBody() {
    private val contentResolver = context.contentResolver

    private var fileName = ""
    private var fileSize = -1L

    init {
        val projection = arrayOf(MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DISPLAY_NAME)
        contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                fileSize =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                fileName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
            }
        }
    }

    private fun getFileName() = fileName

    override fun contentLength(): Long = fileSize

    override fun contentType(): MediaType? =
        contentResolver.getType(uri)?.toMediaTypeOrNull()

    override fun writeTo(sink: BufferedSink) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.source().let(sink::writeAll)
        }
    }

    fun toFormData() = MultipartBody.Part.createFormData("file", getFileName(), this)
}
