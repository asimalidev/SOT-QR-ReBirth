package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.navigation.fragment.navArgs
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.qrcodescanner.barcodereader.qrgenerator.R

class ShowBarcodeFragment : Fragment() {

    private lateinit var barcodeImageView: ImageView
    private lateinit var navController:NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_barcode, container, false)
        barcodeImageView =view.findViewById(R.id.barcodeImageView)

        val args: ShowBarcodeFragmentArgs by navArgs()
        val barcodeText = args.inputText

        generateBarcode(barcodeText)

        return view
    }

    private fun generateBarcode(text: String) {
        try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.CODE_128, 600, 300)
            val barcodeBitmap = Bitmap.createBitmap(600, 300, Bitmap.Config.RGB_565)

            for (x in 0 until 600) {
                for (y in 0 until 300) {
                    barcodeBitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            barcodeImageView.setImageBitmap(barcodeBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the error
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action =
                        ShowBarcodeFragmentDirections.actionShowBarCodeFragmentToCreatebarcode()
                    navController.navigate(action)
                }
            })
    }

    override fun onResume() {
        super.onResume()
        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.text = getString(R.string.bar_code_reader)
        TopText.visibility = View.VISIBLE

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        if (back != null) {
            back.visibility = View.VISIBLE
        }
        back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }



        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        if (setting != null) {
            setting.visibility = View.INVISIBLE
        }


        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        if (ivClose != null) {
            ivClose.visibility = View.INVISIBLE
        }

    }
}
