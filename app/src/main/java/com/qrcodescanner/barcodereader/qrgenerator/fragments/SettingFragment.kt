package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import apero.aperosg.firstopen.ui.activity.LanguageSettingsActivity
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentSettingBinding
import com.qrcodescanner.barcodereader.qrgenerator.models.ShareHelper


class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        sharedPreferences = requireActivity().getSharedPreferences("ScanSettings", Context.MODE_PRIVATE)
        // Set default values if not already set
        if (!sharedPreferences.contains("vibrate")) {
            sharedPreferences.edit().putBoolean("vibrate", true).apply()
        }
        if (!sharedPreferences.contains("sound")) {
            sharedPreferences.edit().putBoolean("sound", true).apply()
        }

        binding.ivBack.setOnClickListener {
            val action = SettingFragmentDirections.actionNavSettingToNavHome()
            navController.navigate(action)
        }

        binding.switch01.isChecked = sharedPreferences.getBoolean("vibrate", true)
        binding.switch1.isChecked = sharedPreferences.getBoolean("sound", true)
        binding.switchOpenWebsiteAutomatically.isChecked =
            sharedPreferences.getBoolean("openWebsiteAutomatically", false)
        binding.switchCopyToClipboard.isChecked =
            sharedPreferences.getBoolean("copyToClipboard", false)

        binding.switch01.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("vibrate", isChecked).apply()
        }

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("sound", isChecked).apply()
        }

        binding.switchOpenWebsiteAutomatically.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("openWebsiteAutomatically", isChecked).apply()
        }

        binding.switchCopyToClipboard.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("copyToClipboard", isChecked).apply()
        }
        binding.llHelp.setOnClickListener {
            val action = SettingFragmentDirections.actionNavSettingsToNavHelp()
            navController.navigate(action)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = SettingFragmentDirections.actionNavSettingToNavHome()
                    navController.navigate(action)
                }
            }
        )

        binding.llShare.setOnClickListener {
            ShareHelper.shareAppLink(requireContext())
        }

        binding.llRateUs.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${requireContext().packageName}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                    )
                )
            }
        }

        binding.llFeedBack.setOnClickListener {
            ShareHelper.sendFeedback(requireContext())
        }

        binding.llLaqnguages.setOnClickListener {
            startActivity(Intent(requireActivity(),LanguageSettingsActivity::class.java))
//            val action=SettingFragmentDirections.actionNavSettingToNavLanguage()
//            navController.navigate(action)
        }

        binding.llPrivacyPolicy.setOnClickListener {
            val action = SettingFragmentDirections.actionNavSettingToWebview()
            findNavController().navigate(action)
        }


        binding.llMoreApps.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/dev?id=7920435114857967276")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                // Handle exception if Google Play Store app is not available
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val showTopLayer = arguments?.getBoolean("ShowTopLayer", false) ?: false
        if (showTopLayer) {
            // Show the top layer (e.g., make a specific view visible)
            binding.topLayer.visibility = View.VISIBLE
        }
        val topText: TextView = requireActivity().findViewById(R.id.mainText)
        topText.text = getString(R.string.settings)
        topText.visibility = View.VISIBLE

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        back?.visibility = View.VISIBLE
        back?.setOnClickListener {
            requireActivity().onBackPressed()
        }
        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        setting?.visibility = View.INVISIBLE

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        ivClose?.visibility = View.INVISIBLE
    }

}
