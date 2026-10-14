package com.example.dle_prototype

import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.graph.KnowledgeGraphEngine
import com.example.dle_prototype.data.graph.MasteryTrendDayPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Recharts30DayMasteryTrendsTest {

    @Test
    fun testRechartsMasteryTrendsHtmlAssetExistsAndHasRecharts() {
        val candidates = listOf(
            File("src/main/assets/recharts_mastery_trends.html"),
            File("app/src/main/assets/recharts_mastery_trends.html"),
            File("applet/src/main/assets/recharts_mastery_trends.html")
        )
        val assetFile = candidates.firstOrNull { it.exists() }
        assertNotNull("recharts_mastery_trends.html must exist in app assets", assetFile)

        val content = assetFile!!.readText()
        assertTrue("HTML asset must include Recharts library script", content.contains("recharts@2.12.7/umd/Recharts.min.js"))
        assertTrue("HTML asset must include React script", content.contains("react@18/umd/react.production.min.js"))
        assertTrue("HTML asset must provide window.updateMasteryTrendsData function", content.contains("window.updateMasteryTrendsData"))
        assertTrue("HTML asset must provide window.setMetricView function", content.contains("window.setMetricView"))
        assertTrue("HTML asset must provide window.setTimeWindow function", content.contains("window.setTimeWindow"))
        assertTrue("HTML asset must contain fallback SVG renderer", content.contains("renderFallbackSvg"))
        assertTrue("HTML asset must have chart container", content.contains("chart-wrapper"))
    }

    @Test
    fun test30DayMasteryTrendsDataGenerationAndProgression() {
        val testAttempts = listOf(
            QuizAttempt(
                id = 1,
                username = "test_user",
                category = "Jetpack Compose",
                score = 9,
                totalQuestions = 10,
                difficultyLevel = 2f,
                timestamp = System.currentTimeMillis() - 86400000L * 2
            ),
            QuizAttempt(
                id = 2,
                username = "test_user",
                category = "Coroutines",
                score = 8,
                totalQuestions = 10,
                difficultyLevel = 2f,
                timestamp = System.currentTimeMillis() - 86400000L * 1
            )
        )

        val summary = KnowledgeGraphEngine.build30DayMasteryTrends(
            attempts = testAttempts,
            currentMasteryPercent = 88.0f,
            currentExpertTopics = 4,
            currentIntermediateTopics = 5,
            currentBeginnerTopics = 1
        )

        assertEquals("Must generate exactly 30 daily progress points", 30, summary.points.size)
        assertTrue("Start mastery must be positive", summary.startMastery > 0f)
        assertTrue("Current mastery must be >= start mastery", summary.currentMastery >= summary.startMastery)
        assertTrue("Growth delta must be >= 0", summary.growthDelta >= 0f)
        assertTrue("Total quizzes count must be >= 30", summary.total30DayQuizzes >= 30)
        assertTrue("Average accuracy must be valid percentage", summary.averageAccuracy in 50f..100f)

        // Verify progression points integrity
        val firstPoint = summary.points.first()
        val lastPoint = summary.points.last()

        assertEquals(1, firstPoint.dayNumber)
        assertEquals(30, lastPoint.dayNumber)
        assertTrue("Last point must be marked as today", lastPoint.isToday)
        assertEquals(88.0f, lastPoint.masteryScore, 0.1f)

        // Tiers progression
        assertTrue("Expert topics count must increase or stay stable", lastPoint.expertTopicsCount >= firstPoint.expertTopicsCount)
    }

    @Test
    fun testMasteryTrendPointsDataIntegrity() {
        val points = (1..30).map { day ->
            MasteryTrendDayPoint(
                dayNumber = day,
                dateLabel = "Day $day",
                masteryScore = 40f + day * 1.5f,
                accuracyPercent = 70f + day * 0.8f,
                expertTopicsCount = day / 10,
                intermediateTopicsCount = 3,
                beginnerTopicsCount = (30 - day) / 10,
                xpEarned = 50 + day * 2,
                quizzesTaken = 2,
                focusMinutes = 20,
                highlightMilestone = if (day == 15) "Milestone" else null,
                isToday = day == 30
            )
        }

        assertEquals(30, points.size)
        val first = points.first()
        assertEquals(1, first.dayNumber)
        assertEquals("Day 1", first.dateLabel)
        assertEquals(41.5f, first.masteryScore, 0.01f)

        val last = points.last()
        assertEquals(30, last.dayNumber)
        assertTrue(last.isToday)
        assertEquals(85.0f, last.masteryScore, 0.01f)

        // Ensure all 30 points have valid non-negative metrics
        points.forEach { pt ->
            assertTrue(pt.dayNumber in 1..30)
            assertTrue(pt.masteryScore > 0f)
            assertTrue(pt.accuracyPercent > 0f)
            assertTrue(pt.xpEarned > 0)
            assertTrue(pt.quizzesTaken > 0)
        }
    }
}
