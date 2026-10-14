package com.example.dle_prototype.data.graph

import androidx.compose.ui.graphics.Color
import com.example.dle_prototype.data.badges.BadgeTier
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent

/**
 * Definition for a Knowledge Graph Expert Mastery Badge that unlocks
 * an exclusive profile icon / avatar symbol when the user achieves 'Expert'
 * mastery in a defined set of interconnected topics within the Knowledge Graph.
 */
data class KnowledgeExpertBadgeDefinition(
    val id: String,
    val badgeTitle: String,
    val unlockedIconEmoji: String,
    val unlockedIconName: String,
    val description: String,
    val requiredTopicIds: List<String>, // Interconnected topic node IDs in Knowledge Graph
    val requiredExpertCount: Int = requiredTopicIds.size, // Required number of expert nodes
    val clusterName: String,
    val tier: BadgeTier,
    val xpReward: Int,
    val accentColor: Color,
    val rarityText: String
)

data class TopicMasteryDetail(
    val topicId: String,
    val title: String,
    val accuracyPercent: Float,
    val attemptsCount: Int,
    val isExpert: Boolean,
    val status: TopicMasteryStatus,
    val masteryLevel: KnowledgeMasteryLevel
)

data class KnowledgeExpertBadgeStatus(
    val definition: KnowledgeExpertBadgeDefinition,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null,
    val isEquipped: Boolean = false,
    val expertTopicsCount: Int,
    val totalRequiredTopicsCount: Int,
    val topicProgress: List<TopicMasteryDetail>,
    val progressFraction: Float // 0.0f..1.0f
)

object KnowledgeExpertBadgeCatalog {
    val ALL_DEFINITIONS: List<KnowledgeExpertBadgeDefinition> = listOf(
        KnowledgeExpertBadgeDefinition(
            id = "kg_expert_web_architect",
            badgeTitle = "Web Architect",
            unlockedIconEmoji = "🌐",
            unlockedIconName = "Web Nexus",
            description = "Master the core Frontend Web Architecture triad (HTML5 Foundations, CSS Modern Layouts, and JavaScript & ES6+).",
            requiredTopicIds = listOf("html", "css", "js"),
            clusterName = "Web Architecture",
            tier = BadgeTier.GOLD,
            xpReward = 300,
            accentColor = CyanAccent,
            rarityText = "Expert · Triad Master"
        ),
        KnowledgeExpertBadgeDefinition(
            id = "kg_expert_stack_dynamo",
            badgeTitle = "Full-Stack Dynamo",
            unlockedIconEmoji = "🐬",
            unlockedIconName = "Database Titan",
            description = "Master the interconnected client-to-database data pipeline (JavaScript & ES6+, PHP Server Runtime, and MySQL Relational Data).",
            requiredTopicIds = listOf("js", "php", "mysql"),
            clusterName = "Backend & Database",
            tier = BadgeTier.PLATINUM,
            xpReward = 450,
            accentColor = Color(0xFF8B5CF6),
            rarityText = "Expert · Pipeline Master"
        ),
        KnowledgeExpertBadgeDefinition(
            id = "kg_expert_reactive_luminary",
            badgeTitle = "Reactive Luminary",
            unlockedIconEmoji = "📱",
            unlockedIconName = "Mobile Luminary",
            description = "Master reactive mobile state flow and declarative UI (Jetpack Compose UI, Kotlin Coroutines & Flow, and On-Device TFLite).",
            requiredTopicIds = listOf("compose", "coroutines", "tflite"),
            clusterName = "Mobile & Reactive",
            tier = BadgeTier.PLATINUM,
            xpReward = 500,
            accentColor = EmeraldSuccess,
            rarityText = "Expert · Concurrency Master"
        ),
        KnowledgeExpertBadgeDefinition(
            id = "kg_expert_silicon_sage",
            badgeTitle = "Silicon Sage",
            unlockedIconEmoji = "⚙️",
            unlockedIconName = "Systems Architect",
            description = "Master low-level systems programming and algorithmic performance limits (C Systems & Memory, Python Computational, and Algorithms & Big-O).",
            requiredTopicIds = listOf("c_lang", "python", "big_o"),
            clusterName = "Systems Architecture",
            tier = BadgeTier.PLATINUM,
            xpReward = 550,
            accentColor = RoseAccent,
            rarityText = "Expert · Systems Master"
        ),
        KnowledgeExpertBadgeDefinition(
            id = "kg_expert_synapse_sovereign",
            badgeTitle = "Synapse Sovereign",
            unlockedIconEmoji = "🧠",
            unlockedIconName = "Neural Sovereign",
            description = "Master cognitive feedback and edge neural computation (Python Computational, On-Device TFLite, and Spaced Repetition SM-2).",
            requiredTopicIds = listOf("python", "tflite", "spaced_rep"),
            clusterName = "Applied Intelligence",
            tier = BadgeTier.DIAMOND,
            xpReward = 650,
            accentColor = AmberAccent,
            rarityText = "Expert · AI Sovereign"
        ),
        KnowledgeExpertBadgeDefinition(
            id = "kg_expert_cosmic_grandmaster",
            badgeTitle = "Cosmic Grandmaster",
            unlockedIconEmoji = "👑",
            unlockedIconName = "Cosmic Crown",
            description = "Attain Expert mastery in 6 or more interconnected concepts across the entire Knowledge Graph constellation.",
            requiredTopicIds = listOf("html", "css", "js", "mysql", "compose", "coroutines", "tflite", "c_lang"),
            requiredExpertCount = 6,
            clusterName = "Constellation Spine",
            tier = BadgeTier.DIAMOND,
            xpReward = 1200,
            accentColor = Color(0xFFFFD700),
            rarityText = "Mythic · Grandmaster"
        )
    )

