package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import apero.aperosg.monetization.util.showBannerAd
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.element.Image
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.banner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class NewCreateActivity : AppCompatActivity() {
    private lateinit var documentName: String
    private lateinit var documentNameTextView: TextView
    private lateinit var imageUris: ArrayList<Uri>
    private lateinit var progressDialog: ProgressDialog
    private lateinit var imageListContainer: LinearLayout
    private var adLoadCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_create)
        hideSystemUI()
        imageUris = intent.getParcelableArrayListExtra("imageUris") ?: arrayListOf()
        documentName = intent.getStringExtra("documentName") ?: "Document"
        checkNetworkAndLoadAds() // Check network and load ads in onCreate
        // Initialize views
        documentNameTextView = findViewById(R.id.documentName)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val editDocumentNameButton = findViewById<ImageView>(R.id.editDocumentName)
        val saveDocumentButton = findViewById<Button>(R.id.saveDocumentButton)
        imageListContainer = findViewById(R.id.imageListContainer)
        loadImages()
        editDocumentNameButton.setOnClickListener {
            showRenameDialog()
        }

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                // Pass a flag to indicate we are coming back from CreateDocumentActivity
                putExtra("fromCreateDocument", true)
            }
            startActivity(intent)
        }

        saveDocumentButton.setOnClickListener {
            val intent = Intent(
                this@NewCreateActivity,
                SaveDocumentActivity::class.java
            )
            intent.putExtra("documentName", documentName)
            intent.putParcelableArrayListExtra("imageUris", imageUris)
            startActivity(intent) }
    }

    private fun checkNetworkAndLoadAds() {
        val adLayout: FrameLayout = findViewById(R.id.bannerFr)
        if (NetworkCheck.isNetworkAvailable(this) && getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(banner, true)) {
            loadShowBannerAd()
            adLayout.visibility = View.VISIBLE
        } else {
            adLayout.visibility = View.GONE // Hide the ad layout if no network
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
    }

    private fun showRenameDialog() {
        // Create an EditText for user input
        val editText = EditText(this).apply {
            setText(documentName) // Set current document name as default
        }


        // Build the AlertDialog
        AlertDialog.Builder(this)
            .setTitle("Edit Name")
            .setView(editText) // Set the EditText as dialog view
            .setPositiveButton("OK") { dialog, _ ->
                // Update the document name with user input
                documentName = editText.text.toString()
                documentNameTextView.text = documentName // Set the updated document name

                dialog.dismiss() // Dismiss the dialog
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel() // Cancel the dialog
            }
            .create()
            .show() // Show the dialog
    }

    // Inflate the menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_example, menu)
        return true // Indicate that the menu was created successfully
    }

    // Handle menu item selections
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_pdf -> {
                shareAsPdf(documentName, imageUris)
                true
            }

            R.id.action_share_image -> {
                shareAsJpg(documentName, imageUris)
                true
            }

            R.id.action_export_pdf -> {
                exportAsPdf(documentName, imageUris)
                true
            }

            R.id.action_export_image -> {
                exportAsJpg(documentName, imageUris)
                true
            }

            else -> super.onOptionsItemSelected(item) // Handle other menu items
        }
    }

    override fun onBackPressed() {
        handleBackPress(this)
        val abc = 3
        if (abc == 5) {
            super.onBackPressed()
        }
    }

    fun handleBackPress(activity: NewCreateActivity) {
        // Log the back press
        Log.e("BackPress", "System Back press triggered")

        // Create the intent to navigate back to HomeActivity
        val intent = Intent(activity, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // Pass a flag to indicate we are coming back from CreateDocumentActivity
            putExtra("fromCreateDocument", true)
        }

        // Start the HomeActivity
        activity.startActivity(intent)

        // Finish the current activity
        activity.finish()
    }



    private fun loadImages() {
        imageListContainer.removeAllViews() // Clear any existing views
        for (index in imageUris.indices) {
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setImageURI(imageUris[index]) // Load the image URI
                scaleType = ImageView.ScaleType.FIT_CENTER // Scale the image
                adjustViewBounds = true // Maintain aspect ratio
            }

            // Create a TextView for the image number
            val textView = TextView(this).apply {
                text = "${index + 1}/${imageUris.size}"
                setPadding(4, 4, 4, 4)
                gravity = Gravity.CENTER
                textSize = 16f // Set text size
                setTextColor(ContextCompat.getColor(this@NewCreateActivity, R.color.black)) // Assuming you have a color resource defined
            }

            // Create a FrameLayout to overlay the TextView on the ImageView
            val frameLayout = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                addView(imageView) // Add the image view to the container
                addView(textView) // Add the text view to the container on top of the image
            }

            // Add the FrameLayout to the LinearLayout
            imageListContainer.addView(frameLayout)
        }
    }




    private fun shareAsPdf(documentName: String, imageUris: List<Uri>) {
        CoroutineScope(Dispatchers.IO).launch {
            // Create a PDF document
            val pdfFile = File(cacheDir, "$documentName.pdf")
            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = com.itextpdf.layout.Document(pdfDocument)

            try {
                imageUris.forEach { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                    // Create an Image instance from the bitmap
                    val img = Image(ImageDataFactory.create(stream.toByteArray()))
                    document.add(img)
                }
                document.close()

                // Share the PDF on the main thread
                withContext(Dispatchers.Main) {
                    val pdfUri = FileProvider.getUriForFile(
                        this@NewCreateActivity,
                        "com.qrcodescanner.barcodereader.qrgenerator.fileprovider",
                        pdfFile
                    )
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, pdfUri)
                        type = "application/pdf"
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@NewCreateActivity,
                        "Error creating PDF: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun shareAsJpg(documentName: String, imageUris: List<Uri>) {
        // Convert the file URIs to content URIs
        val contentUris = imageUris.map { uri ->
            FileProvider.getUriForFile(
                this,
                "com.qrcodescanner.barcodereader.qrgenerator.fileprovider",
                File(uri.path!!)
            )
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(contentUris))
            type = "image/jpeg"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(shareIntent, "Share Images"))
    }

    private fun exportAsPdf(documentName: String, imageUris: List<Uri>) {
        // Show the ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Exporting PDF, please wait...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val pdfDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "QR Code Scanner"
            )
            if (!pdfDir.exists() && !pdfDir.mkdirs()) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@NewCreateActivity,
                        "Failed to create directory for PDF.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            val pdfFile = File(pdfDir, "$documentName.pdf")
            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = com.itextpdf.layout.Document(pdfDocument)

            try {
                imageUris.forEach { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                    val img = Image(ImageDataFactory.create(stream.toByteArray()))
                    document.add(img)
                }
                document.close()

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@NewCreateActivity,
                        "PDF exported to ${pdfFile.absolutePath}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@NewCreateActivity,
                        "Error creating PDF: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun exportAsJpg(documentName: String, imageUris: List<Uri>) {
        // Show the ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Exporting Images, please wait...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create the "QR Code Scanner/Scanned Image" directory in Downloads
                val baseDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "QR Code Scanner"
                )
                val jpgDir = File(
                    baseDir,
                    "Scanned Image"
                ) // Create the "Scanned Image" folder inside "QR Code Scanner"

                // Create directory if it doesn't exist
                if (!jpgDir.exists() && !jpgDir.mkdirs()) {
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@NewCreateActivity,
                            "Failed to create directory for images.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val imageUrisToShare = ArrayList<Uri>()
                imageUris.forEach { uri ->
                    try {
                        // Decode the image based on Android version
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(contentResolver, uri)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        }

                        // Create file to save the image
                        val imageFile = File(jpgDir, "Document${System.currentTimeMillis()}.jpg")
                        val outputStream = FileOutputStream(imageFile)

                        try {
                            // Compress the bitmap and save as JPG
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            imageUrisToShare.add(
                                FileProvider.getUriForFile(
                                    this@NewCreateActivity,
                                    "com.qrcodescanner.barcodereader.qrgenerator.fileprovider",
                                    imageFile
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                progressDialog.dismiss()
                            }
                        } finally {
                            outputStream.close()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            progressDialog.dismiss()
                        }
                    }
                }

                // Notify user once images are successfully saved
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@NewCreateActivity,
                        "Images exported to ${jpgDir.absolutePath}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@NewCreateActivity,
                        "Unexpected error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            controller?.hide(WindowInsets.Type.systemBars())
            controller?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // For Android 10 and below
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }
}