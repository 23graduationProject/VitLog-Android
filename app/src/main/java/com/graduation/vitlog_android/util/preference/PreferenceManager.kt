package com.graduation.vitlog_android.util.preference

import android.content.Context
import android.content.SharedPreferences

object SharedPrefManager {
    private const val PREFS_NAME = "MyPrefsFile"
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun save(key: String, value: Any) {
        val editor = preferences.edit()
        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is String -> editor.putString(key, value)
            else -> error("Unsupported data type")
        }
        editor.apply()
    }

    fun getString(key: String, defaultValue: String = ""): String =
        preferences.getString(key, defaultValue) ?: defaultValue

    fun getInt(key: String, defaultValue: Int = 0): Int =
        preferences.getInt(key, defaultValue)

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        preferences.getBoolean(key, defaultValue)


    var uid: Int
        get() = getInt("uid", -1)
        set(value) = save("uid", value)

    var vid: String
        get() = getString("vid", "")
        set(value) = save("vid", value)
}
