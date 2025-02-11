package com.qrcodescanner.barcodereader.qrgenerator.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QRCodeViewModel : ViewModel() {
    private val _qrCodeText = MutableLiveData<String>()
    val qrCodeText: LiveData<String> get() = _qrCodeText

    private val _isQrCode = MutableLiveData<Boolean>()
    val isQrCode: LiveData<Boolean> get() = _isQrCode

    fun setQRCodeText(text: String) {
        _qrCodeText.value = text
    }

    fun setIsQrCode(isQr: Boolean) {
        _isQrCode.value = isQr
    }
}
