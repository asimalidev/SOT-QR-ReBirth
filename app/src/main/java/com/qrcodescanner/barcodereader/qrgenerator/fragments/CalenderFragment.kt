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

class CalenderFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var dbHelper: QRCodeDatabaseHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calender, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = QRCodeDatabaseHelper(requireContext())
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Tab Create",
            trigger = "User tap a type of QR want to create",
            eventName = "createqr_scr"
        )
        navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = CalenderFragmentDirections.actionCalenderFragmentToCreateqr()
                    navController!!.navigate(action)
                }
            })

        // Assuming clBarCode is the ConstraintLayout that triggers QR code creation
        view.findViewById<ConstraintLayout>(R.id.clBarCode).setOnClickListener {
            val eventTitle = view.findViewById<EditText>(R.id.tvEventTitle).text.toString()
            val allDayEvent = view.findViewById<EditText>(R.id.tvAllDayEvent).text.toString()
            val startDate = view.findViewById<EditText>(R.id.tvstartDate).text.toString()
            val startTime = view.findViewById<EditText>(R.id.tvStartTime).text.toString()
            val endDate = view.findViewById<EditText>(R.id.tvEndDate).text.toString()
            val endTime = view.findViewById<EditText>(R.id.tvEndTime).text.toString()
            val eventLocation = view.findViewById<EditText>(R.id.tvEventLocation).text.toString()
            val description = view.findViewById<EditText>(R.id.tvDescription).text.toString()
            val combinedText = "$eventTitle|$allDayEvent|$startDate|$startTime|$endDate|$endTime|$eventLocation|$description"
//            insertDataIntoDatabase(combinedText)
            val action = CalenderFragmentDirections.actionCalenderFragmentToShowCalenderFragment(
                eventTitle, allDayEvent, startDate, startTime, endDate, endTime,
                eventLocation, description
            )
            navController.navigate(action)
        }
    }

//    private fun insertDataIntoDatabase(combinedText: String) {
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val timeFormat = SimpleDateFormat("H:mm a", Locale.getDefault())
//        val date = dateFormat.format(Date())
//        val time = timeFormat.format(Date())
//
//        val drawableResId = R.drawable.ic_calender
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
        TopText.text = getString(R.string.calender)

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
