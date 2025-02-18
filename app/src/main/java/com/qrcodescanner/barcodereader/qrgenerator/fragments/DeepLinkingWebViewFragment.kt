package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentDeepLinkingWebViewBinding
import java.io.File

class DeepLinkingWebViewFragment : Fragment() {

    var navController: NavController? = null
    private lateinit var viewBinding: FragmentDeepLinkingWebViewBinding
    private var googleUrl = "https://lens.google.com/uploadbyurl?url="
    private var bingUrl = "https://www.bing.com/images/search?view=detailv2&iss=sbi&form=SBIVSP&sbisrc="
    private var yandexUrl = "https://yandex.ru/images/touch/search?rpt=imageview&url="
    private var btnBack: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentDeepLinkingWebViewBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    private fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNavControllerAdded()
        val activity = requireActivity() as HomeActivity
        activity.updateAdLayoutVisibility(shouldShowAd = false)
        CustomFirebaseEvents.logEvent(context = requireActivity(), eventName = "search_img_src")
        clearTempFiles()
        startShimmerLayout()
        initializeHeader()
        setupWebViewGoogle()
//        setupWebViewBing()
//        setupWebViewYandex()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController != null) {
                    CustomFirebaseEvents.logEvent(context = requireActivity(), eventName = "search_img_tap_back")
                    val action = DeepLinkingWebViewFragmentDirections.actionNavDeepLinkingWebViewToNavImageSearch()
                    navController?.navigate(action)
                } else {
                    isNavControllerAdded()
                }
            }
        })

        viewBinding.webViewGoogle.loadUrl(googleUrl + requireActivity().getSharedPreferences("DownloadURL", Context.MODE_PRIVATE).getString("DownloadURL",""))
//        viewBinding.webViewBing.loadUrl(bingUrl + requireActivity().getSharedPreferences("DownloadURL", Context.MODE_PRIVATE).getString("DownloadURL","") + "&q=imgurl:" + requireActivity().getSharedPreferences("DownloadURL", Context.MODE_PRIVATE).getString("DownloadURL",""))
//        viewBinding.webViewYandex.loadUrl(yandexUrl + requireActivity().getSharedPreferences("DownloadURL", Context.MODE_PRIVATE).getString("DownloadURL",""))

        viewBinding.ivGoogle.setOnClickListener { visibilityForGoogleWebView() }
