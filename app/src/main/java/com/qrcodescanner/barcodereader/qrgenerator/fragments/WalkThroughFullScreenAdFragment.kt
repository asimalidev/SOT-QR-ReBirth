package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import apero.aperosg.monetization.util.showNativeAd
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig.native_onb1_f_0
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig.native_onb2_f_0
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig.native_onb2_f_2
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig.native_onb2_f_2_2
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentWalkThroughFullScreenAdBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.BaseActivity
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb_f_fb
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WalkThroughFullScreenAdFragment(val prefHelper: PrefHelper) : Fragment() {

    lateinit var binding: FragmentWalkThroughFullScreenAdBinding
    var reloadAdAfterClickOrResume = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWalkThroughFullScreenAdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!requireActivity().getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE).getBoolean(native_onb_f_fb, false)) {
            binding.ivCloseF.visibility = View.INVISIBLE
        } else {
            binding.ivCloseF.visibility = View.VISIBLE
        }

        binding.ivCloseF.setOnClickListener {
            val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
            viewPager?.currentItem = 3
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                /*if (!reloadAdAfterClickOrResume) {
                    delay(200)
                }*/


                cancel()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.nativeFr.visibility = View.VISIBLE

        if (reloadAdAfterClickOrResume) {
            AdsProvider.nativeWalkThroughFullScreen.config(
                requireActivity().getSharedPreferences("RemoteConfig", BaseActivity.MODE_PRIVATE).getBoolean(native_onb1_f_0, true),
                requireActivity().getSharedPreferences("RemoteConfig", BaseActivity.MODE_PRIVATE).getBoolean(native_onb2_f_2, true),
                requireActivity().getSharedPreferences("RemoteConfig", BaseActivity.MODE_PRIVATE).getBoolean(native_onb2_f_2_2, true),
                requireActivity().getSharedPreferences("RemoteConfig", BaseActivity.MODE_PRIVATE).getBoolean(native_onb2_f_0, true))
            AdsProvider.nativeWalkThroughFullScreen.loadAds(MyApplication.getApplication())
        }

        showNativeAd(AdsProvider.nativeWalkThroughFullScreen, binding.nativeFr, R.layout.native_custom_free_size)
    }

    override fun onStop() {
        super.onStop()
        Log.e("reloadAdAfterClickOrResume", "onStop: ")
        reloadAdAfterClickOrResume = true
    }
}