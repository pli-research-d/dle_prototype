package com.example.dle_prototype.data.graph

import com.example.dle_prototype.data.ml.AnswerConfidence
import kotlin.math.abs

enum class KnowledgeQuizDifficultyTier(
    val label: String,
    val iconEmoji: String,
    val baseComplexity: Float,
    val minComplexity: Float,
    val maxComplexity: Float
) {
    FOUNDATIONAL(
        label = "Foundational",
        iconEmoji = "🌱",
        baseComplexity = 1.3f,
        minComplexity = 1.0f,
        maxComplexity = 1.8f
    ),
    CORE_APPLICATION(
        label = "Core Application",
        iconEmoji = "⚡",
        baseComplexity = 2.1f,
        minComplexity = 1.8f,
        maxComplexity = 2.6f
    ),
    EXPERT_ARCHITECTURE(
        label = "Expert Architecture",
        iconEmoji = "🥇",
        baseComplexity = 2.9f,
        minComplexity = 2.6f,
        maxComplexity = 3.5f
    );

    companion object {
        fun fromComplexity(complexity: Float): KnowledgeQuizDifficultyTier {
            return when {
                complexity >= 2.6f -> EXPERT_ARCHITECTURE
                complexity >= 1.8f -> CORE_APPLICATION
                else -> FOUNDATIONAL
            }
        }
    }
}

enum class KnowledgeGraphTriggerType {
    INITIAL_CALIBRATION,
    TIER_ESCALATION,
    EXPERT_MASTERY_REACHED,
    PREREQUISITE_SCAFFOLDING,
    MISCONCEPTION_REPAIR,
    DOWNSTREAM_UNLOCKED,
    STABILIZATION
}

data class KnowledgeGraphQuizQuestion(
    val id: String,
    val nodeId: String,
    val nodeTitle: String,
    val conceptTag: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: Any,
    val type: String = "mcq", // "mcq" or "true_false"
    val explanation: String,
    val difficultyTier: KnowledgeQuizDifficultyTier,
    val intrinsicComplexity: Float,
    val prerequisiteNodeId: String? = null,
    val isCrossNodeSynthesis: Boolean = false,
    val connectedNodeIds: List<String> = emptyList()
)

data class KnowledgeGraphAdjustmentEvent(
    val questionIndex: Int,
    val wasCorrect: Boolean,
    val confidence: AnswerConfidence,
    val previousComplexity: Float,
    val newComplexity: Float,
    val previousTier: KnowledgeQuizDifficultyTier,
    val newTier: KnowledgeQuizDifficultyTier,
    val previousMastery: Float,
    val newMastery: Float,
    val reason: String,
    val triggerType: KnowledgeGraphTriggerType,
    val scaffoldedPrerequisiteNodeId: String? = null
)

data class KnowledgeGraphAdaptiveSession(
    val currentNode: KnowledgeTopicNode,
    val graphData: KnowledgeGraphData? = null,
    val currentComplexity: Float,
    val currentTier: KnowledgeQuizDifficultyTier,
    val currentMasteryPercent: Float,
    val masteryLevel: KnowledgeMasteryLevel,
    val totalAnswered: Int = 0,
    val correctCount: Int = 0,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val consecutiveErrors: Int = 0,
    val scaffoldActive: Boolean = false,
    val scaffoldPrerequisiteNodeId: String? = null,
    val scaffoldPrerequisiteTitle: String? = null,
    val unlockedDownstreamNodes: List<String> = emptyList(),
    val events: List<KnowledgeGraphAdjustmentEvent> = emptyList()
)

object KnowledgeGraphAdaptiveQuizEngine {

