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
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AppFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var editText: EditText
    private lateinit var createButton: ConstraintLayout
    private lateinit var dbHelper: QRCodeDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        dbHelper = QRCodeDatabaseHelper(requireContext())
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = AppFragmentDirections.actionNavAppFragmentToCreateqr()
                    navController.navigate(action)
                }
            })
        editText = view.findViewById(R.id.passwordField1)
        createButton = view.findViewById(R.id.clBarCode)

        createButton.setOnClickListener {
            val appLink = editText.text.toString()
            if (validateAppLink(appLink)) {
//                insertDataIntoDatabase(appLink)
                val action =
                    AppFragmentDirections.actionNavAppFragmentToShowQRForAppFragment(appLink)
                navController.navigate(action)
            } else {
                Toast.makeText(requireContext(), "Invalid app link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateAppLink(link: String): Boolean {
        // Example validation logic for app link (You can customize this as needed)
        return link.startsWith("https://play.google.com/store/apps/details?id=") ||
                link.startsWith("https://apps.apple.com/")
    }

    override fun onResume() {
        super.onResume()
        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text=getString(R.string.apps)

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
