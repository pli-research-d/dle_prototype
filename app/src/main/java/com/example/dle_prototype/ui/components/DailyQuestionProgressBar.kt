package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent

/**
 * Visual progress bar component that tracks user daily question completion against their configured goal.
 * Features animated Material 3 progress indicators with milestone ticks, dynamic color transitions,
 * and remaining questions countdown.
 */
@Composable
fun DailyQuestionProgressBar(
    answeredToday: Int,
    targetQuestions: Int,
    modifier: Modifier = Modifier,
    percentComplete: Float = (if (targetQuestions > 0) answeredToday.toFloat() / targetQuestions.toFloat() else 0f).coerceIn(0f, 1f),
    isAchieved: Boolean = answeredToday >= targetQuestions && targetQuestions > 0,
    showLabels: Boolean = true,
    showMilestones: Boolean = true,
    showRemainingText: Boolean = true,
    trackHeight: Dp = 12.dp
) {
    // Smooth animated progress value
    val animatedProgress by animateFloatAsState(
        targetValue = percentComplete.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "daily_question_progress"
    )

    // Dynamic color theme based on completion stage
    val activeColor = when {
        isAchieved -> EmeraldSuccess
        percentComplete >= 0.75f -> Color(0xFF10B981) // Vibrant green
        percentComplete >= 0.40f -> IndigoPrimaryLight // Indigo / purple
        else -> CyanAccent // Electric cyan
    }

    val trackBgColor = Color(0xFF1E293B)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_question_progress_bar"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showLabels) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Large Questions Counter
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$answeredToday",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = activeColor,
                        modifier = Modifier.testTag("daily_goal_answered_count")
                    )
                    Text(
                        text = "/ $targetQuestions questions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier
                            .padding(bottom = 3.dp)
                            .testTag("daily_goal_target_count")
                    )
                }

                // Completion Pill Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = activeColor.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.35f)),
                    modifier = Modifier.testTag("daily_goal_percentage")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isAchieved) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                        } else if (answeredToday > 0) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (isAchieved) "GOAL COMPLETED" else "${(percentComplete * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Material 3 LinearProgressIndicator + Custom Visual Track Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .testTag("daily_goal_progress_indicator")
        ) {
            // Material 3 Progress Indicator
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2)),
                color = activeColor,
                trackColor = trackBgColor,
                strokeCap = StrokeCap.Round
            )

            // High-finish ambient gradient sheen on active portion
            if (animatedProgress > 0.03f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(trackHeight)
                        .clip(RoundedCornerShape(trackHeight / 2))
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isAchieved) {
                                    listOf(
                                        EmeraldSuccess.copy(alpha = 0.9f),
                                        Color(0xFF34D399),
                                        EmeraldSuccess
                                    )
                                } else {
                                    listOf(
                                        CyanAccent.copy(alpha = 0.85f),
                                        IndigoPrimaryLight,
                                        activeColor
                                    )
                                }
                            )
                        )
                )
            }
        }

        // Milestone Pips (25%, 50%, 75%, 100%)
        if (showMilestones) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val milestones = listOf(0.25f to "25%", 0.50f to "50%", 0.75f to "75%", 1.00f to "Goal")
                milestones.forEach { (fraction, label) ->
                    val reached = percentComplete >= fraction
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (reached) activeColor else Color(0xFF475569)
                                )
                        )
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
                            color = if (reached) activeColor else Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Remaining or Completion Subtitle
        if (showRemainingText) {
            val remaining = (targetQuestions - answeredToday).coerceAtLeast(0)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_goal_remaining"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAchieved) {
                        "🎉 Fantastic work! Daily learning goal conquered today."
                    } else if (remaining == 1) {
                        "🔥 Only 1 more question to reach today's target!"
                    } else {
                        "⚡ $remaining questions remaining to complete your goal"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = if (isAchieved) EmeraldSuccess else Color(0xFFCBD5E1),
                    fontWeight = if (isAchieved) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Convenience overload that directly accepts [DailyGoalProgress]
 */
@Composable
fun DailyQuestionProgressBar(
    goalProgress: DailyGoalProgress,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    showMilestones: Boolean = true,
    showRemainingText: Boolean = true,
    trackHeight: Dp = 12.dp
) {
    DailyQuestionProgressBar(
        answeredToday = goalProgress.answeredToday,
        targetQuestions = goalProgress.targetQuestions,
        percentComplete = goalProgress.percentComplete,
        isAchieved = goalProgress.isAchieved,
        modifier = modifier,
        showLabels = showLabels,
        showMilestones = showMilestones,
        showRemainingText = showRemainingText,
        trackHeight = trackHeight
    )
}

/**
 * Compact circular visual progress indicator for header or widget placement.
 */
@Composable
fun DailyQuestionCircularGauge(
    answeredToday: Int,
    targetQuestions: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp
) {
    val percentComplete = (if (targetQuestions > 0) answeredToday.toFloat() / targetQuestions.toFloat() else 0f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = percentComplete,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "daily_question_circular_progress"
    )

    val isAchieved = answeredToday >= targetQuestions && targetQuestions > 0
    val activeColor = if (isAchieved) EmeraldSuccess else CyanAccent

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(size),
            color = activeColor,
            trackColor = Color(0xFF1E293B),
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round
        )

        if (isAchieved) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Achieved",
                tint = EmeraldSuccess,
                modifier = Modifier.size(size * 0.45f)
            )
        } else {
            Text(
                text = "${(percentComplete * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
