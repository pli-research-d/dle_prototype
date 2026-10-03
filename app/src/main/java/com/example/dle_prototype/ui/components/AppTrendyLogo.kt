package com.example.dle_prototype.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.R
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent

/**
 * Modern, trendy Brand Logo for PLi (Personal Learning Intelligence).
 * Renders an adaptive glowing squircle containing the stylized PLi neural intelligence glyph.
 */
@Composable
fun AppTrendyLogo(
    size: Dp = 44.dp,
    showLabel: Boolean = false,
    labelSubtitle: String? = "Personal Learning Intelligence",
    animatedGlow: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (animatedGlow) 0.55f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val cornerRadius = size * 0.28f

    val logoBox = @Composable {
        Box(
            modifier = Modifier
                .size(size)
                .drawBehind {
                    // Soft ambient gradient blur behind logo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyanAccent.copy(alpha = glowAlpha),
                                IndigoPrimaryLight.copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent
                            ),
                            radius = size.toPx() * 0.75f
                        )
                    )
                }
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E1B4B),
                            Color(0xFF0B132B)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                CyanAccent,
                                IndigoPrimaryLight,
                                RoseAccent.copy(alpha = 0.8f)
                            )
                        )
                    ),
                    RoundedCornerShape(cornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_pli_logo),
                contentDescription = "PLi Brand Logo",
                modifier = Modifier.size(size * 0.82f)
            )
        }
    }

    if (!showLabel) {
        Box(modifier = modifier) {
            logoBox()
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            logoBox()
            Column {
                Text(
                    text = "PLi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.3.sp
                )
                if (labelSubtitle != null) {
                    Text(
                        text = labelSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
