package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.app.FragmentManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import apero.aperosg.monetization.util.showNativeAd
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.adapters.LanguageAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.SelectLanguageAdapter
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivityLanguageSelectedBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.AppLanguages
import com.qrcodescanner.barcodereader.qrgenerator.utils.LocaleManager
import com.qrcodescanner.barcodereader.qrgenerator.utils.MyLocaleHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.Utils
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang2_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang2_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang2_fb

class LanguageSelectedActivity : AppCompatActivity() {

    lateinit var binding: ActivityLanguageSelectedBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LanguageSelectedActivity
    private var isAdLoaded = false // Default status

    var selectedLanguage: String = ""

    lateinit var prefHelper: PrefHelper

    var check: Boolean = false
    var reloadAdAfterClickOrResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        // MobileAds.openAdInspector(this){}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

        if (intent.getStringExtra("From").equals("Splash")) {
            if (reloadAdAfterClickOrResume) {
                AdsProvider.nativeLanguageTwo.config(
                    getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                        native_lang2_2,
                        true
                    ),
                    getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                        native_lang2_0,
                        true
                    )
                )
                AdsProvider.nativeLanguageTwo.loadAds(MyApplication.getApplication())
            }

            showNativeAd(
                AdsProvider.nativeLanguageTwo, binding.layoutAdNative,
                if (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                        native_lang2_fb,
                        false
                    )
                ) {
                    R.layout.custom_native_ads_language_first_fb
                } else {
                    R.layout.custom_native_ads_language_first
                },
                facebookAdLayout = R.layout.custom_native_ads_language_first_fb
            )
        }

        binding.btnSelectLanguage.visibility = View.INVISIBLE

        binding.btnSelectLanguage.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = this@LanguageSelectedActivity,
                screenName = "Language 2",
                trigger = "User tap Next button",
                eventName = "language2_scr_tap_next"
            )
            prefHelper.putBoolean("isLanguageCheck", true)
            val language = prefHelper.getStringDefault("language", "en")
            LocaleManager.setLocale(this, language ?: "en")
            if (intent.getStringExtra("From").equals("Splash")) {
                startActivity(Intent(this, WalkThroughActivity::class.java))
            }
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

            if (intent.getStringExtra("From").equals("Splash")) {
                startActivity(Intent(this, WalkThroughActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            finish()
        }

        Utils.hideStatusBar(this)
        CustomFirebaseEvents.logEvent(
            context = this@LanguageSelectedActivity,
            screenName = "Language 2",
            trigger = "App display Language 2 screen",
            eventName = "language2_scr"
        )

        check = intent.getBooleanExtra("home", false)
        prefHelper = PrefHelper(this)
        recyclerView = findViewById(R.id.recyclerViewLanguage)
        recyclerView.layoutManager = LinearLayoutManager(this)
        setRecyclerView()
    }

    private fun reloadNativeAd() {
        // Reload the native ad
        AdsProvider.nativeLanguageTwo.loadAds(this)

        // Show the native ad in the layout
        showNativeAd(
            AdsProvider.nativeLanguageTwo,
            binding.layoutAdNative,
            if (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                    native_lang2_fb,
                    false
                )
            ) {
                R.layout.custom_native_ads_language_first_fb
            } else {
                R.layout.custom_native_ads_language_first
            },
            facebookAdLayout = R.layout.custom_native_ads_language_first_fb
        )

        // Set the ad layout visibility
        binding.layoutAdNative.visibility = View.VISIBLE

        // Show a toast message
        Toast.makeText(this, "Ad reloaded successfully!", Toast.LENGTH_SHORT).show()
    }


    override fun onResume() {
        super.onResume()
        if (NetworkCheck.isNetworkAvailable(this@LanguageSelectedActivity) &&
            intent.getStringExtra("From").equals("Splash") &&
            (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_lang2_2, true) ||
                    getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                        native_lang2_0,
                        true
                    ))
        ) {
            AdsProvider.nativeLanguageTwo.loadAds(this)
            binding.layoutAdNative.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e("reloadAdAfterClickOrResume", "onStop: ")
        reloadAdAfterClickOrResume = true
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

        val adapter = SelectLanguageAdapter(languages, this) { position ->
            val selectedLanguage = languages[position]
            CustomFirebaseEvents.logEvent(
                context = this@LanguageSelectedActivity,
                screenName = "Language 2",
                trigger = "User select a language",
                eventName = "language2_scr_tap_language"
            )
            Utils.toast(this@LanguageSelectedActivity, selectedLanguage.languageCode)
            prefHelper.putString("language", selectedLanguage.languageCode)
            MyLocaleHelper.setLocale(this, selectedLanguage.languageCode)
            prefHelper.putString("languagePosition", selectedLanguage.itemPosition.toString())

            binding.btnSelectLanguage.visibility = View.VISIBLE

            // Reload the native ad
            reloadNativeAd()
        }

        recyclerView.adapter = adapter
        val languagePosition = prefHelper.getStringDefault("languagePosition", "-1")
        if (languagePosition != "-1") {
            binding.btnSelectLanguage.visibility = View.VISIBLE
        }
    }
}