//        viewBinding.ivBing.setOnClickListener { visibilityForBingWebView() }
//        viewBinding.ivYandex.setOnClickListener { visibilityForYandexWebView() }

        viewBinding.ivHome.setOnClickListener {
            if (navController != null) {
                CustomFirebaseEvents.logEvent(context = requireActivity(), eventName = "search_img_tap_home")
                val action = DeepLinkingWebViewFragmentDirections.actionNavDeepLinkingWebViewToNavHome()
                navController?.navigate(action)
            } else {
                isNavControllerAdded()
            }
        }
        viewBinding.ivReload.setOnClickListener {
            when {
                viewBinding.webViewGoogle.visibility == View.VISIBLE -> {
                    viewBinding.webViewGoogle.reload()
                }
                viewBinding.webViewBing.visibility == View.VISIBLE -> {
                    viewBinding.webViewBing.reload()
                }
                viewBinding.webViewYandex.visibility == View.VISIBLE -> {
                    viewBinding.webViewYandex.reload()
                }
            }
            CustomFirebaseEvents.logEvent(context = requireActivity(), eventName = "search_img_tap_reload")
        }
        viewBinding.ivCopy.setOnClickListener {
            when {
                viewBinding.webViewGoogle.visibility == View.VISIBLE -> {
                    copyLink(viewBinding.webViewGoogle.originalUrl!!)
                    Toast.makeText(requireActivity(), getString(R.string.label_copied_google_link), Toast.LENGTH_SHORT).show()
                }
                viewBinding.webViewBing.visibility == View.VISIBLE -> {
                    copyLink(viewBinding.webViewBing.originalUrl!!)
                    Toast.makeText(requireActivity(), "Copied Bing link", Toast.LENGTH_SHORT).show()
                }
                viewBinding.webViewYandex.visibility == View.VISIBLE -> {
                    copyLink(viewBinding.webViewYandex.originalUrl!!)
                    Toast.makeText(requireActivity(), "Copied Yandex link", Toast.LENGTH_SHORT).show()
                }
            }
            CustomFirebaseEvents.logEvent(context = requireActivity(), eventName = "search_img_tap_copy")
        }
        viewBinding.ivShare.setOnClickListener {
            when {
                viewBinding.webViewGoogle.visibility == View.VISIBLE -> {
                    Log.i("UrlLink", "onViewCreated: "+viewBinding.webViewGoogle.originalUrl!!)
                    shareCurrentUrl(viewBinding.webViewGoogle.originalUrl!!)
                    Toast.makeText(requireActivity(), getString(R.string.label_share_google_link), Toast.LENGTH_SHORT).show()
                }
                viewBinding.webViewBing.visibility == View.VISIBLE -> {
                    Log.i("UrlLink", "onViewCreated: "+viewBinding.webViewBing.originalUrl!!)
                    shareCurrentUrl(viewBinding.webViewBing.originalUrl!!)
                    Toast.makeText(requireActivity(), "Share Bing link", Toast.LENGTH_SHORT).show()
                }
                viewBinding.webViewYandex.visibility == View.VISIBLE -> {
                    Log.i("UrlLink", "onViewCreated: "+viewBinding.webViewYandex.originalUrl!!)
                        shareCurrentUrl(viewBinding.webViewYandex.originalUrl!!)
                    Toast.makeText(requireActivity(), "Share Yandex link", Toast.LENGTH_SHORT).show()
                }
            }
            CustomFirebaseEvents.logEvent(context = requireActivity(), eventName = "search_img_tap_share")
        }
        viewBinding.ivBack.setOnClickListener {
            when {
                viewBinding.webViewGoogle.visibility == View.VISIBLE -> {
                    if (viewBinding.webViewGoogle.canGoBack()) {
                        viewBinding.webViewGoogle.goBack()
                    }
                }
                viewBinding.webViewBing.visibility == View.VISIBLE -> {
                    if (viewBinding.webViewBing.canGoBack()) {
                        viewBinding.webViewBing.goBack()
                    }
                }
                viewBinding.webViewYandex.visibility == View.VISIBLE -> {
                    if (viewBinding.webViewYandex.canGoBack()) {
                        viewBinding.webViewYandex.goBack()
                    }
                }
            }
            CustomFirebaseEvents.logEvent(context = requireActivity(), eventName = "search_img_tap_back_website")
        }
        viewBinding.ivForward.setOnClickListener {
            when {
                viewBinding.webViewGoogle.visibility == View.VISIBLE -> {
                    if (viewBinding.webViewGoogle.canGoForward()) {
                        viewBinding.webViewGoogle.goForward()
                    }
                }
                viewBinding.webViewBing.visibility == View.VISIBLE -> {
                    if (viewBinding.webViewBing.canGoForward()) {
                        viewBinding.webViewBing.goForward()
                    }
                }
                viewBinding.webViewYandex.visibility == View.VISIBLE -> {
                    if (viewBinding.webViewYandex.canGoForward()) {
                        viewBinding.webViewYandex.goForward()
                    }
                }
            }
            CustomFirebaseEvents.logEvent(context = requireActivity(), eventName = "search_img_tap_forward_website")
        }
    }

    private fun copyLink(url: String) {
        val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Source Text", url)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun shareCurrentUrl(url: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,"Check out this link: $url")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun initializeHeader() {
        val topText: TextView = requireActivity().findViewById(R.id.mainText)
        topText.visibility = View.VISIBLE
        topText.text = getString(R.string.label_searched_image_results)

        btnBack = requireActivity().findViewById(R.id.ivBack)
        if (btnBack != null) {
            btnBack?.isEnabled = true
            btnBack?.visibility = View.VISIBLE
            btnBack?.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.webViewGoogle.destroy()
        viewBinding.webViewBing.destroy()
        viewBinding.webViewYandex.destroy()
    }

    private fun setupWebViewGoogle() {
        with(viewBinding.webViewGoogle) {
            settings.javaScriptEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: android.webkit.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    startShimmerLayout()
                    visibilityForGoogleWebView()
                }

                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    visibilityForGoogleWebView()
                    stopShimmerLayout()
                }

                override fun onReceivedError(view: android.webkit.WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    Toast.makeText(requireActivity(), requireActivity().getString(R.string.label_error_processing_image), Toast.LENGTH_LONG).show()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: android.webkit.WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    Log.e("webChromeClient", "onProgressChanged: $newProgress")
                }
            }
        }
    }

    private fun visibilityForGoogleWebView() {
        viewBinding.webViewGoogle.visibility = View.VISIBLE
        viewBinding.webViewBing.visibility = View.GONE
        viewBinding.webViewYandex.visibility = View.GONE
    }

    private fun setupWebViewBing() {
        with(viewBinding.webViewBing) {
            settings.javaScriptEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: android.webkit.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    stopShimmerLayout()
                }

                override fun onReceivedError(view: android.webkit.WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    stopShimmerLayout()
                    Toast.makeText(requireActivity(), "Error Processing Image", Toast.LENGTH_LONG).show()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: android.webkit.WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    Log.e("webChromeClient", "onProgressChanged: $newProgress")
                }
            }
        }
    }

    private fun visibilityForBingWebView() {
        viewBinding.webViewGoogle.visibility = View.GONE
        viewBinding.webViewBing.visibility = View.VISIBLE
        viewBinding.webViewYandex.visibility = View.GONE
    }

    private fun setupWebViewYandex() {
        with(viewBinding.webViewYandex) {
            settings.javaScriptEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: android.webkit.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    stopShimmerLayout()
                }

                override fun onReceivedError(view: android.webkit.WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    Toast.makeText(requireActivity(), "Error Processing Image", Toast.LENGTH_LONG).show()
                    stopShimmerLayout()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: android.webkit.WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    Log.e("webChromeClient", "onProgressChanged: $newProgress")
                }
            }
        }
    }

    private fun visibilityForYandexWebView() {
        viewBinding.webViewGoogle.visibility = View.GONE
        viewBinding.webViewBing.visibility = View.GONE
        viewBinding.webViewYandex.visibility = View.VISIBLE
    }

    private fun startShimmerLayout() {
        viewBinding.shimmerLoadingData.visibility = View.VISIBLE
        viewBinding.shimmerLoadingData.startShimmer()
    }

    private fun stopShimmerLayout() {
        viewBinding.shimmerLoadingData.visibility = View.GONE
        viewBinding.shimmerLoadingData.stopShimmer()
    }

    private fun clearTempFiles() {
        val outputDirectory = getOutputDirectory()
        val tempFile = File(outputDirectory, "/ImageSearchTemp")

        if (tempFile.exists() && tempFile.isDirectory) {
            tempFile.listFiles()?.forEach { it.delete() }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }
}