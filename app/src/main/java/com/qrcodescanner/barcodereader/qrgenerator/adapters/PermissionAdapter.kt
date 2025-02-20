package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity.Companion.isFullScreenDialogVisible
import com.qrcodescanner.barcodereader.qrgenerator.databinding.LayoutPermissionsBottomSheetBinding
import com.qrcodescanner.barcodereader.qrgenerator.models.Permission

class PermissionAdapter(
    private val permissions: MutableList<Permission>,
    private val onPermissionChanged: (Permission) -> Unit,
    private val onRequestCameraPermission: () -> Unit, // Callback to request camera permission
    private val onRequestNotificationPermission: () -> Unit, // Callback to request notification permission
    private val onRequestFullIntentPermission: () -> Unit, // Callback to request notification permission
    private val homeActivity: HomeActivity // Pass HomeActivity instance
) : RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {
    companion object {
        private const val REQUEST_FULLSCREEN_PERMISSION = 1003
    }


override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
    // Inflate the view for the permission item
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_permission, parent, false)

    // Create the dialogBinding and get the ad configuration value
    val dialogBinding = LayoutPermissionsBottomSheetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    val isAdEnabled = parent.context.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        .getBoolean("native_permission_bottomsheet", true)

    // Return the PermissionViewHolder with the necessary parameters
    return PermissionViewHolder(view, this, dialogBinding, isAdEnabled)
}


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        val permission = permissions[position]
        holder.bind(permission, position)
    }

    override fun getItemCount(): Int = permissions.size

    inner class PermissionViewHolder(itemView: View, private val adapter: PermissionAdapter, private val dialogBinding: LayoutPermissionsBottomSheetBinding,
                                     private val isAdEnabled: Boolean) : RecyclerView.ViewHolder(itemView) {
        private val switch: Switch = itemView.findViewById(R.id.switchPermission)
        private val nameText: TextView = itemView.findViewById(R.id.tvPermissionName)
        private val descriptionText: TextView = itemView.findViewById(R.id.tvPermissionDescription)

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun bind(permission: Permission, position: Int) {
            nameText.text = permission.name
            descriptionText.text = permission.description

            switch.setOnCheckedChangeListener(null)
            val context = itemView.context
            when (permission.key) {
                "cameraPermission" -> {
                    val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    switch.isChecked = isCameraPermissionGranted

                    switch.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            if (!isCameraPermissionGranted) {
                                onRequestCameraPermission() // Request camera permission
                            } else {
                                permission.isChecked = true
                                onPermissionChanged(permission)

                            }
                        } else {
                            permission.isChecked = false
                            onPermissionChanged(permission)
                        }
                        homeActivity.showNativeAdIfAvailable(isAdEnabled, context as HomeActivity, dialogBinding)
                        refreshItem(position)
                    }
                }

                "notificationPermission" -> {
                    val isNotificationPermissionGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    switch.isChecked = isNotificationPermissionGranted
                    switch.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            if (!isNotificationPermissionGranted) {
                                onRequestNotificationPermission() // Request notification permission
                            } else {
                                permission.isChecked = true
                                onPermissionChanged(permission)
                            }
                        } else {
                            permission.isChecked = false
                            onPermissionChanged(permission)
                        }
                        homeActivity.showNativeAdIfAvailable(isAdEnabled, context as HomeActivity, dialogBinding)
                        refreshItem(position)
                    }
                }

                "dailyAwesomePermission" -> {
                    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    var isSwitchEnabled = sharedPreferences.getBoolean("dailyAwesomePermission", false)
                    switch.isChecked = isSwitchEnabled

                    switch.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    ActivityCompat.requestPermissions(
                                        context as Activity,
                                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                        REQUEST_FULLSCREEN_PERMISSION
                                    )
                                }
                            }

                            sharedPreferences.edit().putBoolean("dailyAwesomePermission", true).apply()
                            permission.isChecked = true
                            isSwitchEnabled=true
                            onPermissionChanged(permission)
                            if (!isFullScreenDialogVisible) {
                                homeActivity.showFullScreenNotification()
                            }
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(context, "It calls the layout after 1 minute", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            sharedPreferences.edit().putBoolean("dailyAwesomePermission", false).apply()
                            permission.isChecked = false
                            isSwitchEnabled=false
                            onPermissionChanged(permission)
                        }
                        homeActivity.showNativeAdIfAvailable(isAdEnabled, context as HomeActivity, dialogBinding)
                        refreshItem(position)
                    }
                }

                else -> {
                    switch.isChecked = permission.isChecked

                    switch.setOnCheckedChangeListener { _, isChecked ->
                        permission.isChecked = isChecked
                        onPermissionChanged(permission)
                        refreshItem(position)
                    }
                }
            }
        }
        }

        private fun refreshItem(position: Int) {
            notifyItemChanged(position)
        }
}




