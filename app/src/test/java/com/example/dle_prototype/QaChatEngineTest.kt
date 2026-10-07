package com.example.dle_prototype

import com.example.dle_prototype.data.qa.QaChatEngine
import com.example.dle_prototype.data.qa.QaChatMessage
import com.example.dle_prototype.data.qa.QaResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QaChatEngineTest {

    @Test
    fun testKnowledgeBaseCurriculumIntegrity() {
        val kb = QaChatEngine.KNOWLEDGE_BASE
        assertTrue("Knowledge base should contain curated curriculum entries", kb.isNotEmpty())
        assertTrue("Knowledge base should contain at least 5 core curriculum topics", kb.size >= 5)

        for (entry in kb) {
            assertTrue("Entry ID must not be blank: ${entry.id}", entry.id.isNotBlank())
            assertTrue("Entry Category must not be blank: ${entry.category}", entry.category.isNotBlank())
            assertTrue("Entry Title must not be blank: ${entry.title}", entry.title.isNotBlank())
            assertTrue("Entry Explanation must not be blank: ${entry.id}", entry.explanation.isNotBlank())
            assertTrue("Entry Keywords must have at least one keyword: ${entry.id}", entry.keywords.isNotEmpty())
        }

        // Validate major core categories exist
        val categories = kb.map { it.category }.toSet()
        assertTrue("Must include Jetpack Compose", categories.contains("Jetpack Compose"))
        assertTrue("Must include Kotlin & Concurrency", categories.contains("Kotlin & Concurrency"))
        assertTrue("Must include Machine Learning & AI", categories.contains("Machine Learning & AI"))
        assertTrue("Must include Algorithms & Data Structures", categories.contains("Algorithms & Data Structures"))
        assertTrue("Must include Learning Science", categories.contains("Learning Science"))
    }

    @Test
    fun testInitialPromptSuggestions() {
        val prompts = QaChatEngine.getInitialPromptSuggestions()
        assertTrue("Should offer initial prompt suggestions", prompts.size >= 5)
        assertTrue("Prompts should include Jetpack Compose prompt", prompts.any { it.contains("Compose") })
        assertTrue("Prompts should include Coroutines prompt", prompts.any { it.contains("Coroutines") })
        assertTrue("Prompts should include TFLite prompt", prompts.any { it.contains("TFLite") })
        assertTrue("Prompts should include Streak prompt", prompts.any { it.contains("streak") })
    }

    @Test
    fun testComposeStateQueryRetrieval() = runBlocking {
        val response = QaChatEngine.generateResponse(
            query = "How do state and recomposition work in Compose?",
            username = "test_user"
        )

        assertEquals("Jetpack Compose", response.category)
        assertTrue("Confidence should be high for direct keyword match", response.confidence >= 0.7f)
        assertTrue("Answer should explain state", response.answer.contains("State"))
        assertTrue("Answer should mention remember", response.answer.contains("remember"))
        assertNotNull("Should provide relevant code snippet for compose state", response.codeSnippet)
        assertTrue("Code snippet should contain Composable", response.codeSnippet?.contains("@Composable") == true)
        assertTrue("Suggested follow-ups should not be empty", response.suggestedFollowUps.isNotEmpty())
    }

    @Test
    fun testSideEffectsQueryRetrieval() = runBlocking {
        val response = QaChatEngine.generateResponse(
            query = "What is LaunchedEffect and how do side effects work?",
            username = "test_user"
        )

        assertEquals("Jetpack Compose", response.category)
        assertTrue("Answer should describe side effect", response.answer.contains("Side Effect"))
        assertTrue("Answer should mention LaunchedEffect", response.answer.contains("LaunchedEffect"))
        assertNotNull("Code snippet should be provided", response.codeSnippet)
    }

    @Test
    fun testKotlinCoroutinesQueryRetrieval() = runBlocking {
        val response = QaChatEngine.generateResponse(
            query = "Explain Kotlin coroutines and dispatchers",
            username = "test_user"
        )

        assertEquals("Kotlin & Concurrency", response.category)
        assertTrue("Answer should mention coroutines", response.answer.contains("Coroutines"))
        assertTrue("Answer should mention Dispatchers", response.answer.contains("Dispatchers"))
        assertNotNull("Code snippet should be provided", response.codeSnippet)
        assertTrue(response.suggestedFollowUps.isNotEmpty())
    }

    @Test
    fun testFlowVsStateFlowQueryRetrieval() = runBlocking {
        val response = QaChatEngine.generateResponse(
            query = "What is the difference between Flow and StateFlow?",
            username = "test_user"
        )

        assertEquals("Kotlin & Concurrency", response.category)
        assertTrue("Answer should mention StateFlow", response.answer.contains("StateFlow"))
        assertTrue("Answer should explain cold vs hot stream", response.answer.contains("cold") && response.answer.contains("hot"))
    }

    @Test
    fun testTfliteOnDeviceQueryRetrieval() = runBlocking {
        val response = QaChatEngine.generateResponse(
            query = "How does on-device TensorFlow Lite neural inference work in this app?",
            username = "test_user"
        )

        assertEquals("Machine Learning & AI", response.category)
        assertTrue("Answer should mention TFLite", response.answer.contains("TensorFlow Lite"))
        assertTrue("Answer should mention privacy or on-device", response.answer.contains("privacy") || response.answer.contains("on-device"))
        assertNotNull(response.codeSnippet)
    }

    @Test
    fun testGradientDescentQueryRetrieval() = runBlocking {
        val response = QaChatEngine.generateResponse(
            query = "What is gradient descent and learning rate?",
            username = "test_user"
        )

        assertEquals("Machine Learning & AI", response.category)
        assertTrue("Answer should mention gradient", response.answer.contains("Gradient"))
        assertTrue("Answer should mention loss function", response.answer.contains("Loss Function"))
    }

    @Test
    fun testBigOComplexityQueryRetrieval() = runBlocking {
        val response = QaChatEngine.generateResponse(
            query = "Explain Big-O notation and time complexity with binary search",
            username = "test_user"
        )

        assertEquals("Algorithms & Data Structures", response.category)
        assertTrue("Answer should mention O(log n)", response.answer.contains("O(log n)"))
        assertNotNull(response.codeSnippet)
        assertTrue("Snippet should demonstrate binary search", response.codeSnippet?.contains("binarySearch") == true)
    }

    @Test
    fun testSpacedRepetitionQueryRetrieval() = runBlocking {
        val response = QaChatEngine.generateResponse(
            query = "How does spaced repetition and SM-2 flashcard scheduling work?",
            username = "test_user"
        )

        assertEquals("Learning Science", response.category)
        assertTrue("Answer should mention Spaced Repetition", response.answer.contains("Spaced Repetition"))
        assertTrue("Answer should mention forgetting curve or SuperMemo", response.answer.contains("forgetting curve") || response.answer.contains("SuperMemo"))
    }

    @Test
    fun testConceptualSynthesisFallbackForNovelQuery() = runBlocking {
        val response = QaChatEngine.generateResponse(
            query = "Explain event-driven microservices domain boundaries",
            username = "test_user"
        )

        assertEquals("Conceptual Synthesis", response.category)
        assertTrue("Confidence should reflect synthetic response", response.confidence in 0.5f..0.85f)
        assertTrue("Answer should contain structured guidance", response.answer.contains("Core Concept"))
        assertTrue("Answer should contain best practice recommendation", response.answer.contains("Best Practice"))
        assertTrue("Suggested follow-ups should be offered", response.suggestedFollowUps.isNotEmpty())
    }

    @Test
    fun testQaChatMessageDataModel() {
        val now = 1700000000000L
        val msg = QaChatMessage(
            id = 42L,
            username = "charlie",
            sender = "user",
            message = "What is State?",
            timestamp = now
        )

        assertEquals(42L, msg.id)
        assertEquals("charlie", msg.username)
        assertEquals("user", msg.sender)
        assertEquals("What is State?", msg.message)
        assertEquals(now, msg.timestamp)
    }

    @Test
    fun testQaResponseDataModel() {
        val followUps = listOf("Followup 1", "Followup 2")
        val resp = QaResponse(
            answer = "Detailed explanation",
            category = "Architecture",
            confidence = 0.95f,
            codeSnippet = "val x = 1",
            suggestedFollowUps = followUps
        )

        assertEquals("Detailed explanation", resp.answer)
        assertEquals("Architecture", resp.category)
        assertEquals(0.95f, resp.confidence, 0.001f)
        assertEquals("val x = 1", resp.codeSnippet)
        assertEquals(2, resp.suggestedFollowUps.size)
    }
}
