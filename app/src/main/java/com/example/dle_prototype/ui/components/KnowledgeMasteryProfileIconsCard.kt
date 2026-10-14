package com.example.dle_prototype.ui.components

import android.widget.Toast
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.UserSettings
import com.example.dle_prototype.data.graph.KnowledgeExpertBadgeDefinition
import com.example.dle_prototype.data.graph.KnowledgeExpertBadgeStatus
import com.example.dle_prototype.data.graph.KnowledgeMasteryLevel
import com.example.dle_prototype.data.graph.TopicMasteryDetail
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent

/**
 * Knowledge Graph Mastery Profile Icons Card.
 * Visualizes badges that unlock exclusive profile avatar icons when the user
 * reaches 'Expert' mastery in defined sets of interconnected Knowledge Graph topics.
 */
@Composable
fun KnowledgeMasteryProfileIconsCard(
    badgeStatuses: List<KnowledgeExpertBadgeStatus>,
    currentAvatarEmoji: String,
    onEquipAvatar: (KnowledgeExpertBadgeDefinition) -> Unit,
    onPracticeTopic: (categoryName: String, categoryNumber: Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedBadgeDetail by remember { mutableStateOf<KnowledgeExpertBadgeStatus?>(null) }

    val unlockedCount = badgeStatuses.count { it.isUnlocked }
    val totalBadges = badgeStatuses.size.coerceAtLeast(1)
    val currentlyEquippedBadge = badgeStatuses.find { it.definition.unlockedIconEmoji == currentAvatarEmoji }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.5.dp, CyanAccent.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("knowledge_mastery_profile_icons_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -----------------------------------------------------------------
            // 1. HEADER: Title, Unlocked Count Pill & Description
            // -----------------------------------------------------------------
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
                                    listOf(CyanAccent.copy(alpha = 0.35f), Color(0xFF1E293B))
                                )
                            )
                            .border(1.5.dp, CyanAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = "Knowledge Graph Mastery Icons",
                            tint = CyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Knowledge Mastery Icons",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Reach 'Expert' in interconnected topics to unlock avatars",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "$unlockedCount / $totalBadges Unlocked",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // -----------------------------------------------------------------
            // 2. CURRENTLY EQUIPPED AVATAR STATUS BANNER
            // -----------------------------------------------------------------
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF131D31),
                border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentlyEquippedBadge != null)
                                        currentlyEquippedBadge.definition.accentColor.copy(alpha = 0.25f)
                                    else Color(0xFF1E293B)
                                )
                                .border(
                                    1.dp,
                                    if (currentlyEquippedBadge != null)
                                        currentlyEquippedBadge.definition.accentColor
                                    else Color(0xFF334155),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentAvatarEmoji.ifBlank { "👤" }, fontSize = 20.sp)
                        }

                        Column {
                            Text(
                                text = "CURRENT PROFILE AVATAR",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = currentlyEquippedBadge?.let {
                                    "${it.definition.unlockedIconName} (${it.definition.badgeTitle})"
                                } ?: "Standard Avatar Symbol ($currentAvatarEmoji)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentlyEquippedBadge != null) currentlyEquippedBadge.definition.accentColor else Color(0xFFF8FAFC)
                            )
                        }
                    }

                    if (currentlyEquippedBadge != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "EXPERT EQUIPPED 🥇",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 3. BADGE CARDS LIST: INTERCONNECTED TOPICS & UNLOCK CONTROLS
            // -----------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (badgeStatus in badgeStatuses) {
                    KnowledgeMasteryBadgeItem(
                        badgeStatus = badgeStatus,
                        onInspect = { selectedBadgeDetail = badgeStatus },
                        onEquip = {
                            onEquipAvatar(badgeStatus.definition)
                            Toast.makeText(
                                context,
                                "Equipped ${badgeStatus.definition.unlockedIconName} (${badgeStatus.definition.unlockedIconEmoji}) on profile!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onPracticeTopic = onPracticeTopic
                    )
                }
            }
        }
    }

    // Modal inspector dialog when clicking any badge
    selectedBadgeDetail?.let { status ->
        KnowledgeMasteryBadgeDetailDialog(
            status = status,
            onDismiss = { selectedBadgeDetail = null },
            onEquip = {
                onEquipAvatar(status.definition)
                selectedBadgeDetail = null
                Toast.makeText(
                    context,
                    "Equipped ${status.definition.unlockedIconName} (${status.definition.unlockedIconEmoji}) on profile!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onPracticeTopic = onPracticeTopic
        )
    }
}

/**
 * Individual badge item card presenting interconnected topic progress,
 * unlock status, and equip action.
 */
@Composable
private fun KnowledgeMasteryBadgeItem(
    badgeStatus: KnowledgeExpertBadgeStatus,
    onInspect: () -> Unit,
    onEquip: () -> Unit,
    onPracticeTopic: (categoryName: String, categoryNumber: Float) -> Unit
) {
    val def = badgeStatus.definition
    val isUnlocked = badgeStatus.isUnlocked
    val isEquipped = badgeStatus.isEquipped

    // Pulsing subtle glow for unlocked, unequipped badges
    val infiniteTransition = rememberInfiniteTransition(label = "badge_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF131D31),
        border = BorderStroke(
            if (isEquipped) 2.dp else if (isUnlocked) 1.5.dp else 1.dp,
            when {
                isEquipped -> EmeraldSuccess
                isUnlocked -> def.accentColor.copy(alpha = pulseAlpha)
                else -> Color(0xFF1E2E4A)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("km_badge_${def.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Emoji in circular frame + Title + Cluster + Tier
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) def.accentColor.copy(alpha = 0.2f)
                                else Color(0xFF1E293B)
                            )
                            .border(
                                1.5.dp,
                                if (isUnlocked) def.accentColor else Color(0xFF334155),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUnlocked) def.unlockedIconEmoji else "🔒",
                            fontSize = if (isUnlocked) 24.sp else 18.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = def.badgeTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isUnlocked) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmeraldSuccess.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "UNLOCKED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Unlocks '${def.unlockedIconName}' avatar (${def.unlockedIconEmoji})",
                            fontSize = 11.sp,
                            color = if (isUnlocked) def.accentColor else Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = def.tier.primaryColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, def.tier.primaryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "+${def.xpReward} XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = def.tier.primaryColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Description
            Text(
                text = def.description,
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 15.sp
            )

            // Interconnected Topics Progress Tracker
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INTERCONNECTED TOPIC MASTERY:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "${badgeStatus.expertTopicsCount} / ${badgeStatus.totalRequiredTopicsCount} Expert",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) EmeraldSuccess else AmberAccent
                    )
                }

                // Horizontal Flow of Topic Pills with connector arrows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    badgeStatus.topicProgress.forEachIndexed { index, topic ->
                        TopicMasteryPill(topic = topic)
                        if (index < badgeStatus.topicProgress.size - 1) {
                            Text(
                                text = "→",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { badgeStatus.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isUnlocked) EmeraldSuccess else def.accentColor,
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Footer Action: Equip Icon or Practice Prompt
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = def.rarityText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = def.tier.primaryColor
                )

                if (isUnlocked) {
                    if (isEquipped) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, EmeraldSuccess)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                                Text("Equipped as Avatar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }
                        }
                    } else {
                        Button(
                            onClick = onEquip,
                            colors = ButtonDefaults.buttonColors(containerColor = def.accentColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("equip_badge_${def.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = def.unlockedIconEmoji, fontSize = 14.sp)
                                Text(
                                    text = "Equip Icon",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    // Find first unmastered topic to practice
                    val nextTopic = badgeStatus.topicProgress.firstOrNull { !it.isExpert }
                    Button(
                        onClick = {
                            if (nextTopic != null) {
                                onPracticeTopic(nextTopic.title, 3f)
                            } else {
                                onInspect()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Practice ${nextTopic?.topicId?.uppercase() ?: "Topics"}",
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicMasteryPill(topic: TopicMasteryDetail) {
    val isExpert = topic.isExpert
    val pillColor = when {
        isExpert -> EmeraldSuccess
        topic.masteryLevel == KnowledgeMasteryLevel.INTERMEDIATE -> CyanAccent
        else -> AmberAccent
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = pillColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, pillColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isExpert) "✓" else "⏳",
                fontSize = 9.sp,
                color = pillColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${topic.title.take(10)}: ${topic.accuracyPercent.toInt()}%",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = pillColor
            )
        }
    }
}

/**
 * Deep inspector dialog presenting the interconnected topic map,
 * mastery breakdown, and instant avatar equip control.
 */
@Composable
fun KnowledgeMasteryBadgeDetailDialog(
    status: KnowledgeExpertBadgeStatus,
    onDismiss: () -> Unit,
    onEquip: () -> Unit,
    onPracticeTopic: (categoryName: String, categoryNumber: Float) -> Unit
) {
    val def = status.definition

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(def.accentColor.copy(alpha = 0.2f))
                        .border(1.5.dp, def.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (status.isUnlocked) def.unlockedIconEmoji else "🔒", fontSize = 24.sp)
                }

                Column {
                    Text(
                        text = def.badgeTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Unlocks '${def.unlockedIconName}' Profile Avatar",
                        fontSize = 11.sp,
                        color = def.accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = def.description,
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )

                // Interconnected Curriculum Requirement Section
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "INTERCONNECTED TOPIC MASTERY (≥80% REQUIRED):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )

                        for (topic in status.topicProgress) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (topic.isExpert) Icons.Default.CheckCircle else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (topic.isExpert) EmeraldSuccess else AmberAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = topic.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF8FAFC)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${topic.accuracyPercent.toInt()}% (${topic.masteryLevel.label})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (topic.isExpert) EmeraldSuccess else CyanAccent
                                    )
                                    if (!topic.isExpert) {
                                        Text(
                                            text = "Practice",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent,
                                            modifier = Modifier
                                                .clickable {
                                                    onDismiss()
                                                    onPracticeTopic(topic.title, 3f)
                                                }
                                                .padding(2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Reward & Cluster Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = def.tier.primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, def.tier.primaryColor.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("REWARD", fontSize = 9.sp, color = def.tier.primaryColor, fontFamily = FontFamily.Monospace)
                            Text("+${def.xpReward} XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = def.tier.primaryColor)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("CLUSTER", fontSize = 9.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                            Text(def.clusterName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = def.accentColor, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (status.isUnlocked) {
                Button(
                    onClick = onEquip,
                    colors = ButtonDefaults.buttonColors(containerColor = def.accentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (status.isEquipped) "Already Equipped" else "Equip '${def.unlockedIconEmoji}' Avatar",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (status.isUnlocked) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            }
        }
    )
}

/**
 * Bottom sheet view of all Knowledge Graph mastery badges for quick inspection from Knowledge Graph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeMasteryBadgesSheet(
    badgeStatuses: List<KnowledgeExpertBadgeStatus>,
    currentAvatarEmoji: String,
    onEquipAvatar: (KnowledgeExpertBadgeDefinition) -> Unit,
    onPracticeTopic: (categoryName: String, categoryNumber: Float) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B132B),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Hub, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                    Text(
                        text = "Knowledge Mastery Avatars",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("Done", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            }

            KnowledgeMasteryProfileIconsCard(
                badgeStatuses = badgeStatuses,
                currentAvatarEmoji = currentAvatarEmoji,
                onEquipAvatar = onEquipAvatar,
                onPracticeTopic = onPracticeTopic,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