    /**
     * Initializes a real-time adaptive quiz session calibrated directly to the
     * user's current progress in the Knowledge Graph for the specified topic node.
     */
    fun initializeSession(
        node: KnowledgeTopicNode,
        graphData: KnowledgeGraphData? = null
    ): KnowledgeGraphAdaptiveSession {
        val initialMastery = node.accuracyPercent.coerceIn(0f, 100f)

        // Calibrate initial difficulty tier and complexity based on Knowledge Graph node level
        val initialTier = when {
            node.masteryLevel == KnowledgeMasteryLevel.EXPERT || initialMastery >= 80f ->
                KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE
            node.masteryLevel == KnowledgeMasteryLevel.INTERMEDIATE || initialMastery >= 60f ->
                KnowledgeQuizDifficultyTier.CORE_APPLICATION
            else ->
                KnowledgeQuizDifficultyTier.FOUNDATIONAL
        }

        val initialComplexity = when (initialTier) {
            KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE -> 2.9f
            KnowledgeQuizDifficultyTier.CORE_APPLICATION -> 2.1f
            KnowledgeQuizDifficultyTier.FOUNDATIONAL -> 1.3f
        }

        // Determine downstream connected topics in topological Knowledge Graph
        val downstreamIds = graphData?.edges
            ?.filter { it.fromId.equals(node.id, ignoreCase = true) }
            ?.map { it.toId }
            ?: KnowledgeGraphEngine.STATIC_EDGES
                .filter { it.fromId.equals(node.id, ignoreCase = true) }
                .map { it.toId }

        return KnowledgeGraphAdaptiveSession(
            currentNode = node,
            graphData = graphData,
            currentComplexity = initialComplexity,
            currentTier = initialTier,
            currentMasteryPercent = initialMastery,
            masteryLevel = node.masteryLevel,
            totalAnswered = 0,
            correctCount = 0,
            currentStreak = 0,
            highestStreak = 0,
            consecutiveErrors = 0,
            scaffoldActive = false,
            scaffoldPrerequisiteNodeId = null,
            scaffoldPrerequisiteTitle = null,
            unlockedDownstreamNodes = downstreamIds,
            events = emptyList()
        )
    }

