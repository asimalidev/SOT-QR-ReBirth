package com.qrcodescanner.barcodereader.qrgenerator.database

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R


class QRCodeAdapter(
    private var qrCodeList: MutableList<QRCodeData>,
    private val dbHelper: QRCodeDatabaseHelper,
    private val listener: OnQRCodeClickListener,
) : RecyclerView.Adapter<QRCodeAdapter.ViewHolder>() {

    private var selectedItemPosition: Int? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val qrCodeTextView: TextView = itemView.findViewById(R.id.qrCodeTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val drawableImageView: ImageView = itemView.findViewById(R.id.drawableImageView)
        val deleteButton: ImageView = itemView.findViewById(R.id.btndelete)
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

        if (selectedItemPosition == position) {
            holder.itemView.setBackgroundColor(Color.LTGRAY) // Highlight selected item
            holder.deleteButton.visibility = View.VISIBLE
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT) // Reset background
            holder.deleteButton.visibility = View.GONE
        }

        holder.itemView.setOnLongClickListener {
            selectedItemPosition = if (selectedItemPosition == position) null else position
            notifyDataSetChanged()
            true
        }

        holder.itemView.setOnClickListener {
            listener.onQRCodeClick(qrCodeData.qrCode,"all")
        }

        holder.deleteButton.setOnClickListener {
            val itemToDelete = qrCodeList[position]
            dbHelper.deleteQRCode(itemToDelete.qrCode) // Delete from the database
            qrCodeList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, qrCodeList.size)
            selectedItemPosition = null
            notifyDataSetChanged()
            listener.onDelete(qrCodeList.isEmpty())
        }
    }

    fun updateList(newList: List<QRCodeData>) {
        qrCodeList.clear()
        qrCodeList.addAll(newList)
        notifyDataSetChanged() // Notify adapter to refresh the list
    }


    override fun getItemCount() = qrCodeList.size
}




