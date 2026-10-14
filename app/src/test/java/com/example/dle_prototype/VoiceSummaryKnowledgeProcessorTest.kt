package com.example.dle_prototype

import com.example.dle_prototype.data.graph.KnowledgeEdge
import com.example.dle_prototype.data.graph.KnowledgeGraphEngine
import com.example.dle_prototype.data.speech.VoiceLearningSpeechManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceSummaryKnowledgeProcessorTest {

    @Test
    fun testVoiceSummarySuggestsCoroutinesAndTfliteConnection() {
        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(emptyList())
        val transcript = "Today I learned how Kotlin Coroutines with Dispatchers.IO can execute TFLite quantized neural network inference in background threads without blocking Jetpack Compose."

        val result = KnowledgeGraphEngine.suggestConnectionsFromVoiceSummary(
            transcript = transcript,
            allNodes = graph.nodes,
            existingEdges = graph.edges
        )

        assertNotNull(result)
        assertTrue("Must extract transcript", result.transcript.isNotBlank())
        assertTrue("Must detect topics from spoken words", result.detectedTopics.isNotEmpty())

        val detectedIds = result.detectedTopics.map { it.id }.toSet()
        assertTrue("Should detect coroutines node", detectedIds.contains("coroutines"))
        assertTrue("Should detect tflite node", detectedIds.contains("tflite"))

        assertTrue("Must generate suggested connections", result.suggestions.isNotEmpty())
        val coroutinesTfliteSuggestion = result.suggestions.firstOrNull {
            (it.fromNodeId == "coroutines" && it.toNodeId == "tflite") ||
            (it.fromNodeId == "tflite" && it.toNodeId == "coroutines")
        }
        assertNotNull("Should propose connection between coroutines and tflite", coroutinesTfliteSuggestion)
        assertEquals("Async Neural Inference", coroutinesTfliteSuggestion?.relationshipTitle)
        assertTrue("Confidence should be high", (coroutinesTfliteSuggestion?.confidencePercent ?: 0) >= 85)
        assertTrue("Keywords should be extracted", coroutinesTfliteSuggestion?.matchedKeywords?.isNotEmpty() == true)
    }

    @Test
    fun testVoiceSummarySuggestsMemoryAndBigOConnection() {
        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(emptyList())
        val transcript = "I discovered how C language pointers and malloc heap allocation directly impact Big-O space complexity and memory limits."

        val result = KnowledgeGraphEngine.suggestConnectionsFromVoiceSummary(
            transcript = transcript,
            allNodes = graph.nodes,
            existingEdges = graph.edges
        )

        val detectedIds = result.detectedTopics.map { it.id }.toSet()
        assertTrue("Should detect c_lang", detectedIds.contains("c_lang"))
        assertTrue("Should detect big_o", detectedIds.contains("big_o"))

        val suggestion = result.suggestions.firstOrNull {
            (it.fromNodeId == "c_lang" && it.toNodeId == "big_o") ||
            (it.fromNodeId == "big_o" && it.toNodeId == "c_lang")
        }
        assertNotNull("Should suggest c_lang to big_o connection", suggestion)
        assertEquals("Memory Layout & Complexity", suggestion?.relationshipTitle)
        assertTrue(suggestion?.reasoning?.contains("heap") == true || suggestion?.reasoning?.contains("space complexity") == true)
    }

    @Test
    fun testVoiceSummarySuggestsHtmlAndComposeDeclarativeConnection() {
        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(emptyList())
        val transcript = "I studied how HTML DOM hierarchy and semantic tags correlate with declarative component trees in Jetpack Compose."

        val result = KnowledgeGraphEngine.suggestConnectionsFromVoiceSummary(
            transcript = transcript,
            allNodes = graph.nodes,
            existingEdges = graph.edges
        )

        val suggestion = result.suggestions.firstOrNull {
            (it.fromNodeId == "html" && it.toNodeId == "compose") ||
            (it.fromNodeId == "compose" && it.toNodeId == "html")
        }
        assertNotNull("Should suggest html and compose relationship", suggestion)
        assertEquals("Declarative UI Tree Semantics", suggestion?.relationshipTitle)
        assertFalse("This should be a brand-new connection proposal not in static edges", suggestion?.isAlreadyConnected == true)
    }

    @Test
    fun testVoiceSummarySuggestsSpacedRepetitionAndNeuralAI() {
        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(emptyList())
        val transcript = "I learned that Spaced Repetition SM-2 memory forgetting curves can be calibrated using TFLite neural weight predictions."

        val result = KnowledgeGraphEngine.suggestConnectionsFromVoiceSummary(
            transcript = transcript,
            allNodes = graph.nodes,
            existingEdges = graph.edges
        )

        val suggestion = result.suggestions.firstOrNull {
            (it.fromNodeId == "spaced_rep" && it.toNodeId == "tflite") ||
            (it.fromNodeId == "tflite" && it.toNodeId == "spaced_rep")
        }
        assertNotNull("Should suggest spaced_rep to tflite connection", suggestion)
        assertEquals("Neural Memory Calibration", suggestion?.relationshipTitle)
    }

    @Test
    fun testCustomEdgesIntegrationInGraphEngine() {
        val customEdge = KnowledgeEdge(
            fromId = "html",
            toId = "compose",
            relationship = "Declarative UI Tree Semantics",
            isPrerequisite = false,
            isCustom = true,
            reasoning = "Voice summary correlation"
        )

        val graph = KnowledgeGraphEngine.buildGraphFromTelemetry(
            masteries = emptyList(),
            attempts = emptyList(),
            dueCardsCount = 0,
            customEdges = listOf(customEdge)
        )

        val foundEdge = graph.edges.firstOrNull { it.fromId == "html" && it.toId == "compose" }
        assertNotNull("Custom edge should be merged into graph", foundEdge)
        assertTrue("Edge should be marked as custom", foundEdge?.isCustom == true)
        assertEquals("Declarative UI Tree Semantics", foundEdge?.relationship)
    }

    @Test
    fun testSampleLearningSummariesAvailable() {
        val samples = VoiceLearningSpeechManager.SAMPLE_LEARNING_SUMMARIES
        assertTrue("Must provide at least 4 sample learning summaries", samples.size >= 4)
        samples.forEach { summary ->
            assertTrue("Sample summary should not be empty", summary.isNotBlank())
        }
    }
}
