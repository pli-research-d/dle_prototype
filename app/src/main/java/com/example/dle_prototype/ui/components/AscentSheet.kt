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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AscentSheet(
    altitudeKm: Float,
    streak: Int,
    onDismiss: () -> Unit,
    onReplayCelebration: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = EmeraldSuccess)
                    Text("Orbital Ascent Stats", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CURRENT ALTITUDE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = CyanAccent)
                    Text("${"%.2f".format(altitudeKm)} km", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Every correct answer burns Fuel and accelerates your orbital velocity.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }

            Text("ATMOSPHERE LAYERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            val layers = listOf(
                Triple("Troposphere (0 - 12 km)", "Foundational concepts & syntax drills", altitudeKm >= 0f),
                Triple("Stratosphere (12 - 50 km)", "Algorithmic thinking & spot-the-bug runs", altitudeKm >= 12f),
                Triple("Mesosphere (50 - 85 km)", "Competitive Duels & mastery peaks", altitudeKm >= 50f),
                Triple("Thermosphere (85 - 600 km)", "Low Earth Orbit (LEO) insertion!", altitudeKm >= 85f)
            )

            layers.forEach { (name, desc, reached) ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (reached) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFF131D31),
                    border = BorderStroke(1.dp, if (reached) EmeraldSuccess.copy(alpha = 0.5f) else Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (reached) EmeraldSuccess else Color(0xFF475569))
                        )
                        Column {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text(desc, fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onReplayCelebration()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Replay Milestone Confetti ✨", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        }
    }
}
