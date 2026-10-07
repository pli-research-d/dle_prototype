package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.graph.KnowledgeEdge
import com.example.dle_prototype.data.graph.KnowledgeGraphData
import com.example.dle_prototype.data.graph.KnowledgeGraphEngine
import com.example.dle_prototype.data.graph.KnowledgeTopicNode
import com.example.dle_prototype.data.graph.TopicMasteryStatus
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import kotlin.math.sqrt

enum class GraphFilterMode(val label: String) {
    ALL("All Concepts"),
    STRENGTHS("Strengths 🚀"),
    REVIEW("Needs Review ⚠️"),
    SOLIDIFYING("Solidifying ⚡")
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

    val density = LocalDensity.current

    // Pulsing halo animation for nodes needing review and top strengths
    val infiniteTransition = rememberInfiniteTransition(label = "halo_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    LaunchedEffect(username) {
        graphData = KnowledgeGraphEngine.buildKnowledgeGraph(username, dbHelper)
    }

    val data = graphData ?: return

    val filteredNodes = remember(data.nodes, filterMode) {
        when (filterMode) {
            GraphFilterMode.ALL -> data.nodes
            GraphFilterMode.STRENGTHS -> data.nodes.filter { it.status == TopicMasteryStatus.STRENGTH }
            GraphFilterMode.REVIEW -> data.nodes.filter { it.status == TopicMasteryStatus.NEEDS_REVIEW }
            GraphFilterMode.SOLIDIFYING -> data.nodes.filter { it.status == TopicMasteryStatus.SOLIDIFYING }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visual_knowledge_graph_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
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
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = "Knowledge Graph Icon",
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
                                text = "Visual Knowledge Graph",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Adaptive",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Interactive topological map of your learning history",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "${data.averageMasteryPercent.toInt()}% Avg Mastery",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Metric Summary Row (Strengths · Solidifying · Review · Unexplored)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090E1A), RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricPill("Strengths", "${data.strengthsCount}", EmeraldSuccess, "🚀")
                MetricPill("Solidifying", "${data.solidifyingCount}", CyanAccent, "⚡")
                MetricPill("Needs Review", "${data.reviewCount}", AmberAccent, "⚠️")
                MetricPill("Unexplored", "${data.unexploredCount}", Color(0xFF64748B), "🌱")
            }

