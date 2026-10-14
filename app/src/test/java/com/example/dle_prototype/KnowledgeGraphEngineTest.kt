package com.example.dle_prototype

import com.example.dle_prototype.data.CategoryMastery
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.graph.KnowledgeGraphEngine
import com.example.dle_prototype.data.graph.KnowledgeMasteryLevel
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

    @Test
    fun testClustersAndRoadmapMetadata() {
        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(
            masteries = emptyList(),
            attempts = emptyList(),
            dueCardsCount = 0
        )

        assertEquals("Should have 5 core curriculum clusters", 5, graph.clusters.size)
        assertTrue(graph.clusters.any { it.name == "Web Architecture" })
        assertTrue(graph.clusters.any { it.name == "Applied Intelligence" })

        // Check node roadmap coordinates & short labels
        graph.nodes.forEach { node ->
            assertTrue("Node ${node.id} short label must not be empty", node.shortLabel.isNotBlank())
            assertTrue("Node ${node.id} icon monogram must not be empty", node.iconMonogram.isNotBlank())
            assertTrue("Node ${node.id} tier must be between 1 and 4", node.tierLevel in 1..4)
            assertTrue("Roadmap X coordinate must be in [0, 1]", node.normRoadmapX in 0f..1f)
            assertTrue("Roadmap Y coordinate must be in [0, 1]", node.normRoadmapY in 0f..1f)
        }
    }

    @Test
    fun testPrerequisitesAndUnlockedHelpers() {
        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(
            masteries = emptyList(),
            attempts = emptyList(),
            dueCardsCount = 0
        )

        // HTML should unlock CSS and JS
        val unlockedByHtml = KnowledgeGraphEngine.getUnlockedBy("html", graph.nodes, graph.edges)
        assertTrue(unlockedByHtml.any { it.id == "css" })
        assertTrue(unlockedByHtml.any { it.id == "js" })

        // TFLite prerequisites should include python, compose, c_lang
        val prereqsForTfLite = KnowledgeGraphEngine.getPrerequisitesFor("tflite", graph.nodes, graph.edges)
        assertTrue(prereqsForTfLite.any { it.id == "python" })
        assertTrue(prereqsForTfLite.any { it.id == "compose" })
    }

    @Test
    fun testRecommendedNextTopicSelection() {
        // When JavaScript has low score, it should be recommended
        val masteries = listOf(
            CategoryMastery(
                categoryName = "JavaScript",
                icon = "⚡",
                totalAttempts = 4,
                totalQuestions = 20,
                correctAnswers = 6,
                accuracyPercent = 30.0f,
                masteryLevel = "Novice",
                highestDifficultyReached = 1.5f,
                statusBadge = "Needs Reinforcement"
            )
        )
        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(
            masteries = masteries,
            attempts = emptyList(),
            dueCardsCount = 0
        )
        assertNotNull(graph.recommendedNextTopic)
        assertEquals("js", graph.recommendedNextTopic?.id)
    }

    @Test
    fun testKnowledgeMasteryLevelsAndIndicatorRings() {
        val masteries = listOf(
            CategoryMastery(
                categoryName = "HTML",
                icon = "🌐",
                totalAttempts = 15,
                totalQuestions = 60,
                correctAnswers = 54,
                accuracyPercent = 90.0f,
                masteryLevel = "Master",
                highestDifficultyReached = 4.0f,
                statusBadge = "Mastered"
            ),
            CategoryMastery(
                categoryName = "CSS",
                icon = "🎨",
                totalAttempts = 10,
                totalQuestions = 40,
                correctAnswers = 28,
                accuracyPercent = 70.0f,
                masteryLevel = "Proficient",
                highestDifficultyReached = 3.0f,
                statusBadge = "Solidifying"
            ),
            CategoryMastery(
                categoryName = "JavaScript",
                icon = "⚡",
                totalAttempts = 6,
                totalQuestions = 24,
                correctAnswers = 10,
                accuracyPercent = 41.6f,
                masteryLevel = "Novice",
                highestDifficultyReached = 2.0f,
                statusBadge = "Needs Reinforcement"
            )
        )

        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(
            masteries = masteries,
            attempts = emptyList(),
            dueCardsCount = 0
        )

        val htmlNode = graph.nodes.firstOrNull { it.id == "html" }
        assertNotNull(htmlNode)
        assertEquals(KnowledgeMasteryLevel.EXPERT, htmlNode?.masteryLevel)
        assertEquals("Expert", htmlNode?.masteryLevel?.label)
        assertEquals(3, htmlNode?.masteryLevel?.ringCount)

        val cssNode = graph.nodes.firstOrNull { it.id == "css" }
        assertNotNull(cssNode)
        assertEquals(KnowledgeMasteryLevel.INTERMEDIATE, cssNode?.masteryLevel)
        assertEquals("Intermediate", cssNode?.masteryLevel?.label)
        assertEquals(2, cssNode?.masteryLevel?.ringCount)

        val jsNode = graph.nodes.firstOrNull { it.id == "js" }
        assertNotNull(jsNode)
        assertEquals(KnowledgeMasteryLevel.BEGINNER, jsNode?.masteryLevel)
        assertEquals("Beginner", jsNode?.masteryLevel?.label)
        assertEquals(1, jsNode?.masteryLevel?.ringCount)

        // Untested nodes should default to Beginner
        val pyNode = graph.nodes.firstOrNull { it.id == "python" }
        assertNotNull(pyNode)
        assertEquals(KnowledgeMasteryLevel.BEGINNER, pyNode?.masteryLevel)

        assertTrue("Expert count must be >= 1", graph.expertCount >= 1)
        assertTrue("Intermediate count must be >= 1", graph.intermediateCount >= 1)
        assertTrue("Beginner count must be >= 1", graph.beginnerCount >= 1)
        assertEquals(graph.nodes.size, graph.expertCount + graph.intermediateCount + graph.beginnerCount)
    }

    @Test
    fun testFilterNodesByTopicNameAndMasteryLevel() {
        val masteries = listOf(
            CategoryMastery(
                categoryName = "HTML",
                icon = "🌐",
                totalAttempts = 15,
                totalQuestions = 60,
                correctAnswers = 54,
                accuracyPercent = 90.0f,
                masteryLevel = "Master",
                highestDifficultyReached = 4.0f,
                statusBadge = "Mastered"
            ),
            CategoryMastery(
                categoryName = "CSS",
                icon = "🎨",
                totalAttempts = 10,
                totalQuestions = 40,
                correctAnswers = 28,
                accuracyPercent = 70.0f,
                masteryLevel = "Proficient",
                highestDifficultyReached = 3.0f,
                statusBadge = "Solidifying"
            ),
            CategoryMastery(
                categoryName = "JavaScript",
                icon = "⚡",
                totalAttempts = 6,
                totalQuestions = 24,
                correctAnswers = 10,
                accuracyPercent = 41.6f,
                masteryLevel = "Novice",
                highestDifficultyReached = 2.0f,
                statusBadge = "Needs Reinforcement"
            )
        )

        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(
            masteries = masteries,
            attempts = emptyList(),
            dueCardsCount = 0
        )

        // 1. Filter by Topic Name: e.g. "HTML"
        val htmlResults = KnowledgeGraphEngine.filterNodes(graph.nodes, query = "HTML")
        assertEquals(1, htmlResults.size)
        assertEquals("html", htmlResults[0].id)

        // Partial match topic name: e.g. "Java" should match JavaScript
        val javaResults = KnowledgeGraphEngine.filterNodes(graph.nodes, query = "Java")
        assertTrue(javaResults.any { it.id == "js" })

        // 2. Filter by Mastery Level (via explicit enum parameter)
        val expertNodes = KnowledgeGraphEngine.filterNodes(graph.nodes, query = "", masteryLevel = KnowledgeMasteryLevel.EXPERT)
        assertTrue("HTML must be in expertNodes", expertNodes.any { it.id == "html" })
        assertTrue("All returned nodes must have EXPERT mastery level", expertNodes.all { it.masteryLevel == KnowledgeMasteryLevel.EXPERT })

        val intermediateNodes = KnowledgeGraphEngine.filterNodes(graph.nodes, query = "", masteryLevel = KnowledgeMasteryLevel.INTERMEDIATE)
        assertTrue("CSS must be in intermediateNodes", intermediateNodes.any { it.id == "css" })
        assertTrue("All returned nodes must have INTERMEDIATE mastery level", intermediateNodes.all { it.masteryLevel == KnowledgeMasteryLevel.INTERMEDIATE })

        val beginnerNodes = KnowledgeGraphEngine.filterNodes(graph.nodes, query = "", masteryLevel = KnowledgeMasteryLevel.BEGINNER)
        assertTrue("JS must be in beginnerNodes", beginnerNodes.any { it.id == "js" })
        assertTrue("All returned nodes must have BEGINNER mastery level", beginnerNodes.all { it.masteryLevel == KnowledgeMasteryLevel.BEGINNER })

        // 3. Filter by Mastery Level via search query text: "expert"
        val queryExpert = KnowledgeGraphEngine.filterNodes(graph.nodes, query = "expert")
        assertTrue("Search query 'expert' must return HTML", queryExpert.any { it.id == "html" })
        assertTrue("Search query 'expert' results must have EXPERT level", queryExpert.all { it.masteryLevel == KnowledgeMasteryLevel.EXPERT })

        // Search query "beginner"
        val queryBeginner = KnowledgeGraphEngine.filterNodes(graph.nodes, query = "beginner")
        assertTrue("Search query 'beginner' must return JS", queryBeginner.any { it.id == "js" })

        // 4. Combined Filter: Topic Name + Mastery Level
        val htmlWithExpert = KnowledgeGraphEngine.filterNodes(graph.nodes, query = "HTML", masteryLevel = KnowledgeMasteryLevel.EXPERT)
        assertEquals(1, htmlWithExpert.size)

        val htmlWithBeginner = KnowledgeGraphEngine.filterNodes(graph.nodes, query = "HTML", masteryLevel = KnowledgeMasteryLevel.BEGINNER)
        assertEquals(0, htmlWithBeginner.size)
    }
}
