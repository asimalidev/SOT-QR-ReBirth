package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentCropperImageSearchBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.Utils.hideSystemUI
import java.io.File
import java.io.FileOutputStream

class CropperImageSearchFragment : Fragment() {

    var navController: NavController? = null
    private lateinit var viewBinding: FragmentCropperImageSearchBinding
    var imagePath = ""
    var btnBack: ImageView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentCropperImageSearchBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()
        hideSystemUI(requireActivity())
        val args: CropperImageSearchFragmentArgs by navArgs()
        imagePath = args.imagePath
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController != null) {
                    val action = CropperImageSearchFragmentDirections.actionNavCropperImageSearchToNavImageSearch()
                    navController!!.navigate(action)
                } else {
                    isNavControllerAdded()
                }
            }
        })

        if (imagePath != "") {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            viewBinding.cropImageView.setImageBitmap(bitmap)
        }

        initializeHeader()

        viewBinding.btnCancel.setOnClickListener { requireActivity().onBackPressed() }
        viewBinding.btnOK.setOnClickListener {
            if (NetworkCheck.isNetworkAvailable(requireActivity())) {
                viewBinding.clProgress.visibility = View.VISIBLE

                viewBinding.btnCancel.isEnabled = false
                viewBinding.btnOK.isEnabled = false

                val croppedImage = viewBinding.cropImageView.croppedImage
                imagePath = croppedImage?.let { it1 -> saveBitmapToFile(it1) }!!
                if (imagePath != "") {
                    btnBack?.let {
                        it.isEnabled = false
                    }
                    uploadImageToFirebase(imagePath)
                } else {
                    if (navController != null) {
                        val action = CropperImageSearchFragmentDirections.actionNavCropperImageSearchToNavImageSearch()
                        navController!!.navigate(action)
                    } else {
                        isNavControllerAdded()
                    }
                }
            } else {
                viewBinding.clProgress.visibility = View.VISIBLE
                viewBinding.progress.visibility = View.GONE
                viewBinding.txProgressText.text = "No Internet...!"
                Handler().postDelayed({
                    viewBinding.clProgress.visibility = View.GONE
                    viewBinding.progress.visibility = View.VISIBLE
                    viewBinding.txProgressText.text = ""
                }, 3000)
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val fileName = "ImageSearchTemp_${System.currentTimeMillis()}.jpg"
        val outputDirectory = getOutputDirectory()
        val photoFile = File(outputDirectory, fileName)
        FileOutputStream(photoFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return photoFile.absolutePath
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    private fun initializeHeader() {
        val topText: TextView = requireActivity().findViewById(R.id.mainText)
        topText.visibility = View.VISIBLE
        topText.text = getString(R.string.crop_image)

        btnBack = requireActivity().findViewById(R.id.ivBack)
        if (btnBack != null) {
            btnBack?.visibility = View.VISIBLE
            btnBack?.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }

        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        if (setting != null) {
            setting.visibility = View.INVISIBLE
        }
        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        if (ivClose != null) {
            ivClose.setImageResource(R.drawable.ic_premium)
            ivClose.visibility = View.INVISIBLE
        }
    }

    private fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    private fun uploadImageToFirebase(imagePath: String) {
        val storageRef = Firebase.storage.reference
        val file = Uri.fromFile(File(imagePath))
        val imageRef = storageRef.child("${imagePath.toUri().lastPathSegment}")

        val messages = listOf(
            "Preparing your image...",
            "Hold on a moment...",
            "Almost there...",
            "Fetching the best results for you...",
            "Processing your request...",
            "Optimizing for better results...",
            "Just a few more seconds...",
            "Finalizing your search...",
            "Hang tight, we're on it!"
        )
        var messageIndex = 0

        val handler = android.os.Handler()
        val updateMessageRunnable = object : Runnable {
            override fun run() {
                if (messageIndex < messages.size) {
                    viewBinding.txProgressText.text = messages[messageIndex]
                    messageIndex++
                    handler.postDelayed(this, 5000)
                } else {
                    messageIndex = 0
                    handler.postDelayed(this, 5000)
                }
            }
        }
        handler.post(updateMessageRunnable)

        val uploadTask = imageRef.putFile(file)
        uploadTask.addOnSuccessListener {
            handler.removeCallbacks(updateMessageRunnable)
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                Log.d("FirebaseUpload", "Download URL: $downloadUrl")
                requireActivity().getSharedPreferences("DownloadURL", Context.MODE_PRIVATE)
                    .edit()
                    .putString("DownloadURL", downloadUrl)
                    .apply()
                navController?.navigate(CropperImageSearchFragmentDirections.actionNavCropperImageSearchToNavDeepLinkingWebView())
            }
        }.addOnFailureListener { exception ->
            handler.removeCallbacks(updateMessageRunnable) // Stop message updates
            btnBack?.isEnabled = true
            viewBinding.btnCancel.isEnabled = true
            viewBinding.btnOK.isEnabled = true
            viewBinding.txProgressText.text = "Processing Failed"
            Log.e("FirebaseUpload", "Upload failed: ${exception.message}")
            Toast.makeText(requireContext(), "Processing Failed!", Toast.LENGTH_SHORT).show()
        }
    }
}