    /**
     * Updates question difficulty in real-time based on the user's progress through the Knowledge Graph.
     * Evaluates answer correctness, confidence level, prerequisite graph relationships, and mastery thresholds.
     */
    fun processAnswer(
        session: KnowledgeGraphAdaptiveSession,
        question: KnowledgeGraphQuizQuestion,
        isCorrect: Boolean,
        confidence: AnswerConfidence = AnswerConfidence.CONFIDENT,
        secondsTaken: Int = 10
    ): Pair<KnowledgeGraphAdaptiveSession, KnowledgeGraphAdjustmentEvent> {
        val prevComplexity = session.currentComplexity
        val prevTier = session.currentTier
        val prevMastery = session.currentMasteryPercent

        var newComplexity = prevComplexity
        var newTier = prevTier
        var newStreak = if (isCorrect) session.currentStreak + 1 else 0
        val newHighestStreak = maxOf(session.highestStreak, newStreak)
        val newConsecutiveErrors = if (isCorrect) 0 else session.consecutiveErrors + 1
        val newTotalAnswered = session.totalAnswered + 1
        val newCorrectCount = if (isCorrect) session.correctCount + 1 else session.correctCount

        var scaffoldActive = false
        var scaffoldPrereqId: String? = null
        var scaffoldPrereqTitle: String? = null
        val triggerType: KnowledgeGraphTriggerType
        val adjustmentReason: String

        // 1. Update real-time node mastery percentage
        val newMastery = when {
            confidence == AnswerConfidence.CERTAIN && isCorrect -> {
                // High confidence reward (+8% to +15% progress)
                (prevMastery * 0.85f + 100f * 0.15f).coerceIn(0f, 100f)
            }
            confidence == AnswerConfidence.GUESSING && isCorrect -> {
                // Guessing correct provides modest consolidation (+4%)
                (prevMastery * 0.92f + 75f * 0.08f).coerceIn(0f, 100f)
            }
            confidence == AnswerConfidence.CERTAIN && !isCorrect -> {
                // Misconception penalty on overconfidence
                (prevMastery * 0.80f + 0f * 0.20f).coerceIn(0f, 100f)
            }
            else -> {
                // Guessing & incorrect step-down
                (prevMastery * 0.85f + 0f * 0.15f).coerceIn(0f, 100f)
            }
        }

        // Evaluate updated Knowledge Graph mastery level
        val newMasteryLevel = when {
            newMastery >= 80f -> KnowledgeMasteryLevel.EXPERT
            newMastery >= 60f -> KnowledgeMasteryLevel.INTERMEDIATE
            else -> KnowledgeMasteryLevel.BEGINNER
        }

        // 2. Compute dynamic complexity and tier adjustments
        when {
            // Case A: Confident & Correct -> Accelerate difficulty and promote tiers on streak
            confidence == AnswerConfidence.CERTAIN && isCorrect -> {
                val speedBonus = if (secondsTaken <= 7) 0.15f else 0.05f
                newComplexity = (newComplexity + 0.25f + speedBonus).coerceAtMost(3.5f)

                when {
                    // Transition to Expert Architecture when reaching Expert tier in Knowledge Graph
                    (newMastery >= 80f || newStreak >= 3) && prevTier != KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE -> {
                        newTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE
                        triggerType = KnowledgeGraphTriggerType.EXPERT_MASTERY_REACHED
                        adjustmentReason = "🥇 Expert Mastery Reached! Accuracy rose to ${newMastery.toInt()}%. Escalating difficulty to Expert Architecture (Ring 3)."
                    }
                    // Transition to Core Application when breaking out of Foundational
                    (newMastery >= 60f || newStreak >= 2) && prevTier == KnowledgeQuizDifficultyTier.FOUNDATIONAL -> {
                        newTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION
                        triggerType = KnowledgeGraphTriggerType.TIER_ESCALATION
                        adjustmentReason = "⚡ Core Mastery Unlocked: 2-in-a-row confident streak! Escalated to Core Application tier."
                    }
                    // Sustained Expert Architecture
                    prevTier == KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE -> {
                        triggerType = KnowledgeGraphTriggerType.DOWNSTREAM_UNLOCKED
                        val downstreamTarget = session.unlockedDownstreamNodes.firstOrNull() ?: "Connected Systems"
                        adjustmentReason = "🌌 Constellation Mastery Sustained: Hard tier sustained at complexity ${"%.2f".format(newComplexity)}. Unlocking pathway to $downstreamTarget!"
                    }
                    else -> {
                        triggerType = KnowledgeGraphTriggerType.TIER_ESCALATION
                        adjustmentReason = "🎯 Confident & Correct: Accelerated node mastery to ${newMastery.toInt()}% (+${"%.2f".format(0.25f + speedBonus)} complexity)."
                    }
                }
            }

            // Case B: Confident & Incorrect -> Misconception Penalty & Prerequisite Scaffolding
            confidence == AnswerConfidence.CERTAIN && !isCorrect -> {
                newComplexity = (newComplexity - 0.42f).coerceAtLeast(1.0f)

                // Check Knowledge Graph for prerequisite nodes
                val prereqEdge = session.graphData?.edges?.find {
                    it.toId.equals(session.currentNode.id, ignoreCase = true)
                } ?: KnowledgeGraphEngine.STATIC_EDGES.find {
                    it.toId.equals(session.currentNode.id, ignoreCase = true)
                }

                if (prereqEdge != null) {
                    scaffoldActive = true
                    scaffoldPrereqId = prereqEdge.fromId
                    val prereqNode = session.graphData?.nodes?.find { it.id.equals(prereqEdge.fromId, ignoreCase = true) }
                    scaffoldPrereqTitle = prereqNode?.title ?: prereqEdge.fromId.uppercase()
                    newTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL
                    triggerType = KnowledgeGraphTriggerType.PREREQUISITE_SCAFFOLDING
                    adjustmentReason = "⚠️ Conceptual Gap: Misconception detected! Scaffolding to Foundational tier on prerequisite '$scaffoldPrereqTitle' to repair core concepts."
                } else {
                    newTier = when (prevTier) {
                        KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE -> KnowledgeQuizDifficultyTier.CORE_APPLICATION
                        KnowledgeQuizDifficultyTier.CORE_APPLICATION -> KnowledgeQuizDifficultyTier.FOUNDATIONAL
                        KnowledgeQuizDifficultyTier.FOUNDATIONAL -> KnowledgeQuizDifficultyTier.FOUNDATIONAL
                    }
                    triggerType = KnowledgeGraphTriggerType.MISCONCEPTION_REPAIR
                    adjustmentReason = "⚠️ Misconception Penalty: Steep step-down to ${newTier.label} tier to rebuild foundational mental model."
                }
            }

            // Case C: Guessing & Correct -> Consolidate without premature difficulty inflation
            confidence == AnswerConfidence.GUESSING && isCorrect -> {
                newComplexity = (newComplexity + 0.05f).coerceAtMost(3.0f)
                newTier = prevTier // Maintain current tier to consolidate
                triggerType = KnowledgeGraphTriggerType.STABILIZATION
                adjustmentReason = "🎲 Guessing & Correct: Stabilizing at ${newTier.label} tier (50% velocity) to build genuine conceptual confidence."
            }

            // Case D: Guessing & Incorrect -> Scaffolds down to reinforce fundamentals
            else -> {
                newComplexity = (newComplexity - 0.32f).coerceAtLeast(1.0f)
                newTier = when (prevTier) {
                    KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE -> KnowledgeQuizDifficultyTier.CORE_APPLICATION
                    KnowledgeQuizDifficultyTier.CORE_APPLICATION -> KnowledgeQuizDifficultyTier.FOUNDATIONAL
                    KnowledgeQuizDifficultyTier.FOUNDATIONAL -> KnowledgeQuizDifficultyTier.FOUNDATIONAL
                }
                triggerType = KnowledgeGraphTriggerType.MISCONCEPTION_REPAIR
                adjustmentReason = "❌ Reinforcement Needed: Reduced adaptive complexity to ${newTier.label} tier to ground core knowledge."
            }
        }

        // Align tier with complexity bounds if drifted
        if (newComplexity < newTier.minComplexity && newTier != KnowledgeQuizDifficultyTier.FOUNDATIONAL) {
            newTier = KnowledgeQuizDifficultyTier.fromComplexity(newComplexity)
        } else if (newComplexity > newTier.maxComplexity && newTier != KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE) {
            newTier = KnowledgeQuizDifficultyTier.fromComplexity(newComplexity)
        }

        val event = KnowledgeGraphAdjustmentEvent(
            questionIndex = newTotalAnswered,
            wasCorrect = isCorrect,
            confidence = confidence,
            previousComplexity = prevComplexity,
            newComplexity = newComplexity,
            previousTier = prevTier,
            newTier = newTier,
            previousMastery = prevMastery,
            newMastery = newMastery,
            reason = adjustmentReason,
            triggerType = triggerType,
            scaffoldedPrerequisiteNodeId = scaffoldPrereqId
        )

        val updatedSession = session.copy(
            currentComplexity = newComplexity,
            currentTier = newTier,
            currentMasteryPercent = newMastery,
            masteryLevel = newMasteryLevel,
            totalAnswered = newTotalAnswered,
            correctCount = newCorrectCount,
            currentStreak = newStreak,
            highestStreak = newHighestStreak,
            consecutiveErrors = newConsecutiveErrors,
            scaffoldActive = scaffoldActive,
            scaffoldPrerequisiteNodeId = scaffoldPrereqId,
            scaffoldPrerequisiteTitle = scaffoldPrereqTitle,
            events = session.events + event
        )

        return Pair(updatedSession, event)
    }

