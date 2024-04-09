package com.eduramza.cameratextconversor.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

object FileUtils {

    private const val PAGE_WIDTH = 794
    private const val PAGE_HEIGHT = 1123

    fun saveTextToTxt(analyzedText: String, appName: String, context: Context, onSuccess: (File) -> Unit, onError: (Exception) -> Unit) {
        try {
            val outputDir = getOutputDirectory(context, appName)
            val currentDate = getCurrentDateTime()
            val fileName = "$appName-$currentDate.pdf"
            val file = File(outputDir, fileName)

            FileOutputStream(file).use {
                it.write(analyzedText.toByteArray())
            }

            updateMedia(context, file)
            onSuccess(file)
        } catch (ex: Exception) {
            Log.e("SaveFile", ex.message.toString())
            onError(ex)
        }
    }

    //region - PDF
    fun saveTextToPdf(
        textFromImage: String,
        appName: String,
        bitmapList: List<Bitmap>,
        context: Context,
        onSuccess: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val outputDir = getOutputDirectory(context, appName)
            val currentDate = getCurrentDateTime()
            val fileName = "$appName-$currentDate.pdf"
            val file = File(outputDir, fileName)
            val document = PdfDocument()

            saveImagesOnThePdf(document, bitmapList, file)

            val pagesToText = calculatePagesNeeded(
                text = textFromImage,
                textSize = 24f,
                paddingVertical = 24f
            )

            var remainingText = textFromImage
            repeat(pagesToText) { pgNumber ->
                val pageInfo =
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pgNumber + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                remainingText = drawTextOnPage(canvas, remainingText)

                document.finishPage(page)
            }

            val fileOutputStream = FileOutputStream(file)
            document.writeTo(fileOutputStream)
            fileOutputStream.close()

            document.close()
            updateMedia(context, file)

            onSuccess(file)
        } catch (ex: Exception) {
            Log.e("SaveFile", ex.message.toString())
            onError(ex)
        }
    }

    private fun saveImagesOnThePdf(
        document: PdfDocument,
        bitmapList: List<Bitmap>,
        file: File
    ) {

        bitmapList.forEachIndexed { _, bitmap ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val imagePage = document.startPage(pageInfo)
            val canvas = imagePage.canvas

            // Scale the bitmap to fit the page while maintaining aspect ratio
            val scaledBitmap = scaleBitmapToFitPage(bitmap, PAGE_WIDTH, PAGE_HEIGHT)

            // Calculate the position to center the image on the page
            val x = (PAGE_WIDTH - scaledBitmap.width) / 2f
            val y = (PAGE_HEIGHT - scaledBitmap.height) / 2f

            // Draw the scaled bitmap onto the page
            canvas.drawBitmap(scaledBitmap, x, y, null)

            document.finishPage(imagePage)
        }

        Log.d("SaveFile", "PDF file saved at ${file.absolutePath}")
    }

    private fun calculatePagesNeeded(
        text: String,
        textSize: Float,
        paddingVertical: Float
    ): Int {
        val paint = Paint()
        paint.textSize = textSize

        val textLines = text.split("\n")
        var totalLines = 0
        var currentPageLines = 0

        textLines.forEach { line ->
            val lineWidth = paint.measureText(line)
            val lineHeight = textSize

            // Calculate lines that fit on the current page
            val linesOnCurrentPage = ((PAGE_HEIGHT - 2 * paddingVertical) / lineHeight).toInt()
            if (currentPageLines + 1 <= linesOnCurrentPage) {
                currentPageLines++
            } else {
                // Move to the next page
                totalLines++
                currentPageLines = 1
            }

            // Check if line wraps to the next line
            if (lineWidth > PAGE_WIDTH) {
                val wrappedLines = (lineWidth / PAGE_WIDTH).toInt() + 1
                currentPageLines += wrappedLines - 1 // Subtract 1 for the original line
            }
        }

        // Account for the last page
        if (currentPageLines > 0) {
            totalLines++
        }

        return totalLines
    }

    private fun drawTextOnPage(
        canvas: Canvas,
        text: String,
        textSize: Float = 24f,
        paddingVertical: Float = 20f,
        paddingHorizontal: Float = 20f
    ): String {
        val paint = Paint()
        paint.textSize = textSize

        val textLines = text.split("\n")

        var yPosition = paddingVertical + textSize

        val remainingLines = mutableListOf<String>()

        textLines.forEach { line ->
            // Check if there is enough space vertically for the next line
            if (yPosition + textSize <= PAGE_HEIGHT - paddingVertical) {
                // Calculate the position to align the text to the left
                val xPosition = paddingHorizontal

                // Draw the text
                canvas.drawText(line, xPosition, yPosition, paint)
                yPosition += textSize + 5 // Adjust spacing between lines
            } else {
                // Store the remaining lines
                remainingLines.add(line)
            }
        }

        // Return the remaining text
        return remainingLines.joinToString("\n")
    }

    private fun scaleBitmapToFitPage(bitmap: Bitmap, pageWidth: Int, pageHeight: Int): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        val scaleFactor = min(
            pageWidth.toFloat() / bitmapWidth.toFloat(),
            pageHeight.toFloat() / bitmapHeight.toFloat()
        )

        val scaledWidth = (bitmapWidth * scaleFactor).toInt()
        val scaledHeight = (bitmapHeight * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }
    //endregion

    private fun getOutputDirectory(context: Context, pathName: String): File {
        return context.getExternalFilesDir(pathName) ?: File("/storage/emulated/0/$pathName")
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-SSS", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun updateMedia(context: Context, file: File) {
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { _, _ -> }
    }
}