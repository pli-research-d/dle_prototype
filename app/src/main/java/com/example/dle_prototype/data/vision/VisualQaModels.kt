package com.example.dle_prototype.data.vision

import android.graphics.Bitmap

/**
 * Origin of the captured or uploaded educational image.
 */
enum class VisualQaSourceType(val displayName: String) {
    CAMERA_CAPTURE("Camera Capture"),
    GALLERY_UPLOAD("Uploaded Image"),
    SAMPLE_PRESET("Curated Study Preset")
}

/**
 * Rich multimodal QA source elevating questions with YOLO11n visual detections + OCR text.
 */
data class VisualQaSource(
    val id: String,
    val title: String,
    val sourceType: VisualQaSourceType,
    val bitmap: Bitmap,
    val imageUri: String? = null,
    val yoloResult: Yolo11nInferenceResult,
    val ocrResult: OcrResult,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalElementsDetected: Int get() = yoloResult.detections.size
    val totalWordsExtracted: Int get() = ocrResult.wordCount
    val totalFormulas: Int get() = ocrResult.detectedFormulas.size
    val totalQuestions: Int get() = ocrResult.detectedQuestions.size

    val visualTags: List<String> get() = yoloResult.detections
        .map { it.label }
        .distinct()

    /**
     * Concise summary badge text for the chat UI.
     */
    val badgeSummary: String get() = "YOLO11n: ${yoloResult.detections.size} regions • OCR: ${ocrResult.wordCount} words"

    /**
     * Formulates an elevated study prompt that combines YOLO11n visual insights and OCR text.
     */
    fun buildElevatedPrompt(userCustomQuery: String? = null): String {
        val sb = StringBuilder()
        sb.append("📸 **[ELEVATED VISUAL QA SOURCE: YOLO11n + OCR]**\n")
        sb.append("**Source**: ").append(title).append(" (").append(sourceType.displayName).append(")\n")
        sb.append("**Visual Regions (YOLO11n)**: ")
        val classCounts = yoloResult.detections.groupingBy { it.displayName }.eachCount()
        sb.append(classCounts.entries.joinToString(", ") { "${it.value}x ${it.key}" })
        sb.append("\n\n")

        sb.append("📋 **Extracted OCR Text**:\n")
        sb.append("```text\n")
        sb.append(ocrResult.fullText.trim())
        sb.append("\n```\n\n")

        if (!userCustomQuery.isNullOrBlank()) {
            sb.append("❓ **Student Inquiry**:\n")
            sb.append(userCustomQuery.trim())
        } else {
            sb.append("💡 **Study Request**:\n")
            sb.append("Please analyze this visual study material step-by-step. Break down the detected diagrams and formulas, solve any included questions, and provide key conceptual takeaways.")
        }

        return sb.toString()
    }
}

/**
 * Curated educational document preset for instant testing of YOLO11n + OCR.
 */
data class VisualQaPreset(
    val id: String,
    val title: String,
    val subject: String,
    val description: String,
    val rawText: String,
    val detectedClasses: List<Yolo11nClass>
)
