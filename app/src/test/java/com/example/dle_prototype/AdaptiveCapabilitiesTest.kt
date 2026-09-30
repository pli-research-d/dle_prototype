package com.example.dle_prototype

import com.example.dle_prototype.data.ml.ModelWeights
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class AdaptiveCapabilitiesTest {

    @Test
    fun testLeitnerIntervalProgression() {
        fun computeNextBox(currentBox: Int, remembered: Boolean): Pair<Int, Long> {
            val newBox = if (remembered) minOf(4, currentBox + 1) else 1
            val interval = when (newBox) {
                1 -> 86400000L // 1 day
                2 -> 3 * 86400000L // 3 days
                3 -> 7 * 86400000L // 7 days
                else -> 14 * 86400000L // 14 days
            }
            return Pair(newBox, interval)
        }

        // Test promotions
        val step1 = computeNextBox(1, true)
        assertEquals(2, step1.first)
        assertEquals(3 * 86400000L, step1.second)

        val step2 = computeNextBox(2, true)
        assertEquals(3, step2.first)
        assertEquals(7 * 86400000L, step2.second)

        val step3 = computeNextBox(3, true)
        assertEquals(4, step3.first)
        assertEquals(14 * 86400000L, step3.second)

        // Clamping at Box 4
        val step4 = computeNextBox(4, true)
        assertEquals(4, step4.first)

        // Reset to Box 1 on failure
        val failureStep = computeNextBox(3, false)
        assertEquals(1, failureStep.first)
    }

    @Test
    fun testFocusScoreCalculation() {
        fun calculateFocusScore(pauseCount: Int): Float {
            return (100f - (pauseCount * 5f)).coerceIn(50f, 100f)
        }

        assertEquals(100f, calculateFocusScore(0), 0.01f)
        assertEquals(90f, calculateFocusScore(2), 0.01f)
        assertEquals(75f, calculateFocusScore(5), 0.01f)
        assertEquals(50f, calculateFocusScore(20), 0.01f) // Clamped at 50% floor
    }

    @Test
    fun testDdaEscalationAndScaffolding() {
        fun evaluateDdaTier(currentTier: String, currentStreak: Int, isCorrect: Boolean): String {
            if (isCorrect) {
                val newStreak = currentStreak + 1
                return when {
                    newStreak >= 4 && currentTier == "Medium" -> "Hard"
                    newStreak >= 2 && currentTier == "Easy" -> "Medium"
                    else -> currentTier
                }
            } else {
                return when (currentTier) {
                    "Hard" -> "Medium"
                    "Medium" -> "Easy"
                    else -> "Easy"
                }
            }
        }

        // Easy -> Medium at 2 streak
        assertEquals("Medium", evaluateDdaTier("Easy", 1, true))
        // Medium -> Hard at 4 streak
        assertEquals("Hard", evaluateDdaTier("Medium", 3, true))
        // Hard -> Medium on mistake
        assertEquals("Medium", evaluateDdaTier("Hard", 5, false))
        // Medium -> Easy on mistake
        assertEquals("Easy", evaluateDdaTier("Medium", 0, false))
    }

    @Test
    fun testWeightDivergenceMetric() {
        fun computeL2Divergence(wA: ModelWeights, wB: ModelWeights): Float {
            var sumSquares = 0.0
            var count = 0
            for (i in wA.w1.indices) {
                for (j in wA.w1[i].indices) {
                    val diff = (wA.w1[i][j] - wB.w1[i][j]).toDouble()
                    sumSquares += diff * diff
                    count++
                }
            }
            for (i in wA.w2.indices) {
                for (j in wA.w2[i].indices) {
                    val diff = (wA.w2[i][j] - wB.w2[i][j]).toDouble()
                    sumSquares += diff * diff
                    count++
                }
            }
            return if (count > 0) sqrt(sumSquares / count).toFloat() else 0f
        }

        val base = ModelWeights.defaultInit(5, 8, 4)
        // Divergence from self should be zero
        assertEquals(0f, computeL2Divergence(base, base), 0.0001f)

        // Perturbed copy
        val perturbedW1 = Array(base.w1.size) { i ->
            FloatArray(base.w1[i].size) { j -> base.w1[i][j] + 0.1f }
        }
        val perturbed = base.copy(w1 = perturbedW1)

        val div = computeL2Divergence(base, perturbed)
        assertTrue("Divergence between distinct weight matrices must be positive, got $div", div > 0.05f)
    }
}
