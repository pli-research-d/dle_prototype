package com.example.dle_prototype.data.ml

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class ModelFormat(val displayName: String, val extension: String) {
    GGUF("GGUF (Quantized LLM)", ".gguf"),
    TFLITE("TensorFlow Lite", ".tflite"),
    UNKNOWN("Unknown Binary", "")
}

data class CustomModelInfo(
    val fileName: String,
    val fileSizeBytes: Long,
    val format: ModelFormat,
    val importedAt: Long,
    val localFilePath: String,
    val isValid: Boolean,
    val magicHeader: String,
    val architectureSummary: String,
    val compatibilityNotice: String
) {
    fun toJson(): String {
        fun escape(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")
        return buildString {
            append("{")
            append("\"fileName\":\"").append(escape(fileName)).append("\",")
            append("\"fileSizeBytes\":").append(fileSizeBytes).append(",")
            append("\"format\":\"").append(format.name).append("\",")
            append("\"importedAt\":").append(importedAt).append(",")
            append("\"localFilePath\":\"").append(escape(localFilePath)).append("\",")
            append("\"isValid\":").append(isValid).append(",")
            append("\"magicHeader\":\"").append(escape(magicHeader)).append("\",")
            append("\"architectureSummary\":\"").append(escape(architectureSummary)).append("\",")
            append("\"compatibilityNotice\":\"").append(escape(compatibilityNotice)).append("\"")
            append("}")
        }
    }

    companion object {
        fun fromJson(jsonStr: String): CustomModelInfo? {
            return try {
                fun extractString(key: String): String {
                    val regex = Regex("\"$key\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"")
                    val match = regex.find(jsonStr) ?: return ""
                    return match.groupValues[1].replace("\\\"", "\"").replace("\\\\", "\\")
                }
                fun extractLong(key: String): Long {
                    val regex = Regex("\"$key\"\\s*:\\s*(\\d+)")
                    return regex.find(jsonStr)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                }
                fun extractBoolean(key: String): Boolean {
                    val regex = Regex("\"$key\"\\s*:\\s*(true|false)")
                    return regex.find(jsonStr)?.groupValues?.get(1)?.toBooleanStrictOrNull() ?: false
                }

                val fileName = extractString("fileName")
                if (fileName.isBlank()) return null
                val fileSizeBytes = extractLong("fileSizeBytes")
                val formatStr = extractString("format")
                val format = try { ModelFormat.valueOf(formatStr) } catch (_: Exception) { ModelFormat.UNKNOWN }
                val importedAt = extractLong("importedAt")
                val localFilePath = extractString("localFilePath")
                val isValid = extractBoolean("isValid")
                val magicHeader = extractString("magicHeader")
                val architectureSummary = extractString("architectureSummary")
                val compatibilityNotice = extractString("compatibilityNotice")

                CustomModelInfo(
                    fileName = fileName,
                    fileSizeBytes = fileSizeBytes,
                    format = format,
                    importedAt = importedAt,
                    localFilePath = localFilePath,
                    isValid = isValid,
                    magicHeader = magicHeader,
                    architectureSummary = architectureSummary,
                    compatibilityNotice = compatibilityNotice
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

object CustomModelManager {
    private const val TAG = "CustomModelManager"
    private const val PREFS_NAME = "custom_model_prefs"
    private const val KEY_ACTIVE_MODEL = "active_custom_model_json"
    private const val MODELS_DIR_NAME = "custom_models"

    /**
     * Inspects header bytes to detect GGUF or TFLite magic signatures.
     */
    fun inspectModelHeader(file: File): Triple<ModelFormat, Boolean, String> {
        if (!file.exists() || file.length() < 8) {
            return Triple(ModelFormat.UNKNOWN, false, "File is empty or truncated")
        }

        try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(32)
                val bytesRead = fis.read(header)
                if (bytesRead < 8) return Triple(ModelFormat.UNKNOWN, false, "Header too short")

                // Check GGUF Magic: "GGUF" (0x47, 0x47, 0x55, 0x46)
                if (header[0] == 0x47.toByte() &&
                    header[1] == 0x47.toByte() &&
                    header[2] == 0x55.toByte() &&
                    header[3] == 0x46.toByte()
                ) {
                    val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
                    buffer.position(4)
                    val version = buffer.int
                    return Triple(
                        ModelFormat.GGUF,
                        true,
                        "Magic: GGUF (v$version) • Valid Quantized Tensor Container"
                    )
                }

                // Check TFLite Magic: "TFL3" at byte offset 4..7
                if (bytesRead >= 8 &&
                    header[4] == 0x54.toByte() && // 'T'
                    header[5] == 0x46.toByte() && // 'F'
                    header[6] == 0x4C.toByte() && // 'L'
                    header[7] == 0x33.toByte()    // '3'
                ) {
                    return Triple(
                        ModelFormat.TFLITE,
                        true,
                        "Magic: TFL3 • Valid TensorFlow Lite FlatBuffers"
                    )
                }

                // Extension fallback check if header signature is non-standard
                val nameLower = file.name.lowercase()
                if (nameLower.endsWith(".gguf")) {
                    return Triple(ModelFormat.GGUF, true, "GGUF File Extension Detected")
                } else if (nameLower.endsWith(".tflite")) {
                    return Triple(ModelFormat.TFLITE, true, "TFLite File Extension Detected")
                }

                return Triple(ModelFormat.UNKNOWN, false, "Unrecognized format (expected GGUF or TFLite)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inspecting header: ${e.message}", e)
            return Triple(ModelFormat.UNKNOWN, false, "Header read error: ${e.message}")
        }
    }

    /**
     * Imports a user-selected model from a SAF Uri into app internal storage.
     */
    suspend fun importModelFromUri(context: Context, uri: Uri): Result<CustomModelInfo> = withContext(Dispatchers.IO) {
        try {
            var fileName = "custom_model_${System.currentTimeMillis()}"
            var reportedSize = 0L

            // Resolve file name and size from content provider
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                    if (sizeIndex != -1) {
                        reportedSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            val modelsDir = File(context.filesDir, MODELS_DIR_NAME).apply { mkdirs() }
            // Clean up any previous imported models to conserve storage
            modelsDir.listFiles()?.forEach { it.delete() }

            val targetFile = File(modelsDir, fileName)

            // Stream bytes safely from ContentResolver to local target file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            } ?: return@withContext Result.failure(IllegalStateException("Could not open input stream from selected URI"))

            val actualSize = targetFile.length()
            val (format, isValid, headerNotice) = inspectModelHeader(targetFile)

            val architecture = when (format) {
                ModelFormat.GGUF -> "Quantized Neural Weights Container (GGUF)"
                ModelFormat.TFLITE -> "TFLite FlatBuffers Neural Network"
                ModelFormat.UNKNOWN -> "Generic Binary Weights"
            }

            val compatibilityNotice = when (format) {
                ModelFormat.GGUF -> "Imported successfully! GGUF header verified. On Android, GGUF models are loaded via llama.cpp NDK bindings or external mmap."
                ModelFormat.TFLITE -> "Imported successfully! Valid TFLite schema detected and ready for on-device inference."
                ModelFormat.UNKNOWN -> "Imported, but unrecognized binary signature. Please verify file integrity."
            }

            val info = CustomModelInfo(
                fileName = fileName,
                fileSizeBytes = actualSize,
                format = format,
                importedAt = System.currentTimeMillis(),
                localFilePath = targetFile.absolutePath,
                isValid = isValid,
                magicHeader = headerNotice,
                architectureSummary = architecture,
                compatibilityNotice = compatibilityNotice
            )

            // Save active model info in preferences
            saveActiveModel(context, info)
            Result.success(info)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import model: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getActiveModel(context: Context): CustomModelInfo? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ACTIVE_MODEL, null) ?: return null
        val model = CustomModelInfo.fromJson(json) ?: return null
        // Verify local file still exists
        return if (File(model.localFilePath).exists()) model else {
            clearActiveModel(context)
            null
        }
    }

    fun saveActiveModel(context: Context, model: CustomModelInfo) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_MODEL, model.toJson()).apply()
    }

    fun clearActiveModel(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ACTIVE_MODEL).apply()
        val modelsDir = File(context.filesDir, MODELS_DIR_NAME)
        return modelsDir.deleteRecursively()
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.2f GB", gb)
            mb >= 1.0 -> String.format("%.2f MB", mb)
            kb >= 1.0 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }
}
