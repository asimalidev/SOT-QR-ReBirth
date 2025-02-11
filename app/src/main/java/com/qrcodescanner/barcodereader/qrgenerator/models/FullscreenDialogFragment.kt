package com.qrcodescanner.barcodereader.qrgenerator.models

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity.Companion.isFullScreenDialogVisible
import com.qrcodescanner.barcodereader.qrgenerator.notification.AppAlarmManager
import com.qrcodescanner.barcodereader.qrgenerator.notification.AppNotificationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FullscreenDialogFragment : DialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_fullscreen_custom, container, false)

        val window = dialog?.window
        window?.let {
            val params = it.attributes
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.setBackgroundDrawableResource(android.R.color.transparent) // Optional background
            it.attributes = params

            // Set margins using DialogUtils
            DialogUtils.setMargins(it, 40, 0, 40, 0)
        }

        isFullScreenDialogVisible = true
        setupUI(view)
        return view
    }


    @SuppressLint("DefaultLocale")
    private fun setupUI(view: View) {
        view.findViewById<ConstraintLayout>(R.id.lltime).setOnClickListener {
            val calendar = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val time = String.format(
                    "%02d:%02d %s",
                    if (hourOfDay > 12) hourOfDay - 12 else hourOfDay,
                    minute,
                    if (hourOfDay >= 12) "PM" else "AM"
                )
                view.findViewById<TextView>(R.id.tvTime).text = time
            }

            TimePickerDialog(
                requireContext(),
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        view.findViewById<ImageView>(R.id.ivDoneNotificationTime).setOnClickListener {
            val timeString = view.findViewById<TextView>(R.id.tvTime).text.toString()
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            try {
                val selectedTime = sdf.parse(timeString)
                val selectedCal = Calendar.getInstance()
                selectedCal.time = selectedTime!!

                val hour = selectedCal.get(Calendar.HOUR_OF_DAY)
                val minute = selectedCal.get(Calendar.MINUTE)

                // Save time to SharedPreferences
                saveTimeToPreferences(hour, minute)

                // Schedule the notification
                AppAlarmManager.scheduleLockscreenWidget(
                    requireContext(),
                    AppAlarmManager.lockscreenWidgetRequestCode,
                    hour,
                    minute
                )
                Toast.makeText(context, "schedule set", Toast.LENGTH_SHORT).show()
                isFullScreenDialogVisible = false
                dismiss()
            } catch (e: ParseException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Invalid time format.", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<TextView>(R.id.tvRemindMeLater).setOnClickListener {
            isFullScreenDialogVisible = false
            dismiss()
        }
    }

    private fun saveTimeToPreferences(hour: Int, minute: Int) {
        val sharedPref =
            requireContext().getSharedPreferences("NotificationTimePrefs", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putInt("hour", hour)
            putInt("minute", minute)
            apply()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        isFullScreenDialogVisible = false
    }
}


