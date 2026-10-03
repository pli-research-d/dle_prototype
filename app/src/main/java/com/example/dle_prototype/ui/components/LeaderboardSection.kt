package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.LeaderboardEntry
import com.example.dle_prototype.data.LeaderboardScope
import com.example.dle_prototype.data.LeaderboardSort
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent

@Composable
fun LeaderboardSection(
    entries: List<LeaderboardEntry>,
    currentScope: LeaderboardScope,
    currentSort: LeaderboardSort,
    onScopeChange: (LeaderboardScope) -> Unit,
    onSortChange: (LeaderboardSort) -> Unit,
    onToggleFriend: (String) -> Unit,
    onAddFriendByName: (String) -> Unit,
    onStartQuizToClimb: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var selectedEntryForDetail by remember { mutableStateOf<LeaderboardEntry?>(null) }
    var newFriendUsername by remember { mutableStateOf("") }
    var addFriendError by remember { mutableStateOf<String?>(null) }

    val currentUserEntry = entries.firstOrNull { it.isCurrentUser }
    val topThree = entries.take(3)
    val remainingEntries = if (entries.size > 3) entries.drop(3) else emptyList()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("leaderboard_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Arena Header Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                                .background(AmberAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Learning Arena",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFF8FAFC)
                            )
                            Text(
                                text = "Compete on Daily Streaks & Accuracy",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Add Buddy Button
                    Button(
                        onClick = {
                            newFriendUsername = ""
                            addFriendError = null
                            showAddFriendDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_friend_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Buddy", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scope Tab Switcher: Global vs Friends
                TabRow(
                    selectedTabIndex = if (currentScope == LeaderboardScope.GLOBAL) 0 else 1,
                    containerColor = Color(0xFF090E1A),
                    contentColor = CyanAccent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = currentScope == LeaderboardScope.GLOBAL,
                        onClick = { onScopeChange(LeaderboardScope.GLOBAL) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Global Arena", fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("leaderboard_scope_global")
                    )
                    Tab(
                        selected = currentScope == LeaderboardScope.FRIENDS,
                        onClick = { onScopeChange(LeaderboardScope.FRIENDS) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Study Buddies", fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("leaderboard_scope_friends")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ranking Criteria Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentSort == LeaderboardSort.STREAK,
                        onClick = { onSortChange(LeaderboardSort.STREAK) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Daily Streak 🔥", fontSize = 12.sp, fontWeight = if (currentSort == LeaderboardSort.STREAK) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberAccent.copy(alpha = 0.2f),
                            selectedLabelColor = AmberAccent,
                            selectedLeadingIconColor = AmberAccent,
                            containerColor = Color(0xFF090E1A),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = currentSort == LeaderboardSort.STREAK,
                            borderColor = if (currentSort == LeaderboardSort.STREAK) AmberAccent else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("leaderboard_sort_streak")
                    )

                    FilterChip(
                        selected = currentSort == LeaderboardSort.SCORE,
                        onClick = { onSortChange(LeaderboardSort.SCORE) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Total Score 🎯", fontSize = 12.sp, fontWeight = if (currentSort == LeaderboardSort.SCORE) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimaryLight.copy(alpha = 0.2f),
                            selectedLabelColor = IndigoPrimaryLight,
                            selectedLeadingIconColor = IndigoPrimaryLight,
                            containerColor = Color(0xFF090E1A),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = currentSort == LeaderboardSort.SCORE,
                            borderColor = if (currentSort == LeaderboardSort.SCORE) IndigoPrimaryLight else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("leaderboard_sort_score")
                    )

                    FilterChip(
                        selected = currentSort == LeaderboardSort.ACCURACY,
                        onClick = { onSortChange(LeaderboardSort.ACCURACY) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Accuracy % 🏆", fontSize = 12.sp, fontWeight = if (currentSort == LeaderboardSort.ACCURACY) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSuccess.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldSuccess,
                            selectedLeadingIconColor = EmeraldSuccess,
                            containerColor = Color(0xFF090E1A),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = currentSort == LeaderboardSort.ACCURACY,
                            borderColor = if (currentSort == LeaderboardSort.ACCURACY) EmeraldSuccess else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("leaderboard_sort_accuracy")
                    )
                }
            }
        }

        // Prominent "Your Status / Standings" Card
        if (currentUserEntry != null) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF090E1A),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(CyanAccent, IndigoPrimaryLight))),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("your_ranking_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f))
                                .border(BorderStroke(2.dp, CyanAccent), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${currentUserEntry.rank}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyanAccent
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Your Standing",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldSuccess.copy(alpha = 0.18f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = currentUserEntry.tier,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                            Text(
                                text = when (currentSort) {
                                    LeaderboardSort.STREAK -> "Streak: ${currentUserEntry.dailyStreak} days 🔥 • Score: ${currentUserEntry.totalScore} pts"
                                    LeaderboardSort.SCORE -> "Score: ${currentUserEntry.totalScore} pts 🎯 • Accuracy: ${currentUserEntry.accuracyPercent.toInt()}%"
                                    LeaderboardSort.ACCURACY -> "Accuracy: ${currentUserEntry.accuracyPercent.toInt()}% 🏆 • ${currentUserEntry.totalQuizzes} quizzes"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Button(
                        onClick = onStartQuizToClimb,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("climb_rank_button")
                    ) {
                        Text("Climb Rank →", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Top 3 Podium View (only when at least 3 entries exist)
        if (topThree.isNotEmpty()) {
            PodiumView(
                topThree = topThree,
                currentSort = currentSort,
                onSelectEntry = { selectedEntryForDetail = it }
            )
        }

        // Remaining Ranking List
        if (remainingEntries.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Runner-Ups & Contenders",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                remainingEntries.forEach { entry ->
                    LeaderboardRowItem(
                        entry = entry,
                        currentSort = currentSort,
                        onToggleFriend = { onToggleFriend(entry.username) },
                        onClick = { selectedEntryForDetail = entry }
                    )
                }
            }
        } else if (topThree.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("👥", fontSize = 32.sp)
                    Text(
                        text = "No study buddies yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Add friends from the Global Arena or tap 'Add Buddy' above to compare streaks and scores!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Add Friend Dialog
    if (showAddFriendDialog) {
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text("Add Study Buddy") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter a username to add them to your friends leaderboard and follow their quiz streak:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newFriendUsername,
                        onValueChange = {
                            newFriendUsername = it.trim()
                            addFriendError = null
                        },
                        label = { Text("Username") },
                        placeholder = { Text("e.g. sophia_ai or marcus_v") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_friend_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                    if (addFriendError != null) {
                        Text(
                            text = addFriendError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFriendUsername.isBlank()) {
                            addFriendError = "Please enter a valid username"
                        } else {
                            onAddFriendByName(newFriendUsername)
                            showAddFriendDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.testTag("confirm_add_friend_button")
                ) {
                    Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Detail Dialog when tapping an entry
    selectedEntryForDetail?.let { entry ->
        AlertDialog(
            onDismissRequest = { selectedEntryForDetail = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = entry.avatarEmoji, fontSize = 28.sp)
                    Column {
                        Text(
                            text = entry.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${entry.username}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Rank", color = Color(0xFF94A3B8))
                        Text("#${entry.rank} (${entry.tier})", fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily Streak", color = Color(0xFF94A3B8))
                        Text("${entry.dailyStreak} Days 🔥", fontWeight = FontWeight.Bold, color = AmberAccent)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Quizzes", color = Color(0xFF94A3B8))
                        Text("${entry.totalQuizzes} sessions", fontWeight = FontWeight.Bold, color = Color(0xFFF1F5F9))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Score", color = Color(0xFF94A3B8))
                        Text("${entry.totalScore} pts", fontWeight = FontWeight.Bold, color = IndigoPrimaryLight)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Quiz Accuracy", color = Color(0xFF94A3B8))
                        Text("${String.format(java.util.Locale.getDefault(), "%.1f", entry.accuracyPercent)}%", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dominant Trait", color = Color(0xFF94A3B8))
                        Text(entry.dominantTrait, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                }
            },
            confirmButton = {
                if (!entry.isCurrentUser) {
                    Button(
                        onClick = {
                            onToggleFriend(entry.username)
                            selectedEntryForDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (entry.isFriend) RoseAccent else CyanAccent
                        )
                    ) {
                        Icon(
                            if (entry.isFriend) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (entry.isFriend) "Remove Buddy" else "Add as Study Buddy",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = { selectedEntryForDetail = null },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!entry.isCurrentUser) {
                    TextButton(onClick = { selectedEntryForDetail = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }
}

/**
 * Visual Top-3 Podium (2nd Silver on left, 1st Gold in center, 3rd Bronze on right)
 */
@Composable
private fun PodiumView(
    topThree: List<LeaderboardEntry>,
    currentSort: LeaderboardSort,
    onSelectEntry: (LeaderboardEntry) -> Unit
) {
    val first = topThree.getOrNull(0)
    val second = topThree.getOrNull(1)
    val third = topThree.getOrNull(2)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_podium")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOP PODIUM CHAMPIONS 👑",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = AmberAccent,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                if (second != null) {
                    PodiumPillar(
                        entry = second,
                        place = 2,
                        podiumHeight = 85.dp,
                        accentColor = Color(0xFF94A3B8), // Silver
                        currentSort = currentSort,
                        onClick = { onSelectEntry(second) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 1st Place (Gold)
                if (first != null) {
                    PodiumPillar(
                        entry = first,
                        place = 1,
                        podiumHeight = 115.dp,
                        accentColor = Color(0xFFFFD700), // Gold
                        currentSort = currentSort,
                        onClick = { onSelectEntry(first) },
                        modifier = Modifier.weight(1.15f)
                    )
                }

                // 3rd Place (Bronze)
                if (third != null) {
                    PodiumPillar(
                        entry = third,
                        place = 3,
                        podiumHeight = 65.dp,
                        accentColor = Color(0xFFCD7F32), // Bronze
                        currentSort = currentSort,
                        onClick = { onSelectEntry(third) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumPillar(
    entry: LeaderboardEntry,
    place: Int,
    podiumHeight: androidx.compose.ui.unit.Dp,
    accentColor: Color,
    currentSort: LeaderboardSort,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with Medal Crown
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .size(if (place == 1) 54.dp else 46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(BorderStroke(2.dp, accentColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = entry.avatarEmoji, fontSize = if (place == 1) 26.sp else 22.sp)
            }
            if (place == 1) {
                Text(
                    text = "👑",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = entry.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (entry.isCurrentUser) CyanAccent else Color(0xFFF1F5F9),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val statText = when (currentSort) {
            LeaderboardSort.STREAK -> "${entry.dailyStreak}d 🔥"
            LeaderboardSort.SCORE -> "${entry.totalScore} pts"
            LeaderboardSort.ACCURACY -> "${entry.accuracyPercent.toInt()}%"
        }
        Text(
            text = statText,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = accentColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.35f), Color(0xFF090E1A))
                    )
                )
                .border(
                    BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#$place",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRowItem(
    entry: LeaderboardEntry,
    currentSort: LeaderboardSort,
    onToggleFriend: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (entry.isCurrentUser) CyanAccent.copy(alpha = 0.10f) else Color(0xFF0F172A),
        border = BorderStroke(
            1.dp,
            if (entry.isCurrentUser) CyanAccent.copy(alpha = 0.6f) else Color(0xFF1E293B)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("leaderboard_row_${entry.username}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Rank Number
                Text(
                    text = "#${entry.rank}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = if (entry.isCurrentUser) CyanAccent else Color(0xFF94A3B8),
                    modifier = Modifier.width(32.dp)
                )

                // Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = entry.avatarEmoji, fontSize = 18.sp)
                }

                // Name & Traits
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = entry.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (entry.isCurrentUser) CyanAccent else Color(0xFFF1F5F9),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (entry.isCurrentUser) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CyanAccent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "YOU",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "@${entry.username} • ${entry.dominantTrait}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }

            // Metric Value & Friend Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    val primaryVal = when (currentSort) {
                        LeaderboardSort.STREAK -> "${entry.dailyStreak}d 🔥"
                        LeaderboardSort.SCORE -> "${entry.totalScore} pts"
                        LeaderboardSort.ACCURACY -> "${entry.accuracyPercent.toInt()}%"
                    }
                    val secondaryVal = when (currentSort) {
                        LeaderboardSort.STREAK -> "${entry.totalScore} pts"
                        LeaderboardSort.SCORE -> "${entry.dailyStreak}d streak"
                        LeaderboardSort.ACCURACY -> "${entry.totalQuizzes} quizzes"
                    }
                    Text(
                        text = primaryVal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = when (currentSort) {
                            LeaderboardSort.STREAK -> AmberAccent
                            LeaderboardSort.SCORE -> IndigoPrimaryLight
                            LeaderboardSort.ACCURACY -> EmeraldSuccess
                        }
                    )
                    Text(
                        text = secondaryVal,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                if (!entry.isCurrentUser) {
                    IconButton(
                        onClick = onToggleFriend,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_friend_${entry.username}")
                    ) {
                        Icon(
                            imageVector = if (entry.isFriend) Icons.Default.Star else Icons.Default.PersonAdd,
                            contentDescription = if (entry.isFriend) "Friend" else "Add Friend",
                            tint = if (entry.isFriend) AmberAccent else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
