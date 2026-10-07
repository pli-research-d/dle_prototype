package com.example.dle_prototype.data.graph

import androidx.compose.ui.graphics.Color
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess

enum class TopicMasteryStatus(
    val label: String,
    val color: Color,
    val iconEmoji: String,
    val description: String
) {
    STRENGTH(
        label = "Strength",
        color = EmeraldSuccess,
        iconEmoji = "🚀",
        description = "High proficiency (≥75% accuracy). Ready for hard adaptive challenges."
    ),
    SOLIDIFYING(
        label = "Solidifying",
        color = CyanAccent,
        iconEmoji = "⚡",
        description = "Moderate proficiency (50%-74% accuracy). Needs consistent reinforcement."
    ),
    NEEDS_REVIEW(
        label = "Needs Review",
        color = AmberAccent,
        iconEmoji = "⚠️",
        description = "Areas for improvement (<50% accuracy or recent misses). Prioritize review."
    ),
    UNEXPLORED(
        label = "Unexplored",
        color = Color(0xFF64748B),
        iconEmoji = "🌱",
        description = "No quiz attempts recorded yet. Start a baseline quiz to map skill radar."
    )
}

data class KnowledgeTopicNode(
    val id: String,
    val title: String,
    val category: String,
    val categoryNumber: Float,
    val accuracyPercent: Float,
    val attemptsCount: Int,
    val status: TopicMasteryStatus,
    val normX: Float, // 0f..1f normalized relative position
    val normY: Float, // 0f..1f normalized relative position
    val description: String,
    val keyConcepts: List<String>,
    val clusterName: String
)

data class KnowledgeEdge(
    val fromId: String,
    val toId: String,
    val relationship: String
)

data class KnowledgeGraphData(
    val nodes: List<KnowledgeTopicNode>,
    val edges: List<KnowledgeEdge>,
    val strengthsCount: Int,
    val solidifyingCount: Int,
    val reviewCount: Int,
    val unexploredCount: Int,
    val averageMasteryPercent: Float
)
