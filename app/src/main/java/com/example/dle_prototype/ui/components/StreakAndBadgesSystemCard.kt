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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.UserGamificationState
import com.example.dle_prototype.data.UserStreakData
import com.example.dle_prototype.data.badges.BadgeCategory
import com.example.dle_prototype.data.badges.BadgeTier
import com.example.dle_prototype.data.badges.DigitalBadge
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data model representing daily activity in the rolling 7-day consistency calendar.
 */
data class RollingDayConsistency(
    val dayLabel: String,      // e.g. "M", "T", "W"
    val dateFormatted: String, // e.g. "Oct 12"
    val dateIso: String,       // e.g. "2026-10-12"
    val isCompleted: Boolean,
    val isToday: Boolean,
    val questionsAnswered: Int = 0,
    val isFreezeProtected: Boolean = false
)

/**
 * Milestone target data structure for streak progression.
 */
data class StreakMilestoneTarget(
    val targetDays: Int,
    val title: String,
    val emoji: String,
    val xpReward: Int
)

val STREAK_MILESTONES = listOf(
    StreakMilestoneTarget(1, "Streak Spark", "⚡", 50),
    StreakMilestoneTarget(3, "3-Day Flame", "🔥", 100),
    StreakMilestoneTarget(5, "5-Day Momentum", "🚀", 175),
    StreakMilestoneTarget(7, "7-Day Inferno", "🌋", 300),
    StreakMilestoneTarget(14, "14-Day Titan", "⚔️", 600),
    StreakMilestoneTarget(30, "30-Day Legend", "👑", 1500)
)

/**
 * Computes multiplier bonus from learning streak consistency.
 */
fun getStreakXpMultiplier(streak: Int): Float {
    return when {
        streak >= 30 -> 2.0f
        streak >= 14 -> 1.50f
        streak >= 7 -> 1.25f
        streak >= 3 -> 1.15f
        streak >= 1 -> 1.05f
        else -> 1.0f
    }
}

/**
 * Computes rolling 7-day consistency data from user attempts and streak information.
 */
fun computeRolling7DayConsistency(
    attempts: List<QuizAttempt>,
    streakData: UserStreakData?
): List<RollingDayConsistency> {
    val result = mutableListOf<RollingDayConsistency>()
    val cal = Calendar.getInstance()
    val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    val dayFormat = SimpleDateFormat("EEE", Locale.US)
    val shortDateFormat = SimpleDateFormat("MMM d", Locale.US)
    val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Build set of days that have activity from quiz attempts
    val activeDaysMap = mutableMapOf<String, Int>()
    for (att in attempts) {
        val attDate = isoFormat.format(Date(att.timestamp))
        activeDaysMap[attDate] = (activeDaysMap[attDate] ?: 0) + att.totalQuestions
    }

    // Add last active date from streak data if marked
    streakData?.lastActiveDate?.let { lastActive ->
        if (lastActive.isNotBlank() && !activeDaysMap.containsKey(lastActive)) {
            activeDaysMap[lastActive] = 5
        }
    }

    // Iterate backwards 6 days to today
    for (i in 6 downTo 0) {
        val dayCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -i)
        }
        val dateIso = isoFormat.format(dayCal.time)
        val isToday = dateIso == todayIso
        val questions = activeDaysMap[dateIso] ?: 0
        val isCompleted = questions > 0 || (isToday && (streakData?.isStreakActiveToday == true))

        val dayLetter = when (dayCal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "M"
            Calendar.TUESDAY -> "T"
            Calendar.WEDNESDAY -> "W"
            Calendar.THURSDAY -> "T"
            Calendar.FRIDAY -> "F"
            Calendar.SATURDAY -> "S"
            Calendar.SUNDAY -> "S"
            else -> "D"
        }

        result.add(
            RollingDayConsistency(
                dayLabel = dayLetter,
                dateFormatted = shortDateFormat.format(dayCal.time),
                dateIso = dateIso,
                isCompleted = isCompleted,
                isToday = isToday,
                questionsAnswered = questions,
                isFreezeProtected = false
            )
        )
    }

    return result
}

/**
 * Comprehensive User Consistency Streak Counter Card.
 * Visualizes live streak count, streak protection status, 7-day consistency tracker,
 * milestone progress bar, and multiplier bonuses.
 */
