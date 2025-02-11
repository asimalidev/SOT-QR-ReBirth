package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.models.SocialItem

class SocialAdapter(private val items: List<SocialItem>, private val onItemClick: (SocialItem) -> Unit) :
    RecyclerView.Adapter<SocialAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_social, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)
        private val textView: TextView = itemView.findViewById(R.id.textView)

        fun bind(item: SocialItem) {
            iconImageView.setImageResource(item.iconResId)
            textView.text = item.text

            itemView.setOnClickListener {
                onItemClick.invoke(item)
            }
        }
    }
}


