package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import apero.aperosg.monetization.util.showNativeAd
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentCreateQRorBarCodeBinding
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_result


class CreateQRorBarCodeFragment : Fragment() {
    private lateinit var qrCodeText: String
    private var isQrCode: Boolean = true
    private lateinit var binding: FragmentCreateQRorBarCodeBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateQRorBarCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        qrCodeText = arguments?.getString("qrCodeText") ?: ""
        isQrCode = arguments?.getBoolean("isQrCode") ?: true // Default to QR code
        updateBarcodeUI(qrCodeText)
        binding.tvCopy.setOnClickListener { copyToClipboard() }
        binding.tvShare.setOnClickListener { shareText() }
        binding.btnOpenWebsite.setOnClickListener {
            searchText()
        }
    }

    private fun generateQRCode(text: String) {
        try {
            val size = 512 // Dimension of the QR code
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            binding.qrBarcodeImageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun copyToClipboard() {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QR/Barcode Info", getTextToCopy())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun getTextToCopy(): String {
        val format = determineBarcodeFormat(qrCodeText)
        val notes =
            if (isQrCode) binding.tvAddNotes.text.toString() else binding.tvAddNotesbar.text.toString()
        return "Text: $qrCodeText\nFormat: $format\nNotes: $notes"
    }

    private fun shareText() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getTextToCopy())
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun searchText() {
        // Create the URL for the search query
        val searchUrl = "https://www.google.com/search?q=${Uri.encode(qrCodeText)}"

        // Create an Intent to open the URL in a web browser
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))

        // Start the browser activity
        startActivity(browserIntent)
    }

    private fun generateBarcode(text: String) {
        try {
            val width = 800 // Width of the barcode
            val height = 300 // Height of the barcode
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.CODE_128, width, height)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            binding.qrBarcodeImageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun determineBarcodeFormat(text: String): String {
        // Remove non-numeric characters for easier pattern matching
        val cleanText = text.replace(Regex("\\D"), "")

        return when {
            cleanText.length == 8 -> "EAN-8"
            cleanText.length == 12 -> "UPC-A"
            cleanText.length == 13 -> "EAN-13"
            cleanText.length in 3..20 -> "CODE_39"
            cleanText.length <= 80 -> "CODE_128"
            else -> "UNKNOWN"
        }
    }

    private fun updateBarcodeUI(text: String) {
        val format = determineBarcodeFormat(text)

        if (isQrCode) {
            binding.clqrcode.visibility = View.VISIBLE
            binding.clbarcode.visibility = View.GONE
            binding.tvqrCode.text = text
            generateQRCode(text)
        } else {
            binding.clqrcode.visibility = View.GONE
            binding.clbarcode.visibility = View.VISIBLE
            binding.tvBarCode.text = text
            binding.tvFormat.text = format
            generateBarcode(text)
        }

        binding.tvNotes.setOnClickListener {
            showAddNoteDialog()
        }
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
                binding.tvAddNotesbar.text = noteText
                binding.tvAddNotes.text = noteText
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        val isFromBarcodeReader = arguments?.getBoolean("isFromBarcodeReader", false) ?: false

        // Check if we are back from WebViewFragment
        val currentDestination = navController.currentDestination
        if (currentDestination?.id == R.id.nav_createqrorbarcode) {
            // Show the ad layout again
            requireActivity().findViewById<View>(R.id.layoutAdNative)?.visibility = View.VISIBLE
        }

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
            requireActivity().findViewById<FrameLayout>(R.id.layoutAdNative).visibility = View.GONE
        }
        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
//        if (isFromBarcodeReader) {
            TopText.text = getString(R.string.qr_code_reader)  // If coming from Barcode Reader
//        } else {
//            TopText.text = getString(R.string.create_qr_code)  // Default case
//        }
//        TopText.text = getString(R.string.create_qr_code)
        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }
    }

}