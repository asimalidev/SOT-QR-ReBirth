package com.qrcodescanner.barcodereader.qrgenerator.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val isBannerVisible = MutableLiveData(true) // True if the banner should be visible
}
