package com.example.dle_prototype.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.qa.QaChatEngine
import com.example.dle_prototype.data.qa.QaChatMessage
import com.example.dle_prototype.data.vision.VisualQaPreset
import com.example.dle_prototype.data.vision.VisualQaProcessor
import com.example.dle_prototype.data.vision.VisualQaSource
import com.example.dle_prototype.data.vision.VisualQaSourceType
import com.example.dle_prototype.ui.components.RealTimeCameraPreviewOverlay
import com.example.dle_prototype.ui.components.VisualQaInspectorSheet
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QaChatScreen(
    username: String,
    dbHelper: DatabaseHelper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<QaChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var activeFollowUps by remember { mutableStateOf<List<String>>(emptyList()) }

    // Visual QA Source (Camera / Upload / Presets with YOLO11n + OCR)
    var activeVisualSource by remember { mutableStateOf<VisualQaSource?>(null) }
    var isProcessingVisualSource by remember { mutableStateOf(false) }
    var showVisualInspector by remember { mutableStateOf(false) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var showCameraLiveScanner by remember { mutableStateOf(false) }
    var capturedPreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val initialSuggestions = remember { QaChatEngine.getInitialPromptSuggestions() }

    BackHandler {
        onBack()
    }

    // Camera Capture Launcher - opens live camera overlay with YOLO11n bounding boxes
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { capturedBitmap ->
        if (capturedBitmap != null) {
            capturedPreviewBitmap = capturedBitmap
            showCameraLiveScanner = true
        } else {
            // If native camera preview returned null or user canceled, open live interactive scanner viewfinder
            capturedPreviewBitmap = null
            showCameraLiveScanner = true
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhotoLauncher.launch(null)
        } else {
            // Still allow scanner with live virtual feed for privacy or permission-free mode
            capturedPreviewBitmap = null
            showCameraLiveScanner = true
            Toast.makeText(context, "Opening Live YOLO11n Simulator Feed", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo Picker Launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessingVisualSource = true
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.isMutableRequired = true
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }

                    val source = VisualQaProcessor.processImage(
                        context = context,
                        bitmap = bitmap,
                        title = "Uploaded Image: Study Material",
                        sourceType = VisualQaSourceType.GALLERY_UPLOAD
                    )
                    activeVisualSource = source
                    Toast.makeText(
                        context,
                        "YOLO11n detected ${source.yoloResult.detections.size} regions • OCR: ${source.ocrResult.wordCount} words",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isProcessingVisualSource = false
                }
            }
        }
    }

    fun selectPreset(preset: VisualQaPreset) {
        showPresetDialog = false
        coroutineScope.launch {
            isProcessingVisualSource = true
            try {
                val source = VisualQaProcessor.loadPreset(context, preset)
                activeVisualSource = source
                Toast.makeText(context, "Loaded preset: ${preset.title}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading preset: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isProcessingVisualSource = false
            }
        }
    }

    // Load initial chat history from SQLite
    LaunchedEffect(username) {
        messages = dbHelper.getQaChatHistory(username)
    }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(queryText: String) {
        val trimmed = queryText.trim()
        if ((trimmed.isEmpty() && activeVisualSource == null) || isGenerating || isProcessingVisualSource) return

        val promptToSend = if (activeVisualSource != null) {
            activeVisualSource!!.buildElevatedPrompt(userCustomQuery = trimmed.ifBlank { null })
        } else {
            trimmed
        }

        inputText = ""
        coroutineScope.launch {
            // Save & display user message
            val userTimestamp = System.currentTimeMillis()
            dbHelper.insertQaChatMessage(username, "user", promptToSend, userTimestamp)
            val updatedUserList = messages + QaChatMessage(
                username = username,
                sender = "user",
                message = promptToSend,
                timestamp = userTimestamp
            )
            messages = updatedUserList
            isGenerating = true

            try {
                val response = QaChatEngine.generateResponse(promptToSend, username, dbHelper)
                val assistantTimestamp = System.currentTimeMillis()

                // Construct full message payload
                val fullResponseContent = buildString {
                    append(response.answer)
                    if (!response.codeSnippet.isNullOrBlank()) {
                        append("\n\n```code\n")
                        append(response.codeSnippet.trim())
                        append("\n```")
                    }
                }

                dbHelper.insertQaChatMessage(username, "assistant", fullResponseContent, assistantTimestamp)
                messages = updatedUserList + QaChatMessage(
                    username = username,
                    sender = "assistant",
                    message = fullResponseContent,
                    timestamp = assistantTimestamp
                )
                activeFollowUps = response.suggestedFollowUps
            } catch (e: Exception) {
                val errorMsg = "I encountered an error retrieving that answer. Please try again."
                dbHelper.insertQaChatMessage(username, "assistant", errorMsg, System.currentTimeMillis())
                messages = updatedUserList + QaChatMessage(
                    username = username,
                    sender = "assistant",
                    message = errorMsg
                )
            } finally {
                isGenerating = false
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("qa_chat_screen"),
        containerColor = Color(0xFF090E1A),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyanAccent.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "QA Tutor Avatar",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "AI Study Tutor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmeraldSuccess.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "On-Device",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Grounded in Curriculum & Your Telemetry",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("qa_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showPresetDialog = true },
                        modifier = Modifier.testTag("qa_top_presets_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Study Presets",
                            tint = AmberAccent
                        )
                    }
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("qa_clear_chat_button"),
                        enabled = messages.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Chat History",
                            tint = if (messages.isNotEmpty()) Color(0xFF94A3B8) else Color(0xFF475569)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Active Elevated Visual Source Banner (YOLO11n + OCR)
                    if (activeVisualSource != null) {
                        ElevatedSourceBanner(
                            source = activeVisualSource!!,
                            onInspect = { showVisualInspector = true },
                            onClear = { activeVisualSource = null },
                            onQuickAction = { promptText ->
                                sendMessage(promptText)
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    } else if (isProcessingVisualSource) {
                        ProcessingVisionBanner()
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Follow-up suggestion pills if available
                    if (activeFollowUps.isNotEmpty() && !isGenerating) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeFollowUps.forEachIndexed { idx, prompt ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .clickable {
                                            activeFollowUps = emptyList()
                                            sendMessage(prompt)
                                        }
                                        .testTag("qa_follow_up_chip_$idx")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = prompt,
                                            fontSize = 11.sp,
                                            color = Color(0xFFE2E8F0),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action Input Row: Camera + Upload + Presets + Text Field + Send Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Camera Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                                .testTag("qa_camera_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Scan with Camera",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Upload Image Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                                .testTag("qa_upload_image_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Upload Study Image",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Study Presets Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    showPresetDialog = true
                                }
                                .testTag("qa_presets_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Study Presets",
                                    tint = AmberAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Input Text Field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = if (activeVisualSource != null) "Ask about this visual source..." else "Ask code, concepts, or stats...",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_chat_input"),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF090E1A),
                                unfocusedContainerColor = Color(0xFF090E1A),
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            maxLines = 3,
                            singleLine = false
                        )

                        // Send Action Button
                        val canSend = (inputText.isNotBlank() || activeVisualSource != null) && !isGenerating && !isProcessingVisualSource
                        Surface(
                            shape = CircleShape,
                            color = if (canSend) CyanAccent else Color(0xFF1E293B),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable(enabled = canSend) {
                                    sendMessage(inputText)
                                }
                                .testTag("qa_send_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Message",
                                    tint = if (canSend) Color(0xFF090E1A) else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Privacy & Architecture Banner
            Surface(
                color = Color(0xFF0B132B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Privacy Shield",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "100% Private & Offline. Powered by YOLO11n Nano & on-device cognitive intelligence.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Message list or empty state
            if (messages.isEmpty()) {
                EmptyChatWelcome(
                    initialSuggestions = initialSuggestions,
                    onSelectSuggestion = { prompt ->
                        sendMessage(prompt)
                    },
                    onScanCamera = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    onUploadImage = {
                        pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onSelectPreset = {
                        showPresetDialog = true
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("qa_messages_list"),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(messages, key = { it.id.takeIf { id -> id > 0 } ?: it.timestamp }) { msg ->
                        ChatMessageItem(
                            message = msg,
                            onCopyText = { textToCopy ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("QA Answer", textToCopy)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (isGenerating) {
                        item {
                            ThinkingIndicator()
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for clearing chat history
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Clear Study Chat History", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Text(
                    "Are you sure you want to clear your current Q&A chat history? This cannot be undone.",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            dbHelper.clearQaChatHistory(username)
                            messages = emptyList()
                            activeFollowUps = emptyList()
                            activeVisualSource = null
                            showClearDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Preset Study Materials Chooser Dialog
    if (showPresetDialog) {
        PresetPickerDialog(
            presets = VisualQaProcessor.PRESETS,
            onSelect = { preset ->
                selectPreset(preset)
            },
            onDismiss = { showPresetDialog = false }
        )
    }

    // Real-Time Camera Preview Overlay with Live YOLO11n Bounding Boxes & OCR
    if (showCameraLiveScanner) {
        RealTimeCameraPreviewOverlay(
            initialBitmap = capturedPreviewBitmap,
            onConfirmCapture = { source ->
                activeVisualSource = source
                showCameraLiveScanner = false
                capturedPreviewBitmap = null
                Toast.makeText(
                    context,
                    "YOLO11n detected ${source.yoloResult.detections.size} regions • OCR: ${source.ocrResult.wordCount} words",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = {
                showCameraLiveScanner = false
                capturedPreviewBitmap = null
            }
        )
    }

    // Fullscreen Interactive Visual QA Inspector Sheet
    if (showVisualInspector && activeVisualSource != null) {
        VisualQaInspectorSheet(
            visualSource = activeVisualSource!!,
            onDismiss = { showVisualInspector = false },
            onSendElevatedPrompt = { elevatedPrompt ->
                showVisualInspector = false
                sendMessage(elevatedPrompt)
            }
        )
    }
}

/**
 * Banner showing active elevated visual source attached to the chat session.
 */
@Composable
private fun ElevatedSourceBanner(
    source: VisualQaSource,
    onInspect: () -> Unit,
    onClear: () -> Unit,
    onQuickAction: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("elevated_source_banner")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val imageBitmap = remember(source.bitmap) { source.bitmap.asImageBitmap() }
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Source Thumbnail",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Elevated QA Source",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CyanAccent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "YOLO11n + OCR",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = source.badgeSummary,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onInspect,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("inspect_source_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Inspect Visuals",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("clear_source_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Source",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick suggestion chips for the active visual source
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "💡 Solve & Explain Step-by-Step",
                    "🔍 Breakdown Diagram Structure",
                    "📐 Extract & Derive Formulas",
                    "📝 Create Practice Quiz"
                ).forEach { actionText ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.clickable { onQuickAction(actionText) }
                    ) {
                        Text(
                            text = actionText,
                            fontSize = 10.sp,
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Processing banner shown during YOLO11n Nano and OCR execution.
 */
@Composable
private fun ProcessingVisionBanner() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("processing_vision_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = CyanAccent,
                strokeWidth = 2.dp
            )
            Text(
                text = "Processing image with YOLO11n Nano & OCR engine...",
                fontSize = 11.sp,
                color = CyanAccent,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Dialog enabling 1-tap selection of curated educational preset images.
 */
@Composable
private fun PresetPickerDialog(
    presets: List<VisualQaPreset>,
    onSelect: (VisualQaPreset) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Select Study Preset Source",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(preset) }
                            .testTag("preset_item_${preset.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = preset.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CyanAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = preset.subject,
                                        fontSize = 9.sp,
                                        color = CyanAccent,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.description,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}

/**
 * Empty Chat screen displaying welcome banner and interactive topic suggestion chips.
 */
@Composable
private fun EmptyChatWelcome(
    initialSuggestions: List<String>,
    onSelectSuggestion: (String) -> Unit,
    onScanCamera: () -> Unit,
    onUploadImage: () -> Unit,
    onSelectPreset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = CyanAccent.copy(alpha = 0.15f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Welcome to your AI Study Tutor!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Ask questions or scan textbooks, diagrams, equations, and code.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Visual QA Elevation Hero Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("empty_chat_visual_qa_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Elevate QA with Camera & YOLO11n",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Snap a photo or upload an image. YOLO11n Nano object detection (<15ms) and OCR identify diagrams, formulas, and questions instantly.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onScanCamera,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("empty_scan_camera_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onUploadImage,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("empty_upload_image_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSelectPreset,
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("empty_try_presets_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = Color(0xFF090E1A)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Presets", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "POPULAR TOPICS TO TRY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = AmberAccent
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            initialSuggestions.forEachIndexed { index, suggestion ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSuggestion(suggestion) }
                        .testTag("qa_prompt_chip_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0)
                        )
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chat bubble representing either user input or tutor response.
 */
@Composable
private fun ChatMessageItem(
    message: QaChatMessage,
    onCopyText: (String) -> Unit
) {
    val isUser = message.sender == "user"
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                shape = CircleShape,
                color = CyanAccent.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Tutor Avatar",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) Color(0xFF1E293B) else Color(0xFF0F172A)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isUser) CyanAccent.copy(alpha = 0.4f) else Color(0xFF1E293B)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Visual source attached badge
                    if (message.message.contains("ELEVATED VISUAL QA SOURCE")) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "YOLO11n + OCR Source Attached",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                        }
                    }

                    // Check if message contains code block
                    val raw = message.message
                    if (raw.contains("```code")) {
                        val parts = raw.split("```code")
                        val textPart = parts[0].trim()
                        val codeAndRest = parts.getOrNull(1)?.split("```")
                        val codePart = codeAndRest?.getOrNull(0)?.trim()

                        if (textPart.isNotBlank()) {
                            Text(
                                text = textPart,
                                color = Color(0xFFF1F5F9),
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }

                        if (!codePart.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            CodeSnippetView(
                                code = codePart,
                                onCopy = { onCopyText(codePart) }
                            )
                        }
                    } else {
                        Text(
                            text = message.message,
                            color = if (isUser) Color.White else Color(0xFFF1F5F9),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }

                    // Copy action button for assistant responses
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Copy",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier
                                    .clickable { onCopyText(message.message) }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeFormatted,
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = Color(0xFF334155),
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Formatted code snippet card with dark background, syntax styling, and copy button.
 */
@Composable
private fun CodeSnippetView(
    code: String,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF030712),
        border = BorderStroke(1.dp, Color(0xFF1F2937)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Kotlin / Code",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFF38BDF8),
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Subtle typing / thinking indicator while engine generates responses.
 */
@Composable
private fun ThinkingIndicator() {
    val transition = rememberInfiniteTransition(label = "thinking")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("qa_thinking_indicator"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = CyanAccent.copy(alpha = 0.2f),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Tutor is thinking...",
                    fontSize = 12.sp,
                    color = CyanAccent,
                    modifier = Modifier.alpha(alphaAnim)
                )
            }
        }
    }
}