    /**
     * Selects the optimal question matching the real-time Knowledge Graph state,
     * target tier, intrinsic complexity, and any active prerequisite scaffolding.
     */
    fun selectNextQuestion(
        session: KnowledgeGraphAdaptiveSession,
        candidatePool: List<KnowledgeGraphQuizQuestion>,
        usedQuestionIds: Set<String>
    ): KnowledgeGraphQuizQuestion? {
        val available = candidatePool.filterNot { usedQuestionIds.contains(it.id) }
        if (available.isEmpty()) return null

        // 1. If prerequisite scaffolding is active, prioritize questions for the prerequisite node
        if (session.scaffoldActive && session.scaffoldPrerequisiteNodeId != null) {
            val prereqMatch = available.find { q ->
                q.nodeId.equals(session.scaffoldPrerequisiteNodeId, ignoreCase = true) &&
                        q.difficultyTier == KnowledgeQuizDifficultyTier.FOUNDATIONAL
            }
            if (prereqMatch != null) return prereqMatch
        }

        // 2. Filter for current node matching target tier
        val nodeMatches = available.filter { it.nodeId.equals(session.currentNode.id, ignoreCase = true) }
        val pool = if (nodeMatches.isNotEmpty()) nodeMatches else available

        val tierMatches = pool.filter { it.difficultyTier == session.currentTier }
        val finalPool = if (tierMatches.isNotEmpty()) tierMatches else pool

        // 3. Min-distance selection to match continuous complexity
        return finalPool.minByOrNull { q ->
            abs(q.intrinsicComplexity - session.currentComplexity)
        }
    }
}
