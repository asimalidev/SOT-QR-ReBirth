package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FinalImageFragment : Fragment(), View.OnTouchListener {
    private lateinit var selectedImageView: ImageView
    private lateinit var overlayImageView: ImageView
    private lateinit var pasteCodeButton: Button
    private lateinit var saveButton: Button
    private lateinit var navController: NavController
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var scaleFactor = 1f
    private var rotationAngle = 0f
    private var matrix = Matrix()
    private var savedMatrix = Matrix()
    private var mode = NONE
    private val PICK_IMAGE_REQUEST = 1
    private var start = PointF()
    private var mid = PointF()
    private var oldDist = 1f
    private var d = 0f
    private var newRot = 0f
    private var lastEvent: FloatArray? = null
    private var isImagePicked = false // Flag to check if an image is picked
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector
    private lateinit var qrCodeBitmap: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_final_image, container, false)
        selectedImageView = view.findViewById(R.id.selectedImageView)
        overlayImageView = view.findViewById(R.id.overlayImageView)
        pasteCodeButton = view.findViewById(R.id.pasteCodeButton)
        saveButton = view.findViewById(R.id.saveButton)
        navController = findNavController()

        val args = FinalImageFragmentArgs.fromBundle(requireArguments())
        val filePath = args.bitmapByteArray
        qrCodeBitmap = BitmapFactory.decodeFile(filePath) ?: throw IllegalArgumentException("Invalid file path")
        // Initialize the overlay image
        overlayImageView.setImageBitmap(qrCodeBitmap)
        overlayImageView.visibility = View.VISIBLE // Ensure it's visible initially
        overlayImageView.scaleType = ImageView.ScaleType.MATRIX
        matrix.postTranslate(0f, 0f)
        overlayImageView.imageMatrix = matrix

        // Initially hide the save button
        saveButton.visibility = View.INVISIBLE

        // Set up button click listeners
        pasteCodeButton.setOnClickListener {
            // Open gallery to pick an image
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        saveButton.setOnClickListener {
            // Combine and save the image
            combineAndSaveBitmap()
        }

        setupGestureDetection()
        overlayImageView.setOnTouchListener(this)
        return view
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // Make sure to cast 'v' to 'ImageView' safely
        val imageView = v as? ImageView ?: return false

        if (!isImagePicked) {
            // If image is not picked, do not allow moving the overlay image
            return false
        }

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
                lastEvent = null
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
                lastEvent = FloatArray(4).apply {
                    this[0] = event.getX(0)
                    this[1] = event.getX(1)
                    this[2] = event.getY(0)
                    this[3] = event.getY(1)
                }
                d = rotation(event)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_MOVE -> {
                when (mode) {
                    DRAG -> {
                        matrix.set(savedMatrix)
                        val dx = event.x - start.x
                        val dy = event.y - start.y
                        matrix.postTranslate(dx, dy)
                    }

                    ZOOM -> {
                        val newDist = spacing(event)
                        if (newDist > 10f) {
                            matrix.set(savedMatrix)
                            val scale = newDist / oldDist
                            matrix.postScale(scale, scale, mid.x, mid.y)
                        }
                        lastEvent?.let {
                            if (event.pointerCount == 2 || event.pointerCount == 3) {
                                newRot = rotation(event)
                                val r = newRot - d
                                val values = FloatArray(9)
                                matrix.getValues(values)
                                val tx = values[Matrix.MTRANS_X]
                                val ty = values[Matrix.MTRANS_Y]
                                val sx = values[Matrix.MSCALE_X]
                                val xc = (imageView.width / 2) * sx
                                val yc = (imageView.height / 2) * sx
                                matrix.postRotate(r, tx + xc, ty + yc)
                            }
                        }
                    }
                }
            }
        }
        imageView.imageMatrix = matrix
        return true
    }

    private fun setupGestureDetection() {
        // Initialize ScaleGestureDetector for pinch-to-zoom
        scaleGestureDetector = ScaleGestureDetector(
            requireContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    // Update scale factor and apply it to the matrix
                    scaleFactor *= detector.scaleFactor
                    scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f))
                    matrix.setScale(scaleFactor, scaleFactor)
                    matrix.postRotate(rotationAngle)
                    overlayImageView.imageMatrix = matrix
                    return true
                }

                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    super.onScaleEnd(detector)
                }
            })

        // Initialize GestureDetector for double-tap
        gestureDetector =
            GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    // Rotate on double tap
                    rotationAngle += 45f
                    matrix.setRotate(rotationAngle, e.x, e.y)
                    matrix.postScale(scaleFactor, scaleFactor)
                    overlayImageView.imageMatrix = matrix
                    return true
                }
            })
    }

    private fun combineAndSaveBitmap() {
        // Make sure the layout is fully drawn
        requireView().post {
            // Create a bitmap from the ConstraintLayout
            val finalBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )

            // Save the final bitmap
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageToDownloadsQAndAbove(finalBitmap)
            } else {
                saveImageToDownloadsLegacy(finalBitmap)
            }

            // Optional: Recycle bitmap if you are done with it
            finalBitmap.recycle()
        }
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return this.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveImageToDownloadsQAndAbove(bitmap: Bitmap) {
        val mainFolderName = "QR Code Scanner"
        val subFolderName = "Created QR"
        val fileName = "QRCode_${System.currentTimeMillis()}.png"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val qrCodeScannerDir = File(downloadsDir, mainFolderName)
        val createdQRDir = File(qrCodeScannerDir, subFolderName)
        // Create the main folder and subfolder if they don't exist
        if (!createdQRDir.exists()) {
            createdQRDir.mkdirs()
        }
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$mainFolderName/$subFolderName")
        }

        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Saving image in download...")
            isIndeterminate = true
            setCancelable(false)
            show()
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(requireContext(), "Image saved to Downloads/$mainFolderName/$subFolderName", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                    val action = FinalImageFragmentDirections.actionFastToHome()
                    navController.navigate(action)
                }, 1000) // Show the progress bar for 1 second
            }
        } else {
            Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            try {
                // Use content resolver to get InputStream
                val inputStream = requireContext().contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedImageView.setImageBitmap(bitmap)
                selectedImageView.visibility = View.VISIBLE // Make sure it's visible
                isImagePicked = true // Set the flag to true when an image is picked
                // Show the save button
                saveButton.visibility = View.VISIBLE
                pasteCodeButton.visibility = View.INVISIBLE

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToDownloadsLegacy(bitmap: Bitmap) {
        val mainFolderName = "QR Code Scanner"
        val subFolderName = "Created QR"
        val fileName = "QRCode_${System.currentTimeMillis()}.png"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val qrCodeScannerDir = File(downloadsDir, mainFolderName)
        val createdQRDir = File(qrCodeScannerDir, subFolderName)
        // Create the main folder and subfolder if they don't exist
        if (!createdQRDir.exists()) {
            createdQRDir.mkdirs()
        }

        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Saving image in download...")
            isIndeterminate = true
            setCancelable(false)
            show()
        }

        val file = File(createdQRDir, fileName)
        try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(requireContext(), "Image saved to Downloads/$mainFolderName/$subFolderName", Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
        } finally {
            progressDialog.dismiss()
        }
    }



    private fun createBitmapFromView(ctx: Context, view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    private fun rotation(event: MotionEvent): Float {
        val deltaX = (event.getX(0) - event.getX(1)).toDouble()
        val deltaY = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(deltaY, deltaX)
        return Math.toDegrees(radians).toFloat()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()

        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.qr_code_reader)

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


        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.VISIBLE
        }

        download.setOnClickListener {
            saveImageToDownloads(qrCodeBitmap)
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        if (ivClose != null) {
            ivClose.setImageResource(R.drawable.ic_premium)
            ivClose.visibility = View.INVISIBLE
        }
    }


    private fun saveImageToDownloads(bitmap: Bitmap) {
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Create QR code screen",
            trigger = "User tap button Save",
            eventName = "createqr_scr_tap_save"
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToDownloadsQAndAbove1(bitmap)
        } else {
            saveImageToDownloadsLegacy1(bitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveImageToDownloadsQAndAbove1(bitmap: Bitmap) {
        val fileName = "QRCode_${System.currentTimeMillis()}.png"
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Saving image in download...")
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.show()

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(requireContext(), "Image saved to Downloads", Toast.LENGTH_SHORT)
                    .show()
                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                    val action = FinalImageFragmentDirections.actionFastToHome()
                    navController.navigate(action)
                }, 1000) // Show the progress bar for 1 second
            }
        } else {
            Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
    }

    private fun saveImageToDownloadsLegacy1(bitmap: Bitmap) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        } else {
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Saving image in download...")
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            progressDialog.show()

            val directory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(directory, "QRCode_${System.currentTimeMillis()}.png")
            try {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Toast.makeText(requireContext(), "Image saved to Downloads", Toast.LENGTH_SHORT)
                        .show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()
                        val action = FinalImageFragmentDirections.actionFastToHome()
                        navController.navigate(action)
                    }, 1000) // Show the progress bar for 1 second
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }
    }
}









