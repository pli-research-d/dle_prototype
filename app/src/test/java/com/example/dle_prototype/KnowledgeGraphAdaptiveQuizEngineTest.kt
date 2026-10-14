package com.example.dle_prototype

import com.example.dle_prototype.data.graph.KnowledgeEdge
import com.example.dle_prototype.data.graph.KnowledgeGraphAdaptiveQuizEngine
import com.example.dle_prototype.data.graph.KnowledgeGraphData
import com.example.dle_prototype.data.graph.KnowledgeGraphQuestionBank
import com.example.dle_prototype.data.graph.KnowledgeGraphTriggerType
import com.example.dle_prototype.data.graph.KnowledgeMasteryLevel
import com.example.dle_prototype.data.graph.KnowledgeQuizDifficultyTier
import com.example.dle_prototype.data.graph.KnowledgeTopicNode
import com.example.dle_prototype.data.graph.TopicMasteryStatus
import com.example.dle_prototype.data.ml.AnswerConfidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeGraphAdaptiveQuizEngineTest {

    private fun createTopicNode(
        id: String,
        title: String,
        accuracy: Float,
        attempts: Int = 10,
        level: KnowledgeMasteryLevel
    ): KnowledgeTopicNode {
        return KnowledgeTopicNode(
            id = id,
            title = title,
            category = "Computer Science",
            categoryNumber = 3f,
            accuracyPercent = accuracy,
            attemptsCount = attempts,
            status = if (accuracy >= 75f) TopicMasteryStatus.STRENGTH else TopicMasteryStatus.SOLIDIFYING,
            normX = 0.5f,
            normY = 0.5f,
            description = "Description for $title",
            keyConcepts = listOf("fundamentals", "advanced"),
            clusterName = "Web",
            masteryLevel = level
        )
    }

    @Test
    fun testInitializeSession_calibratesToNodeMastery() {
        val beginnerNode = createTopicNode("html", "HTML5", 40f, 3, KnowledgeMasteryLevel.BEGINNER)
        val sessionBeginner = KnowledgeGraphAdaptiveQuizEngine.initializeSession(beginnerNode)
        assertEquals(KnowledgeQuizDifficultyTier.FOUNDATIONAL, sessionBeginner.currentTier)
        assertEquals(1.3f, sessionBeginner.currentComplexity, 0.01f)

        val expertNode = createTopicNode("js", "JavaScript", 92f, 25, KnowledgeMasteryLevel.EXPERT)
        val sessionExpert = KnowledgeGraphAdaptiveQuizEngine.initializeSession(expertNode)
        assertEquals(KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE, sessionExpert.currentTier)
        assertEquals(2.9f, sessionExpert.currentComplexity, 0.01f)
    }

    @Test
    fun testProcessAnswer_correctWithConfidence_escalatesComplexity() {
        val node = createTopicNode("js", "JavaScript", 65f, 10, KnowledgeMasteryLevel.INTERMEDIATE)
        val session = KnowledgeGraphAdaptiveQuizEngine.initializeSession(node)

        val questions = KnowledgeGraphQuestionBank.getCuratedQuestionsForNode("js")
        assertTrue("Questions should exist for js node", questions.isNotEmpty())
        val question = questions.first()

        val (newSession, event) = KnowledgeGraphAdaptiveQuizEngine.processAnswer(
            session = session,
            question = question,
            isCorrect = true,
            confidence = AnswerConfidence.CONFIDENT
        )

        assertTrue(newSession.currentComplexity > session.currentComplexity)
        assertTrue(newSession.currentMasteryPercent > session.currentMasteryPercent)
        assertEquals(1, newSession.correctCount)
        assertEquals(1, newSession.currentStreak)
        assertNotNull(event)
        assertTrue(event!!.wasCorrect)
    }

    @Test
    fun testProcessAnswer_misconceptionScaffoldsPrerequisiteNode() {
        val htmlNode = createTopicNode("html", "HTML5", 70f, 10, KnowledgeMasteryLevel.INTERMEDIATE)
        val jsNode = createTopicNode("js", "JavaScript", 65f, 10, KnowledgeMasteryLevel.INTERMEDIATE)

        val graphData = KnowledgeGraphData(
            nodes = listOf(htmlNode, jsNode),
            edges = listOf(
                KnowledgeEdge(
                    fromId = "html",
                    toId = "js",
                    relationship = "Prerequisite for DOM manipulation",
                    isPrerequisite = true
                )
            ),
            strengthsCount = 1,
            solidifyingCount = 1,
            reviewCount = 0,
            unexploredCount = 0,
            averageMasteryPercent = 68f
        )

        val session = KnowledgeGraphAdaptiveQuizEngine.initializeSession(jsNode, graphData)
        val question = KnowledgeGraphQuestionBank.getCuratedQuestionsForNode("js").first()

        // Answer incorrectly with CERTAIN confidence (misconception)
        val (scaffoldedSession, event) = KnowledgeGraphAdaptiveQuizEngine.processAnswer(
            session = session,
            question = question,
            isCorrect = false,
            confidence = AnswerConfidence.CERTAIN
        )

        assertTrue("Scaffolding should activate on high-confidence misconception", scaffoldedSession.scaffoldActive)
        assertEquals("html", scaffoldedSession.scaffoldPrerequisiteNodeId)
        assertEquals(KnowledgeQuizDifficultyTier.FOUNDATIONAL, scaffoldedSession.currentTier)
        assertEquals(KnowledgeGraphTriggerType.PREREQUISITE_SCAFFOLDING, event?.triggerType)
    }

    @Test
    fun testProcessAnswer_reachingExpertMastery_unlocksDownstreamNodes() {
        val jsNode = createTopicNode("js", "JavaScript", 78f, 15, KnowledgeMasteryLevel.INTERMEDIATE)
        val reactNode = createTopicNode("compose", "Modern Frameworks", 30f, 2, KnowledgeMasteryLevel.BEGINNER)

        val graphData = KnowledgeGraphData(
            nodes = listOf(jsNode, reactNode),
            edges = listOf(
                KnowledgeEdge(
                    fromId = "js",
                    toId = "compose",
                    relationship = "Unlocks reactive programming",
                    isPrerequisite = true
                )
            ),
            strengthsCount = 1,
            solidifyingCount = 1,
            reviewCount = 0,
            unexploredCount = 0,
            averageMasteryPercent = 60f
        )

        val session = KnowledgeGraphAdaptiveQuizEngine.initializeSession(jsNode, graphData)
        val questions = KnowledgeGraphQuestionBank.getCuratedQuestionsForNode("js")

        // Answer two consecutive questions correctly with confidence to cross into Expert (>= 80%)
        val (s1, _) = KnowledgeGraphAdaptiveQuizEngine.processAnswer(
            session = session,
            question = questions[0],
            isCorrect = true,
            confidence = AnswerConfidence.CONFIDENT
        )
        val (s2, _) = KnowledgeGraphAdaptiveQuizEngine.processAnswer(
            session = s1,
            question = questions.getOrElse(1) { questions[0] },
            isCorrect = true,
            confidence = AnswerConfidence.CONFIDENT
        )

        assertTrue("Mastery should reach Expert threshold: ${s2.currentMasteryPercent}", s2.currentMasteryPercent >= 80f)
        assertEquals(KnowledgeMasteryLevel.EXPERT, s2.masteryLevel)
        assertTrue("Downstream node 'compose' should be unlocked", s2.unlockedDownstreamNodes.contains("compose"))
    }

    @Test
    fun testQuestionBank_hasCoverageAcrossAllTiers() {
        val nodeIds = listOf("html", "css", "js", "php", "mysql", "python", "compose", "coroutines", "tflite", "spaced_rep", "big_o", "c_lang")
        for (nodeId in nodeIds) {
            val questions = KnowledgeGraphQuestionBank.getCuratedQuestionsForNode(nodeId)
            assertTrue("Node $nodeId should have questions in the bank", questions.isNotEmpty())
            val foundational = questions.any { it.difficultyTier == KnowledgeQuizDifficultyTier.FOUNDATIONAL }
            val coreOrExpert = questions.any { it.difficultyTier != KnowledgeQuizDifficultyTier.FOUNDATIONAL }
            assertTrue("Node $nodeId should have foundational questions", foundational)
            assertTrue("Node $nodeId should have core or expert questions", coreOrExpert)
        }
    }
}
