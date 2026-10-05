package com.example.dle_prototype.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.dle_prototype.data.ml.PeakLearningHoursAnalysis
import com.example.dle_prototype.data.ml.PeakLearningHoursAnalyzer
import com.example.dle_prototype.notifications.PeakStudyNotificationManager
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess

/**
 * Intelligent card displaying the user's historical peak learning hours identified from
 * progress data, along with smart local notification study session scheduling.
 */
@Composable
fun PeakStudyNotificationCard(
    username: String,
    analysis: PeakLearningHoursAnalysis,
    modifier: Modifier = Modifier,
    onStartPractice: () -> Unit = {},
    onNotificationTested: (String) -> Unit = {}
) {
    val context = LocalContext.current

    var isEnabled by remember {
        mutableStateOf(PeakStudyNotificationManager.isPeakStudyEnabled(context))
    }
    var isAutoDetect by remember {
        mutableStateOf(PeakStudyNotificationManager.isAutoDetectEnabled(context))
    }
    var scheduledHour by remember {
        mutableIntStateOf(
            if (isAutoDetect) analysis.peakHour else PeakStudyNotificationManager.getScheduledHour(context)
        )
    }

    var testNotificationSentMessage by remember { mutableStateOf<String?>(null) }

    // Android 13+ Notification Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isEnabled = true
            PeakStudyNotificationManager.setPeakStudyEnabled(context, true, scheduledHour)
            onNotificationTested("Notifications enabled! Study suggestions active.")
        } else {
            isEnabled = false
            PeakStudyNotificationManager.setPeakStudyEnabled(context, false)
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0D1527),
        border = BorderStroke(
            1.dp,
            if (isEnabled) CyanAccent.copy(alpha = 0.5f) else Color(0xFF1E293B)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("peak_study_notification_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Lightning Icon + Title + Calibrated Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Peak Learning",
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "PEAK LEARNING INTELLIGENCE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Text(
                            text = "Historical Cognitive Focus Analysis",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Calibration Status Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (analysis.isCalibrated) EmeraldSuccess.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.dp,
                        if (analysis.isCalibrated) EmeraldSuccess.copy(alpha = 0.4f) else AmberAccent.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (analysis.isCalibrated) "CALIBRATED ⚡" else "LEARNING 📊",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (analysis.isCalibrated) EmeraldSuccess else AmberAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Hero Window & Accuracy Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF131D31),
                border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HISTORICAL PEAK WINDOW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = analysis.peakWindowFormatted,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanAccent,
                                modifier = Modifier.testTag("peak_window_text")
                            )
                            Text(
                                text = analysis.timeOfDayLabel,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Accuracy Comparison Metric Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0B132B),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${analysis.accuracyAtPeakPercent.toInt()}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldSuccess
                                )
                                Text(
                                    text = "Peak Accuracy",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    // Recommendation text
                    Text(
                        text = analysis.recommendationMessage,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )

                    // Confidence Meter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Model Confidence: ${(analysis.confidenceScore * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        LinearProgressIndicator(
                            progress = { analysis.confidenceScore },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CyanAccent,
                            trackColor = Color(0xFF1E293B)
                        )
                        Text(
                            text = "${analysis.totalSessionsAnalyzed} sessions",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // 24-Hour Activity Distribution Mini-Bar Chart
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HOURLY COGNITIVE ACTIVITY (24H)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Peak: ${analysis.peakHourFormatted}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxCount = (analysis.hourlyDistribution.values.maxOrNull() ?: 1).coerceAtLeast(1)

                        (0..23).forEach { hour ->
                            val count = analysis.hourlyDistribution[hour] ?: 0
                            val isPeak = hour == analysis.peakHour
                            val isSecondary = hour == analysis.secondaryPeakHour
                            val normalizedHeight = if (count > 0) {
                                (count.toFloat() / maxCount.toFloat()).coerceIn(0.2f, 1.0f)
                            } else 0.08f

                            val barColor = when {
                                isPeak -> CyanAccent
                                isSecondary -> EmeraldSuccess
                                count > 0 -> Color(0xFF38BDF8).copy(alpha = 0.5f)
                                else -> Color(0xFF1E293B)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .fillMaxHeight(normalizedHeight)
                                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                        .background(barColor)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("12 AM", fontSize = 9.sp, color = Color(0xFF475569))
                    Text("6 AM", fontSize = 9.sp, color = Color(0xFF475569))
                    Text("12 PM", fontSize = 9.sp, color = Color(0xFF475569))
                    Text("6 PM", fontSize = 9.sp, color = Color(0xFF475569))
                    Text("11 PM", fontSize = 9.sp, color = Color(0xFF475569))
                }
            }

            // Notification Scheduling Controls
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF131D31),
                border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Toggle: Enable Peak Study Suggestions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (isEnabled) CyanAccent else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Peak Study Suggestions",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Receive smart notification reminders at ${PeakLearningHoursAnalyzer.formatHour(scheduledHour)}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasPermission) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@Switch
                                    }
                                }
                                isEnabled = checked
                                PeakStudyNotificationManager.setPeakStudyEnabled(context, checked, scheduledHour)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0F172A),
                                checkedTrackColor = CyanAccent
                            ),
                            modifier = Modifier.testTag("peak_study_notifications_switch")
                        )
                    }

                    // Auto-sync or custom hour selection chips
                    AnimatedVisibility(visible = isEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SUGGESTION TRIGGER TIME",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF64748B)
                                )

                                Text(
                                    text = if (isAutoDetect) "⚡ Auto-calibrated" else "Custom",
                                    fontSize = 10.sp,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Option 1: Auto Peak
                                FilterChip(
                                    selected = isAutoDetect,
                                    onClick = {
                                        isAutoDetect = true
                                        scheduledHour = analysis.peakHour
                                        PeakStudyNotificationManager.setAutoDetectEnabled(context, true)
                                        PeakStudyNotificationManager.schedulePeakStudyAlarm(context, analysis.peakHour)
                                    },
                                    label = {
                                        Text(
                                            text = "Auto Peak (${analysis.peakHourFormatted})",
                                            fontSize = 11.sp,
                                            fontWeight = if (isAutoDetect) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanAccent,
                                        selectedLabelColor = Color(0xFF0F172A),
                                        containerColor = Color(0xFF0B132B),
                                        labelColor = Color(0xFF94A3B8)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isAutoDetect,
                                        borderColor = Color(0xFF1E293B),
                                        selectedBorderColor = CyanAccent
                                    ),
                                    modifier = Modifier.testTag("chip_auto_peak_hour")
                                )

                                // Preset hours
                                listOf(9 to "9:00 AM", 12 to "12:00 PM", 18 to "6:00 PM", 20 to "8:00 PM").forEach { (h, label) ->
                                    val isSel = !isAutoDetect && scheduledHour == h
                                    FilterChip(
                                        selected = isSel,
                                        onClick = {
                                            isAutoDetect = false
                                            scheduledHour = h
                                            PeakStudyNotificationManager.setAutoDetectEnabled(context, false)
                                            PeakStudyNotificationManager.setScheduledHour(context, h)
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
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
                        }
                    }
                }
            }

            // Notification Feedback Text
            testNotificationSentMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = msg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            // Action Buttons: [ Test Notification ] and [ Start Study Session ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!hasPermission) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@OutlinedButton
                            }
                        }
                        PeakStudyNotificationManager.sendTestNotification(context, username, analysis)
                        testNotificationSentMessage = "Test study suggestion sent to notification tray! Check your status bar."
                        onNotificationTested("Test study suggestion sent! Check notification shade.")
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("test_peak_notification_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Test Alert",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }

                Button(
                    onClick = onStartPractice,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .testTag("start_peak_study_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Study Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }
    }
}
