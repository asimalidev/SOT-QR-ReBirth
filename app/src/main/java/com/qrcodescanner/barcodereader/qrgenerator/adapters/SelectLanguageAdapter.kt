package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.utils.AppLanguages
import com.qrcodescanner.barcodereader.qrgenerator.utils.PrefHelper

class SelectLanguageAdapter(
    private val languages: List<AppLanguages>,
    var ctx: Context,
    private val onItemClickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<SelectLanguageAdapter.LanguageViewHolder>() {

    private var selectedItem = RecyclerView.NO_POSITION
    lateinit var prefHelper: PrefHelper

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.language_listview_row, parent, false)

        prefHelper = PrefHelper(ctx)

        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.flagImageView.setImageDrawable(language.flagDrawable)
        holder.languageTextView.text = language.name
        holder.selectionStatus.setImageDrawable(language.selection)

        val savedPosition = prefHelper.getStringDefault("languagePosition", "-1")?.toInt() ?: -1

        if (savedPosition == position || selectedItem == position) {
            holder.selectedBackground.setBackgroundResource(R.drawable.locale_selected)
            holder.selectionStatus.setBackgroundResource(R.drawable.selected_radio)
        } else {
            holder.selectedBackground.setBackgroundResource(R.drawable.locale_unselected)
            holder.selectionStatus.setBackgroundResource(R.drawable.unselect_radio)
        }

        holder.itemView.setOnClickListener {
            val previousSelectedItem = prefHelper.getStringDefault("languagePosition", "-1")?.toInt() ?: -1
            prefHelper.putString("languagePosition", position.toString())

            notifyItemChanged(previousSelectedItem)
            prefHelper.getStringDefault("languagePosition", "-1")?.toInt()?.let { notifyItemChanged(it) }
            onItemClickListener(position)
        }
    }

    override fun getItemCount(): Int = languages.size

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectedBackground: ConstraintLayout = itemView.findViewById(R.id.selectedBackground)
        val flagImageView: ImageView = itemView.findViewById(R.id.ivCountryFlagLanguage)
        val selectionStatus: ImageView = itemView.findViewById(R.id.ivUnSelected)
        val languageTextView: TextView = itemView.findViewById(R.id.tvCountryNameLanguage)
    }
}
