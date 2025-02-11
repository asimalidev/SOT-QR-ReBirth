package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class NewOCR(private val language: Language) {

    enum class Language {
        LATIN, CHINESE, DEVANAGARI, JAPANESE, KOREAN
    }

    private val textRecognizer = when (language) {
        Language.LATIN -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        Language.CHINESE -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
        Language.DEVANAGARI -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        Language.JAPANESE -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
        Language.KOREAN -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    }

    suspend fun performOcr(bitmap: Bitmap): Map<Rect, Text.TextBlock> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val textResult = textRecognizer.process(image).await() // Use await to handle asynchronous task
        return extractTextBlocks(textResult)
    }

    private fun extractTextBlocks(text: Text): Map<Rect, Text.TextBlock> {
        val blockMap = mutableMapOf<Rect, Text.TextBlock>()

        for (textBlock in text.textBlocks) {
            val rect = textBlock.boundingBox
            if (rect != null) {
                blockMap[rect] = textBlock
            }
        }
        return blockMap
    }
}
