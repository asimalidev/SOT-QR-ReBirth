package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {
    private lateinit var binding: FragmentHelpBinding
    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentHelpBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController=findNavController()

        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action= HelpFragmentDirections.actionNavhelpToNavHome()
                    navController.navigate(action)
                }
            })


        binding.clHelp.setOnClickListener {
            val action= HelpFragmentDirections.actionNavHelpToNavUse()
            navController.navigate(action)
        }

        binding.clScanTips.setOnClickListener {
            val action= HelpFragmentDirections.actionNavhelpToNavScanning()
            navController.navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()

        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.help)

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
        setting?.setOnClickListener {
            val action = HelpFragmentDirections.actionNavhelpToNavScanning()
            navController?.navigate(action)
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        ivClose?.setImageResource(R.drawable.ic_help)
        ivClose?.visibility = View.GONE


    }

}