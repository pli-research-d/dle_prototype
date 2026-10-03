package com.example.dle_prototype.ui.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.TrainingCheckpoint
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import org.json.JSONArray
import org.json.JSONObject

data class TrainingProgressPoint(
    val epoch: Int,
    val loss: Float,
    val valLoss: Float,
    val accuracy: Float,
    val tag: String? = null
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsTrainingCurveCard(
    checkpoints: List<TrainingCheckpoint>,
    userWeights: ModelWeights?,
    onOpenTraining: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRechartsMode by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Build synthesized or checkpoint-backed training epoch data
    val chartPoints = remember(checkpoints, userWeights) {
        buildTrainingChartData(checkpoints, userWeights)
    }

    val latestPoint = remember(chartPoints) {
        chartPoints.lastOrNull() ?: TrainingProgressPoint(1, 0.05f, 0.06f, 85f)
    }

    val chartJson = remember(chartPoints) {
        val array = JSONArray()
        for (pt in chartPoints) {
            val obj = JSONObject()
            obj.put("epoch", pt.epoch)
            obj.put("loss", pt.loss.toDouble())
            obj.put("valLoss", pt.valLoss.toDouble())
            obj.put("accuracy", pt.accuracy.toDouble())
            if (pt.tag != null) {
                obj.put("tag", pt.tag)
            }
            array.put(obj)
        }
        array.toString()
    }

    LaunchedEffect(chartJson, webViewRef) {
        webViewRef?.let { wv ->
            wv.evaluateJavascript("if (window.updateTrainingData) { window.updateTrainingData($chartJson); }", null)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_training_curve_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimaryLight.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Timeline,
                            contentDescription = null,
                            tint = IndigoPrimaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Training Progress Curve",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Recharts Visualization • Loss & Accuracy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = isRechartsMode,
                        onClick = { isRechartsMode = true },
                        label = { Text("Recharts", fontSize = 11.sp) },
                        modifier = Modifier.testTag("toggle_recharts_view")
                    )
                    FilterChip(
                        selected = !isRechartsMode,
                        onClick = { isRechartsMode = false },
                        label = { Text("Canvas", fontSize = 11.sp) },
                        modifier = Modifier.testTag("toggle_canvas_view")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Highlight Metric Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Loss",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.4f".format(latestPoint.loss),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = IndigoPrimaryLight
                    )
                }

                Column {
                    Text(
                        text = "Trait Accuracy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.1f%%".format(latestPoint.accuracy),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }

                Column {
                    Text(
                        text = "Trained Epochs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${latestPoint.epoch} Ep",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "Checkpoints",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${checkpoints.size} Saved",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Render Section
            if (isRechartsMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .testTag("recharts_webview_container")
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
                                            "if (window.updateTrainingData) { window.updateTrainingData($chartJson); }",
                                            null
                                        )
                                    }
                                }
                                loadUrl("file:///android_asset/recharts_training_curve.html")
                                webViewRef = this
                            }
                        },
                        update = { wv ->
                            wv.evaluateJavascript(
                                "if (window.updateTrainingData) { window.updateTrainingData($chartJson); }",
                                null
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(290.dp)
                    )
                }
            } else {
                // Native Jetpack Compose Canvas Dual-Axis Chart
                NativeCanvasTrainingCurve(
                    data = chartPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenTraining,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(40.dp)
                        .testTag("dashboard_train_model_cta"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ModelTraining, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Train New Epochs", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = {
                        webViewRef?.evaluateJavascript(
                            "if (window.updateTrainingData) { window.updateTrainingData($chartJson); }",
                            null
                        )
                    },
                    modifier = Modifier
                        .weight(0.9f)
                        .height(40.dp)
                        .testTag("reload_chart_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync")
                }
            }
        }
    }
}

/**
 * Builds training chart data points from user checkpoints or trained weights.
 */
