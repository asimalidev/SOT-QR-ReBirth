package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.text.InputFilter
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import apero.aperosg.monetization.util.showNativeAd
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_create

class CreateBarCodeFragment : Fragment() {

    private lateinit var enterEmailText: EditText
    private lateinit var clBarCode: View
    private var selectedType: String = "DataMatrix"
    private lateinit var navController: NavController
    private lateinit var topText: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_bar_code, container, false)

        CustomFirebaseEvents.logEvent(context = requireActivity(), screenName = "Create Barcode screen", trigger = "App display Create Barcode screen", eventName = "createbarcode_scr_")
        enterEmailText = view.findViewById(R.id.enterEmailText)
        clBarCode = view.findViewById(R.id.clBarCode)
        topText = requireActivity().findViewById(R.id.mainText)

        // Set initial main text to "DataMatrix" on create view

        topText.text = "DataMatrix"

        val tvDataMatrix: TextView = view.findViewById(R.id.tvDataMatrix)
        val tvPDF: TextView = view.findViewById(R.id.tvPDF)
        val tvAZtec: TextView = view.findViewById(R.id.tvAZtec)
        val tvEAN13: TextView = view.findViewById(R.id.tvEAN13)
        val tvEAN8: TextView = view.findViewById(R.id.tvEAN8)
        val tvUPCE: TextView = view.findViewById(R.id.tvUPCE)
        val tvUPCA: TextView = view.findViewById(R.id.tvUPCA)
        val tvCode128: TextView = view.findViewById(R.id.tvCode128)
        val tvCode93: TextView = view.findViewById(R.id.tvCode93)
        val tvCode39: TextView = view.findViewById(R.id.tvCode39)
        val tvCodaBar: TextView = view.findViewById(R.id.tvCodeBar)
        val ITF: TextView = view.findViewById(R.id.tvITF)

        tvDataMatrix.setOnClickListener {
            selectedType = "DataMatrix"
            updateMainText(selectedType)
            enterEmailText.hint =getString(R.string.text_without_special_character)
            enterEmailText.inputType = InputType.TYPE_CLASS_TEXT
            enterEmailText.filters = arrayOf()
            enterEmailText.setText("")
        }

        tvPDF.setOnClickListener {
            selectedType = "PDF417"
            updateMainText(selectedType)
            enterEmailText.hint = "Enter text"
            enterEmailText.inputType = InputType.TYPE_CLASS_TEXT
            enterEmailText.filters = arrayOf()
            enterEmailText.setText("")
        }

        tvCode39.setOnClickListener {
            selectedType = "Code39"
            updateMainText(selectedType)
            enterEmailText.hint = getString(R.string.uppercase_text_without_special_characters)
            enterEmailText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            enterEmailText.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                if (source != null) {
                    val filtered = source.filter { it.isUpperCase() && it.isLetterOrDigit() }
                    if (filtered.length == source.length) {
                        source // All characters are valid
                    } else {
                        "" // Some characters are invalid, so return an empty string to reject the input
                    }
                } else {
                    null
                }
            })
            enterEmailText.setText("")
        }

        tvCodaBar.setOnClickListener {
            selectedType = "CodeBar"
            updateMainText(selectedType)
            enterEmailText.hint = getString(R.string.enter_digits_only)
            enterEmailText.inputType = InputType.TYPE_CLASS_NUMBER
            enterEmailText.filters = arrayOf(InputFilter.LengthFilter(50))
            enterEmailText.setText("")
        }

        ITF.setOnClickListener {
            selectedType = "ITF"
            updateMainText(selectedType)
            enterEmailText.hint = getString(R.string.enter_even_digits)
            enterEmailText.inputType = InputType.TYPE_CLASS_NUMBER
            enterEmailText.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                source?.let {
                    if (it.all { c -> c.isDigit() && (c - '0') % 2 == 0 }) {
                        source
                    } else {
                        ""
                    }
                }
            })
            enterEmailText.setText("")
        }

        tvAZtec.setOnClickListener {
            selectedType = "AZtec"
            updateMainText(selectedType)
            enterEmailText.hint =getString(R.string.text_without_special_character)
            enterEmailText.inputType = InputType.TYPE_CLASS_TEXT
            enterEmailText.filters = arrayOf()
            enterEmailText.setText("")
        }

        tvEAN13.setOnClickListener {
            selectedType = "EAN13"
            updateMainText(selectedType)
            enterEmailText.hint = getString(R.string.enter_12_digits)
            enterEmailText.inputType = InputType.TYPE_CLASS_NUMBER
            enterEmailText.filters = arrayOf(InputFilter.LengthFilter(12))
            enterEmailText.setText("")
        }

        tvEAN8.setOnClickListener {
            selectedType = "EAN8"
            updateMainText(selectedType)
            enterEmailText.hint = getString(R.string.enter_7_digits)
            enterEmailText.inputType = InputType.TYPE_CLASS_NUMBER
            enterEmailText.filters = arrayOf(InputFilter.LengthFilter(7))
            enterEmailText.setText("")
        }

        tvUPCE.setOnClickListener {
            selectedType = "UPCE"
            updateMainText(selectedType)
            enterEmailText.hint = getString(R.string.enter_7_digits_starting_with_1)
            enterEmailText.inputType = InputType.TYPE_CLASS_NUMBER
            enterEmailText.filters = arrayOf(InputFilter.LengthFilter(7))
            enterEmailText.setText("")
        }

        tvUPCA.setOnClickListener {
            selectedType = "UPCA"
            updateMainText(selectedType)
            updateMainText(selectedType)
            enterEmailText.hint = getString(R.string.enter_11_digits_only)
            enterEmailText.inputType = InputType.TYPE_CLASS_NUMBER
            enterEmailText.filters = arrayOf(InputFilter.LengthFilter(11))
            enterEmailText.setText("")
        }

        tvCode128.setOnClickListener {
            selectedType = "Code128"
            updateMainText(selectedType)
            enterEmailText.hint = getString(R.string.text_without_special_characters)
            enterEmailText.inputType = InputType.TYPE_CLASS_TEXT
            enterEmailText.filters = arrayOf()
            enterEmailText.setText("")
        }

        tvCode93.setOnClickListener {
            selectedType = "Code93"
            updateMainText(selectedType)
            enterEmailText.hint =getString(R.string.uppercase_text_without_special_characters)
            enterEmailText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            enterEmailText.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                if (source != null) {
                    val filtered = source.filter { it.isUpperCase() && it.isLetterOrDigit() }
                    if (filtered.length == source.length) {
                        source // All characters are valid
                    } else {
                        "" // Some characters are invalid, so return an empty string to reject the input
                    }
                } else {
                    null
                }
            })
            enterEmailText.setText("")
        }

