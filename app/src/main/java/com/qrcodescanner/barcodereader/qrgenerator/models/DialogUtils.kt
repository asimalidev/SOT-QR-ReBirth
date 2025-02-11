package com.qrcodescanner.barcodereader.qrgenerator.models

import android.view.ViewGroup
import android.view.Window

object DialogUtils {
    fun setMargins(window: Window, left: Int, top: Int, right: Int, bottom: Int) {
        val decorView = window.decorView
        val horizontalMargin = left + right
        val verticalMargin = top + bottom

        decorView.setPadding(left, top, right, bottom)
        val params = window.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        window.attributes = params
    }
}
