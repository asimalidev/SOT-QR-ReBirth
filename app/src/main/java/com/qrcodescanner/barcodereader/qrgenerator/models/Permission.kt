package com.qrcodescanner.barcodereader.qrgenerator.models

data class Permission(
    val key: String, // Unique key for SharedPreferences
    val name: String,
    val description: String,
    var isChecked: Boolean
)
