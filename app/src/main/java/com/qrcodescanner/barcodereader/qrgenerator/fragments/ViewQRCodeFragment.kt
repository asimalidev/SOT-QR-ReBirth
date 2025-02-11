package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import apero.aperosg.monetization.util.showNativeAd
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_result
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewQRCodeFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var ivQrCode: ImageView
    private lateinit var btnSave: AppCompatButton
    private lateinit var tvDateTime: TextView
    private lateinit var tvQrType: TextView
    private lateinit var tvQRCode: TextView
    private lateinit var dbHelper: QRCodeDatabaseHelper // Database helper
    private lateinit var layoutAdNative: FrameLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_q_r_code, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivQrCode = view.findViewById(R.id.qrCodeImageView)
        tvDateTime = view.findViewById(R.id.tvdateandtime)
        tvQrType = view.findViewById(R.id.qrcodeType)
        tvQRCode = view.findViewById(R.id.textViewQRCode)
        btnSave = view.findViewById(R.id.btnSave)
        layoutAdNative = view.findViewById(R.id.layoutAdNative)
        val isFromHistory = arguments?.getBoolean("isFromHistory", false) ?:false
        // Initialize the database helper
        dbHelper = QRCodeDatabaseHelper(requireContext())

        val args: ViewQRCodeFragmentArgs by navArgs()
        val qrCodeText = args.qrCode
        tvQRCode.text = qrCodeText

        // Check if qrCodeText is empty or null
        if (qrCodeText.isNullOrEmpty()) {
            // Show an error message or handle the empty input scenario
            Toast.makeText(requireContext(), "QR code content is empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Retrieve QR code data from the database
        val qrCodeData = dbHelper.getQRCodeData(qrCodeText)


        if (qrCodeData != null) {
            val currentDateTime = getCurrentDateTime()
            tvDateTime.text = currentDateTime

            // Set the QR code image from the database
            val imagePath = qrCodeData.imagePath
            if (imagePath != null) {
                // Load image using Glide or any other image loading library
                Glide.with(this)
                    .load(File(imagePath))
                    .into(ivQrCode)
            } else {
                // Generate QR code bitmap if no image path is found
                val qrCodeBitmap = generateQRCodeBitmap(qrCodeText)
                ivQrCode.setImageBitmap(qrCodeBitmap)
            }

            // Set QR code type
            if (isValidUrl(qrCodeText)) {
                tvQrType.text = "Website"
            } else {
                tvQrType.text = "Text"
            }


            btnSave.setOnClickListener {
                saveImageToDownloads(ivQrCode.drawable.toBitmap()) // Save the displayed image
            }
        } else {
            Toast.makeText(requireContext(), "QR code not found in database", Toast.LENGTH_SHORT).show()
        }

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        val action =
                            ViewQRCodeFragmentDirections.actionNavViewqrToNavHome()
                        findNavController().navigate(action)
                    }
                })

    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yy : hh:mm a", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Function to share the QR code image
    private fun shareQRCodeImage() {
        val bitmap = ivQrCode.drawable.toBitmap()
        val file = File(requireContext().cacheDir, "shared_qr_code.png")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val fileUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider", // Replace with your package name
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    }

    private fun generateQRCodeBitmap(data: String): Bitmap {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    private fun saveImageToDownloads(bitmap: Bitmap) {
        CustomFirebaseEvents.logEvent(context = requireActivity(), screenName = "Create QR code screen", trigger = "User tap button Save", eventName = "createqr_scr_tap_save")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToDownloadsQAndAbove1(bitmap)
        } else {
            saveImageToDownloadsLegacy1(bitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveImageToDownloadsQAndAbove1(bitmap: Bitmap) {
        val fileName = "QRCode_${System.currentTimeMillis()}.png"
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Saving image in download...")
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.show()

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(requireContext(), "Image saved to Downloads", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                    val action = FinalImageFragmentDirections.actionFastToHome()
                    findNavController().navigate(action)
                }, 1000) // Show the progress bar for 1 second
            }
        } else {
            Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
    }

    private fun saveImageToDownloadsLegacy1(bitmap: Bitmap) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        } else {
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Saving image in download...")
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            progressDialog.show()
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(directory, "QRCode_${System.currentTimeMillis()}.png")
            try {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Toast.makeText(requireContext(), "Image saved to Downloads", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()
                        val action = FinalImageFragmentDirections.actionFastToHome()
                        navController.navigate(action)
                    }, 1000) // Show the progress bar for 1 second
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }
    }

    private fun isValidUrl(qrCode: String): Boolean {
        return try {
            val uri = Uri.parse(qrCode)
            uri.scheme == "http" || uri.scheme == "https"
        } catch (e: Exception) {
            false
        }
    }

    override fun onResume() {
        super.onResume()

        val isAdEnabled = requireActivity()
            .getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)
            .getBoolean("native_result", true)

        Log.e("AdStatus","isAdEnabled: "+isAdEnabled)

        if (NetworkCheck.isNetworkAvailable(requireContext()) && isAdEnabled) {
            AdsProvider.nativeResult.config(requireActivity().getSharedPreferences("RemoteConfig",AppCompatActivity.MODE_PRIVATE).getBoolean(
                native_result, true))
            AdsProvider.nativeResult.loadAds(MyApplication.getApplication())
            showNativeAd(AdsProvider.nativeResult, requireActivity().findViewById(R.id.layoutAdNative), R.layout.layout_home_native_ad)
        }
        else{
            layoutAdNative.visibility = View.GONE
        }

        val topText: TextView = requireActivity().findViewById(R.id.mainText)
        topText.visibility = View.VISIBLE
        topText.text = getString(R.string.qr_code_scanner)

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        back?.visibility = View.VISIBLE

        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        setting?.visibility = View.INVISIBLE

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        ivClose?.setImageResource(R.drawable.ic_premium)
        ivClose?.visibility = View.INVISIBLE
    }
}
