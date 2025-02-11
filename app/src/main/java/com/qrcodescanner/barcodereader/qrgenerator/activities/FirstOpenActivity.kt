package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import apero.aperosg.firstopen.app.AperoFO
import apero.aperosg.firstopen.app.AperoFOAdsConfig
import apero.aperosg.firstopen.app.AperoFOCallback
import apero.aperosg.firstopen.app.AperoFOConfig
import apero.aperosg.firstopen.app.AperoLanguageUiConfig
import apero.aperosg.firstopen.app.AperoOnboardPageConfig
import apero.aperosg.firstopen.app.AperoOnboardUiConfig
import apero.aperosg.firstopen.app.AperoSplashUiConfig
import apero.aperosg.firstopen.app.AperoWelcomeUiConfig
import apero.aperosg.firstopen.model.Language
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.stickynotification.StickyNotification
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
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_welcome
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_welcome_dup
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_welcome_dup_high
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_welcome_high
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class FirstOpenActivity : AppCompatActivity() {
    lateinit var prefHelper: PrefHelper
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private var isChecked = false
    private var isFirstTime: Boolean = true
    private var isNotificationEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        prefHelper = PrefHelper(this)
        setupFirstOpenFlow()
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
//        isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)
        isNotificationEnabled = getSavedPermissionState("notificationPermission")
        Log.e("notificationee","isAllowed : $isNotificationEnabled")

        // Retrieve individual permission states

    }


    private fun setupFirstOpenFlow() {
        val callback = object : AperoFOCallback() {
            override fun onConsentResult(canLoadAds: Boolean) {
                // Do something if user consent/doesn't consent
            }

            override fun onLanguageConfirm(language: Language) {

            }

            override fun onFinished() {
                // Show the sticky notification
                if (isNotificationEnabled)
                    StickyNotification.showNotification(this@FirstOpenActivity)

                // Retrieve the action passed from the notification
                val action = intent.getStringExtra("action")

                // Now, launch HomeActivity with the action
                val homeIntent = Intent(this@FirstOpenActivity, HomeActivity::class.java).apply {
                    putExtra("action", action)  // Pass the action to HomeActivity
                }
                startActivity(homeIntent)
                finish() // Finish FirstOpenActivity to prevent users from coming back here
            }
        }


        val adsConfig = AperoFOAdsConfig.Builder()
            .setInterSplashHighId(BuildConfig.inter_spl_2)
            .setInterSplashId(BuildConfig.inter_spl_0)
            .setBannerSplashId(BuildConfig.banner_spl_0)

            //For Welcome Screens
            .setNativeWelcomeHighId(BuildConfig.native_wel_2)
            .setNativeWelcomeId(BuildConfig.native_wel_0)
            .setNativeWelcomeDupHighId(BuildConfig.native_wel_w_2)
            .setNativeWelcomeDupMediumId(BuildConfig.native_wel_w_2_2)
            .setNativeWelcomeDupId(BuildConfig.native_wel_w_0)


            .setNativeLanguageHighId(BuildConfig.native_lang1_2)
            .setNativeLanguageId(BuildConfig.native_lang1_0)
            .setNativeLanguageDupHighId(BuildConfig.native_lang2_2)
            .setNativeLanguageDupMediumId(BuildConfig.native_lang2_2_2)
            .setNativeLanguageDupId(BuildConfig.native_lang2_0)

            .setNativeOnboardFullscreenHighId(BuildConfig.native_onb1_f_2)
            .setNativeOnboardFullscreenId(BuildConfig.native_onb1_f_0)
            .setNativeOnboardFullscreen2HighId(BuildConfig.native_onb2_f_2)
            .setNativeOnboardFullscreen2MediumId(BuildConfig.native_onb2_f_2_2)
            .setNativeOnboardFullscreen2Id(BuildConfig.native_onb2_f_0)
            .setNativeOnboard1HighId(BuildConfig.native_onb1_2)
            .setNativeOnboard1Id(BuildConfig.native_onb1_0)
            .build()

        val splashConfig = AperoSplashUiConfig.Builder()
            .setCustomSplashLayoutId(R.layout.activity_first_open)
            .setWaitForInitialization(true)
            .build()

        val languageConfig = AperoLanguageUiConfig.Builder()
            .setLanguages(getLanguageList())
            .build()

        val welcomeConfig = AperoWelcomeUiConfig.Builder()
            .setViewContentProvider { setUpWelcomeScreen() }
            .build()

        // Config for onboard screen 1
        val onboard1Config =
            AperoOnboardPageConfig(layoutOnboardContentId = R.layout.fragment_wt_new_one)
        // Config for onboard screen 2
        val onboard2Config =
            AperoOnboardPageConfig(layoutOnboardContentId = R.layout.fragment_wt_new_two)
        // Config for onboard screen 3
        val onboard3Config =
            AperoOnboardPageConfig(layoutOnboardContentId = R.layout.fragment_wt_new_three)

        // Combine config for onboard screens
        val onboardConfig =
            AperoOnboardUiConfig(pages = listOf(onboard1Config, onboard2Config, onboard3Config))




        val config = AperoFOConfig.Builder()
            .setCallback(callback)
            .setAdsConfig(adsConfig)

            .setSplashUiConfig(splashConfig)
            .setLanguageUiConfig(languageConfig)
            .setWelcomeUiConfig(welcomeConfig)
            .setOnboardUiConfig(onboardConfig)
            .setCustomNativeOnboardLayoutId(R.layout.custom_native_ads_onboarding)
            .build()

        AperoFO.startFlow(this, config)

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                fetchAdIDS()
            }
        }
    }

    private fun getSavedPermissionState(permissionKey: String): Boolean {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val state = sharedPreferences.getBoolean(permissionKey, false)
        Log.d("PermissionState", "Retrieved $permissionKey: $state")
        return state
    }

    private fun getLanguageList(): List<Language> {
        val supportedLanguages = listOf(
            Language.Arabic, Language.German, Language.English,
            Language.Spanish, Language.French, Language.Hindi,
            Language.Japanese, Language.Korean, Language.Portuguese
        )

        val deviceLanguageCode = Locale.getDefault().language

        val isDeviceLanguageSupported = supportedLanguages.any { it.code == deviceLanguageCode }
        return if (isDeviceLanguageSupported) {
            listOf(supportedLanguages.first { it.code == deviceLanguageCode }) +
                    supportedLanguages.filter { it.code != deviceLanguageCode }
        } else {
            listOf(Language.English) + supportedLanguages.filter { it != Language.English }
        }
    }

    private fun fetchAdIDS() {
        if (NetworkCheck.isNetworkAvailable(this@FirstOpenActivity)) {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings =
                FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build()
            mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
            mFirebaseRemoteConfig!!.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mFirebaseRemoteConfig!!.activate()
                    saveAllValues()
                    AperoFO.finishSplashInitialization()
                } else {
                    AperoFO.finishSplashInitialization()

                }
            }
        } else {
            AperoFO.finishSplashInitialization()
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
            Log.e("AdStatus", "FirstOpen: " + mFirebaseRemoteConfig!!.getBoolean(native_home))
        }


        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_welcome).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
                .putBoolean(native_welcome, mFirebaseRemoteConfig!!.getBoolean(native_welcome))
                .apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_welcome_high).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit().putBoolean(
                native_welcome_high,
                mFirebaseRemoteConfig!!.getBoolean(native_welcome_high)
            ).apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_welcome_dup).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit().putBoolean(
                native_welcome_dup,
                mFirebaseRemoteConfig!!.getBoolean(native_welcome_dup)
            ).apply()
        }
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(native_welcome_dup_high).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit().putBoolean(
                native_welcome_dup_high,
                mFirebaseRemoteConfig!!.getBoolean(native_welcome_dup_high)
            ).apply()
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

    private fun setUpWelcomeScreen(): View {
        CustomFirebaseEvents.logEvent(
            context = this,
            screenName = "",
            trigger = "",
            eventName = "welcome1_scr"
        )
        val localizedConfig = resources.configuration.apply { setLocale(Locale.getDefault()) }
        val localizedContext = ContextWrapper(this).createConfigurationContext(localizedConfig)

        val welcomeScreenView = LayoutInflater.from(localizedContext)
            .inflate(R.layout.layout_welcome_scr_1, null, false)
        val progressAnim = welcomeScreenView.findViewById<LottieAnimationView>(R.id.progress)
        val txtScanQRCode = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtScanQRCode)
        val txtScanBarCode = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtScanBarCode)
        val txtCreateQRCode =
            welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtCreateQRCode)
        val txtCreateBarCode =
            welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtCreateBarCode)
        val txtCreatePDF = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtCreatePDF)
        val txtTranslateImage =
            welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtTranslateImage)
        val txtSearchImage = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtSearchImage)
        val txtSearchProduct =
            welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtSearchProduct)

        var txtScanQRCodeBool = false
        var txtScanBarCodeBool = false
        var txtCreateQRCodeBool = false
        var txtCreateBarCodeBool = false
        var txtCreatePDFBool = false
        var txtTranslateImageBool = false
        var txtSearchImageBool = false
        var txtSearchProductBool = false

        val nextButton = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtNext)
