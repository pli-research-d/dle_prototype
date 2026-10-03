package com.example.dle_prototype.ui.components

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.dle_prototype.data.LearningTelemetry
import com.example.dle_prototype.data.PersonalizationProfile
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import java.util.Locale

/**
 * Unified, sleek hero card combining user learning state, streak flame,
 * on-device cognitive traits, live learning telemetry metrics, and AI recommendations.
 */
@Composable
fun CombinedLearningCard(
    username: String,
    dailyStreak: Int,
    profile: PersonalizationProfile?,
    telemetry: LearningTelemetry? = null,
    onViewVisualHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("combined_learning_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Greeting & Daily Streak Badge
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CyanAccent.copy(alpha = 0.3f), IndigoPrimaryLight.copy(alpha = 0.4f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Welcome back, $username",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Dominant Trait: ${profile?.primaryStrength ?: "Calibrating"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Fire Streak Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (dailyStreak > 0) AmberAccent.copy(alpha = 0.18f) else Color(0xFF1E293B),
                    border = BorderStroke(
                        1.dp,
                        if (dailyStreak > 0) AmberAccent.copy(alpha = 0.5f) else Color(0xFF334155)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = "Daily Streak",
                            tint = if (dailyStreak > 0) AmberAccent else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (dailyStreak > 0) "$dailyStreak Days 🔥" else "0 Days",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (dailyStreak > 0) AmberAccent else Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Live Telemetry Stats Strip (Sessions, Study Time, Last Score, Level)
            if (telemetry != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF090E1A))
                        .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(14.dp))
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroMetric(
                        label = "Sessions",
                        value = "${telemetry.loginCount}",
                        accentColor = CyanAccent
                    )
                    Box(modifier = Modifier.size(1.dp, 24.dp).background(Color(0xFF1E293B)))
                    HeroMetric(
                        label = "Study Time",
                        value = String.format(Locale.getDefault(), "%.1fm", telemetry.totalTimeMinutes),
                        accentColor = IndigoPrimaryLight
                    )
                    Box(modifier = Modifier.size(1.dp, 24.dp).background(Color(0xFF1E293B)))
                    HeroMetric(
                        label = "Quiz Score",
                        value = "${telemetry.lastQuizScore.toInt()}%",
                        accentColor = EmeraldSuccess
                    )
                    Box(modifier = Modifier.size(1.dp, 24.dp).background(Color(0xFF1E293B)))
                    HeroMetric(
                        label = "AI Level",
                        value = "Lv ${telemetry.lastDifficultyReached.toInt()}",
                        accentColor = AmberAccent
                    )
                }
            }

            // 4 Mini Cognitive Traits Bar Visualizers
            if (profile != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COGNITIVE TRAIT SPECTRUM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onViewVisualHub() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Visual Hub →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TraitBarMini(
                            name = "Conscientious",
                            score = profile.conscientiousness.score,
                            color = IndigoPrimaryLight,
                            modifier = Modifier.weight(1f)
                        )
                        TraitBarMini(
                            name = "Motivation",
                            score = profile.motivation.score,
                            color = AmberAccent,
                            modifier = Modifier.weight(1f)
                        )
                        TraitBarMini(
                            name = "Understanding",
                            score = profile.understanding.score,
                            color = CyanAccent,
                            modifier = Modifier.weight(1f)
                        )
                        TraitBarMini(
                            name = "Engagement",
                            score = profile.engagement.score,
                            color = EmeraldSuccess,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // AI Insight Recommendation snippet
                val topTip = profile.tips.firstOrNull()
                if (!topTip.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberAccent.copy(alpha = 0.08f))
                            .border(BorderStroke(0.8.dp, AmberAccent.copy(alpha = 0.35f)), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = topTip,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF1F5F9),
                            lineHeight = 15.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(
    label: String,
    value: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = accentColor
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
fun TraitBarMini(
    name: String,
    score: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0B1120),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
            Text(
                text = "${(score * 100).toInt()}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
            LinearProgressIndicator(
                progress = { score.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = Color(0xFF1E293B)
            )
        }
    }
}
