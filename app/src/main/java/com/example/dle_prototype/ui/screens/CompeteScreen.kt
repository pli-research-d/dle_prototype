package com.example.dle_prototype.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.DuelMatchRecord
import com.example.dle_prototype.data.LeaderboardEntry
import com.example.dle_prototype.data.LeaderboardScope
import com.example.dle_prototype.data.SeasonTier
import com.example.dle_prototype.data.SquadMember
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.UserGamificationState
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompeteScreen(
    user: User,
    dbHelper: DatabaseHelper,
    onOpenFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = League, 1 = Duels, 2 = Friends, 3 = Season

    var gamification by remember { mutableStateOf<UserGamificationState?>(null) }
    var leagueList by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var recentDuels by remember { mutableStateOf<List<DuelMatchRecord>>(emptyList()) }

    // Duel match dialog state
    var isFindingDuel by remember { mutableStateOf(false) }
    var activeDuelResult by remember { mutableStateOf<DuelMatchRecord?>(null) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var friendInput by remember { mutableStateOf("") }
    var pinnedRival by remember { mutableStateOf("Sam 'Algorithm' Lee") }

    suspend fun loadData() {
        gamification = dbHelper.getGamificationState(user.username)
        leagueList = dbHelper.getLeaderboardEntries(user.username, LeaderboardScope.GLOBAL)
        recentDuels = dbHelper.getRecentDuels(user.username)
    }

    LaunchedEffect(user.username) {
        loadData()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF070B12)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented Top Control: [ League ] [ Duels ] [ Friends ] [ Season ]
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F172A),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanAccent
                    )
                }
            ) {
                listOf("League", "Duels", "Friends", "Season").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) CyanAccent else Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("compete_tab_$title")
                    )
                }
            }

            // TAB CONTENT
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> LeagueTabView(
                        user = user,
                        gamification = gamification,
                        leagueList = leagueList,
                        pinnedRival = pinnedRival,
                        onEarnXp = onOpenFeed
                    )
                    1 -> DuelsTabView(
                        user = user,
                        recentDuels = recentDuels,
                        onStartDuel = {
                            coroutineScope.launch {
                                isFindingDuel = true
                                delay(1600)
                                isFindingDuel = false
                                val record = dbHelper.recordDuel(
                                    username = user.username,
                                    opponentName = "Jordan Hayes",
                                    opponentAvatar = "🚀",
                                    category = "JavaScript",
                                    userScore = 9,
                                    opponentScore = 7,
                                    isWin = true,
                                    xp = 50,
                                    fuel = 20
                                )
                                activeDuelResult = record
                                loadData()
                            }
                        }
                    )
                    2 -> FriendsTabView(
                        user = user,
                        pinnedRival = pinnedRival,
                        onPinRival = { pinnedRival = it },
                        onAddFriendClick = { showAddFriendDialog = true }
                    )
                    3 -> SeasonTabView(
                        gamification = gamification
                    )
                }
            }
        }
    }

    // Live Duel Result Dialog
    activeDuelResult?.let { duel ->
        AlertDialog(
            onDismissRequest = { activeDuelResult = null },
            containerColor = Color(0xFF0F172A),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (duel.isWin) Icons.Default.EmojiEvents else Icons.Default.SportsKabaddi,
                        contentDescription = null,
                        tint = if (duel.isWin) AmberAccent else RoseAccent
                    )
                    Text(if (duel.isWin) "Victory! 🏆" else "Match Finished", color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "60-Second Speed Matchup · ${duel.category}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("You", fontWeight = FontWeight.Bold, color = CyanAccent)
                                Text("${duel.userScore}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Text("VS", fontWeight = FontWeight.Black, color = Color(0xFF64748B))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(duel.opponentName, fontWeight = FontWeight.Bold, color = AmberAccent)
                                Text("${duel.opponentScore}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("+${duel.xpEarned} XP", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        Text("+${duel.fuelEarned} Fuel ⛽", fontWeight = FontWeight.Bold, color = AmberAccent)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDuelResult = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Done", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "I just scored ${duel.userScore} in a 60s Duel on PLi! Can you beat me?")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Duel"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
        )
    }

    // Add Friend Dialog
    if (showAddFriendDialog) {
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text("Add Friend") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter peer username to track XP & challenge directly:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    OutlinedTextField(
                        value = friendInput,
                        onValueChange = { friendInput = it },
                        placeholder = { Text("e.g. dev_sarah") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (friendInput.isNotBlank()) {
                            coroutineScope.launch {
                                dbHelper.addFriend(user.username, friendInput.trim())
                                showAddFriendDialog = false
                                friendInput = ""
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Matchmaking spinner overlay
    if (isFindingDuel) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Matchmaking...", color = Color.White) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(36.dp))
                    Text("Matching with a peer with similar mastery rating...", fontSize = 13.sp, color = Color(0xFFCBD5E1))
                }
            },
            confirmButton = {}
        )
    }
}

// -----------------------------------------------------------------------------
// SUB-TAB 0: LEAGUE
// -----------------------------------------------------------------------------
@Composable
private fun LeagueTabView(
    user: User,
    gamification: UserGamificationState?,
    leagueList: List<LeaderboardEntry>,
    pinnedRival: String,
    onEarnXp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Header: Tier Badge (Bronze -> Diamond) + Days Left
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF131D31),
            border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF94A3B8).copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🥈", fontSize = 22.sp)
                        }
                    }
                    Column {
                        Text(
                            text = "${gamification?.currentLeague ?: "Silver"} League",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Zone: Promotion Band (Top 5)",
                            fontSize = 11.sp,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A)
                ) {
                    Text(
                        text = "⏳ 3 days left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rival Pin Banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF1E1533),
            border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.PushPin, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(16.dp))
                Text(
                    text = "Rival Target: You're 40 XP behind $pinnedRival!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE9D5FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Zone bands legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("▲ Top 5: Promotion", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
            Text("Safe Zone (6-20)", fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text("▼ Bottom 5: Relegation", fontSize = 10.sp, color = RoseAccent, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ranked List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(leagueList) { index, entry ->
                val rank = index + 1
                val isUser = entry.isCurrentUser || entry.username.equals(user.username, ignoreCase = true)
                val zoneBorder = when {
                    rank <= 5 -> EmeraldSuccess.copy(alpha = 0.3f)
                    rank > 20 -> RoseAccent.copy(alpha = 0.3f)
                    else -> Color(0xFF1E293B)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isUser) CyanAccent.copy(alpha = 0.12f) else Color(0xFF0F172A),
                    border = BorderStroke(1.dp, if (isUser) CyanAccent else zoneBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "#$rank",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = when (rank) {
                                    1 -> AmberAccent
                                    2 -> Color(0xFFCBD5E1)
                                    3 -> Color(0xFFD97706)
                                    else -> Color(0xFF64748B)
                                },
                                modifier = Modifier.width(28.dp)
                            )
                            Text(entry.avatarEmoji, fontSize = 16.sp)
                            Column {
                                Text(
                                    text = if (isUser) "${entry.displayName} (You)" else entry.displayName,
                                    fontWeight = if (isUser) FontWeight.Black else FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isUser) CyanAccent else Color.White
                                )
                                Text("🔥 ${entry.dailyStreak} streak", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Text(
                            text = "${entry.totalScore} XP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFF1F5F9)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pinned "Earn XP" Button
        Button(
            onClick = onEarnXp,
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("league_earn_xp_button")
        ) {
            Text("Earn XP in Feed 🚀", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

// -----------------------------------------------------------------------------
// SUB-TAB 1: DUELS
// -----------------------------------------------------------------------------
@Composable
private fun DuelsTabView(
    user: User,
    recentDuels: List<DuelMatchRecord>,
    onStartDuel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Duel Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1035),
            border = BorderStroke(1.dp, Color(0xFFA855F7)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.SportsKabaddi, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(36.dp))
                Text("60-Second Real-Time Duels", fontWeight = FontWeight.Black, fontSize = 17.sp, color = Color.White)
                Text(
                    text = "Answer 5 shared code questions simultaneously against a skill-matched opponent. Fastest correct answers earn Fuel & XP bonuses!",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onStartDuel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("find_duel_button")
                ) {
                    Text("Find a 60-sec Duel ⚔️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Text("RECENT DUELS (LAST 5)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(recentDuels) { d ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, if (d.isWin) EmeraldSuccess.copy(alpha = 0.4f) else RoseAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(d.opponentAvatar, fontSize = 20.sp)
                            Column {
                                Text("vs ${d.opponentName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                Text("${d.category} · Score: ${d.userScore} - ${d.opponentScore}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (d.isWin) "WIN" else "LOSS",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (d.isWin) EmeraldSuccess else RoseAccent
                            )
                            Text("+${d.xpEarned} XP", fontSize = 10.sp, color = CyanAccent)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SUB-TAB 2: FRIENDS & SQUAD
// -----------------------------------------------------------------------------
@Composable
private fun FriendsTabView(
    user: User,
    pinnedRival: String,
    onPinRival: (String) -> Unit,
    onAddFriendClick: () -> Unit
) {
    val friends = listOf(
        Triple("Sam 'Algorithm' Lee", "⚡", 420),
        Triple("Sarah Chen", "🦊", 380),
        Triple("Alex Rivera", "🐬", 310)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action: Add Friend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FRIENDS & RIVALS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF94A3B8))
            Button(
                onClick = onAddFriendClick,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Friend", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        // Friends list
        friends.forEach { (name, emoji, weeklyXp) ->
            val isRival = pinnedRival == name
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, if (isRival) Color(0xFFA855F7) else Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(emoji, fontSize = 20.sp)
                        Column {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("$weeklyXp weekly XP", fontSize = 11.sp, color = AmberAccent)
                        }
                    }
                    Button(
                        onClick = { onPinRival(name) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRival) Color(0xFFA855F7) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isRival) "📌 Rival" else "Pin Rival", fontSize = 11.sp)
                    }
                }
            }
        }

        // Squad Section
        Text("WEEKLY SQUAD QUEST", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF94A3B8))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131D31),
            border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Squad Goal: 5,000 XP", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    Text("3,450 / 5,000 (69%)", fontSize = 11.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { 0.69f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = CyanAccent,
                    trackColor = Color(0xFF1E293B)
                )
                Text("Reward: +100 Fuel & Cosmic Mystery Chest upon reaching 5,000 XP before Sunday midnight!", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SUB-TAB 3: SEASON
// -----------------------------------------------------------------------------
@Composable
private fun SeasonTabView(
    gamification: UserGamificationState?
) {
    val currentXp = gamification?.totalXp ?: 340
    val tiers = (1..15).map { lvl ->
        val req = lvl * 100
        SeasonTier(
            level = lvl,
            xpRequired = req,
            freeReward = "+${lvl * 10} Fuel",
            bonusReward = if (lvl % 5 == 0) "Rocket Skin" else "+${lvl * 25} XP",
            isUnlocked = currentXp >= req,
            isClaimed = currentXp >= req + 50
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Theme & Countdown
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1035),
            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("SEASON 1 · ORBITAL GENESIS", fontWeight = FontWeight.Black, fontSize = 11.sp, color = AmberAccent)
                Text("Cyberpunk Algorithms", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                Text("Climb all 30 seasonal tiers before the window closes. Free track is unlocked for all learners.", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                Spacer(modifier = Modifier.height(4.dp))
                Text("⏳ 14 Days Remaining", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyanAccent)
            }
        }

        Text("SEASON REWARD TRACK (TIERS 1 - 15)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))

        tiers.forEach { tier ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (tier.isUnlocked) Color(0xFF0F172A) else Color(0xFF090D16),
                border = BorderStroke(1.dp, if (tier.isUnlocked) EmeraldSuccess.copy(alpha = 0.5f) else Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = if (tier.isUnlocked) EmeraldSuccess.copy(alpha = 0.2f) else Color(0xFF1E293B),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${tier.level}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = if (tier.isUnlocked) EmeraldSuccess else Color(0xFF64748B))
                            }
                        }
                        Column {
                            Text("Free: ${tier.freeReward}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("Bonus: ${tier.bonusReward}", fontSize = 11.sp, color = Color(0xFFA855F7))
                        }
                    }
                    if (tier.isUnlocked) {
                        Text("Unlocked ✅", color = EmeraldSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("${tier.xpRequired} XP 🔒", color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
