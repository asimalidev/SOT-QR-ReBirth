package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.models.SharedViewModel

class WebViewFragment : Fragment() {

    private lateinit var webView: WebView
    private val privacyPolicyUrl = "https://sites.google.com/view/privacy-policy-softkeysinc"
    private var listener: OnWebViewFragmentInteraction? = null
    private lateinit var viewModel: SharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnWebViewFragmentInteraction) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.webView)
        setupWebView()
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        webView.loadUrl(privacyPolicyUrl)
    }

    private fun setupWebView() {
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.settings.javaScriptEnabled = true // Enable JavaScript if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.destroy() // Clean up the WebView
    }
//    override fun onPause() {
//        super.onPause()
//        // Show the banner ad when leaving this fragment
//        val activity = requireActivity() as HomeActivity
//        activity.showBannerAd()
//    }


    override fun onResume() {
        super.onResume()
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Privacy Policy  screen",
            trigger = "App display Privacy Policy screen",
            eventName = "privacy_policy_scr"
        )
        viewModel.isBannerVisible.value = false // Hide the banner when this fragment is visible

        listener?.onWebViewFragmentVisible() // Notify the activity
//        val activity = requireActivity() as HomeActivity
//        activity.hideBannerAd()

        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.privacy_policy)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        back?.visibility = View.VISIBLE

        back?.setOnClickListener {
            val action=WebViewFragmentDirections.actionNavWebNavSettings()
            findNavController().navigate(action)
        }

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

    }
}
