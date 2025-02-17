package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

object PermissionUtils {

    fun checkUserPermission(activity: Activity, permissionsList: Collection<String>, onAllGranted:() -> Unit) {
        Dexter.withActivity(activity)
            .withPermissions(permissionsList)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        onAllGranted.invoke()
                        Log.i("PermissionsUtils", "Are All Permissions Granted")
                    } else {
                        val responses = report.deniedPermissionResponses
                        val permissionsDenied = StringBuilder("Permissions denied: ")
                        for (response in responses) {
                            permissionsDenied.append(response.permissionName).append(" ")
                        }
                        Log.i("PermissionsUtils", "Denied Permissions$permissionsDenied")
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        val dialog = AlertDialog.Builder(activity)
                            .setTitle("Need Permissions")
                            .setMessage(
                                "This application need to use some permissions, " +
                                        "you can grant them in the application settings."
                            )
                            .setPositiveButton("GOTO SETTINGS") { dialogInterface, i ->
                                dialogInterface.cancel()
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", activity.packageName, null)
                                intent.setData(uri)
                                activity.startActivity(intent)
                            }
                            .setNegativeButton("CANCEL") { dialogInterface, i ->
                                dialogInterface.cancel()
                            }
                        dialog.show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()
    }
}