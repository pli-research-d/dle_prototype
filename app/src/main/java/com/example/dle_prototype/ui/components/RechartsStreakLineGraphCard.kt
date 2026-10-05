package com.example.dle_prototype.ui.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class StreakDayActivity(
    val day: String,
    val streak: Int,
    val xp: Int,
    val questions: Int,
    val minutes: Int,
    val goalMet: Boolean,
    val isToday: Boolean
)

/**
 * Recharts-powered interactive line graph component visualizing the user's weekly
 * learning streak and activity trends.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsStreakLineGraphCard(
    dailyStreak: Int,
    longestStreak: Int,
    recentAttempts: List<QuizAttempt> = emptyList(),
    weeklyXp: Int = 380,
    modifier: Modifier = Modifier
) {
    var isRechartsMode by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Build the 7-day weekly learning streak activity data
    val weeklyPoints = remember(dailyStreak, recentAttempts, weeklyXp) {
        buildWeeklyStreakData(dailyStreak, recentAttempts, weeklyXp)
    }

    val streakJson = remember(weeklyPoints) {
        val array = JSONArray()
        for (pt in weeklyPoints) {
            val obj = JSONObject()
            obj.put("day", pt.day)
            obj.put("streak", pt.streak)
            obj.put("xp", pt.xp)
            obj.put("questions", pt.questions)
            obj.put("minutes", pt.minutes)
            obj.put("goalMet", pt.goalMet)
            obj.put("isToday", pt.isToday)
            array.put(obj)
        }
        array.toString()
    }

    LaunchedEffect(streakJson, webViewRef) {
        webViewRef?.let { wv ->
            wv.evaluateJavascript("if (window.updateStreakData) { window.updateStreakData($streakJson); }", null)
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_weekly_streak_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Title + Recharts Badge + Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AmberAccent.copy(alpha = 0.18f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Weekly Streak Line Graph",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CyanAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Recharts",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Visualizing daily consistency & XP momentum",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Toggle Recharts vs Native Canvas
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = isRechartsMode,
                        onClick = { isRechartsMode = true },
                        label = { Text("Recharts", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberAccent,
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isRechartsMode,
                            borderColor = Color(0xFF334155),
                            selectedBorderColor = AmberAccent
                        ),
                        modifier = Modifier.testTag("btn_toggle_recharts_mode")
                    )
                    FilterChip(
                        selected = !isRechartsMode,
                        onClick = { isRechartsMode = false },
                        label = { Text("Canvas", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberAccent,
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = !isRechartsMode,
                            borderColor = Color(0xFF334155),
                            selectedBorderColor = AmberAccent
                        )
                    )
                }
            }

            // Streak Metric Highlighting Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$dailyStreak Days",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = AmberAccent
                        )
                        Text(
                            text = "Current Streak",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${maxOf(longestStreak, dailyStreak)} Days",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "Personal Best",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$weeklyXp XP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanAccent
                        )
                        Text(
                            text = "7-Day Total",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Chart Render Section
            if (isRechartsMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF070B12))
                        .testTag("recharts_streak_webview_container")
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = true
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        view?.evaluateJavascript(
                                            "if (window.updateStreakData) { window.updateStreakData($streakJson); }",
                                            null
                                        )
                                    }
                                }
                                loadUrl("file:///android_asset/recharts_weekly_streak.html")
                                webViewRef = this
                            }
                        },
                        update = { wv ->
                            wv.evaluateJavascript(
                                "if (window.updateStreakData) { window.updateStreakData($streakJson); }",
                                null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )
                }
            } else {
                // Native Jetpack Compose Canvas fallback line graph
                NativeWeeklyStreakCanvas(
                    points = weeklyPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }
        }
    }
}

/**
 * Builds realistic 7-day trailing data for the user's weekly streak and learning activity.
 */
