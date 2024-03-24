package com.eduramza.cameratextconversor.domain.usecase

import android.graphics.Bitmap
import com.eduramza.cameratextconversor.domain.repository.ImageRepository

class AnalyzeImageTextUseCase(private val repository: ImageRepository) {
    suspend operator fun invoke(bitmap: Bitmap): String =
        repository.analyzeImageText(bitmap)
}