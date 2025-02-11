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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.adapters.ScanListAdapter
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import com.qrcodescanner.barcodereader.qrgenerator.models.ScannedItem

class ShowBatchFragment : Fragment() {

    private lateinit var scanListRecyclerView: RecyclerView
    private lateinit var scanListAdapter: ScanListAdapter
    private lateinit var scannedData: Array<ScannedItem> // To store the passed data
    private lateinit var navController: NavController
    private lateinit var dbHelper: QRCodeDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_batch, container, false)
        navController = findNavController()
        // Retrieve the passed data
        val scannedData =
            arguments?.getParcelableArray("scannedData")?.map { it as ScannedItem } ?: emptyList()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action =
                        ShowBatchFragmentDirections.actionShowbatchFragmentToBatchFragment()
                    navController.navigate(action)
                }
            })
        dbHelper = QRCodeDatabaseHelper(requireContext())

        // Initialize RecyclerView
        scanListRecyclerView = view.findViewById(R.id.scanListRecyclerView)
        scanListAdapter = ScanListAdapter(scannedData.toList()) { qrCodeText, isQrCode ->
            val action =
                ShowBatchFragmentDirections.actionShowBatchFragmentToCreateQRorBarcodeFragment(qrCodeText, isQrCode,isBarCode=true)
            navController.navigate(action)
        }

        scanListRecyclerView.adapter = scanListAdapter
        scanListRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }



    override fun onResume() {
        super.onResume()

        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.batch_scanner)

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
}

