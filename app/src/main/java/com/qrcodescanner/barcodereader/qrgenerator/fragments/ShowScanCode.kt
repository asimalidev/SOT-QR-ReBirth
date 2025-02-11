package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import apero.aperosg.monetization.util.showNativeAd
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentShowScanCodeBinding
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_home
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_result
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShowScanCode : Fragment() {

    private var navController: NavController? = null
    private lateinit var binding: FragmentShowScanCodeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: QRCodeDatabaseHelper
    private lateinit var layoutAdNative: FrameLayout
    private lateinit var ivQrCode: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShowScanCodeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Scan QR barcode result screen",
            trigger = "App display Scan QR result screen",
            eventName = "scanqr_result_scr"
        )
        navController = findNavController()
        sharedPreferences =
            requireActivity().getSharedPreferences("ScanSettings", Context.MODE_PRIVATE)
        val args: ShowScanCodeArgs by navArgs()
        val qrCode = args.qrCode
        val isFromHistory = arguments?.getBoolean("isFromHistory", false) ?: false
        layoutAdNative = requireActivity().findViewById(R.id.layoutAdNative)
        binding.textViewQRCode.text = qrCode
        ivQrCode = view.findViewById(R.id.qrCodeImageView)
        // Initialize database helper
        dbHelper = QRCodeDatabaseHelper(requireContext())
        generateAndShowQRCode(qrCode)

        if (isFromHistory) {
            binding.btnSave.visibility = View.GONE
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (navController != null) {
                            val action = ShowScanCodeDirections.actionNavShowcodeToNavHistory()
                            findNavController().navigate(action)
                        } else {
                            isNavControllerAdded()
                        }
                    }
                })

        } else {
            binding.btnSave.visibility = View.VISIBLE
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (navController != null) {
                            CustomFirebaseEvents.logEvent(
                                context = requireActivity(),
                                screenName = "Scan QR barcode result screen",
                                trigger = "User tap button Back",
                                eventName = "create_result_scr_tap_back"
                            )
                            val action = ShowScanCodeDirections.actionNavShowcodeToNavScancode()
                            findNavController().navigate(action)
                        } else {
                            isNavControllerAdded()
                        }
                    }
                })
        }



        if (isValidUrl(qrCode)) {
            binding.qrcodeType.text = getString(R.string.website)
            binding.btnOpenWebsite.visibility = View.VISIBLE

            // Check if the switch is enabled and open the website automatically
            if (sharedPreferences.getBoolean("openWebsiteAutomatically", false)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    openWebsite(qrCode)
                }, 1000)
            }
        } else {
            binding.qrcodeType.text = getString(R.string.text)
            binding.btnOpenWebsite.visibility = View.VISIBLE
            if (sharedPreferences.getBoolean("openWebsiteAutomatically", false)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    openWebsite(qrCode)
                }, 1000)
            }
        }


        binding.btnSave.setOnClickListener {
            saveImageToDownloads(ivQrCode.drawable.toBitmap()) // Save the displayed image
        }


        binding.tvNotes.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Create QR result screen",
                trigger = "User tap button Note",
                eventName = "create_result_note"
            )
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Scan QR barcode result screen",
                trigger = "User tap button Note",
                eventName = "create_result_scr_tap_note"
            )
            showAddNoteDialog()
        }

        binding.tvCopy.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Create QR result screen",
                trigger = "User tap Copy",
                eventName = "create_result_copy"
            )
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Scan QR barcode result screen",
                trigger = "User tap button Copy",
                eventName = "scanqr_result_scr_tap_copy"
            )
            copyTextToClipboard(qrCode)
        }

        binding.tvShare.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Create QR result screen",
                trigger = "User tap share",
                eventName = "create_result_share"
            )
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Scan QR barcode result screen",
                trigger = "User tap button Share",
                eventName = "scanqr_result_scr_tap_share"
            )
            shareText(qrCode)
        }

        binding.btnOpenWebsite.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Create QR result screen",
                trigger = "Tap on Open Website",
                eventName = "create_result_openweb"
            )
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Scan QR barcode result screen",
                trigger = "User tap button Open Web",
                eventName = "scanqr_result_scr_tap_open_web"
            )
            openWebsite(qrCode)
        }

        // Get the current date and time and set the text
        val currentDateTime = getCurrentDateTime()
        binding.tvdateandtime.text = currentDateTime

        if (sharedPreferences.getBoolean("copyToClipboard", false)) {
            copyTextToClipboard(qrCode)
        }
    }

    private fun saveImageToDownloads(bitmap: Bitmap) {
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Create QR code screen",
            trigger = "User tap button Save",
            eventName = "createqr_scr_tap_save"
        )
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
                Toast.makeText(requireContext(), "Image saved to Downloads", Toast.LENGTH_SHORT)
                    .show()
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
            val directory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(directory, "QRCode_${System.currentTimeMillis()}.png")
            try {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Toast.makeText(requireContext(), "Image saved to Downloads", Toast.LENGTH_SHORT)
                        .show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()
                        val action = ShowScanCodeDirections.actionFastToHome()
                        navController?.navigate(action)
                    }, 1000) // Show the progress bar for 1 second
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }
    }

    private fun generateAndShowQRCode(qrCode: String) {
        try {
            // Generate QR code
            val size = 500 // Size of the QR Code
            val bitMatrix = MultiFormatWriter().encode(
                qrCode,
                BarcodeFormat.QR_CODE,
                size,
                size
            )
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)

            // Set the QR code bitmap to the ImageView
            binding.qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to generate QR code", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun copyTextToClipboard(text: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QR Code", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), "Text copied", Toast.LENGTH_SHORT).show()
    }

    private fun shareText(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }


    private fun openWebsite(content: String) {
        val url = if (isValidUrl(content)) {
            content
        } else {
            "https://www.google.com/search?q=${Uri.encode(content)}"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }


    private fun showAddNoteDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_note, null)
        val etNote = dialogView.findViewById<EditText>(R.id.etNote)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            val noteText = etNote.text.toString()
            if (noteText.isNotEmpty()) {
                binding.notetext.text = noteText
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun isValidUrl(qrCode: String): Boolean {
        return try {
            val uri = Uri.parse(qrCode)
            uri.scheme == "http" || uri.scheme == "https"
        } catch (e: Exception) {
            false
        }
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yy : hh:mm a", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        val isAdEnabled = requireActivity()
            .getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)
            .getBoolean("native_result", true)

        Log.e("AdStatus", "isAdEnabled: " + isAdEnabled)

        if (NetworkCheck.isNetworkAvailable(requireContext()) && isAdEnabled) {
            AdsProvider.nativeResult.config(
                requireActivity().getSharedPreferences(
                    "RemoteConfig",
                    AppCompatActivity.MODE_PRIVATE
                ).getBoolean(
                    native_result, true
                )
            )
            AdsProvider.nativeResult.loadAds(MyApplication.getApplication())
            showNativeAd(
                AdsProvider.nativeResult,
                binding.layoutAdNative,
                R.layout.layout_home_native_ad
            )
        } else {
            binding.layoutAdNative.visibility = View.GONE
        }

//        val isAdEnabled = requireActivity()
//            .getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)
//            .getBoolean("native_result", true)
//
//        if (NetworkCheck.isNetworkAvailable(requireContext()) && isAdEnabled) {
//
//            layoutAdNative.visibility = View.VISIBLE
//
//            AdsProvider.nativeResult.config(
//                requireActivity().getSharedPreferences(
//                    "RemoteConfig",
//                    AppCompatActivity.MODE_PRIVATE
//                ).getBoolean(
//                    native_result, true
//                )
//            )
//
//            AdsProvider.nativeResult.loadAds(MyApplication.getApplication())
//            showNativeAd(
//                AdsProvider.nativeResult,
//                requireActivity().findViewById(R.id.layoutAdNative),
//                R.layout.layout_home_native_ad
//            )
//        } else {
//            layoutAdNative.visibility = View.GONE
//        }

        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.qr_code_reader)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        if (back != null) {
            back.visibility = View.VISIBLE
        }
        back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        if (setting != null) {
            setting.visibility = View.INVISIBLE
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        if (ivClose != null) {
            ivClose.setImageResource(R.drawable.ic_premium)
            ivClose.visibility = View.INVISIBLE
        }

    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }
}
