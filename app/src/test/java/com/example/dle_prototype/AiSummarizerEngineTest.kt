package com.example.dle_prototype

import com.example.dle_prototype.data.ml.OnDeviceSummarizerEngine
import com.example.dle_prototype.data.ml.SummaryDepth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiSummarizerEngineTest {

    private val sampleText = """
        Jetpack Compose is Android's modern declarative UI toolkit designed to simplify and accelerate UI development.
        At the heart of Compose is the concept of state management, where UI automatically recomposes whenever underlying state changes.
        MutableStateFlow and StateFlow provide reactive, lifecycle-aware data streams that safely emit state updates to composables.
        State hoisting is a crucial architectural pattern in Compose that decouples UI rendering from business logic, making components reusable and testable.
        Side-effects should be managed using LaunchedEffect and rememberCoroutineScope to prevent blocking the main UI thread during asynchronous operations.
        Finally, preview annotations enable rapid prototyping and layout inspection directly within the IDE without requiring full device deployment.
    """.trimIndent()

    @Test
    fun testSentenceSplitting() {
        val sentences = OnDeviceSummarizerEngine.splitIntoSentences(sampleText)
        assertEquals(6, sentences.size)
        assertTrue(sentences[0].contains("declarative UI toolkit"))
        assertTrue(sentences[1].contains("state management"))
        assertTrue(sentences[3].contains("State hoisting"))
    }

    @Test
    fun testFeatureExtraction() {
        val sentences = OnDeviceSummarizerEngine.splitIntoSentences(sampleText)
        val termFreq = mapOf("compose" to 3, "state" to 4, "ui" to 2)

        val features = OnDeviceSummarizerEngine.extractFeatures(
            sentence = sentences[0],
            index = 0,
            totalSentences = sentences.size,
            globalTermFreq = termFreq
        )

        assertEquals(5, features.size)
        for (i in 0 until 5) {
            assertTrue("Feature $i must be >= 0f", features[i] >= 0f)
            assertTrue("Feature $i must be <= 1f", features[i] <= 1f)
        }
        // First sentence position weight should be high
        assertTrue("First sentence position weight must be high", features[0] >= 0.8f)
    }

    @Test
    fun testSummarizationConciseDepth() = runBlocking {
        val result = OnDeviceSummarizerEngine.summarize(
            context = null,
            text = sampleText,
            depth = SummaryDepth.CONCISE
        )

        assertNotNull(result)
        assertEquals(3, result.bulletPoints.size)
        for (bullet in result.bulletPoints) {
            assertTrue("Bullet must start with bullet symbol", bullet.startsWith("• "))
            assertTrue("Bullet must have substantial text", bullet.length > 20)
        }
        assertTrue("Word count must be reduced", result.summaryWordCount < result.originalWordCount)
        assertTrue("Compression ratio must be positive", result.compressionRatioPercent > 0)
        assertTrue("Seconds saved must be positive", result.estimatedSecondsSaved > 0)
        assertTrue("Latency must be positive", result.inferenceLatencyMs > 0L)
        assertTrue("Key concepts must be extracted", result.keyConcepts.isNotEmpty())
    }

    @Test
    fun testSummarizationStandardDepth() = runBlocking {
        val result = OnDeviceSummarizerEngine.summarize(
            context = null,
            text = sampleText,
            depth = SummaryDepth.STANDARD
        )

        assertNotNull(result)
        assertEquals(4, result.bulletPoints.size)
        assertTrue(result.originalWordCount > 80)
        assertTrue(result.summaryWordCount > 0)
        assertTrue(result.compressionRatioPercent in 10..90)
        assertEquals(6, result.sentenceCount)
    }

    @Test
    fun testEmptyAndShortTextEdgeCases() = runBlocking {
        val emptyResult = OnDeviceSummarizerEngine.summarize(
            context = null,
            text = "   ",
            depth = SummaryDepth.STANDARD
        )
        assertEquals(0, emptyResult.bulletPoints.size)
        assertEquals(0, emptyResult.originalWordCount)

        val shortResult = OnDeviceSummarizerEngine.summarize(
            context = null,
            text = "Kotlin is concise.",
            depth = SummaryDepth.CONCISE
        )
        assertEquals(1, shortResult.bulletPoints.size)
        assertTrue(shortResult.bulletPoints[0].startsWith("• "))
    }

    @Test
    fun testKeywordExtraction() {
        val keywords = OnDeviceSummarizerEngine.extractKeywords(sampleText)
        assertTrue(keywords.isNotEmpty())
        assertTrue("Should extract domain keywords like Compose or State", keywords.any { it.equals("Compose", ignoreCase = true) || it.equals("State", ignoreCase = true) })
    }
}
