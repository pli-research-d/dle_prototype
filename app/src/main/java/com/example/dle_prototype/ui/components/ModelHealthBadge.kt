package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.TrainingCheckpoint
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlin.math.abs

enum class HealthState(
    val displayName: String,
    val badgeText: String,
    val color: Color,
    val backgroundColor: Color,
    val icon: ImageVector,
    val summary: String
) {
    STABLE(
        displayName = "Stable",
        badgeText = "STABLE",
        color = EmeraldSuccess,
        backgroundColor = EmeraldSuccess.copy(alpha = 0.15f),
        icon = Icons.Default.CheckCircle,
        summary = "Optimal convergence • Loss steadily decreasing • Generalization healthy"
    ),
    DIVERGING(
        displayName = "Diverging",
        badgeText = "DIVERGING",
        color = RoseAccent,
        backgroundColor = RoseAccent.copy(alpha = 0.15f),
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        summary = "Gradient instability • Loss spiking • Optimization failing"
    ),
    OVERFITTING(
        displayName = "Overfitting",
        badgeText = "OVERFITTING",
        color = AmberAccent,
        backgroundColor = AmberAccent.copy(alpha = 0.15f),
        icon = Icons.Default.Warning,
        summary = "Generalization gap widening • Validation accuracy stalled • Memorization risk"
    ),
    BASELINE(
        displayName = "Baseline",
        badgeText = "BASELINE",
        color = CyanAccent,
        backgroundColor = CyanAccent.copy(alpha = 0.15f),
        icon = Icons.Default.Info,
        summary = "Pre-trained TFLite baseline active • Ready for on-device personalization"
    )
}

data class ModelHealthEvaluation(
    val state: HealthState,
    val currentLoss: Float,
    val previousLoss: Float,
    val lossDelta: Float,
    val lossDeltaPercent: Float,
    val lossTrend: String,
    val stabilityScore: Int,
    val evaluatedEpochs: Int,
    val accuracyPct: Float,
    val explanation: String,
    val recommendation: String,
    val recentLosses: List<Float>
)

fun evaluateModelHealth(
    checkpoints: List<TrainingCheckpoint>,
    userWeights: ModelWeights?
): ModelHealthEvaluation {
    // Collect chronological loss progression
    val losses = mutableListOf<Float>()

    val sortedCheckpoints = checkpoints.sortedWith(compareBy({ it.savedAt }, { it.currentEpoch }))
    val latestCp = sortedCheckpoints.lastOrNull()

    if (latestCp != null && latestCp.lossHistory.isNotEmpty()) {
        losses.addAll(latestCp.lossHistory)
    } else if (sortedCheckpoints.isNotEmpty()) {
        losses.addAll(sortedCheckpoints.map { it.currentLoss })
    } else if (userWeights != null && userWeights.trainedEpochs > 0) {
        losses.add(userWeights.finalLoss)
    }

    // Default Baseline condition when no on-device training has taken place yet
    if (losses.isEmpty()) {
        return ModelHealthEvaluation(
            state = HealthState.BASELINE,
            currentLoss = 0.185f,
            previousLoss = 0.185f,
            lossDelta = 0.0f,
            lossDeltaPercent = 0.0f,
            lossTrend = "Pre-trained Baseline",
            stabilityScore = 80,
            evaluatedEpochs = 0,
            accuracyPct = 75.0f,
            explanation = "Standard pre-trained TFLite factory weights are running. The neural network is ready for on-device personalized calibration.",
            recommendation = "Run on-device training with your recent quiz attempts to calibrate personalized student trait weights.",
            recentLosses = listOf(0.24f, 0.21f, 0.19f, 0.185f)
        )
    }

    val currentLoss = losses.last()
    val previousLoss = if (losses.size >= 2) losses[losses.size - 2] else currentLoss
    val lossDelta = currentLoss - previousLoss
    val lossDeltaPercent = if (previousLoss > 0.0001f) (lossDelta / previousLoss) * 100f else 0.0f

    val evaluatedEpochs = userWeights?.trainedEpochs ?: latestCp?.currentEpoch ?: losses.size
    val currentAccuracy = latestCp?.accuracyPct ?: 85.0f
    val bestAccuracy = checkpoints.maxOfOrNull { it.accuracyPct } ?: currentAccuracy

    val hasAccuracyDropFlag = checkpoints.any { it.triggerType == "ACCURACY_DROP" }

    // 1. DIVERGING CONDITION:
    // Loss sharply jumps upward (>15% increase and delta > 0.015), or consecutive increases, or currentLoss > 0.30
    val isDiverging = (lossDelta > 0.015f && lossDeltaPercent > 15.0f) ||
            (losses.size >= 3 && losses.takeLast(3).let { it[2] > it[1] && it[1] > it[0] && (it[2] - it[0]) > 0.02f }) ||
            (currentLoss > 0.32f && lossDelta > 0.01f)

    if (isDiverging) {
        val score = (50 - lossDeltaPercent.toInt()).coerceIn(15, 55)
        return ModelHealthEvaluation(
            state = HealthState.DIVERGING,
            currentLoss = currentLoss,
            previousLoss = previousLoss,
            lossDelta = lossDelta,
            lossDeltaPercent = lossDeltaPercent,
            lossTrend = "Surging (+${"%.1f".format(lossDeltaPercent)}%)",
            stabilityScore = score,
            evaluatedEpochs = evaluatedEpochs,
            accuracyPct = currentAccuracy,
            explanation = "Loss sharply increased from ${"%.4f".format(previousLoss)} to ${"%.4f".format(currentLoss)} (+${"%.1f".format(lossDeltaPercent)}%). Gradients are destabilized or the learning rate is too high for the dataset distribution.",
            recommendation = "Reduce learning rate (e.g. 0.01) or revert immediately to a previous stable checkpoint in SQLite.",
            recentLosses = losses.takeLast(8)
        )
    }

    // 2. OVERFITTING CONDITION:
    // Generalization gap: validation accuracy drop triggered or accuracy dropped by >= 4% while loss is ultra low (< 0.06)
    // Or loss dropped to near 0 (< 0.012) and plateaued while training epochs are high
    val isOverfitting = hasAccuracyDropFlag ||
            (bestAccuracy > 0f && (bestAccuracy - currentAccuracy) >= 4.0f && currentLoss < 0.06f) ||
            (losses.size >= 6 && currentLoss < 0.012f && abs(lossDelta) < 0.001f && evaluatedEpochs >= 10)

    if (isOverfitting) {
        return ModelHealthEvaluation(
            state = HealthState.OVERFITTING,
            currentLoss = currentLoss,
            previousLoss = previousLoss,
            lossDelta = lossDelta,
            lossDeltaPercent = lossDeltaPercent,
            lossTrend = "Over-optimized (${"%.4f".format(currentLoss)})",
            stabilityScore = 48,
            evaluatedEpochs = evaluatedEpochs,
            accuracyPct = currentAccuracy,
            explanation = "Training loss has converged to near zero (${"%.4f".format(currentLoss)}), but validation accuracy degraded from ${"%.1f".format(bestAccuracy)}% to ${"%.1f".format(currentAccuracy)}%. The neural network is memorizing noise rather than generalizable cognitive traits.",
            recommendation = "Enable Early Stopping or revert weights to the best performing checkpoint prior to the accuracy drop.",
            recentLosses = losses.takeLast(8)
        )
    }

    // 3. STABLE CONDITION (Default healthy state):
    val stabilityScore = (88 + if (lossDelta <= 0f) 8 else -4).coerceIn(75, 98)
    val trend = if (lossDelta < -0.005f) "Decreasing (Converging)"
    else if (abs(lossDelta) <= 0.005f) "Steady & Converged"
    else "Minor Fluctuation"

    return ModelHealthEvaluation(
        state = HealthState.STABLE,
        currentLoss = currentLoss,
        previousLoss = previousLoss,
        lossDelta = lossDelta,
        lossDeltaPercent = lossDeltaPercent,
        lossTrend = trend,
        stabilityScore = stabilityScore,
        evaluatedEpochs = evaluatedEpochs,
        accuracyPct = currentAccuracy,
        explanation = "Training loss is steadily converging (${"%.4f".format(currentLoss)}, Δ: ${if (lossDelta >= 0) "+" else ""}${"%.4f".format(lossDelta)}). On-device backpropagation is producing stable gradient updates without variance anomalies.",
        recommendation = "Model health is optimal. The personalized weights are actively refining trait inference for flashcards and quiz difficulty.",
        recentLosses = losses.takeLast(8)
    )
}

