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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.graph.KnowledgeGraphEngine
import com.example.dle_prototype.data.graph.KnowledgeMasteryLevel
import com.example.dle_prototype.data.graph.MasteryTrendDayPoint
import com.example.dle_prototype.data.graph.MasteryTrends30DaySummary
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import org.json.JSONArray
import org.json.JSONObject

/**
 * Recharts-powered interactive data visualization card showcasing the user's
 * progress and mastery improvement trends over the last 30 days.
 *
 * Includes dual-rendering mode:
 * 1. Primary: Rich web-standard Recharts area/line/bar charts via WebView
 * 2. Fallback / Alternative: High-performance Native Jetpack Compose Canvas
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Recharts30DayMasteryTrendCard(
    attempts: List<QuizAttempt> = emptyList(),
    currentMasteryPercent: Float = 75f,
    expertTopicsCount: Int = 3,
    intermediateTopicsCount: Int = 4,
    beginnerTopicsCount: Int = 2,
    modifier: Modifier = Modifier
) {
    var isRechartsMode by remember { mutableStateOf(true) }
    var selectedDaysWindow by remember { mutableIntStateOf(30) } // 30, 14, 7
    var selectedViewMode by remember { mutableStateOf("mastery") } // "mastery", "tiers", "accuracy"
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Build 30-day mastery trends from engine
    val summary = remember(attempts, currentMasteryPercent, expertTopicsCount, intermediateTopicsCount, beginnerTopicsCount) {
        KnowledgeGraphEngine.build30DayMasteryTrends(
            attempts = attempts,
            currentMasteryPercent = currentMasteryPercent,
            currentExpertTopics = expertTopicsCount,
            currentIntermediateTopics = intermediateTopicsCount,
            currentBeginnerTopics = beginnerTopicsCount
        )
    }

    val displayPoints = remember(summary.points, selectedDaysWindow) {
        summary.points.takeLast(selectedDaysWindow)
    }

    // Convert points to JSON array for Recharts bridge
    val trendJson = remember(summary.points) {
        val array = JSONArray()
        for (pt in summary.points) {
            val obj = JSONObject()
            obj.put("dayNumber", pt.dayNumber)
            obj.put("dateLabel", pt.dateLabel)
            obj.put("masteryScore", pt.masteryScore)
            obj.put("accuracyPercent", pt.accuracyPercent)
            obj.put("expertTopicsCount", pt.expertTopicsCount)
            obj.put("intermediateTopicsCount", pt.intermediateTopicsCount)
            obj.put("beginnerTopicsCount", pt.beginnerTopicsCount)
            obj.put("xpEarned", pt.xpEarned)
            obj.put("quizzesTaken", pt.quizzesTaken)
            obj.put("focusMinutes", pt.focusMinutes)
            if (pt.highlightMilestone != null) {
                obj.put("highlightMilestone", pt.highlightMilestone)
            }
            obj.put("isToday", pt.isToday)
            array.put(obj)
        }
        array.toString()
    }

    // Update WebView on state change
    LaunchedEffect(trendJson, webViewRef) {
        webViewRef?.evaluateJavascript(
            "if (window.updateMasteryTrendsData) { window.updateMasteryTrendsData($trendJson); }",
            null
        )
    }

    LaunchedEffect(selectedViewMode, webViewRef) {
        webViewRef?.evaluateJavascript(
            "if (window.setMetricView) { window.setMetricView('$selectedViewMode'); }",
            null
        )
    }

    LaunchedEffect(selectedDaysWindow, webViewRef) {
        webViewRef?.evaluateJavascript(
            "if (window.setTimeWindow) { window.setTimeWindow($selectedDaysWindow); }",
            null
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_30day_mastery_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title + Recharts Badge + Mode Toggle (Recharts vs Canvas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoGraph,
                                contentDescription = null,
                                tint = CyanAccent,
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
                                text = "30-Day Mastery Trends",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
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
                            text = "Tracking mastery acceleration & topic level growth",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Mode Toggle (Recharts / Canvas)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = isRechartsMode,
                        onClick = { isRechartsMode = true },
                        label = { Text("Recharts", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isRechartsMode,
                            borderColor = Color(0xFF334155),
                            selectedBorderColor = CyanAccent
                        ),
                        modifier = Modifier.testTag("btn_toggle_recharts_mode")
                    )
                    FilterChip(
                        selected = !isRechartsMode,
                        onClick = { isRechartsMode = false },
                        label = { Text("Canvas", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = !isRechartsMode,
                            borderColor = Color(0xFF334155),
                            selectedBorderColor = CyanAccent
                        ),
                        modifier = Modifier.testTag("btn_toggle_canvas_mode")
                    )
                }
            }

            // Summary Metrics Row: Mastery, Growth, Expert Topics, Total XP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Current Mastery
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_current_mastery"),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${summary.currentMastery.toInt()}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanAccent
                        )
                        Text(
                            text = "Current Mastery",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Metric 2: 30-Day Growth
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_growth_delta"),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "+${summary.growthDelta}% 🚀",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "30-Day Delta",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Metric 3: Expert Topics
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_expert_topics"),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${summary.currentExpertCount} 🥇",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "Expert Tiers",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Metric 4: 30-Day XP
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_total_xp"),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${summary.total30DayXp}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = AmberAccent
                        )
                        Text(
                            text = "Total XP",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Interactive View Filters & Time Window Buttons (Syncs with Recharts & Native Canvas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Mode Selectors
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "mastery" to "Mastery",
                        "tiers" to "Tiers",
                        "accuracy" to "Accuracy"
                    ).forEach { (mode, label) ->
                        val isSel = selectedViewMode == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) CyanAccent.copy(alpha = 0.2f) else Color(0xFF131D31),
                            border = BorderStroke(
                                1.dp,
                                if (isSel) CyanAccent else Color(0xFF1E293B)
                            ),
                            onClick = {
                                selectedViewMode = mode
                                webViewRef?.evaluateJavascript(
                                    "if (window.setMetricView) { window.setMetricView('$mode'); }",
                                    null
                                )
                            },
                            modifier = Modifier.testTag("view_mode_$mode")
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) CyanAccent else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Time Window Selectors
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(30 to "30D", 14 to "14D", 7 to "7D").forEach { (days, label) ->
                        val isSel = selectedDaysWindow == days
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) AmberAccent.copy(alpha = 0.2f) else Color(0xFF131D31),
                            border = BorderStroke(
                                1.dp,
                                if (isSel) AmberAccent else Color(0xFF1E293B)
                            ),
                            onClick = {
                                selectedDaysWindow = days
                                webViewRef?.evaluateJavascript(
                                    "if (window.setTimeWindow) { window.setTimeWindow($days); }",
                                    null
                                )
                            },
                            modifier = Modifier.testTag("time_filter_${days}d")
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) AmberAccent else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Visual Chart Display Section
            if (isRechartsMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF070B14))
                        .testTag("recharts_mastery_webview_container")
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
                                            "if (window.updateMasteryTrendsData) { window.updateMasteryTrendsData($trendJson); }",
                                            null
                                        )
                                        view?.evaluateJavascript(
                                            "if (window.setMetricView) { window.setMetricView('$selectedViewMode'); }",
                                            null
                                        )
                                        view?.evaluateJavascript(
                                            "if (window.setTimeWindow) { window.setTimeWindow($selectedDaysWindow); }",
                                            null
                                        )
                                    }
                                }
                                loadUrl("file:///android_asset/recharts_mastery_trends.html")
                                webViewRef = this
                            }
                        },
                        update = { wv ->
                            wv.evaluateJavascript(
                                "if (window.updateMasteryTrendsData) { window.updateMasteryTrendsData($trendJson); }",
                                null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                    )
                }
            } else {
                // High-fidelity Native Jetpack Compose Canvas implementation
                NativeMasteryTrendsCanvas(
                    points = displayPoints,
                    viewMode = selectedViewMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                )
            }

            // Mastery Milestones Footnote
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF070B14),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Milestones Achieved: 4 Breakthroughs in last 30d",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                    Text(
                        text = "Avg Acc: ${summary.averageAccuracy}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
            }
        }
    }
}

/**
 * High-performance Native Jetpack Compose Canvas fallback line & area chart.
 */
