package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Progress Ring component that tracks the user's daily learning goal completion
 * percentage, allowing users to view real-time completion and dynamically set/adjust their target hours.
 */
@Composable
fun DailyGoalProgressRingCard(
    goalProgress: DailyGoalProgress,
    onUpdateTargetHours: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onStartPractice: () -> Unit = {}
) {
    var showAdjustDialog by remember { mutableStateOf(false) }

    val targetHours = goalProgress.targetHours.coerceAtLeast(0.25f)
    val hoursCompleted = goalProgress.hoursCompletedToday
    val completionFraction = if (targetHours > 0f) {
        hoursCompleted / targetHours
    } else 1.0f

    val isAchieved = goalProgress.isAchieved || completionFraction >= 1.0f

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            1.dp,
            if (isAchieved) EmeraldSuccess.copy(alpha = 0.5f) else CyanAccent.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_goal_progress_ring_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Icon + Title + Edit Button
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
                        color = if (isAchieved) EmeraldSuccess.copy(alpha = 0.15f) else CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isAchieved) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = "Daily Goal",
                                tint = if (isAchieved) EmeraldSuccess else CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "DAILY LEARNING GOAL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            if (isAchieved) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmeraldSuccess.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "MET 🎉",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isAchieved) "Target reached today! Great work." else "Track and hit your daily target hours",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Adjust Target button
                IconButton(
                    onClick = { showAdjustDialog = true },
                    modifier = Modifier.testTag("adjust_target_hours_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Adjust Target Hours",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Main Visual: Progress Ring & Status Column
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Circular Progress Ring Visual
                DailyGoalProgressRing(
                    progressFraction = completionFraction,
                    targetHours = targetHours,
                    hoursCompleted = hoursCompleted,
                    isAchieved = isAchieved,
                    ringSize = 140.dp,
                    strokeWidth = 12.dp
                )

                // Goal Stats & Quick Info
                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stat 1: Target Hours
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF131D31),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "DAILY TARGET",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "${formatHours(targetHours)} (${(targetHours * 60).toInt()} mins)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.testTag("target_hours_text")
                            )
                        }
                    }

                    // Stat 2: Completed Time
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF131D31),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "TIME LOGGED TODAY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "${formatHours(hoursCompleted)} (${(goalProgress.minutesCompletedToday).toInt()} mins)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isAchieved) EmeraldSuccess else CyanAccent,
                                modifier = Modifier.testTag("completed_hours_text")
                            )
                        }
                    }

                    // Stat 3: Remaining / Status
                    val remainingMins = ((targetHours - hoursCompleted) * 60).coerceAtLeast(0f).toInt()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isAchieved) Icons.Default.Star else Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = if (isAchieved) EmeraldSuccess else AmberAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isAchieved) "Completed (+50 XP)" else "$remainingMins mins left",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAchieved) EmeraldSuccess else AmberAccent
                        )
                    }
                }
            }

            // Target Hours Inline Stepper Controls
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF131D31),
                border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Set Target Hours",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Adjust daily study commitment",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Stepper: [-] [ 1.5 hrs ] [+]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val nextHours = (targetHours - 0.25f).coerceAtLeast(0.25f)
                                onUpdateTargetHours(nextHours)
                            },
                            enabled = targetHours > 0.25f,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .testTag("target_hours_decrease_button")
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease Target Hours",
                                tint = if (targetHours > 0.25f) Color.White else Color(0xFF475569),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${formatHours(targetHours)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanAccent,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("target_hours_value_text")
                            )
                        }

                        IconButton(
                            onClick = {
                                val nextHours = (targetHours + 0.25f).coerceAtMost(8.0f)
                                onUpdateTargetHours(nextHours)
                            },
                            enabled = targetHours < 8.0f,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .testTag("target_hours_increase_button")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase Target Hours",
                                tint = if (targetHours < 8.0f) Color.White else Color(0xFF475569),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Quick Target Hours Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0.5f to "30m", 1.0f to "1.0h", 1.5f to "1.5h", 2.0f to "2.0h", 3.0f to "3.0h").forEach { (hrs, label) ->
                    val isSelected = kotlin.math.abs(targetHours - hrs) < 0.05f
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateTargetHours(hrs) },
                        label = {
                            Text(
                                text = label,
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
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preset_target_chip_$label")
                    )
                }
            }

            // Action Button: "Start Practice" or "Target Completed"
            Button(
                onClick = onStartPractice,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAchieved) EmeraldSuccess else CyanAccent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("daily_goal_action_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isAchieved) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isAchieved) "Goal Completed! Practice More" else "Practice to Reach Target",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }

    // Modal dialog to adjust target hours with slider & presets
    if (showAdjustDialog) {
        AdjustTargetHoursDialog(
            currentTargetHours = targetHours,
            onDismiss = { showAdjustDialog = false },
            onConfirm = { newHours ->
                onUpdateTargetHours(newHours)
                showAdjustDialog = false
            }
        )
    }
}

