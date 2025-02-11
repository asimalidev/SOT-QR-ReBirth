package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.google.android.material.tabs.TabLayout
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HistoryListener
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.database.OnQRCodeClickListener
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeAdapter
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeData
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper

class HistoryFragment : Fragment(), OnQRCodeClickListener {
    private lateinit var recyclerViewAll: RecyclerView
    private lateinit var recyclerViewCreated: RecyclerView
    private lateinit var recyclerViewScanned: RecyclerView
    private lateinit var adapterAll: QRCodeAdapter
    private lateinit var adapterCreated: QRCodeAdapter
    private lateinit var adapterScanned: QRCodeAdapter
    private lateinit var dbHelper: QRCodeDatabaseHelper
    private lateinit var navController: NavController
    private lateinit var emptyTextView: TextView
    private lateinit var emptyImageView: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var homeActivity: HomeActivity
    private var selectedTabPosition: Int = 0 // Declare it as a class-level variable

    private var historyListener: HistoryListener? = null // Listener for history events

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        // Initializing RecyclerViews
        recyclerViewAll = view.findViewById(R.id.recyclerViewAll)
        recyclerViewCreated = view.findViewById(R.id.recyclerViewCreated)
        recyclerViewScanned = view.findViewById(R.id.recyclerViewScanned)
        tabLayout = view.findViewById(R.id.tabLayout)

