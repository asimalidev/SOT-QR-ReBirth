package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import apero.aperosg.monetization.util.showBannerAd
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.utils.CountryList
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivityAllLanguagesBinding
import com.qrcodescanner.barcodereader.qrgenerator.adapters.CountryAdapter
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.models.AllCountryModel
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.banner

class AllLanguagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllLanguagesBinding
    private lateinit var countryAdapter: CountryAdapter
    private var adLoadCount = 0
    private val adReloadHandler = Handler(Looper.getMainLooper())
    private lateinit var adReloadRunnable: Runnable
    private val adReloadInterval: Long = 15000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllLanguagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adReloadRunnable = Runnable {
            Log.d("AdTimer", "10 seconds passed. Reloading ad...")
            checkNetworkAndLoadAds()
            startAdReloadTimer()
        }

        if (NetworkCheck.isNetworkAvailable(this@AllLanguagesActivity)) {
            loadShowBannerAd()
        }
        supportActionBar?.hide()
        hideSystemUI()
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        initRecyclerView()
        setupSearchView()
        loadSelectedLanguage()
    }

    private fun startAdReloadTimer() {
        Log.d("AdTimer", "Starting or restarting ad reload timer for 10 seconds.")
        adReloadHandler.postDelayed(adReloadRunnable, adReloadInterval)
    }

    private fun checkNetworkAndLoadAds() {
        val adLayout: FrameLayout = findViewById(R.id.bannerFr)
        val adLayoutCl: ConstraintLayout? = findViewById(R.id.clbanner)
        adLayoutCl?.let {
            if (NetworkCheck.isNetworkAvailable(this) && getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(banner, true)) {
                loadShowBannerAd()
                adLayout.visibility = View.VISIBLE
            } else {
                adLayout.visibility = View.GONE
                adLayoutCl.visibility=View.GONE
            }
        } ?: run {
            return
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("ActivityState", "HomeActivity paused. Stopping ad reload timer.")
        stopAdReloadTimer()  // Stop the timer when the activity goes into the background
    }

    private fun stopAdReloadTimer() {
        Log.d("AdTimer", "Stopping ad reload timer.")
        adReloadHandler.removeCallbacks(adReloadRunnable)
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

    private fun setupSearchView() {
        binding.idSV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                countryAdapter.filterList(newText.orEmpty()) // Filter the list based on the input text
                return true
            }
        })
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
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun initRecyclerView() {
        countryAdapter = CountryAdapter(CountryList.getCountryListDup(), this)
        binding.recyclerView.adapter = countryAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        // Set the listener for empty results
        countryAdapter.setOnEmptyResultsListener { isEmpty, query ->
            if (isEmpty) {
                binding.tvNoResults.text = getString(R.string.no_result_found_for, query)
                binding.tvNoResults.visibility = View.VISIBLE
                binding.selectedLanguage.visibility = View.GONE
                binding.textView18.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE // Hide the RecyclerView if no results
                binding.constraintLayout6.visibility =
                    View.GONE // Hide the RecyclerView if no results
            } else {
                binding.selectedLanguage.visibility = View.VISIBLE
                binding.tvNoResults.visibility = View.GONE
                binding.textView18.visibility = View.VISIBLE
                binding.constraintLayout6.visibility = View.VISIBLE
                binding.recyclerView.visibility =
                    View.VISIBLE // Show the RecyclerView if there are results
            }
        }

        countryAdapter.setOnLanguageSelectedListener { selectedLanguage ->
            updateSelectedLanguage(selectedLanguage)
        }
    }

    // Method to update the selected language and its flag
    private fun updateSelectedLanguage(language: AllCountryModel) {
        binding.selectedLanguage.text = language.languageName // Set the selected language name
        binding.ivFlag.setImageResource(language.flagResId) // Set the corresponding flag image
    }

    override fun onResume() {
        super.onResume()
        startAdReloadTimer()  // Start or resume the timer when activity is visible
    }

    private fun loadSelectedLanguage() {
        val sharedPreferences = getSharedPreferences("LanguagePreferences", MODE_PRIVATE)
        val selectedLanguageName = sharedPreferences.getString("LANGUAGE_NAME", null)
        val selectedFlagResId = sharedPreferences.getInt("SELECTED_FLAG_RES_ID", 0)
        // If there's a saved language name, update the UI
        if (selectedLanguageName != null && selectedFlagResId != 0) {
            binding.tvLanguage.text = selectedLanguageName
            binding.ivFlag.setImageResource(selectedFlagResId)
        }
    }
}





