package com.qrcodescanner.barcodereader.qrgenerator.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.qrcodescanner.barcodereader.qrgenerator.R
import java.lang.Exception

class CustomDialog private constructor() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var dialogAAFontsUtility: CustomDialog? = null
        var dialog: Dialog? = null
            private set
        @SuppressLint("StaticFieldLeak")
        private var context: Activity? = null
        fun getInstance(con: Activity?): CustomDialog? {
            context = con
            return if (dialogAAFontsUtility == null) {
                dialogAAFontsUtility = CustomDialog()
                dialogAAFontsUtility
            } else {
                dialogAAFontsUtility
            }
        }
    }

    fun setContentView(view: View?, isCancelable: Boolean): CustomDialog? {
        context?.let {
            dialog = Dialog(it)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog?.setContentView(view!!)
            dialog?.setCancelable(isCancelable)
            val displayMetrics = DisplayMetrics()
            it.windowManager.defaultDisplay.getMetrics(displayMetrics)
            dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
            return dialogAAFontsUtility
        }
        return null
    }

    fun showDialogInterstitial(): Dialog? {
        dialog?.let {
            if(!it.isShowing) {
                dialog?.show()
            }
        }
        return dialog
    }

    fun showDialogResume(ctx: Activity): Dialog? {
        dialog?.let {
            if(!it.isShowing) {
                if (!ctx.isFinishing) {
                    dialog?.show()
                }
            }
        }
        return dialog
    }

    fun dismissDialog(activity: Activity) {
        try {
            dialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        } catch (ex: Exception) {
            activity.let {
//                CustomFirebaseEvents.customDialogDismissEvent(activity, activity.localClassName)
            }
        }
    }
}