/**
 * Pure Progress Ring Composable drawing a glowing circular arc with animated sweep angle,
 * center percentage, and time display.
 */
@Composable
fun DailyGoalProgressRing(
    progressFraction: Float,
    targetHours: Float,
    hoursCompleted: Float,
    isAchieved: Boolean,
    modifier: Modifier = Modifier,
    ringSize: Dp = 130.dp,
    strokeWidth: Dp = 12.dp
) {
    val clampedFraction = progressFraction.coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = clampedFraction,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "daily_goal_progress_ring_anim"
    )

    val percentInt = (progressFraction * 100).toInt()
    val primaryColor = if (isAchieved) EmeraldSuccess else CyanAccent
    val secondaryColor = if (isAchieved) Color(0xFF34D399) else EmeraldSuccess

    Box(
        modifier = modifier
            .size(ringSize)
            .testTag("daily_goal_progress_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokePx = strokeWidth.toPx()
            val diameter = size.minDimension - strokePx
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background track ring
            drawCircle(
                color = Color(0xFF1E293B),
                radius = radius,
                center = center,
                style = Stroke(width = strokePx)
            )

            // Inner subtle border
            drawCircle(
                color = Color(0xFF0F172A).copy(alpha = 0.5f),
                radius = radius - strokePx / 2f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Active Progress Sweep Arc
            val sweepAngle = animatedFraction * 360f
            if (sweepAngle > 0f) {
                val brush = Brush.sweepGradient(
                    0.0f to primaryColor,
                    0.5f to secondaryColor,
                    1.0f to primaryColor,
                    center = center
                )

                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Glow dot at the tip of the progress arc
                val angleRad = Math.toRadians((sweepAngle - 90f).toDouble())
                val dotX = center.x + radius * cos(angleRad).toFloat()
                val dotY = center.y + radius * sin(angleRad).toFloat()

                drawCircle(
                    color = Color.White,
                    radius = strokePx * 0.35f,
                    center = Offset(dotX, dotY)
                )
            }
        }

        // Center Content of the Progress Ring
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "$percentInt%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (isAchieved) EmeraldSuccess else Color.White,
                modifier = Modifier.testTag("progress_ring_percent_text")
            )

            Text(
                text = if (isAchieved) "GOAL MET! 🎉" else "COMPLETE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isAchieved) EmeraldSuccess else Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${formatHours(hoursCompleted)} / ${formatHours(targetHours)}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = CyanAccent
            )
        }
    }
}

/**
 * Dialog allowing users to interactively set and adjust their daily target hours.
 */
@Composable
fun AdjustTargetHoursDialog(
    currentTargetHours: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var selectedHours by remember { mutableFloatStateOf(currentTargetHours) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Adjust Daily Target Hours",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("adjust_target_hours_dialog"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Set your ideal daily learning duration. Progress updates dynamically as you complete quizzes and focus sessions.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                // Large Hours Display
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${formatHours(selectedHours)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanAccent,
                            modifier = Modifier.testTag("dialog_target_hours_text")
                        )
                        Text(
                            text = "Equivalent to ${(selectedHours * 60).toInt()} minutes per day",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                // Slider Control
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.25 hrs (15m)", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("6.0 hrs", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    Slider(
                        value = selectedHours,
                        onValueChange = { selectedHours = (Math.round(it * 4f) / 4f).coerceIn(0.25f, 6.0f) },
                        valueRange = 0.25f..6.0f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAccent,
                            activeTrackColor = CyanAccent,
                            inactiveTrackColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_hours_slider")
                    )
                }

                // Quick presets
                Text(
                    text = "RECOMMENDED COMMITMENTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0.5f to "Casual (30m)",
                        1.0f to "Steady (1h)",
                        1.5f to "Focused (1.5h)",
                        2.0f to "Intense (2h)"
                    ).forEach { (hrs, label) ->
                        val isSel = kotlin.math.abs(selectedHours - hrs) < 0.05f
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) CyanAccent.copy(alpha = 0.2f) else Color(0xFF131D31),
                            border = BorderStroke(1.dp, if (isSel) CyanAccent else Color(0xFF1E2E4A)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedHours = hrs }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = formatHours(hrs),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) CyanAccent else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedHours) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_adjust_target_hours_button")
            ) {
                Text("Set Goal", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_adjust_target_hours_button")
            ) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Utility helper to format hours nicely (e.g., "1.5 hrs" or "1 hr").
 */
fun formatHours(hours: Float): String {
    return if (hours % 1f == 0f) {
        "${hours.toInt()} hrs"
    } else {
        String.format(Locale.US, "%.1f hrs", hours)
    }
}