        return view
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        CustomFirebaseEvents.logEvent(
//            context = requireActivity(),
//            screenName = "Tab Batch",
//            trigger = "App display tab History",
//            eventName = "tab_history_scr"
//        )
//        navController = findNavController()
//        dbHelper = QRCodeDatabaseHelper(requireContext())
//        emptyTextView = view.findViewById(R.id.tvNoData)
//        emptyImageView = view.findViewById(R.id.ivNoData)
//
//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    // Ensure navController is available and you're navigating to the correct destination
//                    val navController =
//                        findNavController() // Ensure this points to the correct NavController
//
//                    // Check if the action exists in the navigation graph before navigating
//                    try {
//                        val action = HistoryFragmentDirections.actionHistoryToHome()
//                        navController.navigate(action)
//                    } catch (e: IllegalArgumentException) {
//                        Log.e("ScanCode", "Navigation action not found: ${e.message}")
//                        // Handle navigation failure, e.g., navigate to a fallback destination
//                    }
//                }
//            })
//
////         Get QR Codes for each category
//        val allQRCodes = dbHelper.getAllQRCodes().toMutableList()
//        val createdQRCodes = dbHelper.getQRCodesByEntryType("created").toMutableList()
//        val scannedQRCodes = dbHelper.getQRCodesByEntryType("scanned").toMutableList()
//
//
//        // Setup Adapters
//        adapterAll = QRCodeAdapter(allQRCodes, dbHelper, this)
//        adapterCreated = QRCodeAdapter(createdQRCodes, dbHelper, this)
//        adapterScanned = QRCodeAdapter(scannedQRCodes, dbHelper, this)
//
//        // Setup RecyclerViews with their respective adapters
//        recyclerViewAll.layoutManager = LinearLayoutManager(requireContext())
//        recyclerViewCreated.layoutManager = LinearLayoutManager(requireContext())
//        recyclerViewScanned.layoutManager = LinearLayoutManager(requireContext())
//
//        recyclerViewAll.adapter = adapterAll
//        recyclerViewCreated.adapter = adapterCreated
//        recyclerViewScanned.adapter = adapterScanned
//
//        // Handle visibility based on data
//        if (allQRCodes.isEmpty()) {
//            recyclerViewAll.visibility = View.GONE
//        }
//        if (createdQRCodes.isEmpty()) {
//            recyclerViewCreated.visibility = View.GONE
//        }
//        if (scannedQRCodes.isEmpty()) {
//            recyclerViewScanned.visibility = View.GONE
//        }
//
//        // Log history empty state to the listener
//        // In HistoryFragment, where you detect the empty state and selected tab:
//        historyListener?.onHistoryListEmpty(
//            isEmpty = allQRCodes.isEmpty() && createdQRCodes.isEmpty() && scannedQRCodes.isEmpty(),
//            selectedTab = selectedTab,
//            isAllEmpty = allQRCodes.isEmpty(),
//            isCreatedEmpty = createdQRCodes.isEmpty(),
//            isScannedEmpty = scannedQRCodes.isEmpty()
//        )
//
//
//        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                selectedTabPosition = tab.position // Store the selected tab position
//
//                // Call handleEmptyState with the selected tab position as the 4th argument
//                handleEmptyState(allQRCodes, createdQRCodes, scannedQRCodes, selectedTabPosition)
//
//                when (tab.position) {
//                    0 -> {
//                        recyclerViewAll.visibility = View.VISIBLE
//                        recyclerViewCreated.visibility = View.GONE
//                        recyclerViewScanned.visibility = View.GONE
//                    }
//
//                    1 -> {
//                        recyclerViewAll.visibility = View.GONE
//                        recyclerViewCreated.visibility = View.VISIBLE
//                        recyclerViewScanned.visibility = View.GONE
//                    }
//
//                    2 -> {
//                        recyclerViewAll.visibility = View.GONE
//                        recyclerViewCreated.visibility = View.GONE
//                        recyclerViewScanned.visibility = View.VISIBLE
//                    }
//                }
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab) {}
//            override fun onTabReselected(tab: TabLayout.Tab) {}
//        })
//
//        // Add tabs to TabLayout
//        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.all)))
//        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.created)))
//        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.scanned)))
//
//        handleEmptyState(allQRCodes, createdQRCodes, scannedQRCodes, selectedTabPosition)
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Tab Batch",
            trigger = "App display tab History",
            eventName = "tab_history_scr"
        )
        navController = findNavController()
        dbHelper = QRCodeDatabaseHelper(requireContext())
        emptyTextView = view.findViewById(R.id.tvNoData)
        emptyImageView = view.findViewById(R.id.ivNoData)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Ensure navController is available and you're navigating to the correct destination
                    val navController = findNavController()

                    try {
                        val action = HistoryFragmentDirections.actionHistoryToHome()
                        navController.navigate(action)
                    } catch (e: IllegalArgumentException) {
                        Log.e("ScanCode", "Navigation action not found: ${e.message}")
                    }
                }
            })

        // Get QR Codes for each category
        val allQRCodes = dbHelper.getAllQRCodes().toMutableList()
        val createdQRCodes = dbHelper.getQRCodesByEntryType("created").toMutableList()
        val scannedQRCodes = dbHelper.getQRCodesByEntryType("scanned").toMutableList()

        // Setup Adapters
        adapterAll = QRCodeAdapter(allQRCodes, dbHelper, this)
        adapterCreated = QRCodeAdapter(createdQRCodes, dbHelper, this)
        adapterScanned = QRCodeAdapter(scannedQRCodes, dbHelper, this)

        // Setup RecyclerViews with their respective adapters
        recyclerViewAll.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCreated.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewScanned.layoutManager = LinearLayoutManager(requireContext())

        recyclerViewAll.adapter = adapterAll
        recyclerViewCreated.adapter = adapterCreated
        recyclerViewScanned.adapter = adapterScanned

        // Handle visibility based on data
        if (allQRCodes.isEmpty()) {
            recyclerViewAll.visibility = View.GONE
        }
        if (createdQRCodes.isEmpty()) {
            recyclerViewCreated.visibility = View.GONE
        }
        if (scannedQRCodes.isEmpty()) {
            recyclerViewScanned.visibility = View.GONE
        }

        // Log history empty state to the listener
        historyListener?.onHistoryListEmpty(
            isEmpty = allQRCodes.isEmpty() && createdQRCodes.isEmpty() && scannedQRCodes.isEmpty(),
            selectedTab = selectedTabPosition,
            isAllEmpty = allQRCodes.isEmpty(),
            isCreatedEmpty = createdQRCodes.isEmpty(),
            isScannedEmpty = scannedQRCodes.isEmpty()
        )

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTabPosition = tab.position // Store the selected tab position

                // Call handleEmptyState with the selected tab position as the 4th argument
                handleEmptyState(allQRCodes, createdQRCodes, scannedQRCodes, selectedTabPosition)

                when (tab.position) {
                    0 -> {
                        recyclerViewAll.visibility = View.VISIBLE
                        recyclerViewCreated.visibility = View.GONE
                        recyclerViewScanned.visibility = View.GONE
                    }

                    1 -> {
                        recyclerViewAll.visibility = View.GONE
                        recyclerViewCreated.visibility = View.VISIBLE
                        recyclerViewScanned.visibility = View.GONE
                    }

                    2 -> {
                        recyclerViewAll.visibility = View.GONE
                        recyclerViewCreated.visibility = View.GONE
                        recyclerViewScanned.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Add tabs to TabLayout
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.all)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.created)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.scanned)))

        // Ensure to handle visibility after initializing tabs
        handleEmptyState(allQRCodes, createdQRCodes, scannedQRCodes, selectedTabPosition)
    }

    //    private fun handleEmptyState(
