package com.example.dle_prototype.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlin.math.sin

enum class RocketFlightState(val label: String, val isAscending: Boolean) {
    LIFT_OFF("LIFT OFF 🚀", true),
    ASCENDING("ASCENT ⚡", true),
    CRUISING("CRUISING 🌌", true),
    RETREATING("FALLING ⚠️", false),
    STANDBY("LAUNCHPAD 🌍", true)
}

@Composable
fun RocketProgressDiagram(
    altitudeFraction: Float, // 0.0 (ground) to 1.0 (deep orbit)
    isProgressing: Boolean,  // true if last answer was correct/streak increasing, false if retreated
    streak: Int,
    score: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier
) {
    var showTelemetryDialog by remember { mutableStateOf(false) }

    // Smooth altitude animation
    val animatedAltitude by animateFloatAsState(
        targetValue = altitudeFraction.coerceIn(0.05f, 0.95f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "rocketAltitude"
    )

    // Thruster flame pulse
    val infiniteTransition = rememberInfiniteTransition(label = "rocketFlame")
    val flamePulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flamePulse"
    )

    val flightState = when {
        altitudeFraction <= 0.12f -> RocketFlightState.STANDBY
        isProgressing && streak >= 3 -> RocketFlightState.LIFT_OFF
        isProgressing -> RocketFlightState.ASCENDING
        else -> RocketFlightState.RETREATING
    }

    val stateColor = if (flightState.isAscending) EmeraldSuccess else RoseAccent
    val altitudeKm = (animatedAltitude * 250f).toInt()

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { showTelemetryDialog = true }
            .testTag("rocket_progress_gauge"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF070B14),
        border = BorderStroke(
            1.5.dp,
            if (isProgressing) EmeraldSuccess.copy(alpha = 0.5f) else RoseAccent.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Telemetry Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = stateColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, stateColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isProgressing) "LIFT OFF" else "FALLING",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = stateColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${altitudeKm}km",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE2E8F0)
                )

                Text(
                    text = if (isProgressing) "▲ UP" else "▼ DOWN",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = stateColor
                )
            }

            // Central Vertical Rocket Trajectory Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f

                    // Atmospheric Gradient Background Line
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F172A), // Orbit
                                Color(0xFF1E1B4B), // Mesosphere
                                Color(0xFF172554), // Stratosphere
                                Color(0xFF0C4A6E), // Troposphere
                                Color(0xFF064E3B)  // Launchpad Earth
                            )
                        ),
                        topLeft = Offset(cx - 3.dp.toPx(), 0f),
                        size = Size(6.dp.toPx(), h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )

                    // Stage markers along track
                    val levels = listOf(0.12f to "PAD", 0.35f to "TROP", 0.60f to "STRAT", 0.85f to "ORBIT")
                    levels.forEach { (frac, _) ->
                        val markY = h * (1f - frac)
                        drawLine(
                            color = Color(0xFF334155),
                            start = Offset(cx - 10.dp.toPx(), markY),
                            end = Offset(cx + 10.dp.toPx(), markY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Rocket Y position (0 at top, h at bottom)
                    // altitude 0 -> bottom (h - pad)
                    // altitude 1 -> top
                    val rocketY = h * (1f - animatedAltitude)

                    // Draw Rocket Vehicle
                    val rocketLength = 28.dp.toPx()
                    val rocketRadius = 7.dp.toPx()

                    val rotationAngle = if (isProgressing) 0f else 180f

                    rotate(degrees = rotationAngle, pivot = Offset(cx, rocketY)) {
                        // Thruster Fire Trail (when ascending)
                        if (flightState.isAscending) {
                            val flameLength = (16.dp.toPx() * flamePulse)
                            val flamePath = Path().apply {
                                moveTo(cx - 4.dp.toPx(), rocketY + rocketLength / 2f)
                                lineTo(cx + 4.dp.toPx(), rocketY + rocketLength / 2f)
                                lineTo(cx, rocketY + rocketLength / 2f + flameLength)
                                close()
                            }
                            drawPath(
                                path = flamePath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(AmberAccent, Color(0xFFEF4444), Color.Transparent),
                                    startY = rocketY + rocketLength / 2f,
                                    endY = rocketY + rocketLength / 2f + flameLength
                                )
                            )
                        } else {
                            // Falling smoke / retro sparks
                            drawCircle(
                                color = RoseAccent.copy(alpha = 0.5f),
                                radius = 6.dp.toPx() * flamePulse,
                                center = Offset(cx, rocketY + rocketLength / 2f + 4.dp.toPx())
                            )
                        }

                        // Rocket Body (Capsule)
                        val bodyPath = Path().apply {
                            // Nose cone tip
                            moveTo(cx, rocketY - rocketLength / 2f)
                            // Right shoulder
                            lineTo(cx + rocketRadius, rocketY - rocketLength / 6f)
                            // Right base
                            lineTo(cx + rocketRadius, rocketY + rocketLength / 2f)
                            // Thruster bell
                            lineTo(cx - rocketRadius, rocketY + rocketLength / 2f)
                            // Left shoulder
                            lineTo(cx - rocketRadius, rocketY - rocketLength / 6f)
                            close()
                        }

                        drawPath(
                            path = bodyPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White, Color(0xFFCBD5E1), Color(0xFF64748B)),
                                startY = rocketY - rocketLength / 2f,
                                endY = rocketY + rocketLength / 2f
                            )
                        )

                        // Fin Wings
                        val leftFin = Path().apply {
                            moveTo(cx - rocketRadius, rocketY + rocketLength / 6f)
                            lineTo(cx - rocketRadius - 5.dp.toPx(), rocketY + rocketLength / 2f)
                            lineTo(cx - rocketRadius, rocketY + rocketLength / 2f)
                            close()
                        }
                        val rightFin = Path().apply {
                            moveTo(cx + rocketRadius, rocketY + rocketLength / 6f)
                            lineTo(cx + rocketRadius + 5.dp.toPx(), rocketY + rocketLength / 2f)
                            lineTo(cx + rocketRadius, rocketY + rocketLength / 2f)
                            close()
                        }
                        drawPath(leftFin, color = if (isProgressing) CyanAccent else RoseAccent)
                        drawPath(rightFin, color = if (isProgressing) CyanAccent else RoseAccent)

                        // Porthole Window
                        drawCircle(
                            color = CyanAccent,
                            radius = 3.dp.toPx(),
                            center = Offset(cx, rocketY - rocketLength / 8f)
                        )
                    }
                }
            }

            // Bottom Streak & Atmosphere Indicator
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (streak > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AmberAccent.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${streak}x 🔥",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberAccent,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else {
                    Text(
                        text = "PAD",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Flight Telemetry Info",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }

    // Interactive Rocket Flight Telemetry Dialog
    if (showTelemetryDialog) {
        AlertDialog(
            onDismissRequest = { showTelemetryDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(stateColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = stateColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Rocket Ascent Telemetry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isProgressing) "Status: LIFT OFF & ASCENDING 🚀" else "Status: RETREAT & DESCENT ⚠️",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = stateColor
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "TRAJECTORY DYNAMICS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CyanAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Every correct answer ignites booster thrust, propelling the rocket into higher atmospheric tiers (Troposphere → Stratosphere → Mesosphere → Orbit). Consecutive correct streaks amplify lift off velocity!",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "RETREAT & RECOVERY FALL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = RoseAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Incorrect answers cause atmospheric drag and descent towards the launchpad. The DDA engine lowers difficulty to help you stabilize flight and regain thrust.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("ALTITUDE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                                Text("${altitudeKm} km (${(animatedAltitude * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("CURRENT STREAK", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                                Text("${streak} In a Row 🔥", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTelemetryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = stateColor)
                ) {
                    Text("Return to Mission", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