//        clBarCode.setOnClickListener {
//            val inputText = enterEmailText.text.toString()
//            if (isValidInput(inputText)) {
//                CustomFirebaseEvents.logEvent(context = requireActivity(), screenName = "Create Barcode screen", trigger = "User tap button Create", eventName = "createbarcode_scr_tap_create")
//                val action =
//                    CreateBarCodeFragmentDirections.actionCreateBarCodeFragmentToShowBarcodeFragment(
//                        inputText
//                    )
//                findNavController().navigate(action)
//            } else {
//                enterEmailText.error = getString(R.string.invalid_input)
//                Toast.makeText(requireContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show()
//            }
//        }

        clBarCode.setOnClickListener {
            val inputText = enterEmailText.text.toString()

            if (inputText.isEmpty()) {
                // Show toast if the input is empty
                Toast.makeText(requireContext(), getString(R.string.fill_email_first), Toast.LENGTH_SHORT).show()
                // Optionally, set an error on the EditText
                enterEmailText.error = getString(R.string.fill_email_first)
            } else if (isValidInput(inputText)) {
                CustomFirebaseEvents.logEvent(context = requireActivity(), screenName = "Create Barcode screen", trigger = "User tap button Create", eventName = "createbarcode_scr_tap_create")

                val action = CreateBarCodeFragmentDirections.actionCreateBarCodeFragmentToShowBarcodeFragment(inputText)
                findNavController().navigate(action)
            } else {
                // Show error for invalid input
                enterEmailText.error = getString(R.string.invalid_input)
                Toast.makeText(requireContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topText = requireActivity().findViewById(R.id.mainText)
        navController = findNavController()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    CustomFirebaseEvents.logEvent(context = requireActivity(), screenName = "Create Barcode screen", trigger = "User tap button Back", eventName = "createbarcode_scr_tap_back")
                    val action = CreateBarCodeFragmentDirections.actionCreateBarCodeFragmentToHome()
                    navController.navigate(action)
                }
            })
    }

    private fun isValidInput(input: String): Boolean {
        return when (selectedType) {
            "DataMatrix", "AZtec" -> input.all { it.isLetterOrDigit() }
            "PDF417" -> true
            "EAN13" -> input.length == 12 && input.all { it.isDigit() }
            "EAN8" -> input.length == 7 && input.all { it.isDigit() }
            "UPCA" -> input.length == 11 && input.all { it.isDigit() }
            "UPCE" -> input.length == 7 && input.startsWith("1") && input.all { it.isDigit() }
            "Code128" -> input.none { it.isWhitespace() }
            "Code93" -> input.all { it.isUpperCase() && it.isLetterOrDigit() }
            "Code39" -> input.all { it.isUpperCase() && it.isLetterOrDigit() }
            "CodeBar" -> input.all { it.isDigit() }
            "ITF" -> input.all { it in '0'..'9' && (it - '0') % 2 == 0 }
            else -> false
        }
    }

    private fun updateMainText(type: String) {
        if (::topText.isInitialized) {
            topText.text = type
        }
    }


    override fun onResume() {
        super.onResume()

        if (NetworkCheck.isNetworkAvailable(requireContext())) {
            AdsProvider.nativeCreate.config(requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_create, true))
            AdsProvider.nativeCreate.loadAds(MyApplication.getApplication())
            showNativeAd(AdsProvider.nativeCreate, requireActivity().findViewById(R.id.layoutAdNative), R.layout.layout_home_native_ad)
        }
        // Set initial main text to "DataMatrix" on resume
        topText.text = selectedType

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
            ivClose.visibility = View.INVISIBLE
        }

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }
    }
}


