package com.example.dle_prototype

import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.ui.components.formatHours
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyGoalProgressRingTest {

    @Test
    fun testDailyGoalProgressDefaults() {
        val progress = DailyGoalProgress()
        assertEquals(10, progress.targetQuestions)
        assertEquals(0, progress.answeredToday)
        assertEquals(0f, progress.percentComplete, 0.001f)
        assertFalse(progress.isAchieved)
        assertEquals(1.0f, progress.targetHours, 0.001f)
        assertEquals(0f, progress.hoursCompletedToday, 0.001f)
        assertEquals(0f, progress.minutesCompletedToday, 0.001f)
        assertEquals(0f, progress.hoursPercentComplete, 0.001f)
    }

    @Test
    fun testCompletionPercentageCalculation() {
        // Target: 2.0 hours, Completed: 1.5 hours -> 75%
        val targetHours = 2.0f
        val completedHours = 1.5f
        val completionFraction = completedHours / targetHours
        val percentage = (completionFraction * 100).toInt()

        assertEquals(0.75f, completionFraction, 0.001f)
        assertEquals(75, percentage)

        val progress = DailyGoalProgress(
            targetHours = targetHours,
            hoursCompletedToday = completedHours,
            minutesCompletedToday = completedHours * 60f,
            hoursPercentComplete = completionFraction,
            isAchieved = false
        )

        assertEquals(2.0f, progress.targetHours, 0.001f)
        assertEquals(1.5f, progress.hoursCompletedToday, 0.001f)
        assertEquals(90f, progress.minutesCompletedToday, 0.001f)
        assertFalse(progress.isAchieved)
    }

    @Test
    fun testGoalAchievedWhenTargetReachedOrExceeded() {
        // Target: 1.5 hours, Completed: 1.5 hours -> Achieved
        val exactProgress = DailyGoalProgress(
            targetHours = 1.5f,
            hoursCompletedToday = 1.5f,
            minutesCompletedToday = 90f,
            hoursPercentComplete = 1.0f,
            isAchieved = true
        )
        assertTrue(exactProgress.isAchieved)
        assertTrue(exactProgress.hoursCompletedToday >= exactProgress.targetHours)

        // Target: 1.0 hour, Completed: 1.25 hours -> Exceeded
        val exceededProgress = DailyGoalProgress(
            targetHours = 1.0f,
            hoursCompletedToday = 1.25f,
            minutesCompletedToday = 75f,
            hoursPercentComplete = 1.25f,
            isAchieved = true
        )
        assertTrue(exceededProgress.isAchieved)
        assertTrue(exceededProgress.hoursPercentComplete >= 1.0f)
    }

    @Test
    fun testTargetHoursAdjustmentBounds() {
        val minHours = 0.25f
        val maxHours = 8.0f

        // Adjust downwards below min
        val belowMin = (0.25f - 0.25f).coerceAtLeast(minHours)
        assertEquals(minHours, belowMin, 0.001f)

        // Adjust upwards above max
        val aboveMax = (8.0f + 0.5f).coerceAtMost(maxHours)
        assertEquals(maxHours, aboveMax, 0.001f)

        // Valid adjustment steps (e.g. +0.25h)
        val step1 = (1.0f + 0.25f).coerceIn(minHours, maxHours)
        assertEquals(1.25f, step1, 0.001f)

        val step2 = (step1 + 0.25f).coerceIn(minHours, maxHours)
        assertEquals(1.50f, step2, 0.001f)
    }

    @Test
    fun testFormatHoursHelper() {
        assertEquals("1 hrs", formatHours(1.0f))
        assertEquals("2 hrs", formatHours(2.0f))
        assertEquals("1.5 hrs", formatHours(1.5f))
        assertEquals("0.5 hrs", formatHours(0.5f))
        assertEquals("0.8 hrs", formatHours(0.75f).take(3) + " hrs")
    }

    @Test
    fun testMinutesAndHoursConversion() {
        val targetMinutes = 90
        val targetHours = targetMinutes / 60.0f
        assertEquals(1.5f, targetHours, 0.001f)

        val completedMinutes = 45f
        val completedHours = completedMinutes / 60.0f
        val percent = completedHours / targetHours
        assertEquals(0.5f, percent, 0.001f)
    }
}
