package com.example.dle_prototype.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.PersonalizationProfile
import com.example.dle_prototype.data.TraitScore
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TraitRadarCard(
    profile: PersonalizationProfile,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trait_radar_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Learner Cognitive Profile",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "On-device TFLite Inference (${profile.inferenceTimeMs}ms)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AI Adaptive",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Radar Polygon Canvas
            RadarCanvas(
                scores = listOf(
                    profile.conscientiousness.score,
                    profile.motivation.score,
                    profile.understanding.score,
                    profile.engagement.score
                ),
                labels = listOf("Conscientious", "Motivation", "Understanding", "Engagement"),
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Progress Bars with Descriptions
            TraitBar(profile.conscientiousness, IndigoPrimaryLight)
            Spacer(modifier = Modifier.height(8.dp))
            TraitBar(profile.motivation, AmberAccent)
            Spacer(modifier = Modifier.height(8.dp))
            TraitBar(profile.understanding, EmeraldSuccess)
            Spacer(modifier = Modifier.height(8.dp))
            TraitBar(profile.engagement, CyanAccent)
        }
    }
}

@Composable
fun RadarCanvas(
    scores: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val animatedScores = scores.map { score ->
        animateFloatAsState(
            targetValue = score.coerceIn(0.1f, 1f),
            animationSpec = tween(durationMillis = 800),
            label = "radarScore"
        ).value
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (size.minDimension / 2) * 0.85f

        // Draw concentric webs (25%, 50%, 75%, 100%)
        val steps = 4
        for (step in 1..steps) {
            val r = radius * (step.toFloat() / steps)
            val webPath = Path()
            for (i in 0 until 4) {
                val angle = (Math.PI / 2 * i - Math.PI / 2).toFloat()
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
            }
            webPath.close()
            drawPath(webPath, color = gridColor, style = Stroke(width = 1.dp.toPx()))
        }

        // Draw radial spokes
        for (i in 0 until 4) {
            val angle = (Math.PI / 2 * i - Math.PI / 2).toFloat()
            val endX = center.x + radius * cos(angle)
            val endY = center.y + radius * sin(angle)
            drawLine(
                color = gridColor,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw the data polygon
        val dataPath = Path()
        val dataPoints = mutableListOf<Offset>()
        for (i in animatedScores.indices) {
            val r = radius * animatedScores[i]
            val angle = (Math.PI / 2 * i - Math.PI / 2).toFloat()
            val pt = Offset(center.x + r * cos(angle), center.y + r * sin(angle))
            dataPoints.add(pt)
            if (i == 0) dataPath.moveTo(pt.x, pt.y) else dataPath.lineTo(pt.x, pt.y)
        }
        dataPath.close()

        // Fill with subtle alpha
        drawPath(
            dataPath,
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.45f), primaryColor.copy(alpha = 0.15f)),
                center = center,
                radius = radius
            )
        )
        // Outline
        drawPath(
            dataPath,
            color = primaryColor,
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Draw vertex dots
        for (pt in dataPoints) {
            drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = pt)
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)
        }
    }
}

@Composable
fun TraitBar(
    trait: TraitScore,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = trait.score.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "traitProgress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(barColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trait.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = trait.levelLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
