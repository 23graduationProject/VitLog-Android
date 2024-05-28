package com.graduation.vitlog_android.util.context

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class ContextExt {
    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun Context.longToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun Context.snackBar(anchorView: View, message: () -> String) {
        Snackbar.make(anchorView, message(), Snackbar.LENGTH_SHORT).show()
    }
}