@Composable
fun ModelHealthBadge(
    health: ModelHealthEvaluation,
    onOpenTraining: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    // Pulsing animation for the status indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                health.state.color.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .clickable { showDialog = true }
            .testTag("model_health_badge"),
        color = health.state.backgroundColor.copy(alpha = 0.12f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Glowing Indicator + Status text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Pulsing dot container
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("model_health_indicator"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(health.state.color.copy(alpha = 0.25f))
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(health.state.color)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Model Health: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = health.state.badgeText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = health.state.color,
                            modifier = Modifier.testTag("model_health_state_text")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Stability score badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(health.state.color.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${health.stabilityScore}% Score",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = health.state.color
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Loss: ${"%.4f".format(health.currentLoss)} • Δ: ${if (health.lossDelta >= 0) "+" else ""}${"%.4f".format(health.lossDelta)} (${health.lossTrend})",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("model_health_loss_text")
                    )
                }
            }

            // Right: Details affordance chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Inspect →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = health.state.color
                )
            }
        }
    }

    if (showDialog) {
        ModelHealthDetailsDialog(
            health = health,
            onDismiss = { showDialog = false },
            onOpenTraining = {
                showDialog = false
                onOpenTraining()
            }
        )
    }
}

@Composable
fun ModelHealthDetailsDialog(
    health: ModelHealthEvaluation,
    onDismiss: () -> Unit,
    onOpenTraining: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("model_health_details_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(health.state.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = health.state.icon,
                        contentDescription = null,
                        tint = health.state.color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Model Health: ${health.state.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = health.state.color
                    )
                    Text(
                        text = "On-Device Neural Convergence Profiler",
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
                // Stability Score Progress Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Convergence Stability Score",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${health.stabilityScore} / 100",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = health.state.color
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { health.stabilityScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = health.state.color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                // Loss Metrics Grid
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Training Loss:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                "${"%.4f".format(health.currentLoss)} MSE",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Epoch Loss Delta (Δ):", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                "${if (health.lossDelta >= 0) "+" else ""}${"%.4f".format(health.lossDelta)} (${"%.1f".format(health.lossDeltaPercent)}%)",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (health.lossDelta <= 0) EmeraldSuccess else RoseAccent
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Validation Accuracy:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                "${"%.1f".format(health.accuracyPct)}%",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Evaluated Epochs:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                "${health.evaluatedEpochs} epochs",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }

                // Diagnostic Explanation Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, health.state.color.copy(alpha = 0.3f)),
                    color = health.state.backgroundColor.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Diagnostic Analysis",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = health.state.color
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = health.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Actionable Recommendation Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Recommended Action",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = health.recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenTraining,
                colors = ButtonDefaults.buttonColors(containerColor = health.state.color),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ModelTraining, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Training Studio", fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
