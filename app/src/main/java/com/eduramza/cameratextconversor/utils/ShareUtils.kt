package com.eduramza.cameratextconversor.utils

import android.content.Context
import android.content.Intent

object ShareUtils {
    fun shareContent(analyzedText: String, context: Context) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, analyzedText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share text using...")
        context.startActivity(shareIntent)
    }
}