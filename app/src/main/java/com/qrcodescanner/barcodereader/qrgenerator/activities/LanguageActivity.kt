package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import apero.aperosg.monetization.util.showBannerAd
import apero.aperosg.monetization.util.showNativeAd
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R

import com.qrcodescanner.barcodereader.qrgenerator.adapters.LanguageAdapter
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivityLanguageBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.AppLanguages
import com.qrcodescanner.barcodereader.qrgenerator.utils.BaseActivity
import com.qrcodescanner.barcodereader.qrgenerator.utils.LocaleManager
import com.qrcodescanner.barcodereader.qrgenerator.utils.MyLocaleHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.Utils
import com.qrcodescanner.barcodereader.qrgenerator.utils.banner
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang1_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang1_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang1_fb
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang2_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang2_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb1_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb1_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb_0_f
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb_2_f
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LanguageActivity : BaseActivity() {
    lateinit var binding : ActivityLanguageBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LanguageAdapter
    var selectedLanguage: String = ""
    private lateinit var navController: NavController

    lateinit var prefHelper: PrefHelper
    var reloadAdAfterClickOrResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        prefHelper = PrefHelper(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                // Hide only the navigation bar
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Fallback for devices with Android 9 and below
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        binding.btnback.setOnClickListener {
            if (intent.getStringExtra("From") == "Settings") {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("NavigateTo", "NavSettingFragment")
                intent.putExtra("ShowTopLayer", true) // Pass boolean to show the top layer
                startActivity(intent)
            }
        }


        binding.btnSelectLanguage.setOnClickListener {
            if (intent.getStringExtra("From").equals("Settings")) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                overridePendingTransition(0, 0)
            }
        }

        Utils.hideStatusBar(this)

        prefHelper = PrefHelper(this)
        recyclerView = findViewById(R.id.recyclerViewLanguage)
        recyclerView.layoutManager = LinearLayoutManager(this)
        setRecyclerView()

        AdsProvider.nativeLanguageOne.config(
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                native_lang1_2, true),
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                native_lang1_0, true))
        showNativeAd(
            AdsProvider.nativeLanguageOne,
            binding.layoutAdNative, if (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_lang1_fb, false)) {
                R.layout.custom_native_ads_language_first_fb
            } else {
                R.layout.custom_native_ads_language_first
            },
            facebookAdLayout = R.layout.custom_native_ads_language_first_fb
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                delay(200)
                CustomFirebaseEvents.logEvent(context = this@LanguageActivity, screenName = "Language 1", trigger = "App display Language screen", eventName = "language1_scr")
                if (intent.getStringExtra("From").equals("Splash")) {
                    Log.e("reloadAdAfterClickOrResume", ":"+reloadAdAfterClickOrResume)
                    binding.btnSelectLanguage.visibility = View.INVISIBLE

                    if (!prefHelper.getBoolean("isLanguageCheck")) {
                        AdsProvider.nativeLanguageTwo.config(
                            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_lang2_2, true),
                            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_lang2_0, true))
                        AdsProvider.nativeLanguageTwo.loadAds(MyApplication.getApplication())

                        AdsProvider.nativeWalkThroughOne.config(
                            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_onb1_2, true),
                            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_onb1_0, true))
                        AdsProvider.nativeWalkThroughOne.loadAds(MyApplication.getApplication())

                        AdsProvider.nativeWalkThroughFullScreen.config(
                            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                                native_onb_2_f, true),
                            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                                native_onb_0_f, true))
                        AdsProvider.nativeWalkThroughFullScreen.loadAds(MyApplication.getApplication())
                    }
                }
                cancel()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        if (NetworkCheck.isNetworkAvailable(this@LanguageActivity) &&
            intent.getStringExtra("From").equals("Splash") &&
            (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_lang1_2, true) ||
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_lang1_0, true))) {
            AdsProvider.nativeLanguageOne.loadAds(MyApplication.getApplication())
            binding.layoutAdNative.visibility = View.VISIBLE
        }

        if (intent.getStringExtra("From").equals("Settings")) {
            binding.btnSelectLanguage.visibility = View.VISIBLE
            AdsProvider.bannerAll.config(getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(banner, true))
            AdsProvider.bannerAll.loadAds(MyApplication.getApplication())
            showBannerAd(AdsProvider.bannerAll, binding.bannerFr)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e("reloadAdAfterClickOrResume", "onStop: ")
        reloadAdAfterClickOrResume = true
    }

    private fun setRecyclerView() {
        val languages = listOf(
            AppLanguages("English", resources.getDrawable(R.drawable.ic_uk), 0, "en",resources.getDrawable(R.drawable.unselect_radio)),
            AppLanguages("Hindi", resources.getDrawable(R.drawable.ic_hindi), 1, "hi",resources.getDrawable(R.drawable.unselect_radio)),
            AppLanguages("Spanish", resources.getDrawable(R.drawable.ic_spain), 2, "es",resources.getDrawable(R.drawable.unselect_radio)),
            AppLanguages("French", resources.getDrawable(R.drawable.ic_french), 3, "fr",resources.getDrawable(R.drawable.unselect_radio)),
            AppLanguages("Portuguese", resources.getDrawable(R.drawable.ic_portugese), 4, "pt",resources.getDrawable(R.drawable.unselect_radio)),
            AppLanguages("German", resources.getDrawable(R.drawable.ic_german), 5, "de",resources.getDrawable(R.drawable.unselect_radio)),
            AppLanguages("Arabic", resources.getDrawable(R.drawable.ic_arabic), 6, "ar",resources.getDrawable(R.drawable.unselect_radio))
        )

        adapter = LanguageAdapter(languages, this) { position ->
            CustomFirebaseEvents.logEvent(context = this@LanguageActivity, screenName = "Language 1", trigger = "User select a language", eventName = "language1_scr_tap_language")
            selectedLanguage = languages[position].name
            println("Selected language: ${selectedLanguage}")
            prefHelper.putString("language", languages[position].languageCode)
            MyLocaleHelper.setLocale(this, languages[position].languageCode)
            prefHelper.putString("languagePosition", languages[position].itemPosition.toString())

            val language = prefHelper.getStringDefault("language", "en")
            LocaleManager.setLocale(this@LanguageActivity, language ?: "en")
            if (intent.getStringExtra("From").equals("Splash")) {
                val options = ActivityOptionsCompat.makeCustomAnimation(this@LanguageActivity, 0, 0)
                startActivity(
                    Intent(this@LanguageActivity, LanguageSelectedActivity::class.java)
                        .putExtra("From", intent.getStringExtra("From")),
                    options.toBundle())
                finish()
                overridePendingTransition(0, 0)
            }
        }

        recyclerView.adapter = adapter

        // Check if a language is already selected
        /*val languagePosition = prefHelper.getStringDefault("languagePosition", "-1")
        if (languagePosition != "-1") {
            binding.btnSelectLanguage.visibility = View.VISIBLE
        }*/
    }
}

