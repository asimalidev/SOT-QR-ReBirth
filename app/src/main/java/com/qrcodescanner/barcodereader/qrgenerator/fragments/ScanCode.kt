package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.common.Barcode
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentScanCodeBinding
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.inter_scan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanCode : Fragment() {
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE =
            101 // Define a request code for camera permission
    }
    private lateinit var binding: FragmentScanCodeBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var camera: Camera
    private lateinit var preview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var zoomSeekBar: SeekBar
    private lateinit var flashButton: ImageView
    private lateinit var galleryButton: ImageView
    private lateinit var changeCameraButton: ImageView
    private lateinit var plusButton: ImageView
    private lateinit var minusButton: ImageView
    private var isTorchOn = false
    private var isUsingBackCamera = true
    private lateinit var dbHelper: QRCodeDatabaseHelper
    private var isScanInProgress = false
    private val REQUEST_GALLERY_IMAGE = 202
    private var navController: NavController? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up the preview
        preview = Preview.Builder().setTargetRotation(activity?.windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0).build()

        // Set up image analysis
        imageAnalysis = ImageAnalysis.Builder().setTargetRotation(activity?.windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0).build()

        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Scan QR barcode screen",
            trigger = "App display Scan QR screen",
            eventName = "scanqr_scr"
        )

        if (isAdded) {
            navController = findNavController()
        }
        dbHelper = QRCodeDatabaseHelper(requireContext())
        sharedPreferences =
            requireActivity().getSharedPreferences("ScanSettings", Context.MODE_PRIVATE)
        zoomSeekBar = view.findViewById(R.id.zoomSeekBar)
        flashButton = view.findViewById(R.id.ic_flash)
        galleryButton = view.findViewById(R.id.button_gallery)
        changeCameraButton = view.findViewById(R.id.changeCamera)
        plusButton = view.findViewById(R.id.ic_plus)
        minusButton = view.findViewById(R.id.ic_minus)
        setupZoomButtons()


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Ensure navController is available and you're navigating to the correct destination
                    val navController = findNavController() // Ensure this points to the correct NavController

                    // Check if the action exists in the navigation graph before navigating
                    try {
                        val action = ScanCodeDirections.actionNavScancodeToNavHome()
                        navController.navigate(action)
                    } catch (e: IllegalArgumentException) {
                        Log.e("ScanCode", "Navigation action not found: ${e.message}")
                        // Handle navigation failure, e.g., navigate to a fallback destination
                    }
                }
            })

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is granted, set up the camera
            setupCamera()
        } else {
            // If permission is not granted, request permission
            requestCameraPermission()
        }


        setupZoomSeekBar()
        setupFlashButton()
        setupGalleryButton()
        setupChangeCameraButton()
    }

    // Function to request camera permission
    private fun requestCameraPermission() {
        // Check if we should show rationale for permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            )
        ) {
            // Show an explanation to the user
            Toast.makeText(
                requireContext(),
                "Camera permission is needed to scan QR codes",
                Toast.LENGTH_LONG
            ).show()
        }

        // Request the permission
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission is granted, set up the camera
                setupCamera()
            } else {
                // Permission denied, show a message
                Toast.makeText(
                    requireContext(),
                    "Camera permission is required to scan QR codes",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupZoomButtons() {
        plusButton.setOnClickListener {
            adjustZoomLevel(increase = true)
        }

        minusButton.setOnClickListener {
            adjustZoomLevel(increase = false)
        }
    }


    private fun adjustZoomLevel(increase: Boolean) {
        // Ensure camera is initialized
        if (!::camera.isInitialized) {
            // Handle the error, for example, log it or show a message
            Log.e("ScanCode", "Camera not initialized!")
            return
        }

        val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: return
        val maxZoomRatio = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: return
        val minZoomRatio = camera.cameraInfo.zoomState.value?.minZoomRatio ?: return

        // Determine the step size for zooming
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

        // Update the SeekBar to reflect the new zoom level
        val zoomProgress =
            ((newZoomRatio - minZoomRatio) / (maxZoomRatio - minZoomRatio) * 100).toInt()
        zoomSeekBar.progress = zoomProgress
    }


    private fun setupCamera() {
        Log.d("ScanCode", "setupCamera called")
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            Log.d("ScanCode", "cameraProviderFuture initialization completed")
            preview = Preview.Builder().build()

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val barcodeScanner = BarcodeScanning.getClient()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                processImageProxy(barcodeScanner, imageProxy)
            }

            val cameraSelector = if (isUsingBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
                preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

                // Initialize zoom controls after the camera is set up
                setupZoomSeekBar()

            } catch (exc: Exception) {
                Log.e("ScanCode", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {
        if (isScanInProgress) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val qrCode = barcode.rawValue
                        if (qrCode != null) {
                            isScanInProgress = true
                            if (sharedPreferences.getBoolean("vibrate", false)) {
                                vibratePhone()
                            }
                            if (sharedPreferences.getBoolean("sound", false)) {
                                playBeepSound()
                            }

                            // Determine the icon based on the barcode format
                            val icon = if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                R.drawable.ic_website
                            } else {
                                R.drawable.ic_barcode
                            }

                            handleQRCode(qrCode, icon)
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle any errors
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun handleQRCode(qrCode: String, icon: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            saveQRCodeToDatabase(qrCode, icon)
            withContext(Dispatchers.Main) {
                if (!AdsProvider.interScan.isAdReady()) {
                    navigateToNextFragment(qrCode)
                    isScanInProgress = false
                } else {
                    AdsProvider.interScan.showAds(
                        activity = requireActivity(),
                        onNextAction = { adShown ->
                            navigateToNextFragment(qrCode)
                            isScanInProgress = false
                        }
                    )
                }
            }
        }
    }

    private fun vibratePhone() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    private fun playBeepSound() {
        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }


    private fun setupZoomSeekBar() {
        zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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


    private fun setupFlashButton() {
        flashButton.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Scan QR barcode screen",
                trigger = "User tap toggle flash",
                eventName = "scanqr_scr_toggle_flash"
            )
            toggleTorch()
        }
    }

    private fun setupGalleryButton() {
        galleryButton.setOnClickListener {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is already granted
                logEventAndOpenGallery()
            } else {
                // Check if we should show a rationale
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        permission
                    )
                ) {
                    // Show the permission rationale and request the permission
                    showPermissionRationaleAndRequest(permission)
                } else {
                    // Direct user to app settings after 2nd denial or if "Don't ask again" is checked
                    requestPermissionOrShowSettings(permission)
                }
            }
        }
    }


    private fun logEventAndOpenGallery() {
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Scan QR barcode screen",
            trigger = "User tap to import image from library",
            eventName = "scanqr_scr_tap_import_image"
        )
        openGallery()
    }

    private fun showPermissionRationaleAndRequest(permission: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("We need access to your media to import images. Please grant the permission.")
            .setPositiveButton("Grant") { dialog, _ ->
                // Request the permission again
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 0)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
    }

    private fun setupChangeCameraButton() {
        changeCameraButton.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Scan QR barcode screen",
                trigger = "User tap toggle camera back and front",
                eventName = "scanqr_scr_toggle_camera"
            )
            isUsingBackCamera = !isUsingBackCamera
            setupCamera()
        }
    }


    override fun onPause() {
        super.onPause()
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        Log.d("ScanCode", "onPause called")
        if (::cameraProviderFuture.isInitialized) {
            Log.d("ScanCode", "cameraProviderFuture is initialized")
            cameraProviderFuture.get()?.unbindAll()
        } else {
            Log.e("ScanCode", "cameraProviderFuture is not initialized")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        releaseCamera()
    }

    private fun releaseCamera() {
        // Unbind all use cases from the camera
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.get()?.unbindAll()
    }


    override fun onResume() {
        super.onResume()
        isNavControllerAdded()

        AdsProvider.interScan.config(
            requireActivity().getSharedPreferences(
                "RemoteConfig",
                AppCompatActivity.MODE_PRIVATE
            ).getBoolean(inter_scan, true)
        )
        AdsProvider.interScan.loadAds(MyApplication.getApplication())

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupCamera()
        }

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }
        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.qr_code_reader)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        if (back != null) {
            back.visibility = View.VISIBLE
        }
        back.setOnClickListener {
            cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            releaseCamera()
            requireActivity().onBackPressed()
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

    private fun requestPermissionOrShowSettings(permission: String) {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 0)

        // If permission was denied twice or "Don't ask again" is selected
        if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)) {
            // Show dialog directing to settings
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("It seems you've denied the permission twice. Please go to settings to enable the permission manually.")
            .setPositiveButton("Go to Settings") { dialog, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    private fun handleDatabaseResult(context: Context, result: Boolean, data: String) {
        if (result) {
            Toast.makeText(context, "Data saved successfully: $data", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to save data: $data", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close() // Close the input stream

                bitmap?.let {
                    val inputImage = InputImage.fromBitmap(it, 0)
                    val barcodeScanner = BarcodeScanning.getClient()

                    barcodeScanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                val qrCode = barcode.rawValue
                                if (qrCode != null) {
                                    isScanInProgress = true
                                    if (sharedPreferences.getBoolean("vibrate", false)) {
                                        vibratePhone()
                                    }
                                    if (sharedPreferences.getBoolean("sound", false)) {
                                        playBeepSound()
                                    }
                                    val icon = if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                        R.drawable.ic_website
                                    } else {
                                        R.drawable.ic_barcode
                                    }
                                    handleQRCode(qrCode, icon)
                                    break
                                }
                            }
                        }
                        .addOnFailureListener {
                            // Handle any errors
                        }
                }
            }
        }
    }

    fun setupBarcodeScanner() {
        // ML Kit Barcode Scanner setup
        val barcodeScanner = BarcodeScanning.getClient()
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
            processImageProxy(barcodeScanner, imageProxy)
        }
    }

    // Ensure to handle torch functionality elsewhere
    private fun toggleTorch() {
        if (::camera.isInitialized) {
            isTorchOn = !isTorchOn
            camera.cameraControl.enableTorch(isTorchOn)
            if (isTorchOn) {
                flashButton.setImageResource(R.drawable.ic_flash) // Update flash icon
            } else {
                flashButton.setImageResource(R.drawable.ic_flash_off) // Update flash icon
            }
        } else {
            Toast.makeText(requireContext(), "Camera not initialized", Toast.LENGTH_SHORT).show()
        }
    }


    private fun navigateToNextFragment(qrCode: String) {
        val currentDestination = navController?.currentDestination
        if (currentDestination?.id == R.id.nav_scancode) {
            val action = ScanCodeDirections.actionNavScancodeToNavShowcode(qrCode)
            navController?.navigate(action)
        } else {
            Log.e("NavigationError", "Cannot navigate: Current destination is ${currentDestination?.id}")
        }
    }


    private suspend fun saveQRCodeToDatabase(qrCode: String, icon: Int) {
        val currentDate = SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val result = dbHelper.insertQRCode(qrCode, "Created on $currentDate", currentTime, icon,"","scanned")
        Log.d(
            "ScanCodeFragment",
            "saveQRCodeToDatabase called. QR Code: $qrCode, Date: $currentDate, Time: $currentTime, Insert Result: $result"
        )
    }
}

