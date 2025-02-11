package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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

class EmailFragment : Fragment() {

    private lateinit var emailAddress: EditText
    private lateinit var subject: EditText
    private lateinit var message: EditText
    private lateinit var createQRButton: ConstraintLayout
    private lateinit var characterCountText: TextView
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_email, container, false)
        emailAddress = view.findViewById(R.id.emailAddress)
        subject = view.findViewById(R.id.enterSubject)
        message = view.findViewById(R.id.enterEmailText)
        createQRButton = view.findViewById(R.id.clBarCode)
        characterCountText = view.findViewById(R.id.characterCountText)
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Tab Create",
            trigger = "User tap a type of QR want to create",
            eventName = "createqr_scr"
        )

        createQRButton.setOnClickListener {
            if (validateFields()) {
//                saveEmailToDatabase()
                navigateToQRCodeFragment()
            }
        }

        message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }

            override fun afterTextChanged(s: Editable?) {
                val length = s?.length ?: 0
                characterCountText.text = "$length/500"
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = EmailFragmentDirections.emailfraggmentToCreateQRFragment()
                    navController!!.navigate(action)
                }
            })
    }

    private fun validateFields(): Boolean {
        val email = emailAddress.text.toString()
        val subj = subject.text.toString()
        val msg = message.text.toString()

        if (email.isEmpty()) {
            emailAddress.error = "Email address is required"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailAddress.error = "Invalid email address"
            return false
        }
        if (subj.isEmpty()) {
            subject.error = "Subject is required"
            return false
        }
        if (msg.isEmpty()) {
            message.error = "Message is required"
            return false
        }
        if (msg.length > 500) {
            message.error = "Message cannot exceed 500 characters"
            return false
        }

        return true
    }

//    private fun saveEmailToDatabase() {
//        val email = emailAddress.text.toString()
//        val subj = subject.text.toString()
//        val msg = message.text.toString()
//
//        val emailDetails = "$email\n$subj\n$msg"
//        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//        val time = SimpleDateFormat("H:mm a", Locale.getDefault()).format(Date())
//        val drawable = R.drawable.ic_email
//
//        val dbHelper = QRCodeDatabaseHelper(requireContext())
//        dbHelper.insertQRCode(emailDetails, date, time, drawable)
//    }

    private fun navigateToQRCodeFragment() {
        val email = emailAddress.text.toString()
        val subj = subject.text.toString()
        val msg = message.text.toString()

        val action = EmailFragmentDirections.actionEmailFragmentToQrCodeFragment(email, subj, msg)
        navController.navigate(action)
    }

    override fun onResume() {
        super.onResume()
        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.email)

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
            download.visibility = View.GONE
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        if (ivClose != null) {
            ivClose.setImageResource(R.drawable.ic_premium)
            ivClose.visibility = View.INVISIBLE
        }
    }
}



