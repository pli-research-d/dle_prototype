package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.ml.SpacedRepetitionOverview
import com.example.dle_prototype.data.ml.TopicReviewSchedule
import com.example.dle_prototype.data.ml.UrgencyTier
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent

/**
 * UI Component displaying the algorithmic spaced repetition review queue.
 * Prioritizes learning materials and quiz topics based on memory decay, past performance,
 * and scheduled review intervals.
 */
@Composable
fun SpacedRepetitionQueueCard(
    overview: SpacedRepetitionOverview,
    onStartReview: (categoryName: String, categoryNumber: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredQueue = remember(overview.prioritizedQueue, selectedFilter) {
        when (selectedFilter) {
            "DUE" -> overview.prioritizedQueue.filter {
                it.isOverdue || it.urgencyTier == UrgencyTier.CRITICAL_OVERDUE || it.urgencyTier == UrgencyTier.DUE_TODAY
            }
            "SOLIDIFYING" -> overview.prioritizedQueue.filter { it.urgencyTier == UrgencyTier.SOLIDIFYING }
            "MASTERED" -> overview.prioritizedQueue.filter { it.urgencyTier == UrgencyTier.MASTERED }
            else -> overview.prioritizedQueue
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("spaced_repetition_queue_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
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
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Spaced Repetition",
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "SPACED REPETITION QUEUE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                        Text(
                            text = "SM-2 Forgetting Curve · Memory Retention Optimization",
                            fontSize = 11.sp,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                ) {
                    Text(
                        text = "${overview.dueNowCount} Due Now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (overview.dueNowCount > 0) RoseAccent else EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Top Metric Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Retention Rate
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${overview.averageRetentionPercent}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanAccent
                        )
                        Text(
                            text = "Avg Retention",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Metric 2: Next Action Topic
                Surface(
                    modifier = Modifier.weight(1.8f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Highest Priority:",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8)
                            )
                            overview.mostUrgentTopic?.let {
                                Text(
                                    text = "${it.iconEmoji} ${it.topicName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = it.urgencyTier.badgeColor
                                )
                            }
                        }
                        overview.mostUrgentTopic?.let {
                            Text(
                                text = it.dueStatusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "All Topics (${overview.prioritizedQueue.size})",
                    "DUE" to "Due Now (${overview.dueNowCount})",
                    "SOLIDIFYING" to "Solidifying (${overview.prioritizedQueue.count { it.urgencyTier == UrgencyTier.SOLIDIFYING }})",
                    "MASTERED" to "Mastered (${overview.prioritizedQueue.count { it.urgencyTier == UrgencyTier.MASTERED }})"
                ).forEach { (key, label) ->
                    val isSel = selectedFilter == key
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF131D31),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSel,
                            borderColor = Color(0xFF1E293B),
                            selectedBorderColor = CyanAccent
                        ),
                        modifier = Modifier.testTag("filter_spaced_$key")
                    )
                }
            }

            // Prioritized Review Topics List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredQueue.forEachIndexed { index, schedule ->
                    TopicReviewQueueItem(
                        item = schedule,
                        rankNumber = index + 1,
                        onReviewClick = { onStartReview(schedule.topicName, schedule.categoryNumber) }
                    )
                }
            }
        }
    }
}

@Composable
fun TopicReviewQueueItem(
    item: TopicReviewSchedule,
    rankNumber: Int,
    onReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tierColor = item.urgencyTier.badgeColor

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF131D31),
        border = BorderStroke(1.dp, if (item.isOverdue) tierColor.copy(alpha = 0.6f) else Color(0xFF1E2E4A)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("spaced_topic_item_${item.topicName}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Rank · Topic Name · Urgency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#$rankNumber",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Text(text = item.iconEmoji, fontSize = 20.sp)

                    Column {
                        Text(
                            text = item.topicName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = item.dueStatusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tierColor
                        )
                    }
                }

                // Urgency Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tierColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, tierColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = item.urgencyTier.label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = tierColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Retention & Stability Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Memory Retention: ${(item.retentionProbability * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Interval: ${String.format(java.util.Locale.US, "%.1f", item.scheduledIntervalDays)}d",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                }

                LinearProgressIndicator(
                    progress = { item.retentionProbability },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when {
                        item.retentionProbability >= 0.85f -> EmeraldSuccess
                        item.retentionProbability >= 0.65f -> CyanAccent
                        item.retentionProbability >= 0.50f -> AmberAccent
                        else -> RoseAccent
                    },
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Algorithm Reason Explanation
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0B132B),
                border = BorderStroke(1.dp, Color(0xFF1E2E4A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = item.reasonExplanation,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 14.sp
                    )
                }
            }

            // Action Row: Review Button + Past Accuracy Stat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Past Accuracy: ${item.historicalAccuracyPercent.toInt()}% (${item.totalAttempts} sessions)",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )

                Button(
                    onClick = onReviewClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isOverdue) tierColor else CyanAccent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("start_review_${item.topicName}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (item.isOverdue) "Review Now" else "Practice",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }
    }
}
