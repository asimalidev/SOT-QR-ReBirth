package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.install.model.ActivityResult
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.qrcodescanner.barcodereader.qrgenerator.R

class MainActivity2 : AppCompatActivity() {
    private lateinit var scanner: GmsDocumentScanner
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var isScanning = false // Control scanning state
    private var hasScanResult = false // Flag to control if a valid result has been received
    private var cameFromCreateDocument = false // Flag to track navigation from CreateDocumentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        Log.e("BackHandler", "Main Activity 2")

        // Initialize the scanner options
        val option = GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setPageLimit(10)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e("backpress", "Back button pressed from OnBackPressedDispatcher")

                if (isScanning || hasScanResult || cameFromCreateDocument) {
                    // If there's any ongoing scanning or results, handle accordingly (if needed).
                    // Optional: handle specific logic based on states here.
                    // But for now, just navigate back to Home
                startDocumentScanning()
                } else {
                    // Simply navigate to HomeActivity on the first back press
                    navigateToHome() // Always navigate on first back press
                }
            }
        })

        // Initialize the scanner
        scanner = GmsDocumentScanning.getClient(option)

        // Set the content view using XML layout
        setContentView(R.layout.activity_main) // Make sure you have this layout

        // Set up the result launcher
        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result -> handleScanResult(result) }

        // Start the scanning process when the activity is created
        if (!isScanning && !hasScanResult) {
            startDocumentScanning()
            isScanning = true // Set the flag to indicate scanning has started
        }
    }

    private fun handleScanResult(result: androidx.activity.result.ActivityResult) {
        // Check if the activity is finishing or already finished
        if (isFinishing || isDestroyed) {
            Log.e("MainActivity2", "Activity is finishing or destroyed, skipping result handling.")
            return
        }

        if (result.resultCode == RESULT_OK) {
            // Process the scanning result only if it was successful
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            if (scanningResult != null) {
                val imageUris = scanningResult.pages!!.map { it.imageUri }

                // Generate document name
                val documentName = "Document_${System.currentTimeMillis()}"

                // Start CreateDocumentActivity and pass image URIs and document name
                val intent = Intent(this@MainActivity2, NewCreateActivity::class.java).apply {
                    putParcelableArrayListExtra("imageUris", ArrayList(imageUris))
                    putExtra("documentName", documentName) // Pass document name
                }
                startActivityForResult(intent, REQUEST_CODE_CREATE_DOCUMENT)

                // After successfully processing, set hasScanResult to true
                hasScanResult = true
                isScanning = false // Reset scanning flag after processing the result
                cameFromCreateDocument =
                    true // Set the flag indicating we came from CreateDocumentActivity
            } else {
                // Handle the case where the scanning result is null
                Toast.makeText(
                    this@MainActivity2,
                    "Failed to get scanning result",
                    Toast.LENGTH_SHORT
                ).show()
                isScanning = false
            }
        } else {
            // If the scan was canceled or failed
            isScanning = false
        }
    }

    private fun startDocumentScanning() {
        // Initiate the scanning process
        scanner.getStartScanIntent(this@MainActivity2)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
                isScanning = false // Reset scanning flag on failure
            }
    }

    override fun onPause() {
        super.onPause()
        // Reset scan result flag to allow new scanning when returning to activity
        hasScanResult = false
    }

    private fun navigateToHome() {
        Log.d("MainActivity2", "Navigating to HomeActivity")
        // Ensure the scanning is stopped
        isScanning = false
        hasScanResult = false
        cameFromCreateDocument = false

        // Navigate to HomeActivity
        val intent = Intent(this@MainActivity2, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // Clear the back stack and start a new task
        startActivity(intent)

        // Delay finishing the activity to prevent UI issues
        Handler(Looper.getMainLooper()).postDelayed({
            finish() // Finish MainActivity2 so it is removed from the back stack
        }, 100)
    }

    companion object {
        const val REQUEST_CODE_CREATE_DOCUMENT = 1001 // Define request code for activity result
    }
}


