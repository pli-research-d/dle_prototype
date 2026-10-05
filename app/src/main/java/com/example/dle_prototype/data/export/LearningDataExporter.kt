package com.example.dle_prototype.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.dle_prototype.data.QuizAttempt
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Robust export engine for generating readable PDF documents and structured CSV files
 * capturing user daily learning summaries and historical quiz performance.
 */
object LearningDataExporter {

    private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"

    /**
     * Formats the export data into a clean, human-readable, RFC-4180 compliant CSV string.
     */
    fun generateCsv(bundle: LearningExportBundle): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val exportDateStr = dateFormat.format(Date(bundle.exportTimestamp))
        val sb = StringBuilder()

        // 1. Learning Profile Header
        sb.appendLine("================================================================================")
        sb.appendLine("NEURALPREP LEARNING PERFORMANCE & PROGRESS ARCHIVE")
        sb.appendLine("================================================================================")
        sb.appendLine("Learner Username,${escapeCsv(bundle.user.username)}")
        sb.appendLine("Generated At,${escapeCsv(exportDateStr)}")
        sb.appendLine("Active Daily Streak,${bundle.dailyStreak} days")
        sb.appendLine("All-Time Longest Streak,${bundle.longestStreak} days")
        sb.appendLine("Total Quizzes Completed,${bundle.quizPerformanceStats.totalQuizzes}")
        sb.appendLine("Total Questions Answered,${bundle.quizPerformanceStats.totalQuestionsAnswered}")
        sb.appendLine("Overall Average Accuracy,${"%.1f".format(bundle.quizPerformanceStats.averageAccuracyPercent)}%")
        sb.appendLine("Total Learning Points (XP),${bundle.quizPerformanceStats.totalScore * 10}")
        sb.appendLine()

        // 2. Today's Daily Learning Goal & Telemetry
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("DAILY LEARNING GOAL & TELEMETRY")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("Target Questions,${bundle.dailyGoalProgress.targetQuestions}")
        sb.appendLine("Answered Questions Today,${bundle.dailyGoalProgress.answeredToday}")
        sb.appendLine("Question Goal Completion,${"%.1f".format(bundle.dailyGoalProgress.percentComplete * 100)}%")
        sb.appendLine("Target Study Hours,${bundle.dailyGoalProgress.targetHours}h")
        sb.appendLine("Completed Study Hours Today,${"%.2f".format(bundle.dailyGoalProgress.hoursCompletedToday)}h (${"%.0f".format(bundle.dailyGoalProgress.minutesCompletedToday)} mins)")
        sb.appendLine("Goal Status,${if (bundle.dailyGoalProgress.isAchieved) "ACHIEVED ✓" else "IN PROGRESS"}")
        sb.appendLine()

        // 3. Peak Learning Hours Analysis
        bundle.peakLearningAnalysis?.let { peak ->
            sb.appendLine("--------------------------------------------------------------------------------")
            sb.appendLine("PEAK COGNITIVE LEARNING HOURS")
            sb.appendLine("--------------------------------------------------------------------------------")
            sb.appendLine("Optimal Peak Window,${escapeCsv(peak.peakWindowFormatted)}")
            sb.appendLine("Time of Day Label,${escapeCsv(peak.timeOfDayLabel)}")
            sb.appendLine("Peak Accuracy Rate,${"%.1f".format(peak.accuracyAtPeakPercent)}%")
            sb.appendLine("Cognitive Calibration,${if (peak.isCalibrated) "Calibrated" else "Preliminary"}")
            sb.appendLine("Recommendation,${escapeCsv(peak.recommendationMessage)}")
            sb.appendLine()
        }

        // 4. Study & Focus Sessions
        if (bundle.focusSessions.isNotEmpty()) {
            sb.appendLine("--------------------------------------------------------------------------------")
            sb.appendLine("STUDY & FOCUS SESSIONS")
            sb.appendLine("--------------------------------------------------------------------------------")
            sb.appendLine("Session ID,Date,Duration (Minutes),Target (Minutes),Pauses,Focus Score (%)")
            bundle.focusSessions.forEach { session ->
                val sDate = dateFormat.format(Date(session.timestamp))
                val durMin = session.durationSeconds / 60
                val focusPct = (session.focusScore * 100).toInt()
                sb.appendLine("${session.id},${escapeCsv(sDate)},$durMin,${session.targetMinutes},${session.pauseCount},$focusPct%")
            }
            sb.appendLine()
        }

