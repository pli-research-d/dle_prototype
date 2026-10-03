package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Confetti particle data structure with dynamic flight physics:
 * - Air drag, gravity acceleration, flutter rotation, and color variation.
 */
data class ConfettiParticle(
    val id: Int,
    val initialX: Float, // 0f to 1f normalized across track
    val initialY: Float, // 0f to 1f normalized across track
    val vx: Float,       // horizontal drift velocity
    val vy: Float,       // vertical ejection velocity
    val color: Color,
    val sizePx: Float,
    val rotationOffset: Float,
    val rotationSpeed: Float,
    val wobbleSpeed: Float,
    val shapeType: Int   // 0 = classic ribbon, 1 = star/diamond, 2 = circle, 3 = streamer
)

/**
 * Custom Compose component that visualizes user progress in the quiz:
 * - Animates a rocket moving UP when the score increases (Lift-Off with thruster fire).
 * - Animates the rocket DESCENDING if the user gets an answer wrong (Atmospheric retreat/fall).
 * - Triggers a dynamic celebratory confetti animation when the user hits milestones or achieves their daily goal!
 */
@Composable
fun RocketProgressView(
    score: Int,
    totalQuestions: Int,
    streak: Int = 0,
    isAscending: Boolean = true,
    lastAnswerCorrect: Boolean? = null,
    isDailyGoalAchieved: Boolean = false,
    milestoneAchieved: Boolean = false,
    triggerConfetti: Boolean = false,
    onMilestoneCelebrated: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showTelemetryDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Normalized altitude calculation (0.0 = Ground Launchpad, 1.0 = Deep Orbit)
    val maxTargetScore = totalQuestions.coerceAtLeast(1)
    val baseFraction = (score.toFloat() / maxTargetScore.toFloat()).coerceIn(0f, 1f)
    val streakBonus = (streak * 0.04f).coerceAtMost(0.16f)
    val targetAltitude = (0.12f + baseFraction * 0.76f + streakBonus).coerceIn(0.10f, 0.95f)

    // Smooth physics-based altitude transition
    val animatedAltitude by animateFloatAsState(
        targetValue = targetAltitude,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "rocketAltitudeAnim"
    )

    // Infinite engine thruster flame pulsation
    val infiniteTransition = rememberInfiniteTransition(label = "rocketThrusterPulse")
    val flamePulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flamePulseAnim"
    )

    // Star twinkle effect in upper space
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinkleAnim"
    )

    val isProgressing = lastAnswerCorrect ?: isAscending
    val statusColor = if (isProgressing) EmeraldSuccess else RoseAccent
    val altitudeKm = (animatedAltitude * 280f).toInt()

    // ----------------------------------------------------
    // Confetti Animation Engine
    // ----------------------------------------------------
    val confettiParticles = remember { mutableStateListOf<ConfettiParticle>() }
    val confettiAnim = remember { Animatable(0f) }
    var isConfettiActive by remember { mutableStateOf(false) }
    var celebrationTitle by remember { mutableStateOf("") }
    var celebrationSubtitle by remember { mutableStateOf("") }

    // Milestone history tracking to detect fresh triggers
    var prevScore by remember { mutableIntStateOf(score) }
    var prevStreak by remember { mutableIntStateOf(streak) }
    var prevGoalAchieved by remember { mutableStateOf(isDailyGoalAchieved) }
    var prevMilestoneAchieved by remember { mutableStateOf(milestoneAchieved) }

    fun launchConfetti(title: String, subtitle: String) {
        celebrationTitle = title
        celebrationSubtitle = subtitle
        confettiParticles.clear()

        val confettiColors = listOf(
            CyanAccent,
            AmberAccent,
            EmeraldSuccess,
            Color(0xFFE879F9), // Neon Magenta
            Color(0xFF38BDF8), // Electric Sky
            Color(0xFFFACC15), // Gold
            Color(0xFFFF6B6B), // Coral
            Color.White
        )

        val rng = Random(System.currentTimeMillis())
        for (i in 0 until 55) {
            confettiParticles.add(
                ConfettiParticle(
                    id = i,
                    initialX = rng.nextFloat(), // spawn across track width
                    initialY = rng.nextFloat() * 0.35f + (1f - targetAltitude) * 0.5f,
                    vx = (rng.nextFloat() - 0.5f) * 1.6f,
                    vy = rng.nextFloat() * 1.5f + 0.8f,
                    color = confettiColors[rng.nextInt(confettiColors.size)],
                    sizePx = rng.nextFloat() * 7f + 5f,
                    rotationOffset = rng.nextFloat() * 360f,
                    rotationSpeed = (rng.nextFloat() - 0.5f) * 720f,
                    wobbleSpeed = rng.nextFloat() * 6f + 3f,
                    shapeType = rng.nextInt(4)
                )
            )
        }

        coroutineScope.launch {
            isConfettiActive = true
            confettiAnim.snapTo(0f)
            confettiAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2600, easing = LinearEasing)
            )
            isConfettiActive = false
            onMilestoneCelebrated?.invoke()
        }
    }

    // Detect milestones automatically
    LaunchedEffect(score, streak, isDailyGoalAchieved, milestoneAchieved, triggerConfetti) {
        if (triggerConfetti) {
            launchConfetti("🎉 MISSION SUCCESS!", "Telemetry Confetti Triggered")
        } else if (isDailyGoalAchieved && !prevGoalAchieved) {
            launchConfetti("🎯 GOAL ACHIEVED!", "Daily Target Reached!")
        } else if (milestoneAchieved && !prevMilestoneAchieved) {
            launchConfetti("🚀 NEW MILESTONE!", "Milestone Achieved!")
        } else if (score >= totalQuestions && totalQuestions > 0 && prevScore < totalQuestions) {
            launchConfetti("👑 ORBIT COMPLETE!", "100% Mastery Unlocked!")
        } else if (score == (totalQuestions / 2) && score > 0 && prevScore < score) {
            launchConfetti("⭐ 50% CHECKPOINT!", "Halfway into Deep Orbit!")
        } else if (streak >= 3 && streak > prevStreak && (streak == 3 || streak == 5 || streak == 10)) {
            launchConfetti("🔥 ${streak}X STREAK!", "Velocity Combustion!")
        }
        prevScore = score
        prevStreak = streak
        prevGoalAchieved = isDailyGoalAchieved
        prevMilestoneAchieved = milestoneAchieved
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { showTelemetryDialog = true }
            .testTag("rocket_progress_view"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF070B14),
        border = BorderStroke(
            1.5.dp,
            if (isConfettiActive) AmberAccent else if (isProgressing) EmeraldSuccess.copy(alpha = 0.5f) else RoseAccent.copy(alpha = 0.5f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                        color = if (isConfettiActive) AmberAccent.copy(alpha = 0.25f) else statusColor.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, if (isConfettiActive) AmberAccent else statusColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (isConfettiActive) "CELEBRATION" else if (isProgressing) "LIFT OFF" else "FALLING",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isConfettiActive) AmberAccent else statusColor,
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
                        text = if (isProgressing) "▲ ASCENT" else "▼ RETREAT",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                // Central Vertical Rocket Track Canvas with Confetti Overlay
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

                        // Atmospheric Gradient Rail (Space -> Mesosphere -> Stratosphere -> Troposphere -> Launchpad)
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A), // Orbit / Deep Space
                                    Color(0xFF1E1B4B), // Mesosphere
                                    Color(0xFF172554), // Stratosphere
                                    Color(0xFF0C4A6E), // Troposphere
                                    Color(0xFF064E3B)  // Launchpad Base
                                )
                            ),
                            topLeft = Offset(cx - 3.dp.toPx(), 0f),
                            size = Size(6.dp.toPx(), h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )

                        // Atmospheric Stage Level Ticks
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

                        // Twinkling Stars in high altitude
                        drawCircle(Color.White.copy(alpha = starTwinkle * 0.8f), 1.5.dp.toPx(), Offset(cx - 12.dp.toPx(), h * 0.10f))
                        drawCircle(CyanAccent.copy(alpha = starTwinkle * 0.7f), 1.2.dp.toPx(), Offset(cx + 14.dp.toPx(), h * 0.18f))
                        drawCircle(Color.White.copy(alpha = (1f - starTwinkle) * 0.7f), 1.5.dp.toPx(), Offset(cx - 14.dp.toPx(), h * 0.28f))

                        // Dynamic Rocket Y position (0 at top, h at bottom)
                        val rocketY = h * (1f - animatedAltitude)

                        val rocketLength = 28.dp.toPx()
                        val rocketRadius = 7.dp.toPx()
                        val rotationAngle = if (isProgressing) 0f else 180f

                        rotate(degrees = rotationAngle, pivot = Offset(cx, rocketY)) {
                            // Thruster Flame & Trail (Ascent)
                            if (isProgressing) {
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
                                        colors = listOf(AmberAccent, RoseAccent, Color.Transparent),
                                        startY = rocketY + rocketLength / 2f,
                                        endY = rocketY + rocketLength / 2f + flameLength
                                    )
                                )
                            } else {
                                // Red smoke puff / retreat wake
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(RoseAccent.copy(alpha = 0.7f), Color.Transparent),
                                        center = Offset(cx, rocketY - rocketLength / 2f),
                                        radius = 12.dp.toPx()
                                    ),
                                    radius = 12.dp.toPx(),
                                    center = Offset(cx, rocketY - rocketLength / 2f)
                                )
                            }

                            // Aerodynamic Rocket Capsule Body
                            val rocketBody = Path().apply {
                                moveTo(cx, rocketY - rocketLength / 2f)
                                lineTo(cx + rocketRadius, rocketY)
                                lineTo(cx + rocketRadius, rocketY + rocketLength / 2f)
                                lineTo(cx - rocketRadius, rocketY + rocketLength / 2f)
                                lineTo(cx - rocketRadius, rocketY)
                                close()
                            }
                            drawPath(
                                path = rocketBody,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, Color(0xFFE2E8F0), Color(0xFF94A3B8)),
                                    startY = rocketY - rocketLength / 2f,
                                    endY = rocketY + rocketLength / 2f
                                )
                            )

                            // Rocket Nose Cone (Titanium Tip)
                            val noseCone = Path().apply {
                                moveTo(cx, rocketY - rocketLength / 2f)
                                lineTo(cx + rocketRadius * 0.7f, rocketY - rocketLength / 4f)
                                lineTo(cx - rocketRadius * 0.7f, rocketY - rocketLength / 4f)
                                close()
                            }
                            drawPath(
                                path = noseCone,
                                color = if (isProgressing) CyanAccent else RoseAccent
                            )

                            // Cockpit Porthole Window
                            drawCircle(
                                color = if (isProgressing) CyanAccent else Color(0xFF1E293B),
                                radius = 2.5.dp.toPx(),
                                center = Offset(cx, rocketY - 2.dp.toPx())
                            )

                            // Aerodynamic Stabilizer Fins
                            val leftFin = Path().apply {
                                moveTo(cx - rocketRadius, rocketY + rocketLength / 4f)
                                lineTo(cx - rocketRadius - 4.dp.toPx(), rocketY + rocketLength / 2f + 2.dp.toPx())
                                lineTo(cx - rocketRadius, rocketY + rocketLength / 2f)
                                close()
                            }
                            val rightFin = Path().apply {
                                moveTo(cx + rocketRadius, rocketY + rocketLength / 4f)
                                lineTo(cx + rocketRadius + 4.dp.toPx(), rocketY + rocketLength / 2f + 2.dp.toPx())
                                lineTo(cx + rocketRadius, rocketY + rocketLength / 2f)
                                close()
                            }
                            drawPath(leftFin, color = if (isProgressing) EmeraldSuccess else RoseAccent)
                            drawPath(rightFin, color = if (isProgressing) EmeraldSuccess else RoseAccent)
                        }

                        // ----------------------------------------------------
                        // Dynamic Confetti Particle Rendering
                        // ----------------------------------------------------
                        if (isConfettiActive && confettiAnim.value > 0f) {
                            val progress = confettiAnim.value
                            val alpha = (1f - progress * 0.85f).coerceIn(0f, 1f)

                            confettiParticles.forEach { p ->
                                val currentX = (p.initialX * w) + (p.vx * progress * w * 0.6f) + (sin(progress * p.wobbleSpeed) * 8.dp.toPx())
                                val currentY = (p.initialY * h) + (p.vy * progress * h * 0.85f) + (progress * progress * 40.dp.toPx()) // gravity acceleration
                                val rotation = p.rotationOffset + p.rotationSpeed * progress

                                rotate(degrees = rotation, pivot = Offset(currentX, currentY)) {
                                    val particleColor = p.color.copy(alpha = alpha)
                                    when (p.shapeType) {
                                        0 -> {
                                            // Classic rectangle flake
                                            drawRect(
                                                color = particleColor,
                                                topLeft = Offset(currentX - p.sizePx / 2f, currentY - p.sizePx / 4f),
                                                size = Size(p.sizePx, p.sizePx / 2f)
                                            )
                                        }
                                        1 -> {
                                            // Diamond / Star flake
                                            val diamond = Path().apply {
                                                moveTo(currentX, currentY - p.sizePx / 2f)
                                                lineTo(currentX + p.sizePx / 2f, currentY)
                                                lineTo(currentX, currentY + p.sizePx / 2f)
                                                lineTo(currentX - p.sizePx / 2f, currentY)
                                                close()
                                            }
                                            drawPath(diamond, particleColor)
                                        }
                                        2 -> {
                                            // Circular glitter particle
                                            drawCircle(
                                                color = particleColor,
                                                radius = p.sizePx / 3f,
                                                center = Offset(currentX, currentY)
                                            )
                                        }
                                        3 -> {
                                            // Streamer ribbon line
                                            drawLine(
                                                color = particleColor,
                                                start = Offset(currentX - p.sizePx / 2f, currentY - p.sizePx / 2f),
                                                end = Offset(currentX + p.sizePx / 2f, currentY + p.sizePx / 2f),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Streak / Launchpad Indicator
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

            // Milestone Banner Overlay
            AnimatedVisibility(
                visible = isConfettiActive && celebrationTitle.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xEE0B132B),
                    border = BorderStroke(1.dp, AmberAccent),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = celebrationTitle,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = AmberAccent,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }

    // Interactive Flight Telemetry Dialog on tap
    if (showTelemetryDialog) {
        AlertDialog(
            onDismissRequest = { showTelemetryDialog = false },
            modifier = Modifier.testTag("rocket_telemetry_dialog"),
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
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = statusColor,
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
                            color = statusColor
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
                                text = "LIFT-OFF PROGRESSION DYNAMICS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CyanAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Each correct answer fires booster thrust, ascending the rocket through Earth's atmospheric tiers toward orbit. Sustained streaks and reaching daily goals trigger confetti celebrations!",
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
                                text = "Incorrect answers trigger atmospheric re-entry drag, descending altitude. Adaptive difficulty adjusts question complexity to help you rebuild momentum.",
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
                                Text("SCORE / TARGET", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                                Text("$score / $totalQuestions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }
                        }
                    }

                    // Interactive Celebration Trigger Button
                    OutlinedButton(
                        onClick = {
                            showTelemetryDialog = false
                            launchConfetti("🎉 MANUAL CONFETTI!", "Rocket Celebrations Active")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_confetti_button"),
                        border = BorderStroke(1.dp, AmberAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trigger Confetti Animation", color = AmberAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTelemetryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor),
                    modifier = Modifier.testTag("close_rocket_telemetry_button")
                ) {
                    Text("Return to Mission", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