fun buildTrainingChartData(
    checkpoints: List<TrainingCheckpoint>,
    userWeights: ModelWeights?
): List<TrainingProgressPoint> {
    // If checkpoints exist, build trajectory from checkpoints history
    val sortedCheckpoints = checkpoints.sortedBy { it.currentEpoch }
    val latestCp = sortedCheckpoints.lastOrNull()

    if (latestCp != null && latestCp.lossHistory.isNotEmpty()) {
        val totalEpochs = latestCp.lossHistory.size
        val points = mutableListOf<TrainingProgressPoint>()

        for (i in 0 until totalEpochs) {
            val epoch = i + 1
            val loss = latestCp.lossHistory[i]
            val valLoss = loss * (1.05f + (i * 0.003f)) // slight regularization gap
            // Estimated accuracy progress tracking logarithmic convergence
            val progress = (i + 1).toFloat() / totalEpochs.toFloat()
            val acc = (55f + 35f * (1f - Math.exp(-3.5 * progress.toDouble()).toFloat())).coerceIn(50f, 96f).toFloat()

            val cpAtEpoch = sortedCheckpoints.firstOrNull { it.currentEpoch == epoch }
            val tag = when {
                cpAtEpoch?.isBest == true -> "⭐ Best (${"%.1f".format(cpAtEpoch.accuracyPct)}%)"
                cpAtEpoch?.triggerType == "ACCURACY_DROP" -> "⚠️ Drop"
                cpAtEpoch != null -> "Epoch $epoch (CP)"
                epoch == totalEpochs -> "Final"
                else -> null
            }

            points.add(
                TrainingProgressPoint(
                    epoch = epoch,
                    loss = loss,
                    valLoss = valLoss,
                    accuracy = cpAtEpoch?.accuracyPct?.takeIf { it > 0f } ?: acc,
                    tag = tag
                )
            )
        }
        return points
    }

    // If trained weights exist without dense checkpoint history
    if (userWeights != null && userWeights.trainedEpochs > 0) {
        val epochs = userWeights.trainedEpochs.coerceAtLeast(10)
        val finalLoss = userWeights.finalLoss.coerceAtLeast(0.015f)
        val initLoss = finalLoss + (epochs * 0.004f).coerceAtLeast(0.15f)
        val points = mutableListOf<TrainingProgressPoint>()

        for (ep in 1..epochs) {
            val prog = ep.toFloat() / epochs.toFloat()
            val loss = initLoss - (initLoss - finalLoss) * (1f - Math.exp(-3.2 * prog.toDouble()).toFloat())
            val valLoss = loss * 1.06f
            val acc = 54f + (36f * prog).coerceIn(0f, 38f)
            val tag = if (ep == epochs) "Current v${userWeights.version}" else null
            points.add(TrainingProgressPoint(ep, loss, valLoss, acc, tag))
        }
        return points
    }

    // Default Baseline initialization curve
    return listOf(
        TrainingProgressPoint(1, 0.2240f, 0.2310f, 50.0f, "Baseline Init"),
        TrainingProgressPoint(2, 0.1820f, 0.1910f, 59.5f, null),
        TrainingProgressPoint(3, 0.1410f, 0.1530f, 68.0f, "Checkpoint 3"),
        TrainingProgressPoint(4, 0.1100f, 0.1220f, 75.0f, null),
        TrainingProgressPoint(5, 0.0860f, 0.0980f, 81.5f, null),
        TrainingProgressPoint(6, 0.0680f, 0.0810f, 86.0f, "⭐ Best State"),
        TrainingProgressPoint(7, 0.0550f, 0.0710f, 87.5f, null),
        TrainingProgressPoint(8, 0.0470f, 0.0650f, 88.5f, null),
        TrainingProgressPoint(9, 0.0420f, 0.0610f, 89.0f, "Checkpoint 9"),
        TrainingProgressPoint(10, 0.0380f, 0.0590f, 89.5f, "Epoch 10")
    )
}

/**
 * Native Jetpack Compose Canvas Dual-Axis Fallback View.
 */
@Composable
fun NativeCanvasTrainingCurve(
    data: List<TrainingProgressPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxEpoch = data.maxOf { it.epoch }
    val minEpoch = data.minOf { it.epoch }
    val maxLoss = (data.maxOf { maxOf(it.loss, it.valLoss) } * 1.15f).coerceAtLeast(0.1f)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IndigoPrimaryLight))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Loss (MSE)", fontSize = 10.sp, color = IndigoPrimaryLight, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmberAccent))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Val Loss", fontSize = 10.sp, color = AmberAccent)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldSuccess))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Accuracy %", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val padL = 36.dp.toPx()
            val padR = 36.dp.toPx()
            val padT = 10.dp.toPx()
            val padB = 22.dp.toPx()

            val chartW = size.width - padL - padR
            val chartH = size.height - padT - padB

            fun getX(ep: Int): Float {
                if (maxEpoch == minEpoch) return padL + chartW / 2
                return padL + ((ep - minEpoch).toFloat() / (maxEpoch - minEpoch).toFloat()) * chartW
            }

            fun getYLoss(loss: Float): Float {
                return padT + chartH - (loss / maxLoss) * chartH
            }

            fun getYAcc(acc: Float): Float {
                return padT + chartH - (acc / 100f) * chartH
            }

            // Grid lines
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(padL, padT),
                end = Offset(size.width - padR, padT),
                strokeWidth = 1f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(padL, padT + chartH * 0.5f),
                end = Offset(size.width - padR, padT + chartH * 0.5f),
                strokeWidth = 1f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(padL, padT + chartH),
                end = Offset(size.width - padR, padT + chartH),
                strokeWidth = 1.5f
            )

            // Accuracy Area & Line
            val accPath = Path()
            val accArea = Path()
            data.forEachIndexed { i, pt ->
                val x = getX(pt.epoch)
                val y = getYAcc(pt.accuracy)
                if (i == 0) {
                    accPath.moveTo(x, y)
                    accArea.moveTo(x, padT + chartH)
                    accArea.lineTo(x, y)
                } else {
                    accPath.lineTo(x, y)
                    accArea.lineTo(x, y)
                }
            }
            accArea.lineTo(getX(data.last().epoch), padT + chartH)
            accArea.close()

            drawPath(
                path = accArea,
                brush = Brush.verticalGradient(
                    colors = listOf(EmeraldSuccess.copy(alpha = 0.25f), Color.Transparent),
                    startY = padT,
                    endY = padT + chartH
                )
            )
            drawPath(path = accPath, color = EmeraldSuccess, style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round))

            // Loss Path
            val lossPath = Path()
            data.forEachIndexed { i, pt ->
                val x = getX(pt.epoch)
                val y = getYLoss(pt.loss)
                if (i == 0) lossPath.moveTo(x, y) else lossPath.lineTo(x, y)
            }
            drawPath(path = lossPath, color = IndigoPrimaryLight, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

            // Points
            data.forEach { pt ->
                val x = getX(pt.epoch)
                val yL = getYLoss(pt.loss)
                val yA = getYAcc(pt.accuracy)
                drawCircle(color = IndigoPrimaryLight, radius = 3.5.dp.toPx(), center = Offset(x, yL))
                drawCircle(color = EmeraldSuccess, radius = 3.dp.toPx(), center = Offset(x, yA))
            }
        }
    }
}
