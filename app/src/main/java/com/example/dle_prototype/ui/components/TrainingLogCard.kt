package com.example.dle_prototype.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.ml.LogCategory
import com.example.dle_prototype.data.ml.LogLevel
import com.example.dle_prototype.data.ml.TrainingLogEntry
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent

@Composable
fun TrainingLogCard(
    logs: List<TrainingLogEntry>,
    onClearLogs: () -> Unit,
    onRefreshLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedCategory by remember { mutableStateOf(LogCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var expandedLogId by remember { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()

    val filteredLogs = remember(logs, selectedCategory, searchQuery) {
        logs.filter { entry ->
            val matchesCategory = when (selectedCategory) {
                LogCategory.ALL -> true
                else -> entry.category == selectedCategory
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                entry.title.contains(searchQuery, ignoreCase = true) ||
                        entry.message.contains(searchQuery, ignoreCase = true) ||
                        entry.category.label.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    // JSON file saver launcher using Storage Access Framework (SAF)
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val jsonPayload = TrainingLogEntry.listToJson(
                    logs = if (filteredLogs.isNotEmpty()) filteredLogs else logs,
                    username = logs.firstOrNull()?.username ?: "student"
                )
                context.contentResolver.openOutputStream(uri)?.use { outStream ->
                    outStream.write(jsonPayload.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Training logs exported successfully as JSON!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving JSON file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to trigger native Android Sharesheet
    fun shareViaSystemSheet() {
        val targetLogs = if (filteredLogs.isNotEmpty()) filteredLogs else logs
        if (targetLogs.isEmpty()) {
            Toast.makeText(context, "No logs available to share", Toast.LENGTH_SHORT).show()
            return
        }
        val jsonPayload = TrainingLogEntry.listToJson(
            logs = targetLogs,
            username = targetLogs.firstOrNull()?.username ?: "student"
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "DLE Training Logs (${targetLogs.size} events)")
            putExtra(Intent.EXTRA_TEXT, jsonPayload)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Training Logs via...")
        context.startActivity(chooser)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("training_log_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title, Live Event Count, Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimaryLight.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = IndigoPrimaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Training Log",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(IndigoPrimaryLight.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${filteredLogs.size} events",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPrimaryLight
                                )
                            }
                        }
                        Text(
                            text = "Timestamped checkpoints, data alerts & parameter adjustments",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Direct Share Button
                    IconButton(
                        onClick = { shareViaSystemSheet() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("training_log_share_button"),
                        enabled = logs.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share via System Sheet",
                            tint = if (logs.isNotEmpty()) CyanAccent else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Export / Save Dialog Button
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("training_log_export_button"),
                        enabled = logs.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = "Export JSON File",
                            tint = if (logs.isNotEmpty()) IndigoPrimaryLight else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = onRefreshLogs,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("training_log_refresh_button")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh Logs",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Clear Button
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("training_log_clear_button"),
                        enabled = logs.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Clear Logs",
                            tint = if (logs.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_log_search_field"),
                placeholder = {
                    Text(
                        "Search logs by keyword, loss, or parameter...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    LogCategory.ALL to "All Events",
                    LogCategory.CHECKPOINT to "Checkpoints",
                    LogCategory.DATA_QUALITY to "Data Quality",
                    LogCategory.PARAM_ADJUSTMENT to "Parameters",
                    LogCategory.CONVERGENCE to "Convergence"
                ).forEach { (cat, label) ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimaryLight,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("training_log_filter_${cat.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Log List
            if (filteredLogs.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("training_log_empty_view"),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No matching log entries found",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Adjust filter chips or run model training to record events",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0B101B),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_log_list"),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                            TrainingLogItemRow(
                                entry = log,
                                isExpanded = expandedLogId == log.id,
                                onToggleExpand = {
                                    expandedLogId = if (expandedLogId == log.id) null else log.id
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Export & Share Dialog
    if (showExportDialog) {
        val targetLogs = if (filteredLogs.isNotEmpty()) filteredLogs else logs
        val jsonPayload = remember(targetLogs) {
            TrainingLogEntry.listToJson(
                logs = targetLogs,
                username = targetLogs.firstOrNull()?.username ?: "student"
            )
        }
        val payloadBytes = remember(jsonPayload) { jsonPayload.toByteArray(Charsets.UTF_8).size }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            modifier = Modifier.testTag("training_log_export_dialog"),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimaryLight.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = IndigoPrimaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Export Training Logs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${targetLogs.size} events • ${"%.1f".format(payloadBytes / 1024f)} KB JSON",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Export timestamped telemetry, checkpoint histories, and parameter updates for external Jupyter / Python analysis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // JSON Preview Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF090D16),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Text(
                            text = jsonPayload,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }

                    // Action buttons grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Share via system sheet button
                        Button(
                            onClick = {
                                showExportDialog = false
                                shareViaSystemSheet()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_log_share_sheet_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("System Share", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        // Save as JSON file button
                        Button(
                            onClick = {
                                showExportDialog = false
                                saveFileLauncher.launch("dle_training_logs_${System.currentTimeMillis()}.json")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_log_save_file_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimaryLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Copy to clipboard option
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(jsonPayload))
                            Toast.makeText(context, "JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                            showExportDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_log_copy_json_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Raw JSON to Clipboard", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Training Logs?") },
            text = { Text("This will permanently remove all recorded telemetry audit events and checkpoint logs from SQLite.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearLogs()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TrainingLogItemRow(
    entry: TrainingLogEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val (catColor, catIcon) = when (entry.category) {
        LogCategory.CHECKPOINT -> Pair(EmeraldSuccess, Icons.Default.Bookmark)
        LogCategory.DATA_QUALITY -> Pair(AmberAccent, Icons.Default.Warning)
        LogCategory.PARAM_ADJUSTMENT -> Pair(CyanAccent, Icons.Default.Tune)
        LogCategory.CONVERGENCE -> Pair(IndigoPrimaryLight, Icons.AutoMirrored.Filled.TrendingUp)
        LogCategory.EARLY_STOPPING -> Pair(RoseAccent, Icons.Default.Warning)
        else -> Pair(Color(0xFF94A3B8), Icons.Default.Info)
    }

    val levelBorder = when (entry.level) {
        LogLevel.ERROR -> RoseAccent
        LogLevel.WARNING -> AmberAccent
        LogLevel.SUCCESS -> EmeraldSuccess
        LogLevel.INFO -> Color(0xFF334155)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggleExpand() }
            .testTag("training_log_item_${entry.id}"),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF131C2E),
        border = BorderStroke(1.dp, levelBorder.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row: Category Badge, Timestamp, and Level Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(catColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                catIcon,
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = entry.category.label.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = catColor
                            )
                        }
                    }
                }

                // Monospace Timestamp
                Text(
                    text = entry.formattedTime(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = entry.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF1F5F9)
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Message text
            Text(
                text = entry.message,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFFCBD5E1)
            )

            // Expanded JSON details
            AnimatedVisibility(visible = isExpanded && entry.detailsJson != "{}") {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF090D16),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "RAW TELEMETRY PAYLOAD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = catColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entry.detailsJson,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }
        }
    }
}
