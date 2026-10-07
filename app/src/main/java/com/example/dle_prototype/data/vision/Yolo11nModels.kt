package com.example.dle_prototype.data.vision

import android.graphics.RectF
import androidx.compose.ui.graphics.Color

/**
 * Visual element classes detected by the YOLO11n (Nano) educational vision engine.
 */
enum class Yolo11nClass(
    val classIndex: Int,
    val label: String,
    val displayName: String,
    val color: Color
) {
    QUESTION_STATEMENT(0, "question_statement", "Question Statement", Color(0xFFA855F7)), // Purple
    MATH_FORMULA(1, "math_formula", "Math Formula", Color(0xFFF59E0B)),          // Amber
    CODE_BLOCK(2, "code_block", "Code Block", Color(0xFF06B6D4)),               // Cyan
    DIAGRAM(3, "diagram", "System Diagram", Color(0xFF10B981)),                 // Emerald
    TABLE_DATA(4, "table_data", "Table / Matrix", Color(0xFFEC4899)),           // Pink
    HEADING_TITLE(5, "heading_title", "Section Title", Color(0xFF6366F1)),       // Indigo
    ILLUSTRATION(6, "illustration", "Figure / Plot", Color(0xFF38BDF8)),         // Sky Blue
    DOCUMENT(7, "document", "Study Document", Color(0xFF94A3B8));                // Slate

    companion object {
        fun fromIndex(index: Int): Yolo11nClass = entries.getOrNull(index) ?: DOCUMENT
        fun fromLabel(label: String): Yolo11nClass = entries.find { it.label.equals(label, ignoreCase = true) } ?: DOCUMENT
    }
}

/**
 * Normalized bounding box coordinates [0.0f .. 1.0f] relative to image dimensions.
 */
data class NormalizedBoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
    val centerX: Float get() = left + width / 2f
    val centerY: Float get() = top + height / 2f

    fun toRectF(imageWidth: Float, imageHeight: Float): RectF {
        return RectF(
            left * imageWidth,
            top * imageHeight,
            right * imageWidth,
            bottom * imageHeight
        )
    }

    fun intersects(other: NormalizedBoundingBox): Boolean {
        return left < other.right && right > other.left && top < other.bottom && bottom > other.top
    }
}

/**
 * Single detected object / visual region produced by YOLO11n Nano.
 */
data class Yolo11nDetection(
    val id: String,
    val yoloClass: Yolo11nClass,
    val confidence: Float,
    val box: NormalizedBoundingBox,
    val extractedText: String? = null,
    val attributes: Map<String, String> = emptyMap()
) {
    val displayName: String get() = yoloClass.displayName
    val label: String get() = yoloClass.label
    val color: Color get() = yoloClass.color
    val confidencePercent: Int get() = (confidence * 100).toInt()
}

/**
 * Full inference result from the YOLO11n Nano edge vision model.
 */
data class Yolo11nInferenceResult(
    val detections: List<Yolo11nDetection>,
    val latencyMs: Long,
    val imageWidth: Int,
    val imageHeight: Int,
    val modelArchitecture: String = "Ultralytics YOLO11n (2.6M params)",
    val inputResolution: String = "640x640 Nano Tensor",
    val nmsIouThreshold: Float = 0.45f,
    val confidenceThreshold: Float = 0.50f
)