//        txtScanQRCode.setBackgroundResource(R.drawable.btn_bg_feature_green)

        txtScanQRCode.setOnClickListener {
            isChecked = true
            progressAnim.visibility = View.GONE
            AperoFO.showWelcomeDupScreen()
            CustomFirebaseEvents.logEvent(
                context = this,
                screenName = "",
                trigger = "",
                eventName = "welcome_scr_check_scan_qr"
            )
            if (txtScanQRCodeBool) {
                txtScanQRCodeBool = false
                txtScanQRCode.setBackgroundResource(R.drawable.btn_bg_feature_off)
            } else {
                txtScanQRCodeBool = true
                txtScanQRCode.setBackgroundResource(R.drawable.btn_bg_feature_on)
            }
        }
        txtScanBarCode.setOnClickListener {
            isChecked = true
            progressAnim.visibility = View.GONE
            AperoFO.showWelcomeDupScreen()
            CustomFirebaseEvents.logEvent(
                context = this,
                screenName = "",
                trigger = "",
                eventName = "welcome_scr_check_scan_barcode"
            )
            if (txtScanBarCodeBool) {
                txtScanBarCodeBool = false
                txtScanBarCode.setBackgroundResource(R.drawable.btn_bg_feature_off)
            } else {
                txtScanBarCodeBool = true
                txtScanBarCode.setBackgroundResource(R.drawable.btn_bg_feature_on)
            }
        }
        txtCreateQRCode.setOnClickListener {
            isChecked = true
            progressAnim.visibility = View.GONE
            AperoFO.showWelcomeDupScreen()
            CustomFirebaseEvents.logEvent(
                context = this,
                screenName = "",
                trigger = "",
                eventName = "welcome_scr_check_create_qr"
            )
            if (txtCreateQRCodeBool) {
                txtCreateQRCodeBool = false
                txtCreateQRCode.setBackgroundResource(R.drawable.btn_bg_feature_off)
            } else {
                txtCreateQRCodeBool = true
                txtCreateQRCode.setBackgroundResource(R.drawable.btn_bg_feature_on)
            }
        }
        txtCreateBarCode.setOnClickListener {
            isChecked = true
            progressAnim.visibility = View.GONE
            AperoFO.showWelcomeDupScreen()
            CustomFirebaseEvents.logEvent(
                context = this,
                screenName = "",
                trigger = "",
                eventName = "welcome_scr_check_create_barcode"
            )

            if (txtCreateBarCodeBool) {
                txtCreateBarCodeBool = false
                txtCreateBarCode.setBackgroundResource(R.drawable.btn_bg_feature_off)
            } else {
                txtCreateBarCodeBool = true
                txtCreateBarCode.setBackgroundResource(R.drawable.btn_bg_feature_on)
            }
        }
        txtSearchImage.setOnClickListener {
            isChecked = true
            progressAnim.visibility = View.GONE
            AperoFO.showWelcomeDupScreen()
            CustomFirebaseEvents.logEvent(
                context = this,
                screenName = "",
                trigger = "",
                eventName = "welcome_scr_check_create_pdf"
            )
            if (txtSearchImageBool) {
                txtSearchImageBool = false
                txtSearchImage.setBackgroundResource(R.drawable.btn_bg_feature_off)
            } else {
                txtSearchImageBool = true
                txtSearchImage.setBackgroundResource(R.drawable.btn_bg_feature_on)
            }
        }
        txtCreatePDF.setOnClickListener {
            isChecked = true
            progressAnim.visibility = View.GONE
            AperoFO.showWelcomeDupScreen()
            CustomFirebaseEvents.logEvent(
                context = this,
                screenName = "",
                trigger = "",
                eventName = "welcome_scr_check_translate_image"
            )
            if (txtCreatePDFBool) {
                txtCreatePDFBool = false
                txtCreatePDF.setBackgroundResource(R.drawable.btn_bg_feature_off)
            } else {
                txtCreatePDFBool = true
                txtCreatePDF.setBackgroundResource(R.drawable.btn_bg_feature_on)
            }
        }
        txtTranslateImage.setOnClickListener {
            isChecked = true
            progressAnim.visibility = View.GONE
            AperoFO.showWelcomeDupScreen()
            CustomFirebaseEvents.logEvent(
                context = this,
                screenName = "",
                trigger = "",
                eventName = "welcome_scr_check_search_image"
            )
            if (txtTranslateImageBool) {
                txtTranslateImageBool = false
                txtTranslateImage.setBackgroundResource(R.drawable.btn_bg_feature_off)
            } else {
                txtTranslateImageBool = true
                txtTranslateImage.setBackgroundResource(R.drawable.btn_bg_feature_on)
            }
        }
        txtSearchProduct.setOnClickListener {
            isChecked = true
            progressAnim.visibility = View.GONE
            AperoFO.showWelcomeDupScreen()
            CustomFirebaseEvents.logEvent(
                context = this,
                screenName = "",
                trigger = "",
                eventName = "welcome1_scr_check_search_product"
            )
            if (txtSearchProductBool) {
                txtSearchProductBool = false
                txtSearchProduct.setBackgroundResource(R.drawable.btn_bg_feature_off)
            } else {
                txtSearchProductBool = true
                txtSearchProduct.setBackgroundResource(R.drawable.btn_bg_feature_on)
            }
        }

        nextButton.setOnClickListener {
            if (txtScanQRCodeBool || txtScanBarCodeBool ||
                txtCreateQRCodeBool || txtCreateBarCodeBool ||
                txtCreatePDFBool || txtTranslateImageBool ||
                txtSearchImageBool || txtSearchProductBool
            ) {
                CustomFirebaseEvents.logEvent(
                    context = this,
                    screenName = "",
                    trigger = "",
                    eventName = "welcome2_scr_tap_continue"
                )
                AperoFO.completeWelcomeScreen()
            } else {
                AperoFO.showWelcomeDupScreen()
                CustomFirebaseEvents.logEvent(
                    context = this,
                    screenName = "",
                    trigger = "",
                    eventName = "welcome1_scr_tap_continue"
                )
                val toast =
                    Toast.makeText(this, R.string.please_check_the_checkbox, Toast.LENGTH_SHORT)
                /*toast.view?.let {
                    it.setBackgroundColor(getColor(R.color.black))  // Ensure view is not null before setting the color
                }*/
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
        return welcomeScreenView
    }
}