package com.qrcodescanner.barcodereader.qrgenerator.activities
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageView
import com.qrcodescanner.barcodereader.qrgenerator.R
import java.io.File
import java.io.FileOutputStream


class CropActivity : AppCompatActivity() {

    private lateinit var cropImageView: CropImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        hideSystemUI()

        supportActionBar?.hide()
        cropImageView = findViewById(R.id.cropImageView)

        // Get the image path from the Intent
        val imagePath = intent.getStringExtra("EXTRA_IMAGE_PATH")
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            cropImageView.setImageBitmap(bitmap)
        }

        findViewById<Button>(R.id.btnCrop).setOnClickListener {
            val croppedImage = cropImageView.croppedImage
            // Save the cropped image to a file
            val croppedImagePath = croppedImage?.let { it1 -> saveBitmapToFile(it1) }
            // Return the file path in the result Intent
            val resultIntent = Intent()
            resultIntent.putExtra("CROPPED_IMAGE_PATH", croppedImagePath)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    // Function to save the Bitmap to a file
    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val file = File(filesDir, "cropped_image.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // Save as PNG with 100% quality
        }
        return file.absolutePath
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