//        allQRCodes: List<QRCodeData>,
//        createdQRCodes: List<QRCodeData>,
//        scannedQRCodes: List<QRCodeData>,
//        selectedTab: Int // Pass the selected tab (0 = All, 1 = Created, 2 = Scanned)
//    ) {
//        val isAllEmpty = allQRCodes.isEmpty()
//        val isCreatedEmpty = createdQRCodes.isEmpty()
//        val isScannedEmpty = scannedQRCodes.isEmpty()
//
//        // Handle visibility based on the selected tab
//        when (selectedTab) {
//            0 -> { // All Tab
//                recyclerViewAll.visibility = if (isAllEmpty) View.GONE else View.VISIBLE
//                recyclerViewCreated.visibility = View.GONE
//                recyclerViewScanned.visibility = View.GONE
//            }
//            1 -> { // Created Tab
//                recyclerViewAll.visibility = View.GONE
//                recyclerViewCreated.visibility = if (isCreatedEmpty) View.GONE else View.VISIBLE
//                recyclerViewScanned.visibility = View.GONE
//            }
//            2 -> { // Scanned Tab
//                recyclerViewAll.visibility = View.GONE
//                recyclerViewCreated.visibility = View.GONE
//                recyclerViewScanned.visibility = if (isScannedEmpty) View.GONE else View.VISIBLE
//            }
//        }
//
//        // If all lists are empty, show empty state
//        if (isAllEmpty && isCreatedEmpty && isScannedEmpty) {
//            emptyTextView.visibility = View.VISIBLE
//            emptyImageView.visibility = View.VISIBLE
//        } else {
//            emptyTextView.visibility = View.GONE
//            emptyImageView.visibility = View.GONE
//        }
//    }

