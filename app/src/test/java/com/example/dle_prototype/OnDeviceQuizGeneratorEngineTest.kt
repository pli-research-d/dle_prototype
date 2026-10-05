package com.example.dle_prototype

import com.example.dle_prototype.data.ml.OnDeviceQuizGeneratorEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnDeviceQuizGeneratorEngineTest {

    private val sampleBullets = listOf(
        "• State hoisting is a crucial architectural pattern in Compose that decouples UI rendering from business logic, making components reusable and testable.",
        "• MutableStateFlow and StateFlow provide reactive, lifecycle-aware data streams that safely emit state updates to composables.",
        "• Side-effects should be managed using LaunchedEffect and rememberCoroutineScope to prevent blocking the main UI thread during asynchronous operations.",
        "• Jetpack Compose is Android's modern declarative UI toolkit designed to simplify and accelerate UI development."
    )

    private val sampleConcepts = listOf("State Hoisting", "MutableStateFlow", "LaunchedEffect", "Jetpack Compose")

    @Test
    fun testConceptAndPredicateExtraction() {
        val (concept, predicate) = OnDeviceQuizGeneratorEngine.extractConceptAndPredicate(
            bullet = "Backpropagation calculates gradients of the loss function using the chain rule",
            keyConcepts = listOf("Backpropagation", "Neural Network")
        )

        assertEquals("Backpropagation", concept)
        assertTrue("Predicate must contain calculation details", predicate.contains("calculates gradients"))
    }

    @Test
    fun testQuizGenerationFromSummaryBullets() = runBlocking {
        val quiz = OnDeviceQuizGeneratorEngine.generateQuizFromSummary(
            context = null,
            summaryBullets = sampleBullets,
            keyConcepts = sampleConcepts,
            rawText = sampleBullets.joinToString(" "),
            maxQuestions = 4
        )

        assertNotNull(quiz)
        assertTrue("Must generate at least 2 questions", quiz.questions.size >= 2)
        assertTrue("Generation latency must be positive", quiz.generationLatencyMs > 0L)
        assertEquals("On-Device TFLite (dle_model)", quiz.modelSource)

        for (q in quiz.questions) {
            assertTrue("Question stem must not be empty", q.question.isNotBlank())
            assertTrue("Question stem should contain question mark", q.question.contains("?"))
            assertEquals("MCQ must have 4 options", 4, q.options.size)
            assertTrue("Correct option index must be valid", q.correctOptionIndex in 0..3)
            assertTrue("Explanation must be non-empty", q.explanation.isNotBlank())
            assertTrue("Difficulty must be valid", q.difficulty in listOf("Easy", "Medium", "Hard"))
            assertTrue("TFLite score must be > 0", q.tfliteScore > 0f)

            // Distinct options
            assertEquals("All options in an MCQ must be distinct", 4, q.options.distinct().size)
        }
    }

    @Test
    fun testQuestionFeatureExtraction() {
        val (concept, predicate) = OnDeviceQuizGeneratorEngine.extractConceptAndPredicate(
            bullet = sampleBullets[0],
            keyConcepts = sampleConcepts
        )

        val q = com.example.dle_prototype.data.ml.GeneratedMcqQuestion(
            id = 1,
            question = "What is the primary role of $concept?",
            options = listOf("Option A", "Option B", "Option C", "Option D"),
            correctOptionIndex = 0,
            explanation = "$concept $predicate",
            keyConcept = concept,
            difficulty = "Medium",
            tfliteScore = 0.5f
        )

        val features = OnDeviceQuizGeneratorEngine.extractQuestionFeatures(
            q = q,
            rawText = sampleBullets.joinToString(" "),
            bulletCount = sampleBullets.size
        )

        assertEquals(5, features.size)
        for (i in 0 until 5) {
            assertTrue("Feature $i must be >= 0f", features[i] >= 0f)
            assertTrue("Feature $i must be <= 1f", features[i] <= 1f)
        }
    }

    @Test
    fun testConversionToAppQuestion() {
        val q = com.example.dle_prototype.data.ml.GeneratedMcqQuestion(
            id = 2,
            question = "Which component manages state in Compose?",
            options = listOf("StateFlow", "LiveData", "Observable", "Thread"),
            correctOptionIndex = 0,
            explanation = "StateFlow provides lifecycle-aware data streams.",
            keyConcept = "StateFlow",
            difficulty = "Medium",
            tfliteScore = 0.6f
        )

        val appQ = q.toAppQuestion()
        assertEquals("mcq", appQ.type)
        assertEquals("AI Generated", appQ.category)
        assertEquals("Medium", appQ.difficulty)
        assertEquals(4, appQ.options.size)
        assertEquals("StateFlow", appQ.answer)
        assertEquals(appQ.options[q.correctOptionIndex], appQ.answer)
        assertTrue(appQ.example.contains("StateFlow"))
    }
}
