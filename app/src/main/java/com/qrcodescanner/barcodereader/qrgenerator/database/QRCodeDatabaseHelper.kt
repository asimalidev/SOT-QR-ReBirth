package com.qrcodescanner.barcodereader.qrgenerator.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.util.Log



//class QRCodeDatabaseHelper(context: Context) :
//    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
//
//    override fun onCreate(db: SQLiteDatabase) {
//        db.execSQL(CREATE_TABLE)
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
//        onCreate(db)
//    }
//
//
//
//    fun insertQRCode(qrCode: String, date: String, time: String, drawable: Int, imagePath: String? = null, entryType: String): Boolean {
//        val db = this.writableDatabase
//        val contentValues = ContentValues().apply {
//            put(COLUMN_QR_CODE, qrCode)
//            put(COLUMN_DATE, date)
//            put(COLUMN_TIME, time)
//            put(COLUMN_DRAWABLE, drawable)
//            put(COLUMN_IMAGE_PATH, imagePath)
//            put("entryType", entryType) // Insert entry type (created/scanned)
//        }
//
//        // Insert or replace the row if there is a conflict on the unique column
//        val result = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
//        Log.d("DbHelper", "Inserting/Updating QR code: $qrCode, Date: $date, Time: $time, Image Path: $imagePath, Result: $result")
//        db.close()
//        return result != -1L // Return true if the insert was successful, false otherwise
//    }
//
//
//
//
//    fun getQRCodeData(qrCode: String): QRCodeData? {
//        val db = this.readableDatabase
//        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_QR_CODE=?", arrayOf(qrCode))
//
//        return if (cursor.moveToFirst()) {
//            val qrCodeData = QRCodeData(
//                qrCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QR_CODE)),
//                date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
//                time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
//                drawable = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DRAWABLE)),
//                imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)),
//                entryType = cursor.getString(cursor.getColumnIndexOrThrow("entryType")) // Get entryType
//            )
//            cursor.close()
//            qrCodeData
//        } else {
//            cursor.close()
//            null // Return null if not found
//        }
//    }
//
//    fun getQRCodesByEntryType(entryType: String): List<QRCodeData> {
//        val qrCodeList = mutableListOf<QRCodeData>()
//        val db = this.readableDatabase
//        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE entryType = ?", arrayOf(entryType))
//
//        if (cursor.moveToFirst()) {
//            do {
//                val qrCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QR_CODE))
//                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
//                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
//                val drawable = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DRAWABLE))
//                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH))
//                val entryTypeValue = cursor.getString(cursor.getColumnIndexOrThrow("entryType"))
//
//                qrCodeList.add(QRCodeData(qrCode, date, time, drawable, imagePath, entryTypeValue))
//            } while (cursor.moveToNext())
//        }
//        cursor.close()
//        return qrCodeList
//    }
//
//
//
//    // Update QR code's image path
//    fun updateQRCodeImagePath(qrCode: String, newImagePath: String?): Boolean {
//        val db = this.writableDatabase
//        val contentValues = ContentValues()
//        contentValues.put(COLUMN_IMAGE_PATH, newImagePath) // Set new image path
//
//        val result = db.update(TABLE_NAME, contentValues, "$COLUMN_QR_CODE=?", arrayOf(qrCode))
//        db.close()
//        return result > 0 // Return true if update was successful, false otherwise
//    }
//
//
//
//    fun getAllQRCodes(): List<QRCodeData> {
//        val qrCodeList = mutableListOf<QRCodeData>()
//        val db = this.readableDatabase
//        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
//
//        if (cursor.moveToFirst()) {
//            do {
//                val qrCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QR_CODE))
//                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
//                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
//                val drawable = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DRAWABLE))
//                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)) // Get image path
//                val entryType = cursor.getString(cursor.getColumnIndexOrThrow("entryType")) // Get entryType
//                qrCodeList.add(QRCodeData(qrCode, date, time, drawable, imagePath, entryType)) // Include entryType
//            } while (cursor.moveToNext())
//        }
//        cursor.close()
//        return qrCodeList
//    }
//
//
//    // Delete QR code
//    fun deleteQRCode(qrCode: String): Boolean {
//        val db = this.writableDatabase
//        val result = db.delete(TABLE_NAME, "$COLUMN_QR_CODE=?", arrayOf(qrCode))
//        db.close()
//        return result > 0 // Return true if delete was successful, false otherwise
//    }
//
//    companion object {
//        private const val DATABASE_NAME = "qrCodes.db"
//        private const val DATABASE_VERSION = 1
//        private const val TABLE_NAME = "ScannedCodes"
//        private const val COLUMN_ID = "id"
//        private const val COLUMN_QR_CODE = "qrCode"
//        private const val COLUMN_DATE = "date"
//        private const val COLUMN_TIME = "time"
//        private const val COLUMN_DRAWABLE = "drawable"
//        private const val COLUMN_IMAGE_PATH = "imagePath" // Add image path column
//
//
//
//        private const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + " (" +
//                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//                COLUMN_QR_CODE + " TEXT," +
//                COLUMN_DATE + " TEXT," +
//                COLUMN_TIME + " TEXT," +
//                COLUMN_DRAWABLE + " INTEGER," +
//                COLUMN_IMAGE_PATH + " TEXT," +
//                "entryType TEXT)") // Add entryType column
//
//    }
//}


class QRCodeDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertQRCode(qrCode: String, date: String, time: String, drawable: Int, imagePath: String? = null, entryType: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_QR_CODE, qrCode)
            put(COLUMN_DATE, date)
            put(COLUMN_TIME, time)
            put(COLUMN_DRAWABLE, drawable)
            put(COLUMN_IMAGE_PATH, imagePath)
            put(COLUMN_ENTRY_TYPE, entryType)
        }

        val result = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
        Log.d("QRCodeDatabaseHelper", "Inserted QR Code: $qrCode, Type: $entryType, Result: $result")
        db.close()
        return result != -1L
    }

    fun getQRCodeData(qrCode: String): QRCodeData? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_QR_CODE=?", arrayOf(qrCode))

        return if (cursor.moveToFirst()) {
            val qrCodeData = QRCodeData(
                qrCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QR_CODE)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                drawable = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DRAWABLE)),
                imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)),
                entryType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_TYPE))
            )
            cursor.close()
            qrCodeData
        } else {
            cursor.close()
            null
        }
    }

    fun getAllQRCodes(): List<QRCodeData> {
        val qrCodeList = mutableListOf<QRCodeData>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        Log.d("QRCodeDatabaseHelper", "Fetching all QR codes")

        if (cursor.moveToFirst()) {
            do {
                val qrCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QR_CODE))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                val drawable = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DRAWABLE))
                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH))
                val entryType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_TYPE))

                Log.d("QRCodeDatabaseHelper", "QR Code: $qrCode, Type: $entryType")
                qrCodeList.add(QRCodeData(qrCode, date, time, drawable, imagePath, entryType))
            } while (cursor.moveToNext())
        } else {
            Log.d("QRCodeDatabaseHelper", "No QR codes found")
        }
        cursor.close()
        return qrCodeList
    }

    fun getQRCodesByEntryType(entryType: String): List<QRCodeData> {
        val qrCodeList = mutableListOf<QRCodeData>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ENTRY_TYPE = ?",
            arrayOf(entryType)
        )
        Log.d("QRCodeDatabaseHelper", "Fetching QR codes for type: $entryType")

        if (cursor.moveToFirst()) {
            do {
                val qrCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QR_CODE))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                val drawable = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DRAWABLE))
                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH))
                val entryTypeValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_TYPE))

                qrCodeList.add(QRCodeData(qrCode, date, time, drawable, imagePath, entryTypeValue))
            } while (cursor.moveToNext())
        } else {
            Log.d("QRCodeDatabaseHelper", "No QR codes found for type: $entryType")
        }

        cursor.close()
        return qrCodeList
    }

    fun deleteQRCode(qrCode: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_QR_CODE=?", arrayOf(qrCode))
        Log.d("QRCodeDatabaseHelper", "Deleted QR Code: $qrCode, Result: $result")
        db.close()
        return result > 0
    }

    companion object {
        private const val DATABASE_NAME = "qrCodes.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "ScannedCodes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_QR_CODE = "qrCode"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_DRAWABLE = "drawable"
        private const val COLUMN_IMAGE_PATH = "imagePath"
        private const val COLUMN_ENTRY_TYPE = "entryType"

        private const val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_QR_CODE TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_TIME TEXT,
                $COLUMN_DRAWABLE INTEGER,
                $COLUMN_IMAGE_PATH TEXT,
                $COLUMN_ENTRY_TYPE TEXT
            )
        """
    }
}




