package com.qrcodescanner.barcodereader.qrgenerator.models

import android.os.Parcel
import android.os.Parcelable

data class ScannedItem(
    val data: String,
    val date: String,
    val time: String,
    val isQrCode: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(data)
        parcel.writeString(date)
        parcel.writeString(time)
        parcel.writeByte(if (isQrCode) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScannedItem> {
        override fun createFromParcel(parcel: Parcel): ScannedItem {
            return ScannedItem(parcel)
        }

        override fun newArray(size: Int): Array<ScannedItem?> {
            return arrayOfNulls(size)
        }
    }
}

