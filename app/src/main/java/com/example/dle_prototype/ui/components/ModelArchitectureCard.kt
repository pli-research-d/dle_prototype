package com.example.dle_prototype.ui.components

import android.app.ActivityManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.TealAccent

data class LayerSpec(
    val id: String,
    val name: String,
    val layerType: String,
    val inputDim: String,
    val outputDim: String,
    val weightShape: String?,
    val biasShape: String?,
    val paramCount: Int,
    val activation: String,
    val description: String,
    val formula: String,
    val themeColor: Color,
    val features: List<String> = emptyList()
)

data class RuntimeMemoryStats(
    val usedHeapMb: Float,
    val totalHeapMb: Float,
    val maxHeapMb: Float,
    val heapUsagePercent: Float,
    val deviceAvailRamMb: Long,
    val deviceTotalRamMb: Long,
    val isLowMemory: Boolean,
    val activeWeightsBytes: Long,
    val momentumBuffersBytes: Long,
    val tfliteFileSizeBytes: Long,
    val totalModelFootprintBytes: Long,
    val measuredAtMillis: Long = System.currentTimeMillis()
)

fun queryMemoryDiagnostics(context: Context, customWeights: ModelWeights?): RuntimeMemoryStats {
    val runtime = Runtime.getRuntime()
    val usedHeap = (runtime.totalMemory() - runtime.freeMemory()).toFloat() / (1024f * 1024f)
    val totalHeap = runtime.totalMemory().toFloat() / (1024f * 1024f)
    val maxHeap = runtime.maxMemory().toFloat() / (1024f * 1024f)
    val heapPct = if (maxHeap > 0) (usedHeap / maxHeap) * 100f else 0f

    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager?.getMemoryInfo(memInfo)

    var tfliteSize = 12312L
    try {
        context.assets.openFd("dle_model.tflite").use { fd ->
            tfliteSize = fd.length
        }
    } catch (_: Exception) {}

    // Model weight parameters: 84 Float32 values = 336 bytes
    val weightsBytes = 84L * 4L
    // Momentum tensors (84 floats) + gradient cache (84 floats) + scratch activations
    val momentumBytes = 724L
    val totalFootprint = weightsBytes + momentumBytes + tfliteSize

    return RuntimeMemoryStats(
        usedHeapMb = usedHeap,
        totalHeapMb = totalHeap,
        maxHeapMb = maxHeap,
        heapUsagePercent = heapPct.coerceIn(0f, 100f),
        deviceAvailRamMb = memInfo.availMem / (1024L * 1024L),
        deviceTotalRamMb = memInfo.totalMem / (1024L * 1024L),
        isLowMemory = memInfo.lowMemory,
        activeWeightsBytes = weightsBytes,
        momentumBuffersBytes = momentumBytes,
        tfliteFileSizeBytes = tfliteSize,
        totalModelFootprintBytes = totalFootprint
    )
}

val MODEL_LAYERS = listOf(
    LayerSpec(
        id = "layer_input",
        name = "Input Telemetry Layer",
        layerType = "Input Normalizer / Feature Vector",
        inputDim = "[1, 5]",
        outputDim = "[1, 5]",
        weightShape = null,
        biasShape = null,
        paramCount = 0,
        activation = "Z-Score Standardization (μ, σ)",
        description = "Ingests 5 core behavioral signals from user learning interactions and scales them to zero mean and unit variance.",
        formula = "z_i = (x_i - μ_i) / σ_i",
        themeColor = CyanAccent,
        features = listOf(
            "1. Login Frequency (sessions)",
            "2. Total Time Engaged (mins)",
            "3. Average Quiz Score (%)",
            "4. Dynamic Difficulty Reached",
            "5. Category Breadth Index"
        )
    ),
    LayerSpec(
        id = "layer_hidden",
        name = "Cognitive Projection Dense 1",
        layerType = "Dense (Fully Connected) + LeakyReLU",
        inputDim = "[1, 5]",
        outputDim = "[1, 8]",
        weightShape = "[8, 5] (40 weights)",
        biasShape = "[8] (8 biases)",
        paramCount = 48,
        activation = "LeakyReLU (α = 0.1)",
        description = "Learns latent cognitive dimensions, mapping standardized learning behaviors into high-order student representations.",
        formula = "h = LeakyReLU(W_1 · z + b_1),  α = 0.1",
        themeColor = IndigoPrimaryLight,
        features = listOf(
            "40 Trainable Weight Parameters (W_1)",
            "8 Trainable Bias Parameters (b_1)",
            "8 Latent Cognitive Feature Nodes"
        )
    ),
    LayerSpec(
        id = "layer_output",
        name = "Trait Probabilities Dense 2",
        layerType = "Dense (Fully Connected) + Sigmoid",
        inputDim = "[1, 8]",
        outputDim = "[1, 4]",
        weightShape = "[4, 8] (32 weights)",
        biasShape = "[4] (4 biases)",
        paramCount = 36,
        activation = "Sigmoid (Logistic Logistic Curve)",
        description = "Outputs bounded [0.0, 1.0] pedagogical trait probabilities used to tailor quiz pacing, difficulty scaling, and review flashcards.",
        formula = "y = σ(W_2 · h + b_2) = 1 / (1 + e^{-(W_2 h + b_2)})",
        themeColor = EmeraldSuccess,
        features = listOf(
            "1. Analytical Reasoning (40% weight)",
            "2. Memorization Retention (25% weight)",
            "3. Conceptual Synthesis (20% weight)",
            "4. Focus & Endurance (15% weight)"
        )
    )
)

