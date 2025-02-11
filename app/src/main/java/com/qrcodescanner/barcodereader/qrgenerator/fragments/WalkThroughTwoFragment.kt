package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentWalkThroughTwoBinding
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkThroughTwoFragment(val prefHelper: PrefHelper) : Fragment() {

    lateinit var binding: FragmentWalkThroughTwoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWalkThroughTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomFirebaseEvents.logEvent(context = requireActivity(), screenName = "Onboard 2", trigger = "App display Onboard 2 screen", eventName = "onboard2_scr")

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Glide.with(requireActivity())
                    .asBitmap()
                    .load(R.drawable.ic_wt_2)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(true)
                    .into(binding.main)
            }
        }
        if (NetworkCheck.isNetworkAvailable(requireContext())){
            val params = binding.btnNext.layoutParams as ConstraintLayout.LayoutParams

            // Remove the bottom constraint
            params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET

            // Add padding top of 110dp
            params.topMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 110f, resources.displayMetrics
            ).toInt()

            // Apply the new layout parameters
            binding.btnNext.layoutParams = params

        }

        binding.tvSkip.setOnClickListener {
            CustomFirebaseEvents.logEvent(context = requireActivity(), screenName = "Onboard 2", trigger = "User tap skip", eventName = "onboard2_scr_skip")
            prefHelper.putBoolean(key = "walkThrough", value = true)
            startActivity(Intent(context, HomeActivity::class.java))
            (context as Activity).finish()
        }

        binding.btnNext.setOnClickListener {
            CustomFirebaseEvents.logEvent(context = requireActivity(), screenName = "Onboard 2", trigger = "User tap button Next to Onboard 3 screen", eventName = "onboard2_scr_tap_next")
            val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
            viewPager?.currentItem = 2
        }
    }
}
