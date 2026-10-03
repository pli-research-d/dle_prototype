package com.example.dle_prototype.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess

@Composable
fun BatchBreakDialog(
    batchCount: Int,
    accuracyPercent: Int,
    xpGained: Int,
    kmGained: Float,
    nextTeaser: String,
    onKeepGoing: () -> Unit,
    onDoneForNow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onKeepGoing,
        containerColor = Color(0xFF0F172A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(24.dp))
                Text(
                    text = "Batch Complete! ($batchCount Cards)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Mini summary metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        title = "Accuracy",
                        value = "$accuracyPercent%",
                        color = if (accuracyPercent >= 70) EmeraldSuccess else AmberAccent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        title = "XP Gained",
                        value = "+$xpGained",
                        color = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        title = "Climbed",
                        value = "+${"%.1f".format(kmGained)} km",
                        color = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Cliffhanger Teaser
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "UP NEXT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nextTeaser,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onKeepGoing,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Keep Going 🔥", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDoneForNow,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done for Now", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun SoftStopDialog(
    sessionMinutes: Int,
    totalKm: Float,
    cardsDone: Int,
    onOneMoreBatch: () -> Unit,
    onFinish: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onFinish,
        containerColor = Color(0xFF0F172A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(24.dp))
                Text(
                    text = "Nice Session! 🎉",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "You've been in the flow for ~$sessionMinutes minutes and climbed ${"%.1f".format(totalKm)} km today across $cardsDone learning cards!",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF064E3B).copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🧠 Cognitive Tip: Taking a quick breather solidifies neural connections in long-term memory.",
                        fontSize = 11.sp,
                        color = Color(0xFFA7F3D0),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Finish Session", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onOneMoreBatch,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("One More Batch", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
private fun MetricChip(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
