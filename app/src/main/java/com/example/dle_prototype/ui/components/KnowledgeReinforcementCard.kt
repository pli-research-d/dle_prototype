package com.example.dle_prototype.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.graph.KnowledgeMasteryLevel
import com.example.dle_prototype.data.graph.KnowledgeReinforcementSchedule
import com.example.dle_prototype.data.graph.KnowledgeSpacedRepetitionSummary
import com.example.dle_prototype.data.graph.TopicMasteryStatus
import com.example.dle_prototype.notifications.KnowledgeSpacedRepetitionNotificationManager
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent
import java.util.Locale

/**
 * Spaced Repetition Scheduling Card calibrated to Knowledge Graph Mastery Levels.
 * Prioritizes learning materials based on memory decay curves, topic mastery tiers
 * (Beginner, Intermediate, Expert), and graph prerequisite dependencies.
 * Provides controls for configuring and testing rich push notification reminders.
 */
@Composable
fun KnowledgeReinforcementCard(
    summary: KnowledgeSpacedRepetitionSummary,
    username: String,
    onStartReview: (categoryName: String, categoryNumber: Float) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationFeedback: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("DUE") }
    var notificationsEnabled by remember {
        mutableStateOf(KnowledgeSpacedRepetitionNotificationManager.isReminderEnabled(context))
    }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var scheduledHour by remember {
        mutableStateOf(KnowledgeSpacedRepetitionNotificationManager.getScheduledHour(context))
    }
    var retentionThreshold by remember {
        mutableStateOf(KnowledgeSpacedRepetitionNotificationManager.getRetentionThreshold(context))
    }

    val filteredQueue = remember(summary.schedules, selectedFilter) {
        when (selectedFilter) {
            "DUE" -> summary.schedules.filter { it.isOverdue }
            "PREREQS" -> summary.schedules.filter { it.prerequisiteImpactCount > 0 }
            "BEGINNER" -> summary.schedules.filter { it.masteryLevel == KnowledgeMasteryLevel.BEGINNER }
            "INTERMEDIATE" -> summary.schedules.filter { it.masteryLevel == KnowledgeMasteryLevel.INTERMEDIATE }
            "EXPERT" -> summary.schedules.filter { it.masteryLevel == KnowledgeMasteryLevel.EXPERT }
            else -> summary.schedules
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.40f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("knowledge_reinforcement_card")
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
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Spaced Repetition Mastery",
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "KNOWLEDGE REINFORCEMENT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                        Text(
                            text = "Memory Decay by Mastery Level · SM-2 Scheduling",
                            fontSize = 11.sp,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("reinforcement_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Notification Settings",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                    ) {
                        Text(
                            text = "${summary.dueNowCount} Due Now",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.dueNowCount > 0) RoseAccent else EmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Quick Metric Counters
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
                            text = "${summary.averageRetentionPercent}%",
                            fontSize = 17.sp,
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

                // Metric 2: Prerequisite Anchors
                Surface(
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${summary.dependentPrereqAlertsCount} Anchors",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (summary.dependentPrereqAlertsCount > 0) AmberAccent else EmeraldSuccess
                        )
                        Text(
                            text = "Prereqs At Risk",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Metric 3: Critical Overdue
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
                            text = "${summary.criticalCount}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = if (summary.criticalCount > 0) RoseAccent else EmeraldSuccess
                        )
                        Text(
                            text = "Critical Queue",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Notification Reminder Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF131D31),
                border = BorderStroke(1.dp, if (notificationsEnabled) CyanAccent.copy(alpha = 0.35f) else Color(0xFF1E2E4A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (notificationsEnabled) CyanAccent else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (notificationsEnabled) "Push Reminders Active" else "Reminders Paused",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Daily alerts when retention < $retentionThreshold%",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val topItem = summary.mostUrgentSchedule
                                if (topItem != null) {
                                    KnowledgeSpacedRepetitionNotificationManager.sendReinforcementNotification(
                                        context, topItem, username
                                    )
                                } else {
                                    KnowledgeSpacedRepetitionNotificationManager.sendTestNotification(
                                        context, username, com.example.dle_prototype.data.DatabaseHelper.getInstance(context)
                                    )
                                }
                                onNotificationFeedback?.invoke("Spaced repetition reminder sent to notification tray!")
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("test_reinforcement_push_button")
                        ) {
                            Text("Test Push", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { isChecked ->
                                notificationsEnabled = isChecked
                                KnowledgeSpacedRepetitionNotificationManager.setReminderEnabled(context, isChecked)
                                onNotificationFeedback?.invoke(
                                    if (isChecked) "Reinforcement notification reminders enabled!" else "Reinforcement notifications disabled."
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0F172A),
                                checkedTrackColor = CyanAccent,
                                uncheckedThumbColor = Color(0xFF64748B),
                                uncheckedTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.testTag("reinforcement_notifications_switch")
                        )
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
                    "DUE" to "Due Now (${summary.dueNowCount})",
                    "PREREQS" to "Prereqs (${summary.dependentPrereqAlertsCount})",
                    "ALL" to "All Topics (${summary.schedules.size})",
                    "BEGINNER" to "Beginner 🥉 (${summary.schedules.count { it.masteryLevel == KnowledgeMasteryLevel.BEGINNER }})",
                    "INTERMEDIATE" to "Intermediate 🥈 (${summary.schedules.count { it.masteryLevel == KnowledgeMasteryLevel.INTERMEDIATE }})",
                    "EXPERT" to "Expert 🥇 (${summary.schedules.count { it.masteryLevel == KnowledgeMasteryLevel.EXPERT }})"
                ).forEach { (key, label) ->
                    val isSel = selectedFilter == key
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedFilter = key },
                        label = {
                            Text(
                                label,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
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
                            selected = isSel,
                            borderColor = Color(0xFF1E293B),
                            selectedBorderColor = CyanAccent
                        ),
                        modifier = Modifier.testTag("filter_reinforce_$key")
                    )
                }
            }

            // Prioritized Review Topics List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (filteredQueue.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131D31),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🎉 All Synaptic Connections Consolidated!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            Text("No topics matching this filter are currently due for reinforcement.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                } else {
                    filteredQueue.forEachIndexed { index, schedule ->
                        KnowledgeReinforcementQueueItem(
                            item = schedule,
                            rankNumber = index + 1,
                            onReviewClick = { onStartReview(schedule.category, schedule.categoryNumber) },
                            onPushNotificationClick = {
                                KnowledgeSpacedRepetitionNotificationManager.sendReinforcementNotification(
                                    context, schedule, username
                                )
                                onNotificationFeedback?.invoke("Dispatched reinforcement alert for ${schedule.title}!")
                            }
                        )
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "⚙️ Spaced Repetition Notification Settings",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Customize when and how the spaced repetition system pushes reminders to reinforce knowledge graph topics.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    // Retention threshold slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Retention Alert Threshold", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("$retentionThreshold%", fontSize = 12.sp, color = CyanAccent, fontWeight = FontWeight.Black)
                        }
                        Text(
                            text = "Alerts trigger when estimated memory decay falls below this percentage.",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Slider(
                            value = retentionThreshold.toFloat(),
                            onValueChange = { retentionThreshold = it.toInt() },
                            valueRange = 50f..85f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanAccent,
                                activeTrackColor = CyanAccent
                            ),
                            modifier = Modifier.testTag("retention_threshold_slider")
                        )
                    }

                    // Scheduled Hour Selector
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Daily Alert Time", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            val displayHour = if (scheduledHour == 0) "12 AM" else if (scheduledHour < 12) "$scheduledHour AM" else if (scheduledHour == 12) "12 PM" else "${scheduledHour - 12} PM"
                            Text(displayHour, fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.Black)
                        }
                        Slider(
                            value = scheduledHour.toFloat(),
                            onValueChange = { scheduledHour = it.toInt() },
                            valueRange = 6f..22f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldSuccess,
                                activeTrackColor = EmeraldSuccess
                            ),
                            modifier = Modifier.testTag("scheduled_hour_slider")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        KnowledgeSpacedRepetitionNotificationManager.setScheduledTime(context, scheduledHour, 0)
                        KnowledgeSpacedRepetitionNotificationManager.setRetentionThreshold(context, retentionThreshold)
                        showSettingsDialog = false
                        onNotificationFeedback?.invoke("Spaced repetition reminder preferences updated!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Save Preferences", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}

@Composable
fun KnowledgeReinforcementQueueItem(
    item: KnowledgeReinforcementSchedule,
    rankNumber: Int,
    onReviewClick: () -> Unit,
    onPushNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tierColor = item.masteryLevel.ringColor
    val statusColor = item.status.color

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF131D31),
        border = BorderStroke(
            1.dp,
            if (item.isOverdue) RoseAccent.copy(alpha = 0.6f) else Color(0xFF1E2E4A)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("reinforce_item_${item.nodeId}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Rank · Topic Name · Mastery Rings Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Mini Concentric Ring Visualizer for KnowledgeMasteryLevel
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(tierColor.copy(alpha = 0.15f), CircleShape)
                            .border(
                                width = if (item.masteryLevel == KnowledgeMasteryLevel.EXPERT) 2.5.dp else 1.5.dp,
                                color = tierColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.masteryLevel.iconEmoji,
                            fontSize = 12.sp
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            if (item.prerequisiteImpactCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AmberAccent.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "⚓ Anchor (${item.prerequisiteImpactCount})",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberAccent,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.dueStatusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.isOverdue) RoseAccent else CyanAccent
                            )
                            Text(
                                text = "•",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "${item.masteryLevel.badgeText} (${item.accuracyPercent.toInt()}%)",
                                fontSize = 10.sp,
                                color = tierColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Push Alert Button for this item
                IconButton(
                    onClick = onPushNotificationClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("push_alert_item_${item.nodeId}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send notification for ${item.title}",
                        tint = CyanAccent.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Retention & Decay Bar
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
                        text = "Interval: ${String.format(Locale.US, "%.1f", item.scheduledIntervalDays)}d",
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
                        item.retentionProbability >= 0.80f -> EmeraldSuccess
                        item.retentionProbability >= 0.65f -> CyanAccent
                        item.retentionProbability >= 0.50f -> AmberAccent
                        else -> RoseAccent
                    },
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Reasoning & Knowledge Graph Explanation
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
                        text = item.reinforcementReason,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 14.sp
                    )
                }
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.attemptsCount} sessions recorded",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )

                Button(
                    onClick = onReviewClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isOverdue) RoseAccent else CyanAccent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("reinforce_button_${item.nodeId}")
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
                            text = item.recommendedAction,
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