    fun getDefinition(id: String): KnowledgeExpertBadgeDefinition? {
        return ALL_DEFINITIONS.find { it.id.equals(id, ignoreCase = true) }
    }
}

object KnowledgeMasteryBadgeEngine {

    /**
     * Evaluates knowledge graph nodes to determine unlocked profile icons and badges.
     */
    fun evaluateBadges(
        nodes: List<KnowledgeTopicNode>,
        currentAvatarEmoji: String,
        unlockedMap: Map<String, Long> = emptyMap()
    ): List<KnowledgeExpertBadgeStatus> {
        val nodeMap = nodes.associateBy { it.id.lowercase() }

        return KnowledgeExpertBadgeCatalog.ALL_DEFINITIONS.map { def ->
            val requiredNodes = def.requiredTopicIds.mapNotNull { id -> nodeMap[id.lowercase()] }
            val expertTopics = requiredNodes.filter { it.masteryLevel == KnowledgeMasteryLevel.EXPERT }
            val isUnlocked = (expertTopics.size >= def.requiredExpertCount) || unlockedMap.containsKey(def.id)
            val unlockedTimestamp = unlockedMap[def.id] ?: if (isUnlocked) System.currentTimeMillis() else null

            val topicProgress = def.requiredTopicIds.map { topicId ->
                val node = nodeMap[topicId.lowercase()]
                TopicMasteryDetail(
                    topicId = topicId,
                    title = node?.title ?: topicId.uppercase(),
                    accuracyPercent = node?.accuracyPercent ?: 0f,
                    attemptsCount = node?.attemptsCount ?: 0,
                    isExpert = (node?.masteryLevel == KnowledgeMasteryLevel.EXPERT),
                    status = node?.status ?: TopicMasteryStatus.UNEXPLORED,
                    masteryLevel = node?.masteryLevel ?: KnowledgeMasteryLevel.BEGINNER
                )
            }

            val fraction = (expertTopics.size.toFloat() / def.requiredExpertCount.toFloat()).coerceIn(0f, 1f)

            KnowledgeExpertBadgeStatus(
                definition = def,
                isUnlocked = isUnlocked,
                unlockedAt = unlockedTimestamp,
                isEquipped = currentAvatarEmoji == def.unlockedIconEmoji,
                expertTopicsCount = expertTopics.size,
                totalRequiredTopicsCount = def.requiredExpertCount,
                topicProgress = topicProgress,
                progressFraction = fraction
            )
        }
    }
}
