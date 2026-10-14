package com.example.dle_prototype.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.graph.KnowledgeEdge
import com.example.dle_prototype.data.graph.KnowledgeGraphEngine
import com.example.dle_prototype.data.graph.KnowledgeTopicNode
import com.example.dle_prototype.data.graph.SuggestedKnowledgeConnection
import com.example.dle_prototype.data.graph.VoiceSummaryAnalysisResult
import com.example.dle_prototype.data.speech.VoiceLearningSpeechManager
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceLearningSummarySheet(
    username: String,
    allNodes: List<KnowledgeTopicNode>,
    existingEdges: List<KnowledgeEdge>,
    dbHelper: DatabaseHelper,
    onDismiss: () -> Unit,
    onConnectionsUpdated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val speechManager = remember { VoiceLearningSpeechManager() }
    val recordingState by speechManager.recordingState.collectAsState()

    var transcriptInput by remember { mutableStateOf("") }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDeniedMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            permissionDeniedMessage = null
            speechManager.startListening(context) { newText ->
                transcriptInput = newText
            }
        } else {
            permissionDeniedMessage = "Microphone access is needed for voice recording. You can also type or use preset summaries."
        }
    }

    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<VoiceSummaryAnalysisResult?>(null) }
    val acceptedConnectionIds = remember { mutableStateListOf<String>() }

    var recordingDurationSeconds by remember { mutableIntStateOf(0) }

    // Recording timer tick
    LaunchedEffect(recordingState.isListening) {
        if (recordingState.isListening) {
            recordingDurationSeconds = 0
            while (true) {
                delay(1000L)
                recordingDurationSeconds++
            }
        }
    }

    // Keep transcript input in sync with incoming speech
    LaunchedEffect(recordingState.transcript) {
        if (recordingState.transcript.isNotBlank()) {
            transcriptInput = recordingState.transcript
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.clear()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF070B14),
        scrimColor = Color.Black.copy(alpha = 0.72f),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(3.dp)
            ) {
                Box(modifier = Modifier.size(width = 38.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
                .testTag("voice_summary_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =================================================================
            // HEADER
            // =================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Voice Learning Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Record reflections to discover new Graph links",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_voice_summary_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            // =================================================================
            // RECORDING & AUDIO VISUALIZER CARD
            // =================================================================
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0D1527),
                border = BorderStroke(
                    1.dp,
                    if (recordingState.isListening) CyanAccent.copy(alpha = 0.6f) else Color(0xFF1E2E4A)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Recording Status Label & Timer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (recordingState.isListening) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Text(
                                text = "RECORDING LIVE (${formatDuration(recordingDurationSeconds)})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                letterSpacing = 1.sp
                            )
                        } else if (recordingState.isProcessing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = CyanAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "PROCESSING SPEECH...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )
                        } else {
                            Text(
                                text = "MICROPHONE READY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Pulsing Microphone Button
                    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (recordingState.isListening) 1.25f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_scale"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        // Outer ripple
                        if (recordingState.isListening) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.15f))
                            )
                        }

                        // Central Action Button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (recordingState.isListening) {
                                        Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
                                    } else {
                                        Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4)))
                                    }
                                )
                                .clickable {
                                    if (recordingState.isListening) {
                                        speechManager.stopListening()
                                    } else {
                                        if (hasPermission) {
                                            speechManager.startListening(context) { newText ->
                                                transcriptInput = newText
                                            }
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                                .testTag("mic_record_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (recordingState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (recordingState.isListening) "Stop Recording" else "Start Voice Recording",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Audio Waveform Equalizer (7 dynamic bars)
                    AudioWaveformVisualizer(
                        isListening = recordingState.isListening,
                        rmsDb = recordingState.rmsDecibels
                    )

                    Text(
                        text = if (recordingState.isListening) {
                            "Speak your learning reflection clearly..."
                        } else {
                            "Tap the microphone to record what you learned today"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )

                    // Permission notice if needed
                    permissionDeniedMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF451A03),
                            border = BorderStroke(1.dp, Color(0xFFB45309)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MicOff,
                                    contentDescription = "Permission Needed",
                                    tint = AmberAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = msg,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFDE68A)
                                )
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TRANSCRIPT INPUT & EDIT BOX
            // =================================================================
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Learner's Spoken Summary",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2E8F0)
                    )
                    if (transcriptInput.isNotBlank()) {
                        Text(
                            text = "Clear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier
                                .clickable {
                                    transcriptInput = ""
                                    speechManager.clear()
                                }
                                .padding(4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = transcriptInput,
                    onValueChange = { transcriptInput = it },
                    placeholder = {
                        Text(
                            text = "Spoken words will appear here in real-time... (or type your study summary)",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("transcript_input_field"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    )
                )

                // Partial real-time speech preview
                if (recordingState.partialText.isNotBlank()) {
                    Text(
                        text = "Listening: \"${recordingState.partialText}\"",
                        fontSize = 11.sp,
                        color = CyanAccent,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // =================================================================
            // QUICK PRESET SUMMARY CHIPS (For rapid testing in emulator & physical)
            // =================================================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "💡 Quick Presets (Tap to test voice suggestions):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetSummaryChip(
                        label = "⚡ Coroutines & TFLite",
                        summary = VoiceLearningSpeechManager.SAMPLE_LEARNING_SUMMARIES[0],
                        onSelect = { transcriptInput = it }
                    )
                    PresetSummaryChip(
                        label = "💾 C Memory & Big-O",
                        summary = VoiceLearningSpeechManager.SAMPLE_LEARNING_SUMMARIES[1],
                        onSelect = { transcriptInput = it }
                    )
                    PresetSummaryChip(
                        label = "📱 HTML & Compose UI",
                        summary = VoiceLearningSpeechManager.SAMPLE_LEARNING_SUMMARIES[2],
                        onSelect = { transcriptInput = it }
                    )
                    PresetSummaryChip(
                        label = "🧠 Spaced Repetition & AI",
                        summary = VoiceLearningSpeechManager.SAMPLE_LEARNING_SUMMARIES[3],
                        onSelect = { transcriptInput = it }
                    )
                    PresetSummaryChip(
                        label = "🗄️ MySQL & Binary Trees",
                        summary = VoiceLearningSpeechManager.SAMPLE_LEARNING_SUMMARIES[4],
                        onSelect = { transcriptInput = it }
                    )
                }
            }

            // =================================================================
            // PRIMARY ACTION BUTTON: PROCESS & SUGGEST CONNECTIONS
            // =================================================================
            Button(
                onClick = {
                    if (transcriptInput.isNotBlank()) {
                        isAnalyzing = true
                        scope.launch {
                            delay(400L) // UI responsive feel
                            val result = KnowledgeGraphEngine.suggestConnectionsFromVoiceSummary(
                                transcript = transcriptInput,
                                allNodes = allNodes,
                                existingEdges = existingEdges
                            )
                            analysisResult = result
                            isAnalyzing = false

                            // Persist summary record to DB
                            dbHelper.saveVoiceSummary(
                                username = username,
                                transcript = transcriptInput,
                                suggestedCount = result.suggestions.size
                            )
                        }
                    }
                },
                enabled = transcriptInput.isNotBlank() && !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("analyze_summary_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0xFF1E293B)
                ),
                border = BorderStroke(
                    1.dp,
                    if (transcriptInput.isNotBlank()) CyanAccent else Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            if (transcriptInput.isNotBlank()) {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFF06B6D4), Color(0xFF10B981))
                                )
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B)))
                            },
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAnalyzing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Analyzing Neural Connections...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Analyze & Suggest Graph Connections",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // =================================================================
            // SUGGESTED CONNECTIONS RESULTS
            // =================================================================
            AnimatedVisibility(
                visible = analysisResult != null,
                enter = fadeIn() + slideInVertically()
            ) {
                analysisResult?.let { result ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section Header & Detected Insights
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0A1324),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hub,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Connection Proposals (${result.suggestions.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                                result.keyInsights.forEach { insight ->
                                    Text(
                                        text = "• $insight",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        // Cards list
                        result.suggestions.forEach { suggestion ->
                            val isAccepted = acceptedConnectionIds.contains(suggestion.id) || suggestion.isAccepted

                            SuggestedConnectionCard(
                                suggestion = suggestion,
                                isAccepted = isAccepted,
                                onAccept = {
                                    scope.launch {
                                        // Save into database
                                        dbHelper.insertCustomKnowledgeEdge(
                                            username = username,
                                            fromId = suggestion.fromNodeId,
                                            toId = suggestion.toNodeId,
                                            relationship = suggestion.relationshipTitle,
                                            reasoning = suggestion.reasoning
                                        )
                                        acceptedConnectionIds.add(suggestion.id)
                                        onConnectionsUpdated()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Visual card displaying a suggested cross-topic connection derived from voice transcript.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestedConnectionCard(
    suggestion: SuggestedKnowledgeConnection,
    isAccepted: Boolean,
    onAccept: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0E172B),
        border = BorderStroke(
            1.dp,
            if (isAccepted) EmeraldSuccess.copy(alpha = 0.7f) else Color(0xFF1E2E4A)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("suggested_connection_${suggestion.fromNodeId}_${suggestion.toNodeId}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with Node Pair and Relationship
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Confidence badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF042F2E),
                    border = BorderStroke(1.dp, Color(0xFF059669))
                ) {
                    Text(
                        text = "${suggestion.confidencePercent}% Match",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // New vs Existing Badge
                if (suggestion.isAlreadyConnected) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "🔗 Reinforces Link",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF451A03),
                        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "⚡ New Connection",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCD34D),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Connection Diagram: Node A ──► Node B
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TopicPill(title = suggestion.fromNodeTitle)
                Text(
                    text = " ──► ",
                    color = CyanAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                TopicPill(title = suggestion.toNodeTitle)
            }

            // Relationship Title
            Text(
                text = suggestion.relationshipTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Reasoning deduced from learner's spoken reflection
            Text(
                text = suggestion.reasoning,
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 16.sp
            )

            // Matched keywords chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                suggestion.matchedKeywords.forEach { kw ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "#$kw",
                            fontSize = 10.sp,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Action Button: Accept & Add to Knowledge Graph
            Button(
                onClick = onAccept,
                enabled = !isAccepted,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAccepted) Color(0xFF064E3B) else Color(0xFF0284C7),
                    disabledContainerColor = Color(0xFF064E3B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("accept_connection_button_${suggestion.id}")
            ) {
                if (isAccepted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Added to Knowledge Graph",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Accept & Add Connection to Graph",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicPill(title: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE2E8F0),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PresetSummaryChip(
    label: String,
    summary: String,
    onSelect: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.clickable { onSelect(summary) }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF38BDF8),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * 7-bar dynamic audio equalizer waveform visualizer.
 */
@Composable
private fun AudioWaveformVisualizer(
    isListening: Boolean,
    rmsDb: Float
) {
    val barCount = 7
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    Row(
        modifier = Modifier.height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animFraction by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + i * 70, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            val baseHeight = if (isListening) {
                val amplitudeScale = (rmsDb / 15f).coerceIn(0.15f, 1f)
                (8.dp + 18.dp * animFraction * amplitudeScale)
            } else {
                4.dp
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(baseHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isListening) {
                            if (i % 2 == 0) CyanAccent else EmeraldSuccess
                        } else {
                            Color(0xFF334155)
                        }
                    )
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