//    private fun handleEmptyState(
//        allQRCodes: List<QRCodeData>,
//        createdQRCodes: List<QRCodeData>,
//        scannedQRCodes: List<QRCodeData>,
//        selectedTab: Int // Pass the selected tab (0 = All, 1 = Created, 2 = Scanned)
//    ) {
//        val isAllEmpty = allQRCodes.isEmpty()
//        val isCreatedEmpty = createdQRCodes.isEmpty()
//        val isScannedEmpty = scannedQRCodes.isEmpty()
//
//        // Handle visibility based on the selected tab
//        when (selectedTab) {
//            0 -> { // All Tab
//                recyclerViewAll.visibility = if (isAllEmpty) View.GONE else View.VISIBLE
//                recyclerViewCreated.visibility = View.GONE
//                recyclerViewScanned.visibility = View.GONE
//            }
//            1 -> { // Created Tab
//                recyclerViewAll.visibility = View.GONE
//                recyclerViewCreated.visibility = if (isCreatedEmpty) View.GONE else View.VISIBLE
//                recyclerViewScanned.visibility = View.GONE
//            }
//            2 -> { // Scanned Tab
//                recyclerViewAll.visibility = View.GONE
//                recyclerViewCreated.visibility = View.GONE
//                recyclerViewScanned.visibility = if (isScannedEmpty) View.GONE else View.VISIBLE
//            }
//        }
//
//        // Update ad visibility based on the selected tab's list
//        homeActivity.updateAdLayoutVisibility(selectedTab, isAllEmpty, isCreatedEmpty, isScannedEmpty)
//    }

    private fun handleEmptyState(
        allQRCodes: List<QRCodeData>,
        createdQRCodes: List<QRCodeData>,
        scannedQRCodes: List<QRCodeData>,
        selectedTab: Int // Pass the selected tab (0 = All, 1 = Created, 2 = Scanned)
    ) {
        val isAllEmpty = allQRCodes.isEmpty()
        val isCreatedEmpty = createdQRCodes.isEmpty()
        val isScannedEmpty = scannedQRCodes.isEmpty()

        // Handle visibility based on the selected tab
        when (selectedTab) {
            0 -> { // All Tab
                recyclerViewAll.visibility = if (isAllEmpty) View.GONE else View.VISIBLE
                recyclerViewCreated.visibility = View.GONE
                recyclerViewScanned.visibility = View.GONE
                // Show empty state only for the All tab
                emptyTextView.visibility = if (isAllEmpty) View.VISIBLE else View.GONE
                emptyImageView.visibility = if (isAllEmpty) View.VISIBLE else View.GONE
            }
            1 -> { // Created Tab
                recyclerViewAll.visibility = View.GONE
                recyclerViewCreated.visibility = if (isCreatedEmpty) View.GONE else View.VISIBLE
                recyclerViewScanned.visibility = View.GONE
                // Show empty state only for the Created tab
                emptyTextView.visibility = if (isCreatedEmpty) View.VISIBLE else View.GONE
                emptyImageView.visibility = if (isCreatedEmpty) View.VISIBLE else View.GONE
            }
            2 -> { // Scanned Tab
                recyclerViewAll.visibility = View.GONE
                recyclerViewCreated.visibility = View.GONE
                recyclerViewScanned.visibility = if (isScannedEmpty) View.GONE else View.VISIBLE
                // Show empty state only for the Scanned tab
                emptyTextView.visibility = if (isScannedEmpty) View.VISIBLE else View.GONE
                emptyImageView.visibility = if (isScannedEmpty) View.VISIBLE else View.GONE
            }
        }

        // If all lists are empty, show empty state for the selected tab
        if (isAllEmpty && isCreatedEmpty && isScannedEmpty) {
            emptyTextView.visibility = View.VISIBLE
            emptyImageView.visibility = View.VISIBLE
        }

        // Update ad visibility based on the selected tab's list
        homeActivity.updateAdLayoutVisibility(selectedTab, isAllEmpty, isCreatedEmpty, isScannedEmpty)
    }




    override fun onQRCodeClick(qrCodeText: String, entryType: String) {
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Tab Batch",
            trigger = "User tap a QR, barcode to view",
            eventName = "tab_history_scr_tap_view_code"
        )

        // Get QR Code data by text
        val qrCodeData = dbHelper.getQRCodeData(qrCodeText)

        if (qrCodeData != null) {
            // Check entry type and navigate accordingly
            when (qrCodeData.entryType) {
                "created" -> {
                    // If it's a Created QR code, navigate to NaveditedQr
                    val action = HistoryFragmentDirections.actionHistoryToNaveditedQr(qrCodeText)
                    navController.navigate(action)
                }

                "scanned" -> {
                    // If it's a Scanned QR code, navigate to ShowScannedCode
                    val action =
                        HistoryFragmentDirections.actionHistoryToNavshowqr(qrCodeText, true)
                    navController.navigate(action)
                }

                else -> {
                    // Handle the case where entryType is not "created" or "scanned"
                    // We need to check if the QR code exists as a "created" or "scanned" QR code
                    val qrCodeDataFromCreated =
                        dbHelper.getQRCodesByEntryType("created").find { it.qrCode == qrCodeText }
                    val qrCodeDataFromScanned =
                        dbHelper.getQRCodesByEntryType("scanned").find { it.qrCode == qrCodeText }

                    when {
                        qrCodeDataFromCreated != null -> {
                            // If it's found in the created QR codes list, navigate to NaveditedQr
                            val action =
                                HistoryFragmentDirections.actionHistoryToNaveditedQr(qrCodeText)
                            navController.navigate(action)
                        }

                        qrCodeDataFromScanned != null -> {
                            // If it's found in the scanned QR codes list, navigate to ShowScannedCode
                            val action =
                                HistoryFragmentDirections.actionHistoryToNavshowqr(qrCodeText, true)
                            navController.navigate(action)
                        }

                        else -> {
                            // Handle unexpected case where QR code is not found
                            Log.e(
                                "onQRCodeClick",
                                "QR Code not found in either created or scanned list: $qrCodeText"
                            )
                        }
                    }
                }
            }
        } else {
            // Handle the case where QR code data isn't found in the database
            Log.e("onQRCodeClick", "QR Code not found in the database: $qrCodeText")
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HistoryListener) {
            historyListener = context
        }
        if (context is HomeActivity) {
            homeActivity = context
        }
    }


    override fun onDelete(deleted: Boolean) {
        // Get the updated list of QR codes from the database
        val updatedAllQRCodes = dbHelper.getAllQRCodes().toMutableList()

        if (deleted) {
            // If deleted is true, show the empty state and hide RecyclerViews
            emptyTextView.visibility = View.VISIBLE
            emptyImageView.visibility = View.VISIBLE

            recyclerViewAll.visibility = View.GONE
            recyclerViewCreated.visibility = View.GONE
            recyclerViewScanned.visibility = View.GONE

            // Update the adapter with the updated list after deletion
            adapterAll.updateList(updatedAllQRCodes)

            // Notify the listener about the empty state
            historyListener?.onHistoryListEmpty(updatedAllQRCodes.isEmpty(), selectedTabPosition,
                updatedAllQRCodes.isEmpty(), updatedAllQRCodes.isEmpty(), updatedAllQRCodes.isEmpty())

        } else {
            // If nothing was deleted, hide the empty state and show RecyclerViews
            emptyTextView.visibility = View.GONE
            emptyImageView.visibility = View.GONE

            recyclerViewAll.visibility = View.VISIBLE
            recyclerViewCreated.visibility = View.VISIBLE
            recyclerViewScanned.visibility = View.VISIBLE

            // Reload the QR codes to make sure they're up-to-date
            adapterAll.updateList(updatedAllQRCodes)
        }

        // After updating the list, check if we need to adjust visibility based on the current data
        handleEmptyState(updatedAllQRCodes, dbHelper.getQRCodesByEntryType("created").toMutableList(),
            dbHelper.getQRCodesByEntryType("scanned").toMutableList(), selectedTabPosition)
    }

