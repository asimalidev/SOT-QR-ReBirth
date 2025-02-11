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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentWalkThroughThreeBinding
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb3_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb3_2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkThroughThreeFragment(val prefHelper: PrefHelper) : Fragment() {

    lateinit var binding: FragmentWalkThroughThreeBinding
    var reloadAdAfterClickOrResume = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWalkThroughThreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomFirebaseEvents.logEvent(context = requireActivity(), screenName = "Onboard 3", trigger = "App display Onboard 3 screen", eventName = "onboard3_scr")

        val constraintLayout = view.findViewById<ConstraintLayout>(R.id.cl2)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        if (NetworkCheck.isNetworkAvailable(requireContext()) &&
            (requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb3_2, false) ||
                    requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb3_0, false))) {
            binding.layoutAdNative.visibility = View.VISIBLE
            constraintSet.connect(
                R.id.btnNext, ConstraintSet.BOTTOM,
                R.id.layoutAdNative, ConstraintSet.TOP
            )
            constraintSet.clear(R.id.btnNext, ConstraintSet.TOP)
        } else {
            binding.layoutAdNative.visibility = View.GONE
        }

        constraintSet.applyTo(constraintLayout)
        if (NetworkCheck.isNetworkAvailable(requireContext()) &&
            (requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb3_2, false) ||
                    requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb3_0, false))) {
            binding.layoutAdNative.visibility = View.VISIBLE
        }
        if (NetworkCheck.isNetworkAvailable(requireContext()) &&
            (requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb3_2, false) ||
                    requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb3_0, false))) {

//            showNativeAd(AdsProvider.nativeWalkThroughThree, binding.layoutAdNative,
//                if (requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(
//                        native_onb3_fb, false)) {
//                    R.layout.custom_native_ads_onboarding_fb
//                } else {
//                    R.layout.custom_native_ads_onboarding
//                },
//                facebookAdLayout = R.layout.custom_native_ads_onboarding_fb
//            )
        }

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Glide.with(requireActivity())
                    .asBitmap()
                    .load(R.drawable.ic_wt_3)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(true)
                    .into(binding.main)
            }
        }

        binding.tvSkip.setOnClickListener {
            prefHelper.let {
                it.putBoolean(key = "walkThrough", value = true)
            }
            startActivity(Intent(context, HomeActivity::class.java))
            (context as Activity).finish()
        }

        binding.btnNext.setOnClickListener {
            CustomFirebaseEvents.logEvent(context = requireActivity(), screenName = "Onboard 3", trigger = "User tap button Start", eventName = "onboard3_scr_tap_start")
            prefHelper.let {
                it.putBoolean(key = "walkThrough", value = true)
            }
            startActivity(Intent(context, HomeActivity::class.java))
            (context as Activity).finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                /*if (!reloadAdAfterClickOrResume) {
                    delay(200)
                }*/
                if (NetworkCheck.isNetworkAvailable(requireContext()) &&
                    (requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb3_2, false) ||
                            requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb3_0, false))) {
//                    if (reloadAdAfterClickOrResume) {
//                        AdsProvider.nativeWalkThroughThree.config(
//                            requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(
//                                native_onb3_2, true),
//                            requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(
//                                native_onb3_0, true))
//                        AdsProvider.nativeWalkThroughThree.loadAds(MyApplication.getApplication())
//                    }
                }
                cancel()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (NetworkCheck.isNetworkAvailable(requireContext()) &&
            (requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb3_2, false) ||
                    requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb3_0, false))) {
            binding.layoutAdNative.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e("reloadAdAfterClickOrResume", "onStop: ")
        reloadAdAfterClickOrResume = true
    }
}
