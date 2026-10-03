package com.example.dle_prototype.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess

@Composable
fun WeeklyRecapCard(
    username: String,
    kmClimbed: Float,
    accuracyPercent: Int,
    leagueFinish: String,
    totalXp: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = CircleShape, color = AmberAccent.copy(alpha = 0.2f), modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text("WEEKLY LEARNING RECAP", fontSize = 10.sp, fontWeight = FontWeight.Black, color = CyanAccent)
                        Text("Orbital Ascent Milestone", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0F172A)) {
                    Text("Week 40", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            // High-impact stats visual
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF090E1A),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${"%.1f".format(kmClimbed)} km", fontWeight = FontWeight.Black, fontSize = 18.sp, color = EmeraldSuccess)
                        Text("🚀 Climbed", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color(0xFF1E2E4A)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$accuracyPercent%", fontWeight = FontWeight.Black, fontSize = 18.sp, color = CyanAccent)
                        Text("🎯 Accuracy", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color(0xFF1E2E4A)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(leagueFinish, fontWeight = FontWeight.Black, fontSize = 18.sp, color = AmberAccent)
                        Text("🏆 League", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }

            Text(
                text = "✨ Outstanding performance! You maintained an active daily habit and outperformed 84% of learners in your league cohort.",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 16.sp
            )

            // Shareable CTA button
            Button(
                onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "🚀 My PLi Weekly Learning Recap: Climbed ${"%.1f".format(kmClimbed)} km with $accuracyPercent% accuracy! Placed $leagueFinish in League! #BuildInPublic #LearnToCode"
                        )
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Weekly Recap"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Weekly Recap Card", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
