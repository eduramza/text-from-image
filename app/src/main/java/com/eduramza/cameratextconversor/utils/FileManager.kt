package com.eduramza.cameratextconversor.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.util.Log
import com.eduramza.cameratextconversor.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

interface FileManager {
    suspend fun saveToPDFFile(
        textFromImage: String,
        bitmapList: List<Bitmap>
    ): File?

    suspend fun saveTextToTXT(
        textFromImage: String
    ): File?

    fun shareContent(textFromImage: String)
}

class FileManagerImpl(
    private val context: Context
): FileManager{

    companion object{
        private const val PAGE_WIDTH = 794
        private const val PAGE_HEIGHT = 1123
    }
    private val appName: String by lazy { context.getString(R.string.app_name) }

    override fun shareContent(textFromImage: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textFromImage)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, "Share text using...")
            context.startActivity(shareIntent)
        } catch (ex: Exception){
            throw ex
        }
    }

    override suspend fun saveTextToTXT(textFromImage: String): File? {
        return try {
            val outputDir = getOutputDirectory()
            val currentDate = getCurrentDateTime()
            val fileName = "$appName-$currentDate.pdf"
            val file = File(outputDir, fileName)

            FileOutputStream(file).use {
                it.write(textFromImage.toByteArray())
            }

            updateMedia(file)
            file
        } catch (ex: Exception) {
            Log.e("SaveFile", ex.message.toString())
            null
        }
    }

    //region - PDF
    override suspend fun saveToPDFFile(textFromImage: String, bitmapList: List<Bitmap>): File? {
        return try {
            val outputDir = getOutputDirectory()
            val currentDate = getCurrentDateTime()
            val fileName = "$appName-$currentDate.pdf"
            val file = File(outputDir, fileName)
            val document = PdfDocument()

            saveImagesOnThePDF(document, bitmapList)

            val pagesToText = calculatePagesNeeded(
                text = textFromImage,
                textSize = 24f,
                paddingVertical = 24f
            )

            var remainingText = textFromImage
            repeat(pagesToText) { pgNumber ->
                val pageInfo =
                    PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH,
                        PAGE_HEIGHT, pgNumber + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                remainingText = drawTextOnPage(canvas, remainingText)

                document.finishPage(page)
            }

            val fileOutputStream = FileOutputStream(file)
            document.writeTo(fileOutputStream)
            fileOutputStream.close()


            document.close()
            updateMedia(file)

            file
        } catch (ex: Exception) {
            Log.e("SaveFile", ex.message.toString())
            null
        }
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

    private fun saveImagesOnThePDF(
        document: PdfDocument,
        bitmapList: List<Bitmap>
    ){
        bitmapList.forEachIndexed { _, bitmap ->
            val pageInfo = PdfDocument.PageInfo.Builder(
                PAGE_WIDTH,
                PAGE_HEIGHT, 1).create()
            val imagePage = document.startPage(pageInfo)
            val canvas = imagePage.canvas

            // Scale the bitmap to fit the page while maintaining aspect ratio
            val scaledBitmap =
                scaleBitmapToFitPage(bitmap)

            // Calculate the position to center the image on the page
            val x = (PAGE_WIDTH - scaledBitmap.width) / 2f
            val y = (PAGE_HEIGHT - scaledBitmap.height) / 2f

            // Draw the scaled bitmap onto the page
            canvas.drawBitmap(scaledBitmap, x, y, null)

            document.finishPage(imagePage)
        }
    }

    private fun scaleBitmapToFitPage(bitmap: Bitmap): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        val scaleFactor = min(
            PAGE_WIDTH.toFloat() / bitmapWidth.toFloat(),
            PAGE_HEIGHT.toFloat() / bitmapHeight.toFloat()
        )

        val scaledWidth = (bitmapWidth * scaleFactor).toInt()
        val scaledHeight = (bitmapHeight * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }
    //endregion

    private fun getOutputDirectory(): File {
        return context.getExternalFilesDir(appName) ?: File("/storage/emulated/0/$appName")
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-SSS", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun updateMedia(file: File) {
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { _, _ -> }
    }

}