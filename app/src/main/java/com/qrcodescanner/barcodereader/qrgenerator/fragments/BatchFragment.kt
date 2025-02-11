package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.adapters.ScanListAdapter1
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import com.qrcodescanner.barcodereader.qrgenerator.models.ScannedItem
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BatchFragment : Fragment() {
    private lateinit var dbHelper: QRCodeDatabaseHelper
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var scanListRecyclerView: RecyclerView
    private lateinit var scanListAdapter: ScanListAdapter1
    private val scannedData = mutableListOf<ScannedItem>()
    private val scannedBarcodes = mutableSetOf<String>() // To track unique barcodes
    private lateinit var numberofQR: TextView
    private lateinit var QRText: TextView
    private lateinit var forward: ImageView
    private lateinit var navController: NavController
    private lateinit var topbarLayout: ConstraintLayout
    private lateinit var camera: Camera
    private lateinit var plusButton: ImageView
    private lateinit var minusButton: ImageView
    private lateinit var zoomSeekBar: SeekBar
    private lateinit var flashButton: ImageView
    private lateinit var switchCameraButton: ImageView
    private var isTorchOn = false
    private lateinit var galleryButton: ImageView
    private val REQUEST_GALLERY_IMAGE = 202
    private var isUsingBackCamera = true
    private var isScanning = false // To manage scanning state
    private var isFirstScan = true // To show clTopbar after the first scan

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_batch, container, false)
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Tab Batch",
            trigger = "App display tab Batch",
            eventName = "tab_batch_scr"
        )

        // Initialize views
        dbHelper = QRCodeDatabaseHelper(requireContext())
        numberofQR = view.findViewById(R.id.numberofQR)
        QRText = view.findViewById(R.id.QRText)
        forward = view.findViewById(R.id.forward)
        previewView = view.findViewById(R.id.camera_preview)
        scanListRecyclerView = view.findViewById(R.id.scan_list)
        scanListAdapter = ScanListAdapter1(scannedData)
        scanListRecyclerView.adapter = scanListAdapter
        topbarLayout = view.findViewById(R.id.clTopbar)
        zoomSeekBar = view.findViewById(R.id.zoomSeekBar)
        flashButton = view.findViewById(R.id.ic_flash)
        galleryButton = view.findViewById(R.id.button_gallery)
        plusButton = view.findViewById(R.id.ic_plus)
        minusButton = view.findViewById(R.id.ic_minus)
        switchCameraButton = view.findViewById(R.id.changeCamera) // Initialize switch camera button
        setupSwitchCameraButton()
        navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    CustomFirebaseEvents.logEvent(
                        context = requireActivity(),
                        screenName = "Tab Batch",
                        trigger = "User tap button Back",
                        eventName = "tab_batch_scr_tap_back"
                    )
                    val action = BatchFragmentDirections.actionBatchFragmentToHomefragment()
                    navController.navigate(action)
                }
            })
        setupZoomButtons()
        setupZoomSeekBar()
        setupFlashButton()
        setupGalleryButton()
        forward.setOnClickListener {
            if (scannedData.size < 2) {
                // Show a toast message if less than 2 QR codes/barcodes are scanned
                Toast.makeText(
                    requireContext(),
                    "Please scan at least 2 QR codes/barcodes",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Pass data to the next fragment if 2 or more QR codes/barcodes are scanned
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Tab Batch",
                    trigger = "User tap button Next",
                    eventName = "tab_batch_scr_tap_next"
                )
                passDataToNextFragment()
            }
        }

        setupCamera()
        return view
    }

    private fun setupGalleryButton() {
        galleryButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Tab Batch",
                    trigger = "User tap to import image from library",
                    eventName = "tab_batch_scr_tap_import_image"
                )
                openGallery()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    ), 0
                )
            }
        }
    }

    private fun switchCamera() {
        isUsingBackCamera = !isUsingBackCamera
        val cameraProvider = cameraProviderFuture.get()

        // Unbind all use cases before rebinding with the new camera selector
        cameraProvider.unbindAll()

        // Bind the camera use cases with the new camera selector
        bindCameraUseCases(cameraProvider)
    }

    private fun openGallery() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)  // Allow multiple selection
            }
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
    }

    private fun setupFlashButton() {
        flashButton.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Tab Batch",
                trigger = "User tap toggle flash",
                eventName = "tab_batch_scr_toggle_flash"
            )
            toggleTorch()
        }
    }

    private fun setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setupZoomButtons() {
        plusButton.setOnClickListener {
            adjustZoomLevel(increase = true)
        }

        minusButton.setOnClickListener {
            adjustZoomLevel(increase = false)
        }
    }

    // Ensure to handle torch functionality elsewhere
    private fun toggleTorch() {
        isTorchOn = !isTorchOn
        camera.cameraControl.enableTorch(isTorchOn)
        if (isTorchOn) {
            flashButton.setImageResource(R.drawable.ic_flash) // Update flash icon
        } else {
            flashButton.setImageResource(R.drawable.ic_flash_off) // Update flash icon
        }
    }

    private fun adjustZoomLevel(increase: Boolean) {
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

    private fun setupSwitchCameraButton() {
        switchCameraButton.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Tab Batch",
                trigger = "User tap toggle camera back and front",
                eventName = "tab_batch_scr_toggle_camera"
            )
            switchCamera()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val clipData = data.clipData
            if (clipData != null) {
                // User selected multiple images
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
                    processGalleryImage(imageUri)  // Process each image
                }
            } else {
                // User selected a single image
                val imageUri = data.data
                if (imageUri != null) {
                    processGalleryImage(imageUri)  // Process the single image
                }
            }
        }
    }

    private fun processGalleryImage(uri: Uri) {
        try {
            val image: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
            val inputImage = InputImage.fromBitmap(image, 0)
            val barcodeScanner = BarcodeScanning.getClient()

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes.first()
                        val barcodeData = barcode.rawValue ?: ""

                        if (barcodeData.isNotEmpty() && !scannedBarcodes.contains(barcodeData)) {
                            scannedBarcodes.add(barcodeData)

                            val scannedItem = ScannedItem(
                                data = barcodeData,
                                date = getCurrentDate(),
                                time = getCurrentTime(),
                                isQrCode = barcode.format == Barcode.FORMAT_QR_CODE
                            )

                            onBarcodeScanned(scannedItem)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BatchFragment", "Error processing image", e)
                }
        } catch (e: IOException) {
            Log.e("BatchFragment", "Error loading image", e)
        }
    }

    private fun setupZoomSeekBar() {
        if (!::camera.isInitialized) {
            // Handle the case when camera is not initialized
            return
        }

        zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minZoomRatio = camera.cameraInfo.zoomState.value?.minZoomRatio ?: return
                val maxZoomRatio = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: return
                val zoomRatio = minZoomRatio + (progress / 100f) * (maxZoomRatio - minZoomRatio)
                camera.cameraControl.setZoomRatio(zoomRatio)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val barcodeScanner = BarcodeScanning.getClient()
        val imageAnalysis = ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(
                    ContextCompat.getMainExecutor(requireContext()),
                    BarcodeAnalyzer(barcodeScanner)
                )
            }

        val cameraSelector = if (isUsingBackCamera) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        camera = cameraProvider.bindToLifecycle(
            this,
            cameraSelector,
            preview,
            imageAnalysis
        )
    }

    private inner class BarcodeAnalyzer(
        private val barcodeScanner: BarcodeScanner
    ) : ImageAnalysis.Analyzer {

        @SuppressLint("UnsafeOptInUsageError")
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(image: ImageProxy) {
            if (!isScanning) {
                isScanning = true
                // Convert ImageProxy to InputImage
                val mediaImage = image.image
                if (mediaImage != null) {
                    val inputImage =
                        InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

                    barcodeScanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                val barcode = barcodes.first()
                                val barcodeData = barcode.rawValue ?: ""

                                // Check if the barcode has already been scanned
                                if (barcodeData.isNotEmpty() && !scannedBarcodes.contains(
                                        barcodeData
                                    )
                                ) {
                                    scannedBarcodes.add(barcodeData)

                                    // Create a ScannedItem object
                                    val scannedItem = ScannedItem(
                                        data = barcodeData,
                                        date = getCurrentDate(), // Implement this method to get the current date
                                        time = getCurrentTime(), // Implement this method to get the current time
                                        isQrCode = barcode.format == Barcode.FORMAT_QR_CODE
                                    )

                                    // Pass the ScannedItem to the onBarcodeScanned method
                                    onBarcodeScanned(scannedItem)
                                }
                            }
                        }
                        .addOnCompleteListener {
                            image.close() // Close the image to release resources
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            image.close()
                        }
                        .addOnCompleteListener {
                            // Delay before allowing the next scan
                            Handler(Looper.getMainLooper()).postDelayed({
                                isScanning = false // Set scanning state to false after the delay
                            }, 500) // Adjust delay as needed
                        }
                } else {
                    image.close()
                    isScanning = false
                }
            } else {
                image.close() // If scanning is ongoing, close the image to release resources
            }
        }

    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
        return timeFormat.format(Date())
    }

    private fun onBarcodeScanned(scannedItem: ScannedItem) {
        if (isFirstScan) {
            // Show the topbar only after the first scan
            topbarLayout.visibility = View.VISIBLE
            isFirstScan = false
        }
        insertDataIntoDatabase(scannedItem.data, scannedItem.isQrCode)
        scannedData.add(scannedItem)
        updateUI()
    }

    private fun insertDataIntoDatabase(qrCodeText: String, isQrCode: Boolean) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("H:mm a", Locale.getDefault())
        val date = dateFormat.format(Date())
        val time = timeFormat.format(Date())

        // Choose the drawable resource based on whether it's a QR code or barcode
        val drawableResId = if (isQrCode) {
            R.drawable.create_code
        } else {
            R.drawable.ic_barcode
        }

        val success = dbHelper.insertQRCode(qrCodeText, date, time, drawableResId,"","")
        if (success) {
            Toast.makeText(requireContext(), "Data inserted successfully", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Failed to insert data", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateUI() {
        numberofQR.text = scannedData.size.toString()
        val lastScannedItem = scannedData.lastOrNull()
        QRText.text = lastScannedItem?.data ?: ""
    }

    private fun passDataToNextFragment() {
        val action =
            BatchFragmentDirections.actionBatchFragmentToNextFragment(scannedData.toTypedArray())
        navController.navigate(action)
    }

    // Helper function to get current date and time
    private fun getCurrentDateTime(): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = dateFormat.format(Date())
        val time = timeFormat.format(Date())
        return Pair(date, time)
    }

    override fun onResume() {
        super.onResume()
        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.batch_scanner)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        if (back != null) {
            back.visibility = View.VISIBLE
        }
        back.setOnClickListener {
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

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }
    }
}