@Composable
fun StreakConsistencyCounterCard(
    streakData: UserStreakData,
    attempts: List<QuizAttempt> = emptyList(),
    gamificationState: UserGamificationState? = null,
    onPracticeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentStreak = streakData.currentStreak
    val longestStreak = streakData.longestStreak
    val totalDaysActive = streakData.totalDaysActive
    val isActiveToday = streakData.isStreakActiveToday
    val streakFreezes = gamificationState?.streakFreezes ?: 1
    val multiplier = getStreakXpMultiplier(currentStreak)

    // Pulsing flame animation for active streaks
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    // Calculate next milestone target
    val nextMilestone = STREAK_MILESTONES.firstOrNull { it.targetDays > currentStreak }
        ?: STREAK_MILESTONES.last()
    val prevTarget = STREAK_MILESTONES.lastOrNull { it.targetDays <= currentStreak }?.targetDays ?: 0
    val targetRange = (nextMilestone.targetDays - prevTarget).coerceAtLeast(1)
    val progressInRange = (currentStreak - prevTarget).coerceAtLeast(0)
    val milestoneProgressFraction = if (currentStreak >= nextMilestone.targetDays) {
        1.0f
    } else {
        (progressInRange.toFloat() / targetRange.toFloat()).coerceIn(0f, 1f)
    }

    val rollingDays = remember(attempts, streakData) {
        computeRolling7DayConsistency(attempts, streakData)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            1.5.dp,
            if (isActiveToday) AmberAccent.copy(alpha = 0.6f) else Color(0xFF1E293B)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_consistency_counter_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -------------------------------------------------------------
            // 1. TOP HEADER: Streak Flame, Count, Multiplier & Freeze Shield
            // -------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Pulsing Flame Icon Box
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .scale(if (currentStreak > 0) flameScale else 1.0f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        AmberAccent.copy(alpha = 0.35f),
                                        RoseAccent.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(
                                1.5.dp,
                                Brush.linearGradient(listOf(AmberAccent, RoseAccent)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentStreak > 0) "🔥" else "⚡",
                            fontSize = 28.sp
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "$currentStreak",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = if (currentStreak > 0) AmberAccent else Color(0xFF94A3B8)
                            )
                            Text(
                                text = if (currentStreak == 1) "Day Streak" else "Days Streak",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                        }

                        Text(
                            text = if (isActiveToday) "Momentum Active Today" else "Practice Needed Today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isActiveToday) EmeraldSuccess else AmberAccent
                        )
                    }
                }

                // Right Badges: Multiplier & Freeze Shield
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${multiplier}x XP Boost",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyanAccent.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "🧊 $streakFreezes Freezes",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. CONSISTENCY STATUS CALLOUT & QUICK PRACTICE ACTION
            // -------------------------------------------------------------
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isActiveToday) Color(0xFF0F291E) else Color(0xFF26190B),
                border = BorderStroke(
                    1.dp,
                    if (isActiveToday) EmeraldSuccess.copy(alpha = 0.4f) else AmberAccent.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isActiveToday) Icons.Default.CheckCircle else Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (isActiveToday) EmeraldSuccess else AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isActiveToday) "Consistency Goal Met Today!" else "Protect Your Streak!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActiveToday) EmeraldSuccess else AmberAccent
                            )
                            Text(
                                text = if (isActiveToday) "Learning activity recorded. Streak extended." else "Complete a quiz to prevent streak decay.",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    if (!isActiveToday) {
                        Button(
                            onClick = onPracticeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("practice_now_streak_button")
                        ) {
                            Text(
                                text = "Practice",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. 7-DAY ROLLING CONSISTENCY CALENDAR MATRIX
            // -------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LAST 7 DAYS CONSISTENCY TRACK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )

                    val activeCount = rollingDays.count { it.isCompleted }
                    Text(
                        text = "$activeCount / 7 Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeCount >= 5) EmeraldSuccess else CyanAccent
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in rollingDays) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = day.dayLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (day.isToday) AmberAccent else Color(0xFF94A3B8)
                            )

                            // Day Node Circle
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            day.isCompleted -> EmeraldSuccess.copy(alpha = 0.2f)
                                            day.isToday -> AmberAccent.copy(alpha = 0.15f)
                                            else -> Color(0xFF1E293B)
                                        }
                                    )
                                    .border(
                                        1.5.dp,
                                        when {
                                            day.isCompleted -> EmeraldSuccess
                                            day.isToday -> AmberAccent
                                            else -> Color(0xFF334155)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active day",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else if (day.isToday) {
                                    Text(
                                        text = "⏳",
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF475569))
                                    )
                                }
                            }

                            Text(
                                text = if (day.isToday) "Today" else day.dateFormatted.split(" ").getOrNull(1) ?: "",
                                fontSize = 9.sp,
                                color = if (day.isToday) AmberAccent else Color(0xFF64748B),
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 4. NEXT STREAK MILESTONE PROGRESS
            // -------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = nextMilestone.emoji, fontSize = 14.sp)
                        Text(
                            text = "Next Target: ${nextMilestone.title} (${nextMilestone.targetDays}d)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }

                    val daysRemaining = (nextMilestone.targetDays - currentStreak).coerceAtLeast(0)
                    Text(
                        text = if (daysRemaining == 0) "Unlocked!" else "$daysRemaining days left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                }

                // Smooth Progress Bar
                LinearProgressIndicator(
                    progress = { milestoneProgressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AmberAccent,
                    trackColor = Color(0xFF1E293B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$currentStreak / ${nextMilestone.targetDays} Days",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "+${nextMilestone.xpReward} Milestone XP",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // -------------------------------------------------------------
            // 5. STREAK SUMMARY METRICS FOOTER
            // -------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("LONGEST STREAK", fontSize = 9.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                        Text("${longestStreak}d Record ⚡", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("TOTAL ACTIVE", fontSize = 9.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                        Text("$totalDaysActive Days 📅", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                    }
                }
            }
        }
    }
}

/**
 * Achievements & Badges Showcase Card.
 * Displays overall badge unlocked counts, tier distribution breakdown,
 * in-progress badges nearest unlock, recently earned badges, and detail inspection dialog.
 */
@Composable
fun AchievementsBadgeShowcaseCard(
    badges: List<DigitalBadge>,
    onViewAllBadges: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedBadgeForDetail by remember { mutableStateOf<DigitalBadge?>(null) }

    val unlockedCount = badges.count { it.isUnlocked }
    val totalBadges = badges.size.coerceAtLeast(1)
    val unlockedPercent = ((unlockedCount.toFloat() / totalBadges.toFloat()) * 100).toInt()
    val totalXpAwarded = badges.filter { it.isUnlocked }.sumOf { it.xpReward }

    // Identify in-progress badges closest to unlocking
    val inProgressBadges = remember(badges) {
        badges.filter { !it.isUnlocked }
            .sortedByDescending { it.progressFraction }
            .take(3)
    }

    // Identify recent unlocks
    val recentUnlocks = remember(badges) {
        badges.filter { it.isUnlocked }
            .sortedByDescending { it.unlockedAt ?: 0L }
            .take(4)
    }

    // Tier counts
    val bronzeCount = badges.count { it.tier == BadgeTier.BRONZE && it.isUnlocked }
    val silverCount = badges.count { it.tier == BadgeTier.SILVER && it.isUnlocked }
    val goldCount = badges.count { it.tier == BadgeTier.GOLD && it.isUnlocked }
    val platinumCount = badges.count { it.tier == BadgeTier.PLATINUM && it.isUnlocked }
    val diamondCount = badges.count { it.tier == BadgeTier.DIAMOND && it.isUnlocked }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.5.dp, CyanAccent.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("achievements_badge_showcase_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -------------------------------------------------------------
            // 1. HEADER: Title, Total Unlocked & View All Action
            // -------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFD700).copy(alpha = 0.3f), AmberAccent.copy(alpha = 0.1f), Color.Transparent)
                                )
                            )
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Achievements",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Learning Achievements",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "$unlockedCount of $totalBadges Badges Unlocked · $unlockedPercent%",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                TextButton(
                    onClick = onViewAllBadges,
                    modifier = Modifier.testTag("view_all_badges_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("View All", fontSize = 12.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. OVERALL PROGRESS BAR & XP REWARDED
            // -------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { (unlockedCount.toFloat() / totalBadges.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyanAccent,
                    trackColor = Color(0xFF1E293B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Achievement Reward",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "+$totalXpAwarded XP Earned",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
            }

            // -------------------------------------------------------------
            // 3. TIER DISTRIBUTION PILLS
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TierCounterChip("🥉 Bronze", "$bronzeCount", Color(0xFFCD7F32))
                TierCounterChip("🥈 Silver", "$silverCount", Color(0xFFC0C0C0))
                TierCounterChip("🥇 Gold", "$goldCount", Color(0xFFFFD700))
                TierCounterChip("💎 Platinum", "$platinumCount", Color(0xFFE2E8F0))
                TierCounterChip("👑 Diamond", "$diamondCount", Color(0xFF38BDF8))
            }

            // -------------------------------------------------------------
            // 4. IN-PROGRESS BADGES (CLOSEST TO UNLOCK)
            // -------------------------------------------------------------
            if (inProgressBadges.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NEXT UNLOCKS IN PROGRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )

                    for (badge in inProgressBadges) {
                        InProgressBadgeRow(
                            badge = badge,
                            onClick = { selectedBadgeForDetail = badge }
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 5. RECENTLY UNLOCKED BADGES HIGHLIGHT
            // -------------------------------------------------------------
            if (recentUnlocks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RECENTLY UNLOCKED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (badge in recentUnlocks) {
                            RecentUnlockedBadgePill(
                                badge = badge,
                                onClick = { selectedBadgeForDetail = badge }
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail dialog when user clicks on any badge
    selectedBadgeForDetail?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedBadgeForDetail = null }
        )
    }
}

@Composable
private fun TierCounterChip(
    label: String,
    count: String,
    tierColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF131D31),
        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, fontSize = 11.sp, color = tierColor, fontWeight = FontWeight.Bold)
            Text(text = "· $count", fontSize = 11.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InProgressBadgeRow(
    badge: DigitalBadge,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF131D31),
        border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = badge.iconEmoji, fontSize = 22.sp)

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "${(badge.progressFraction * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }

                LinearProgressIndicator(
                    progress = { badge.progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyanAccent,
                    trackColor = Color(0xFF1E293B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = badge.progressLabel,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "+${badge.xpReward} XP",
                        fontSize = 10.sp,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentUnlockedBadgePill(
    badge: DigitalBadge,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF131D31),
        border = BorderStroke(1.dp, badge.tier.primaryColor.copy(alpha = 0.6f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = badge.iconEmoji, fontSize = 16.sp)
            Column {
                Text(
                    text = badge.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC),
                    maxLines = 1
                )
                Text(
                    text = badge.tier.label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = badge.tier.primaryColor
                )
            }
        }
    }
}

/**
 * Detailed inspector dialog for any achievement badge with requirements, XP reward, and rarity lore.
 */
@Composable
fun BadgeDetailDialog(
    badge: DigitalBadge,
    onDismiss: () -> Unit
) {
    val tierColor = badge.tier.primaryColor

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = badge.iconEmoji, fontSize = 28.sp)
                Column {
                    Text(
                        text = badge.title,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${badge.tier.label.uppercase()} · ${badge.category.displayName}",
                        fontSize = 11.sp,
                        color = tierColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = badge.description,
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )

                // Criteria & Status Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("STATUS", fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                            Text(
                                text = if (badge.isUnlocked) "UNLOCKED ✅" else "IN PROGRESS (${(badge.progressFraction * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.isUnlocked) EmeraldSuccess else CyanAccent
                            )
                        }

                        if (!badge.isUnlocked) {
                            LinearProgressIndicator(
                                progress = { badge.progressFraction.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyanAccent,
                                trackColor = Color(0xFF1E293B)
                            )
                            Text(
                                text = badge.progressLabel,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        } else {
                            val unlockDateStr = badge.unlockedAt?.let {
                                SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(it))
                            } ?: "Unlocked"
                            Text(
                                text = "Earned on $unlockDateStr",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Reward & Rarity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldSuccess.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("REWARD", fontSize = 9.sp, color = EmeraldSuccess, fontFamily = FontFamily.Monospace)
                            Text("+${badge.xpReward} XP", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("RARITY", fontSize = 9.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                            Text(badge.rarityText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tierColor, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Got It", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        }
    )
}
