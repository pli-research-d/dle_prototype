package com.example.dle_prototype

import com.example.dle_prototype.data.CategoryMastery
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.graph.KnowledgeGraphEngine
import com.example.dle_prototype.data.graph.TopicMasteryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeGraphEngineTest {

    @Test
    fun testStaticEdgesTopologicalIntegrity() {
        val edges = KnowledgeGraphEngine.STATIC_EDGES
        assertTrue("Graph must have at least 10 topological connections", edges.size >= 10)

        val htmlCssEdge = edges.firstOrNull { it.fromId == "html" && it.toId == "css" }
        assertNotNull("HTML to CSS edge must exist", htmlCssEdge)
        assertEquals("Semantic Layout", htmlCssEdge?.relationship)

        val composeCoroutinesEdge = edges.firstOrNull { it.fromId == "compose" && it.toId == "coroutines" }
        assertNotNull("Compose to Coroutines edge must exist", composeCoroutinesEdge)
    }

    @Test
    fun testUnexploredTopicsWhenNoHistory() {
        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(
            masteries = emptyList(),
            attempts = emptyList(),
            dueCardsCount = 0
        )

        assertNotNull(graph)
        assertTrue(graph.nodes.isNotEmpty())
        assertEquals(0, graph.strengthsCount)
        assertEquals(0, graph.solidifyingCount)
        assertEquals(0, graph.reviewCount)
        assertEquals(graph.nodes.size, graph.unexploredCount)
        assertEquals(0f, graph.averageMasteryPercent, 0.01f)

        val htmlNode = graph.nodes.firstOrNull { it.id == "html" }
        assertNotNull(htmlNode)
        assertEquals(TopicMasteryStatus.UNEXPLORED, htmlNode?.status)
    }

    @Test
    fun testStrengthsAndReviewClassificationFromAdaptiveTelemetry() {
        val masteries = listOf(
            CategoryMastery(
                categoryName = "HTML",
                icon = "🌐",
                totalAttempts = 12,
                totalQuestions = 60,
                correctAnswers = 53,
                accuracyPercent = 88.5f,
                masteryLevel = "Proficient",
                highestDifficultyReached = 4.5f,
                statusBadge = "Mastered"
            ),
            CategoryMastery(
                categoryName = "JavaScript",
                icon = "⚡",
                totalAttempts = 5,
                totalQuestions = 25,
                correctAnswers = 10,
                accuracyPercent = 42.0f,
                masteryLevel = "Novice",
                highestDifficultyReached = 2.0f,
                statusBadge = "Needs Reinforcement"
            ),
            CategoryMastery(
                categoryName = "Python",
                icon = "🐍",
                totalAttempts = 8,
                totalQuestions = 40,
                correctAnswers = 26,
                accuracyPercent = 65.0f,
                masteryLevel = "Practitioner",
                highestDifficultyReached = 3.2f,
                statusBadge = "Solidifying"
            )
        )

        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(
            masteries = masteries,
            attempts = emptyList(),
            dueCardsCount = 2
        )

        val htmlNode = graph.nodes.firstOrNull { it.id == "html" }
        assertNotNull(htmlNode)
        assertEquals(TopicMasteryStatus.STRENGTH, htmlNode?.status)
        assertEquals(88.5f, htmlNode?.accuracyPercent ?: 0f, 0.1f)

        val jsNode = graph.nodes.firstOrNull { it.id == "js" }
        assertNotNull(jsNode)
        assertEquals(TopicMasteryStatus.NEEDS_REVIEW, jsNode?.status)
        assertEquals(42.0f, jsNode?.accuracyPercent ?: 0f, 0.1f)

        val pythonNode = graph.nodes.firstOrNull { it.id == "python" }
        assertNotNull(pythonNode)
        assertEquals(TopicMasteryStatus.SOLIDIFYING, pythonNode?.status)
        assertEquals(65.0f, pythonNode?.accuracyPercent ?: 0f, 0.1f)

        assertTrue(graph.strengthsCount >= 1)
        assertTrue(graph.reviewCount >= 1)
        assertTrue(graph.solidifyingCount >= 1)
        assertTrue(graph.averageMasteryPercent > 0f)
    }

    @Test
    fun testRecentMissesTriggersReviewStatus() {
        val masteries = listOf(
            CategoryMastery(
                categoryName = "CSS",
                icon = "🎨",
                totalAttempts = 10,
                totalQuestions = 50,
                correctAnswers = 39,
                accuracyPercent = 78.0f,
                masteryLevel = "Proficient",
                highestDifficultyReached = 3.5f,
                statusBadge = "Mastered"
            )
        )
        val recentAttempts = listOf(
            QuizAttempt(
                username = "alice",
                category = "CSS",
                score = 1,
                totalQuestions = 5,
                difficultyLevel = 3.0f,
                timestamp = System.currentTimeMillis()
            )
        )

        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(
            masteries = masteries,
            attempts = recentAttempts,
            dueCardsCount = 3
        )

        val cssNode = graph.nodes.firstOrNull { it.id == "css" }
        assertNotNull(cssNode)
        assertEquals(78.0f, cssNode?.accuracyPercent ?: 0f, 0.1f)
    }

    @Test
    fun testCLanguageNodeInKnowledgeGraph() {
        val masteries = listOf(
            CategoryMastery(
                categoryName = "C Language",
                icon = "⚙️",
                totalAttempts = 6,
                totalQuestions = 30,
                correctAnswers = 27,
                accuracyPercent = 90.0f,
                masteryLevel = "Master",
                highestDifficultyReached = 3.0f,
                statusBadge = "Mastered"
            )
        )

        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(
            masteries = masteries,
            attempts = emptyList(),
            dueCardsCount = 0
        )

        val cNode = graph.nodes.firstOrNull { it.id == "c_lang" }
        assertNotNull("C Language node must be present in knowledge graph", cNode)
        assertEquals("C Language", cNode?.category)
        assertEquals(7f, cNode?.categoryNumber)
        assertEquals(TopicMasteryStatus.STRENGTH, cNode?.status)
        assertEquals(90.0f, cNode?.accuracyPercent ?: 0f, 0.1f)
        assertTrue(cNode?.keyConcepts?.contains("malloc & free") == true)

        val edgesWithC = graph.edges.filter { it.fromId == "c_lang" || it.toId == "c_lang" }
        assertTrue("C Language must connect to at least 2 nodes", edgesWithC.size >= 2)
    }
}
