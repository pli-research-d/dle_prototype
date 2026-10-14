package com.example.dle_prototype

import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.graph.KnowledgeEdge
import com.example.dle_prototype.data.graph.KnowledgeGraphEngine
import com.example.dle_prototype.data.graph.KnowledgeMasteryLevel
import com.example.dle_prototype.data.graph.KnowledgeTopicNode
import com.example.dle_prototype.data.graph.TopicMasteryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeSpacedRepetitionSchedulerTest {

    private fun createTestNode(
        id: String,
        title: String,
        category: String,
        accuracy: Float,
        attempts: Int,
        status: TopicMasteryStatus
    ): KnowledgeTopicNode {
        return KnowledgeTopicNode(
            id = id,
            title = title,
            category = category,
            categoryNumber = 1f,
            accuracyPercent = accuracy,
            attemptsCount = attempts,
            status = status,
            normX = 0.5f,
            normY = 0.5f,
            description = "Test description",
            keyConcepts = listOf("Concept A", "Concept B"),
            clusterName = "Test Cluster"
        )
    }

    @Test
    fun testBeginnerMasteryDecaysFasterThanExpert() {
        val currentTime = 1700000000000L
        val twoDaysAgo = currentTime - (86400000L * 2)

        val beginnerNode = createTestNode(
            id = "node_beginner",
            title = "Basics",
            category = "Basics",
            accuracy = 45f,
            attempts = 2,
            status = TopicMasteryStatus.NEEDS_REVIEW
        )

        val expertNode = createTestNode(
            id = "node_expert",
            title = "Advanced Mastery",
            category = "Advanced",
            accuracy = 95f,
            attempts = 8,
            status = TopicMasteryStatus.STRENGTH
        )

        val beginnerAttempt = QuizAttempt(
            id = 1,
            username = "test_user",
            category = "Basics",
            score = 2,
            totalQuestions = 5,
            difficultyLevel = 2f,
            timestamp = twoDaysAgo
        )

        val expertAttempt = QuizAttempt(
            id = 2,
            username = "test_user",
            category = "Advanced",
            score = 5,
            totalQuestions = 5,
            difficultyLevel = 4f,
            timestamp = twoDaysAgo
        )

        val summary = KnowledgeGraphEngine.scheduleKnowledgeReinforcements(
            nodes = listOf(beginnerNode, expertNode),
            edges = emptyList(),
            quizAttempts = listOf(beginnerAttempt, expertAttempt),
            currentTimeMillis = currentTime
        )

        val beginnerSchedule = summary.schedules.first { it.nodeId == "node_beginner" }
        val expertSchedule = summary.schedules.first { it.nodeId == "node_expert" }

        assertEquals(KnowledgeMasteryLevel.BEGINNER, beginnerSchedule.masteryLevel)
        assertEquals(KnowledgeMasteryLevel.EXPERT, expertSchedule.masteryLevel)

        // Beginner retention must be significantly lower than expert retention after 2 days
        assertTrue(
            "Beginner retention (${beginnerSchedule.retentionProbability}) should be lower than expert (${expertSchedule.retentionProbability})",
            beginnerSchedule.retentionProbability < expertSchedule.retentionProbability
        )

        // Beginner priority score must be higher (urgency to reinforce)
        assertTrue(
            "Beginner urgency priority (${beginnerSchedule.urgencyPriorityScore}) should exceed expert (${expertSchedule.urgencyPriorityScore})",
            beginnerSchedule.urgencyPriorityScore > expertSchedule.urgencyPriorityScore
        )
    }

    @Test
    fun testPrerequisiteAnchorBoostsReinforcementPriority() {
        val currentTime = 1700000000000L
        val oneDayAgo = currentTime - 86400000L

        val anchorNode = createTestNode(
            id = "anchor",
            title = "Core Syntax",
            category = "Core",
            accuracy = 70f,
            attempts = 3,
            status = TopicMasteryStatus.SOLIDIFYING
        )

        val leafNode = createTestNode(
            id = "leaf",
            title = "Isolated Topic",
            category = "Leaf",
            accuracy = 70f,
            attempts = 3,
            status = TopicMasteryStatus.SOLIDIFYING
        )

        // Downstream edges where anchor is a prerequisite for 2 downstream topics
        val edges = listOf(
            KnowledgeEdge(fromId = "anchor", toId = "topic_b", relationship = "Prereq 1", isPrerequisite = true),
            KnowledgeEdge(fromId = "anchor", toId = "topic_c", relationship = "Prereq 2", isPrerequisite = true)
        )

        val attempts = listOf(
            QuizAttempt(1, "test_user", "Core", 3, 5, 2f, oneDayAgo),
            QuizAttempt(2, "test_user", "Leaf", 3, 5, 2f, oneDayAgo)
        )

        val summary = KnowledgeGraphEngine.scheduleKnowledgeReinforcements(
            nodes = listOf(anchorNode, leafNode),
            edges = edges,
            quizAttempts = attempts,
            currentTimeMillis = currentTime
        )

        val anchorSchedule = summary.schedules.first { it.nodeId == "anchor" }
        val leafSchedule = summary.schedules.first { it.nodeId == "leaf" }

        assertEquals(2, anchorSchedule.prerequisiteImpactCount)
        assertEquals(0, leafSchedule.prerequisiteImpactCount)

        assertTrue(
            "Prerequisite anchor should have higher priority score due to downstream dependencies",
            anchorSchedule.urgencyPriorityScore > leafSchedule.urgencyPriorityScore
        )

        assertTrue(
            "Reinforcement reason should mention prerequisite anchor",
            anchorSchedule.reinforcementReason.contains("prerequisite anchor")
        )
    }

    @Test
    fun testUntestedNodePrioritizedForBaselineCalibration() {
        val untestedNode = createTestNode(
            id = "untested",
            title = "Quantum Computing",
            category = "Quantum",
            accuracy = 0f,
            attempts = 0,
            status = TopicMasteryStatus.UNEXPLORED
        )

        val summary = KnowledgeGraphEngine.scheduleKnowledgeReinforcements(
            nodes = listOf(untestedNode),
            edges = emptyList(),
            quizAttempts = emptyList(),
            currentTimeMillis = System.currentTimeMillis()
        )

        val schedule = summary.schedules.first()
        assertTrue("Untested node must be overdue for initial calibration", schedule.isOverdue)
        assertEquals("Baseline Untested", schedule.dueStatusText)
        assertEquals("Take Baseline Quiz", schedule.recommendedAction)
        assertEquals(KnowledgeMasteryLevel.BEGINNER, schedule.masteryLevel)
    }

    @Test
    fun testSummaryMetricsAggregation() {
        val node1 = createTestNode("n1", "T1", "C1", 40f, 1, TopicMasteryStatus.NEEDS_REVIEW)
        val node2 = createTestNode("n2", "T2", "C2", 85f, 6, TopicMasteryStatus.STRENGTH)

        val summary = KnowledgeGraphEngine.scheduleKnowledgeReinforcements(
            nodes = listOf(node1, node2),
            edges = emptyList(),
            currentTimeMillis = System.currentTimeMillis()
        )

        assertEquals(2, summary.schedules.size)
        assertNotNull(summary.mostUrgentSchedule)
        assertEquals("n1", summary.mostUrgentSchedule?.nodeId)
        assertTrue(summary.averageRetentionPercent in 5..100)
    }
}
