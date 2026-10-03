package com.example.dle_prototype.data.ml

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

enum class LogCategory(val label: String) {
    ALL("All Events"),
    CHECKPOINT("Checkpoints"),
    DATA_QUALITY("Data Quality"),
    PARAM_ADJUSTMENT("Hyperparameters"),
    CONVERGENCE("Convergence"),
    EARLY_STOPPING("Early Stopping"),
    SYSTEM("System")
}

data class TrainingLogEntry(
    val id: Long = 0,
    val username: String,
    val sessionId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val category: LogCategory = LogCategory.SYSTEM,
    val title: String,
    val message: String,
    val detailsJson: String = "{}"
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formattedDateTime(): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun toJson(): String {
        val escapedTitle = title.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        val escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        val details = if (detailsJson.isBlank() || !detailsJson.trim().startsWith("{")) "{}" else detailsJson.trim()
        return """{
  "id": $id,
  "username": "$username",
  "sessionId": "$sessionId",
  "timestamp": $timestamp,
  "timeFormatted": "${formattedDateTime()}",
  "level": "${level.name}",
  "category": "${category.name}",
  "title": "$escapedTitle",
  "message": "$escapedMessage",
  "details": $details
}"""
    }

    companion object {
        fun listToJson(logs: List<TrainingLogEntry>, username: String = "student"): String {
            val sb = StringBuilder()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val nowStr = sdf.format(Date())
            sb.append("{\n")
            sb.append("  \"exportMetadata\": {\n")
            sb.append("    \"app\": \"DLE Autonomous Prototype\",\n")
            sb.append("    \"username\": \"$username\",\n")
            sb.append("    \"exportTimestamp\": ${System.currentTimeMillis()},\n")
            sb.append("    \"exportDate\": \"$nowStr\",\n")
            sb.append("    \"totalEvents\": ${logs.size}\n")
            sb.append("  },\n")
            sb.append("  \"events\": [\n")
            logs.forEachIndexed { index, entry ->
                val entryJson = entry.toJson().prependIndent("    ")
                sb.append(entryJson)
                if (index < logs.size - 1) {
                    sb.append(",\n")
                } else {
                    sb.append("\n")
                }
            }
            sb.append("  ]\n")
            sb.append("}")
            return sb.toString()
        }
    }
}