        // 5. Unlocked Digital Badges
        if (bundle.digitalBadges.isNotEmpty()) {
            sb.appendLine("--------------------------------------------------------------------------------")
            sb.appendLine("UNLOCKED DIGITAL ACHIEVEMENTS")
            sb.appendLine("--------------------------------------------------------------------------------")
            sb.appendLine("Badge ID,Title,Category,Tier,XP Reward,Status,Unlocked At")
            bundle.digitalBadges.filter { it.isUnlocked }.forEach { badge ->
                val unlockDate = badge.unlockedAt?.let { dateFormat.format(Date(it)) } ?: "Earned"
                sb.appendLine("${badge.id},${escapeCsv(badge.title)},${badge.category.name},${badge.tier.name},${badge.xpReward},Unlocked,${escapeCsv(unlockDate)}")
            }
            sb.appendLine()
        }

        // 6. Complete Quiz Performance History Table
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("QUIZ PERFORMANCE HISTORY")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("Attempt #,Date & Time,Category,Score,Total Questions,Accuracy (%),Difficulty Level,Adaptive Tier")
        bundle.quizAttempts.forEachIndexed { index, attempt ->
            val attemptDate = dateFormat.format(Date(attempt.timestamp))
            val accuracy = if (attempt.totalQuestions > 0) (attempt.score.toFloat() / attempt.totalQuestions.toFloat()) * 100f else 0f
            val tier = when {
                attempt.difficultyLevel >= 2.3f -> "Hard"
                attempt.difficultyLevel >= 1.7f -> "Medium"
                else -> "Easy"
            }
            sb.appendLine("${index + 1},${escapeCsv(attemptDate)},${escapeCsv(attempt.category)},${attempt.score},${attempt.totalQuestions},${"%.1f".format(accuracy)}%,${"%.2f".format(attempt.difficultyLevel)},$tier")
        }

