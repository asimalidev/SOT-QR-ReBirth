package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentImageSearchBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.Utils.hideSystemUI
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageSearchFragment : Fragment() {

    var navController: NavController? = null
    private lateinit var viewBinding: FragmentImageSearchBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isUsingBackCamera = true
    private lateinit var camera: Camera
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var preview: Preview
    private var imageUri: Uri? = null
    private var isTorchOn = false

    companion object {
        private const val TAG = "CameraXApp"
    }

    override fun onStart() {
        super.onStart()
        val activity = requireActivity() as HomeActivity
        activity.changeVisibility(bool = false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentImageSearchBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()
        hideSystemUI(requireActivity())
        val activity = requireActivity() as HomeActivity
        activity.updateAdLayoutVisibility(shouldShowAd = false)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Tab ImageSearch",
                    trigger = "User tap button Back",
                    eventName = "tab_image_search_scr_tap_back")
                val activity = requireActivity() as HomeActivity
                activity.changeVisibility(bool = true)
                if (navController != null) {
                    val action = ImageSearchFragmentDirections.actionNavImageSearchToNavHome()
                    navController?.navigate(action)
                } else {
                    isNavControllerAdded()
                }
            }
        })
        initializeHeader()

        cameraExecutor = Executors.newSingleThreadExecutor()
        viewBinding.icFlashoff.isEnabled = false
        preview = Preview.Builder()
            .setTargetRotation(requireActivity().windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0)
            .build()
        startCamera()
        setupFlashButton()
        setupSwitchCameraButton()

        viewBinding.ivClose.setOnClickListener {
            val activity = requireActivity() as HomeActivity
            activity.changeVisibility(bool = true)
            if (navController != null) {
                val action = ImageSearchFragmentDirections.actionNavImageSearchToNavHome()
                navController?.navigate(action)
            } else {
                isNavControllerAdded()
            }
        }
        viewBinding.btnCapture.setOnClickListener {
            takePhoto()
        }
        viewBinding.icGallery.setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            pickImageLauncher.launch(intent)
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            startPreviewActivity(imageUri.toString())
        }
    }

    private fun startPreviewActivity(imageUri: String?) {
        if (imageUri != null) {
            val imagePath = getAbsolutePathFromUri(imageUri.toUri())

            if (navController != null) {
                val action = ImageSearchFragmentDirections.actionNavImageSearchToNavCropperImageSearch(imagePath)
                navController!!.navigate(action)
            } else {
                isNavControllerAdded()
            }
        }
    }

    private fun getAbsolutePathFromUri(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
        return cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            it.getString(columnIndex)
        } ?: uri.path ?: ""
    }

    private fun initializeHeader() {
        val toolbar = requireActivity().findViewById<View>(R.id.inclToolBar)
        toolbar.visibility = View.GONE
    }

    private fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        viewBinding.progress.visibility = View.INVISIBLE
        viewBinding.progressBar.visibility = View.VISIBLE
        val fileName = "ImageSearchTemp_${System.currentTimeMillis()}.jpg"
        val outputDirectory = getOutputDirectory()
        val photoFile = File(outputDirectory, fileName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireActivity()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    viewBinding.progressBar.visibility = View.GONE
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    viewBinding.progressBar.visibility = View.GONE

                    // Rotate the image if necessary
                    rotateImageIfRequired(photoFile)

                    // Notify media scanner
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(photoFile.absolutePath),
                        arrayOf("image/png")
                    ) { path, uri ->
                        Log.d("MediaScanner", "File scanned: $path, Uri: $uri")
                    }

                    // Start preview activity with corrected image
                    startPreviewActivity(photoFile.absolutePath)
                }
            }
        )
    }

    private fun rotateImageIfRequired(photoFile: File) {
        try {
            val ei = ExifInterface(photoFile.absolutePath)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }

            // Save the rotated bitmap back to the file
            val fos = FileOutputStream(photoFile)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to rotate image: ${e.message}")
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    private fun setupFlashButton() {
        viewBinding.icFlashoff.setOnClickListener {
            toggleTorch()
        }
    }

    private fun setupSwitchCameraButton() {
        viewBinding.ivRotate.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Tab Batch",
                trigger = "User tap toggle camera back and front",
                eventName = "tab_batch_scr_toggle_camera"
            )
            switchCamera()
        }
    }

    private fun toggleTorch() {
        if (::camera.isInitialized) {
            isTorchOn = !isTorchOn
            camera.cameraControl.enableTorch(isTorchOn)
            if (isTorchOn) {
                viewBinding.icFlashoff.setImageResource(R.drawable.ic_flash)
            } else {
                viewBinding.icFlashoff.setImageResource(R.drawable.ic_flash_off)
            }
        } else {
            Toast.makeText(requireActivity(), "Camera not initialized", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        Log.d("ScanCode", "setupCamera called")
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            Log.d("ScanCode", "cameraProviderFuture initialization completed")
            preview = Preview.Builder().build()
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = if (isUsingBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                preview.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                viewBinding.icFlashoff.isEnabled = true
            } catch (exc: Exception) {
                Log.e("ScanCode", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun switchCamera() {
        isUsingBackCamera = !isUsingBackCamera
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()
        bindCameraUseCases(cameraProvider)
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }

        val cameraSelector = if (isUsingBackCamera) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview)
    }
}