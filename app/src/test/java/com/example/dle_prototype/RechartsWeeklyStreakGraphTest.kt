package com.example.dle_prototype

import com.example.dle_prototype.ui.components.StreakDayActivity
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RechartsWeeklyStreakGraphTest {

    @Test
    fun testRechartsWeeklyStreakHtmlAssetExistsAndHasRecharts() {
        val candidates = listOf(
            File("src/main/assets/recharts_weekly_streak.html"),
            File("app/src/main/assets/recharts_weekly_streak.html"),
            File("applet/src/main/assets/recharts_weekly_streak.html")
        )
        val assetFile = candidates.firstOrNull { it.exists() }
        assertNotNull("recharts_weekly_streak.html must exist in app assets", assetFile)

        val content = assetFile!!.readText()
        assertTrue("HTML asset must include Recharts library script", content.contains("recharts@2.12.7/umd/Recharts.min.js"))
        assertTrue("HTML asset must include React script", content.contains("react@18/umd/react.production.min.js"))
        assertTrue("HTML asset must provide window.updateStreakData function", content.contains("window.updateStreakData"))
        assertTrue("HTML asset must contain fallback SVG renderer", content.contains("renderFallbackSvg"))
        assertTrue("HTML asset must have chart container", content.contains("chart-wrapper"))
    }

    @Test
    fun testStreakDayActivityDataIntegrity() {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val testData = days.mapIndexed { index, day ->
            StreakDayActivity(
                day = day,
                streak = index + 1,
                xp = (index + 1) * 20,
                questions = (index + 1) * 2,
                minutes = (index + 1) * 5,
                goalMet = index >= 2,
                isToday = index == 6
            )
        }

        assertEquals(7, testData.size)

        val firstDay = testData[0]
        assertEquals("Mon", firstDay.day)
        assertEquals(1, firstDay.streak)
        assertEquals(20, firstDay.xp)
        assertFalse(firstDay.goalMet)
        assertFalse(firstDay.isToday)

        val lastDay = testData[6]
        assertEquals("Sun", lastDay.day)
        assertEquals(7, lastDay.streak)
        assertEquals(140, lastDay.xp)
        assertTrue(lastDay.goalMet)
        assertTrue(lastDay.isToday)

        // Verify all 7 days have valid, increasing streaks and positive stats
        for (i in 0 until testData.size) {
            assertTrue(testData[i].streak > 0)
            assertTrue(testData[i].xp > 0)
            assertTrue(testData[i].questions > 0)
            assertTrue(testData[i].minutes > 0)
            if (i > 0) {
                assertTrue(testData[i].streak >= testData[i - 1].streak)
            }
        }
    }
}