            // Filter Chips Carousel
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
            }

            // Interactive 2D Canvas Surface
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF030712),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("knowledge_graph_canvas")
                            .pointerInput(data.nodes) {
                                detectTapGestures { tapOffset ->
                                    val tapRadiusPx = with(density) { 36.dp.toPx() }
                                    // Find closest node to tap point
                                    var closestNode: KnowledgeTopicNode? = null
                                    var minDistance = Float.MAX_VALUE

                                    for (node in data.nodes) {
                                        val nodeX = node.normX * size.width
                                        val nodeY = node.normY * size.height
                                        val dx = tapOffset.x - nodeX
                                        val dy = tapOffset.y - nodeY
                                        val dist = sqrt(dx * dx + dy * dy)
                                        if (dist < tapRadiusPx && dist < minDistance) {
                                            minDistance = dist
                                            closestNode = node
                                        }
                                    }
                                    selectedNode = closestNode
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // 1. Draw Subtle Constellation Dot Grid
                        val dotSpacing = 32.dp.toPx()
                        var x = dotSpacing / 2
                        while (x < canvasWidth) {
                            var y = dotSpacing / 2
                            while (y < canvasHeight) {
                                drawCircle(
                                    color = Color(0xFF1E293B).copy(alpha = 0.35f),
                                    radius = 1.2f,
                                    center = Offset(x, y)
                                )
                                y += dotSpacing
                            }
                            x += dotSpacing
                        }

                        // 2. Draw Knowledge Edges (Connections)
                        val nodeMap = data.nodes.associateBy { it.id }
                        data.edges.forEach { edge ->
                            val fromNode = nodeMap[edge.fromId]
                            val toNode = nodeMap[edge.toId]

                            if (fromNode != null && toNode != null) {
                                val start = Offset(fromNode.normX * canvasWidth, fromNode.normY * canvasHeight)
                                val end = Offset(toNode.normX * canvasWidth, toNode.normY * canvasHeight)

                                val isBothStrengths = fromNode.status == TopicMasteryStatus.STRENGTH &&
                                        toNode.status == TopicMasteryStatus.STRENGTH
                                val hasReview = fromNode.status == TopicMasteryStatus.NEEDS_REVIEW ||
                                        toNode.status == TopicMasteryStatus.NEEDS_REVIEW

                                val lineColor = when {
                                    isBothStrengths -> EmeraldSuccess.copy(alpha = 0.6f)
                                    hasReview -> AmberAccent.copy(alpha = 0.5f)
                                    else -> Color(0xFF334155).copy(alpha = 0.45f)
                                }

                                val strokeWidth = if (isBothStrengths) 2.5f else 1.5f

                                val pathEffect = if (hasReview) {
                                    PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                } else null

                                drawLine(
                                    color = lineColor,
                                    start = start,
                                    end = end,
                                    strokeWidth = strokeWidth,
                                    pathEffect = pathEffect
                                )
                            }
                        }

                        // 3. Draw Nodes (Constellation Stars)
                        data.nodes.forEach { node ->
                            val isNodeVisibleInFilter = filteredNodes.contains(node)
                            val isSelected = selectedNode?.id == node.id
                            val nodeCenter = Offset(node.normX * canvasWidth, node.normY * canvasHeight)

                            val nodeRadius = if (isSelected) 22.dp.toPx() else 17.dp.toPx()
                            val haloRadius = nodeRadius + (if (node.status == TopicMasteryStatus.NEEDS_REVIEW) 12.dp.toPx() else 7.dp.toPx())

                            val baseColor = node.status.color
                            val displayColor = if (isNodeVisibleInFilter) baseColor else baseColor.copy(alpha = 0.25f)

                            // Outer Glow Halo for active or selected nodes
                            if (isNodeVisibleInFilter && (node.status == TopicMasteryStatus.NEEDS_REVIEW || isSelected)) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            displayColor.copy(alpha = pulseAlpha * 0.4f),
                                            Color.Transparent
                                        ),
                                        center = nodeCenter,
                                        radius = haloRadius
                                    ),
                                    radius = haloRadius,
                                    center = nodeCenter
                                )
                            }

                            // Mastery Progress Ring (Arc around node)
                            val ringRadius = nodeRadius + 3.dp.toPx()
                            drawCircle(
                                color = Color(0xFF1E293B),
                                radius = ringRadius,
                                center = nodeCenter,
                                style = Stroke(width = 2.5.dp.toPx())
                            )

                            if (node.accuracyPercent > 0f) {
                                val sweepAngle = (node.accuracyPercent / 100f) * 360f
                                drawArc(
                                    color = displayColor,
                                    startAngle = -90f,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = Offset(nodeCenter.x - ringRadius, nodeCenter.y - ringRadius),
                                    size = androidx.compose.ui.geometry.Size(ringRadius * 2, ringRadius * 2),
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                            }

                            // Center Circle
                            drawCircle(
                                color = if (isSelected) displayColor else Color(0xFF0F172A),
                                radius = nodeRadius,
                                center = nodeCenter
                            )

                            drawCircle(
                                color = displayColor,
                                radius = nodeRadius,
                                center = nodeCenter,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
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
                                    text = "Tap any node to inspect mastery & practice prerequisites",
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
                            selectedNode = data.nodes.firstOrNull { it.id == relatedId }
                        },
                        onStartQuiz = {
                            onStartQuiz?.invoke(node.category, node.categoryNumber)
                        },
                        onOpenQaChat = onOpenQaChat
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, count: String, color: Color, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = emoji, fontSize = 11.sp)
            Text(text = count, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
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
    onStartQuiz: () -> Unit,
    onOpenQaChat: (() -> Unit)?
) {
    val nodeMap = remember(allNodes) { allNodes.associateBy { it.id } }
    val connectedEdges = remember(node.id, edges) {
        edges.filter { it.fromId == node.id || it.toId == node.id }
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
                        text = "${node.clusterName} • Category: ${node.category}",
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

            // Connected Concepts in Graph
            if (connectedEdges.isNotEmpty()) {
                Text(
                    text = "TOPOLOGICAL CONNECTIONS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    connectedEdges.forEach { edge ->
                        val targetId = if (edge.fromId == node.id) edge.toId else edge.fromId
                        val targetNode = nodeMap[targetId]
                        if (targetNode != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, targetNode.status.color.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { onSelectRelatedNode(targetId) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${targetNode.status.iconEmoji} ${targetNode.title}",
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
            }

            // Action Buttons (Start Quiz or Ask Tutor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartQuiz,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("graph_start_quiz_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Practice Adaptive Quiz",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
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
