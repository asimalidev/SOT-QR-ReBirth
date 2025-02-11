package com.qrcodescanner.barcodereader.qrgenerator.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // MutableLiveData to hold the QR code text
    private val _navigateToEditedQr = MutableLiveData<String>()

    // LiveData to expose the QR code text for navigation
    val navigateToEditedQr: LiveData<String> get() = _navigateToEditedQr

    // Method to update the QR code text
    fun onQRCodeClicked(qrCodeText: String) {
        _navigateToEditedQr.value = qrCodeText
    }
}

