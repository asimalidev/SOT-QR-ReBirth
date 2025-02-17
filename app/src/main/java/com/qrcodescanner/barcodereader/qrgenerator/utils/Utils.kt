package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import com.qrcodescanner.barcodereader.qrgenerator.R

object Utils {

    fun hideSystemUI(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            activity.window.setDecorFitsSystemWindows(false)
            val controller = activity.window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // For Android 10 and below
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    fun toast(ctx : Context, msg: String) {
        if (ctx.resources.getString(R.string.ShowPopups).equals("")) {
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun mLog(msg: String) {
        Log.e("logmsg", msg)
    }
    fun logTag(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    fun showAlertDialog(ctx: Context) {
        val alertDialog: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(ctx)
        alertDialog.setTitle("AlertDialog")
        val items = arrayOf("Continue", "Rhythm")
        val checkedItem = 1
        alertDialog.setSingleChoiceItems(items, checkedItem,
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 ->
                        toast(ctx, "Clicked on Continue")
//                        Toast.makeText(ctx, "Clicked on Continue", Toast.LENGTH_LONG).show()

                    1 ->
                        toast(ctx, "Clicked on Rhythm")
                        //Toast.makeText(ctx, "Clicked on Rhythm", Toast.LENGTH_LONG).show()
                }
            })
        val alert: android.app.AlertDialog? = alertDialog.create()
        alert?.setCanceledOnTouchOutside(false)
        alert?.show()
    }

    fun hideStatusBar(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}