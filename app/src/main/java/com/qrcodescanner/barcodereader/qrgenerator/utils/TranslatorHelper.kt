package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.app.Activity
import com.google.android.gms.tasks.Tasks
import android.content.Context
import android.graphics.Rect
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.vision.text.Text


class TranslatorHelper(private val context: Context) {

    private var sourceOptions: TranslatorOptions
    private var sourceTranslator: Translator
    private val remoteModelManager = RemoteModelManager.getInstance()

    // AlertDialog for download progress
    private var progressDialog: AlertDialog? = null

    init {
        // Initialize the source (default source language)
        sourceOptions = TranslatorOptions.Builder()
            .setSourceLanguage(
                TranslateLanguage.fromLanguageTag("es").toString()
            ) // Default source language
            .setTargetLanguage(TranslateLanguage.ENGLISH) // Default target language
            .build()

        sourceTranslator = Translation.getClient(sourceOptions)
    }


    private fun translateText(
        text: String,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ): String {
        if (sourceLanguageCode == "und") {
            return text
        }

        // Ensure the source and target language models are downloaded
        downloadModelIfNeeded(sourceLanguageCode)
        downloadModelIfNeeded(targetLanguageCode)

        // Set the source and target languages dynamically
        sourceOptions = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(sourceLanguageCode).toString())
            .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLanguageCode).toString())
            .build()

        // Initialize the translator with the updated options
        sourceTranslator = Translation.getClient(sourceOptions)

        // Translate text
        val task = sourceTranslator.translate(text)
        return Tasks.await(task)
    }

    private fun downloadModelIfNeeded(languageCode: String) {
        // Validate and convert the language code
        val validLanguageCode = TranslateLanguage.fromLanguageTag(languageCode)
            ?: throw IllegalArgumentException("Invalid or unsupported language code: $languageCode")

        // Build the remote model
        val model = TranslateRemoteModel.Builder(validLanguageCode).build()
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        // Check if the model is already downloaded
        if (!isModelDownloaded(validLanguageCode)) {
            // Download the model
            val downloadTask = remoteModelManager.download(model, conditions)
            try {
                Tasks.await(downloadTask)
                Log.d("TranslatorHelper", "Model for $validLanguageCode downloaded successfully.")
            } catch (e: Exception) {
                // Handle download failure
                Log.e("TranslatorHelper", "Failed to download model for $validLanguageCode: ${e.message}")
                throw RuntimeException("Failed to download translation model for $validLanguageCode: ${e.message}")
            }
        } else {
            Log.d("TranslatorHelper", "Model for $validLanguageCode already downloaded.")
        }
    }


    fun releaseTranslator() {
        sourceTranslator.close()
    }


    fun isModelDownloaded(languageCode: String): Boolean {
        val validLanguageCode = TranslateLanguage.fromLanguageTag(languageCode)
            ?: throw IllegalArgumentException("Invalid language code: $languageCode")

        val model = TranslateRemoteModel.Builder(validLanguageCode).build()
        val task = remoteModelManager.isModelDownloaded(model)
        return Tasks.await(task)
    }


    // Download translation model for the given language code
    fun downloadModel(languageCode: String) {
        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
        }

        Handler(Looper.getMainLooper()).post {
            progressDialog = AlertDialog.Builder(context)
                .setTitle("Downloading Translation Model (Requires Wi-Fi connection)")
                .setCancelable(false)
                .setView(progressBar)
                .show()
        }

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        val model = TranslateRemoteModel.Builder(languageCode).build()
        val downloadTask = remoteModelManager.download(model, conditions)

        try {
            Tasks.await(downloadTask)
            Handler(Looper.getMainLooper()).post {
                progressDialog?.dismiss()
                progressDialog = null
            }
            showDownloadToast("Translation Model Downloaded Successfully")
        } catch (e: Exception) {
            // Dismiss the progress dialog on download failure
            Handler(Looper.getMainLooper()).post {
                progressDialog?.dismiss()
                progressDialog = null
            }
            showDownloadToast("Translation Model Download Failed: ${e.message}")
        }
    }

    private fun showDownloadToast(message: String) {
        (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

//    // Detect and translate text from source to target language
//    fun detectAndTranslate(text: String, sourceLanguageCode: String, targetLanguageCode: String): String {
//        return try {
//            translateText(text, sourceLanguageCode, targetLanguageCode)
//        } catch (e: Exception) {
//            "Translation failed: ${e.message}"
//        }
//    }

    fun detectAndTranslate(
        text: String,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ): String {
        return try {
            translateText(text, sourceLanguageCode, targetLanguageCode)
        } catch (e: Exception) {
            Log.e("TranslatorHelper", "Translation failed: ${e.message}", e)
            "Translation failed: ${e.message}"
        }
    }


    // Translate OCR results from source to target language
    fun translateOcrResult(
        ocrResult: Map<Rect, Text.TextBlock>,
        sourceLanguageCode: String,
        targetLanguageCode: String
    ): Map<Rect, String> {
        // Create a map to store the translated result
        val translatedResult = mutableMapOf<Rect, String>()

        // Iterate through the OCR result
        for ((rect, textBlock) in ocrResult) {
            // Translate the textBlock text to the desired target language
            val translatedText =
                translateText(textBlock.text, sourceLanguageCode, targetLanguageCode)
            // Add the translated text to the map
            translatedResult[rect] = translatedText
        }
        return translatedResult
    }
}


