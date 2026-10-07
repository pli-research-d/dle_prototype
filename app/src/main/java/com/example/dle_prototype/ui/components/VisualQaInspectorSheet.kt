package com.example.dle_prototype.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.vision.VisualQaSource
import com.example.dle_prototype.data.vision.Yolo11nClass
import com.example.dle_prototype.data.vision.Yolo11nDetection
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualQaInspectorSheet(
    visualSource: VisualQaSource,
    onDismiss: () -> Unit,
    onSendElevatedPrompt: (promptText: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedDetectionId by remember { mutableStateOf<String?>(null) }
    var showBoxesOverlay by remember { mutableStateOf(true) }

    val selectedDetection = visualSource.yoloResult.detections.find { it.id == selectedDetectionId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF090E1A),
        contentColor = Color.White,
        modifier = modifier.testTag("visual_qa_inspector_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanAccent.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "YOLO11n & OCR Visual Inspector",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = visualSource.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_inspector_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Inspector",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B))

            // Navigation Tabs
            val tabs = listOf("Visual Detections", "Extracted OCR", "Nano Telemetry")
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF090E1A),
                contentColor = CyanAccent,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
                        color = CyanAccent
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) CyanAccent else Color(0xFF94A3B8)
                            )
                        },
                        modifier = Modifier.testTag("inspector_tab_$index")
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTabIndex) {
                    0 -> VisualDetectionsTab(
                        visualSource = visualSource,
                        selectedDetection = selectedDetection,
                        showBoxesOverlay = showBoxesOverlay,
                        onToggleBoxesOverlay = { showBoxesOverlay = !showBoxesOverlay },
                        onSelectDetection = { selectedDetectionId = it },
                        onQuerySpecificElement = { detection ->
                            val prompt = "Please explain and analyze the ${detection.displayName} detected in this study source:\n\n${detection.extractedText ?: ""}"
                            onSendElevatedPrompt(prompt)
                            onDismiss()
                        }
                    )
                    1 -> ExtractedOcrTab(
                        visualSource = visualSource,
                        onCopyText = { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Extracted OCR Text", text))
                            Toast.makeText(context, "Copied OCR text to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                    2 -> NanoTelemetryTab(visualSource = visualSource)
                }
            }

            // Bottom Action Bar: Elevate QA & Send to Tutor
            Surface(
                color = Color(0xFF0F172A),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dismiss_visual_inspector"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF94A3B8)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text("Keep Attached")
                    }

                    Button(
                        onClick = {
                            val elevatedPrompt = visualSource.buildElevatedPrompt()
                            onSendElevatedPrompt(elevatedPrompt)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("elevate_qa_ask_tutor_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color(0xFF090E1A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Elevate QA Source",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Image Canvas with Interactive YOLO11n Bounding Boxes.
 */
@Composable
private fun VisualDetectionsTab(
    visualSource: VisualQaSource,
    selectedDetection: Yolo11nDetection?,
    showBoxesOverlay: Boolean,
    onToggleBoxesOverlay: () -> Unit,
    onSelectDetection: (String?) -> Unit,
    onQuerySpecificElement: (Yolo11nDetection) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overlay Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${visualSource.yoloResult.detections.size} Regions Detected",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${visualSource.yoloResult.latencyMs} ms Latency",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.clickable { onToggleBoxesOverlay() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (showBoxesOverlay) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (showBoxesOverlay) CyanAccent else Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (showBoxesOverlay) "Boxes ON" else "Boxes OFF",
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // Image Canvas with Interactive Bounding Boxes
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("yolo_visual_canvas_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(visualSource.bitmap.width.toFloat() / visualSource.bitmap.height.toFloat())
                ) {
                    val imageBitmap = remember(visualSource.bitmap) { visualSource.bitmap.asImageBitmap() }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(showBoxesOverlay) {
                                detectTapGestures { offset ->
                                    if (showBoxesOverlay) {
                                        val normX = offset.x / size.width
                                        val normY = offset.y / size.height
                                        val clicked = visualSource.yoloResult.detections
                                            .filter { it.yoloClass != Yolo11nClass.DOCUMENT }
                                            .firstOrNull { det ->
                                                normX in det.box.left..det.box.right && normY in det.box.top..det.box.bottom
                                            }
                                        onSelectDetection(clicked?.id)
                                    }
                                }
                            }
                    ) {
                        // 1. Draw base bitmap
                        drawImage(
                            image = imageBitmap,
                            dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                        )

                        // 2. Draw YOLO11n bounding boxes overlay
                        if (showBoxesOverlay) {
                            visualSource.yoloResult.detections.forEach { detection ->
                                if (detection.yoloClass == Yolo11nClass.DOCUMENT) return@forEach

                                val isSelected = detection.id == selectedDetection?.id
                                val rectLeft = detection.box.left * size.width
                                val rectTop = detection.box.top * size.height
                                val rectWidth = detection.box.width * size.width
                                val rectHeight = detection.box.height * size.height

                                // Draw bounding box outline
                                drawRect(
                                    color = if (isSelected) Color.White else detection.color,
                                    topLeft = Offset(rectLeft, rectTop),
                                    size = Size(rectWidth, rectHeight),
                                    style = Stroke(width = if (isSelected) 4.dp.toPx() else 2.dp.toPx())
                                )

                                // Optional fill tint when selected
                                if (isSelected) {
                                    drawRect(
                                        color = detection.color.copy(alpha = 0.25f),
                                        topLeft = Offset(rectLeft, rectTop),
                                        size = Size(rectWidth, rectHeight)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Selected Detection Card
        item {
            AnimatedVisibility(
                visible = selectedDetection != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedDetection?.let { det ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, det.color.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("selected_detection_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = det.color.copy(alpha = 0.2f),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = when (det.yoloClass) {
                                                    Yolo11nClass.MATH_FORMULA -> Icons.Default.Functions
                                                    Yolo11nClass.CODE_BLOCK -> Icons.Default.DataObject
                                                    Yolo11nClass.DIAGRAM -> Icons.Default.Layers
                                                    Yolo11nClass.QUESTION_STATEMENT -> Icons.Default.HelpOutline
                                                    else -> Icons.Default.AutoAwesome
                                                },
                                                contentDescription = null,
                                                tint = det.color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = det.displayName,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = det.color.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${det.confidencePercent}% Conf",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = det.color,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onSelectDetection(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Unselect",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (!det.extractedText.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = det.extractedText,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE2E8F0),
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onQuerySpecificElement(det) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = det.color,
                                    contentColor = Color(0xFF090E1A)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ask_about_selected_element_button")
                            ) {
                                Text(
                                    text = "Ask Tutor About This ${det.displayName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // List of all detected visual elements
        item {
            Text(
                text = "Detected Visual Structures (YOLO11n)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(visualSource.yoloResult.detections.filter { it.yoloClass != Yolo11nClass.DOCUMENT }) { det ->
            val isSelected = det.id == selectedDetection?.id
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                border = BorderStroke(1.dp, if (isSelected) det.color else Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDetection(if (isSelected) null else det.id) }
                    .testTag("detection_chip_${det.id}")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = det.color.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(det.color, CircleShape)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = det.displayName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "BBox: [${(det.box.left * 100).toInt()}%, ${(det.box.top * 100).toInt()}%] • ${det.label}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = det.color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${det.confidencePercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = det.color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Full Extracted OCR Text & Classification Breakdown.
 */
@Composable
private fun ExtractedOcrTab(
    visualSource: VisualQaSource,
    onCopyText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Metrics Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(label = "Words", value = "${visualSource.ocrResult.wordCount}", modifier = Modifier.weight(1f))
                MetricChip(label = "Lines", value = "${visualSource.ocrResult.lineCount}", modifier = Modifier.weight(1f))
                MetricChip(label = "Confidence", value = "${visualSource.ocrResult.confidencePercent}%", modifier = Modifier.weight(1f))
                MetricChip(label = "OCR Latency", value = "${visualSource.ocrResult.latencyMs}ms", modifier = Modifier.weight(1f))
            }
        }

        // Full Raw Text Box
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Full Recognized Text",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        IconButton(
                            onClick = { onCopyText(visualSource.ocrResult.fullText) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Text",
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = visualSource.ocrResult.fullText,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Classified Blocks Section
        item {
            Text(
                text = "Classified Text Elements (${visualSource.ocrResult.blocks.size} blocks)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(visualSource.ocrResult.blocks) { block ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (block.type) {
                            com.example.dle_prototype.data.vision.OcrBlockType.QUESTION -> Color(0xFFA855F7).copy(alpha = 0.2f)
                            com.example.dle_prototype.data.vision.OcrBlockType.MATH_EQUATION -> AmberAccent.copy(alpha = 0.2f)
                            com.example.dle_prototype.data.vision.OcrBlockType.CODE -> CyanAccent.copy(alpha = 0.2f)
                            else -> Color(0xFF64748B).copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = block.type.displayName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (block.type) {
                                com.example.dle_prototype.data.vision.OcrBlockType.QUESTION -> Color(0xFFA855F7)
                                com.example.dle_prototype.data.vision.OcrBlockType.MATH_EQUATION -> AmberAccent
                                com.example.dle_prototype.data.vision.OcrBlockType.CODE -> CyanAccent
                                else -> Color(0xFF94A3B8)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = block.text,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontFamily = if (block.type == com.example.dle_prototype.data.vision.OcrBlockType.CODE) FontFamily.Monospace else FontFamily.Default
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: YOLO11n Nano On-Device ML Architecture & Edge Benchmarks.
 */
@Composable
private fun NanoTelemetryTab(
    visualSource: VisualQaSource,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Ultralytics YOLO11n Engine Specs",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val specs = listOf(
                        "Model Architecture" to visualSource.yoloResult.modelArchitecture,
                        "Input Tensor Shape" to visualSource.yoloResult.inputResolution,
                        "Inference Latency" to "${visualSource.yoloResult.latencyMs} ms (Edge Optimized)",
                        "Non-Max Suppression" to "IoU ${visualSource.yoloResult.nmsIouThreshold}",
                        "Confidence Threshold" to "${(visualSource.yoloResult.confidenceThreshold * 100).toInt()}%",
                        "Source Image Size" to "${visualSource.yoloResult.imageWidth} x ${visualSource.yoloResult.imageHeight} px",
                        "Target Execution" to "On-Device Neural Processing (Private & Offline)"
                    )

                    specs.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Why Nano Architecture for QA?",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "YOLO11n is engineered with 2.6M parameters and C3k2 cross-stage partial blocks, delivering near-zero battery drain and sub-15ms edge inference. By detecting diagram coordinates, formulas, and code blocks before OCR parsing, it structures questions with semantic spatial context.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, color = CyanAccent, fontSize = 13.sp)
            Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
    }
}
