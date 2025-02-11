package com.qrcodescanner.barcodereader.qrgenerator.database

interface OnQRCodeClickListener {
    fun onQRCodeClick(qrCodeText: String,entryType: String)
    fun onDelete(deleted:Boolean)
}
