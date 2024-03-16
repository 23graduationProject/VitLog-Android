package com.graduation.vitlog_android

import android.app.Application
import com.graduation.vitlog_android.util.preference.SharedPrefManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(){
    override fun onCreate() {
        super.onCreate()
        SharedPrefManager.init(this)
    }
}