package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContactsFragment : Fragment() {
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var companyNameEditText: EditText
    private lateinit var jobTitleEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var countryEditText: EditText
    private lateinit var postalCodeEditText: EditText
    private lateinit var navController:NavController
    private lateinit var dbHelper: QRCodeDatabaseHelper


    private lateinit var generateQRButton: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Tab Create",
            trigger = "User tap a type of QR want to create",
            eventName = "createqr_scr"
        )
        firstNameEditText = view.findViewById(R.id.tvFirstName)
        lastNameEditText = view.findViewById(R.id.tvLastName)
        phoneNumberEditText = view.findViewById(R.id.tvPhoneNumber)
        companyNameEditText = view.findViewById(R.id.tvCompanyName)
        jobTitleEditText = view.findViewById(R.id.tvJobTitle)
        emailEditText = view.findViewById(R.id.tvEmail)
        addressEditText = view.findViewById(R.id.tvAddress)
        countryEditText = view.findViewById(R.id.tvCountry)
        postalCodeEditText = view.findViewById(R.id.tvPostalCode)
        dbHelper = QRCodeDatabaseHelper(requireContext())
        generateQRButton = view.findViewById(R.id.clBarCode)
        generateQRButton.setOnClickListener {
            generateQRCode()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController=findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = ContactsFragmentDirections.actionContactsToCreateqr()
                    navController!!.navigate(action)
                }
            })
    }

    private fun generateQRCode() {
        // Retrieve data from EditText fields
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val companyName = companyNameEditText.text.toString().trim()
        val jobTitle = jobTitleEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val country = countryEditText.text.toString().trim()
        val postalCode = postalCodeEditText.text.toString().trim()

        // Create a string with the contact information (customize as per your QR code format)
        val contactInfo = "Name: $firstName $lastName\n" +
                "Phone: $phoneNumber\n" +
                "Email: $email\n" +
                "Company: $companyName\n" +
                "Job Title: $jobTitle\n" +
                "Address: $address, $country, $postalCode"

        // Generate QR code using the contactInfo string
        // Use your QR code generation logic here (ZXing library or any other library)

        // Navigate to the ShowClipboardQRFragment passing generated QR code if needed
//        insertDataIntoDatabase(contactInfo)
        val action = ContactsFragmentDirections.actionContactsToShowContactQr(contactInfo)
        findNavController().navigate(action)
    }


//    private fun insertDataIntoDatabase(combinedText: String) {
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val timeFormat = SimpleDateFormat("H:mm a", Locale.getDefault())
//        val date = dateFormat.format(Date())
//        val time = timeFormat.format(Date())
//
//        val drawableResId = R.drawable.ic_contacts
//
//        val success = dbHelper.insertQRCode(combinedText, date, time, drawableResId)
//        if (success) {
//            Toast.makeText(requireContext(), "Data inserted successfully", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(requireContext(), "Failed to insert data", Toast.LENGTH_SHORT).show()
//        }
//    }

    override fun onResume() {
        super.onResume()

        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text=getString(R.string.contact)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        if (back != null) {
            back.visibility = View.VISIBLE
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

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

    }
}
