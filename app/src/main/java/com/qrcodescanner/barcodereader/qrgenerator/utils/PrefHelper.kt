package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.content.Context
import android.content.SharedPreferences

class PrefHelper(context: Context) {
    private val PREF_NAME = "flashpref"
    private var sharedPreferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun putString(key: String, value: String) {
        editor.putString(key, value).apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

    fun getStringDefault(key: String, default: String): String? {
        return sharedPreferences.getString(key, default)
    }

    fun putInt(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun containsKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    fun putSwitchState(key: String, isChecked: Boolean) {
        putBoolean(key, isChecked)
    }

    fun getSwitchState(key: String): Boolean {
        return getBoolean(key)
    }
        fun getBooleanDefault(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    fun clear() {
        editor.clear().apply()
    }

    fun putApps() {
        // Implement method logic here
    }

    fun putInstalledAppsInJson(key: String, json: String) {
        editor.putString(key, json).apply()
    }

    fun getInstalledApps(key: String): String? {
        return sharedPreferences.getString(key, null)
    }
}