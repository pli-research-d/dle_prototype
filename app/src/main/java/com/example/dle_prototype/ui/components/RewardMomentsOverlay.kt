package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess

sealed class RewardMomentType {
    data class StreakMilestone(val streakCount: Int) : RewardMomentType()
    data class ChestDrop(val chestId: Long, val chestType: String) : RewardMomentType()
    data class MultiplierChange(val newMultiplier: Float) : RewardMomentType()
    data class RocketAltitudeMilestone(val altitudeKm: Float) : RewardMomentType()
    data class LeagueRankChange(val rivalName: String, val newRank: Int) : RewardMomentType()
}

@Composable
fun RewardMomentsOverlay(
    currentMoment: RewardMomentType?,
    onChestClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = currentMoment != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        currentMoment?.let { moment ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                border = BorderStroke(
                    1.5.dp,
                    when (moment) {
                        is RewardMomentType.StreakMilestone -> AmberAccent
                        is RewardMomentType.ChestDrop -> Color(0xFFA855F7)
                        is RewardMomentType.MultiplierChange -> CyanAccent
                        is RewardMomentType.RocketAltitudeMilestone -> EmeraldSuccess
                        is RewardMomentType.LeagueRankChange -> AmberAccent
                    }
                ),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clickable(enabled = moment is RewardMomentType.ChestDrop) {
                            if (moment is RewardMomentType.ChestDrop) {
                                onChestClick(moment.chestId)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (moment) {
                        is RewardMomentType.StreakMilestone -> {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "🔥 ${moment.streakCount} IN A ROW!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = AmberAccent
                                )
                                Text(
                                    text = "Momentum is surging! Multiplier accelerated.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                        is RewardMomentType.ChestDrop -> {
                            Icon(
                                Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎁 MYSTERY CHEST DROPPED!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = Color(0xFFC084FC)
                                )
                                Text(
                                    text = "Tap to unlock XP, Fuel & rare cosmic skin",
                                    fontSize = 11.sp,
                                    color = Color(0xFFF1F5F9)
                                )
                            }
                        }
                        is RewardMomentType.MultiplierChange -> {
                            Icon(
                                Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "⚡ MULTIPLIER BOOST: ${moment.newMultiplier}X",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = CyanAccent
                                )
                                Text(
                                    text = "Keep answering correctly to maintain momentum!",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                        is RewardMomentType.RocketAltitudeMilestone -> {
                            Icon(
                                Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "🚀 ORBIT MILESTONE: ${"%.1f".format(moment.altitudeKm)} KM",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = EmeraldSuccess
                                )
                                Text(
                                    text = "Stratosphere breached! +25 Fuel awarded.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                        is RewardMomentType.LeagueRankChange -> {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "▲ LEAGUE CLIMB: #${moment.newRank} IN SILVER",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = AmberAccent
                                )
                                Text(
                                    text = "You just passed ${moment.rivalName}! Promotion zone secured.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