override fun onResume() {
    super.onResume()

    // Assuming you have the TabLayout and RecyclerViews set up
    // Get selectedTab from TabLayout or elsewhere
    val selectedTab = tabLayout.selectedTabPosition

    // Assuming you have the QR code lists
    val allQRCodes = dbHelper.getAllQRCodes()
    val createdQRCodes = dbHelper.getQRCodesByEntryType("created")
    val scannedQRCodes = dbHelper.getQRCodesByEntryType("scanned")

    // Check if the lists are empty
    val isAllEmpty = allQRCodes.isEmpty()
    val isCreatedEmpty = createdQRCodes.isEmpty()
    val isScannedEmpty = scannedQRCodes.isEmpty()

    // Call updateAdLayoutVisibility in HomeActivity
    val activity = requireActivity() as HomeActivity
    activity.updateAdLayoutVisibility(selectedTab, isAllEmpty, isCreatedEmpty, isScannedEmpty)

    // Set the top text and visibility for other elements
    val TopText: TextView = requireActivity().findViewById(R.id.mainText)
    TopText.visibility = View.VISIBLE
    TopText.text = getString(R.string.history)

    val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
    back?.visibility = View.VISIBLE

    val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
    download?.visibility = View.GONE

    // Set up back button
    back.setOnClickListener {
        requireActivity().onBackPressed()
    }
}
}




