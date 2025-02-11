package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateLinksQRCodeFragment : Fragment() {
    private lateinit var platform: String
    private lateinit var editText: EditText
    private lateinit var navController: NavController
    private lateinit var dbHelper: QRCodeDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_links_q_r_code, container, false)
        navController = findNavController()
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Tab Create",
            trigger = "User tap a type of QR want to create",
            eventName = "createqr_scr"
        )
        dbHelper = QRCodeDatabaseHelper(requireContext())
        platform = CreateLinksQRCodeFragmentArgs.fromBundle(requireArguments()).platform
        editText = view.findViewById(R.id.passwordField1)

        // Set onClickListeners for http, www, and com TextViews
        view.findViewById<TextView>(R.id.http).setOnClickListener {
            editText.append("https://")
        }
        view.findViewById<TextView>(R.id.www).setOnClickListener {
            editText.append("www.")
        }
        view.findViewById<TextView>(R.id.com).setOnClickListener {
            editText.append(".com")
        }

        // Validate URL based on platform
        validatePlatformURL(platform)


        // Handle click on clBarCode for navigation
        view.findViewById<ConstraintLayout>(R.id.clBarCode).setOnClickListener {
            val url = editText.text.toString()
            if (validateUrl(url)) {
                navigateToViewQRCode(url)
//                insertDataIntoDatabase(url)
            } else {
                editText.error = "Invalid URL"
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (navController!=null){
                    val action =
                        CreateLinksQRCodeFragmentDirections.actionCreateLinksQRCodeToCreateqrcode()
                    navController!!.navigate(action)}else{
                        isNavControllerAdded()
                    }
                }
            })
    }

//    private fun insertDataIntoDatabase(combinedText: String) {
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val timeFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
//        val date = dateFormat.format(Date())
//        val time = timeFormat.format(Date())
//
//        val drawableResId = getDrawableResIdForPlatform(platform)
//
//        val success = dbHelper.insertQRCode(combinedText, date, time, drawableResId)
//        if (success) {
//            Toast.makeText(requireContext(), "Data inserted successfully", Toast.LENGTH_SHORT)
//                .show()
//        } else {
//            Toast.makeText(requireContext(), "Failed to insert data", Toast.LENGTH_SHORT).show()
//        }
//    }


    private fun getDrawableResIdForPlatform(platform: String): Int {
        return when (platform.toLowerCase()) {
            "youtube" -> R.drawable.ic_youtube
            "facebook" -> R.drawable.ic_facebook
            "instagram" -> R.drawable.ic_instagram
            "twitter" -> R.drawable.ic_twitter
            "whatsapp" -> R.drawable.ic_whatsapp
            "dailymotion" -> R.drawable.ic_dailymotion
            "website" -> R.drawable.ic_website
            else -> R.drawable.ic_website // Fallback drawable
        }
    }


    private fun validatePlatformURL(platform: String) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Validation on text change can be removed or adjusted as needed
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun validateUrl(url: String): Boolean {
        // Implement your URL validation logic based on platform here
        return when (platform.toLowerCase()) {
            "youtube" -> {
                if (!isValidYouTubeUrl(url)) {
                    Toast.makeText(requireContext(), "Invalid YouTube URL", Toast.LENGTH_SHORT)
                        .show()
                }
                isValidYouTubeUrl(url)
            }

            "facebook" -> {
                if (!isValidFacebookUrl(url)) {
                    Toast.makeText(requireContext(), "Invalid Facebook URL", Toast.LENGTH_SHORT)
                        .show()
                }
                isValidFacebookUrl(url)
            }

            "instagram" -> {
                if (!isValidInstagramUrl(url)) {
                    Toast.makeText(requireContext(), "Invalid Instagram URL", Toast.LENGTH_SHORT)
                        .show()
                }
                isValidInstagramUrl(url)
            }

            "twitter" -> {
                if (!isValidTwitterUrl(url)) {
                    Toast.makeText(requireContext(), "Invalid Twitter URL", Toast.LENGTH_SHORT)
                        .show()
                }
                isValidTwitterUrl(url)
            }

            "whatsapp" -> {
                if (!isValidWhatsAppUrl(url)) {
                    Toast.makeText(requireContext(), "Invalid WhatsApp URL", Toast.LENGTH_SHORT)
                        .show()
                }
                isValidWhatsAppUrl(url)
            }

            "dailymotion" -> {
                if (!isValidDailyMotionUrl(url)) {
                    Toast.makeText(requireContext(), "Invalid DailyMotion URL", Toast.LENGTH_SHORT)
                        .show()
                }
                isValidDailyMotionUrl(url)
            }

            "website" -> {
                if (!isValidWebsiteUrl(url)) {
                    Toast.makeText(requireContext(), "Invalid Website URL", Toast.LENGTH_SHORT)
                        .show()
                }
                isValidWebsiteUrl(url)
            }

            else -> false
        }
    }


    private fun isValidYouTubeUrl(url: String): Boolean {
        // Validating YouTube URL
        return url.startsWith("https://www.youtube.com") || url.startsWith("https://youtu.be")
    }

    private fun isValidFacebookUrl(url: String): Boolean {
        // Validating Facebook URL
        return url.startsWith("https://www.facebook.com") || url.startsWith("https://m.facebook.com")
    }

    private fun isValidInstagramUrl(url: String): Boolean {
        // Validating Instagram URL
        return url.startsWith("https://www.instagram.com") || url.startsWith("https://instagram.com")
    }

    private fun isValidTwitterUrl(url: String): Boolean {
        // Validating Twitter URL
        return url.startsWith("https://twitter.com") || url.startsWith("https://mobile.twitter.com")
    }

    private fun isValidWhatsAppUrl(url: String): Boolean {
        // Validating WhatsApp URL
        return url.startsWith("https://wa.me") || url.startsWith("https://api.whatsapp.com")
    }

    private fun isValidDailyMotionUrl(url: String): Boolean {
        // Validating Daily Motion URL
        return url.startsWith("https://www.dailymotion.com") || url.startsWith("https://dai.ly")
    }

    private fun isValidWebsiteUrl(url: String): Boolean {
        // Validating generic Website URL (starts with http:// or https://)
        return url.startsWith("http://") || url.startsWith("https://")
    }

    private fun navigateToViewQRCode(url: String) {
        if (navController != null) {
            val action =
                CreateLinksQRCodeFragmentDirections.actionCreateLinksQRCodeToQRcreationURL(url)
            navController.navigate(action)
        } else {
            isNavControllerAdded()
        }
    }

    override fun onResume() {
        super.onResume()
        isNavControllerAdded()
        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.url)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        if (back != null) {
            back.visibility = View.VISIBLE
        }
        back.setOnClickListener {
            requireActivity().onBackPressed()
        }
        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
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

    private fun isNavControllerAdded() {
        navController = findNavController()
    }
}


