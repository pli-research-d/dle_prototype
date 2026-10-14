package com.example.dle_prototype.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.graph.KnowledgeEdge
import com.example.dle_prototype.data.graph.KnowledgeGraphEngine
import com.example.dle_prototype.data.graph.KnowledgeSpacedRepetitionSummary
import com.example.dle_prototype.data.graph.KnowledgeTopicNode
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bottom Sheet displaying Spaced Repetition Reinforcements directly within the Knowledge Graph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeReinforcementSheet(
    username: String,
    allNodes: List<KnowledgeTopicNode>,
    existingEdges: List<KnowledgeEdge>,
    dbHelper: DatabaseHelper,
    onDismiss: () -> Unit,
    onStartReview: (categoryName: String, categoryNumber: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var summary by remember { mutableStateOf<KnowledgeSpacedRepetitionSummary?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username, allNodes) {
        withContext(Dispatchers.IO) {
            val attempts = dbHelper.getAllQuizHistory(username, limit = 200)
            summary = KnowledgeGraphEngine.scheduleKnowledgeReinforcements(
                nodes = allNodes,
                edges = existingEdges,
                quizAttempts = attempts,
                currentTimeMillis = System.currentTimeMillis()
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF090D16),
        dragHandle = null,
        modifier = modifier.testTag("knowledge_reinforcement_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "🧠 SPACED REPETITION REINFORCEMENT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                        Text(
                            text = "SM-2 Ebbinghaus Memory Decay · Prerequisite Protection",
                            fontSize = 10.sp,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_reinforcement_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            feedbackMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔔 $msg",
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            val currentSummary = summary
            if (currentSummary != null) {
                KnowledgeReinforcementCard(
                    summary = currentSummary,
                    username = username,
                    onStartReview = { cat, num ->
                        onDismiss()
                        onStartReview(cat, num)
                    },
                    onNotificationFeedback = { feedbackMessage = it }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Analyzing Knowledge Graph Memory Decay...",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
