package com.qrcodescanner.barcodereader.qrgenerator.database

data class QRCodeData(
    val qrCode: String,
    val date: String,
    val time: String,
    val drawable: Int,
    var imagePath: String? = null, // Path for the edited image
    val entryType: String // New field to store whether it's created or scanned
)
