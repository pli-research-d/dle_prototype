package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.PersonalizationProfile
import com.example.dle_prototype.data.ml.InferenceLatencySnapshot
import com.example.dle_prototype.data.ml.LatencyBenchmarkSummary
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.TrainingCheckpoint
import com.example.dle_prototype.data.ml.TrainingLogEntry
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent

/**
 * Unified and organized Visual Hub that combines cognitive profiling,
 * loss curves, hardware inference latency, and system logs into an intuitive,
 * sleek interface.
 */
@Composable
fun CombinedVisualHub(
    profile: PersonalizationProfile?,
    savedCheckpoints: List<TrainingCheckpoint>,
    userWeights: ModelWeights?,
    trainingLogs: List<TrainingLogEntry>,
    latencySnapshot: InferenceLatencySnapshot?,
    latencyBenchmark: LatencyBenchmarkSummary?,
    isBenchmarkingLatency: Boolean,
    onRunLatencyBenchmark: () -> Unit,
    onOpenTraining: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onClearLogs: () -> Unit,
    onRefreshLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSegment by remember { mutableIntStateOf(0) } // 0: Cognitive Radar, 1: Curves & Checkpoints, 2: Latency & Arch, 3: System Logs

    val segments = listOf(
        Triple("Trait Radar", Icons.Default.Psychology, 0),
        Triple("Loss Curves", Icons.Default.ShowChart, 1),
        Triple("Hardware Latency", Icons.Default.Speed, 2),
        Triple("Logs & Checkpoints", Icons.Default.Timeline, 3)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("combined_visual_hub"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Studio Launcher Quick Header
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Neural Visual Control Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Unified On-Device Diagnostics",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Button(
                    onClick = onOpenDiagnostics,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.testTag("hub_deep_diagnostics_button")
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deep Studio", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Horizontal Segment Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            segments.forEach { (label, icon, idx) ->
                val isSelected = activeSegment == idx
                FilterChip(
                    selected = isSelected,
                    onClick = { activeSegment = idx },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                        selectedLabelColor = CyanAccent,
                        selectedLeadingIconColor = CyanAccent,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) CyanAccent else Color(0xFF1E293B)
                    )
                )
            }
        }

        // Animated Content for selected segment
        AnimatedContent(
            targetState = activeSegment,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "visual_hub_segment"
        ) { segment ->
            when (segment) {
                0 -> { // Trait Radar & Pedagogical Recommendations
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        profile?.let { p ->
                            TraitRadarCard(profile = p)
                        }
                    }
                }
                1 -> { // Loss Convergence Curves & Accuracy Curve
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        RechartsTrainingCurveCard(
                            checkpoints = savedCheckpoints,
                            userWeights = userWeights,
                            onOpenTraining = onOpenTraining
                        )
                    }
                }
                2 -> { // Real-time Latency Tracker & Neural Architecture
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        RealTimeLatencyTrackerCard(
                            snapshot = latencySnapshot,
                            benchmarkSummary = latencyBenchmark,
                            isBenchmarking = isBenchmarkingLatency,
                            onRunBenchmark = onRunLatencyBenchmark
                        )
                        ModelArchitectureCard(
                            userWeights = userWeights,
                            modelSource = profile?.modelSource ?: "Baseline TFLite",
                            onOpenTraining = onOpenTraining
                        )
                    }
                }
                3 -> { // Training Checkpoints & System Logs
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        TrainingLogCard(
                            logs = trainingLogs,
                            onClearLogs = onClearLogs,
                            onRefreshLogs = onRefreshLogs
                        )
                    }
                }
            }
        }
    }
}
