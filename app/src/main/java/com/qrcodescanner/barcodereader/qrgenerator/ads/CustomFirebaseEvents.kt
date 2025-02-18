package com.qrcodescanner.barcodereader.qrgenerator.ads

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

object CustomFirebaseEvents {
    lateinit var analytics: FirebaseAnalytics
    lateinit var bundle: Bundle
    lateinit var className: String

    fun init(context: Activity) {
        analytics = FirebaseAnalytics.getInstance(context)
        bundle = Bundle()
        className = context.localClassName
    }

//    fun nativeAdEvent(context: Activity) {
//        init((context))
//        bundle.putString("Native$className", "Native$className")
//        analytics.logEvent("Native$className", bundle)
//    }
//
//    fun nativeKeypadAdEvent(context: Context, eventName: String) {
//        analytics = FirebaseAnalytics.getInstance(context)
//        bundle = Bundle()
//
//        bundle.putString("EventName$eventName", "Event$eventName")
//        analytics.logEvent("KeypadNativeAd$eventName", bundle)
//    }
//
//    fun customDialogDismissEvent(context: Context, eventName: String) {
//        analytics = FirebaseAnalytics.getInstance(context)
//        bundle = Bundle()
//        bundle.putString("customDialogDismiss$eventName", "customDialogDismiss$eventName")
//        analytics.logEvent("customDialogDismiss$eventName", bundle)
//    }
//
//    fun interstitialAdEvent(context: Activity) {
//        init((context))
//        bundle.putString("Interstitial$className", "Interstitial$className")
//        analytics.logEvent("Interstitial$className", bundle)
//    }
//
//    /*fun logEvent(context: Activity, screenName: String, eventName: String, trigger: String? = null) {
//        init(context)
//
//        Log.d("EventTracking", "logEvent: $screenName")
//        bundle.putString("screen_name", screenName)
//
//        trigger?.let {
//            bundle.putString("action_detail", it)
//        }
//
//        analytics.logEvent(eventName, null)
//    }*/

    fun logEvent(context: Activity, screenName: String = "", trigger: String = "", eventName: String) {
        init(context)
        Log.d("EventTracking", "logEvent: $eventName")
        analytics.logEvent(eventName, null)
    }
}