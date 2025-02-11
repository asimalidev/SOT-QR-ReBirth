package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import apero.aperosg.monetization.util.showBannerAd
import com.google.android.material.textfield.TextInputEditText
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivityTextTranslatorBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.qrcodescanner.barcodereader.qrgenerator.utils.LanguageRecognizer
import com.qrcodescanner.barcodereader.qrgenerator.utils.TranslatorHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.banner

class TextTranslator : AppCompatActivity() {
    private lateinit var viewBinding: ActivityTextTranslatorBinding
    private lateinit var sourceLanguageLabel: TextView
    private lateinit var targetLanguageLabel: TextView
    private lateinit var targetLanguage: String // Variable to hold the target language
    private var savedLanguage: String? = null // Variable to hold the target language
    private lateinit var sourceLanguage: TextInputEditText
    private lateinit var btnTranslate: ImageView
    private lateinit var translationResult: TextInputEditText
    private lateinit var translatedText: String
    private var adLoadCount = 0
    private var targetlanguagecode: String? = null
    private var targetlanguageName: String? = null
    private var detectedLanguageCode: String? = null
    private lateinit var copySource: ImageView
    private lateinit var backButton: ImageView
    private val adReloadHandler = Handler(Looper.getMainLooper())
    private lateinit var adReloadRunnable: Runnable
    private val adReloadInterval: Long = 15000

    // Job variable to keep track of the translation job
    private lateinit var translationJob: Job
    //Backup btn
//    private lateinit var btnBackup: FloatingActionButton
    //initialize TranslatorHelper
    private val translatorHelper = TranslatorHelper(this)

    //Initialize the language detection
    private val languageRecognizer = LanguageRecognizer()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityTextTranslatorBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        supportActionBar?.hide()
        if (NetworkCheck.isNetworkAvailable(this@TextTranslator)) {
            loadShowBannerAd()
        }

        adReloadRunnable = Runnable {
            Log.d("AdTimer", "10 seconds passed. Reloading ad...")
            checkNetworkAndLoadAds()
            startAdReloadTimer()  // Schedule the next reload
        }

        hideSystemUI()
        val sharedPreferences = getSharedPreferences("LanguagePreferences", MODE_PRIVATE)
        savedLanguage = sharedPreferences.getString("LANGUAGE_NAME", "English")
        detectedLanguageCode = sharedPreferences.getString(
            "DETECTED_LANGUAGE_CODE",
            "en"
        ) // Default to 'en' if no code is found

        copySource = findViewById(R.id.ivCopySource)
        targetlanguagecode = sharedPreferences.getString("LANGUAGE_CODE", "en")
        targetlanguageName = sharedPreferences.getString("LANGUAGE_NAME", "English")
        targetLanguage = intent.getStringExtra("Target_Language") ?: "" // Use the target language
        sourceLanguageLabel = findViewById(R.id.sourceLanguageLabel)
        targetLanguageLabel = findViewById(R.id.targetLanguageLabel)
        sourceLanguage = findViewById(R.id.sourceLanguage)
        sourceLanguageLabel.text = savedLanguage
        targetLanguageLabel.text = targetlanguageName
        btnTranslate = findViewById(R.id.convertion)
        translationResult = findViewById(R.id.translationResult)
        val ocrResult = intent.getStringExtra("OCR_RESULT")
        sourceLanguage.setText(ocrResult)

        if (!ocrResult.isNullOrEmpty()) {
            translateText(ocrResult)
        } else {
            showToast("No OCR result available.")
        }

        sourceLanguage.setOnLongClickListener {
            pasteFromClipboard()
            true
        }

        viewBinding.btnBack.setOnClickListener {
            startActivity(Intent(this@TextTranslator, HomeActivity::class.java))
        }

        copySource.setOnClickListener {
            val textToCopy = sourceLanguage.text.toString() // Get text from sourceLanguage
            if (textToCopy.isNotEmpty()) {
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Source Text", textToCopy) // Create clip
                clipboardManager.setPrimaryClip(clipData) // Set the clipboard content
                showToast("Text copied to clipboard")
            } else {
                showToast("No text to copy")
            }
        }