private fun buildWeeklyStreakData(
    currentStreak: Int,
    attempts: List<QuizAttempt>,
    weeklyXp: Int
): List<StreakDayActivity> {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val cal = Calendar.getInstance()
    val todayDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 0=Mon ... 6=Sun

    val startStreak = (currentStreak - todayDayOfWeek).coerceAtLeast(1)

    return (0..6).map { idx ->
        val label = dayLabels[idx]
        val isToday = idx == todayDayOfWeek
        val isPastOrToday = idx <= todayDayOfWeek

        val dayStreak = if (isPastOrToday) {
            (startStreak + idx).coerceAtMost(maxOf(1, currentStreak))
        } else {
            currentStreak
        }

        // Calculate day's XP and questions
        val dayAttempts = attempts.filter { a ->
            val aCal = Calendar.getInstance().apply { timeInMillis = a.timestamp }
            val aDay = (aCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
            aDay == idx
        }

        val dayQuestions = if (dayAttempts.isNotEmpty()) {
            dayAttempts.sumOf { it.totalQuestions }
        } else if (isPastOrToday) {
            when (idx) {
                0 -> 8
                1 -> 12
                2 -> 10
                3 -> 15
                4 -> 14
                5 -> 6
                else -> 9
            }
        } else 0

        val dayXp = if (dayAttempts.isNotEmpty()) {
            dayAttempts.sumOf { it.score * 10 }
        } else if (isPastOrToday) {
            dayQuestions * 8 + (idx * 5)
        } else 0

        val goalMet = isPastOrToday && dayQuestions >= 8

        StreakDayActivity(
            day = label,
            streak = dayStreak,
            xp = dayXp,
            questions = dayQuestions,
            minutes = (dayQuestions * 2.2).toInt().coerceAtLeast(10),
            goalMet = goalMet,
            isToday = isToday
        )
    }
}

/**
 * Native Jetpack Compose Canvas dual-line graph fallback.
 */
@Composable
private fun NativeWeeklyStreakCanvas(
    points: List<StreakDayActivity>,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF070B12),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Native Canvas Dual Graph", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔥 Streak", fontSize = 10.sp, color = AmberAccent, fontWeight = FontWeight.Bold)
                    Text("⚡ XP", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                if (points.size < 2) return@Canvas

                val paddingLeft = 30f
                val paddingBottom = 40f
                val chartW = size.width - paddingLeft - 20f
                val chartH = size.height - paddingBottom - 10f

                val maxStreak = points.maxOf { it.streak }.coerceAtLeast(1)
                val maxXP = points.maxOf { it.xp }.coerceAtLeast(50)

                // Grid lines
                for (r in 0..3) {
                    val y = 10f + (chartH / 3f) * r
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(paddingLeft, y),
                        end = Offset(size.width - 20f, y),
                        strokeWidth = 1f
                    )
                }

                // XP Area & Line (Cyan)
                val xpPath = Path()
                val xpAreaPath = Path()
                points.forEachIndexed { i, pt ->
                    val x = paddingLeft + (i.toFloat() / (points.size - 1)) * chartW
                    val y = 10f + chartH - (pt.xp.toFloat() / maxXP.toFloat()) * chartH
                    if (i == 0) {
                        xpPath.moveTo(x, y)
                        xpAreaPath.moveTo(x, 10f + chartH)
                        xpAreaPath.lineTo(x, y)
                    } else {
                        xpPath.lineTo(x, y)
                        xpAreaPath.lineTo(x, y)
                    }
                    if (i == points.size - 1) {
                        xpAreaPath.lineTo(x, 10f + chartH)
                        xpAreaPath.close()
                    }
                }

                drawPath(
                    path = xpAreaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(CyanAccent.copy(alpha = 0.25f), Color.Transparent),
                        startY = 10f,
                        endY = 10f + chartH
                    )
                )
                drawPath(path = xpPath, color = CyanAccent, style = Stroke(width = 3f))

                // Streak Line (Amber)
                val streakPath = Path()
                val streakCoords = mutableListOf<Offset>()
                points.forEachIndexed { i, pt ->
                    val x = paddingLeft + (i.toFloat() / (points.size - 1)) * chartW
                    val y = 10f + chartH - (pt.streak.toFloat() / maxStreak.toFloat()) * chartH
                    streakCoords.add(Offset(x, y))
                    if (i == 0) streakPath.moveTo(x, y) else streakPath.lineTo(x, y)
                }

                drawPath(path = streakPath, color = AmberAccent, style = Stroke(width = 4f))

                // Draw dots on streak
                streakCoords.forEach { pt ->
                    drawCircle(color = Color(0xFF070B12), radius = 6f, center = pt)
                    drawCircle(color = AmberAccent, radius = 4f, center = pt)
                }
            }

            // Labels row
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { pt ->
                    Text(
                        text = pt.day,
                        fontSize = 10.sp,
                        fontWeight = if (pt.isToday) FontWeight.Black else FontWeight.Bold,
                        color = if (pt.isToday) AmberAccent else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
