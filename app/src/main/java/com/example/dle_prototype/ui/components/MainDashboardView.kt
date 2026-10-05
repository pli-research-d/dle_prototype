package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.data.LearningModule
import com.example.dle_prototype.data.ModuleStatus
import com.example.dle_prototype.data.User
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent
import java.util.Calendar

/**
 * Main Dashboard UI displaying:
 * 1. Current Progress (Overall level, XP, completion %, accuracy, daily target)
 * 2. Daily Streak (Hero streak flame, weekly progress pills, personal best)
 * 3. List of Active Learning Modules (Rich interactive module cards with progress & actions)
 */
@Composable
fun MainDashboardView(
    user: User,
    dailyStreak: Int,
    longestStreak: Int,
    totalXp: Int,
    totalQuizzes: Int,
    averageAccuracy: Float,
    dailyGoalProgress: DailyGoalProgress,
    activeModules: List<LearningModule>,
    recentAttempts: List<com.example.dle_prototype.data.QuizAttempt> = emptyList(),
    onStartModule: (categoryName: String, categoryNumber: Float) -> Unit,
    onStartPractice: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onUpdateTargetHours: (Float) -> Unit = {},
    onOpenSummarizer: () -> Unit = {},
    peakAnalysis: com.example.dle_prototype.data.ml.PeakLearningHoursAnalysis? = null,
    spacedRepetitionOverview: com.example.dle_prototype.data.ml.SpacedRepetitionOverview? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val resolvedPeakAnalysis = remember(recentAttempts, peakAnalysis) {
        peakAnalysis ?: com.example.dle_prototype.data.ml.PeakLearningHoursAnalyzer.analyze(recentAttempts)
    }

    val filteredModules = remember(activeModules, selectedFilter) {
        when (selectedFilter) {
            "IN_PROGRESS" -> activeModules.filter { it.status == ModuleStatus.IN_PROGRESS }
            "REVIEW" -> activeModules.filter { it.status == ModuleStatus.REVIEW_DUE }
            "COMPLETED" -> activeModules.filter { it.status == ModuleStatus.COMPLETED }
            else -> activeModules
        }
    }

    val currentLevel = (totalXp / 250).coerceAtLeast(1)
    val levelProgress = ((totalXp % 250).toFloat() / 250f).coerceIn(0f, 1f)
    val avgModuleProgress = if (activeModules.isNotEmpty()) {
        activeModules.map { it.progressPercent }.average().toFloat()
    } else 45f

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_dashboard_view")
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // =====================================================================
        // SECTION 1: USER CURRENT PROGRESS
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("current_progress_section")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Row: Avatar & Greeting + Level Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyanAccent.copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🚀", fontSize = 24.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Welcome back, You!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Learning Engine Active · On-Track",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Level Chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CyanAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CyanAccent)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Level $currentLevel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanAccent
                            )
                        }
                    }
                }

                // Level XP Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level $currentLevel Progress",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "$totalXp XP · Next level at ${(currentLevel + 1) * 250} XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                    LinearProgressIndicator(
                        progress = { levelProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CyanAccent,
                        trackColor = Color(0xFF1E293B)
                    )
                }

                // 3 Metrics Columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 1: Course Mastery
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${avgModuleProgress.toInt()}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanAccent
                            )
                            Text(
                                text = "Curriculum",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Metric 2: Quizzes
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "$totalQuizzes",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Quizzes Done",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Metric 3: Accuracy
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${averageAccuracy.toInt()}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldSuccess
                            )
                            Text(
                                text = "Accuracy",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Daily Goal Row
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (dailyGoalProgress.isAchieved) EmeraldSuccess else CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (dailyGoalProgress.isAchieved) "Today's Target Reached! 🎉" else "Daily Target: ${dailyGoalProgress.answeredToday}/${dailyGoalProgress.targetQuestions} questions",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${dailyGoalProgress.percentComplete.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (dailyGoalProgress.isAchieved) EmeraldSuccess else CyanAccent
                        )
                    }
                }
            }
        }

        // =====================================================================
        // SECTION 1B: DAILY LEARNING GOAL PROGRESS RING & TARGET HOURS
        // =====================================================================
        DailyGoalProgressRingCard(
            goalProgress = dailyGoalProgress,
            onUpdateTargetHours = onUpdateTargetHours,
            onStartPractice = onStartPractice
        )

        // =====================================================================
        // SECTION 1C: HISTORICAL PEAK LEARNING HOURS & STUDY NOTIFICATIONS
        // =====================================================================
        PeakStudyNotificationCard(
            username = user.username,
            analysis = resolvedPeakAnalysis,
            onStartPractice = onStartPractice
        )

        // =====================================================================
        // SECTION 2: DAILY STREAK DISPLAY
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1D1408),
            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_streak_section")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Streak Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AmberAccent.copy(alpha = 0.2f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Flame",
                                    tint = AmberAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "$dailyStreak Day Streak!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AmberAccent
                                )
                                Text("🔥", fontSize = 16.sp)
                            }
                            Text(
                                text = if (dailyStreak > 0) "Momentum is active! Keep it rolling today." else "Start your learning streak today!",
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    // Best streak badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2C1E0A),
                        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Best: ${maxOf(longestStreak, dailyStreak)}d",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // 7-Day Streak Calendar Tracker (Mon - Sun)
                val cal = Calendar.getInstance()
                val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEachIndexed { index, label ->
                        // Map 0..6 to Monday=2 ... Sunday=1
                        val dayIndex = if (index == 6) 1 else index + 2
                        val isToday = dayIndex == currentDayOfWeek
                        val isPastOrCompleted = index < ((currentDayOfWeek + 5) % 7) || (isToday && dailyStreak > 0)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isToday) FontWeight.Black else FontWeight.Bold,
                                color = if (isToday) AmberAccent else Color(0xFF94A3B8)
                            )
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isPastOrCompleted -> AmberAccent
                                    isToday -> AmberAccent.copy(alpha = 0.3f)
                                    else -> Color(0xFF2B2011)
                                },
                                border = if (isToday) BorderStroke(1.5.dp, AmberAccent) else null,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isPastOrCompleted) {
                                        Icon(
                                            Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else if (isToday) {
                                        Text("•", color = AmberAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Safety Freeze / Freeze Shield Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Streak Freeze Active (1 protection remaining)",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Text(
                        text = "Protected",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            }
        }

        // =====================================================================
        // SECTION 2B: RECHARTS WEEKLY STREAK ACTIVITY LINE GRAPH
        // =====================================================================
        RechartsStreakLineGraphCard(
            dailyStreak = dailyStreak,
            longestStreak = longestStreak,
            recentAttempts = recentAttempts,
            weeklyXp = totalXp
        )

        // =====================================================================
        // SECTION 2C: ON-DEVICE AI SUMMARIZER TOOL CARD
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSummarizer() }
                .testTag("dashboard_summarizer_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "AI Summarizer",
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "AI Concept Summarizer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "TFLite",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Compress articles & study text into high-impact bullet points",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Button(
                    onClick = onOpenSummarizer,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("launch_summarizer_button")
                ) {
                    Text("Summarize", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
            }
        }

        // =====================================================================
        // SECTION 2D: SPACED REPETITION SCHEDULER QUEUE
        // =====================================================================
        spacedRepetitionOverview?.let { overview ->
            SpacedRepetitionQueueCard(
                overview = overview,
                onStartReview = onStartModule
            )
        }

        // =====================================================================
        // SECTION 3: ACTIVE LEARNING MODULES
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_learning_modules_section"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section Header & Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ACTIVE LEARNING MODULES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyanAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${activeModules.size} Tracks",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Tap to practice",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Filter Chips: [ All ] [ In Progress ] [ Review ] [ Completed ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "All (${activeModules.size})",
                    "IN_PROGRESS" to "In Progress",
                    "REVIEW" to "Review Due",
                    "COMPLETED" to "Completed"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedFilter == key) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == key,
                            borderColor = Color(0xFF1E293B),
                            selectedBorderColor = CyanAccent
                        )
                    )
                }
            }

            // List of Module Cards
            filteredModules.forEach { module ->
                ModuleCardItem(
                    module = module,
                    onStart = {
                        onStartModule(module.categoryName, module.categoryNumber)
                    }
                )
            }
        }
    }
}

