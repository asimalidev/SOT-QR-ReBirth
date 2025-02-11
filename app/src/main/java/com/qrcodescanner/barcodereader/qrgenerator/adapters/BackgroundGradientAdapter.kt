package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.utils.GradientItem


class BackgroundGradientAdapter(
    private val context: Context,
    private val gradientItems: List<GradientItem>,
    private val listener: onGradientColorItemClick
) : RecyclerView.Adapter<BackgroundGradientAdapter.GradientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradientViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_color, parent, false)
        return GradientViewHolder(view)
    }

    override fun onBindViewHolder(holder: GradientViewHolder, position: Int) {
        val gradientItem = gradientItems[position]
        holder.bind(gradientItem)
    }

    override fun getItemCount(): Int = gradientItems.size

    inner class GradientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gradientImageView: ImageView = itemView.findViewById(R.id.colorView)

        fun bind(item: GradientItem) {
            // Set the gradient drawable resource
            gradientImageView.setBackgroundResource(item.gradientResId)

            // Handle click event
            itemView.setOnClickListener {
                listener.onGradientColorItemClick(item.colors)
            }
        }
    }

    interface onGradientColorItemClick{
        fun onGradientColorItemClick(colors: IntArray)
    }
}

