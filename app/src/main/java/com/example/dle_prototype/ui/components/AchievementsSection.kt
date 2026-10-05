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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.QuizPerformanceStats
import com.example.dle_prototype.data.badges.BadgeCategory
import com.example.dle_prototype.data.badges.DigitalBadge
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val progress: Float, // 0f..1f
    val progressText: String,
    val category: String = "Performance",
    val accentColor: Color = AmberAccent
)

fun buildAchievementsList(stats: QuizPerformanceStats): List<AchievementBadge> {
    return listOf(
        AchievementBadge(
            id = "quiz_master",
            title = "Quiz Master",
            description = "Score 100% on any quiz session with flawless accuracy.",
            icon = Icons.Default.EmojiEvents,
            isUnlocked = stats.perfectQuizzesCount >= 1,
            progress = if (stats.perfectQuizzesCount >= 1) 1f else 0f,
            progressText = if (stats.perfectQuizzesCount >= 1) "Completed (${stats.perfectQuizzesCount} Perfect)" else "0 / 1 Perfect Quiz",
            accentColor = Color(0xFFFFD700)
        ),
        AchievementBadge(
            id = "early_bird",
            title = "Early Bird",
            description = "Complete a quiz session between 5:00 AM and 9:00 AM.",
            icon = Icons.Default.LightMode,
            isUnlocked = stats.earlyBirdQuizzesCount >= 1,
            progress = if (stats.earlyBirdQuizzesCount >= 1) 1f else 0f,
            progressText = if (stats.earlyBirdQuizzesCount >= 1) "Morning Session Completed" else "Take quiz before 9 AM",
            accentColor = AmberAccent
        ),
        AchievementBadge(
            id = "streak_spark",
            title = "Streak Spark",
            description = "Maintain a 3-day consecutive quiz study streak.",
            icon = Icons.Default.LocalFireDepartment,
            isUnlocked = stats.currentDailyStreak >= 3,
            progress = (stats.currentDailyStreak.toFloat() / 3f).coerceIn(0f, 1f),
            progressText = "${stats.currentDailyStreak} / 3 Days",
            accentColor = AmberAccent
        ),
        AchievementBadge(
            id = "streak_inferno",
            title = "Streak Inferno",
            description = "Reach a 7-day uninterrupted quiz study streak.",
            icon = Icons.Default.LocalFireDepartment,
            isUnlocked = stats.currentDailyStreak >= 7,
            progress = (stats.currentDailyStreak.toFloat() / 7f).coerceIn(0f, 1f),
            progressText = "${stats.currentDailyStreak} / 7 Days",
            accentColor = RoseAccent
        ),
        AchievementBadge(
            id = "century_scholar",
            title = "Century Scholar",
            description = "Answer 50 or more questions across all quiz sessions.",
            icon = Icons.Default.School,
            isUnlocked = stats.totalQuestionsAnswered >= 50,
            progress = (stats.totalQuestionsAnswered.toFloat() / 50f).coerceIn(0f, 1f),
            progressText = "${stats.totalQuestionsAnswered} / 50 Questions",
            accentColor = CyanAccent
        ),
        AchievementBadge(
            id = "consistent_learner",
            title = "Consistent Learner",
            description = "Complete 5 total quiz sessions to build muscle memory.",
            icon = Icons.Default.RocketLaunch,
            isUnlocked = stats.totalQuizzes >= 5,
            progress = (stats.totalQuizzes.toFloat() / 5f).coerceIn(0f, 1f),
            progressText = "${stats.totalQuizzes} / 5 Sessions",
            accentColor = EmeraldSuccess
        ),
        AchievementBadge(
            id = "high_flyer",
            title = "High Flyer",
            description = "Maintain an overall average accuracy of 80% or higher.",
            icon = Icons.Default.Speed,
            isUnlocked = stats.totalQuizzes >= 2 && stats.averageAccuracyPercent >= 80f,
            progress = (stats.averageAccuracyPercent / 80f).coerceIn(0f, 1f),
            progressText = "${stats.averageAccuracyPercent.toInt()}% / 80% Min",
            accentColor = IndigoPrimaryLight
        ),
        AchievementBadge(
            id = "neural_pioneer",
            title = "Neural Pioneer",
            description = "Complete at least one quiz to calibrate your neural profile.",
            icon = Icons.Default.Psychology,
            isUnlocked = stats.totalQuizzes >= 1,
            progress = if (stats.totalQuizzes >= 1) 1f else 0f,
            progressText = if (stats.totalQuizzes >= 1) "Calibrated" else "0 / 1 Quiz",
            accentColor = CyanAccent
        )
    )
}

