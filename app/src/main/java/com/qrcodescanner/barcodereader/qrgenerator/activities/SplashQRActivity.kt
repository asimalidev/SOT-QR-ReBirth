package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import apero.aperosg.monetization.screenflow.registerSplashAdsListener
import com.ads.control.admob.AdsConsentManager
import com.ads.control.ads.AperoAd
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivitySplashQrBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.BaseActivity
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.banner
import com.qrcodescanner.barcodereader.qrgenerator.utils.banner_spl
import com.qrcodescanner.barcodereader.qrgenerator.utils.inter_create
import com.qrcodescanner.barcodereader.qrgenerator.utils.inter_scan
import com.qrcodescanner.barcodereader.qrgenerator.utils.inter_spl_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.inter_spl_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_create
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_home
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang1_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang1_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang1_fb
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang2_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang2_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_lang2_fb
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb1_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb1_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb1_fb
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb3_0
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb3_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb3_fb
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb_0_f
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb_2_f
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_onb_f_fb
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SplashQRActivity : BaseActivity() {
    lateinit var binding: ActivitySplashQrBinding
    lateinit var prefHelper: PrefHelper
    var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashQrBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                // Hide only the navigation bar
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Fallback for devices with Android 9 and below
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

        prefHelper = PrefHelper(this)

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                fetchAdIDS()
            }
        }

        updateLocale(
            this,
            getSharedPreferences("flashpref", Context.MODE_PRIVATE).getString("language", "en")
                ?: "en"
        )
        supportActionBar?.hide()

        CustomFirebaseEvents.logEvent(
            context = this,
            screenName = "Splash",
            trigger = "App display Splash screen",
            eventName = "splash_scr"
        )
    }

    private fun fetchAdIDS() {
        if (NetworkCheck.isNetworkAvailable(this@SplashQRActivity)) {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings =
                FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build()
            mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
            mFirebaseRemoteConfig!!.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mFirebaseRemoteConfig!!.activate()
                    saveAllValues()
                    requestUMP()
                } else {
                    requestUMP()
                }
            }
        } else {
            requestUMP()
        }
    }

    private fun saveAllValues() {
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(banner).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(banner, mFirebaseRemoteConfig!!.getBoolean(banner)).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(banner_spl).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(banner_spl, mFirebaseRemoteConfig!!.getBoolean(banner_spl)).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(inter_create).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(inter_create, mFirebaseRemoteConfig!!.getBoolean(inter_create)).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(inter_scan).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(inter_scan, mFirebaseRemoteConfig!!.getBoolean(inter_scan)).apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(inter_spl_2).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(inter_spl_2, mFirebaseRemoteConfig!!.getBoolean(inter_spl_2)).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(inter_spl_0).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(inter_spl_0, mFirebaseRemoteConfig!!.getBoolean(inter_spl_0)).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_create).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_create, mFirebaseRemoteConfig!!.getBoolean(native_create))
                .apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_home).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_home, mFirebaseRemoteConfig!!.getBoolean(native_home)).apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_lang1_2).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_lang1_2, mFirebaseRemoteConfig!!.getBoolean(native_lang1_2))
                .apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_lang1_0).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_lang1_0, mFirebaseRemoteConfig!!.getBoolean(native_lang1_0))
                .apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_lang2_2).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_lang2_2, mFirebaseRemoteConfig!!.getBoolean(native_lang2_2))
                .apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_lang2_0).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_lang2_0, mFirebaseRemoteConfig!!.getBoolean(native_lang2_0))
                .apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_onb1_2).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_onb1_2, mFirebaseRemoteConfig!!.getBoolean(native_onb1_2))
                .apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_onb1_0).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_onb1_0, mFirebaseRemoteConfig!!.getBoolean(native_onb1_0))
                .apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_onb_2_f).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_onb_2_f, mFirebaseRemoteConfig!!.getBoolean(native_onb_2_f))
                .apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_onb_0_f).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_onb_0_f, mFirebaseRemoteConfig!!.getBoolean(native_onb_0_f))
                .apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_onb3_2).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_onb3_2, mFirebaseRemoteConfig!!.getBoolean(native_onb3_2))
                .apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_onb3_0).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_onb3_0, mFirebaseRemoteConfig!!.getBoolean(native_onb3_0))
                .apply()
        }

        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_result).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_result, mFirebaseRemoteConfig!!.getBoolean(native_result))
                .apply()
        }

        // For Meta
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_lang1_fb).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_lang1_fb, mFirebaseRemoteConfig!!.getBoolean(native_lang1_fb))
                .apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_lang2_fb).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_lang2_fb, mFirebaseRemoteConfig!!.getBoolean(native_lang2_fb))
                .apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_onb1_fb).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_onb1_fb, mFirebaseRemoteConfig!!.getBoolean(native_onb1_fb))
                .apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_onb3_fb).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_onb3_fb, mFirebaseRemoteConfig!!.getBoolean(native_onb3_fb))
                .apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_onb_f_fb).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_onb_f_fb, mFirebaseRemoteConfig!!.getBoolean(native_onb_f_fb))
                .apply()
        }
    }

    private fun requestUMP() {
        val adsConsentManager = AdsConsentManager(this)
        adsConsentManager.requestUMP(
            true,
            "84C3994693FB491110A5A4AEF8C5561B",
            false
        ) { canRequestAds ->
            if (canRequestAds) {
                AperoAd.getInstance().initAdsNetwork()

                AdsProvider.bannerSplash.config(
                    getSharedPreferences(
                        "RemoteConfig",
                        MODE_PRIVATE
                    ).getBoolean(banner_spl, true)
                )

                AdsProvider.interSplash.config(
                    getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                        inter_spl_2,
                        true
                    ),
                    getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(inter_spl_0, true)
                )

//                AdsProvider.interSplash.loadAds(this)
//                AdsProvider.bannerSplash.loadAds(this)
                if (!prefHelper.getBoolean("isLanguageCheck")) {
                    AdsProvider.nativeLanguageOne.config(
                        getSharedPreferences(
                            "RemoteConfig",
                            MODE_PRIVATE
                        ).getBoolean(native_lang1_2, true),
                        getSharedPreferences(
                            "RemoteConfig",
                            MODE_PRIVATE
                        ).getBoolean(native_lang1_0, true)
                    )
                    AdsProvider.nativeLanguageOne.loadAds(MyApplication.getApplication())
                }
//                showBannerAd(AdsProvider.bannerSplash, binding.flAdsBanner)

                if (prefHelper.getBoolean("isLanguageCheck") && !prefHelper.getBoolean("walkThrough")) {
                    loadAdsOfWalkThrough()
                }

                registerSplashAdsListener(
                    interSplash = AdsProvider.interSplash,
                    bannerSplash = AdsProvider.bannerSplash,
                    onAdShowed = {
                        GlobalScope.launch {
                            delay(6000)
                            AdsProvider.nativeLanguageOne.loadAds(MyApplication.getApplication())
                        }
                    },
                    onNextAction = {
                        gotoMainActivity()
                    }
                )
            } else {
                gotoMainActivity()
            }
        }
    }

    private fun gotoMainActivity() {
        val intent = if (!prefHelper.getBoolean("isLanguageCheck")) {
            Intent(this@SplashQRActivity, LanguageActivity::class.java).putExtra("From", "Splash")
        } else if (!prefHelper.getBoolean("walkThrough")) {
            Intent(this@SplashQRActivity, WalkThroughActivity::class.java)
        } else {
            Intent(this@SplashQRActivity, HomeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun loadAdsOfWalkThrough() {
        AdsProvider.nativeWalkThroughOne.config(
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_onb1_2, true),
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(native_onb1_0, true)
        )
        AdsProvider.nativeWalkThroughOne.loadAds(MyApplication.getApplication())

        AdsProvider.nativeWalkThroughFullScreen.config(
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                native_onb_2_f, true
            ),
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                native_onb_0_f, true
            )
        )
        AdsProvider.nativeWalkThroughFullScreen.loadAds(MyApplication.getApplication())
    }

    private fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}