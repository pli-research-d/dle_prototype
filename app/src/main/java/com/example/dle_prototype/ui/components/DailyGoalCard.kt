package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight

@Composable
fun DailyGoalCard(
    goalProgress: DailyGoalProgress,
    onUpdateGoal: (Int) -> Unit,
    onStartPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoalSettingsDialog by remember { mutableStateOf(false) }
    var tempTarget by remember(goalProgress.targetQuestions) {
        mutableIntStateOf(goalProgress.targetQuestions)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            1.dp,
            if (goalProgress.isAchieved) EmeraldSuccess.copy(alpha = 0.5f) else CyanAccent.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_goal_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Bullseye Icon + Title + Edit Goal Icon
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
                            .clip(CircleShape)
                            .background(
                                if (goalProgress.isAchieved)
                                    EmeraldSuccess.copy(alpha = 0.15f)
                                else
                                    CyanAccent.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (goalProgress.isAchieved) Icons.Default.CheckCircle else Icons.Default.TrackChanges,
                            contentDescription = "Daily Goal",
                            tint = if (goalProgress.isAchieved) EmeraldSuccess else CyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Daily Learning Goal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (goalProgress.isAchieved) EmeraldSuccess.copy(alpha = 0.18f) else CyanAccent.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = if (goalProgress.isAchieved) "ACHIEVED" else "${(goalProgress.percentComplete * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (goalProgress.isAchieved) EmeraldSuccess else CyanAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Answer questions daily to lock in knowledge retention",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Edit Button
                IconButton(
                    onClick = {
                        tempTarget = goalProgress.targetQuestions
                        showGoalSettingsDialog = true
                    },
                    modifier = Modifier.testTag("adjust_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Adjust Daily Goal",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Visual Progress Bar Component tracking daily question completion against goal
            DailyQuestionProgressBar(
                goalProgress = goalProgress,
                showLabels = true,
                showMilestones = true,
                showRemainingText = true,
                trackHeight = 12.dp
            )

            // Quick Call-to-Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090E1A))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (goalProgress.isAchieved)
                        "⚡ Keep going for extra mastery points"
                    else
                        "🎯 Complete a quiz to hit your target",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onStartPractice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (goalProgress.isAchieved) EmeraldSuccess else CyanAccent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("practice_for_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (goalProgress.isAchieved) "Bonus Quiz" else "Start Quiz",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // Goal Configuration Dialog
    if (showGoalSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showGoalSettingsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.TrackChanges, contentDescription = null, tint = CyanAccent)
                    Text("Set Daily Question Goal")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Choose how many questions you want to practice each day. Consistent daily practice reinforces cognitive neuroplasticity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Stepper control: Minus, Number, Plus
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (tempTarget > 3) tempTarget -= 1 },
                            enabled = tempTarget > 3,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .testTag("goal_minus_button")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease goal", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$tempTarget",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = CyanAccent
                            )
                            Text(
                                text = "questions / day",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        IconButton(
                            onClick = { if (tempTarget < 50) tempTarget += 1 },
                            enabled = tempTarget < 50,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .testTag("goal_plus_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase goal", tint = Color.White)
                        }
                    }

                    // Preset Quick-Select Chips
                    Text(
                        text = "Quick Presets:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )

                    val presets = listOf(
                        Pair(5, "Casual"),
                        Pair(10, "Standard"),
                        Pair(15, "Dedicated"),
                        Pair(20, "Scholar"),
                        Pair(30, "Intense")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { (count, label) ->
                            val isSelected = (tempTarget == count)
                            FilterChip(
                                selected = isSelected,
                                onClick = { tempTarget = count },
                                label = {
                                    Text(
                                        text = "$count ($label)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanAccent,
                                    containerColor = Color(0xFF090E1A),
                                    labelColor = Color(0xFF94A3B8)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) CyanAccent else Color(0xFF1E293B)
                                ),
                                modifier = Modifier.testTag("goal_preset_$count")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateGoal(tempTarget)
                        showGoalSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.testTag("save_goal_button")
                ) {
                    Text("Save Goal", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DailyGoalStepperSheet(
    currentProgress: DailyGoalProgress,
    onDismiss: () -> Unit,
    onGoalChanged: (Int) -> Unit
) {
    var tempTarget by remember(currentProgress.targetQuestions) {
        mutableIntStateOf(currentProgress.targetQuestions)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.TrackChanges, contentDescription = null, tint = CyanAccent)
                Text("Daily Practice Goal")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Choose how many questions to target each day. Regular daily consistency accelerates retention.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (tempTarget > 3) tempTarget-- },
                        enabled = tempTarget > 3,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$tempTarget",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CyanAccent
                        )
                        Text("questions / day", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    IconButton(
                        onClick = { if (tempTarget < 50) tempTarget++ },
                        enabled = tempTarget < 50,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                    }
                }

                // Presets
                val presets = listOf(5, 10, 15, 20)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { count ->
                        val isSel = tempTarget == count
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) CyanAccent.copy(alpha = 0.2f) else Color(0xFF1E293B),
                            border = BorderStroke(1.dp, if (isSel) CyanAccent else Color(0xFF334155)),
                            modifier = Modifier.weight(1f).clickable { tempTarget = count }
                        ) {
                            Text(
                                text = "$count q",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) CyanAccent else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGoalChanged(tempTarget) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Save Target", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

