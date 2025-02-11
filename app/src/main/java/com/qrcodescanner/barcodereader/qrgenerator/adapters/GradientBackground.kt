package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.utils.ImageItem

class GradientBackground(
    private val context: Context,
    private val imageItems: List<ImageItem>,
    private val listener: OnImageItemClickListener
) : RecyclerView.Adapter<GradientBackground.ImageViewHolder>() {

    interface OnImageItemClickListener {
        fun onImageItemClick(imageResId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_color, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageItem = imageItems[position]
        holder.imageView.setImageResource(imageItem.imageResId)

        holder.itemView.setOnClickListener {
            listener.onImageItemClick(imageItem.imageResId)
        }
    }

    override fun getItemCount(): Int = imageItems.size

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.colorView)
    }
}