@Composable
private fun NativeMasteryTrendsCanvas(
    points: List<MasteryTrendDayPoint>,
    viewMode: String,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF070B14))
            .padding(12.dp)
            .testTag("canvas_mastery_trends_container")
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(260.dp)) {
            val w = size.width
            val h = size.height

            val padLeft = 36.dp.toPx()
            val padRight = 16.dp.toPx()
            val padTop = 16.dp.toPx()
            val padBottom = 28.dp.toPx()

            val chartW = w - padLeft - padRight
            val chartH = h - padTop - padBottom

            val primaryColor = when (viewMode) {
                "tiers" -> EmeraldSuccess
                "accuracy" -> EmeraldSuccess
                else -> CyanAccent
            }

            // Draw horizontal reference grid lines
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val y = padTop + chartH * (i.toFloat() / gridSteps)
                drawLine(
                    color = Color.White.copy(alpha = 0.06f),
                    start = Offset(padLeft, y),
                    end = Offset(padLeft + chartW, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Reference Lines for 80% (Expert) and 60% (Intermediate) in mastery mode
            if (viewMode == "mastery") {
                val y80 = padTop + chartH * (1f - (80f - 20f) / 80f)
                val y60 = padTop + chartH * (1f - (60f - 20f) / 80f)
                drawLine(
                    color = EmeraldSuccess.copy(alpha = 0.35f),
                    start = Offset(padLeft, y80),
                    end = Offset(padLeft + chartW, y80),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = CyanAccent.copy(alpha = 0.3f),
                    start = Offset(padLeft, y60),
                    end = Offset(padLeft + chartW, y60),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val minVal = if (viewMode == "tiers") 0f else 20f
            val maxVal = if (viewMode == "tiers") 8f else 100f

            fun getX(idx: Int): Float = padLeft + (idx.toFloat() / (points.size - 1).coerceAtLeast(1)) * chartW
            fun getY(value: Float): Float {
                val norm = ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                return padTop + chartH * (1f - norm)
            }

            val linePath = Path()
            val areaPath = Path()

            points.forEachIndexed { i, pt ->
                val v = when (viewMode) {
                    "tiers" -> pt.expertTopicsCount.toFloat()
                    "accuracy" -> pt.accuracyPercent
                    else -> pt.masteryScore
                }
                val x = getX(i)
                val y = getY(v)

                if (i == 0) {
                    linePath.moveTo(x, y)
                    areaPath.moveTo(x, padTop + chartH)
                    areaPath.lineTo(x, y)
                } else {
                    val prevV = when (viewMode) {
                        "tiers" -> points[i - 1].expertTopicsCount.toFloat()
                        "accuracy" -> points[i - 1].accuracyPercent
                        else -> points[i - 1].masteryScore
                    }
                    val prevX = getX(i - 1)
                    val prevY = getY(prevV)
                    val cX1 = prevX + (x - prevX) / 2f
                    val cX2 = prevX + (x - prevX) / 2f
                    linePath.cubicTo(cX1, prevY, cX2, y, x, y)
                    areaPath.cubicTo(cX1, prevY, cX2, y, x, y)
                }
            }

            areaPath.lineTo(getX(points.size - 1), padTop + chartH)
            areaPath.close()

            // Draw Area Fill Gradient
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                    startY = padTop,
                    endY = padTop + chartH
                )
            )

            // Draw Line Curve
            drawPath(
                path = linePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Dot Markers
            points.forEachIndexed { i, pt ->
                if (i % 5 == 0 || i == points.size - 1) {
                    val v = when (viewMode) {
                        "tiers" -> pt.expertTopicsCount.toFloat()
                        "accuracy" -> pt.accuracyPercent
                        else -> pt.masteryScore
                    }
                    val x = getX(i)
                    val y = getY(v)
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = 4.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
