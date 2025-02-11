package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Toast
import com.qrcodescanner.barcodereader.qrgenerator.R
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import apero.aperosg.monetization.util.showBannerAd
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivityPhotoTranslaterBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.banner
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PhotoTranslaterActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPhotoTranslaterBinding
    private var imageCapture: ImageCapture? = null
    private var adLoadCount = 0
    private lateinit var cameraExecutor: ExecutorService
    private val adReloadHandler = Handler(Looper.getMainLooper())
    private lateinit var adReloadRunnable: Runnable
    private val adReloadInterval: Long = 15000
    private var isUsingBackCamera = true
    private lateinit var camera: Camera
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var preview: Preview
    private var imageUri: Uri? = null
    private var isTorchOn = false
    private var detectedLanguage: String? = null
    private var targetlanguagecode: String? = null

    companion object {
        private const val TAG = "CameraXApp"
        const val EXTRA_DETECTED_LANGUAGE =
            "detected_language" // Define a constant for the detected language
        const val LANGUAGE_REQUEST_CODE = 1 // Constant for language request code
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityPhotoTranslaterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        supportActionBar?.hide()
        checkNetworkAndLoadAds()
        cameraExecutor = Executors.newSingleThreadExecutor()
        // Initialize the ad reload runnable
        adReloadRunnable = Runnable {
            Log.d("AdTimer", "10 seconds passed. Reloading ad...")
            checkNetworkAndLoadAds()
            startAdReloadTimer()  // Schedule the next reload
        }
        // Check if there's an image URI passed in the intent
        val imageUriString = intent.getStringExtra("imageUri")
        imageUriString?.let {
            imageUri = Uri.parse(it)
            startPreviewActivity(imageUri)
            return // Skip camera setup if coming from the gallery
        }

        detectedLanguage =
            intent.getStringExtra(EXTRA_DETECTED_LANGUAGE) ?: "unknown" // Get detected language
        // Load the saved language or set default to "English"
        val sharedPreferences = getSharedPreferences("LanguagePreferences", MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("LANGUAGE_NAME", "English")
        targetlanguagecode = sharedPreferences.getString("LANGUAGE_CODE", "en")
        Toast.makeText(this, targetlanguagecode, Toast.LENGTH_SHORT).show()
        viewBinding.convertedLanguage.text = savedLanguage

        viewBinding.convertedLanguage.setOnClickListener {
            // Start AllLanguagesActivity and wait for a result
            val intent = Intent(this@PhotoTranslaterActivity, AllLanguagesActivity::class.java)
            startActivityForResult(intent, LANGUAGE_REQUEST_CODE)
        }
        hideSystemUI()
        // Initially disable the flash button
        viewBinding.icFlashoff.isEnabled = false
        // Set up the preview
        preview = Preview.Builder()
            .setTargetRotation(windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0)
            .build()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
        setupZoomButtons()
        setupZoomSeekBar()

        viewBinding.history.setOnClickListener {
            // Inside your PhotoTranslateActivity
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("navigateToHistory", true) // Passing extra to indicate navigation
            }
            startActivity(intent)
            finish() // Optional: finish PhotoTranslateActivity if you don't want to keep it in the back stack
        }


        viewBinding.btnCapture.setOnClickListener {
            takePhoto()
        }
        viewBinding.icGallery.setOnClickListener {
            pickImageFromGallery()
        }

        setupFlashButton()
    }

    private fun startAdReloadTimer() {
        Log.d("AdTimer", "Starting or restarting ad reload timer for 10 seconds.")
        adReloadHandler.postDelayed(adReloadRunnable, adReloadInterval)
    }

    private fun stopAdReloadTimer() {
        Log.d("AdTimer", "Stopping ad reload timer.")
        adReloadHandler.removeCallbacks(adReloadRunnable)
    }

    override fun onPause() {
        super.onPause()
        Log.d("ActivityState", "HomeActivity paused. Stopping ad reload timer.")
        stopAdReloadTimer()  // Stop the timer when the activity goes into the background
    }

    override fun onResume() {
        super.onResume()
        startAdReloadTimer()  // Start or resume the timer when activity is visible
    }


    fun checkNetworkAndLoadAds() {
        if (NetworkCheck.isNetworkAvailable(this@PhotoTranslaterActivity)) {
            loadShowBannerAd()
        }
    }

    private fun loadShowBannerAd() {
        adLoadCount++
        // Log the number of times the ad has been loaded
        Log.e("AdLoadCount", "Ad has been loaded $adLoadCount times")

        AdsProvider.bannerAll.config(
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                banner,
                true
            )
        )
        AdsProvider.bannerAll.loadAds(MyApplication.getApplication())
        showBannerAd(AdsProvider.bannerAll, findViewById(R.id.bannerFr), keepAdsWhenLoading = true)
        findViewById<FrameLayout>(R.id.bannerFr).visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.clbanner).visibility = View.VISIBLE
    }

    private fun startCamera() {
        Log.d("ScanCode", "setupCamera called")
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            Log.d("ScanCode", "cameraProviderFuture initialization completed")

            // Preview use case
            preview = Preview.Builder().build()
            // ImageCapture use case for taking photos
            imageCapture = ImageCapture.Builder().build()

            // Image analysis use case
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)  // Process the frame
                    }
                }

            // Select back or front camera based on your variable
            val cameraSelector = if (isUsingBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind the use cases (Preview, ImageCapture, and ImageAnalyzer) to the camera lifecycle
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )

                // Attach the surface provider to the preview
                preview.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                viewBinding.icFlashoff.isEnabled = true

                // Initialize zoom controls after the camera is set up
                setupZoomSeekBar()

            } catch (exc: Exception) {
                Log.e("ScanCode", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private fun setupFlashButton() {
        viewBinding.icFlashoff.setOnClickListener {
            toggleTorch()
        }
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // List of text recognizers
            val recognizers = listOf(
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS), // English and Latin-based
                TextRecognition.getClient(
                    DevanagariTextRecognizerOptions.Builder().build()
                ), // Devanagari
                TextRecognition.getClient(
                    ChineseTextRecognizerOptions.Builder().build()
                ), // Chinese
                TextRecognition.getClient(
                    JapaneseTextRecognizerOptions.Builder().build()
                ), // Japanese
                TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build()) // Korean
            )

            var textFound = false // Flag to indicate when text is found

            for (recognizer in recognizers) {
                if (textFound) {
                    break // Exit the loop if text has been found
                }

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        // Process recognized text
                        val recognizedText = visionText.text
                        if (recognizedText.isNotEmpty() && !textFound) {
                            detectLanguage(recognizedText)
                            textFound = true // Set flag to true once text is found
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle failure for this recognizer
                        Log.e("TextRecognition", "Text recognition failed for this recognizer", e)
                    }
                    .addOnCompleteListener {
                        // Close the image once all recognizers are processed
                        if (recognizer == recognizers.last()) {
                            imageProxy.close()
                        }
                    }
            }
        }
    }




    private fun detectLanguage(text: String) {
        // Ensure you check for null or empty text
        if (text.isBlank()) {
            viewBinding.detectLanguage.text = "No text provided"
            return
        }

        // Check for supported scripts first
        val languageCode = when {
            isDevanagari(text) -> "hi"  // Hindi
            isChinese(text) -> "zh"      // Chinese
            isJapanese(text) -> "ja"     // Japanese
            isKorean(text) -> "ko"       // Korean
            else -> {
                // If not in a recognized script, use ML Kit's Language Identification
                identifyLanguageWithMLKit(text)
                return // Exit the function as we're handling it in a separate function
            }
        }
        // Get detected language from SupportedLanguages enum
        val detectedLanguage = SupportedLanguages.values().find { it.lanCode == languageCode }
        val displayLanguage = detectedLanguage?.langEnglish ?: "Language not supported"

        // Update the TextView with detected language
        viewBinding.detectLanguage.text = displayLanguage
    }


    private fun identifyLanguageWithMLKit(text: String) {
        val languageIdentifier = LanguageIdentification.getClient(
            LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(0.5f) // Set your confidence threshold here
                .build()
        )

        languageIdentifier.identifyPossibleLanguages(text)
            .addOnSuccessListener { identifiedLanguages ->
                if (identifiedLanguages.isEmpty()) {
                    viewBinding.detectLanguage.text = "No language detected"
                    return@addOnSuccessListener
                }

                // Find the language with the highest confidence
                val mostConfidentLanguage = identifiedLanguages.maxByOrNull { it.confidence }

                // If we have a language with high confidence, display it
                if (mostConfidentLanguage != null) {
                    val languageCode = mostConfidentLanguage.languageTag  // Get language code
                    val detectedLanguage =
                        SupportedLanguages.values().find { it.lanCode == languageCode }
                    val displayLanguage = detectedLanguage?.langEnglish ?: "Language not supported"

                    // Store the language code in a variable
                    val detectedLanguageCode = languageCode

                    // Save the detected language name and code in SharedPreferences
                    val sharedPreferences =
                        getSharedPreferences("LanguagePreferences", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("DETECTED_LANGUAGE_CODE", detectedLanguageCode)
                    editor.apply()  // Save the values

                    // Display both the detected language name and code
                    viewBinding.detectLanguage.text = "$displayLanguage"
                } else {
                    viewBinding.detectLanguage.text = "Detect Language"
                }
            }
            .addOnFailureListener { e ->
                Log.e("LanguageDetection", "Error detecting language", e)
                viewBinding.detectLanguage.text = "Error detecting language"
            }
    }


    // Helper functions to determine script type
    private fun isDevanagari(text: String): Boolean {
        return text.any { it in '\u0900'..'\u097F' } // Unicode range for Devanagari
    }

    private fun isChinese(text: String): Boolean {
        return text.any { it in '\u4E00'..'\u9FFF' } // Unicode range for CJK Unified Ideographs
    }

    private fun isJapanese(text: String): Boolean {
        return text.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' } // Hiragana and Katakana ranges
    }

    private fun isKorean(text: String): Boolean {
        return text.any { it in '\uAC00'..'\uD7AF' } // Unicode range for Hangul
    }


    private fun toggleTorch() {
        if (::camera.isInitialized) {
            isTorchOn = !isTorchOn
            camera.cameraControl.enableTorch(isTorchOn)
            if (isTorchOn) {
                viewBinding.icFlashoff.setImageResource(R.drawable.ic_flash) // Update flash icon
            } else {
                viewBinding.icFlashoff.setImageResource(R.drawable.ic_flash_off) // Update flash icon
            }
        } else {
            Toast.makeText(this, "Camera not initialized", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            pickImageLauncher.launch(intent)
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Show the ProgressBar
        viewBinding.progressBar.visibility = View.VISIBLE

        // Create time stamped name and MediaStore entry.
        val fileName = "ttapp.jpg"
        // Get the output directory for saving the image
        val outputDirectory = getOutputDirectory()
        // Create the file for the image capture
        val photoFile = File(outputDirectory, fileName)
        // Create output options object which contains the file to save the image
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    // Hide the ProgressBar on error
                    viewBinding.progressBar.visibility = View.GONE
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Hide the ProgressBar
                    viewBinding.progressBar.visibility = View.GONE

                    // Start the PreviewActivity and pass the image path
                    val intent = Intent(this@PhotoTranslaterActivity, PreviewActivity::class.java)
                    intent.putExtra(PreviewActivity.EXTRA_IMAGE_PATH, photoFile.absolutePath)
                    val languageCode = intent.getStringExtra("LANGUAGE_CODE")
                    intent.putExtra("LANGUAGE_CODE", languageCode) // Pass the language code
                    intent.putExtra(PreviewActivity.EXTRA_DETECTED_LANGUAGE, targetlanguagecode)

                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        )
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    override fun onDestroy() {
        if (::cameraExecutor.isInitialized) {
            Log.d("CameraExecutor", "cameraExecutor is initialized, attempting to shut down.")
            cameraExecutor.shutdown()
            Log.d("CameraExecutor", "cameraExecutor successfully shut down.")
        } else {
            Log.d("CameraExecutor", "cameraExecutor is not initialized, no action taken.")
        }
        super.onDestroy()
    }



    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                imageUri = data?.data
                startPreviewActivity(imageUri)
            }
        }

    private fun setupZoomButtons() {
        viewBinding.icPlus.setOnClickListener {
            adjustZoomLevel(increase = true)
        }

        viewBinding.icMinus.setOnClickListener {
            adjustZoomLevel(increase = false)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@PhotoTranslaterActivity, HomeActivity::class.java))
        val abc = 3
        if (abc == 10) {
            super.onBackPressed()
        }
    }

    private fun adjustZoomLevel(increase: Boolean) {
        if (!::camera.isInitialized) {
            Log.e("PhotoTranslaterActivity", "Camera not initialized yet")
            return
        }
        val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: return
        val maxZoomRatio = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: return
        val minZoomRatio = camera.cameraInfo.zoomState.value?.minZoomRatio ?: return
        val zoomStep = 0.4f
        var newZoomRatio = currentZoomRatio

        if (increase) {
            newZoomRatio += zoomStep
            if (newZoomRatio > maxZoomRatio) newZoomRatio = maxZoomRatio
        } else {
            newZoomRatio -= zoomStep
            if (newZoomRatio < minZoomRatio) newZoomRatio = minZoomRatio
        }

        camera.cameraControl.setZoomRatio(newZoomRatio)

        val zoomProgress =
            ((newZoomRatio - minZoomRatio) / (maxZoomRatio - minZoomRatio) * 100).toInt()
        viewBinding.zoomSeekBar.progress = zoomProgress
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // For Android 10 and below
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun setupZoomSeekBar() {
        viewBinding.zoomSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (::camera.isInitialized) {
                    val minZoomRatio = camera.cameraInfo.zoomState.value?.minZoomRatio ?: return
                    val maxZoomRatio = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: return
                    val zoomRatio = minZoomRatio + (progress / 100f) * (maxZoomRatio - minZoomRatio)
                    camera.cameraControl.setZoomRatio(zoomRatio)
                } else {
                    Log.e("ScanCode", "Camera is not initialized yet")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startPreviewActivity(imageUri: Uri?) {
        if (imageUri != null) {
            val imagePath = getAbsolutePathFromUri(imageUri)
            val intent = Intent(this@PhotoTranslaterActivity, PreviewActivity::class.java).apply {
                putExtra(PreviewActivity.EXTRA_IMAGE_PATH, imagePath)
                putExtra("fromPhotoTranslaterActivity", true) // Add extra flag
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }




    private fun getAbsolutePathFromUri(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            it.getString(columnIndex)
        } ?: uri.path ?: ""
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }
}


