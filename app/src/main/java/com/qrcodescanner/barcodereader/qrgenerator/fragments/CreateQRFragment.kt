package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import apero.aperosg.monetization.util.showNativeAd
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentCreateQRBinding
import com.qrcodescanner.barcodereader.qrgenerator.adapters.SocialAdapter
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.models.SocialItem
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider

class CreateQRFragment : Fragment() {
    private lateinit var socialAdapter: SocialAdapter
    private var navController: NavController? = null
    private lateinit var binding: FragmentCreateQRBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateQRBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as HomeActivity).reloadAds()
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "App display Create QR code screen",
            trigger = "App display tab Create",
            eventName = "createqr_scr"
        )
        navController = findNavController()
        binding.recyclerviewItems.layoutManager = GridLayoutManager(requireContext(), 3)

        val socialItems = listOf(
            SocialItem(R.drawable.ic_youtube, getString(R.string.youtube)),
            SocialItem(R.drawable.ic_facebook, getString(R.string.facebook)),
            SocialItem(R.drawable.ic_instagram, getString(R.string.instagram)),
            SocialItem(R.drawable.ic_twitter, getString(R.string.twitter)),
            SocialItem(R.drawable.ic_whatsapp, getString(R.string.whatsapp)),
            SocialItem(R.drawable.ic_dailymotion, getString(R.string.daily_motion)),
            SocialItem(R.drawable.ic_email, getString(R.string.email)),
            SocialItem(R.drawable.ic_apps, getString(R.string.apps)),
            SocialItem(R.drawable.ic_copy__2_, getString(R.string.clipboard)),
            SocialItem(R.drawable.ic_website, getString(R.string.website)),
            SocialItem(R.drawable.ic_contacts, getString(R.string.contact)),
            SocialItem(R.drawable.ic_wifi, getString(R.string.wifi)),
            SocialItem(R.drawable.ic_location, getString(R.string.location)),
            SocialItem(R.drawable.ic_calender, getString(R.string.calender))
        )

        socialAdapter = SocialAdapter(socialItems) { item ->
            val myItem = item.text
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Tab Create",
                trigger = "User tap $myItem",
                eventName = "tab_create_scr_tap_$myItem"
            )
            navigateToViewQRCode(item.text)
        }

        binding.recyclerviewItems.adapter = socialAdapter

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    CustomFirebaseEvents.logEvent(
                        context = requireActivity(),
                        screenName = "Create QR code screen",
                        trigger = "User tap button Back",
                        eventName = "create_scr_tap_back"
                    )
                    if (navController != null) {
                        val action = CreateQRFragmentDirections.actionNavCreateToNavHome()
                        navController!!.navigate(action)
                    } else {
                        isNavControllerAdded()
                    }
                }
            })
    }


    private fun navigateToViewQRCode(platform: String) {
        Log.d("CreateQRFragment", "Navigating to: $platform")
        when (platform) {
            getString(R.string.email) -> {
                if (navController != null) {
                    CustomFirebaseEvents.logEvent(
                        context = requireActivity(),
                        screenName = "Create QR code screen",
                        trigger = "User tap a type of QR want to create",
                        eventName = "create_scr_tap_type"
                    )
                    val action = CreateQRFragmentDirections.actionNavCreateQRToNavEmail()
                    findNavController().navigate(action)
                } else {
                    isNavControllerAdded()
                }
            }

            getString(R.string.apps) -> {
                if (navController != null) {
                    CustomFirebaseEvents.logEvent(
                        context = requireActivity(),
                        screenName = "Create QR code screen",
                        trigger = "User tap a type of QR want to create",
                        eventName = "create_scr_tap_type"
                    )
                    val action = CreateQRFragmentDirections.actionNavCreateQRToNavApps()
                    findNavController().navigate(action)
                } else {
                    isNavControllerAdded()
                }
            }

            getString(R.string.clipboard) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR code screen",
                    trigger = "User tap a type of QR want to create",
                    eventName = "create_scr_tap_type"
                )
                val clipboard =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = clipboard.primaryClip

                if (clipData != null && clipData.itemCount > 0) {
                    val clipboardText = clipData.getItemAt(0).text.toString()
                    if (navController != null) {
                        val action =
                            CreateQRFragmentDirections.actionNavCreateQRToNavClipboard(clipboardText)
                        findNavController().navigate(action)
                    } else {
                        isNavControllerAdded()
                    }
                } else {
                    Toast.makeText(requireContext(), "Clipboard is empty", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            getString(R.string.contact) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR code screen",
                    trigger = "User tap a type of QR want to create",
                    eventName = "create_scr_tap_type"
                )
                if (navController != null) {
                    val action = CreateQRFragmentDirections.actionNavCreateToNavContact()
                    findNavController().navigate(action)
                } else {
                    isNavControllerAdded()
                }
            }


            getString(R.string.wifi) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR code screen",
                    trigger = "User tap a type of QR want to create",
                    eventName = "create_scr_tap_type"
                )
                if (navController != null) {
                    val action = CreateQRFragmentDirections.actionNavCreateToNavWifi()
                    findNavController().navigate(action)
                } else {
                    isNavControllerAdded()
                }
            }

            getString(R.string.location) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR code screen",
                    trigger = "User tap a type of QR want to create",
                    eventName = "create_scr_tap_type"
                )
                if (navController != null) {
                    val action = CreateQRFragmentDirections.actionNavCreateQRToNavLocation()
                    findNavController().navigate(action)
                } else {
                    isNavControllerAdded()
                }
            }

            getString(R.string.calender) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR code screen",
                    trigger = "User tap a type of QR want to create",
                    eventName = "create_scr_tap_type"
                )
                if (navController != null) {
                    val action = CreateQRFragmentDirections.actionNavCreateToNavCalender()
                    findNavController().navigate(action)
                } else {
                    isNavControllerAdded()
                }
            }

            else -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR code screen",
                    trigger = "User tap a type of QR want to create",
                    eventName = "create_scr_tap_type"
                )
                if (navController != null) {
                    val action =
                        CreateQRFragmentDirections.actionNavCreateToNavCreatelinks(platform)
                    findNavController().navigate(action)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        isNavControllerAdded()


        val isAdEnabled = requireActivity().getSharedPreferences(
            "RemoteConfig", AppCompatActivity.MODE_PRIVATE
        ).getBoolean("native_create", true)

// Configure and load the ad only if it's enabled and network is available
        if (NetworkCheck.isNetworkAvailable(requireContext()) && isAdEnabled) {
            AdsProvider.nativeCreate.config(isAdEnabled)
            AdsProvider.nativeCreate.loadAds(MyApplication.getApplication())

            binding.layoutAdNative.visibility = View.VISIBLE
            showNativeAd(AdsProvider.nativeCreate, binding.layoutAdNative, R.layout.layout_home_native_ad)
        } else {
            binding.layoutAdNative.visibility = View.GONE
        }


        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.create_qr_code)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        if (back != null) {
            back.visibility = View.VISIBLE
        }
        back.setOnClickListener {
            requireActivity().onBackPressed()
        }
        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        if (setting != null) {
            setting.visibility = View.INVISIBLE
        }
        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        if (ivClose != null) {
            ivClose.setImageResource(R.drawable.ic_premium)
            ivClose.visibility = View.INVISIBLE
        }

    }

    private fun isNavControllerAdded() {
        navController = findNavController()
    }

}

