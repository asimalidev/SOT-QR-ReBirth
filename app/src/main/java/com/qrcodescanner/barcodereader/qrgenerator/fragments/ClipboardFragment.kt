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

class ClipboardFragment : Fragment() {
    private lateinit var editText: EditText
    private lateinit var navController: NavController
    private lateinit var dbHelper: QRCodeDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clipboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Tab Create",
            trigger = "User tap a type of QR want to create",
            eventName = "createqr_scr"
        )
        navController = findNavController()
        dbHelper = QRCodeDatabaseHelper(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = ClipboardFragmentDirections.actionClipboardFragmentToCreateqr()
                    navController.navigate(action)
                }
            })

        editText = view.findViewById(R.id.passwordField1)
        val requireText = ClipboardFragmentArgs.fromBundle(requireArguments()).clipboardText
        editText.setText(requireText)

        val clBarCode = view.findViewById<ConstraintLayout>(R.id.clBarCode)
        clBarCode.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) {
//                insertClipboardData(text)
                val action = ClipboardFragmentDirections.actionClipboardFragmentToShowclipboardQR(text)
                navController.navigate(action)
            } else {
                Toast.makeText(requireContext(), "Text cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.clipboard)

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
            ivClose.setImageResource(R.drawable.ic_premium)
            ivClose.visibility = View.INVISIBLE
        }
        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }
    }
}


