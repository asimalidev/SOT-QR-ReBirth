package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import apero.aperosg.monetization.util.showBannerAd
import apero.aperosg.monetization.util.showNativeAd
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.LanguageSelectedActivity

import com.qrcodescanner.barcodereader.qrgenerator.adapters.LanguageAdapter
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentLanguageBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.AppLanguages
import com.qrcodescanner.barcodereader.qrgenerator.utils.LocaleManager
import com.qrcodescanner.barcodereader.qrgenerator.utils.MyLocaleHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.Utils
import com.qrcodescanner.barcodereader.qrgenerator.utils.banner
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang1_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang1_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang1_fb
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class LanguageFragment : Fragment() {

    private lateinit var binding: FragmentLanguageBinding // Change to Fragment binding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LanguageAdapter
    private var selectedLanguage: String = ""
    private lateinit var prefHelper: PrefHelper
    private var reloadAdAfterClickOrResume = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = requireActivity().findViewById<View>(R.id.inclToolBar)
        toolbar.visibility = View.GONE
        // Initialize prefHelper
        prefHelper = PrefHelper(requireContext())

        // Handle system UI visibility in your hosting Activity if needed
        setupSystemUi()

        // Set up the back button click listener
        binding.btnback.setOnClickListener {
            val action=LanguageFragmentDirections.actionNavLanguageToNavSettings()
            findNavController().navigate(action)
        }

        // Handle language selection button
        binding.btnSelectLanguage.setOnClickListener {
            if (requireActivity().intent.getStringExtra("From") == "Settings") {
                findNavController().popBackStack() // Navigate back to the previous fragment
            }
        }

        Utils.hideStatusBar(requireActivity()) // Utility function to hide the status bar

        // Set up RecyclerView and Ad logic
        recyclerView = binding.recyclerViewLanguage
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        setRecyclerView()

        // Ad and other logic
        AdsProvider.nativeLanguageOne.config(
            requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getBoolean(native_lang1_2, true),
            requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getBoolean(native_lang1_0, true)
        )
        showNativeAd(
            AdsProvider.nativeLanguageOne,
            binding.layoutAdNative,
            if (requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getBoolean(native_lang1_fb, false)
            ) {
                R.layout.custom_native_ads_language_first_fb
            } else {
                R.layout.custom_native_ads_language_first
            },
            facebookAdLayout = R.layout.custom_native_ads_language_first_fb
        )

        // Handle Lifecycle Scope for Firebase Events
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                delay(200)
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Language 1",
                    trigger = "App display Language screen",
                    eventName = "language1_scr"
                )
                cancel() // Stop further repeats
            }
        }
    }

    private fun setupSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(false)
            val controller = requireActivity().window.insetsController
            controller?.hide(WindowInsets.Type.navigationBars())
            controller?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            requireActivity().window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun setRecyclerView() {
        val languages = listOf(
            AppLanguages(
                "English",
                resources.getDrawable(R.drawable.ic_uk),
                0,
                "en",
                resources.getDrawable(R.drawable.unselect_radio)
            ),
            AppLanguages(
                "Hindi",
                resources.getDrawable(R.drawable.ic_hindi),
                1,
                "hi",
                resources.getDrawable(R.drawable.unselect_radio)
            ),
            AppLanguages(
                "Spanish",
                resources.getDrawable(R.drawable.ic_spain),
                2,
                "es",
                resources.getDrawable(R.drawable.unselect_radio)
            ),
            AppLanguages(
                "French",
                resources.getDrawable(R.drawable.ic_french),
                3,
                "fr",
                resources.getDrawable(R.drawable.unselect_radio)
            ),
            AppLanguages(
                "Portuguese",
                resources.getDrawable(R.drawable.ic_portugese),
                4,
                "pt",
                resources.getDrawable(R.drawable.unselect_radio)
            ),
            AppLanguages(
                "German",
                resources.getDrawable(R.drawable.ic_german),
                5,
                "de",
                resources.getDrawable(R.drawable.unselect_radio)
            ),
            AppLanguages(
                "Arabic",
                resources.getDrawable(R.drawable.ic_arabic),
                6,
                "ar",
                resources.getDrawable(R.drawable.unselect_radio)
            )
        )

        adapter = LanguageAdapter(languages, requireContext()) { position ->
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Language 1",
                trigger = "User select a language",
                eventName = "language1_scr_tap_language"
            )
            selectedLanguage = languages[position].name
            prefHelper.putString("language", languages[position].languageCode)
            MyLocaleHelper.setLocale(requireContext(), languages[position].languageCode)
            prefHelper.putString("languagePosition", languages[position].itemPosition.toString())

            val language = prefHelper.getStringDefault("language", "en")
            LocaleManager.setLocale(requireContext(), language ?: "en")

            // Handle navigation after language selection
            if (requireActivity().intent.getStringExtra("From") == "Splash") {
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    requireContext(),
                    0,
                    0
                )
                startActivity(
                    Intent(requireContext(), LanguageSelectedActivity::class.java)
                        .putExtra("From", requireActivity().intent.getStringExtra("From")),
                    options.toBundle()
                )
                requireActivity().finish()
            }
        }

        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (NetworkCheck.isNetworkAvailable(requireContext()) &&
            requireActivity().intent.getStringExtra("From") == "Splash" &&
            (requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                .getBoolean(native_lang1_2, true) ||
                    requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                        .getBoolean(native_lang1_0, true))
        ) {
            AdsProvider.nativeLanguageOne.loadAds(MyApplication.getApplication())
            binding.layoutAdNative.visibility = View.VISIBLE
        }

        if (requireActivity().intent.getStringExtra("From") == "Settings") {
            binding.btnSelectLanguage.visibility = View.VISIBLE
            AdsProvider.bannerAll.config(
                requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getBoolean(banner, true)
            )
            AdsProvider.bannerAll.loadAds(MyApplication.getApplication())
            showBannerAd(AdsProvider.bannerAll, binding.bannerFr)
        }
    }

    override fun onStop() {
        super.onStop()
        reloadAdAfterClickOrResume = true
    }
}
