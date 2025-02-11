package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import apero.aperosg.monetization.util.showNativeAd
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentWalkThroughOneBinding
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb1_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb1_2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkThroughOneFragment(val prefHelper: PrefHelper) : Fragment() {

    lateinit var binding: FragmentWalkThroughOneBinding
    var reloadAdAfterClickOrResume = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalkThroughOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Onboard 1",
            trigger = "App display Onboard 1 screen",
            eventName = "onboard1_scr"
        )

        val constraintLayout = view.findViewById<ConstraintLayout>(R.id.cl2)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        if (NetworkCheck.isNetworkAvailable(requireContext()) &&
            (requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)
                .getBoolean(native_onb1_2, false) ||
                    requireActivity().getSharedPreferences(
                        "RemoteConfig",
                        AppCompatActivity.MODE_PRIVATE
                    ).getBoolean(native_onb1_0, false))
        ) {
            constraintSet.connect(
                R.id.btnNext,
                ConstraintSet.BOTTOM,
                R.id.layoutAdNative,
                ConstraintSet.TOP
            )
            constraintSet.clear(R.id.btnNext, ConstraintSet.TOP)
        } else {
            binding.layoutAdNative.visibility = View.GONE
        }

        constraintSet.applyTo(constraintLayout)


        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Glide.with(requireActivity())
                    .asBitmap()
                    .load(R.drawable.ic_wt_1)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(true)
                    .into(binding.main)
            }
        }

        binding.tvSkip.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Onboard 1",
                trigger = "User tap skip",
                eventName = "onboard1_scr_skip"
            )
            prefHelper.let {
                it.putBoolean(key = "walkThrough", value = true)
            }
            startActivity(Intent(context, HomeActivity::class.java))
            (context as Activity).finish()
        }

        binding.btnNext.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Onboard 1",
                trigger = "User tap button Next to Onboard 2 screen",
                eventName = "onboard1_scr_tap_next"
            )
            val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
            viewPager?.currentItem = 1
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.RESUMED) {
//                /*if (!reloadAdAfterClickOrResume) {
//                    delay(200)
//                }*/
//                if (NetworkCheck.isNetworkAvailable(requireContext()) &&
//                    (requireActivity().getSharedPreferences(
//                        "RemoteConfig",
//                        AppCompatActivity.MODE_PRIVATE
//                    ).getBoolean(native_onb1_2, false) ||
//                            requireActivity().getSharedPreferences(
//                                "RemoteConfig",
//                                AppCompatActivity.MODE_PRIVATE
//                            ).getBoolean(native_onb1_0, false))
//                ) {
//                    if (reloadAdAfterClickOrResume) {
//                        AdsProvider.nativeWalkThroughOne.config(
//                            requireActivity().getSharedPreferences(
//                                "RemoteConfig",
//                                BaseActivity.MODE_PRIVATE
//                            ).getBoolean(native_onb1_2, true),
//                            requireActivity().getSharedPreferences(
//                                "RemoteConfig",
//                                BaseActivity.MODE_PRIVATE
//                            ).getBoolean(native_onb1_0, true)
//                        )
//                        AdsProvider.nativeWalkThroughOne.loadAds(MyApplication.getApplication())
//                    }
//
//                    showNativeAd(
//                        AdsProvider.nativeWalkThroughOne, binding.layoutAdNative,
//                        if (requireActivity().getSharedPreferences(
//                                "RemoteConfig",
//                                AppCompatActivity.MODE_PRIVATE
//                            ).getBoolean(native_onb1_fb, false)
//                        ) {
//                            R.layout.custom_native_ads_onboarding_fb
//                        } else {
//                            R.layout.custom_native_ads_onboarding
//                        },
//                        facebookAdLayout = R.layout.custom_native_ads_onboarding_fb
//                    )
//                }
//                cancel()
//            }
//        }
//    }
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (NetworkCheck.isNetworkAvailable(requireContext())) {
                val sharedPrefs = requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)

                // Check for native_onboard1_high first
                val isNativeOnboard1HighEnabled = sharedPrefs.getBoolean("native_onboard1_high", false)

                if (isNativeOnboard1HighEnabled) {
                    // Load the native_onboard1_high ads
                    AdsProvider.nativeWalkThroughOne.config(true, false) // Assuming true means to load this ad type
                    AdsProvider.nativeWalkThroughOne.loadAds(MyApplication.getApplication())
                } else {
                    // If native_onboard1_high is not available, then check for the fallback
                    val isNativeOnboardFullScreenHighEnabled = sharedPrefs.getBoolean("native_onboard_fullscreen_high", false)
                    if (isNativeOnboardFullScreenHighEnabled) {
                        AdsProvider.nativeWalkThroughOne.config(false, true) // Assuming false means to load this ad type
                        AdsProvider.nativeWalkThroughOne.loadAds(MyApplication.getApplication())
                    }
                }

                // Show the appropriate ad layout
                showNativeAd(
                    AdsProvider.nativeWalkThroughOne, binding.layoutAdNative,
                    if (sharedPrefs.getBoolean("native_onb1_fb", false)) {
                        R.layout.custom_native_ads_onboarding_fb
                    } else {
                        R.layout.custom_native_ads_onboarding
                    },
                    facebookAdLayout = R.layout.custom_native_ads_onboarding_fb
                )
            }
            cancel()
        }
    }
}



    override fun onResume() {
        super.onResume()
        val sharedPrefs = requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)
        val isHighPriorityEnabled = sharedPrefs.getBoolean("native_onboard1_high", false)
        val isFallbackEnabled = sharedPrefs.getBoolean("native_onb1_0", false)

        if (NetworkCheck.isNetworkAvailable(requireContext()) && (isHighPriorityEnabled || isFallbackEnabled)) {
            binding.layoutAdNative.visibility = View.VISIBLE
        } else {
            binding.layoutAdNative.visibility = View.GONE
        }
    }



//    override fun onResume() {
//        super.onResume()
//        if (NetworkCheck.isNetworkAvailable(requireContext()) &&
//            (requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)
//                .getBoolean(native_onb1_2, false) ||
//                    requireActivity().getSharedPreferences(
//                        "RemoteConfig",
//                        AppCompatActivity.MODE_PRIVATE
//                    ).getBoolean(native_onb1_0, false))
//        ) {
//            binding.layoutAdNative.visibility = View.VISIBLE
//        }
//    }

    override fun onStop() {
        super.onStop()
        Log.e("reloadAdAfterClickOrResume", "onStop: ")
        reloadAdAfterClickOrResume = true
    }
}
