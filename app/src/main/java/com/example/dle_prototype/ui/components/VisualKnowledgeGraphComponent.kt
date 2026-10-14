package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.graph.GraphViewMode
import com.example.dle_prototype.data.graph.KnowledgeEdge
import com.example.dle_prototype.data.graph.KnowledgeGraphData
import com.example.dle_prototype.data.graph.KnowledgeGraphEngine
import com.example.dle_prototype.data.graph.KnowledgeMasteryLevel
import com.example.dle_prototype.data.graph.KnowledgeTopicNode
import com.example.dle_prototype.data.graph.TopicMasteryStatus
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class GraphFilterMode(val label: String) {
    ALL("All Topics"),
    EXPERT("🥇 Expert (Ring 3)"),
    INTERMEDIATE("🥈 Intermed (Ring 2)"),
    BEGINNER("🥉 Beginner (Ring 1)"),
    STRENGTHS("🚀 Strengths"),
    REVIEW("⚠️ Needs Review")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisualKnowledgeGraphComponent(
    username: String,
    dbHelper: DatabaseHelper,
    modifier: Modifier = Modifier,
    onStartQuiz: ((categoryName: String, categoryNumber: Float) -> Unit)? = null,
    onOpenQaChat: (() -> Unit)? = null
) {
    var graphData by remember { mutableStateOf<KnowledgeGraphData?>(null) }
    var selectedNode by remember { mutableStateOf<KnowledgeTopicNode?>(null) }
    var filterMode by remember { mutableStateOf(GraphFilterMode.ALL) }
    var selectedCluster by remember { mutableStateOf<String?>(null) }
    var viewMode by remember { mutableStateOf(GraphViewMode.CONSTELLATION) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMasteryFilter by remember { mutableStateOf<KnowledgeMasteryLevel?>(null) }
    var showLegendDialog by remember { mutableStateOf(false) }
    var showMasteryTrends by remember { mutableStateOf(false) }
    var showVoiceSummarySheet by remember { mutableStateOf(false) }
    var showReinforcementSheet by remember { mutableStateOf(false) }
    var showAdaptiveQuizSheet by remember { mutableStateOf(false) }
    var adaptiveQuizNode by remember { mutableStateOf<KnowledgeTopicNode?>(null) }
    var recentAttempts by remember { mutableStateOf<List<com.example.dle_prototype.data.QuizAttempt>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Canvas Transformation State (Pan & Zoom)
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableFloatStateOf(1f) }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // Pulsing halo animation for critical nodes & glowing selection
    val infiniteTransition = rememberInfiniteTransition(label = "knowledge_graph_anim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Flowing knowledge energy particles along edges
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_progress"
    )

    LaunchedEffect(username) {
        graphData = KnowledgeGraphEngine.buildKnowledgeGraph(username, dbHelper)
        recentAttempts = dbHelper.getRecentQuizAttempts(username, 30)
    }

    val data = graphData ?: return

    val filteredNodes = remember(data.nodes, filterMode, selectedCluster, searchQuery, selectedMasteryFilter) {
        val cleanQuery = searchQuery.trim().lowercase()

        // Detect if query mentions a mastery level
        val queryMasteryLevel: KnowledgeMasteryLevel? = when {
            cleanQuery.isEmpty() -> null
            "expert".startsWith(cleanQuery) || cleanQuery.contains("expert") -> KnowledgeMasteryLevel.EXPERT
            "intermediate".startsWith(cleanQuery) || cleanQuery.contains("intermediate") || cleanQuery.startsWith("inter") -> KnowledgeMasteryLevel.INTERMEDIATE
            "beginner".startsWith(cleanQuery) || cleanQuery.contains("beginner") || cleanQuery.contains("novice") -> KnowledgeMasteryLevel.BEGINNER
            else -> null
        }

        data.nodes.filter { node ->
            val matchesFilter = when (filterMode) {
                GraphFilterMode.ALL -> true
                GraphFilterMode.EXPERT -> node.masteryLevel == KnowledgeMasteryLevel.EXPERT
                GraphFilterMode.INTERMEDIATE -> node.masteryLevel == KnowledgeMasteryLevel.INTERMEDIATE
                GraphFilterMode.BEGINNER -> node.masteryLevel == KnowledgeMasteryLevel.BEGINNER
                GraphFilterMode.STRENGTHS -> node.status == TopicMasteryStatus.STRENGTH
                GraphFilterMode.REVIEW -> node.status == TopicMasteryStatus.NEEDS_REVIEW
            }

            val matchesMasteryFilter = selectedMasteryFilter == null || node.masteryLevel == selectedMasteryFilter
            val matchesCluster = selectedCluster == null || node.clusterName.equals(selectedCluster, ignoreCase = true)

            val matchesSearch = if (cleanQuery.isEmpty()) {
                true
            } else {
                val matchesTopicName = node.title.contains(cleanQuery, ignoreCase = true) ||
                        node.shortLabel.contains(cleanQuery, ignoreCase = true) ||
                        node.id.contains(cleanQuery, ignoreCase = true)
                val matchesMasteryText = node.masteryLevel.label.contains(cleanQuery, ignoreCase = true) ||
                        node.masteryLevel.name.contains(cleanQuery, ignoreCase = true) ||
                        node.masteryLevel.badgeText.contains(cleanQuery, ignoreCase = true) ||
                        (queryMasteryLevel != null && node.masteryLevel == queryMasteryLevel)
                val matchesCategory = node.category.contains(cleanQuery, ignoreCase = true)
                val matchesClusterName = node.clusterName.contains(cleanQuery, ignoreCase = true)
                val matchesConcepts = node.keyConcepts.any { it.contains(cleanQuery, ignoreCase = true) }
                val matchesDesc = node.description.contains(cleanQuery, ignoreCase = true)

                matchesTopicName || matchesMasteryText || matchesCategory || matchesClusterName || matchesConcepts || matchesDesc
            }

            matchesFilter && matchesMasteryFilter && matchesCluster && matchesSearch
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visual_knowledge_graph_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0B1120),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
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
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = "Knowledge Graph",
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Knowledge Graph",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Canvas 2D",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Interconnected topology of curriculum topics & mastery flow",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // 30D Recharts Trends Toggle Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showMasteryTrends) CyanAccent.copy(alpha = 0.2f) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (showMasteryTrends) CyanAccent else Color(0xFF334155)),
                        modifier = Modifier
                            .clickable { showMasteryTrends = !showMasteryTrends }
                            .testTag("graph_trends_toggle_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = if (showMasteryTrends) CyanAccent else Color(0xFF94A3B8),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "30D Trends",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showMasteryTrends) CyanAccent else Color(0xFF94A3B8)
                            )
                        }
                    }

                    // 🎙️ Voice Learning Summary Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showVoiceSummarySheet) Color(0xFF8B5CF6).copy(alpha = 0.25f) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (showVoiceSummarySheet) Color(0xFFA855F7) else Color(0xFF334155)),
                        modifier = Modifier
                            .clickable { showVoiceSummarySheet = true }
                            .testTag("voice_summary_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Summary",
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Voice Summary",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE9D5FF)
                            )
                        }
                    }

                    // Spaced Repetition Reinforcement Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .clickable { showReinforcementSheet = true }
                            .testTag("spaced_repetition_reinforce_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Spaced Repetition Reinforce",
                                tint = AmberAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Reinforce",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent
                            )
                        }
                    }

                    IconButton(
                        onClick = { showLegendDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("graph_legend_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Graph Legend",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${data.averageMasteryPercent.toInt()}% Avg",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // =========================================================
            // 🔍 TOP SEARCH BAR & MASTERY LEVEL FILTER
            // =========================================================
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF090E1A),
                border = BorderStroke(
                    1.dp,
                    if (searchQuery.isNotBlank() || selectedMasteryFilter != null) CyanAccent.copy(alpha = 0.7f) else Color(0xFF1E293B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("graph_search_bar")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Input: filter by topic name or mastery level
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search topic (e.g. Loops) or mastery (Beginner, Expert)...",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Concepts",
                                tint = if (searchQuery.isNotBlank() || selectedMasteryFilter != null) CyanAccent else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                // Match counter pill
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (filteredNodes.size == data.nodes.size) Color(0xFF1E293B) else CyanAccent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${filteredNodes.size}/${data.nodes.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (filteredNodes.size == data.nodes.size) Color(0xFF94A3B8) else CyanAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (searchQuery.isNotEmpty() || selectedMasteryFilter != null) {
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            selectedMasteryFilter = null
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("graph_search_clear_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = Color(0xFFCBD5E1),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF070B14),
                            unfocusedContainerColor = Color(0xFF070B14)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("graph_search_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Quick Mastery Level Filter Chips (Beginner, Intermediate, Expert)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Filter Level:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(end = 2.dp)
                        )

                        // All Chip
                        val isAllSelected = selectedMasteryFilter == null &&
                                !listOf("beginner", "intermediate", "expert", "novice").any { searchQuery.lowercase().contains(it) }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAllSelected) CyanAccent.copy(alpha = 0.2f) else Color(0xFF131D31),
                            border = BorderStroke(1.dp, if (isAllSelected) CyanAccent else Color(0xFF1E293B)),
                            modifier = Modifier
                                .clickable {
                                    selectedMasteryFilter = null
                                    if (listOf("beginner", "intermediate", "expert", "novice").any { searchQuery.lowercase().contains(it) }) {
                                        searchQuery = ""
                                    }
                                }
                                .testTag("mastery_filter_chip_all")
                        ) {
                            Text(
                                text = "All (${data.nodes.size})",
                                fontSize = 11.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllSelected) CyanAccent else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Beginner Chip
                        val isBeginnerSelected = selectedMasteryFilter == KnowledgeMasteryLevel.BEGINNER ||
                                searchQuery.lowercase().contains("beginner") || searchQuery.lowercase().contains("novice")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isBeginnerSelected) KnowledgeMasteryLevel.BEGINNER.ringColor.copy(alpha = 0.25f) else Color(0xFF131D31),
                            border = BorderStroke(1.dp, if (isBeginnerSelected) KnowledgeMasteryLevel.BEGINNER.ringColor else Color(0xFF1E293B)),
                            modifier = Modifier
                                .clickable {
                                    selectedMasteryFilter = if (selectedMasteryFilter == KnowledgeMasteryLevel.BEGINNER) null else KnowledgeMasteryLevel.BEGINNER
                                }
                                .testTag("mastery_filter_chip_beginner")
                        ) {
                            Text(
                                text = "🥉 Beginner (${data.beginnerCount})",
                                fontSize = 11.sp,
                                fontWeight = if (isBeginnerSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isBeginnerSelected) KnowledgeMasteryLevel.BEGINNER.ringColor else Color(0xFFCBD5E1),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Intermediate Chip
                        val isIntermediateSelected = selectedMasteryFilter == KnowledgeMasteryLevel.INTERMEDIATE ||
                                searchQuery.lowercase().contains("intermediate") || searchQuery.lowercase().contains("inter")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isIntermediateSelected) KnowledgeMasteryLevel.INTERMEDIATE.ringColor.copy(alpha = 0.25f) else Color(0xFF131D31),
                            border = BorderStroke(1.dp, if (isIntermediateSelected) KnowledgeMasteryLevel.INTERMEDIATE.ringColor else Color(0xFF1E293B)),
                            modifier = Modifier
                                .clickable {
                                    selectedMasteryFilter = if (selectedMasteryFilter == KnowledgeMasteryLevel.INTERMEDIATE) null else KnowledgeMasteryLevel.INTERMEDIATE
                                }
                                .testTag("mastery_filter_chip_intermediate")
                        ) {
                            Text(
                                text = "🥈 Intermediate (${data.intermediateCount})",
                                fontSize = 11.sp,
                                fontWeight = if (isIntermediateSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isIntermediateSelected) KnowledgeMasteryLevel.INTERMEDIATE.ringColor else Color(0xFFCBD5E1),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Expert Chip
                        val isExpertSelected = selectedMasteryFilter == KnowledgeMasteryLevel.EXPERT ||
                                searchQuery.lowercase().contains("expert")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isExpertSelected) KnowledgeMasteryLevel.EXPERT.ringColor.copy(alpha = 0.25f) else Color(0xFF131D31),
                            border = BorderStroke(1.dp, if (isExpertSelected) KnowledgeMasteryLevel.EXPERT.ringColor else Color(0xFF1E293B)),
                            modifier = Modifier
                                .clickable {
                                    selectedMasteryFilter = if (selectedMasteryFilter == KnowledgeMasteryLevel.EXPERT) null else KnowledgeMasteryLevel.EXPERT
                                }
                                .testTag("mastery_filter_chip_expert")
                        ) {
                            Text(
                                text = "🥇 Expert (${data.expertCount})",
                                fontSize = 11.sp,
                                fontWeight = if (isExpertSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isExpertSelected) KnowledgeMasteryLevel.EXPERT.ringColor else Color(0xFFCBD5E1),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Matching Results Quick-Jump Strip (When search or filter active)
                    if (searchQuery.isNotBlank() || selectedMasteryFilter != null) {
                        if (filteredNodes.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Matched:",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyanAccent
                                )
                                filteredNodes.forEach { node ->
                                    val isNodeSelected = selectedNode?.id == node.id
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isNodeSelected) CyanAccent.copy(alpha = 0.3f) else Color(0xFF1E293B),
                                        border = BorderStroke(0.5.dp, node.masteryLevel.ringColor),
                                        modifier = Modifier.clickable {
                                            selectedNode = node
                                            val targetX = if (viewMode == GraphViewMode.ROADMAP) node.normRoadmapX else node.normX
                                            val targetY = if (viewMode == GraphViewMode.ROADMAP) node.normRoadmapY else node.normY
                                            panOffset = Offset(
                                                x = (0.5f - targetX) * 200f,
                                                y = (0.5f - targetY) * 200f
                                            )
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = node.masteryLevel.iconEmoji,
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = node.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isNodeSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1010), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "No topics match filter. Try 'Loops' or 'Expert'",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFCA5A5)
                                )
                                Text(
                                    text = "Reset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    modifier = Modifier.clickable {
                                        searchQuery = ""
                                        selectedMasteryFilter = null
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Suggested Next Focus Banner (Based on Adaptive Telemetry)
            data.recommendedNextTopic?.let { nextTopic ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, nextTopic.status.color.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedNode = nextTopic
                            // Center view on this node
                            val targetX = if (viewMode == GraphViewMode.ROADMAP) nextTopic.normRoadmapX else nextTopic.normX
                            val targetY = if (viewMode == GraphViewMode.ROADMAP) nextTopic.normRoadmapY else nextTopic.normY
                            panOffset = Offset(
                                x = (0.5f - targetX) * 200f,
                                y = (0.5f - targetY) * 200f
                            )
                        }
                        .testTag("graph_recommended_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = nextTopic.status.color,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "RECOMMENDED NEXT FOCUS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = nextTopic.status.color
                                )
                                Text(
                                    text = "${nextTopic.title} (${nextTopic.status.label})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = nextTopic.status.color.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, nextTopic.status.color)
                        ) {
                            Text(
                                text = "Focus in Graph",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = nextTopic.status.color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Summary Metrics Row: Color-Coded Mastery Indicator Levels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B14), RoundedCornerShape(12.dp))
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricPill(
                    label = "Beginner",
                    count = "${data.beginnerCount}",
                    color = KnowledgeMasteryLevel.BEGINNER.ringColor,
                    emoji = "🥉",
                    isSelected = selectedMasteryFilter == KnowledgeMasteryLevel.BEGINNER,
                    onClick = {
                        selectedMasteryFilter = if (selectedMasteryFilter == KnowledgeMasteryLevel.BEGINNER) null else KnowledgeMasteryLevel.BEGINNER
                    }
                )
                MetricPill(
                    label = "Intermediate",
                    count = "${data.intermediateCount}",
                    color = KnowledgeMasteryLevel.INTERMEDIATE.ringColor,
                    emoji = "🥈",
                    isSelected = selectedMasteryFilter == KnowledgeMasteryLevel.INTERMEDIATE,
                    onClick = {
                        selectedMasteryFilter = if (selectedMasteryFilter == KnowledgeMasteryLevel.INTERMEDIATE) null else KnowledgeMasteryLevel.INTERMEDIATE
                    }
                )
                MetricPill(
                    label = "Expert",
                    count = "${data.expertCount}",
                    color = KnowledgeMasteryLevel.EXPERT.ringColor,
                    emoji = "🥇",
                    isSelected = selectedMasteryFilter == KnowledgeMasteryLevel.EXPERT,
                    onClick = {
                        selectedMasteryFilter = if (selectedMasteryFilter == KnowledgeMasteryLevel.EXPERT) null else KnowledgeMasteryLevel.EXPERT
                    }
                )
                MetricPill(
                    label = "Strengths",
                    count = "${data.strengthsCount}",
                    color = EmeraldSuccess,
                    emoji = "🚀",
                    isSelected = filterMode == GraphFilterMode.STRENGTHS,
                    onClick = {
                        filterMode = if (filterMode == GraphFilterMode.STRENGTHS) GraphFilterMode.ALL else GraphFilterMode.STRENGTHS
                    }
                )
            }

            // Layout Mode Switcher & Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Mode Toggle (Constellation 🌌 vs Roadmap 🗺️)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        GraphViewMode.values().forEach { mode ->
                            val isSel = viewMode == mode
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) CyanAccent else Color.Transparent,
                                modifier = Modifier
                                    .clickable { viewMode = mode }
                                    .testTag("graph_view_mode_${mode.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (mode == GraphViewMode.CONSTELLATION) Icons.Default.Timeline else Icons.Default.Map,
                                        contentDescription = null,
                                        tint = if (isSel) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = mode.title.split(" ").first(),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }

                // Cluster Filter Dropdown or Quick Reset
                if (selectedCluster != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, CyanAccent)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { selectedCluster = null }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Cluster: $selectedCluster",
                                fontSize = 11.sp,
                                color = CyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = CyanAccent, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            // Filter Chips Carousel (Status & Clusters)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GraphFilterMode.values().forEach { mode ->
                    val isSelected = filterMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterMode = mode },
                        label = {
                            Text(
                                text = mode.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                            selectedLabelColor = CyanAccent,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) CyanAccent else Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("graph_filter_chip_${mode.name}")
                    )
                }

                // Add cluster quick-filter chips
                data.clusters.forEach { cluster ->
                    val isClusterSelected = selectedCluster == cluster.name
                    FilterChip(
                        selected = isClusterSelected,
                        onClick = {
                            selectedCluster = if (isClusterSelected) null else cluster.name
                        },
                        label = {
                            Text(
                                text = cluster.name,
                                fontSize = 11.sp,
                                fontWeight = if (isClusterSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cluster.color.copy(alpha = 0.25f),
                            selectedLabelColor = cluster.color,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isClusterSelected) cluster.color else Color(0xFF334155)
                        )
                    )
                }
            }

            // Interactive 2D Canvas Surface
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF030712),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("knowledge_graph_canvas")
                            .pointerInput(filteredNodes, zoomScale, panOffset, viewMode) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    var totalDrag = Offset.Zero
                                    var pastTouchSlop = false
                                    val touchSlop = viewConfiguration.touchSlop

                                    do {
                                        val event = awaitPointerEvent()
                                        val canceled = event.changes.any { it.isConsumed }
                                        if (canceled) break

                                        if (event.changes.size > 1) {
                                            // Multi-touch Pinch to Zoom & Two-finger Pan
                                            val zoomChange = event.calculateZoom()
                                            val panChange = event.calculatePan()
                                            zoomScale = (zoomScale * zoomChange).coerceIn(0.65f, 2.8f)
                                            panOffset += panChange
                                            event.changes.forEach { it.consume() }
                                            pastTouchSlop = true
                                        } else if (event.changes.size == 1) {
                                            val change = event.changes[0]
                                            val dragDelta = change.position - change.previousPosition
                                            totalDrag += dragDelta

                                            if (!pastTouchSlop && totalDrag.getDistance() > touchSlop) {
                                                pastTouchSlop = true
                                            }

                                            if (pastTouchSlop) {
                                                panOffset += dragDelta
                                                change.consume()
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })

                                    // If gesture ended without significant drag, it's a TAP!
                                    if (!pastTouchSlop) {
                                        val tapPos = down.position
                                        // Invert canvas transformation to find node in world space
                                        val tapWorldX = (tapPos.x - panOffset.x) / zoomScale
                                        val tapWorldY = (tapPos.y - panOffset.y) / zoomScale

                                        val canvasW = size.width
                                        val canvasH = size.height
                                        val tapThresholdPx = 36.dp.toPx()

                                        var closest: KnowledgeTopicNode? = null
                                        var minDist = Float.MAX_VALUE

                                        filteredNodes.forEach { node ->
                                            val nx = if (viewMode == GraphViewMode.ROADMAP) node.normRoadmapX else node.normX
                                            val ny = if (viewMode == GraphViewMode.ROADMAP) node.normRoadmapY else node.normY
                                            val nodeWorldX = nx * canvasW
                                            val nodeWorldY = ny * canvasH
                                            val dist = sqrt((tapWorldX - nodeWorldX) * (tapWorldX - nodeWorldX) + (tapWorldY - nodeWorldY) * (tapWorldY - nodeWorldY))
                                            if (dist < tapThresholdPx && dist < minDist) {
                                                minDist = dist
                                                closest = node
                                            }
                                        }
                                        selectedNode = closest
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Coordinate Transformation Helpers
                        fun nodeScreenPos(node: KnowledgeTopicNode): Offset {
                            val nx = if (viewMode == GraphViewMode.ROADMAP) node.normRoadmapX else node.normX
                            val ny = if (viewMode == GraphViewMode.ROADMAP) node.normRoadmapY else node.normY
                            val worldX = nx * canvasWidth
                            val worldY = ny * canvasHeight
                            return Offset(
                                x = worldX * zoomScale + panOffset.x,
                                y = worldY * zoomScale + panOffset.y
                            )
                        }

                        // 1. Draw Subtle Constellation Dot Grid (Infinite Scrolling)
                        val dotSpacing = (36.dp.toPx() * zoomScale).coerceIn(24f, 72f)
                        val offsetX = (panOffset.x % dotSpacing + dotSpacing) % dotSpacing
                        val offsetY = (panOffset.y % dotSpacing + dotSpacing) % dotSpacing
                        var gx = offsetX
                        while (gx < canvasWidth) {
                            var gy = offsetY
                            while (gy < canvasHeight) {
                                drawCircle(
                                    color = Color(0xFF1E293B).copy(alpha = 0.35f),
                                    radius = 1.3f,
                                    center = Offset(gx, gy)
                                )
                                gy += dotSpacing
                            }
                            gx += dotSpacing
                        }

                        // 2. Draw Soft Ambient Cluster Nebulas (Constellation Mode)
                        if (viewMode == GraphViewMode.CONSTELLATION) {
                            data.clusters.forEach { cluster ->
                                val clusterScreenPos = Offset(
                                    x = (cluster.normCenterX * canvasWidth) * zoomScale + panOffset.x,
                                    y = (cluster.normCenterY * canvasHeight) * zoomScale + panOffset.y
                                )
                                val nebulaRadius = 90.dp.toPx() * zoomScale
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            cluster.color.copy(alpha = 0.10f),
                                            Color.Transparent
                                        ),
                                        center = clusterScreenPos,
                                        radius = nebulaRadius
                                    ),
                                    radius = nebulaRadius,
                                    center = clusterScreenPos
                                )
                            }
                        } else {
                            // In Roadmap Mode, draw tier dividing guideline labels
                            val tierYLevels = listOf(
                                "Tier 1: Foundations" to 0.14f,
                                "Tier 2: Core Architecture" to 0.38f,
                                "Tier 3: Reactive & Async" to 0.64f,
                                "Tier 4: Applied AI & Mastery" to 0.88f
                            )
                            tierYLevels.forEach { (label, normY) ->
                                val screenY = (normY * canvasHeight) * zoomScale + panOffset.y
                                drawLine(
                                    color = Color(0xFF1E293B).copy(alpha = 0.5f),
                                    start = Offset(0f, screenY),
                                    end = Offset(canvasWidth, screenY),
                                    strokeWidth = 1f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f)
                                )
                                val tierTextResult = textMeasurer.measure(
                                    text = label,
                                    style = TextStyle(
                                        color = Color(0xFF475569),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                drawText(
                                    textLayoutResult = tierTextResult,
                                    topLeft = Offset(12.dp.toPx(), screenY - tierTextResult.size.height - 2.dp.toPx())
                                )
                            }
                        }

                        // 3. Draw Interconnected Edges & Animated Knowledge Particles
                        val nodeMap = data.nodes.associateBy { it.id }
                        data.edges.forEachIndexed { edgeIndex, edge ->
                            val fromNode = nodeMap[edge.fromId]
                            val toNode = nodeMap[edge.toId]

                            if (fromNode != null && toNode != null) {
                                val start = nodeScreenPos(fromNode)
                                val end = nodeScreenPos(toNode)

                                val isConnectedToSelected = selectedNode != null &&
                                        (edge.fromId == selectedNode?.id || edge.toId == selectedNode?.id)
                                val isDirectPrerequisite = selectedNode != null && edge.toId == selectedNode?.id
                                val isUnlockedNext = selectedNode != null && edge.fromId == selectedNode?.id

                                val lineColor = when {
                                    edge.isCustom -> Color(0xFFA855F7).copy(alpha = 0.95f)
                                    isDirectPrerequisite -> AmberAccent.copy(alpha = 0.9f)
                                    isUnlockedNext -> EmeraldSuccess.copy(alpha = 0.9f)
                                    selectedNode != null -> Color(0xFF1E293B).copy(alpha = 0.15f)
                                    fromNode.status == TopicMasteryStatus.STRENGTH && toNode.status == TopicMasteryStatus.STRENGTH ->
                                        EmeraldSuccess.copy(alpha = 0.6f)
                                    fromNode.status == TopicMasteryStatus.NEEDS_REVIEW || toNode.status == TopicMasteryStatus.NEEDS_REVIEW ->
                                        AmberAccent.copy(alpha = 0.5f)
                                    else -> Color(0xFF334155).copy(alpha = 0.55f)
                                }

                                val strokeWidth = when {
                                    edge.isCustom -> 3.6f * zoomScale.coerceIn(0.8f, 1.4f)
                                    isConnectedToSelected -> 3.5f * zoomScale.coerceIn(0.8f, 1.4f)
                                    selectedNode != null -> 1.0f
                                    else -> 2.0f * zoomScale.coerceIn(0.8f, 1.2f)
                                }

                                val pathEffect = when {
                                    edge.isCustom -> PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                                    isDirectPrerequisite -> PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                                    fromNode.status == TopicMasteryStatus.NEEDS_REVIEW || toNode.status == TopicMasteryStatus.NEEDS_REVIEW ->
                                        PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                    else -> null
                                }

                                // Draw connection line
                                drawLine(
                                    color = lineColor,
                                    start = start,
                                    end = end,
                                    strokeWidth = strokeWidth,
                                    pathEffect = pathEffect
                                )

                                // Draw directional arrow chevron at 65% of edge
                                if (selectedNode == null || isConnectedToSelected) {
                                    drawArrowChevron(
                                        from = start,
                                        to = end,
                                        color = lineColor,
                                        fraction = 0.62f,
                                        arrowSize = 12f * zoomScale.coerceIn(0.8f, 1.3f)
                                    )
                                }

                                // Draw Animated Flowing Knowledge Particle (Energy Photons)
                                if (selectedNode == null || isConnectedToSelected) {
                                    val particleT = (particleProgress + edgeIndex * 0.13f) % 1f
                                    val particlePos = Offset(
                                        x = start.x + (end.x - start.x) * particleT,
                                        y = start.y + (end.y - start.y) * particleT
                                    )
                                    val particleColor = if (isConnectedToSelected) lineColor else CyanAccent.copy(alpha = 0.8f)

                                    drawCircle(
                                        color = particleColor,
                                        radius = (2.6f * zoomScale.coerceIn(0.8f, 1.4f)),
                                        center = particlePos
                                    )
                                }

                                // If connected to selected node, draw relationship badge text at midpoint
                                if (isConnectedToSelected) {
                                    val edgeMid = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
                                    val badgeText = when {
                                        edge.isCustom -> "🎙️ Voice: ${edge.relationship}"
                                        isUnlockedNext -> "Unlocks: ${edge.relationship}"
                                        else -> "Req: ${edge.relationship}"
                                    }
                                    val badgeStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = (9 * zoomScale.coerceIn(0.85f, 1.2f)).sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                    val badgeResult = textMeasurer.measure(badgeText, style = badgeStyle)
                                    val pad = 6.dp.toPx()
                                    val badgeWidth = badgeResult.size.width + pad * 2
                                    val badgeHeight = badgeResult.size.height + pad

                                    drawRoundRect(
                                        color = Color(0xFF090E1A).copy(alpha = 0.95f),
                                        topLeft = Offset(edgeMid.x - badgeWidth / 2f, edgeMid.y - badgeHeight / 2f),
                                        size = Size(badgeWidth, badgeHeight),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                    drawRoundRect(
                                        color = when {
                                            edge.isCustom -> Color(0xFFA855F7)
                                            isUnlockedNext -> EmeraldSuccess
                                            else -> AmberAccent
                                        },
                                        topLeft = Offset(edgeMid.x - badgeWidth / 2f, edgeMid.y - badgeHeight / 2f),
                                        size = Size(badgeWidth, badgeHeight),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                        style = Stroke(width = 1.2.dp.toPx())
                                    )
                                    drawText(
                                        textLayoutResult = badgeResult,
                                        topLeft = Offset(edgeMid.x - badgeResult.size.width / 2f, edgeMid.y - badgeResult.size.height / 2f)
                                    )
                                }
                            }
                        }

                        // 4. Draw Topic Nodes with Color-Coded Mastery Indicator Rings
                        data.nodes.forEach { node ->
                            val isNodeVisibleInFilter = filteredNodes.contains(node)
                            val isSelected = selectedNode?.id == node.id
                            val nodeCenter = nodeScreenPos(node)

                            val baseRadius = 20.dp.toPx()
                            val nodeRadius = (if (isSelected) baseRadius * 1.25f else baseRadius) * zoomScale.coerceIn(0.8f, 1.35f)

                            val mastery = node.masteryLevel
                            val baseMasteryColor = mastery.ringColor
                            val displayMasteryColor = if (isNodeVisibleInFilter) baseMasteryColor else baseMasteryColor.copy(alpha = 0.25f)
                            val displayGlowColor = if (isNodeVisibleInFilter) mastery.outerGlowColor else mastery.outerGlowColor.copy(alpha = 0.2f)

                            // Outer Radiant Corona / Glow Aura:
                            // Expert has crowning radiant aura; Needs review nodes or selected nodes have beacon glow
                            if (isNodeVisibleInFilter) {
                                if (mastery == KnowledgeMasteryLevel.EXPERT) {
                                    val auraRadius = nodeRadius + 14.dp.toPx() * zoomScale.coerceIn(0.85f, 1.3f)
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                displayMasteryColor.copy(alpha = pulseAlpha * 0.40f),
                                                displayGlowColor.copy(alpha = pulseAlpha * 0.15f),
                                                Color.Transparent
                                            ),
                                            center = nodeCenter,
                                            radius = auraRadius
                                        ),
                                        radius = auraRadius,
                                        center = nodeCenter
                                    )
                                } else if (node.status == TopicMasteryStatus.NEEDS_REVIEW || isSelected) {
                                    val haloRadius = nodeRadius + (if (node.status == TopicMasteryStatus.NEEDS_REVIEW) 14.dp.toPx() else 8.dp.toPx())
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                displayMasteryColor.copy(alpha = pulseAlpha * 0.45f),
                                                Color.Transparent
                                            ),
                                            center = nodeCenter,
                                            radius = haloRadius
                                        ),
                                        radius = haloRadius,
                                        center = nodeCenter
                                    )
                                }
                            }

                            // Selected Target Crosshairs
                            if (isSelected) {
                                drawCircle(
                                    color = CyanAccent,
                                    radius = nodeRadius + 15.dp.toPx(),
                                    center = nodeCenter,
                                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
                                )
                            }

                            // --- COLOR-CODED INDICATOR RINGS AROUND TOPICS ---
                            when (mastery) {
                                KnowledgeMasteryLevel.BEGINNER -> {
                                    // Beginner: 1 Warm Amber Indicator Ring around the topic
                                    val ring1Radius = nodeRadius + 4.dp.toPx() * zoomScale.coerceIn(0.85f, 1.3f)
                                    drawCircle(
                                        color = Color(0xFF1E293B).copy(alpha = if (isNodeVisibleInFilter) 0.8f else 0.3f),
                                        radius = ring1Radius,
                                        center = nodeCenter,
                                        style = Stroke(width = 2.8.dp.toPx())
                                    )
                                    val sweep = ((node.accuracyPercent / 100f).coerceIn(0.12f, 1f)) * 360f
                                    drawArc(
                                        color = displayMasteryColor,
                                        startAngle = -90f,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        topLeft = Offset(nodeCenter.x - ring1Radius, nodeCenter.y - ring1Radius),
                                        size = Size(ring1Radius * 2, ring1Radius * 2),
                                        style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    // 1 Indicator Notch at top
                                    drawCircle(
                                        color = displayMasteryColor,
                                        radius = 2.dp.toPx(),
                                        center = Offset(nodeCenter.x, nodeCenter.y - ring1Radius)
                                    )
                                }

                                KnowledgeMasteryLevel.INTERMEDIATE -> {
                                    // Intermediate: 2 Concentric Electric Cyan Indicator Rings around the topic
                                    val ring1Radius = nodeRadius + 3.5.dp.toPx() * zoomScale.coerceIn(0.85f, 1.3f)
                                    val ring2Radius = nodeRadius + 7.5.dp.toPx() * zoomScale.coerceIn(0.85f, 1.3f)

                                    // Inner Ring 1
                                    drawCircle(
                                        color = Color(0xFF1E293B).copy(alpha = if (isNodeVisibleInFilter) 0.8f else 0.3f),
                                        radius = ring1Radius,
                                        center = nodeCenter,
                                        style = Stroke(width = 2.2.dp.toPx())
                                    )
                                    val innerSweep = ((node.accuracyPercent / 100f).coerceIn(0.15f, 1f)) * 360f
                                    drawArc(
                                        color = displayMasteryColor,
                                        startAngle = -90f,
                                        sweepAngle = innerSweep,
                                        useCenter = false,
                                        topLeft = Offset(nodeCenter.x - ring1Radius, nodeCenter.y - ring1Radius),
                                        size = Size(ring1Radius * 2, ring1Radius * 2),
                                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Outer Concentric Ring 2 (Signature Dual Ring)
                                    drawCircle(
                                        color = displayMasteryColor.copy(alpha = if (isNodeVisibleInFilter) 0.7f else 0.2f),
                                        radius = ring2Radius,
                                        center = nodeCenter,
                                        style = Stroke(width = 1.6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f))
                                    )
                                    // 2 Indicator Notches (top & bottom)
                                    drawCircle(
                                        color = displayMasteryColor,
                                        radius = 2.dp.toPx(),
                                        center = Offset(nodeCenter.x, nodeCenter.y - ring2Radius)
                                    )
                                    drawCircle(
                                        color = displayMasteryColor,
                                        radius = 2.dp.toPx(),
                                        center = Offset(nodeCenter.x, nodeCenter.y + ring2Radius)
                                    )
                                }

                                KnowledgeMasteryLevel.EXPERT -> {
                                    // Expert: 3 Concentric Vivid Emerald Indicator Rings around the topic
                                    val ring1Radius = nodeRadius + 3.5.dp.toPx() * zoomScale.coerceIn(0.85f, 1.3f)
                                    val ring2Radius = nodeRadius + 7.2.dp.toPx() * zoomScale.coerceIn(0.85f, 1.3f)
                                    val ring3Radius = nodeRadius + 11.2.dp.toPx() * zoomScale.coerceIn(0.85f, 1.3f)

                                    // Ring 1 (Inner solid progress arc)
                                    drawCircle(
                                        color = Color(0xFF1E293B).copy(alpha = if (isNodeVisibleInFilter) 0.8f else 0.3f),
                                        radius = ring1Radius,
                                        center = nodeCenter,
                                        style = Stroke(width = 2.5.dp.toPx())
                                    )
                                    drawArc(
                                        color = displayMasteryColor,
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        topLeft = Offset(nodeCenter.x - ring1Radius, nodeCenter.y - ring1Radius),
                                        size = Size(ring1Radius * 2, ring1Radius * 2),
                                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Ring 2 (Middle concentric ring)
                                    drawCircle(
                                        color = displayMasteryColor.copy(alpha = if (isNodeVisibleInFilter) 0.85f else 0.25f),
                                        radius = ring2Radius,
                                        center = nodeCenter,
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )

                                    // Ring 3 (Outer crowning radiant ring)
                                    drawCircle(
                                        color = displayMasteryColor.copy(alpha = if (isNodeVisibleInFilter) 0.9f else 0.3f),
                                        radius = ring3Radius,
                                        center = nodeCenter,
                                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f))
                                    )

                                    // 3 Indicator Star Notches
                                    listOf(-90.0, 30.0, 150.0).forEach { deg ->
                                        val rad = Math.toRadians(deg)
                                        val nx = nodeCenter.x + ring3Radius * cos(rad).toFloat()
                                        val ny = nodeCenter.y + ring3Radius * sin(rad).toFloat()
                                        drawCircle(
                                            color = displayMasteryColor,
                                            radius = 2.2.dp.toPx(),
                                            center = Offset(nx, ny)
                                        )
                                    }
                                }
                            }

                            // Center Circle Disc
                            drawCircle(
                                color = if (isSelected) displayMasteryColor else Color(0xFF0F172A),
                                radius = nodeRadius,
                                center = nodeCenter
                            )
                            drawCircle(
                                color = displayMasteryColor,
                                radius = nodeRadius,
                                center = nodeCenter,
                                style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 1.5.dp.toPx())
                            )

                            // Inner Category Monogram inside Node
                            val monoStyle = TextStyle(
                                color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                fontSize = (10 * zoomScale.coerceIn(0.85f, 1.25f)).sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            val monoResult = textMeasurer.measure(node.iconMonogram, style = monoStyle)
                            drawText(
                                textLayoutResult = monoResult,
                                topLeft = Offset(nodeCenter.x - monoResult.size.width / 2f, nodeCenter.y - monoResult.size.height / 2f)
                            )

                            // Render Title Label Below Node
                            val labelStyle = TextStyle(
                                color = if (isNodeVisibleInFilter) Color.White else Color(0xFF64748B),
                                fontSize = (11 * zoomScale.coerceIn(0.85f, 1.2f)).sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif
                            )
                            val labelResult = textMeasurer.measure(node.shortLabel, style = labelStyle)
                            drawText(
                                textLayoutResult = labelResult,
                                topLeft = Offset(nodeCenter.x - labelResult.size.width / 2f, nodeCenter.y + nodeRadius + 8.dp.toPx())
                            )

                            // Render Color-Coded Mastery Level Badge Below Title (e.g. [🥉 Beg 42%], [🥈 Int 70%], [🥇 Exp 92%])
                            val masteryBadgeText = when (mastery) {
                                KnowledgeMasteryLevel.BEGINNER -> if (node.attemptsCount == 0) "🥉 Beg • New" else "🥉 Beg ${node.accuracyPercent.toInt()}%"
                                KnowledgeMasteryLevel.INTERMEDIATE -> "🥈 Int ${node.accuracyPercent.toInt()}%"
                                KnowledgeMasteryLevel.EXPERT -> "🥇 Exp ${node.accuracyPercent.toInt()}%"
                            }
                            val badgeStyle = TextStyle(
                                color = displayMasteryColor,
                                fontSize = (9 * zoomScale.coerceIn(0.8f, 1.15f)).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            val badgeResult = textMeasurer.measure(masteryBadgeText, style = badgeStyle)
                            val badgePadX = 5.dp.toPx()
                            val badgePadY = 2.dp.toPx()
                            val badgeW = badgeResult.size.width + badgePadX * 2
                            val badgeH = badgeResult.size.height + badgePadY * 2
                            val badgeTopLeft = Offset(
                                nodeCenter.x - badgeW / 2f,
                                nodeCenter.y + nodeRadius + 10.dp.toPx() + labelResult.size.height
                            )

                            drawRoundRect(
                                color = Color(0xFF090E1A).copy(alpha = 0.92f),
                                topLeft = badgeTopLeft,
                                size = Size(badgeW, badgeH),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                            drawRoundRect(
                                color = displayMasteryColor.copy(alpha = 0.5f),
                                topLeft = badgeTopLeft,
                                size = Size(badgeW, badgeH),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                style = Stroke(width = 0.8.dp.toPx())
                            )
                            drawText(
                                textLayoutResult = badgeResult,
                                topLeft = Offset(badgeTopLeft.x + badgePadX, badgeTopLeft.y + badgePadY)
                            )
                        }
                    }

                    // Floating Canvas Overlay Controls (Top-Right HUD: Zoom +, Zoom -, Reset)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = { zoomScale = (zoomScale * 1.25f).coerceIn(0.65f, 2.8f) },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("graph_zoom_in_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { zoomScale = (zoomScale / 1.25f).coerceIn(0.65f, 2.8f) },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("graph_zoom_out_btn")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    zoomScale = 1f
                                    panOffset = Offset.Zero
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("graph_zoom_reset_btn")
                            ) {
                                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset View", tint = CyanAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Floating Persistent Mastery Indicator Rings Legend Bar (Bottom-Start)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF090E1A).copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "RINGS:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF64748B)
                            )
                            // Beginner (1 Ring)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.Transparent, CircleShape)
                                        .border(1.6.dp, KnowledgeMasteryLevel.BEGINNER.ringColor, CircleShape)
                                )
                                Text("Beg (<60%)", fontSize = 10.sp, color = KnowledgeMasteryLevel.BEGINNER.ringColor, fontWeight = FontWeight.Bold)
                            }
                            // Intermediate (2 Rings)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.Transparent, CircleShape)
                                        .border(2.dp, KnowledgeMasteryLevel.INTERMEDIATE.ringColor, CircleShape)
                                )
                                Text("Int (60-79%)", fontSize = 10.sp, color = KnowledgeMasteryLevel.INTERMEDIATE.ringColor, fontWeight = FontWeight.Bold)
                            }
                            // Expert (3 Rings)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(KnowledgeMasteryLevel.EXPERT.ringColor.copy(alpha = 0.25f), CircleShape)
                                        .border(2.dp, KnowledgeMasteryLevel.EXPERT.ringColor, CircleShape)
                                )
                                Text("Exp (≥80%)", fontSize = 10.sp, color = KnowledgeMasteryLevel.EXPERT.ringColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Floating Interactive Instruction Hint
                    if (selectedNode == null) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Drag to explore • Pinch to zoom • Tap node to inspect",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }

            // Node Detail Drill-down Card (when a concept node is tapped)
            AnimatedVisibility(
                visible = selectedNode != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedNode?.let { node ->
                    NodeDetailCard(
                        node = node,
                        edges = data.edges,
                        allNodes = data.nodes,
                        onClose = { selectedNode = null },
                        onSelectRelatedNode = { relatedId ->
                            val target = data.nodes.firstOrNull { it.id == relatedId }
                            selectedNode = target
                            target?.let { t ->
                                val targetX = if (viewMode == GraphViewMode.ROADMAP) t.normRoadmapX else t.normX
                                val targetY = if (viewMode == GraphViewMode.ROADMAP) t.normRoadmapY else t.normY
                                panOffset = Offset(
                                    x = (0.5f - targetX) * 200f,
                                    y = (0.5f - targetY) * 200f
                                )
                            }
                        },
                        onStartAdaptiveQuiz = {
                            adaptiveQuizNode = node
                            showAdaptiveQuizSheet = true
                        },
                        onStartDomainQuiz = {
                            onStartQuiz?.invoke(node.category, node.categoryNumber)
                        },
                        onOpenQaChat = onOpenQaChat
                    )
                }
            }

            // 30-Day Mastery Improvement Trends Section (Recharts Visualization)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF070B14),
                border = BorderStroke(1.dp, if (showMasteryTrends) CyanAccent.copy(alpha = 0.6f) else Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMasteryTrends = !showMasteryTrends }
                    .testTag("graph_mastery_trends_card_header")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "30-Day Mastery Improvement Trends",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Recharts visualization • Progression curve & tier shifts",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (showMasteryTrends) CyanAccent else Color(0xFF1E293B)
                    ) {
                        Text(
                            text = if (showMasteryTrends) "Hide Recharts ▲" else "View Recharts ▼",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showMasteryTrends) Color(0xFF0F172A) else CyanAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showMasteryTrends) {
                Recharts30DayMasteryTrendCard(
                    attempts = recentAttempts,
                    currentMasteryPercent = data.averageMasteryPercent,
                    expertTopicsCount = data.expertCount,
                    intermediateTopicsCount = data.intermediateCount,
                    beginnerTopicsCount = data.beginnerCount,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Graph Legend Dialog
    if (showLegendDialog) {
        AlertDialog(
            onDismissRequest = { showLegendDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Hub, contentDescription = null, tint = CyanAccent)
                    Text("Knowledge Graph Legend", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "The Knowledge Graph maps curriculum concepts and their directional dependencies using your adaptive quiz telemetry.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    // Section 1: Color-coded indicator rings for mastery levels
                    Text(
                        text = "TOPIC MASTERY INDICATOR RINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanAccent
                    )

                    // Beginner (Amber Single Ring)
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.Transparent, CircleShape)
                                .border(2.dp, KnowledgeMasteryLevel.BEGINNER.ringColor, CircleShape)
                        )
                        Column {
                            Text(
                                text = "🥉 Beginner (Single Ring • Warm Amber)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KnowledgeMasteryLevel.BEGINNER.ringColor
                            )
                            Text(
                                text = "Foundational proficiency (<60% accuracy or untested). Single amber indicator ring.",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Intermediate (Cyan Dual Ring)
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.Transparent, CircleShape)
                                .border(2.5.dp, KnowledgeMasteryLevel.INTERMEDIATE.ringColor, CircleShape)
                        )
                        Column {
                            Text(
                                text = "🥈 Intermediate (Dual Ring • Electric Cyan)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KnowledgeMasteryLevel.INTERMEDIATE.ringColor
                            )
                            Text(
                                text = "Solid comprehension (60%–79% accuracy). Concentric dual cyan indicator rings.",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Expert (Emerald Triple Ring)
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(KnowledgeMasteryLevel.EXPERT.ringColor.copy(alpha = 0.25f), CircleShape)
                                .border(2.5.dp, KnowledgeMasteryLevel.EXPERT.ringColor, CircleShape)
                        )
                        Column {
                            Text(
                                text = "🥇 Expert (Triple Ring & Aura • Vivid Emerald)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KnowledgeMasteryLevel.EXPERT.ringColor
                            )
                            Text(
                                text = "Mastery achieved (≥80% accuracy). Triple concentric rings with radiant crowning aura.",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "TELEMETRY STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )

                    TopicMasteryStatus.values().forEach { status ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = status.color,
                                modifier = Modifier.size(12.dp)
                            ) {}
                            Column {
                                Text(
                                    text = "${status.iconEmoji} ${status.label}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = status.color
                                )
                                Text(
                                    text = status.description,
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Edges & Flow: Animated pulses show prerequisite flow. Tap any node to highlight unlocked branches and prerequisites.",
                        fontSize = 11.sp,
                        color = CyanAccent
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLegendDialog = false }) {
                    Text("Close", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            },
        )
    }

    // Voice-to-Text Learning Summary Sheet & Graph Connection Suggester
    if (showVoiceSummarySheet) {
        VoiceLearningSummarySheet(
            username = username,
            allNodes = data.nodes,
            existingEdges = data.edges,
            dbHelper = dbHelper,
            onDismiss = { showVoiceSummarySheet = false },
            onConnectionsUpdated = {
                scope.launch {
                    graphData = KnowledgeGraphEngine.buildKnowledgeGraph(username, dbHelper)
                }
            }
        )
    }

    // Spaced Repetition Reinforcement Sheet (Calibrated by Knowledge Graph Mastery)
    if (showReinforcementSheet) {
        KnowledgeReinforcementSheet(
            username = username,
            allNodes = data.nodes,
            existingEdges = data.edges,
            dbHelper = dbHelper,
            onDismiss = { showReinforcementSheet = false },
            onStartReview = { catName, catNum ->
                showReinforcementSheet = false
                onStartQuiz?.invoke(catName, catNum)
            }
        )
    }

    // Real-Time Knowledge Graph Adaptive Quiz Sheet
    if (showAdaptiveQuizSheet && adaptiveQuizNode != null) {
        val currentUser = remember(username) { User(id = 1L, username = username, createdAt = System.currentTimeMillis()) }
        KnowledgeGraphAdaptiveQuizSheet(
            node = adaptiveQuizNode!!,
            graphData = data,
            user = currentUser,
            dbHelper = dbHelper,
            onDismiss = {
                showAdaptiveQuizSheet = false
                adaptiveQuizNode = null
            },
            onQuizFinished = { _, _ ->
                showAdaptiveQuizSheet = false
                adaptiveQuizNode = null
                scope.launch {
                    graphData = KnowledgeGraphEngine.buildKnowledgeGraph(username, dbHelper)
                    recentAttempts = dbHelper.getRecentQuizAttempts(username, 30)
                }
            }
        )
    }
}

/**
 * Draws a directional arrow chevron along an edge at a given fraction.
 */
private fun DrawScope.drawArrowChevron(
    from: Offset,
    to: Offset,
    color: Color,
    fraction: Float = 0.62f,
    arrowSize: Float = 12f
) {
    val px = from.x + (to.x - from.x) * fraction
    val py = from.y + (to.y - from.y) * fraction
    val angle = atan2((to.y - from.y).toDouble(), (to.x - from.x).toDouble())
    val arrowAngle = Math.PI / 6.0 // 30 deg

    val x1 = px - arrowSize * cos(angle - arrowAngle).toFloat()
    val y1 = py - arrowSize * sin(angle - arrowAngle).toFloat()
    val x2 = px - arrowSize * cos(angle + arrowAngle).toFloat()
    val y2 = py - arrowSize * sin(angle + arrowAngle).toFloat()

    val path = Path().apply {
        moveTo(x1, y1)
        lineTo(px, py)
        lineTo(x2, y2)
    }
    drawPath(path, color, style = Stroke(width = 2.2f, cap = StrokeCap.Round))
}

@Composable
private fun MetricPill(
    label: String,
    count: String,
    color: Color,
    emoji: String,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.20f) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, color) else null,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = emoji, fontSize = 11.sp)
                Text(text = count, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isSelected) color else Color(0xFF94A3B8),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NodeDetailCard(
    node: KnowledgeTopicNode,
    edges: List<KnowledgeEdge>,
    allNodes: List<KnowledgeTopicNode>,
    onClose: () -> Unit,
    onSelectRelatedNode: (String) -> Unit,
    onStartAdaptiveQuiz: () -> Unit,
    onStartDomainQuiz: () -> Unit,
    onOpenQaChat: (() -> Unit)?
) {
    val nodeMap = remember(allNodes) { allNodes.associateBy { it.id } }
    val prerequisites = remember(node.id, edges) {
        edges.filter { it.toId == node.id }.mapNotNull { edge ->
            nodeMap[edge.fromId]?.let { fromNode -> edge to fromNode }
        }
    }
    val unlockedTopics = remember(node.id, edges) {
        edges.filter { it.fromId == node.id }.mapNotNull { edge ->
            nodeMap[edge.toId]?.let { toNode -> edge to toNode }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090E1A)),
        border = BorderStroke(1.dp, node.status.color.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("node_details_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${node.clusterName} • Category: ${node.category} (Tier ${node.tierLevel})",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = node.status.color.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, node.status.color)
                    ) {
                        Text(
                            text = "${node.status.iconEmoji} ${node.status.label}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = node.status.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close details",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Color-Coded Mastery Level & Indicator Ring Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = node.masteryLevel.ringColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, node.masteryLevel.ringColor.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF090E1A), CircleShape)
                            .border(
                                width = (node.masteryLevel.ringCount * 1.5).dp,
                                color = node.masteryLevel.ringColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(node.masteryLevel.iconEmoji, fontSize = 15.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${node.masteryLevel.label} Level",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = node.masteryLevel.ringColor
                            )
                            Text(
                                text = "• ${node.masteryLevel.ringCount} Indicator Ring${if (node.masteryLevel.ringCount > 1) "s" else ""}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                        Text(
                            text = node.masteryLevel.description,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Description
            Text(
                text = node.description,
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 16.sp
            )

            // Mastery Progress Indicator
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Proficiency Mastery: ${node.accuracyPercent.toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = node.status.color
                    )
                    Text(
                        text = "${node.attemptsCount} quizzes completed",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                LinearProgressIndicator(
                    progress = { (node.accuracyPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = node.status.color,
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Key Concepts Tags
            Text(
                text = "CORE CONCEPTS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                node.keyConcepts.forEach { concept ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(0.5.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = concept,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Prerequisite Topics Chain
            if (prerequisites.isNotEmpty()) {
                Text(
                    text = "PREREQUISITES REQUIRED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = AmberAccent
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    prerequisites.forEach { (edge, prereqNode) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, prereqNode.status.color.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { onSelectRelatedNode(prereqNode.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${prereqNode.status.iconEmoji} ${prereqNode.title}",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "(${edge.relationship})",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            // Unlocks Next Chain
            if (unlockedTopics.isNotEmpty()) {
                Text(
                    text = "UNLOCKS NEXT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = EmeraldSuccess
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    unlockedTopics.forEach { (edge, nextNode) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, nextNode.status.color.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { onSelectRelatedNode(nextNode.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${nextNode.status.iconEmoji} ${nextNode.title}",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "(${edge.relationship})",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons (Real-Time Knowledge Graph Adaptive Quiz, Domain Quiz, Ask Tutor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartAdaptiveQuiz,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("graph_start_adaptive_quiz_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Live Adaptive Quiz",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                OutlinedButton(
                    onClick = onStartDomainQuiz,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("graph_start_quiz_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Domain Quiz",
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                onOpenQaChat?.let { openTutor ->
                    OutlinedButton(
                        onClick = openTutor,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF6366F1)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ask Tutor",
                            color = Color(0xFF818CF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
