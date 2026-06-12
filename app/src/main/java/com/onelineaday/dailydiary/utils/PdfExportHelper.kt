package com.onelineaday.dailydiary.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.onelineaday.dailydiary.data.JournalEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

object PdfExportHelper {

    suspend fun generateJournalPdf(context: Context, entries: List<JournalEntry>): Uri? = withContext(Dispatchers.IO) {
        if (entries.isEmpty()) return@withContext null

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size

        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val datePaint = TextPaint().apply {
            color = Color.DKGRAY
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")

        var currentPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var currentY = 50f
        val marginX = 50f
        val maxWidth = pageInfo.pageWidth - (marginX * 2).toInt()
        val bottomMargin = 50f

        fun newPage() {
            currentPage?.let { pdfDocument.finishPage(it) }
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage?.canvas
            currentY = 50f
            
            // Draw Title on First Page only
            if (pdfDocument.pages.size == 1) {
                val title = "My One Line A Day Journal"
                canvas?.drawText(title, marginX, currentY, titlePaint)
                currentY += 60f
            }
        }

        newPage()

        for (entry in entries) {
            val dateStr = "${entry.date.format(dateFormatter)} ${entry.mood.emoji}"
            var content = entry.content.trim()
            if (content.isEmpty()) {
                val hasPhoto = entry.photoUri != null
                val hasAudio = File(context.filesDir, "audio_${entry.date}.m4a").exists()
                if (hasPhoto && hasAudio) content = "[Photo and Audio Memory]"
                else if (hasPhoto) content = "[Photo Memory]"
                else if (hasAudio) content = "[Audio Memory]"
                else content = "[Empty Entry]"
            }

            val staticLayout = StaticLayout.Builder.obtain(content, 0, content.length, textPaint, maxWidth)
                .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1f, 1.2f)
                .setIncludePad(false)
                .build()

            val entryHeight = 30f + staticLayout.height + 20f // Date + Text + Spacing

            if (currentY + entryHeight > pageInfo.pageHeight - bottomMargin) {
                newPage()
            }

            canvas?.drawText(dateStr, marginX, currentY, datePaint)
            currentY += 20f

            canvas?.save()
            canvas?.translate(marginX, currentY)
            staticLayout.draw(canvas)
            canvas?.restore()

            currentY += staticLayout.height + 30f
        }

        currentPage?.let { pdfDocument.finishPage(it) }

        val pdfFile = File(context.cacheDir, "OneLineADay_Journal.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()
            return@withContext FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return@withContext null
        }
    }
}
