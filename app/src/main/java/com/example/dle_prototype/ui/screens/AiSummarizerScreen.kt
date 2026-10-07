package com.example.dle_prototype.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.ml.GeneratedQuiz
import com.example.dle_prototype.data.ml.OnDeviceQuizGeneratorEngine
import com.example.dle_prototype.data.ml.OnDeviceSummarizerEngine
import com.example.dle_prototype.data.ml.SummarizationResult
import com.example.dle_prototype.data.ml.SummaryDepth
import com.example.dle_prototype.ui.components.GeneratedQuizPlayer
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import kotlinx.coroutines.launch

/**
 * Interactive AI-Driven Summarization Tool powered by the on-device TFLite model.
 * Evaluates sentence importance and outputs crisp, concept-focused bullet points.
 */
@Composable
fun AiSummarizerScreen(
    modifier: Modifier = Modifier,
    username: String? = null,
    dbHelper: DatabaseHelper? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = onBack != null) {
        onBack?.invoke()
    }

    val sampleTopics = remember {
        listOf(
            "Compose State" to "Jetpack Compose is Android's modern declarative UI toolkit designed to simplify and accelerate UI development. At the heart of Compose is the concept of state management, where UI automatically recomposes whenever underlying state changes. MutableStateFlow and StateFlow provide reactive, lifecycle-aware data streams that safely emit state updates to composables. State hoisting is a crucial architectural pattern in Compose that decouples UI rendering from business logic, making components reusable and testable. Side-effects should be managed using LaunchedEffect and rememberCoroutineScope to prevent blocking the main UI thread during asynchronous operations.",

            "Coroutines" to "Kotlin Coroutines represent light-weight threads that enable non-blocking, asynchronous programming without callback hell. Unlike traditional OS threads which require significant memory overhead, thousands of coroutines can execute concurrently on a single thread pool. CoroutineScope manages the lifecycle and structured concurrency of coroutines, ensuring that child tasks are cancelled when their parent scope finishes. Dispatchers.IO is optimized for disk and network I/O operations, while Dispatchers.Default handles CPU-intensive computation. Flow provides a cold asynchronous data stream that emits multiple values sequentially over time.",

            "Neural Networks" to "Artificial Neural Networks are computational models inspired by the biological architecture of the human brain. Deep learning systems pass input vectors through multiple layers of artificial neurons, applying matrix weights and non-linear activation functions such as ReLU. Backpropagation is the fundamental learning algorithm that calculates gradients of the loss function with respect to each model parameter using the chain rule. Gradient descent optimizes the network by iteratively adjusting weights in the direction opposite to the gradient to minimize prediction error. On-device inference with TensorFlow Lite allows models to run privately with low latency and zero network reliance.",

            "Algorithms" to "Algorithm analysis relies on Big O notation to classify how runtime or memory requirements scale as the input size grows. Binary search offers logarithmic time complexity O(log n) by repeatedly halving the search interval in sorted data collections. In contrast, linear search evaluates elements sequentially with O(n) performance, making it inefficient for large datasets. Hash tables achieve average-case constant time complexity O(1) for lookup, insertion, and deletion by mapping keys to array buckets through a hash function. Choosing the appropriate data structure involves balancing time complexity trade-offs against memory overhead.",

            "C Memory Architecture" to "The C programming language provides low-level control over hardware memory through pointers and explicit heap management. Pointers store hexadecimal memory addresses, allowing direct dereferencing and pass-by-reference semantics in a language that is fundamentally pass-by-value. The C standard library offers malloc and calloc to allocate dynamic heap buffers, which must subsequently be released using free to prevent cumulative memory leaks. Failing to clear freed pointers results in dangerous dangling pointer references that trigger segmentation faults or security vulnerabilities. Furthermore, hardware word alignment mandates compiler structure padding, inserting unused bytes to align struct members with multi-byte boundaries."
        )
    }

    var inputText by remember { mutableStateOf(sampleTopics[0].second) }
    var selectedDepth by remember { mutableStateOf(SummaryDepth.STANDARD) }
    var isSummarizing by remember { mutableStateOf(false) }
    var summaryResult by remember { mutableStateOf<SummarizationResult?>(null) }
    var copyStatusMessage by remember { mutableStateOf<String?>(null) }
    var isGeneratingQuiz by remember { mutableStateOf(false) }
    var generatedQuiz by remember { mutableStateOf<GeneratedQuiz?>(null) }

    fun runSummarize() {
        if (inputText.isBlank()) return
        isSummarizing = true
        generatedQuiz = null
        coroutineScope.launch {
            val res = OnDeviceSummarizerEngine.summarize(
                context = context,
                text = inputText,
                depth = selectedDepth
            )
            summaryResult = res
            isSummarizing = false
        }
    }

    fun runGenerateQuiz() {
        val res = summaryResult ?: return
        if (res.bulletPoints.isEmpty()) return
        isGeneratingQuiz = true
        coroutineScope.launch {
            val quiz = OnDeviceQuizGeneratorEngine.generateQuizFromSummary(
                context = context,
                summaryBullets = res.bulletPoints,
                keyConcepts = res.keyConcepts,
                rawText = inputText,
                maxQuestions = 4
            )
            generatedQuiz = quiz
            isGeneratingQuiz = false
        }
    }

    // Auto-run initial summary on first composition
    LaunchedEffect(Unit) {
        if (summaryResult == null && inputText.isNotBlank()) {
            runSummarize()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_summarizer_screen")
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyanAccent.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Summarizer",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "AI CONCEPT SUMMARIZER",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                            Text(
                                text = "On-Device Neural Model · Instant Core Insights",
                                fontSize = 11.sp,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // TFLite Engine Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Text(
                                text = "TFLite Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }

                Text(
                    text = "Extract the most impactful conceptual statements from technical texts. The neural engine analyzes information density, linguistic centrality, and semantic signals to build concise study summaries.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )
            }
        }

        // Quick Topic Presets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "EXPLORE SAMPLE LEARNING MATERIAL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF64748B)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sampleTopics.forEach { (title, content) ->
                    val isSelected = inputText == content
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            inputText = content
                            summaryResult = null
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF131D31),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFF1E293B),
                            selectedBorderColor = CyanAccent
                        ),
                        modifier = Modifier.testTag("preset_chip_$title")
                    )
                }
            }
        }

        // Input Text Area
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LEARNING MATERIAL TEXT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )

                    val words = inputText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                    Text(
                        text = "$words words",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanAccent
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Paste or type study text, documentation, or lecture notes here...",
                            color = Color(0xFF475569),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("learning_material_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF0B132B),
                        unfocusedContainerColor = Color(0xFF0B132B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    )
                )

                // Depth Controls & Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Summary Depth Selector
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SummaryDepth.values().forEach { depth ->
                            val isSel = selectedDepth == depth
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) CyanAccent.copy(alpha = 0.2f) else Color(0xFF131D31),
                                border = BorderStroke(1.dp, if (isSel) CyanAccent else Color(0xFF1E293B)),
                                modifier = Modifier
                                    .clickable { selectedDepth = depth }
                                    .testTag("depth_${depth.name}")
                            ) {
                                Text(
                                    text = "${depth.targetBullets} bullets",
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) CyanAccent else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                inputText = ""
                                summaryResult = null
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear text",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Summarize Button
                Button(
                    onClick = { runSummarize() },
                    enabled = inputText.isNotBlank() && !isSummarizing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        disabledContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("generate_summary_button")
                ) {
                    if (isSummarizing) {
                        CircularProgressIndicator(
                            color = Color(0xFF0F172A),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Running TFLite Inference...",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate Bulleted Summary",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        // Summary Output Section
        summaryResult?.let { res ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("summary_result_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row with Latency & Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "CORE CONCEPT SUMMARY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = EmeraldSuccess
                            )
                        }

                        // Latency chip
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${res.inferenceLatencyMs} ms on-device",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                        }
                    }

                    // Stat Metrics Row: Reduction %, Words, Time Saved
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat 1: Reduction
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF131D31),
                            border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${res.compressionRatioPercent}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldSuccess
                                )
                                Text(
                                    text = "Reduction",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Stat 2: Word Counts
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF131D31),
                            border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${res.summaryWordCount} / ${res.originalWordCount}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Words",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Stat 3: Time Saved
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF131D31),
                            border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "~${res.estimatedSecondsSaved}s",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AmberAccent
                                )
                                Text(
                                    text = "Time Saved",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    // Key Concepts Chips
                    if (res.keyConcepts.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CORE CONCEPTS IDENTIFIED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF64748B)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                res.keyConcepts.forEach { concept ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF0B132B),
                                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = "#$concept",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bulleted Points Display
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "BULLETED SUMMARY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )

                        res.bulletPoints.forEachIndexed { idx, bullet ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF131D31),
                                border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("summary_bullet_item_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(CyanAccent)
                                    )
                                    Text(
                                        text = bullet.removePrefix("•").trim(),
                                        fontSize = 13.sp,
                                        color = Color(0xFFF1F5F9),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Feedback Message
                    copyStatusMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val formatted = res.bulletPoints.joinToString("\n")
                                val clip = ClipData.newPlainText("AI Summary", formatted)
                                clipboard?.setPrimaryClip(clip)
                                copyStatusMessage = "Summary copied to clipboard!"
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("copy_summary_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Copy Bullets", fontSize = 11.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                summaryResult = null
                                generatedQuiz = null
                                inputText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .height(40.dp)
                        ) {
                            Text("New Text", fontSize = 11.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Automated Quiz Generator Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "TEST COMPREHENSION",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmeraldSuccess.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "TFLite Ready",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Craft automated multiple-choice questions evaluated by the on-device TFLite model to reinforce these core concepts and earn XP.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 15.sp
                            )

                            Button(
                                onClick = { runGenerateQuiz() },
                                enabled = !isGeneratingQuiz,
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("generate_quiz_from_summary_button")
                            ) {
                                if (isGeneratingQuiz) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF0F172A),
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Crafting MCQs with TFLite...",
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Generate Practice Quiz from Summary",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Generated Practice Quiz Player (Visible after generation)
        generatedQuiz?.let { quiz ->
            GeneratedQuizPlayer(
                quiz = quiz,
                username = username,
                dbHelper = dbHelper,
                onFinish = { generatedQuiz = null },
                modifier = Modifier.testTag("summary_generated_quiz_player")
            )
        }
    }
}
