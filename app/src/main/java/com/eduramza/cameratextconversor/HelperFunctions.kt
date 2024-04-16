package com.eduramza.cameratextconversor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun getUriForFile(context: Context, file: File): Uri {
    val uri = FileProvider.getUriForFile(context, "com.eduramza.cameratextconversor.provider", file)
    Log.d("FileProviderDebug", "Generated Uri: $uri")
    return uri
}

suspend fun loadBitmap(context: Context, imageUri: Uri): Bitmap {
    return withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw IOException("Failed to load bitmap from Uri")
    }
}

fun saveLocalPDF(context: Context, pdf: GmsDocumentScanningResult.Pdf) {
    val fos = FileOutputStream(File(context.filesDir, "scan.pdf"))
    context.contentResolver.openInputStream(pdf.uri)?.use {
        it.copyTo(fos)
    }
}