package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.content.Context
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
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentDeepLinkingWebViewBinding

class DeepLinkingWebViewFragment : Fragment() {

    var navController: NavController? = null
    private lateinit var viewBinding: FragmentDeepLinkingWebViewBinding
    private val googleUrl = "https://lens.google.com/uploadbyurl?url="
    private val bingUrl = "https://www.bing.com/images/search?view=detailv2&iss=sbi&form=SBIVSP&sbisrc="
    private val yandexUrl = "https://yandex.ru/images/touch/search?rpt=imageview&url="
    var btnBack: ImageView? = null

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
        startShimmerLayout()
        initializeHeader()
        setupWebViewGoogle()
        setupWebViewBing()
        setupWebViewYandex()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController != null) {
                    val action = DeepLinkingWebViewFragmentDirections.actionNavDeepLinkingWebViewToNavImageSearch()
                    navController?.navigate(action)
                } else {
                    isNavControllerAdded()
                }
            }
        })

        viewBinding.webViewGoogle.loadUrl(googleUrl + requireActivity().getSharedPreferences("DownloadURL", Context.MODE_PRIVATE).getString("DownloadURL",""))
        viewBinding.webViewBing.loadUrl(bingUrl + requireActivity().getSharedPreferences("DownloadURL", Context.MODE_PRIVATE).getString("DownloadURL","") + "&q=imgurl:" + requireActivity().getSharedPreferences("DownloadURL", Context.MODE_PRIVATE).getString("DownloadURL",""))
        viewBinding.webViewYandex.loadUrl(yandexUrl + requireActivity().getSharedPreferences("DownloadURL", Context.MODE_PRIVATE).getString("DownloadURL",""))

        viewBinding.imgGoogle.setOnClickListener { visibilityForGoogleWebView() }
        viewBinding.imgBing.setOnClickListener { visibilityForBingWebView() }
        viewBinding.imgYandex.setOnClickListener { visibilityForYandexWebView() }
    }

    private fun initializeHeader() {
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "",
            trigger = "",
            eventName = "image_search_result_scr"
        )

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

    private fun visibilityForGoogleWebView() {
        viewBinding.webViewGoogle.visibility = View.VISIBLE
        viewBinding.webViewBing.visibility = View.GONE
        viewBinding.webViewYandex.visibility = View.GONE
    }

    private fun setupWebViewBing() {
        with(viewBinding.webViewGoogle) {
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
        with(viewBinding.webViewGoogle) {
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
}