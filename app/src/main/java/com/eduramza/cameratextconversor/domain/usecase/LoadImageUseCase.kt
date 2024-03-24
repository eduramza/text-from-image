package com.eduramza.cameratextconversor.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.eduramza.cameratextconversor.domain.repository.ImageRepository

class LoadImageUseCase(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(context: Context, imageUri: Uri): Bitmap =
        imageRepository.loadImage(context, imageUri)
}