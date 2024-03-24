package com.eduramza.cameratextconversor.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.eduramza.cameratextconversor.domain.repository.ImageRepository
import com.google.mlkit.vision.text.TextRecognizer

class ImageRepositoryImpl(
    private val textRecognizer: TextRecognizer
) : ImageRepository {
    override suspend fun loadImage(context: Context, imageUri: Uri): Bitmap {
        TODO("Not yet implemented")
    }

    override suspend fun analyzeImageText(bitmap: Bitmap): String {
        TODO("Not yet implemented")
    }
}