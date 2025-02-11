package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.os.Bundle
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


class WifiFragment : Fragment() {
    private lateinit var tvNetwork: EditText
    private lateinit var Password: EditText
    private lateinit var createQRButton: ConstraintLayout
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_wifi, container, false)
        tvNetwork = view.findViewById(R.id.tvNetworkName)
        Password = view.findViewById(R.id.tvPassword)
        createQRButton = view.findViewById(R.id.clBarCode)

        createQRButton.setOnClickListener {
            navigateToQRCodeFragment()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Tab Create",
            trigger = "User tap a type of QR want to create",
            eventName = "createqr_scr"
        )
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = WifiFragmentDirections.actionWifiFragmentToCreateqr()
                    navController!!.navigate(action)
                }
            })
    }

    private fun navigateToQRCodeFragment() {
        val network = tvNetwork.text.toString()
        val password = Password.text.toString()
        val combinedData = combineNetworkAndPassword(network, password)
        val dateTime = getCurrentDateTime()
        val currentDate = SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val drawable = R.drawable.ic_wifi
        val dbHelper = QRCodeDatabaseHelper(requireContext())
//        val result = dbHelper.insertQRCode(combinedData, "Created on$currentDate", currentTime, drawable)
        val action =
            WifiFragmentDirections.actionWifiFragmentToShowWifiQrFragment(network, password)
        navController.navigate(action)
    }

    private fun combineNetworkAndPassword(network: String, password: String): String {
        return "$network\n$password"
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onResume() {
        super.onResume()
        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text=getString(R.string.wifi)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        if (back != null) {
            back.visibility = View.VISIBLE
        }

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
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

    }
}