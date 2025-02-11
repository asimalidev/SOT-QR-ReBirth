package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import apero.aperosg.monetization.util.showNativeAd
import com.google.android.material.tabs.TabLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.adapters.BackgroundGradientAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.BackgroundGradientColorAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.BackgroundRecyclerAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.ColorRecyclerAdapter
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.ColorItem
import com.qrcodescanner.barcodereader.qrgenerator.utils.GradientItem
import com.qrcodescanner.barcodereader.qrgenerator.utils.ImageItem
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_result
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumMap
import java.util.Locale

class CreateEmailQRFragment : Fragment(), ColorRecyclerAdapter.OnItemClickListener,
    BackgroundGradientAdapter.onGradientColorItemClick {
    private lateinit var colorPickerView: ColorPickerView
    private lateinit var navController: NavController
    private lateinit var qrCodeBitmap: Bitmap
    private lateinit var qrCodeImageView: ImageView
    private lateinit var save: AppCompatButton
    private lateinit var layoutAdNative: FrameLayout
    private lateinit var colorRecyclerAdapter: ColorRecyclerAdapter
    private lateinit var backgroundRecyclerAdapter: BackgroundRecyclerAdapter
    private lateinit var bacgroundgradientRecyclerAdapter: BackgroundGradientColorAdapter
    private var currentColor: Int = Color.BLACK // Default color for QR code
    private lateinit var tabLayout: TabLayout
    private lateinit var qrData: String
    private lateinit var backgroundLayout: ConstraintLayout
    private lateinit var forgoundLayout: ConstraintLayout
    private lateinit var dbHelper: QRCodeDatabaseHelper

    //    private lateinit var imageRecyclerAdapter: ImageRecyclerAdapter
    var background = Color.WHITE // Example background color
    private var isClColorsClicked = false
    private var eyeShapeType: EyeShapeType = EyeShapeType.DEFAULT


    var selectedFrameColor: Int = Color.parseColor("#1B1A1A")
    var currentDrawableResource: Int? = null
    private lateinit var templetenew: ConstraintLayout

    enum class EyeShapeType {
        CIRCLE, SQUARE, DIAMOND, ROUNDED_SQUARE, DEFAULT, CIRCLE_DIAMOND, CIRCLE_CIRCLE, CIRCLE_SQUARE, ROUNDED1, ROUNDED2, ROUNDED3, ROUNDED4
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_email_q_r, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colorPickerView = view.findViewById(R.id.colorPickerView)
        val clColors: ConstraintLayout = view.findViewById(R.id.clColors)
        val clTablayout: ConstraintLayout = view.findViewById(R.id.layoutForeground)
        val clCustomization: ConstraintLayout = view.findViewById(R.id.clCustomization)
        tabLayout = view.findViewById(R.id.tabLayoutColors)
        backgroundLayout = view.findViewById(R.id.constraintLayoutBackground)
        forgoundLayout = view.findViewById(R.id.constraintLayoutForeground)
        tabLayout.selectTab(tabLayout.getTabAt(0))
        qrCodeImageView = view.findViewById(R.id.qrCodeImageView)
        val clLogo: ConstraintLayout = view.findViewById(R.id.clLogos)
        val clDots: ConstraintLayout = view.findViewById(R.id.clDots)
        val clTemplates: ConstraintLayout = view.findViewById(R.id.clTemplates)
        dbHelper = QRCodeDatabaseHelper(requireContext())

        val noselected: ImageView = view.findViewById(R.id.ivNoSelected)
        val whatsapp: ImageView = view.findViewById(R.id.ivWhatsapp)
        val facebook: ImageView = view.findViewById(R.id.ivFacebook)
        val twitter: ImageView = view.findViewById(R.id.ivTwitter)
        val imagelogo: ImageView = view.findViewById(R.id.ivLogo)
        val snapchat: ImageView = view.findViewById(R.id.ivSnapchat)
        val youtube: ImageView = view.findViewById(R.id.ivYoutube)
        val linkedin: ImageView = view.findViewById(R.id.ivLinkedIn)
        val google: ImageView = view.findViewById(R.id.ivGoogle)
        val pinterest: ImageView = view.findViewById(R.id.ivPintrest)
        val tiktok: ImageView = view.findViewById(R.id.ivTiktok)
        val gmail: ImageView = view.findViewById(R.id.ivGmail)
        val insta: ImageView = view.findViewById(R.id.ivInstagram)
        val doneLogo: AppCompatButton = view.findViewById(R.id.btnDoneLogos)
        val clLogoLayout: ConstraintLayout = view.findViewById(R.id.logoLayout)


        val args = CreateEmailQRFragmentArgs.fromBundle(requireArguments())
        val email = args.emailAddress
        val subject = args.subject
        val message = args.message

        templetenew = view.findViewById(R.id.newtemplate)
        val framecolor0: ImageView = view.findViewById(R.id.noColor)
        val framecolor1: ImageView = view.findViewById(R.id.framecolor1)
        val framecolor2: ImageView = view.findViewById(R.id.framecolor2)
        val framecolor3: ImageView = view.findViewById(R.id.framecolor3)
        val framecolor4: ImageView = view.findViewById(R.id.framecolor4)
        val framecolor5: ImageView = view.findViewById(R.id.framecolor5)
        val framecolor6: ImageView = view.findViewById(R.id.framecolor6)
        val framecolor7: ImageView = view.findViewById(R.id.framecolor7)

        layoutAdNative = requireActivity().findViewById(R.id.layoutAdNative)



        framecolor0.setOnClickListener {
            selectedFrameColor = Color.parseColor("#1B1A1A")
            currentDrawableResource?.let { drawableResource ->
                // Get the drawable from resources
                val drawable = ContextCompat.getDrawable(requireContext(), drawableResource)

                // Check if drawable is not null
                drawable?.let {
                    // Use a ColorFilter to change the color
                    it.setColorFilter(Color.parseColor("#1B1A1A"), PorterDuff.Mode.SRC_IN)

                    // Apply the modified drawable to the view
                    templetenew.background = it
                }
            }
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }
        framecolor1.setOnClickListener {
            selectedFrameColor = Color.parseColor("#01FF57")
            currentDrawableResource?.let { drawableResource ->
                // Get the drawable from resources
                val drawable = ContextCompat.getDrawable(requireContext(), drawableResource)

                // Check if drawable is not null
                drawable?.let {
                    // Use a ColorFilter to change the color
                    it.setColorFilter(Color.parseColor("#01FF57"), PorterDuff.Mode.SRC_IN)

                    // Apply the modified drawable to the view
                    templetenew.background = it
                }
            }
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }
        framecolor2.setOnClickListener {
            selectedFrameColor = Color.parseColor("#01FF57")
            currentDrawableResource?.let { drawableResource ->
                // Get the drawable from resources
                val drawable = ContextCompat.getDrawable(requireContext(), drawableResource)

                // Check if drawable is not null
                drawable?.let {
                    // Use a ColorFilter to change the color
                    it.setColorFilter(Color.parseColor("#00FFE0"), PorterDuff.Mode.SRC_IN)

                    // Apply the modified drawable to the view
                    templetenew.background = it
                }
            }
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }
        framecolor3.setOnClickListener {
            selectedFrameColor = Color.parseColor("#01FF57")
            currentDrawableResource?.let { drawableResource ->
                // Get the drawable from resources
                val drawable = ContextCompat.getDrawable(requireContext(), drawableResource)

                // Check if drawable is not null
                drawable?.let {
                    // Use a ColorFilter to change the color
                    it.setColorFilter(Color.parseColor("#0093FF"), PorterDuff.Mode.SRC_IN)

                    // Apply the modified drawable to the view
                    templetenew.background = it
                }
            }
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }
        framecolor4.setOnClickListener {
            selectedFrameColor = Color.parseColor("#01FF57")
            currentDrawableResource?.let { drawableResource ->
                // Get the drawable from resources
                val drawable = ContextCompat.getDrawable(requireContext(), drawableResource)

                // Check if drawable is not null
                drawable?.let {
                    // Use a ColorFilter to change the color
                    it.setColorFilter(Color.parseColor("#AD00FF"), PorterDuff.Mode.SRC_IN)

                    // Apply the modified drawable to the view
                    templetenew.background = it
                }
            }
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }
        framecolor5.setOnClickListener {
            selectedFrameColor = Color.parseColor("#01FF57")
            currentDrawableResource?.let { drawableResource ->
                // Get the drawable from resources
                val drawable = ContextCompat.getDrawable(requireContext(), drawableResource)

                // Check if drawable is not null
                drawable?.let {
                    // Use a ColorFilter to change the color
                    it.setColorFilter(Color.parseColor("#FF2C78"), PorterDuff.Mode.SRC_IN)

                    // Apply the modified drawable to the view
                    templetenew.background = it
                }
            }
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }
        framecolor6.setOnClickListener {
            currentDrawableResource?.let { drawableResource ->
                // Get the drawable from resources
                val drawable = ContextCompat.getDrawable(requireContext(), drawableResource)

                // Check if drawable is not null
                drawable?.let {
                    // Use a ColorFilter to change the color
                    it.setColorFilter(Color.parseColor("#FFF500"), PorterDuff.Mode.SRC_IN)

                    // Apply the modified drawable to the view
                    templetenew.background = it
                }
            }
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }
        framecolor7.setOnClickListener {
            currentDrawableResource?.let { drawableResource ->
                // Get the drawable from resources
                val drawable = ContextCompat.getDrawable(requireContext(), drawableResource)

                // Check if drawable is not null
                drawable?.let {
                    // Use a ColorFilter to change the color
                    it.setColorFilter(Color.parseColor("#00507C"), PorterDuff.Mode.SRC_IN)

                    // Apply the modified drawable to the view
                    templetenew.background = it
                }
            }
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }


//        qrData = "MATMSG:To:$email;SUB:=${Uri.encode(subject)}&BODY:=${Uri.encode(message)}"
        qrData = "MATMSG:To:$email;SUB:${Uri.encode(subject)};BODY:${Uri.encode(message)};;"


        val clEyes: ConstraintLayout = view.findViewById(R.id.clEyes)
        val eyesLayout: ConstraintLayout = view.findViewById(R.id.eyesLayout)
        clEyes.setOnClickListener {
            clCustomization.visibility = View.GONE
            eyesLayout.visibility = View.VISIBLE
            save.visibility = View.GONE

        }
        val doneEyes: AppCompatButton = view.findViewById(R.id.btnDoneEyes)
        doneEyes.setOnClickListener {
            eyesLayout.visibility = View.GONE
            clCustomization.visibility = View.VISIBLE
            save.visibility = View.VISIBLE
        }
        view.findViewById<ImageView>(R.id.ivcross).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.DEFAULT
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }

        view.findViewById<ImageView>(R.id.ivsquare).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.DIAMOND
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }

        view.findViewById<ImageView>(R.id.ivsquare2).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.CIRCLE
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }

        view.findViewById<ImageView>(R.id.ivsquare3).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.ROUNDED_SQUARE
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }

        view.findViewById<ImageView>(R.id.ivsquare4).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.DEFAULT
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }

        view.findViewById<ImageView>(R.id.circle1).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.CIRCLE_DIAMOND
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }

        view.findViewById<ImageView>(R.id.circle2).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.CIRCLE_CIRCLE
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )

        }

        view.findViewById<ImageView>(R.id.circle3).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.CIRCLE_SQUARE
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }

        view.findViewById<ImageView>(R.id.roundedsquare1).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.ROUNDED1
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }

        view.findViewById<ImageView>(R.id.roundedsquare2).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.ROUNDED2
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }

        view.findViewById<ImageView>(R.id.roundedsquare3).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.ROUNDED3
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }
        view.findViewById<ImageView>(R.id.roundedsquare4).setOnClickListener {
            eyeShapeType = CreateEmailQRFragment.EyeShapeType.ROUNDED4
            generateAndDisplayQRCode(qrData)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
        }


        val doneTemplate: AppCompatButton = view.findViewById(R.id.btnDoneTemplates)
        val templateLayout: ConstraintLayout = view.findViewById(R.id.layoutTemplate)
        val template1: ImageView = view.findViewById(R.id.temp1)
        val template2: ImageView = view.findViewById(R.id.temp2)
        val template3: ImageView = view.findViewById(R.id.temp3)
        val template4: ImageView = view.findViewById(R.id.temp4)
        val template5: ImageView = view.findViewById(R.id.temp5)
        val template6: ImageView = view.findViewById(R.id.temp6)
        val qrtemplate: ImageView = view.findViewById(R.id.qrTemplate)
        val template0: ImageView = view.findViewById(R.id.temp0)



        clTemplates.setOnClickListener {
            clCustomization.visibility = View.GONE
            templateLayout.visibility = View.VISIBLE
            save.visibility = View.GONE
        }

        template0.setOnClickListener {
            templetenew.setBackgroundResource(0)
            qrCodeBitmap = generateQRCode(
                qrData,
                qrCodeImageView,
                currentColor,
                background,
                eyeShapeType
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            val layoutParams = templetenew.layoutParams
            layoutParams.height =
                dpToPx(250, requireContext()) // for example, setting height to 200dp
            layoutParams.width =
                dpToPx(250, requireContext()) // for example, setting height to 200dp
//
//            // Set different padding values for each side
            templetenew.layoutParams = layoutParams
            val paddingLeftInDp = 0 // example padding for left in dp
            val paddingTopInDp = 0 // example padding for top in dp
            val paddingRightInDp = 0 // example padding for right in dp
            val paddingBottomInDp = 0// example padding for bottom in dp
//
            val paddingLeftInPixels = dpToPx(paddingLeftInDp, requireContext())
            val paddingTopInPixels = dpToPx(paddingTopInDp, requireContext())
            val paddingRightInPixels = dpToPx(paddingRightInDp, requireContext())
            val paddingBottomInPixels = dpToPx(paddingBottomInDp, requireContext())
//
            templetenew.setPadding(
                paddingLeftInPixels,
                paddingTopInPixels,
                paddingRightInPixels,
                paddingBottomInPixels
            )
            templetenew.setPadding(
                paddingLeftInDp,
                paddingTopInDp,
                paddingRightInDp,
                paddingBottomInDp
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )

        }
        template1.setOnClickListener {
            currentDrawableResource = R.drawable.temp_1
//            currentColor = Color.parseColor("#1B1A1A")
            templetenew.setBackgroundResource(R.drawable.temp_1)
            qrCodeBitmap = generateQRCode(
                qrData,
                qrCodeImageView,
                currentColor,
                background,
                eyeShapeType
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            val layoutParams = templetenew.layoutParams
            layoutParams.height =
                dpToPx(250, requireContext()) // for example, setting height to 200dp
            layoutParams.width =
                dpToPx(250, requireContext()) // for example, setting height to 200dp
//
//            // Set different padding values for each side
            templetenew.layoutParams = layoutParams
            val paddingLeftInDp = 0 // example padding for left in dp
            val paddingTopInDp = 0 // example padding for top in dp
            val paddingRightInDp = 0 // example padding for right in dp
            val paddingBottomInDp = 0// example padding for bottom in dp
//
            val paddingLeftInPixels = dpToPx(paddingLeftInDp, requireContext())
            val paddingTopInPixels = dpToPx(paddingTopInDp, requireContext())
            val paddingRightInPixels = dpToPx(paddingRightInDp, requireContext())
            val paddingBottomInPixels = dpToPx(paddingBottomInDp, requireContext())
//
            templetenew.setPadding(
                paddingLeftInPixels,
                paddingTopInPixels,
                paddingRightInPixels,
                paddingBottomInPixels
            )
            templetenew.setPadding(
                paddingLeftInDp,
                paddingTopInDp,
                paddingRightInDp,
                paddingBottomInDp
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )

        }
        template2.setOnClickListener {
            currentDrawableResource = R.drawable.temp_2
//            currentColor = Color.parseColor("#1B1A1A")
            templetenew.setBackgroundResource(R.drawable.temp_2)
            qrCodeBitmap = generateQRCode(
                qrData,
                qrCodeImageView,
                currentColor,
                background,
                eyeShapeType
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            val layoutParams = templetenew.layoutParams
            layoutParams.height =
                dpToPx(250, requireContext()) // for example, setting height to 200dp
            layoutParams.width =
                dpToPx(250, requireContext()) // for example, setting height to 200dp
//
//            // Set different padding values for each side
            templetenew.layoutParams = layoutParams
            val paddingLeftInDp = 0 // example padding for left in dp
            val paddingTopInDp = 0 // example padding for top in dp
            val paddingRightInDp = 0 // example padding for right in dp
            val paddingBottomInDp = 0// example padding for bottom in dp
//
            val paddingLeftInPixels = dpToPx(paddingLeftInDp, requireContext())
            val paddingTopInPixels = dpToPx(paddingTopInDp, requireContext())
            val paddingRightInPixels = dpToPx(paddingRightInDp, requireContext())
            val paddingBottomInPixels = dpToPx(paddingBottomInDp, requireContext())
//
            templetenew.setPadding(
                paddingLeftInPixels,
                paddingTopInPixels,
                paddingRightInPixels,
                paddingBottomInPixels
            )
            templetenew.setPadding(
                paddingLeftInDp,
                paddingTopInDp,
                paddingRightInDp,
                paddingBottomInDp
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }
        template3.setOnClickListener {
            currentDrawableResource = R.drawable.temp_new3
//            currentColor = Color.parseColor("#1B1A1A")
            templetenew.setBackgroundResource(R.drawable.temp_new3)
            qrCodeBitmap = generateQRCode(
                qrData,
                qrCodeImageView,
                currentColor,
                background,
                eyeShapeType
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            val layoutParams = templetenew.layoutParams
            layoutParams.height =
                dpToPx(260, requireContext()) // for example, setting height to 200dp
            layoutParams.width =
                dpToPx(250, requireContext()) // for example, setting height to 200dp
//
//            // Set different padding values for each side
            templetenew.layoutParams = layoutParams
            val paddingLeftInDp = 0 // example padding for left in dp
            val paddingTopInDp = 30 // example padding for top in dp
            val paddingRightInDp = 0 // example padding for right in dp
            val paddingBottomInDp = 0// example padding for bottom in dp
//
            val paddingLeftInPixels = dpToPx(paddingLeftInDp, requireContext())
            val paddingTopInPixels = dpToPx(paddingTopInDp, requireContext())
            val paddingRightInPixels = dpToPx(paddingRightInDp, requireContext())
            val paddingBottomInPixels = dpToPx(paddingBottomInDp, requireContext())
//
            templetenew.setPadding(
                paddingLeftInPixels,
                paddingTopInPixels,
                paddingRightInPixels,
                paddingBottomInPixels
            )
            templetenew.setPadding(
                paddingLeftInDp,
                paddingTopInDp,
                paddingRightInDp,
                paddingBottomInDp
            )

            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
//
//            templetenew.layoutParams = layoutParams
//            templetenew.scaleType = ImageView.ScaleType.FIT_XY

        }
        template4.setOnClickListener {
            currentDrawableResource = R.drawable.temp_new4
//            currentColor = Color.parseColor("#1B1A1A")
            templetenew.setBackgroundResource(R.drawable.temp_new4)
            qrCodeBitmap = generateQRCode(
                qrData,
                qrCodeImageView,
                currentColor,
                background,
                eyeShapeType
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            val layoutParams = templetenew.layoutParams
            layoutParams.height =
                dpToPx(310, requireContext()) // for example, setting height to 200dp
            layoutParams.width =
                dpToPx(300, requireContext()) // for example, setting height to 200dp
//
//            // Set different padding values for each side
            templetenew.layoutParams = layoutParams
            val paddingLeftInDp = 0 // example padding for left in dp
            val paddingTopInDp = 0 // example padding for top in dp
            val paddingRightInDp = 0 // example padding for right in dp
            val paddingBottomInDp = 60// example padding for bottom in dp
//
            val paddingLeftInPixels = dpToPx(paddingLeftInDp, requireContext())
            val paddingTopInPixels = dpToPx(paddingTopInDp, requireContext())
            val paddingRightInPixels = dpToPx(paddingRightInDp, requireContext())
            val paddingBottomInPixels = dpToPx(paddingBottomInDp, requireContext())
//
            templetenew.setPadding(
                paddingLeftInPixels,
                paddingTopInPixels,
                paddingRightInPixels,
                paddingBottomInPixels
            )
            templetenew.setPadding(
                paddingLeftInDp,
                paddingTopInDp,
                paddingRightInDp,
                paddingBottomInDp
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )


        }
        template5.setOnClickListener {
            currentDrawableResource = R.drawable.tem_new05
//            currentColor = Color.parseColor("#1B1A1A")
            templetenew.setBackgroundResource(R.drawable.tem_new05)
            qrCodeBitmap = generateQRCode(
                qrData,
                qrCodeImageView,
                currentColor,
                background,
                eyeShapeType
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)

            )
            val layoutParams = templetenew.layoutParams
            layoutParams.height =
                dpToPx(280, requireContext()) // for example, setting height to 200dp
            layoutParams.width =
                dpToPx(260, requireContext()) // for example, setting height to 200dp
//
//            // Set different padding values for each side
            templetenew.layoutParams = layoutParams
            val paddingLeftInDp = 0 // example padding for left in dp
            val paddingTopInDp = 100 // example padding for top in dp
            val paddingRightInDp = 0 // example padding for right in dp
            val paddingBottomInDp = 0// example padding for bottom in dp
//
            val paddingLeftInPixels = dpToPx(paddingLeftInDp, requireContext())
            val paddingTopInPixels = dpToPx(paddingTopInDp, requireContext())
            val paddingRightInPixels = dpToPx(paddingRightInDp, requireContext())
            val paddingBottomInPixels = dpToPx(paddingBottomInDp, requireContext())
//
            templetenew.setPadding(
                paddingLeftInPixels,
                paddingTopInPixels,
                paddingRightInPixels,
                paddingBottomInPixels
            )
            templetenew.setPadding(
                paddingLeftInDp,
                paddingTopInDp,
                paddingRightInDp,
                paddingBottomInDp
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )

        }
        template6.setOnClickListener {
            currentDrawableResource = R.drawable.new_new
//            currentColor = Color.parseColor("#1B1A1A")
            templetenew.setBackgroundResource(R.drawable.new_new)
            qrCodeBitmap = generateQRCode(
                qrData,
                qrCodeImageView,
                currentColor,
                background,
                eyeShapeType
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            val layoutParams = templetenew.layoutParams
            layoutParams.height =
                dpToPx(280, requireContext()) // for example, setting height to 200dp
            layoutParams.width =
                dpToPx(280, requireContext()) // for example, setting height to 200dp
//
//            // Set different padding values for each side
            templetenew.layoutParams = layoutParams
            val paddingLeftInDp = 0 // example padding for left in dp
            val paddingTopInDp = 0 // example padding for top in dp
            val paddingRightInDp = 10 // example padding for right in dp
            val paddingBottomInDp = 70// example padding for bottom in dp
//
            val paddingLeftInPixels = dpToPx(paddingLeftInDp, requireContext())
            val paddingTopInPixels = dpToPx(paddingTopInDp, requireContext())
            val paddingRightInPixels = dpToPx(paddingRightInDp, requireContext())
            val paddingBottomInPixels = dpToPx(paddingBottomInDp, requireContext())
//
            templetenew.setPadding(
                paddingLeftInPixels,
                paddingTopInPixels,
                paddingRightInPixels,
                paddingBottomInPixels
            )
            templetenew.setPadding(
                paddingLeftInDp,
                paddingTopInDp,
                paddingRightInDp,
                paddingBottomInDp
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )


        }

        doneTemplate.setOnClickListener {
            clCustomization.visibility = View.VISIBLE
            templateLayout.visibility = View.GONE
            save.visibility = View.VISIBLE
        }


        clLogo.setOnClickListener {
            clLogoLayout.visibility = View.VISIBLE
            clCustomization.visibility = View.GONE
            save.visibility = View.GONE
        }



        doneLogo.setOnClickListener {
            clLogoLayout.visibility = View.GONE
            clCustomization.visibility = View.VISIBLE
            save.visibility = View.VISIBLE
        }

        noselected.setOnClickListener {
            imagelogo.setImageResource(0)
            imagelogo.setBackgroundColor(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        whatsapp.setOnClickListener {
            imagelogo.setImageResource(R.drawable.ic_whatsapp)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )

        }

        twitter.setOnClickListener {
            imagelogo.setImageResource(R.drawable.x_2)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        facebook.setOnClickListener {
            imagelogo.setImageResource(R.drawable.fb_2)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        noselected.setOnClickListener {
            imagelogo.setImageResource(0)
            imagelogo.setBackgroundColor(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        youtube.setOnClickListener {
            imagelogo.setImageResource(R.drawable.ic_youtube)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        insta.setOnClickListener {
            imagelogo.setImageResource(R.drawable.ic_instagram)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        tiktok.setOnClickListener {
            imagelogo.setImageResource(R.drawable.ic_tiktok)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        linkedin.setOnClickListener {
            imagelogo.setImageResource(R.drawable.linkedin2)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        snapchat.setOnClickListener {
            imagelogo.setImageResource(R.drawable.ic_snapchat)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        google.setOnClickListener {
            imagelogo.setImageResource(R.drawable.ic_google)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        pinterest.setOnClickListener {
            imagelogo.setImageResource(R.drawable.pintrest2)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        gmail.setOnClickListener {
            imagelogo.setImageResource(R.drawable.ic_gmail)
            imagelogo.setBackgroundResource(0)
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
            )
            qrCodeBitmap = createBitmapFromView(
                requireContext(),
                requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
            )
        }

        if (isClColorsClicked) {
            qrCodeBitmap =
                generateQRCode(qrData, qrCodeImageView, currentColor, background, eyeShapeType)
        } else {
            qrCodeBitmap = generateQRCodeBitmap(qrData)
        }

        qrCodeImageView.setImageBitmap(qrCodeBitmap) // Set the QR code initially


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d(
                    "CreateEmailQRFragmentFragment",
                    "onTabSelected: tab position ${tab?.position}"
                )

                when (tab?.position) {
                    0 -> { // Assuming first tab is Foreground
                        backgroundLayout.visibility = View.GONE
                        forgoundLayout.visibility = View.VISIBLE
                        // Log the visibility after changing
                        Log.d(
                            "CreateEmailQRFragmentFragment",
                            "Foreground selected - backgroundLayout: ${backgroundLayout.visibility}, forgoundLayout: ${forgoundLayout.visibility}"
                        )
                    }

                    1 -> { // Assuming second tab is Background
                        backgroundLayout.visibility = View.VISIBLE
                        forgoundLayout.visibility = View.GONE
                        // Log the visibility after changing
                        Log.d(
                            "CreateEmailQRFragmentFragment",
                            "Background selected - backgroundLayout: ${backgroundLayout.visibility}, forgoundLayout: ${forgoundLayout.visibility}"
                        )
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        save = view.findViewById(R.id.btnSave)

        navController = findNavController()

        val colorItems = listOf(
            ColorItem(R.drawable.pink, Color.parseColor("#FF2C78")),
            ColorItem(
                R.drawable.purple,
                Color.parseColor("#9C28B1")
            ), // Example of direct color value using hex color
            ColorItem(
                R.drawable.darkblue,
                Color.parseColor("#673BB7")
            ), // Example of direct color value using hex color
            ColorItem(
                R.drawable.navyblue,
                Color.parseColor("#3F51B5")
            ), // Example of direct color value using hex color
            ColorItem(
                R.drawable.skyblue,
                Color.parseColor("#2196F3")
            ), // Example of direct color value using hex color
            ColorItem(R.drawable.white, Color.parseColor("#03A9F5")),
            ColorItem(
                R.drawable.red,
                Color.parseColor("#F5033D")
            ) // Example of direct color value using hex color
        )


        val imageItems2 = listOf(
            ImageItem(R.drawable.bcc),
            ImageItem(R.drawable.bcc1),
            ImageItem(R.drawable.bcc2),
            ImageItem(R.drawable.bcc3),
            ImageItem(R.drawable.bcc4),
            ImageItem(R.drawable.bcc5),
            ImageItem(R.drawable.bcc6)
        )

        val btnColors = view.findViewById<ImageView>(R.id.colorPallete)
        val btnGradient = view.findViewById<ImageView>(R.id.gradientPallete)
        val done = view.findViewById<AppCompatButton>(R.id.btnDone)

        done.setOnClickListener {
            clTablayout.visibility = View.GONE
            clCustomization.visibility = View.VISIBLE
            save.visibility = View.VISIBLE

        }
        btnColors.setOnClickListener {
            showColorPickerDialog()
        }

        btnGradient.setOnClickListener {
            showGradientPickerDialog()
        }
        save.setOnClickListener {
            saveImageToDownloads(qrCodeBitmap)
        }


//        val imageItems = listOf(
//            ImageItem(R.drawable.image1),
//            ImageItem(R.drawable.image2),
//            ImageItem(R.drawable.image3),
//            ImageItem(R.drawable.image4),
//            ImageItem(R.drawable.image5),
//            ImageItem(R.drawable.image6),
//            ImageItem(R.drawable.image7)
//            // Add more ImageItem instances as needed
//        )

//        val imageItems1 = listOf(
//            ImageItem(R.drawable.image01),
//            ImageItem(R.drawable.image02),
//            ImageItem(R.drawable.image03),
//            ImageItem(R.drawable.image04),
//            ImageItem(R.drawable.image05),
//            ImageItem(R.drawable.image06),
//            ImageItem(R.drawable.image07)
//            // Add more ImageItem instances as needed
//        )

//        val imageItems2 = listOf(
//            ImageItem(R.drawable.image10),
//            ImageItem(R.drawable.image20),
//            ImageItem(R.drawable.image30),
//            ImageItem(R.drawable.image40),
//            ImageItem(R.drawable.image50),
//            ImageItem(R.drawable.image60),
//            ImageItem(R.drawable.image70)
//            // Add more ImageItem instances as needed
//        )

//        imageRecyclerAdapter = ImageRecyclerAdapter(
//            requireContext(),
//            imageItems,
//            object : ImageRecyclerAdapter.OnImageItemClickListener {
//                override fun onImageItemClick(imageResId: Int) {
//                    applyImageBackgroundToQRCode(imageResId)
//                }
//            })


        backgroundRecyclerAdapter = BackgroundRecyclerAdapter(
            requireContext(),
            imageItems2,
            object : BackgroundRecyclerAdapter.OnImageItemClickListener {
                override fun onImageItemClick(imageResId: Int) {
                    applyImageBackgroundToQRCode(imageResId)
                }
            })

//        val gradientItems = listOf(
//            GradientItem(
//                R.drawable.ic_gradient1,
//                intArrayOf(Color.parseColor("#FF00FFD1"), Color.parseColor("#FF033D2D"))
//            ),
//            GradientItem(
//                R.drawable.ic_gradient2,
//                intArrayOf(Color.parseColor("#FFFF0000"), Color.parseColor("#FF01B43E"))
//            ),
//            GradientItem(
//                R.drawable.ic_gradient3,
//                intArrayOf(Color.parseColor("#FFC107"), Color.parseColor("#673AB7"))
//            ),
//            GradientItem(
//                R.drawable.ic_gradient4,
//                intArrayOf(Color.parseColor("#FFFF006B"), Color.parseColor("#FF020202"))
//            ),
//            GradientItem(
//                R.drawable.ic_gradient5,
//                intArrayOf(Color.parseColor("#FF131313"), Color.parseColor("#FFFF6B00"))
//            ),
//            GradientItem(
//                R.drawable.ic_gradient6,
//                intArrayOf(Color.parseColor("#FFFF003D"), Color.parseColor("#FF0057FF"))
//            ),
//            GradientItem(
//                R.drawable.ic_gradient7,
//                intArrayOf(Color.parseColor("#FFFF0099"), Color.parseColor("#FFFF003D"))
//            )
//            // Add more GradientItem instances as needed
//        )

        colorRecyclerAdapter = ColorRecyclerAdapter(requireContext(), colorItems, this)

        //for Forground
        //for single color
        val colorRecyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        colorRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        colorRecyclerView.adapter = colorRecyclerAdapter
        //for gradient
        //for single color
//        val gradientRecyclerView: RecyclerView = view.findViewById(R.id.recyclerView01)
//        gradientRecyclerView.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        gradientRecyclerView.adapter = gradientRecyclerAdapter
        //for Background
        //for single color
        val backgroundRecyclerView: RecyclerView = view.findViewById(R.id.recyclerView1)
        backgroundRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        backgroundRecyclerView.adapter = backgroundRecyclerAdapter
        //for gradient


        // Set layout managers and adapters for RecyclerViews
        // Set adapters to RecyclerViews
//        val imageRecyclerView: RecyclerView = view.findViewById(R.id.recyclerViewImage)
//        imageRecyclerView.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        imageRecyclerView.adapter = imageRecyclerAdapter


        clColors.setOnClickListener {
            isClColorsClicked = true
            generateQRCode(qrData, qrCodeImageView, currentColor, background, eyeShapeType)
            clTablayout.visibility = View.VISIBLE
            clCustomization.visibility = View.GONE
            save.visibility = View.GONE
        }


        clDots.setOnClickListener {
            Toast.makeText(requireContext(), "Under Development", Toast.LENGTH_SHORT).show()
        }


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showDiscardChangesDialog {
                        val action = CreateEmailQRFragmentDirections.actionNavCreateToNavBack()
                        navController.navigate(action)
                    }
                }
            }
        )


        val btnSave: AppCompatButton = view.findViewById(R.id.btnSave)
        btnSave.setOnClickListener {
            saveImageToDownloads(qrCodeBitmap)
        }
    }

    private fun generateAndDisplayQRCode(data: String) {
        isClColorsClicked = true
        val qrCodeBitmap =
            generateQRCode(qrData, qrCodeImageView, currentColor, background, eyeShapeType)
        qrCodeImageView.setImageBitmap(qrCodeBitmap)
    }

    private fun generateQRCodeBitmap(data: String): Bitmap {
        // Use ZXing library or any other QR code generation library to generate QR code bitmap
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bmp
    }

    private fun applyImageBackgroundToQRCode(imageResId: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, imageResId)
        val scaledBitmap =
            Bitmap.createScaledBitmap(bitmap, qrCodeBitmap.width, qrCodeBitmap.height, true)
        requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR).background =
            BitmapDrawable(resources, scaledBitmap)
        qrCodeBitmap = createBitmapFromView(
            requireContext(),
            requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
        )
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


    fun showGradientPickerDialog() {
        val builder1 = ColorPickerDialog.Builder(requireContext())
            .setTitle("Select Color 1")
            .setPreferenceName("MyGradientPickerDialog")
            .setPositiveButton(getString(R.string.confirm),
                object : ColorEnvelopeListener {
                    @RequiresApi(Build.VERSION_CODES.Q)
                    override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                        val color1 = envelope.color

                        val builder2 = ColorPickerDialog.Builder(requireContext())
                            .setTitle("Select Color 2")
                            .setPreferenceName("MyGradientPickerDialog")
                            .setPositiveButton(getString(R.string.confirm),
                                object : ColorEnvelopeListener {
                                    @RequiresApi(Build.VERSION_CODES.Q)
                                    override fun onColorSelected(
                                        envelope2: ColorEnvelope,
                                        fromUser: Boolean
                                    ) {
                                        val color2 = envelope2.color
                                        applyGradientToQRCode(color1, color2)
                                    }
                                })
                            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                                dialogInterface.dismiss()
                            }
                            .attachAlphaSlideBar(true)
                            .attachBrightnessSlideBar(true)
                            .setBottomSpace(12)

                        builder2.show()
                    }
                })
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)

        builder1.show()
    }

    private fun showColorPickerDialog() {
        val builder = ColorPickerDialog.Builder(requireContext())
            .setTitle("Pick a Color")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton(getString(R.string.confirm),
                ColorEnvelopeListener { envelope, fromUser ->
                    currentColor = envelope.color

                    generateQRCode(qrData, qrCodeImageView, currentColor, background, eyeShapeType)
                })
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(true) // Optional: Attach alpha slide bar
            .attachBrightnessSlideBar(true) // Optional: Attach brightness slide bar
            .setBottomSpace(12) // Optional: Set bottom space between sliders and buttons

        builder.show()
    }

    private fun renderQRImage(
        code: com.google.zxing.qrcode.encoder.QRCode,
        width: Int,
        height: Int,
        quietZone: Int,
        eyeShapeType: EyeShapeType,
        color1: Int, // Primary color (for single color or gradient start)
        color2: Int? = null // Optional secondary color (for gradient end)
    ): Bitmap {
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)

        // Create a Paint object to use for drawing
        val paint = Paint().apply {
            isAntiAlias = true
        }

        canvas.drawColor(Color.TRANSPARENT)

        val input: ByteMatrix = code.matrix ?: throw IllegalStateException()
        val inputWidth = input.width
        val inputHeight = input.height
        val qrWidth = inputWidth + (quietZone * 2)
        val qrHeight = inputHeight + (quietZone * 2)
        val outputWidth = maxOf(width, qrWidth)
        val outputHeight = maxOf(height, qrHeight)

        val multiple = minOf(outputWidth / qrWidth, outputHeight / qrHeight)
        val leftPadding = (outputWidth - (inputWidth * multiple)) / 2
        val topPadding = (outputHeight - (inputHeight * multiple)) / 2
        val FINDER_PATTERN_SIZE = 7

        // Draw the QR code matrix with single color or gradient
        for (inputY in 0 until inputHeight) {
            for (inputX in 0 until inputWidth) {
                if (input[inputX, inputY].toInt() == 1) {
                    // Determine the color based on whether we are using a gradient
                    val color = if (color2 != null) {
                        val fraction = (inputX + inputY) / (inputWidth + inputHeight).toFloat()
                        interpolateColor(color1, color2, fraction)
                    } else {
                        color1
                    }

                    if (eyeShapeType in listOf(
                            EyeShapeType.CIRCLE_CIRCLE,
                            EyeShapeType.CIRCLE_DIAMOND,
                            EyeShapeType.CIRCLE_SQUARE,
                            EyeShapeType.ROUNDED1,
                            EyeShapeType.ROUNDED2,
                            EyeShapeType.ROUNDED3,
                            EyeShapeType.ROUNDED4
                        )
                    ) {
                        if (!isFinderPattern(inputX, inputY, inputWidth, inputHeight)) {
                            paint.color = color
                            canvas.drawRect(
                                leftPadding + inputX * multiple.toFloat(),
                                topPadding + inputY * multiple.toFloat(),
                                leftPadding + (inputX + 1) * multiple.toFloat(),
                                topPadding + (inputY + 1) * multiple.toFloat(),
                                paint
                            )
                        }
                    } else {
                        paint.color = color
                        canvas.drawRect(
                            leftPadding + inputX * multiple.toFloat(),
                            topPadding + inputY * multiple.toFloat(),
                            leftPadding + (inputX + 1) * multiple.toFloat(),
                            topPadding + (inputY + 1) * multiple.toFloat(),
                            paint
                        )
                    }
                }
            }
        }

        // Draw custom shapes for the finder patterns
        val circleDiameter = multiple * FINDER_PATTERN_SIZE
        drawFinderPatternCircleStyle(
            canvas,
            paint,
            leftPadding,
            topPadding,
            circleDiameter,
            eyeShapeType,
            color1 // Primary color for eyes
        )
        drawFinderPatternCircleStyle(
            canvas,
            paint,
            leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple,
            topPadding,
            circleDiameter,
            eyeShapeType,
            color1 // Primary color for eyes
        )
        drawFinderPatternCircleStyle(
            canvas,
            paint,
            leftPadding,
            topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple,
            circleDiameter,
            eyeShapeType,
            color1 // Primary color for eyes
        )

        return image
    }

    fun generateQRCode(
        data: String,
        imageView: ImageView,
        color: Int,
        background: Int,
        eyeShapeType: EyeShapeType
    ): Bitmap {
        isClColorsClicked = true
        Log.e("value", "" + qrCodeBitmap)
        val writer = QRCodeWriter()
        return try {
            val encodingHints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                this[EncodeHintType.CHARACTER_SET] = "UTF-8"
            }
            val qrCode = Encoder.encode(data, ErrorCorrectionLevel.H, encodingHints)
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)

            val width = bitMatrix.width
            val height = bitMatrix.height

            // Create a bitmap with ARGB_8888 config for transparency
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            // Fill the entire bitmap with the background color
            bmp.eraseColor(background)

            // Apply QR code matrix to bitmap pixels for foreground color
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix[x, y]) {
                        // Set the foreground color only where the QR code modules are "true"
                        bmp.setPixel(x, y, color)
                    }
                }
            }

            // Render QR code with custom eyes
            val renderedBitmap = renderQRImage(qrCode, width, height, 4, eyeShapeType, color)

            // Set the generated bitmap to the ImageView
            imageView.setImageBitmap(renderedBitmap)

            // Return the bitmap for any further use if needed
            renderedBitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            // Handle WriterException (e.g., show error message)
            Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun generateQRCodeWithGradient(
        data: String,
        imageView: ImageView,
        color1: Int,
        color2: Int,
        eyeShapeType: CreateEmailQRFragment.EyeShapeType // Add parameter for eye shapes
    ): Bitmap {
        val writer = QRCodeWriter()
        return try {
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            // Create a bitmap with transparent background
            val canvas = Canvas(bmp)
            val paint = Paint().apply {
                isAntiAlias = true
                color = Color.TRANSPARENT // Use transparent color initially
            }
            canvas.drawColor(Color.TRANSPARENT) // Ensure transparent background

            // Apply QR code matrix to bitmap pixels
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix[x, y]) {
                        bmp.setPixel(x, y, paint.color)
                    }
                }
            }

            // Apply gradient to the QR code bitmap
            val gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val gradientCanvas = Canvas(gradientBitmap)
            val shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                color1, color2, Shader.TileMode.CLAMP
            )
            val gradientPaint = Paint().apply {
                isAntiAlias = true
                setShader(shader)
            }
            gradientCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)

            // Combine the gradient with the QR code bitmap
            val finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val finalCanvas = Canvas(finalBitmap)
            finalCanvas.drawBitmap(gradientBitmap, 0f, 0f, null)
            finalCanvas.drawBitmap(bmp, 0f, 0f, null)

            // Render QR code with custom eyes
            val qrCode = Encoder.encode(data, ErrorCorrectionLevel.H)
            val renderedBitmap = renderQRImage(
                qrCode,
                width,
                height,
                4, // Quiet zone
                eyeShapeType,
                color1, // Primary color for QR code foreground
                color2 // Secondary color (optional, used in gradient)
            )

            // Set the generated bitmap to the ImageView
            imageView.setImageBitmap(renderedBitmap)

            // Return the generated bitmap
            renderedBitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun applyGradientToQRCode(color1: Int, color2: Int) {
        val args = CreateEmailQRFragmentArgs.fromBundle(requireArguments())
        val email = args.emailAddress
        val subject = args.subject
        val message = args.message
        val qrData = "MATMSG:To:$email;SUB:${Uri.encode(subject)};BODY:${Uri.encode(message)};;"
        qrCodeBitmap =
            generateQRCodeWithGradient(qrData, qrCodeImageView, color1, color2, eyeShapeType)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveImageToDownloads(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToDownloadsQAndAbove(bitmap)
        } else {
            saveImageToDownloadsLegacy(bitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveImageToDownloadsQAndAbove(bitmap: Bitmap) {
        val filePath = saveBitmapToFile(bitmap, qrData)
        val action = CreateEmailQRFragmentDirections.actionEmailToFinalImage(filePath, qrData)
        findNavController().navigate(action)
    }

    private fun saveImageToDownloadsLegacy(bitmap: Bitmap) {
        val filePath = saveBitmapToFile(bitmap, qrData)
        val action = CreateEmailQRFragmentDirections.actionEmailToFinalImage(filePath, qrData)
        findNavController().navigate(action)
    }


//    private fun saveBitmapToFile(bitmap: Bitmap, qrCodeText: String): String {
//        // Generate a unique filename based on the QR code text hash
//        val uniqueFilename = "qr_code_image_${qrCodeText.hashCode()}.png"
//        val file = File(context?.cacheDir, uniqueFilename)
//
//        // Save the bitmap to file
//        FileOutputStream(file).use { out ->
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
//        }
//
//        // Call the function to insert the image path into the database
//        insertImagePathToDatabase(file.absolutePath, qrCodeText) // Pass the generated image path to the database
//
//        // Return the file path
//        return file.absolutePath // Return the path as a String
//    }
//
//    private fun insertImagePathToDatabase(imagePath: String, qrCodeText: String) {
//        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//        val currentTime = SimpleDateFormat("H:mm a", Locale.getDefault()).format(Date())
//        val drawableRes = R.drawable.ic_email
//
//        // Check if the QR code already exists in the database
//        val qrCodeExists = dbHelper.getAllQRCodes().any { it.qrCode == qrCodeText }
//
//        val success = if (qrCodeExists) {
//            // Update existing QR code with the new image path
//            dbHelper.updateQRCodeImagePath(qrCodeText, imagePath)
//        } else {
//            // Insert new QR code entry
//            dbHelper.insertQRCode(qrCodeText, currentDate, currentTime, drawableRes, imagePath)
//        }
//
//        if (success) {
//            Toast.makeText(requireContext(), "Data inserted/updated successfully", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(requireContext(), "Failed to insert/update data", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun saveBitmapToFile(bitmap: Bitmap, qrCodeText: String): String {
        // Generate a unique filename based on the QR code text hash and current time
        val timestamp = System.currentTimeMillis() // Get current time in milliseconds
        val uniqueFilename = "qr_code_image_${qrCodeText.hashCode()}_$timestamp.png"
        val file = File(requireContext().cacheDir, uniqueFilename)

        // Save the bitmap to file
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // Insert or update the image path in the database
        insertImagePathToDatabase(
            file.absolutePath,
            qrCodeText
        ) // Pass the generated image path to the database

        // Return the file path
        return file.absolutePath // Return the path as a String
    }


    private fun insertImagePathToDatabase(imagePath: String, qrCodeText: String) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val drawableRes = R.drawable.ic_email

        // Use dbHelper's insertQRCode method, which updates if the QR code exists
        val success =
            dbHelper.insertQRCode(qrCodeText, currentDate, currentTime, drawableRes, imagePath,"created")

        // Show a message indicating the result
        val message = if (success) {
            "Data inserted/updated successfully"
        } else {
            "Failed to insert/update data"
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    private fun showDiscardChangesDialog(onConfirm: () -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.dialog_title_discard_changes))
        builder.setMessage(getString(R.string.dialog_message_discard_changes))
        builder.setPositiveButton(R.string.dialog_positive_button) { dialog, _ ->
            dialog.dismiss()
            onConfirm()
        }
        builder.setNegativeButton(R.string.dialog_negative_button) { dialog, _ ->
            dialog.dismiss() // Stay on the screen
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        val isAdEnabled = requireActivity()
            .getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)
            .getBoolean("native_result", true)

        Log.e("AdStatus", "isAdEnabled: " + isAdEnabled)

        if (NetworkCheck.isNetworkAvailable(requireContext()) && isAdEnabled) {
            AdsProvider.nativeResult.config(
                requireActivity().getSharedPreferences(
                    "RemoteConfig",
                    AppCompatActivity.MODE_PRIVATE
                ).getBoolean(
                    native_result, true
                )
            )
            AdsProvider.nativeResult.loadAds(MyApplication.getApplication())
            showNativeAd(
                AdsProvider.nativeResult,
                requireActivity().findViewById(R.id.layoutAdNative),
                R.layout.layout_home_native_ad
            )
        } else {
            layoutAdNative.visibility = View.GONE
        }


        val topText: TextView = requireActivity().findViewById(R.id.mainText)
        topText.visibility = View.VISIBLE
        topText.text = getString(R.string.email)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        back?.visibility = View.VISIBLE

        back?.setOnClickListener {
            showDiscardChangesDialog {
                val action = CreateEmailQRFragmentDirections.actionNavCreateToNavBack()
                navController.navigate(action)
            }
        }

        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        setting?.visibility = View.INVISIBLE

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        ivClose?.setImageResource(R.drawable.ic_premium)
        ivClose?.visibility = View.INVISIBLE
    }

    override fun onItemClick(color: Int) {
        currentColor = color
        val args = CreateEmailQRFragmentArgs.fromBundle(requireArguments())
        val email = args.emailAddress
        val subject = args.subject
        val message = args.message
        val qrData = "MATMSG:To:$email;SUB:${Uri.encode(subject)};BODY:${Uri.encode(message)};;"
        qrCodeBitmap =
            generateQRCode(qrData, qrCodeImageView, currentColor, background, eyeShapeType)
        qrCodeBitmap = createBitmapFromView(
            requireContext(),
            requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
        )
    }

//    @RequiresApi(Build.VERSION_CODES.Q)
//    override fun onGradientItemClick(colors: IntArray) {
//        // Handle gradient item click
//        val color1 = colors[0]
//        val color2 = colors[1]
//        val args = CreateEmailQRFragmentArgs.fromBundle(requireArguments())
//        val email = args.emailAddress
//        val subject = args.subject
//        val message = args.message
//        val qrData = "MATMSG:To:$email;SUB:${Uri.encode(subject)};BODY:${Uri.encode(message)};;"
//        qrCodeBitmap = createBitmapFromView(
//            requireContext(),
//            requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
//        )
//        qrCodeBitmap =
//            generateQRCodeWithGradient(qrData, qrCodeImageView, color1, color2, eyeShapeType)
//    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onGradientColorItemClick(colors: IntArray) {
        val color1 = colors[0]
        val color2 = colors[1]
        val args = CreateEmailQRFragmentArgs.fromBundle(requireArguments())
        val email = args.emailAddress
        val subject = args.subject
        val message = args.message
        val qrData = "MATMSG:To:$email;SUB:${Uri.encode(subject)};BODY:${Uri.encode(message)};;"
        qrCodeBitmap = createBitmapFromView(
            requireContext(),
            requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
        )
        qrCodeBitmap =
            generateQRCodeWithGradient(qrData, qrCodeImageView, color1, color2, eyeShapeType)
    }

    fun dpToPx(dp: Int, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    // Function to interpolate between two colors
    private fun interpolateColor(color1: Int, color2: Int, fraction: Float): Int {
        val startRed = Color.red(color1)
        val startGreen = Color.green(color1)
        val startBlue = Color.blue(color1)
        val startAlpha = Color.alpha(color1)

        val endRed = Color.red(color2)
        val endGreen = Color.green(color2)
        val endBlue = Color.blue(color2)
        val endAlpha = Color.alpha(color2)

        val red = (startRed + fraction * (endRed - startRed)).toInt()
        val green = (startGreen + fraction * (endGreen - startGreen)).toInt()
        val blue = (startBlue + fraction * (endBlue - startBlue)).toInt()
        val alpha = (startAlpha + fraction * (endAlpha - startAlpha)).toInt()

        return Color.argb(alpha, red, green, blue)
    }


    private fun isFinderPattern(x: Int, y: Int, width: Int, height: Int): Boolean {
        val FINDER_PATTERN_SIZE = 7
        return (x in 0 until FINDER_PATTERN_SIZE && y in 0 until FINDER_PATTERN_SIZE) ||
                (x in (width - FINDER_PATTERN_SIZE) until width && y in 0 until FINDER_PATTERN_SIZE) ||
                (x in 0 until FINDER_PATTERN_SIZE && y in (height - FINDER_PATTERN_SIZE) until height)
    }

    private fun drawFinderPatternCircleStyle(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        y: Int,
        circleDiameter: Int,
        eyeShapeType: EyeShapeType,
        color: Int
    ) {
        val smallerShapeSize =
            circleDiameter * 3 / 5 // Adjust size relative to circleDiameter as needed
        val smallerShapeOffset =
            circleDiameter / 5 // Adjust offset relative to circleDiameter as needed

        when (eyeShapeType) {
            EyeShapeType.CIRCLE -> {
                // Draw a smaller circle inside the square
                paint.color = color
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    smallerShapeSize / 2f,
                    paint
                )
            }

            EyeShapeType.SQUARE -> {
                // Draw a smaller square inside the square
                paint.color = color
                canvas.drawRect(
                    (x + smallerShapeOffset).toFloat(),
                    (y + smallerShapeOffset).toFloat(),
                    (x + smallerShapeOffset + smallerShapeSize).toFloat(),
                    (y + smallerShapeOffset + smallerShapeSize).toFloat(),
                    paint
                )
            }

            EyeShapeType.DIAMOND -> {
                // Draw a diamond shape inside the square
                paint.color = color // Set inside color to blue
                val diamondSize = smallerShapeSize // Size of the diamond
                val halfDiamondSize = diamondSize / 3

                val path = Path()
                path.moveTo(
                    (x + circleDiameter / 2).toFloat(),
                    (y + halfDiamondSize).toFloat()
                ) // Top point
                path.lineTo(
                    (x + circleDiameter - halfDiamondSize).toFloat(),
                    (y + circleDiameter / 2).toFloat()
                ) // Right point
                path.lineTo(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter - halfDiamondSize).toFloat()
                ) // Bottom point
                path.lineTo(
                    x.toFloat() + halfDiamondSize,
                    (y + circleDiameter / 2).toFloat()
                ) // Left point
                path.close()

                canvas.drawPath(path, paint)
            }

            EyeShapeType.ROUNDED_SQUARE -> {
                // Draw a rounded square inside the square
                paint.color = color
                val cornerRadius = smallerShapeSize / 7f
                val rectF = RectF(
                    (x + smallerShapeOffset).toFloat(),
                    (y + smallerShapeOffset).toFloat(),
                    (x + smallerShapeOffset + smallerShapeSize).toFloat(),
                    (y + smallerShapeOffset + smallerShapeSize).toFloat()
                )
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
            }

            EyeShapeType.CIRCLE_DIAMOND -> {

                // Draw the outer circle as a border
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f // Adjust the border width if needed
                val circleRadius = circleDiameter / 2f
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    circleRadius,
                    paint
                )

                // Draw the inner filled diamond
                paint.style = Paint.Style.FILL
                paint.color = color
                val diamondSize = smallerShapeSize
                val halfDiamondSize = diamondSize / 2

                val diamondPath = Path().apply {
                    moveTo(
                        (x + circleDiameter / 2).toFloat(),
                        (y + (circleDiameter / 2 - halfDiamondSize)).toFloat()
                    ) // Top point
                    lineTo(
                        (x + (circleDiameter / 2 + halfDiamondSize)).toFloat(),
                        (y + circleDiameter / 2).toFloat()
                    ) // Right point
                    lineTo(
                        (x + circleDiameter / 2).toFloat(),
                        (y + (circleDiameter / 2 + halfDiamondSize)).toFloat()
                    ) // Bottom point
                    lineTo(
                        (x + (circleDiameter / 2 - halfDiamondSize)).toFloat(),
                        (y + circleDiameter / 2).toFloat()
                    ) // Left point
                    close()
                }

                canvas.drawPath(diamondPath, paint)

            }

            EyeShapeType.CIRCLE_CIRCLE -> {
                // Draw the outer circle as a border
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f // Adjust the border width if needed
                val circleRadius = circleDiameter / 2f
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    circleRadius,
                    paint
                )

                // Draw the inner filled circle
                paint.style = Paint.Style.FILL
                paint.color = color
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    smallerShapeSize / 2f,
                    paint
                )
            }

            EyeShapeType.CIRCLE_SQUARE -> {

                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f // Adjust the border width if needed
                val circleRadius = circleDiameter / 2f
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    circleRadius,
                    paint
                )

                // Draw the rounded square inside the inner circle
                paint.style = Paint.Style.FILL
                paint.color = color
                val squareSize = smallerShapeSize * 2 / 3 // Adjust size if needed
                val cornerRadius = squareSize / 5f // Rounded corner radius
                val rectF = RectF(
                    (x + circleDiameter / 2 - squareSize / 2).toFloat(),
                    (y + circleDiameter / 2 - squareSize / 2).toFloat(),
                    (x + circleDiameter / 2 + squareSize / 2).toFloat(),
                    (y + circleDiameter / 2 + squareSize / 2).toFloat()
                )
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
            }


            EyeShapeType.ROUNDED1 -> {
                // Draw the rounded square with stroke
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f // Adjust stroke width as needed

                val outerSquareSize = smallerShapeSize * 1.6f // Increase the size factor as needed
                val outerCornerRadius = outerSquareSize / 5f // Adjust corner radius if needed

                val rectF = RectF(
                    (x + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (x + circleDiameter / 2 + outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 + outerSquareSize / 2).toFloat()
                )
                canvas.drawRoundRect(rectF, outerCornerRadius, outerCornerRadius, paint)

                // Draw the filled diamond inside the rounded square
                paint.style = Paint.Style.FILL
                paint.color = color // Adjust color as needed
                val diamondSize = outerSquareSize * 0.6f
                val halfDiamondSize = diamondSize / 2

                val diamondPath = Path().apply {
                    moveTo(
                        (x + circleDiameter / 2).toFloat(),
                        (y + (circleDiameter / 2 - halfDiamondSize)).toFloat()
                    ) // Top point
                    lineTo(
                        (x + (circleDiameter / 2 + halfDiamondSize)).toFloat(),
                        (y + circleDiameter / 2).toFloat()
                    ) // Right point
                    lineTo(
                        (x + circleDiameter / 2).toFloat(),
                        (y + (circleDiameter / 2 + halfDiamondSize)).toFloat()
                    ) // Bottom point
                    lineTo(
                        (x + (circleDiameter / 2 - halfDiamondSize)).toFloat(),
                        (y + circleDiameter / 2).toFloat()
                    ) // Left point
                    close()
                }
                canvas.drawPath(diamondPath, paint)
                qrCodeBitmap = createBitmapFromView(
                    requireContext(),
                    requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
                )
            }

            EyeShapeType.ROUNDED2 -> {
                // Draw the rounded square with stroke
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f
                val outerSquareSize = smallerShapeSize * 1.6f // Increase the size factor as needed
                val outerCornerRadius = outerSquareSize / 5f

                val rectF = RectF(
                    (x + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (x + circleDiameter / 2 + outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 + outerSquareSize / 2).toFloat()
                )
                canvas.drawRoundRect(rectF, outerCornerRadius, outerCornerRadius, paint)

                // Draw the filled circle inside the rounded square
                paint.style = Paint.Style.FILL
                paint.color = color // Adjust color as needed
                val innerCircleRadius = outerSquareSize * 0.4f
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    innerCircleRadius,
                    paint
                )
                qrCodeBitmap = createBitmapFromView(
                    requireContext(),
                    requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
                )

            }

            EyeShapeType.ROUNDED3 -> {
                // Draw the outer rounded square with stroke
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f
                val outerSquareSize = smallerShapeSize * 1.6f // Increase the size factor as needed
                val outerCornerRadius = outerSquareSize / 5f

                val outerRectF = RectF(
                    (x + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (x + circleDiameter / 2 + outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 + outerSquareSize / 2).toFloat()
                )
                canvas.drawRoundRect(outerRectF, outerCornerRadius, outerCornerRadius, paint)

                // Draw the inner rounded square filled
                paint.style = Paint.Style.FILL
                paint.color = color
                val innerSize = outerSquareSize * 0.6f
                val innerCornerRadius = innerSize / 5f

                val innerRectF = RectF(
                    (x + circleDiameter / 2 - innerSize / 2).toFloat(),
                    (y + circleDiameter / 2 - innerSize / 2).toFloat(),
                    (x + circleDiameter / 2 + innerSize / 2).toFloat(),
                    (y + circleDiameter / 2 + innerSize / 2).toFloat()
                )
                canvas.drawRoundRect(innerRectF, innerCornerRadius, innerCornerRadius, paint)
                qrCodeBitmap = createBitmapFromView(
                    requireContext(),
                    requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
                )
            }

            EyeShapeType.ROUNDED4 -> {
                // Draw the outer rounded square with stroke
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f
                val outerSquareSize = smallerShapeSize * 1.6f // Increase the size factor as needed
                val outerCornerRadius = outerSquareSize / 5f

                val outerRectF = RectF(
                    (x + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (x + circleDiameter / 2 + outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 + outerSquareSize / 2).toFloat()
                )
                canvas.drawRoundRect(outerRectF, outerCornerRadius, outerCornerRadius, paint)

                // Draw the inner square filled
                paint.style = Paint.Style.FILL
                paint.color = color
                val innerSize = outerSquareSize * 0.6f

                canvas.drawRect(
                    (x + circleDiameter / 2 - innerSize / 2).toFloat(),
                    (y + circleDiameter / 2 - innerSize / 2).toFloat(),
                    (x + circleDiameter / 2 + innerSize / 2).toFloat(),
                    (y + circleDiameter / 2 + innerSize / 2).toFloat(),
                    paint
                )
                qrCodeBitmap = createBitmapFromView(
                    requireContext(),
                    requireActivity().findViewById<ConstraintLayout>(R.id.newtemplate)
                )
            }


            EyeShapeType.DEFAULT -> {

                generateQRCodeBitmap(qrData)

            }
        }
    }

}