/**
 * Interactive card displaying an active learning module with progress bar,
 * status badge, current lesson title, and action button.
 */
@Composable
private fun ModuleCardItem(
    module: LearningModule,
    onStart: () -> Unit
) {
    val statusColor = when (module.status) {
        ModuleStatus.COMPLETED -> EmeraldSuccess
        ModuleStatus.REVIEW_DUE -> AmberAccent
        ModuleStatus.IN_PROGRESS -> CyanAccent
        ModuleStatus.NOT_STARTED -> Color(0xFF94A3B8)
    }

    val statusText = when (module.status) {
        ModuleStatus.COMPLETED -> "Completed"
        ModuleStatus.REVIEW_DUE -> "Review Due"
        ModuleStatus.IN_PROGRESS -> "In Progress"
        ModuleStatus.NOT_STARTED -> "Ready"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("module_card_${module.id}")
            .clickable { onStart() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Icon + Title + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = module.icon, fontSize = 20.sp)
                        }
                    }
                    Column {
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = module.tag,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("•", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text(
                                text = module.difficulty,
                                fontSize = 10.sp,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Current Topic / Lesson Description
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF131D31),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current: ${module.currentTopic}",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "~${module.estimatedTimeMinutes} min",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Progress Bar and Metric Subtitle
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${module.completedLessons}/${module.totalLessons} Lessons",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "${module.progressPercent.toInt()}% Complete",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                LinearProgressIndicator(
                    progress = { module.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Action Button: "Continue Module"
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (module.status == ModuleStatus.REVIEW_DUE) AmberAccent else CyanAccent
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_continue_module_${module.id}")
            ) {
                Icon(
                    imageVector = if (module.status == ModuleStatus.REVIEW_DUE) Icons.Default.Refresh else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (module.status == ModuleStatus.REVIEW_DUE) "Review Module" else if (module.status == ModuleStatus.COMPLETED) "Practice Again" else "Continue Module",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