@Composable
fun ModelArchitectureCard(
    userWeights: ModelWeights?,
    modelSource: String,
    onOpenTraining: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var memoryStats by remember { mutableStateOf(queryMemoryDiagnostics(context, userWeights)) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Pipeline, 1 = Memory, 2 = Weights
    var expandedLayerId by remember { mutableStateOf<String?>("layer_hidden") }

    val totalParams = remember { MODEL_LAYERS.sumOf { it.paramCount } }

    LaunchedEffect(userWeights) {
        memoryStats = queryMemoryDiagnostics(context, userWeights)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("model_architecture_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize()
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DeviceHub,
                            contentDescription = "Model Architecture",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Model Architecture & Runtime",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Topology • Parameter Counts • Memory Profiler",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Model status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (userWeights != null) EmeraldSuccess.copy(alpha = 0.15f)
                            else IndigoPrimaryLight.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (userWeights != null) "Personalized v${userWeights.version}" else "TFLite Baseline",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (userWeights != null) EmeraldSuccess else IndigoPrimaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Architecture Stats Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Params",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalParams FP32",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanAccent
                    )
                }

                Column {
                    Text(
                        text = "Architecture",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "3 Layers (MLP)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "Model Memory",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${"%.1f".format(memoryStats.totalModelFootprintBytes / 1024f)} KB",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                }

                Column {
                    Text(
                        text = "Process Heap",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${"%.1f".format(memoryStats.usedHeapMb)} MB",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // View Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Layer Topology", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_layer_topology")
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Memory Profiler", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_memory_profiler")
                )
                FilterChip(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("Tensor Weights", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_tensor_weights")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Layer Topology Pipeline
                    LayerTopologyView(
                        layers = MODEL_LAYERS,
                        expandedLayerId = expandedLayerId,
                        onToggleLayer = { id ->
                            expandedLayerId = if (expandedLayerId == id) null else id
                        }
                    )
                }
                1 -> {
                    // Current Memory Usage & Diagnostics
                    MemoryDiagnosticsView(
                        stats = memoryStats,
                        onRefresh = {
                            memoryStats = queryMemoryDiagnostics(context, userWeights)
                        }
                    )
                }
                2 -> {
                    // Tensor Weights Inspector
                    WeightsInspectorView(
                        weights = userWeights
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        memoryStats = queryMemoryDiagnostics(context, userWeights)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("refresh_memory_usage_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refresh Stats", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenTraining,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("tune_weights_cta"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tune Weights", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun LayerTopologyView(
    layers: List<LayerSpec>,
    expandedLayerId: String?,
    onToggleLayer: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        layers.forEachIndexed { index, layer ->
            val isExpanded = expandedLayerId == layer.id

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (isExpanded) layer.themeColor.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onToggleLayer(layer.id) }
                    .testTag("layer_card_${layer.id}"),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(layer.themeColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = layer.themeColor
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = layer.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = layer.layerType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Parameter badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(layer.themeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (layer.paramCount > 0) "${layer.paramCount} params" else "0 params",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = layer.themeColor
                            )
                        }
                    }

                    // Tensor Dimension chips row
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dims: ${layer.inputDim} → ${layer.outputDim}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = layer.activation,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = layer.themeColor
                        )
                    }

                    // Expanded details
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = layer.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Formula Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0A0F1D))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Math: ${layer.formula}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyanAccent
                                )
                            }

                            if (layer.weightShape != null || layer.biasShape != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    layer.weightShape?.let { ws ->
                                        Text(
                                            text = "Weights: $ws",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    layer.biasShape?.let { bs ->
                                        Text(
                                            text = "Biases: $bs",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (layer.features.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    layer.features.forEach { feat ->
                                        Text(
                                            text = feat,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryDiagnosticsView(
    stats: RuntimeMemoryStats,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Heap usage progress bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "JVM Process Heap Memory",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${"%.1f".format(stats.usedHeapMb)} / ${"%.0f".format(stats.maxHeapMb)} MB (${"%.1f".format(stats.heapUsagePercent)}%)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (stats.heapUsagePercent > 80f) AmberAccent else EmeraldSuccess
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { stats.heapUsagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (stats.heapUsagePercent > 80f) AmberAccent else IndigoPrimaryLight,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Memory Footprint Breakdown Table
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            color = Color(0xFF0F172A)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Neural Engine Footprint Breakdown",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )

                MemoryMetricRow(
                    label = "Active Model Weights (FP32)",
                    value = "${stats.activeWeightsBytes} Bytes (84 floats)",
                    color = CyanAccent
                )
                MemoryMetricRow(
                    label = "Momentum & Gradient Cache",
                    value = "${stats.momentumBuffersBytes} Bytes (optimizer buffers)",
                    color = IndigoPrimaryLight
                )
                MemoryMetricRow(
                    label = "TFLite Engine Mapped File",
                    value = "${"%.2f".format(stats.tfliteFileSizeBytes / 1024f)} KB (dle_model.tflite)",
                    color = AmberAccent
                )
                MemoryMetricRow(
                    label = "Total AI Component Memory",
                    value = "${"%.2f".format(stats.totalModelFootprintBytes / 1024f)} KB",
                    color = EmeraldSuccess,
                    isBold = true
                )

                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
                Spacer(modifier = Modifier.height(2.dp))

                MemoryMetricRow(
                    label = "Device Available RAM",
                    value = "${stats.deviceAvailRamMb} MB / ${stats.deviceTotalRamMb} MB",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MemoryMetricRow(
                    label = "Low Memory Warning",
                    value = if (stats.isLowMemory) "ACTIVE (Low RAM)" else "Normal (Optimal)",
                    color = if (stats.isLowMemory) AmberAccent else EmeraldSuccess
                )
            }
        }
    }
}

@Composable
fun MemoryMetricRow(
    label: String,
    value: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isBold) Color.White else Color(0xFF94A3B8),
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = color,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun WeightsInspectorView(
    weights: ModelWeights?
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Active Tensor Parameters Matrix",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Direct inspectable view of trained weights in on-device neural network memory:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            color = Color(0xFF0B0F19),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .horizontalScroll(scrollState)
            ) {
                if (weights != null) {
                    Text(
                        text = "// Layer 1 Weights W1 [8x5]:",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = IndigoPrimaryLight,
                        fontWeight = FontWeight.Bold
                    )
                    weights.w1.forEachIndexed { i, row ->
                        val rowStr = row.joinToString(", ") { "%.3f".format(it) }
                        Text(
                            text = "h$i: [$rowStr]",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "// Layer 1 Bias B1 [8]:",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "[${weights.b1.joinToString(", ") { "%.3f".format(it) }}]",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "// Layer 2 Weights W2 [4x8]:",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold
                    )
                    val traitLabels = listOf("Analytical", "Memorization", "Conceptual", "Focus")
                    weights.w2.forEachIndexed { k, row ->
                        val label = traitLabels.getOrElse(k) { "t$k" }
                        val rowStr = row.joinToString(", ") { "%.3f".format(it) }
                        Text(
                            text = "$label: [$rowStr]",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "// Layer 2 Bias B2 [4]:",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "[${weights.b2.joinToString(", ") { "%.3f".format(it) }}]",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE2E8F0)
                    )
                } else {
                    Text(
                        text = "// Standard Baseline Weights Initialized (84 FP32 params)",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "W1: [8x5] float32 • b1: [8] float32 • W2: [4x8] float32 • b2: [4] float32",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
