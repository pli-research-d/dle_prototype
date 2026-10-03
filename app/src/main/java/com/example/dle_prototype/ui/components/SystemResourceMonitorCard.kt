package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.ml.SystemResourceSnapshot
import com.example.dle_prototype.data.ml.ThermalStatusLevel
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent

@Composable
fun SystemResourceMonitorCard(
    snapshot: SystemResourceSnapshot,
    history: List<SystemResourceSnapshot>,
    autoThrottleEnabled: Boolean,
    onToggleAutoThrottle: (Boolean) -> Unit,
    batterySaverEnabled: Boolean,
    onToggleBatterySaver: (Boolean) -> Unit,
    onToggleSimulation: ((Boolean) -> Unit)? = null,
    onToggleSimulatedLowBattery: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (snapshot.thermalStatus) {
            ThermalStatusLevel.NONE -> EmeraldSuccess
            ThermalStatusLevel.LIGHT -> CyanAccent
            ThermalStatusLevel.MODERATE -> AmberAccent
            ThermalStatusLevel.SEVERE,
            ThermalStatusLevel.CRITICAL,
            ThermalStatusLevel.EMERGENCY,
            ThermalStatusLevel.SHUTDOWN -> RoseAccent
        },
        label = "status_color"
    )

    val cpuColor = when {
        snapshot.cpuUsagePercent >= 88f -> RoseAccent
        snapshot.cpuUsagePercent >= 75f -> AmberAccent
        snapshot.cpuUsagePercent >= 45f -> CyanAccent
        else -> EmeraldSuccess
    }

    val batteryColor = when {
        snapshot.isCharging -> EmeraldSuccess
        snapshot.batteryPercent <= 20 -> RoseAccent
        snapshot.batteryPercent <= 40 -> AmberAccent
        else -> EmeraldSuccess
    }

    // Pulse animation for warning states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("resource_monitor_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            if (snapshot.isBatterySaverEngaged) AmberAccent.copy(alpha = 0.8f)
            else if (snapshot.isThrottling) statusColor.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title, Subtitle, and Status Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (snapshot.isBatterySaverEngaged) AmberAccent.copy(alpha = 0.15f)
                                else statusColor.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (snapshot.isBatterySaverEngaged) Icons.Default.Bolt else Icons.Default.Speed,
                            contentDescription = null,
                            tint = if (snapshot.isBatterySaverEngaged) AmberAccent else statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Real-Time Resource Monitor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hardware thermals, CPU load & Battery Saver",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Battery Saver Pill if active
                    if (snapshot.isBatterySaverEngaged) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .testTag("battery_saver_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "SAVER ON",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmberAccent
                                )
                            }
                        }
                    }

                    // Thermal Status Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("thermal_status_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (snapshot.isThrottling) statusColor.copy(alpha = pulseAlpha)
                                        else statusColor
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = snapshot.thermalStatus.label.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Telemetry Grid: CPU & Thermal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CPU Utilization Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cpu_metric_box"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CPU LOAD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                tint = cpuColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${"%.1f".format(snapshot.cpuUsagePercent)}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = cpuColor,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("cpu_usage_text")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (snapshot.cpuUsagePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = cpuColor,
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${snapshot.cpuCoreCount} Cores Active",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Thermal & SoC Temp Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("thermal_metric_box"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "THERMAL LOAD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            Icon(
                                Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = snapshot.temperatureCelsius?.let { "${"%.1f".format(it)}°C" } ?: "Tier ${snapshot.thermalStatusCode}/6",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Visual Thermal Scale Gauge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            (0..4).forEach { tier ->
                                val active = snapshot.thermalStatus.severity >= tier
                                val blockColor = when (tier) {
                                    0 -> EmeraldSuccess
                                    1 -> CyanAccent
                                    2 -> AmberAccent
                                    3 -> Color(0xFFF97316)
                                    else -> RoseAccent
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (active) blockColor else Color(0xFF1E293B))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (snapshot.isThrottling) "Throttling Active" else "Nominal Headroom",
                            fontSize = 9.sp,
                            color = if (snapshot.isThrottling) RoseAccent else Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (snapshot.isThrottling) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Telemetry Grid: Battery & Adaptive Pacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Battery Level & Charging Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("battery_metric_box"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BATTERY LEVEL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            Icon(
                                if (snapshot.isCharging) Icons.Default.BatteryChargingFull
                                else if (snapshot.batteryPercent <= 20) Icons.Default.BatteryAlert
                                else Icons.Default.BatteryFull,
                                contentDescription = null,
                                tint = batteryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${snapshot.batteryPercent}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = batteryColor,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.testTag("battery_percent_text")
                            )
                            if (snapshot.isCharging) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EmeraldSuccess.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("CHARGING", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                }
                            } else if (snapshot.isLowBattery) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RoseAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("LOW BATTERY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = RoseAccent)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (snapshot.batteryPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = batteryColor,
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (snapshot.isCharging) "Power Connected"
                            else if (snapshot.isLowBattery) "Critical Energy Level"
                            else "Discharging",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Inter-Epoch Pacing & Telemetry Polling Rate Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pacing_metric_box"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXEC PACING & POLL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = if (snapshot.isBatterySaverEngaged) AmberAccent else EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (snapshot.trainingPacingDelayMs > 0) "+${snapshot.trainingPacingDelayMs}ms Yield" else "Full Pace (0ms)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (snapshot.trainingPacingDelayMs > 0) AmberAccent else EmeraldSuccess,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("pacing_delay_text")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = "Polling Rate: ${snapshot.pollingIntervalMs / 1000}s/sample",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (snapshot.pollingIntervalMs > 1000L) AmberAccent else CyanAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (snapshot.isBatterySaverEngaged) "Energy Throttling Engaged" else "Nominal Polling (1s)",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time CPU Waveform Canvas (Sparkline)
            Text(
                text = "REAL-TIME TELEMETRY SPARKLINE (30s)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
                    .testTag("resource_sparkline_canvas"),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF090D16),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Canvas(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    val w = size.width
                    val h = size.height

                    val threshY = h * (1f - 0.85f)
                    drawLine(
                        color = RoseAccent.copy(alpha = 0.35f),
                        start = Offset(0f, threshY),
                        end = Offset(w, threshY),
                        strokeWidth = 1.dp.toPx()
                    )

                    if (history.size > 1) {
                        val stepX = w / (history.size - 1).coerceAtLeast(1)
                        val cpuPath = Path()
                        val fillPath = Path()

                        history.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val normalizedCpu = (pt.cpuUsagePercent / 100f).coerceIn(0f, 1f)
                            val y = h * (1f - normalizedCpu)

                            if (i == 0) {
                                cpuPath.moveTo(x, y)
                                fillPath.moveTo(x, h)
                                fillPath.lineTo(x, y)
                            } else {
                                cpuPath.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                        }

                        val lastX = (history.size - 1) * stepX
                        fillPath.lineTo(lastX, h)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(CyanAccent.copy(alpha = 0.25f), Color.Transparent),
                                startY = 0f,
                                endY = h
                            )
                        )

                        drawPath(
                            path = cpuPath,
                            color = CyanAccent,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Battery Saver / Throttling Dynamic Recommendation Banner
            AnimatedVisibility(visible = snapshot.isBatterySaverEngaged || snapshot.isThrottling) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    val bannerBorder = if (snapshot.isBatterySaverEngaged) AmberAccent else statusColor
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resource_alert_banner"),
                        shape = RoundedCornerShape(10.dp),
                        color = bannerBorder.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, bannerBorder.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (snapshot.isBatterySaverEngaged) Icons.Default.Bolt else Icons.Default.Warning,
                                contentDescription = null,
                                tint = bannerBorder,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (snapshot.isBatterySaverEngaged) "Battery Saver Mode Active" else "Thermal Threshold Alert",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bannerBorder
                                )
                                Text(
                                    text = snapshot.throttlingRecommendation,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch Controls: Battery Saver & Adaptive Thermal Guard
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Battery Saver Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (batterySaverEnabled) AmberAccent else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Battery Saver Mode",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF1F5F9)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (snapshot.isBatterySaverEngaged) AmberAccent.copy(alpha = 0.2f)
                                            else Color(0xFF334155)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (snapshot.isBatterySaverEngaged) "ACTIVE" else "STANDBY",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (snapshot.isBatterySaverEngaged) AmberAccent else Color(0xFF94A3B8)
                                    )
                                }
                            }
                            Text(
                                text = "Auto-reduces training intensity (+40ms yield) & slows polling (1s → 3s) on low battery (≤20%) or high thermal load",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                    Switch(
                        checked = batterySaverEnabled,
                        onCheckedChange = onToggleBatterySaver,
                        modifier = Modifier.testTag("battery_saver_switch"),
                        colors = SwitchDefaults.colors(checkedThumbColor = AmberAccent)
                    )
                }

                // Adaptive Thermal Guard Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = if (autoThrottleEnabled) EmeraldSuccess else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Adaptive Thermal Guard",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF1F5F9)
                            )
                            Text(
                                text = "Auto-paces backpropagation epochs when SoC temperature escalates",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                    Switch(
                        checked = autoThrottleEnabled,
                        onCheckedChange = onToggleAutoThrottle,
                        modifier = Modifier.testTag("adaptive_thermal_guard_switch"),
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSuccess)
                    )
                }

                // Safety Simulation Mode Toggles
                if (onToggleSimulation != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DeveloperMode,
                                contentDescription = null,
                                tint = if (snapshot.isSimulationMode) RoseAccent else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Simulate Severe Thermal Load",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF1F5F9)
                                    )
                                    if (snapshot.isSimulationMode) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(RoseAccent.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("SIM ACTIVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = RoseAccent)
                                        }
                                    }
                                }
                                Text(
                                    text = "Simulates severe thermal load to verify cooldown pacing and recovery",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Switch(
                            checked = snapshot.isSimulationMode,
                            onCheckedChange = { onToggleSimulation(it) },
                            modifier = Modifier.testTag("simulation_mode_toggle"),
                            colors = SwitchDefaults.colors(checkedThumbColor = RoseAccent)
                        )
                    }
                }

                // Simulation: Low Battery (14%) Toggle
                if (onToggleSimulatedLowBattery != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.BatteryAlert,
                                contentDescription = null,
                                tint = if (snapshot.isSimulatedLowBattery) RoseAccent else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Simulate Low Battery (14%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF1F5F9)
                                    )
                                    if (snapshot.isSimulatedLowBattery) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(RoseAccent.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("14% SIM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = RoseAccent)
                                        }
                                    }
                                }
                                Text(
                                    text = "Simulates low battery state to test Battery Saver throttle & down-polling",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Switch(
                            checked = snapshot.isSimulatedLowBattery,
                            onCheckedChange = { onToggleSimulatedLowBattery(it) },
                            modifier = Modifier.testTag("simulate_low_battery_toggle"),
                            colors = SwitchDefaults.colors(checkedThumbColor = RoseAccent)
                        )
                    }
                }
            }
        }
    }
}
