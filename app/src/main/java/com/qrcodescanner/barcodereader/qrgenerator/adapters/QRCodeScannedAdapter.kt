package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.database.OnQRCodeClickListener
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeData
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper

class QRCodeScannedAdapter(
    private var qrCodeList: MutableList<QRCodeData>,
    private val dbHelper: QRCodeDatabaseHelper,
    private val listener: OnQRCodeClickListener
) : RecyclerView.Adapter<QRCodeScannedAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val qrCodeTextView: TextView = itemView.findViewById(R.id.qrCodeTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val drawableImageView: ImageView = itemView.findViewById(R.id.drawableImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.qr_code_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val qrCodeData = qrCodeList[position]
        holder.qrCodeTextView.text = qrCodeData.qrCode
        holder.dateTextView.text = qrCodeData.date
        holder.timeTextView.text = qrCodeData.time
        holder.drawableImageView.setImageResource(qrCodeData.drawable)

        holder.itemView.setOnClickListener {
            listener.onQRCodeClick(qrCodeData.qrCode,"scanned")
        }
    }

    override fun getItemCount() = qrCodeList.size

    fun updateData(newList: List<QRCodeData>) {
        qrCodeList.clear()
        qrCodeList.addAll(newList)
        notifyDataSetChanged()
    }
}
