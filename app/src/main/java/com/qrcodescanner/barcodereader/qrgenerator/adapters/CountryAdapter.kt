package com.qrcodescanner.barcodereader.qrgenerator.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ItemCountryBinding
import com.qrcodescanner.barcodereader.qrgenerator.activities.PhotoTranslaterActivity
import com.qrcodescanner.barcodereader.qrgenerator.models.AllCountryModel


class CountryAdapter(
    private val originalCountryList: List<AllCountryModel>,
    private val context: Context
) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {

    private var filteredList: List<AllCountryModel> = originalCountryList
    private var selectedLanguage: AllCountryModel? = null
    private var emptyResultsListener: ((Boolean, String) -> Unit)? = null

    // Change listener type to lambda
    private var languageSelectedListener: ((AllCountryModel) -> Unit)? = null

    // Modify the method to accept a lambda
    fun setOnLanguageSelectedListener(listener: (AllCountryModel) -> Unit) {
        this.languageSelectedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = filteredList[position]
        holder.bind(country, country == selectedLanguage)
    }

    override fun getItemCount(): Int = filteredList.size

    fun filterList(query: String) {
        filteredList = if (query.isEmpty()) {
            originalCountryList
        } else {
            originalCountryList.filter {
                it.languageName.contains(query, ignoreCase = true)
            }
        }

        // Notify if filteredList is empty
        if (filteredList.isEmpty()) {
            emptyResultsListener?.invoke(true, query) // No results found
        } else {
            emptyResultsListener?.invoke(false, query) // Results found
        }

        // Handle selection update
        selectedLanguage?.let { selected ->
            if (!filteredList.contains(selected)) {
                selectedLanguage = null
            }
        }

        notifyDataSetChanged()
    }




    fun selectLanguage(country: AllCountryModel) {
        selectedLanguage = country
        notifyDataSetChanged()

        // Notify the listener
        languageSelectedListener?.invoke(country) // Using invoke for lambda

        // Save the selected language to SharedPreferences
        val sharedPreferences = context.getSharedPreferences("LanguagePreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        // Save the language name and code
        editor.putString("LANGUAGE_NAME", country.languageName)
        editor.putString("LANGUAGE_CODE", country.languageCode) // Assuming you have languageCode in your model
        editor.putInt("SELECTED_FLAG_RES_ID", country.flagResId) // Save flag resource ID


        editor.apply() // Commit the changes
        editor.apply() // Apply changes

        // Optionally, if you want to navigate to the next activity, create an Intent
        val intent = Intent(context, PhotoTranslaterActivity::class.java)
        context.startActivity(intent)
    }

    fun setOnEmptyResultsListener(listener: (Boolean, String) -> Unit) {
        this.emptyResultsListener = listener
    }

    inner class CountryViewHolder(private val binding: ItemCountryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(country: AllCountryModel, isSelected: Boolean) {
            binding.tvLanguage.text = country.languageName
            binding.ivFlag.setImageResource(country.flagResId)

            if (isSelected) {
                binding.root.setBackgroundResource(R.drawable.selected_background)
            } else {
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
            }

            binding.root.setOnClickListener {
                selectLanguage(country) // This now starts the PhotoTranslaterActivity
            }
        }
    }
}

