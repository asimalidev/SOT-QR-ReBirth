package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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


class LocationFragment : Fragment() {

    private lateinit var etLocation: EditText
    private lateinit var btnGenerateQR: ConstraintLayout
    private lateinit var navController: NavController
    private lateinit var dbHelper: QRCodeDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etLocation = view.findViewById(R.id.etLocation)
        btnGenerateQR = view.findViewById(R.id.clBarCode)
        navController = findNavController()
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Tab Create",
            trigger = "User tap a type of QR want to create",
            eventName = "createqr_scr"
        )
        dbHelper = QRCodeDatabaseHelper(requireContext())
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = LocationFragmentDirections.actionLocationFragmentToCreateqr()
                    navController.navigate(action)
                }
            })
        btnGenerateQR.setOnClickListener {
            val location = etLocation.text.toString().trim()

            // Validate location before inserting into the database
            if (location.isNotEmpty() && isValidLocation(location)) {
//                insertDataIntoDatabase(location)
                val action =
                    LocationFragmentDirections.actionLocationFragmentToShowLocationQRFragment(location)
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Please enter a valid location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidLocation(location: String): Boolean {
        val lowercaseLocation = location.toLowerCase().trim() // Trim whitespace and convert to lowercase

        // Check if it's a Google Maps URL
        if (lowercaseLocation.startsWith("http") && lowercaseLocation.contains("maps")) {
            return true
        }

        // Check if it's a latitude,longitude format
        val latLongPattern = """^[-+]?(\d{1,2}\.\d+|\d{1,3}\.\d+)\s*,\s*[-+]?(\d{1,2}\.\d+|\d{1,3}\.\d+)$""".toRegex()
        return latLongPattern.matches(lowercaseLocation)
    }

//    private fun insertDataIntoDatabase(combinedText: String) {
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
//        val date = dateFormat.format(Date())
//        val time = timeFormat.format(Date())
//
//        val drawableResId = R.drawable.ic_location
//
//        // Insert into the database and show a success toast only if the insertion is successful
//        val success = dbHelper.insertQRCode(combinedText, date, time, drawableResId)
//        if (success) {
//            Toast.makeText(requireContext(), "Data inserted successfully", Toast.LENGTH_SHORT).show()
//        }
//    }


    override fun onResume() {
        super.onResume()
        val topText: TextView = requireActivity().findViewById(R.id.mainText)
        topText.visibility = View.VISIBLE
        topText.text = getString(R.string.location)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        back?.visibility = View.VISIBLE
        back?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        setting?.visibility = View.INVISIBLE

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        ivClose?.setImageResource(R.drawable.ic_premium)
        ivClose?.visibility = View.INVISIBLE
    }
}

