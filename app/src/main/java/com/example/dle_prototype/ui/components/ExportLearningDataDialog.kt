package com.example.dle_prototype.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dle_prototype.data.export.ExportFormat
import com.example.dle_prototype.data.export.LearningDataExporter
import com.example.dle_prototype.data.export.LearningExportBundle
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Material 3 Export Dialog allowing users to archive their daily learning summaries
 * and complete quiz performance history in either PDF or CSV format.
 */
@Composable
fun ExportLearningDataDialog(
    bundle: LearningExportBundle,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }
    var isExporting by remember { mutableStateOf(false) }
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val dateStamp = remember { SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()) }
    val defaultFileName = remember(selectedFormat) {
        "NeuralPrep_${bundle.user.username}_Learning_Archive_$dateStamp.${selectedFormat.extension}"
    }

    // Storage Access Framework launcher to save file directly to Downloads / Storage
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(selectedFormat.mimeType)
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isExporting = true
                exportStatusMessage = "Writing archive file to storage..."
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { os ->
                            LearningDataExporter.writeToOutputStream(context, bundle, selectedFormat, os)
                        }
                    }
                    isSuccess = true
                    exportStatusMessage = "✓ Successfully saved ${selectedFormat.displayName} to device!"
                    Toast.makeText(context, "Archive saved to storage successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    isSuccess = false
                    exportStatusMessage = "Failed to save: ${e.localizedMessage ?: "Unknown error"}"
                } finally {
                    isExporting = false
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp))
                .testTag("export_learning_dialog"),
            color = Color(0xFF0F172A),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = "Export Data",
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Export Learning Archive",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                            Text(
                                text = "Personal record & performance telemetry",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_export")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                // Scope summary preview badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ARCHIVE INCLUDES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanAccent
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• ${bundle.quizAttempts.size} Quiz Attempts\n• ${bundle.dailyStreak}d Streak & Goals",
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = "• ${bundle.focusSessions.size} Focus Sessions\n• ${bundle.digitalBadges.count { it.isUnlocked }} Badges & Peak Hours",
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }

                // Format Selector Title
                Text(
                    text = "SELECT EXPORT FORMAT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )

                // 2 Format Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // PDF Option Card
                    val isPdf = selectedFormat == ExportFormat.PDF
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isPdf) CyanAccent.copy(alpha = 0.12f) else Color(0xFF131D31),
                        border = BorderStroke(
                            width = if (isPdf) 1.5.dp else 1.dp,
                            color = if (isPdf) CyanAccent else Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFormat = ExportFormat.PDF }
                            .testTag("export_format_pdf")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = if (isPdf) CyanAccent else Color(0xFF94A3B8),
                                    modifier = Modifier.size(24.dp)
                                )
                                if (isPdf) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "PDF Document",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isPdf) Color.White else Color(0xFFE2E8F0)
                            )
                            Text(
                                text = "Visual multi-page report with tables & scorecards",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 13.sp
                            )
                        }
                    }

                    // CSV Option Card
                    val isCsv = selectedFormat == ExportFormat.CSV
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCsv) EmeraldSuccess.copy(alpha = 0.12f) else Color(0xFF131D31),
                        border = BorderStroke(
                            width = if (isCsv) 1.5.dp else 1.dp,
                            color = if (isCsv) EmeraldSuccess else Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFormat = ExportFormat.CSV }
                            .testTag("export_format_csv")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = if (isCsv) EmeraldSuccess else Color(0xFF94A3B8),
                                    modifier = Modifier.size(24.dp)
                                )
                                if (isCsv) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "CSV Spreadsheet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isCsv) Color.White else Color(0xFFE2E8F0)
                            )
                            Text(
                                text = "Raw tabular data for Excel, Sheets, or data scripts",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                // Status / Progress feedback banner
                AnimatedVisibility(
                    visible = exportStatusMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    exportStatusMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSuccess) EmeraldSuccess.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (isSuccess) EmeraldSuccess else AmberAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = CyanAccent
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (isSuccess) EmeraldSuccess else AmberAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = msg,
                                    fontSize = 11.sp,
                                    color = if (isSuccess) EmeraldSuccess else AmberAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Primary Action: Save to Device (Downloads / Storage via SAF)
                    Button(
                        onClick = {
                            createDocumentLauncher.launch(defaultFileName)
                        },
                        enabled = !isExporting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_export"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save ${selectedFormat.displayName} to Device",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Secondary Action: Share / Open with System Chooser
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isExporting = true
                                exportStatusMessage = "Preparing ${selectedFormat.displayName} for sharing..."
                                try {
                                    val exportedFile = withContext(Dispatchers.IO) {
                                        LearningDataExporter.exportToCacheFile(context, bundle, selectedFormat)
                                    }
                                    val shareIntent = LearningDataExporter.createShareIntent(context, exportedFile, selectedFormat)
                                    context.startActivity(shareIntent)
                                    isSuccess = true
                                    exportStatusMessage = "✓ Launched system share for ${selectedFormat.displayName}"
                                } catch (e: Exception) {
                                    isSuccess = false
                                    exportStatusMessage = "Sharing error: ${e.localizedMessage ?: "Unknown error"}"
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        enabled = !isExporting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_share_export"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFFF8FAFC),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share / Open ${selectedFormat.extension.uppercase()}",
                                color = Color(0xFFF8FAFC),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Quick Copy for CSV
                    if (selectedFormat == ExportFormat.CSV) {
                        OutlinedButton(
                            onClick = {
                                val csvText = LearningDataExporter.generateCsv(bundle)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("NeuralPrep Learning Archive", csvText)
                                clipboard.setPrimaryClip(clip)
                                isSuccess = true
                                exportStatusMessage = "✓ Copied CSV data to clipboard!"
                                Toast.makeText(context, "CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_copy_csv"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Copy CSV to Clipboard",
                                    color = EmeraldSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
