package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.models.ScannedItem


class ScanListAdapter(
    private val scannedData: List<ScannedItem>,
    private val onItemClicked: (String, Boolean) -> Unit
) : RecyclerView.Adapter<ScanListAdapter.ScanViewHolder>() {

    class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val qrCodeTextView: TextView = itemView.findViewById(R.id.qrCodeTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val drawableImageView: ImageView = itemView.findViewById(R.id.drawableImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scan_result, parent, false)
        return ScanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val scannedItem = scannedData[position]
        holder.qrCodeTextView.text = scannedItem.data
        holder.dateTextView.text = scannedItem.date
        holder.timeTextView.text = scannedItem.time

        // Set the appropriate icon based on whether it's a QR code or barcode
        if (scannedItem.isQrCode) {
            holder.drawableImageView.setImageResource(R.drawable.create_code)
        } else {
            holder.drawableImageView.setImageResource(R.drawable.ic_barcode)
        }

        // Set the click listener
        holder.itemView.setOnClickListener {
            onItemClicked(scannedItem.data, scannedItem.isQrCode)
        }
    }

    override fun getItemCount(): Int {
        return scannedData.size
    }
}



