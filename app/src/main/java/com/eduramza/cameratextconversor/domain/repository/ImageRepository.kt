package com.eduramza.cameratextconversor.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

interface ImageRepository {
    suspend fun loadImage(context: Context, imageUri: Uri): Bitmap
    suspend fun analyzeImageText(bitmap: Bitmap): String
}