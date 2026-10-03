package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.MysteryChest
import com.example.dle_prototype.data.RewardHistoryItem
import com.example.dle_prototype.data.UserGamificationState
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsHubSheet(
    username: String,
    dbHelper: DatabaseHelper,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var gamification by remember { mutableStateOf<UserGamificationState?>(null) }
    var chests by remember { mutableStateOf<List<MysteryChest>>(emptyList()) }
    var rewardHistory by remember { mutableStateOf<List<RewardHistoryItem>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Chests, 1 = Hangar Shop, 2 = History
    var openedChestReward by remember { mutableStateOf<MysteryChest?>(null) }
    var freezePurchaseNotice by remember { mutableStateOf<String?>(null) }

    suspend fun refreshData() {
        gamification = dbHelper.getGamificationState(username)
        chests = dbHelper.getChests(username)
        rewardHistory = dbHelper.getRewardHistory(username)
    }

    LaunchedEffect(username) {
        refreshData()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B101D),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = AmberAccent)
                    Text("Rewards Hub & Hangar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Currency & Assets Balance Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF131D31),
                border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fuel balance
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                            Text("${gamification?.fuel ?: 0}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = AmberAccent)
                        }
                        Text("⛽ Fuel Balance", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }

                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFF1E2E4A)))

                    // Streak Freezes
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Text("${gamification?.streakFreezes ?: 0}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = CyanAccent)
                        }
                        Text("🧊 Streak Freezes", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }

                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFF1E2E4A)))

                    // Total XP
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${gamification?.totalXp ?: 0}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = EmeraldSuccess)
                        Text("⚡ Total XP", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-tabs: [ Chest Inbox ] [ Hangar Shop ] [ Drops History ]
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
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        val unopened = chests.count { !it.isOpened }
                        Text(
                            text = if (unopened > 0) "Chests ($unopened)" else "Chests",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) CyanAccent else Color(0xFF94A3B8)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Hangar",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) CyanAccent else Color(0xFF94A3B8)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "History",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) CyanAccent else Color(0xFF94A3B8)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CONTENT BY TAB
            when (selectedTab) {
                // 0. CHESTS INBOX
                0 -> {
                    val unopened = chests.filter { !it.isOpened }
                    if (unopened.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No unopened chests right now", color = Color(0xFF94A3B8), fontSize = 13.sp)
                                Text("Answer streaks and win Duels to earn Mystery Drops!", color = Color(0xFF64748B), fontSize = 11.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().height(280.dp)
                        ) {
                            items(unopened) { chest ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E1533),
                                    border = BorderStroke(1.dp, Color(0xFFA855F7)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(28.dp))
                                            Column {
                                                Text(chest.chestType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                                Text("Contains XP, Fuel, and rare cosmetic loot", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val res = dbHelper.openMysteryChest(chest.id)
                                                    openedChestReward = res
                                                    refreshData()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Open", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 1. HANGAR SHOP
                1 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Buy Streak Freeze
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF131D31),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Default.AcUnit, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(26.dp))
                                    Column {
                                        Text("Streak Freeze Shield 🧊", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                        Text("Protects your streak if you miss a day", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val ok = dbHelper.buyStreakFreeze(username, cost = 30)
                                            if (ok) {
                                                freezePurchaseNotice = "Streak Freeze acquired successfully!"
                                                refreshData()
                                            } else {
                                                freezePurchaseNotice = "Not enough Fuel! (Requires 30 ⛽)"
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("30 ⛽", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Cosmetic Rocket Skins
                        Text("ROCKET SKINS (COSMETIC)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        val skins = listOf(
                            Triple("Apollo Standard", "Default orbital capsule", true),
                            Triple("Cosmic Voyager", "Deep neon hull with trail", true),
                            Triple("Solar Falcon", "Gold-plated heatshield", false)
                        )
                        skins.forEach { (name, desc, unlocked) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, if (unlocked) EmeraldSuccess.copy(alpha = 0.4f) else Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = if (unlocked) EmeraldSuccess else Color(0xFF64748B))
                                        Column {
                                            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                            Text(desc, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        }
                                    }
                                    if (unlocked) {
                                        Text("Owned", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Chest Drop", color = Color(0xFFA855F7), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. REWARD HISTORY
                2 -> {
                    if (rewardHistory.isEmpty()) {
                        Text("No drop history recorded yet.", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        ) {
                            items(rewardHistory) { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                            Text(item.description, fontSize = 10.sp, color = Color(0xFF94A3B8))
                                        }
                                        if (item.fuelChange != 0) {
                                            Text(
                                                "${if (item.fuelChange > 0) "+" else ""}${item.fuelChange} ⛽",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (item.fuelChange > 0) AmberAccent else RoseAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Chest Opened Celebration Dialog
    openedChestReward?.let { reward ->
        AlertDialog(
            onDismissRequest = { openedChestReward = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = AmberAccent)
                    Text("Chest Unlocked! 🎉")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Congratulations! Your mystery drop contained:", fontSize = 13.sp)
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0F172A), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("⚡ +${reward.rewardXp} XP", fontWeight = FontWeight.Bold, color = CyanAccent)
                            Text("⛽ +${reward.rewardFuel} Fuel", fontWeight = FontWeight.Bold, color = AmberAccent)
                            if (reward.rewardBadge.isNotBlank()) {
                                Text("🏅 Rare Badge: ${reward.rewardBadge}", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }
                            if (reward.rewardSkin.isNotBlank()) {
                                Text("🚀 Rocket Skin: ${reward.rewardSkin}", fontWeight = FontWeight.Bold, color = Color(0xFFC084FC))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { openedChestReward = null }) {
                    Text("Awesome!")
                }
            }
        )
    }

    // Purchase Notice Dialog
    freezePurchaseNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = { freezePurchaseNotice = null },
            title = { Text("Shop Notice") },
            text = { Text(notice) },
            confirmButton = {
                Button(onClick = { freezePurchaseNotice = null }) {
                    Text("OK")
                }
            }
        )
    }
}
