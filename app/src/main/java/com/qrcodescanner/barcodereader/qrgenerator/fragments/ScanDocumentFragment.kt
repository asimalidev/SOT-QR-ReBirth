package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentScanDocumentBinding
import java.io.File
import java.io.IOException



class ScanDocumentFragment : Fragment() {
    private lateinit var binding: FragmentScanDocumentBinding
    private var currentPhotoPath: String? = null

    companion object {
        private const val pic_id = 123
        private const val CAMERA_REQUEST_CODE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, proceed with the camera intent
            openCamera()
        } else {
            // Request the camera permission
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        // Request the CAMERA permission
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    private fun openCamera() {
        val photoFile: File? = createImageFile()
        photoFile?.also {
            // Create a content URI for the file using FileProvider
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.qrcodescanner.barcodereader.qrgenerator.fileprovider", // Your package name
                it
            )

            // Create camera intent and pass the photo URI as the output location
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }

            // Start the camera activity
            startActivityForResult(cameraIntent, pic_id)
        }
    }


    private fun createImageFile(): File? {
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            // Create a unique file name
            File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            ).apply {
                // Save a reference to the file path
                currentPhotoPath = absolutePath
            }
        } catch (ex: IOException) {
            // Handle the error
            null
        }
    }

    // Handle the result of permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                openCamera()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // This method will help to retrieve the image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Match the request 'pic id with requestCode
        if (requestCode == pic_id && resultCode == AppCompatActivity.RESULT_OK) {
            // Load the full-resolution image from the file path
            val file = File(currentPhotoPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                // Set the image in ImageView for display
                binding.clickImage.setImageBitmap(bitmap)
            }
        }
    }
}




