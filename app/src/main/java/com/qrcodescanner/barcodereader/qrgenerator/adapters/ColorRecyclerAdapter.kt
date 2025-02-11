package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.utils.ColorItem


class ColorRecyclerAdapter(
    private val context: Context,
    private val colorItems: List<ColorItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ColorRecyclerAdapter.ColorViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    interface OnItemClickListener {
        fun onItemClick(color: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val colorItem = colorItems[position]
        holder.colorImageView.setImageResource(colorItem.drawableResId)
        holder.itemView.isSelected = (selectedPosition == position)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position

            // Notify both previous and new selected positions to update their states
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)

            listener.onItemClick(colorItem.color)
        }
    }

    override fun getItemCount(): Int = colorItems.size

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorImageView: ImageView = itemView.findViewById(R.id.colorView)
    }
}


