package com.example.dle_prototype.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.dle_prototype.data.AvatarThemePreset
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.UserSettings
import com.example.dle_prototype.data.UserSettingsManager
import com.example.dle_prototype.notifications.DailyStreakReminderManager
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    user: User,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load initial user settings
    var settings by remember {
        mutableStateOf(UserSettingsManager.loadSettings(context, user.username))
    }

    var editDisplayName by remember(settings.displayName) {
        mutableStateOf(settings.displayName)
    }

    var isDisplayNameChanged by remember(editDisplayName, settings.displayName) {
        mutableStateOf(editDisplayName.trim() != settings.displayName.trim() && editDisplayName.isNotBlank())
    }

    // Permission launcher for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            settings = settings.copy(dailyReminderEnabled = true)
            UserSettingsManager.saveSettings(context, user.username, settings)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications enabled successfully")
            }
        } else {
            settings = settings.copy(dailyReminderEnabled = false)
            UserSettingsManager.saveSettings(context, user.username, settings)
        }
    }

    fun parseColorHex(hex: String, fallback: Color): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            fallback
        }
    }

    val themePrimaryColor = parseColorHex(settings.primaryColorHex, CyanAccent)
    val themeSecondaryColor = parseColorHex(settings.secondaryColorHex, IndigoPrimaryLight)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Profile, themes & notification preferences",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("settings_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Live Profile & Display Name Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, themePrimaryColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_settings_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section Header with Live Avatar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Live Preview Avatar Badge
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(themePrimaryColor, themeSecondaryColor)
                                        )
                                    )
                                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    .testTag("live_avatar_preview"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = settings.avatarEmoji,
                                    fontSize = 32.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = settings.displayName.ifBlank { user.username },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "@${user.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = themePrimaryColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${settings.avatarThemeName} Theme",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themePrimaryColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Display Name Editor
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Display Name",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE2E8F0)
                            )

                            OutlinedTextField(
                                value = editDisplayName,
                                onValueChange = { editDisplayName = it },
                                placeholder = { Text("Enter your public display name") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = themePrimaryColor,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("display_name_input")
                            )

                            if (isDisplayNameChanged) {
                                Button(
                                    onClick = {
                                        val newSettings = settings.copy(displayName = editDisplayName.trim())
                                        settings = newSettings
                                        UserSettingsManager.saveSettings(context, user.username, newSettings)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Display name updated to: ${editDisplayName.trim()}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = themePrimaryColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .testTag("save_display_name_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Save Name",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Avatar Theme Picker Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("avatar_theme_section")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = themePrimaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Personalized Avatar & Themes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Avatar Emoji Picker
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Choose Avatar Symbol",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                UserSettingsManager.AVATAR_EMOJIS.forEach { emoji ->
                                    val isSelected = settings.avatarEmoji == emoji
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) themePrimaryColor.copy(alpha = 0.25f)
                                                else Color(0xFF1E293B)
                                            )
                                            .border(
                                                if (isSelected) 2.dp else 1.dp,
                                                if (isSelected) themePrimaryColor else Color(0xFF334155),
                                                CircleShape
                                            )
                                            .clickable {
                                                val newSettings = settings.copy(avatarEmoji = emoji)
                                                settings = newSettings
                                                UserSettingsManager.saveSettings(context, user.username, newSettings)
                                            }
                                            .testTag("avatar_emoji_$emoji"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 20.sp)
                                    }
                                }
                            }
                        }

                        // Avatar Theme Presets Grid
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Select Theme Preset",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8)
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                maxItemsInEachRow = 2
                            ) {
                                UserSettingsManager.AVATAR_PRESETS.forEach { preset ->
                                    val isSelected = settings.avatarThemeId == preset.id
                                    val pPrimary = parseColorHex(preset.primaryColorHex, CyanAccent)
                                    val pSecondary = parseColorHex(preset.secondaryColorHex, IndigoPrimaryLight)

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0B132B),
                                        border = BorderStroke(
                                            if (isSelected) 1.8.dp else 1.dp,
                                            if (isSelected) pPrimary else Color(0xFF334155)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                val newSettings = settings.copy(
                                                    avatarThemeId = preset.id,
                                                    avatarThemeName = preset.name,
                                                    primaryColorHex = preset.primaryColorHex,
                                                    secondaryColorHex = preset.secondaryColorHex,
                                                    avatarEmoji = preset.emoji
                                                )
                                                settings = newSettings
                                                UserSettingsManager.saveSettings(context, user.username, newSettings)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Applied ${preset.name} theme")
                                                }
                                            }
                                            .testTag("theme_preset_${preset.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Mini Color Orb
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(listOf(pPrimary, pSecondary))
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = preset.emoji, fontSize = 16.sp)
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = preset.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                                )
                                                Text(
                                                    text = preset.description,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF94A3B8),
                                                    maxLines = 1,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = pPrimary,
                                                    modifier = Modifier.size(16.dp)
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

            // Section 3: Notification Preferences Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notification_settings_section")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = themePrimaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Notification Preferences",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Android 13+ Warning Banner if permission denied
                        if (!hasNotificationPermission) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RoseAccent.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, RoseAccent.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = RoseAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Notifications Disabled",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = RoseAccent
                                        )
                                        Text(
                                            text = "Enable permission to receive daily streak alerts.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Grant", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Toggle 1: Daily Streak Reminder
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Streak Protection Alerts",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Get reminded before midnight so your streak doesn't reset.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = settings.dailyReminderEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        val newSettings = settings.copy(dailyReminderEnabled = isChecked)
                                        settings = newSettings
                                        UserSettingsManager.saveSettings(context, user.username, newSettings)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = themePrimaryColor
                                ),
                                modifier = Modifier.testTag("daily_reminder_switch")
                            )
                        }

                        // Reminder Time Schedule Selector
                        AnimatedVisibility(visible = settings.dailyReminderEnabled) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Scheduled Reminder Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFCBD5E1),
                                    fontWeight = FontWeight.SemiBold
                                )

                                val timePresets = listOf(
                                    Pair(8, 0) to "8:00 AM",
                                    Pair(12, 0) to "12:00 PM",
                                    Pair(18, 0) to "6:00 PM",
                                    Pair(20, 0) to "8:00 PM",
                                    Pair(22, 0) to "10:00 PM"
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    timePresets.forEach { (time, label) ->
                                        val isCurrentTime = settings.reminderHour == time.first && settings.reminderMinute == time.second
                                        FilterChip(
                                            selected = isCurrentTime,
                                            onClick = {
                                                val newSettings = settings.copy(
                                                    reminderHour = time.first,
                                                    reminderMinute = time.second
                                                )
                                                settings = newSettings
                                                UserSettingsManager.saveSettings(context, user.username, newSettings)
                                            },
                                            label = { Text(label, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = themePrimaryColor,
                                                selectedLabelColor = Color.Black,
                                                containerColor = Color(0xFF0F172A),
                                                labelColor = Color(0xFFCBD5E1)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Toggle 2: Achievement Alerts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Achievement & Milestone Celebrations",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Notifications when you unlock new badges or rank milestones.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = settings.achievementAlertsEnabled,
                                onCheckedChange = { isChecked ->
                                    val newSettings = settings.copy(achievementAlertsEnabled = isChecked)
                                    settings = newSettings
                                    UserSettingsManager.saveSettings(context, user.username, newSettings)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = themePrimaryColor
                                ),
                                modifier = Modifier.testTag("achievement_alert_switch")
                            )
                        }

                        // Toggle 3: Weekly Cognitive Growth Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Weekly Cognitive Growth Digest",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Weekly recap of your learning velocity and trait progression.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = settings.weeklyInsightsEnabled,
                                onCheckedChange = { isChecked ->
                                    val newSettings = settings.copy(weeklyInsightsEnabled = isChecked)
                                    settings = newSettings
                                    UserSettingsManager.saveSettings(context, user.username, newSettings)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = themePrimaryColor
                                ),
                                modifier = Modifier.testTag("weekly_insights_switch")
                            )
                        }

                        // Toggle 4: Haptic Vibration Feedback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Haptic Vibration Feedback",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tactile haptic pulses on correct/incorrect quiz answers.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = settings.hapticFeedbackEnabled,
                                onCheckedChange = { isChecked ->
                                    val newSettings = settings.copy(hapticFeedbackEnabled = isChecked)
                                    settings = newSettings
                                    UserSettingsManager.saveSettings(context, user.username, newSettings)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = themePrimaryColor
                                ),
                                modifier = Modifier.testTag("haptic_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Trigger Test Notification Button
                        OutlinedButton(
                            onClick = {
                                DailyStreakReminderManager.sendStreakReminderNotification(
                                    context = context,
                                    username = settings.displayName.ifBlank { user.username },
                                    currentStreak = 3
                                )
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Test streak reminder notification dispatched! 🔔")
                                }
                            },
                            border = BorderStroke(1.dp, themePrimaryColor.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("send_test_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = themePrimaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Send Test Notification",
                                color = themePrimaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
