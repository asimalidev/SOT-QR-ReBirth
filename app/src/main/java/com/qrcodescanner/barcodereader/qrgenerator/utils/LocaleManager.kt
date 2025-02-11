package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import java.util.Locale


object LocaleManager {

    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.resources.updateConfiguration(
            config,
            context.resources.displayMetrics
        )
    }

    fun applySavedLocale(context: Context) {
        val prefHelper = PrefHelper(context)
        val savedLanguage = prefHelper.getStringDefault("language", "en")
        setLocale(context, savedLanguage ?: "en")
    }

    fun restartActivity(context: Context, activityClass: Class<*>) {
        val intent = Intent(context, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }


    fun onAttach(context: Context, currentLanguage: String?, defaultLanguage: String): Context {
        return setLocaleBase(context, currentLanguage!!)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String? {
        val prefHelper = PrefHelper(context)
        return prefHelper.getStringDefault("language", defaultLanguage)
    }

    fun setLocaleBase(context: Context, language: String): Context {
        return updateResources(context, language)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

}