        viewBinding.ivCopyResult.setOnClickListener {
            val textToCopy =
                viewBinding.translationResult.text.toString() // Get text from sourceLanguage
            if (textToCopy.isNotEmpty()) {
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Source Text", textToCopy) // Create clip
                clipboardManager.setPrimaryClip(clipData) // Set the clipboard content
                showToast("Text copied to clipboard")
            } else {
                showToast("No text to copy")
            }
        }



        viewBinding.ivShareSource.setOnClickListener {
            val textToShare = sourceLanguage.text.toString() // Get text from sourceLanguage
            if (textToShare.isNotEmpty()) {
                // Create the intent to share the text
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, textToShare) // Pass the text to share
                    type = "text/plain" // Specify the type of content being shared
                }
                // Start the share activity
                startActivity(Intent.createChooser(shareIntent, "Share text via"))
            } else {
                showToast("No text to share")
            }
        }

        viewBinding.ivShareResult.setOnClickListener {
            val textToShare =
                viewBinding.translationResult.text.toString() // Get text from sourceLanguage
            if (textToShare.isNotEmpty()) {
                // Create the intent to share the text
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, textToShare) // Pass the text to share
                    type = "text/plain" // Specify the type of content being shared
                }
                // Start the share activity
                startActivity(Intent.createChooser(shareIntent, "Share text via"))
            } else {
                showToast("No text to share")
            }
        }

        btnTranslate.setOnClickListener {
            val langText: String = sourceLanguage.text.toString()
            if (langText.isEmpty()) {
                showToast("Please enter text")
            } else {
                translateText(langText)
            }
        }
    }

    fun checkNetworkAndLoadAds() {
        if (NetworkCheck.isNetworkAvailable(this@TextTranslator)) {
            loadShowBannerAd()
        }
    }

    private fun loadShowBannerAd() {
        adLoadCount++
        // Log the number of times the ad has been loaded
        Log.e("AdLoadCount", "Ad has been loaded $adLoadCount times")

        AdsProvider.bannerAll.config(
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                banner,
                true
            )
        )
        AdsProvider.bannerAll.loadAds(MyApplication.getApplication())
        showBannerAd(AdsProvider.bannerAll, findViewById(R.id.bannerFr), keepAdsWhenLoading = true)
        findViewById<FrameLayout>(R.id.bannerFr).visibility = View.VISIBLE
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            controller?.hide(WindowInsets.Type.systemBars())
            controller?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // For Android 10 and below
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun translateText(langText: String) {
        translationJob = CoroutineScope(Dispatchers.Default).launch {
            val detectedLanguageCode = languageRecognizer.detectLanguage(langText)

            withContext(Dispatchers.Main) {
                if (detectedLanguageCode == "und") {
                    showToast("Can't identify language")
                } else {
                    // Get language name from language code
                    val languageName = getLanguageNameFromCode(detectedLanguageCode)
                    // Set the detected language to the TextView
                    sourceLanguageLabel.text = languageName
                }
            }
            // Translate to English
            translatedText = translatorHelper.detectAndTranslate(
                langText,
                detectedLanguageCode,
                targetlanguagecode!!
            )

            withContext(Dispatchers.Main) {
                translationResult.setText(translatedText)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startAdReloadTimer()  // Start or resume the timer when activity is visible
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getLanguageNameFromCode(languageCode: String): String {
        val locale = Locale(languageCode)
        return locale.displayLanguage
    }

    override fun onBackPressed() {
        startActivity(Intent(this@TextTranslator, HomeActivity::class.java))
        val abc = 4
        if (abc == 2) {
            super.onBackPressed()
        }
    }

    private fun startAdReloadTimer() {
        Log.d("AdTimer", "Starting or restarting ad reload timer for 10 seconds.")
        adReloadHandler.postDelayed(adReloadRunnable, adReloadInterval)
    }

    private fun stopAdReloadTimer() {
        Log.d("AdTimer", "Stopping ad reload timer.")
        adReloadHandler.removeCallbacks(adReloadRunnable)
    }

    override fun onPause() {
        super.onPause()
        Log.d("ActivityState", "HomeActivity paused. Stopping ad reload timer.")
        stopAdReloadTimer()  // Stop the timer when the activity goes into the background
    }

    private fun pasteFromClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val pastedText = clipData.getItemAt(0).text.toString()
            sourceLanguage.setText(pastedText)
            showToast("Text pasted from clipboard")
        } else {
            showToast("Clipboard is empty")
        }
    }
}