        return sb.toString()
    }

    /**
     * Generates a beautifully formatted, multi-page PDF document summarizing daily learning
     * and historical quiz performance using native Android PdfDocument.
     */
    fun generatePdf(context: Context, bundle: LearningExportBundle, outputFile: File): File {
        val document = PdfDocument()

        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points
        val margin = 36f // 0.5 inch margins
        val contentWidth = pageWidth - (margin * 2)

        val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
        val shortDateFormat = SimpleDateFormat("MM/dd/yy HH:mm", Locale.US)
        val exportDateStr = dateFormat.format(Date(bundle.exportTimestamp))

        // Paints for rendering
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 10f
            typeface = Typeface.DEFAULT
        }

        val sectionHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0284C7")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
            typeface = Typeface.DEFAULT
        }

        val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val smallMutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            typeface = Typeface.DEFAULT
        }

        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
        }

        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
        }

        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val rowAltBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1F5F9")
        }

        val cyanAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#06B6D4")
        }

        val emeraldAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#10B981")
        }

        val amberAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F59E0B")
        }

        // ==========================================
        // PAGE 1: Overview, Goals, Peak Hours & Top History
        // ==========================================
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var y = margin

        // --- TOP BANNER ---
        val bannerHeight = 70f
        val bannerRect = RectF(margin, y, pageWidth - margin, y + bannerHeight)
        canvas.drawRoundRect(bannerRect, 10f, 10f, headerBgPaint)

        // Banner Content
        val bannerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bannerSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 9f
            typeface = Typeface.DEFAULT
        }
        val bannerAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#22D3EE")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("NEURALPREP LEARNING PORTFOLIO", margin + 14f, y + 26f, bannerTitlePaint)
        canvas.drawText("Official Personal Archive • On-Device Adaptive Learning Telemetry", margin + 14f, y + 42f, bannerSubPaint)
        canvas.drawText("Learner: @${bundle.user.username}   •   Generated: $exportDateStr", margin + 14f, y + 57f, bannerAccentPaint)

        y += bannerHeight + 14f

        // --- 4 KPI SUMMARY CARDS ---
        val cardSpacing = 8f
        val cardWidth = (contentWidth - (cardSpacing * 3)) / 4f
        val cardHeight = 48f

        val kpiData = listOf(
            Triple("DAILY STREAK", "${bundle.dailyStreak}d 🔥", Color.parseColor("#F59E0B")),
            Triple("ACCURACY", "${bundle.quizPerformanceStats.averageAccuracyPercent.toInt()}%", Color.parseColor("#10B981")),
            Triple("TOTAL QUIZZES", "${bundle.quizPerformanceStats.totalQuizzes} 📚", Color.parseColor("#06B6D4")),
            Triple("TOTAL SCORE", "${bundle.quizPerformanceStats.totalScore * 10} pts", Color.parseColor("#8B5CF6"))
        )

        kpiData.forEachIndexed { i, (label, value, tint) ->
            val cardX = margin + (i * (cardWidth + cardSpacing))
            val rect = RectF(cardX, y, cardX + cardWidth, y + cardHeight)
            canvas.drawRoundRect(rect, 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(rect, 8f, 8f, cardBorderPaint)

            canvas.drawText(label, cardX + 10f, y + 17f, smallMutedPaint)
            val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = tint
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(value, cardX + 10f, y + 37f, valPaint)
        }

        y += cardHeight + 16f

        // --- SECTION 1: TODAY'S LEARNING PROGRESS & GOALS ---
        canvas.drawText("TODAY'S LEARNING PROGRESS & DAILY TARGETS", margin, y + 10f, sectionHeadingPaint)
        y += 18f

        val dailyCardHeight = 60f
        val dailyRect = RectF(margin, y, pageWidth - margin, y + dailyCardHeight)
        canvas.drawRoundRect(dailyRect, 8f, 8f, cardBgPaint)
        canvas.drawRoundRect(dailyRect, 8f, 8f, cardBorderPaint)

        // Left: Questions Target
        canvas.drawText("Daily Question Target:", margin + 14f, y + 20f, bodyBoldPaint)
        canvas.drawText(
            "${bundle.dailyGoalProgress.answeredToday} of ${bundle.dailyGoalProgress.targetQuestions} questions answered (${(bundle.dailyGoalProgress.percentComplete * 100).toInt()}%)",
            margin + 14f,
            y + 36f,
            bodyPaint
        )
        val statusText = if (bundle.dailyGoalProgress.isAchieved) "✓ Daily Target Met!" else "• In Progress"
        val statusColor = if (bundle.dailyGoalProgress.isAchieved) emeraldAccentPaint else amberAccentPaint
        canvas.drawText(statusText, margin + 14f, y + 50f, statusColor)

        // Right: Study Hours
        val col2X = margin + (contentWidth / 2f)
        canvas.drawText("Study Duration Target:", col2X, y + 20f, bodyBoldPaint)
        canvas.drawText(
            "${"%.1f".format(bundle.dailyGoalProgress.hoursCompletedToday)}h of ${bundle.dailyGoalProgress.targetHours}h completed (${bundle.dailyGoalProgress.minutesCompletedToday.toInt()} mins)",
            col2X,
            y + 36f,
            bodyPaint
        )
        canvas.drawText("Longest Active Streak: ${bundle.longestStreak} days", col2X, y + 50f, smallMutedPaint)

        y += dailyCardHeight + 14f

        // --- SECTION 2: PEAK COGNITIVE LEARNING HOURS ---
        bundle.peakLearningAnalysis?.let { peak ->
            canvas.drawText("OPTIMAL PEAK LEARNING WINDOW", margin, y + 10f, sectionHeadingPaint)
            y += 18f

            val peakCardHeight = 46f
            val peakRect = RectF(margin, y, pageWidth - margin, y + peakCardHeight)
            canvas.drawRoundRect(peakRect, 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(peakRect, 8f, 8f, cardBorderPaint)

            canvas.drawText("Optimal Study Window:", margin + 14f, y + 18f, bodyBoldPaint)
            val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0284C7")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("${peak.peakWindowFormatted} (${peak.timeOfDayLabel}) • Accuracy: ${peak.accuracyAtPeakPercent.toInt()}%", margin + 14f, y + 33f, windowPaint)

            val note = if (peak.recommendationMessage.isNotBlank()) peak.recommendationMessage.take(65) + "..." else "Consistently study during peak focus hours to accelerate retention."
            canvas.drawText(note, col2X, y + 26f, smallMutedPaint)

            y += peakCardHeight + 14f
        }

        // --- SECTION 3: QUIZ PERFORMANCE HISTORY TABLE ---
        canvas.drawText("RECENT QUIZ PERFORMANCE HISTORY", margin, y + 10f, sectionHeadingPaint)
        y += 18f

        // Table Header
        val colWidths = floatArrayOf(30f, 110f, 140f, 70f, 75f, 98f) // Total: 523 == contentWidth
        val tableHeaders = arrayOf("#", "Date & Time", "Subject / Category", "Score", "Accuracy", "Tier Reached")

        fun drawTableHeader(c: Canvas, headerY: Float) {
            val thRect = RectF(margin, headerY, pageWidth - margin, headerY + 18f)
            c.drawRect(thRect, headerBgPaint)
            val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            var curX = margin + 6f
            tableHeaders.forEachIndexed { idx, title ->
                c.drawText(title, curX, headerY + 12f, thPaint)
                curX += colWidths[idx]
            }
        }

        drawTableHeader(canvas, y)
        y += 20f

        val rowHeight = 16f
        val attempts = bundle.quizAttempts

        // Iterate through attempts and manage pagination
        var attemptIndex = 0
        while (attemptIndex < attempts.size) {
            // Check if we reached bottom of page
            if (y + rowHeight > pageHeight - margin - 20f) {
                // Draw footer for current page
                drawPageFooter(canvas, pageNumber, exportDateStr, pageWidth, pageHeight, margin, smallMutedPaint)
                document.finishPage(page)

                // Start new page
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = margin

                // Draw continued header
                canvas.drawText("QUIZ PERFORMANCE HISTORY (CONTINUED)", margin, y + 10f, sectionHeadingPaint)
                y += 18f
                drawTableHeader(canvas, y)
                y += 20f
            }

            val attempt = attempts[attemptIndex]
            val isEven = attemptIndex % 2 == 0
            if (isEven) {
                val rowRect = RectF(margin, y - 2f, pageWidth - margin, y + rowHeight - 2f)
                canvas.drawRect(rowRect, rowAltBgPaint)
            }

            val attemptDate = shortDateFormat.format(Date(attempt.timestamp))
            val accuracy = if (attempt.totalQuestions > 0) (attempt.score.toFloat() / attempt.totalQuestions.toFloat()) * 100f else 0f
            val tier = when {
                attempt.difficultyLevel >= 2.3f -> "Hard (Lv 3)"
                attempt.difficultyLevel >= 1.7f -> "Medium (Lv 2)"
                else -> "Easy (Lv 1)"
            }

            var cellX = margin + 6f
            canvas.drawText("${attemptIndex + 1}", cellX, y + 10f, smallMutedPaint)
            cellX += colWidths[0]
            canvas.drawText(attemptDate, cellX, y + 10f, bodyPaint)
            cellX += colWidths[1]
            canvas.drawText(attempt.category.take(22), cellX, y + 10f, bodyBoldPaint)
            cellX += colWidths[2]
            canvas.drawText("${attempt.score} / ${attempt.totalQuestions}", cellX, y + 10f, bodyPaint)
            cellX += colWidths[3]
            canvas.drawText("${accuracy.toInt()}%", cellX, y + 10f, if (accuracy >= 80f) emeraldAccentPaint else bodyPaint)
            cellX += colWidths[4]
            canvas.drawText(tier, cellX, y + 10f, bodyPaint)

            y += rowHeight
            attemptIndex++
        }

        // Draw footer on final page
        drawPageFooter(canvas, pageNumber, exportDateStr, pageWidth, pageHeight, margin, smallMutedPaint)
        document.finishPage(page)

        // Write document to file
        outputFile.parentFile?.mkdirs()
        val fos = FileOutputStream(outputFile)
        document.writeTo(fos)
        fos.flush()
        fos.close()
        document.close()

        return outputFile
    }

    private fun drawPageFooter(
        canvas: Canvas,
        pageNumber: Int,
        exportDateStr: String,
        pageWidth: Int,
        pageHeight: Int,
        margin: Float,
        paint: Paint
    ) {
        val footerY = pageHeight - margin + 10f
        canvas.drawText("NeuralPrep Personal Learning Record • Confidential • Generated $exportDateStr", margin, footerY, paint)
        val pageText = "Page $pageNumber"
        val pageTextWidth = paint.measureText(pageText)
        canvas.drawText(pageText, pageWidth - margin - pageTextWidth, footerY, paint)
    }

    /**
     * Writes either PDF or CSV to an existing OutputStream (e.g. from Storage Access Framework).
     */
    fun writeToOutputStream(
        context: Context,
        bundle: LearningExportBundle,
        format: ExportFormat,
        outputStream: OutputStream
    ) {
        when (format) {
            ExportFormat.CSV -> {
                val csvContent = generateCsv(bundle)
                outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            ExportFormat.PDF -> {
                val tempFile = File(context.cacheDir, "temp_export_${System.currentTimeMillis()}.pdf")
                generatePdf(context, bundle, tempFile)
                tempFile.inputStream().use { input ->
                    input.copyTo(outputStream)
                }
                outputStream.flush()
                tempFile.delete()
            }
        }
    }

    /**
     * Exports the learning bundle to the application's internal cache folder ready for sharing.
     */
    fun exportToCacheFile(
        context: Context,
        bundle: LearningExportBundle,
        format: ExportFormat
    ): File {
        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
        }

        val dateSuffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "NeuralPrep_${bundle.user.username}_Learning_Report_$dateSuffix.${format.extension}"
        val targetFile = File(exportsDir, filename)

        when (format) {
            ExportFormat.CSV -> {
                val csvData = generateCsv(bundle)
                targetFile.writeText(csvData, Charsets.UTF_8)
            }
            ExportFormat.PDF -> {
                generatePdf(context, bundle, targetFile)
            }
        }

        return targetFile
    }

    /**
     * Creates an Android Share Sheet Intent for the exported file using FileProvider.
     */
    fun createShareIntent(context: Context, file: File, format: ExportFormat): Intent {
        val authority = "${context.packageName}$FILE_PROVIDER_AUTHORITY_SUFFIX"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "NeuralPrep Learning Summary & Quiz Performance")
            putExtra(Intent.EXTRA_TEXT, "Here is my learning summary and quiz performance record from NeuralPrep.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(shareIntent, "Export Learning Data via")
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