/**
 * Dedicated Grid Section displaying user achievements and digital badges based on
 * learning streak milestones, high-volume study sessions, and mastery performance.
 */
@Composable
fun AchievementsSection(
    stats: QuizPerformanceStats? = null,
    digitalBadges: List<DigitalBadge> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (digitalBadges.isNotEmpty()) {
        DigitalBadgesSectionView(
            badges = digitalBadges,
            modifier = modifier
        )
    } else {
        LegacyAchievementsView(
            stats = stats ?: QuizPerformanceStats(),
            modifier = modifier
        )
    }
}

@Composable
private fun DigitalBadgesSectionView(
    badges: List<DigitalBadge>,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<BadgeCategory?>(null) }
    var selectedBadge by remember { mutableStateOf<DigitalBadge?>(null) }

    val filteredBadges = remember(badges, selectedCategory) {
        if (selectedCategory == null) badges else badges.filter { it.category == selectedCategory }
    }

    val unlockedCount = badges.count { it.isUnlocked }
    val totalXpEarned = badges.filter { it.isUnlocked }.sumOf { it.xpReward }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("digital_badges_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Header Card
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AmberAccent.copy(alpha = 0.25f), Color(0xFFFFD700).copy(alpha = 0.35f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = "Achievements",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Digital Achievements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "$unlockedCount of ${badges.size} Badges · +$totalXpEarned XP Earned",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${((unlockedCount.toFloat() / badges.size.toFloat()) * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All (${badges.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanAccent,
                    selectedLabelColor = Color(0xFF0F172A),
                    containerColor = Color(0xFF0B132B),
                    labelColor = Color(0xFF94A3B8)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == null,
                    borderColor = Color(0xFF1E293B),
                    selectedBorderColor = CyanAccent
                )
            )

            BadgeCategory.values().forEach { cat ->
                val isSel = selectedCategory == cat
                val catCount = badges.count { it.category == cat }
                FilterChip(
                    selected = isSel,
                    onClick = { selectedCategory = cat },
                    label = { Text("${cat.displayName} ($catCount)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = Color(0xFF0F172A),
                        containerColor = Color(0xFF0B132B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSel,
                        borderColor = Color(0xFF1E293B),
                        selectedBorderColor = CyanAccent
                    )
                )
            }
        }

        // 2-Column Grid of Digital Badges
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            filteredBadges.chunked(2).forEach { rowBadges ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowBadges.forEach { badge ->
                        DigitalBadgeItemCard(
                            badge = badge,
                            onClick = { selectedBadge = badge },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowBadges.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Detail Dialog for Digital Badge
    selectedBadge?.let { badge ->
        val tierColor = badge.tier.primaryColor
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            modifier = Modifier.testTag("digital_badge_details_dialog"),
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF0F172A),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = tierColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.5.dp, tierColor),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = badge.iconEmoji, fontSize = 24.sp)
                        }
                    }
                    Column {
                        Text(
                            text = badge.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (badge.isUnlocked) "UNLOCKED 🏆" else "IN PROGRESS 🔒",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.isUnlocked) EmeraldSuccess else AmberAccent
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tier & Rarity", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    "${badge.tier.label} · ${badge.rarityText}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Reward", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("+${badge.xpReward} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Progress", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(badge.progressLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tierColor)
                            }
                            LinearProgressIndicator(
                                progress = { badge.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = tierColor,
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBadge = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Close", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun DigitalBadgeItemCard(
    badge: DigitalBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tierColor = badge.tier.primaryColor
    val borderColor = if (badge.isUnlocked) tierColor.copy(alpha = 0.6f) else Color(0xFF1E293B)
    val cardBackground = if (badge.isUnlocked) Color(0xFF0F172A) else Color(0xFF0B1120)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBackground,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("digital_badge_${badge.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (badge.isUnlocked) tierColor.copy(alpha = 0.2f) else Color(0xFF1E293B),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = badge.iconEmoji, fontSize = 20.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (badge.isUnlocked) EmeraldSuccess.copy(alpha = 0.15f) else Color(0xFF1E293B),
                    border = BorderStroke(
                        0.5.dp,
                        if (badge.isUnlocked) EmeraldSuccess.copy(alpha = 0.4f) else Color(0xFF334155)
                    )
                ) {
                    Text(
                        text = if (badge.isUnlocked) "UNLOCKED" else "LOCKED",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (badge.isUnlocked) EmeraldSuccess else Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) Color(0xFFF1F5F9) else Color(0xFF94A3B8),
                    maxLines = 1
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = badge.progressLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (badge.isUnlocked) tierColor else Color(0xFF64748B)
                    )
                    Text(
                        text = "+${badge.xpReward} XP",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { badge.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (badge.isUnlocked) tierColor else Color(0xFF475569),
                    trackColor = Color(0xFF1E293B)
                )
            }
        }
    }
}

@Composable
private fun LegacyAchievementsView(
    stats: QuizPerformanceStats,
    modifier: Modifier = Modifier
) {
    val badges = remember(stats) { buildAchievementsList(stats) }
    val unlockedCount = badges.count { it.isUnlocked }
    var selectedBadge by remember { mutableStateOf<AchievementBadge?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("achievements_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Header Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AmberAccent.copy(alpha = 0.25f), Color(0xFFFFD700).copy(alpha = 0.35f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = "Achievements",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Achievements & Badges",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "$unlockedCount of ${badges.size} Badges Unlocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${((unlockedCount.toFloat() / badges.size.toFloat()) * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Dedicated 2-Column Grid of Badges
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            badges.chunked(2).forEach { rowBadges ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowBadges.forEach { badge ->
                        AchievementBadgeCard(
                            badge = badge,
                            onClick = { selectedBadge = badge },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowBadges.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Badge Details Dialog
    selectedBadge?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            modifier = Modifier.testTag("badge_details_dialog"),
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(badge.accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            badge.icon,
                            contentDescription = null,
                            tint = if (badge.isUnlocked) badge.accentColor else Color(0xFF64748B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = badge.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (badge.isUnlocked) "UNLOCKED 🏆" else "IN PROGRESS 🔒",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.isUnlocked) EmeraldSuccess else AmberAccent
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Current Progress",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = badge.progressText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badge.accentColor,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { badge.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = badge.accentColor,
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBadge = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
fun AchievementBadgeCard(
    badge: AchievementBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (badge.isUnlocked) badge.accentColor.copy(alpha = 0.5f) else Color(0xFF1E293B)
    val cardBackground = if (badge.isUnlocked) Color(0xFF0F172A) else Color(0xFF0B1120)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBackground,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("badge_card_${badge.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (badge.isUnlocked) badge.accentColor.copy(alpha = 0.2f)
                            else Color(0xFF1E293B)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        badge.icon,
                        contentDescription = badge.title,
                        tint = if (badge.isUnlocked) badge.accentColor else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (badge.isUnlocked) EmeraldSuccess.copy(alpha = 0.15f) else Color(0xFF1E293B),
                    border = BorderStroke(
                        0.5.dp,
                        if (badge.isUnlocked) EmeraldSuccess.copy(alpha = 0.4f) else Color(0xFF334155)
                    )
                ) {
                    Text(
                        text = if (badge.isUnlocked) "UNLOCKED" else "LOCKED",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (badge.isUnlocked) EmeraldSuccess else Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) Color(0xFFF1F5F9) else Color(0xFF94A3B8)
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = badge.progressText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (badge.isUnlocked) badge.accentColor else Color(0xFF64748B)
                    )
                    Text(
                        text = "${(badge.progress * 100).toInt()}%",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isUnlocked) badge.accentColor else Color(0xFF64748B)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { badge.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (badge.isUnlocked) badge.accentColor else Color(0xFF475569),
                    trackColor = Color(0xFF1E293B)
                )
            }
        }
    }
}
