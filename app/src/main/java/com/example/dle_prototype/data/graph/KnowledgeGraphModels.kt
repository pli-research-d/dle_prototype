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

enum class KnowledgeMasteryLevel(
    val label: String,
    val ringColor: Color,
    val outerGlowColor: Color,
    val ringCount: Int,
    val minAccuracy: Float,
    val iconEmoji: String,
    val badgeText: String,
    val description: String
) {
    BEGINNER(
        label = "Beginner",
        ringColor = Color(0xFFF59E0B), // Warm Amber / Bronze
        outerGlowColor = Color(0xFFD97706),
        ringCount = 1,
        minAccuracy = 0f,
        iconEmoji = "🥉",
        badgeText = "Beginner",
        description = "Foundational proficiency (<60% accuracy or starting out). Core syntax & basic principles."
    ),
    INTERMEDIATE(
        label = "Intermediate",
        ringColor = Color(0xFF06B6D4), // Cyan / Electric Blue
        outerGlowColor = Color(0xFF0284C7),
        ringCount = 2,
        minAccuracy = 60f,
        iconEmoji = "🥈",
        badgeText = "Intermediate",
        description = "Competent proficiency (60%–79% accuracy). Strong understanding of core mechanics."
    ),
    EXPERT(
        label = "Expert",
        ringColor = Color(0xFF10B981), // Emerald / Jade Green
        outerGlowColor = Color(0xFF059669),
        ringCount = 3,
        minAccuracy = 80f,
        iconEmoji = "🥇",
        badgeText = "Expert",
        description = "Mastery achieved (≥80% accuracy). High velocity recall, fluency & deep retention."
    );

    companion object {
        fun fromAccuracyAndAttempts(accuracyPercent: Float, attemptsCount: Int): KnowledgeMasteryLevel {
            return when {
                attemptsCount == 0 -> BEGINNER
                accuracyPercent >= 80f -> EXPERT
                accuracyPercent >= 60f -> INTERMEDIATE
                else -> BEGINNER
            }
        }
    }
}

data class KnowledgeCluster(
    val name: String,
    val color: Color,
    val normCenterX: Float,
    val normCenterY: Float,
    val description: String
)

enum class GraphViewMode(val title: String, val iconEmoji: String) {
    CONSTELLATION("Constellation Map", "🌌"),
    ROADMAP("Curriculum Roadmap", "🗺️")
}

data class KnowledgeTopicNode(
    val id: String,
    val title: String,
    val category: String,
    val categoryNumber: Float,
    val accuracyPercent: Float,
    val attemptsCount: Int,
    val status: TopicMasteryStatus,
    val normX: Float, // 0f..1f normalized relative position in Constellation mode
    val normY: Float, // 0f..1f normalized relative position in Constellation mode
    val description: String,
    val keyConcepts: List<String>,
    val clusterName: String,
    val shortLabel: String = title.take(8),
    val iconMonogram: String = title.take(2).uppercase(),
    val tierLevel: Int = 1,
    val normRoadmapX: Float = normX,
    val normRoadmapY: Float = normY,
    val masteryLevel: KnowledgeMasteryLevel = KnowledgeMasteryLevel.fromAccuracyAndAttempts(accuracyPercent, attemptsCount)
)

data class KnowledgeEdge(
    val fromId: String,
    val toId: String,
    val relationship: String,
    val isPrerequisite: Boolean = true,
    val isCustom: Boolean = false,
    val reasoning: String? = null
)

data class KnowledgeGraphData(
    val nodes: List<KnowledgeTopicNode>,
    val edges: List<KnowledgeEdge>,
    val strengthsCount: Int,
    val solidifyingCount: Int,
    val reviewCount: Int,
    val unexploredCount: Int,
    val averageMasteryPercent: Float,
    val clusters: List<KnowledgeCluster> = emptyList(),
    val recommendedNextTopic: KnowledgeTopicNode? = null,
    val beginnerCount: Int = nodes.count { it.masteryLevel == KnowledgeMasteryLevel.BEGINNER },
    val intermediateCount: Int = nodes.count { it.masteryLevel == KnowledgeMasteryLevel.INTERMEDIATE },
    val expertCount: Int = nodes.count { it.masteryLevel == KnowledgeMasteryLevel.EXPERT }
)

data class MasteryTrendDayPoint(
    val dayNumber: Int,
    val dateLabel: String,
    val masteryScore: Float,
    val accuracyPercent: Float,
    val expertTopicsCount: Int,
    val intermediateTopicsCount: Int,
    val beginnerTopicsCount: Int,
    val xpEarned: Int,
    val quizzesTaken: Int,
    val focusMinutes: Int,
    val highlightMilestone: String? = null,
    val isToday: Boolean = false
)

data class MasteryTrends30DaySummary(
    val points: List<MasteryTrendDayPoint>,
    val startMastery: Float,
    val currentMastery: Float,
    val growthDelta: Float,
    val currentExpertCount: Int,
    val currentIntermediateCount: Int,
    val currentBeginnerCount: Int,
    val total30DayXp: Int,
    val total30DayQuizzes: Int,
    val averageAccuracy: Float
)

data class SuggestedKnowledgeConnection(
    val id: String,
    val fromNodeId: String,
    val toNodeId: String,
    val fromNodeTitle: String,
    val toNodeTitle: String,
    val relationshipTitle: String,
    val reasoning: String,
    val confidencePercent: Int,
    val matchedKeywords: List<String>,
    val isAlreadyConnected: Boolean = false,
    val isAccepted: Boolean = false
)

data class VoiceSummaryAnalysisResult(
    val transcript: String,
    val detectedTopics: List<KnowledgeTopicNode>,
    val suggestions: List<SuggestedKnowledgeConnection>,
    val keyInsights: List<String>
)

data class VoiceSummaryRecord(
    val id: Long,
    val username: String,
    val transcript: String,
    val timestamp: Long,
    val suggestedCount: Int
)

data class KnowledgeReinforcementSchedule(
    val nodeId: String,
    val title: String,
    val category: String,
    val categoryNumber: Float,
    val masteryLevel: KnowledgeMasteryLevel,
    val status: TopicMasteryStatus,
    val accuracyPercent: Float,
    val attemptsCount: Int,
    val retentionProbability: Float,
    val scheduledIntervalDays: Float,
    val lastReviewedTimestamp: Long,
    val nextReviewTimestamp: Long,
    val isOverdue: Boolean,
    val dueStatusText: String,
    val urgencyPriorityScore: Float,
    val prerequisiteImpactCount: Int,
    val reinforcementReason: String,
    val recommendedAction: String
)

data class KnowledgeSpacedRepetitionSummary(
    val schedules: List<KnowledgeReinforcementSchedule>,
    val dueNowCount: Int,
    val criticalCount: Int,
    val averageRetentionPercent: Int,
    val mostUrgentSchedule: KnowledgeReinforcementSchedule?,
    val dependentPrereqAlertsCount: Int
)

