package com.example.dle_prototype

import com.example.dle_prototype.data.Question
import com.example.dle_prototype.data.ml.AnswerConfidence
import com.example.dle_prototype.ui.viewmodels.QuizViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuizViewModelTest {

    private lateinit var viewModel: QuizViewModel
    private val testQuestions = listOf(
        Question(
            type = "mcq",
            category = "General Science",
            difficulty = "Easy",
            question = "What does AI stand for?",
            options = listOf("Artificial Intelligence", "Automated Interface", "Active Info", "Auto Input"),
            answer = "Artificial Intelligence",
            explanation = "AI stands for Artificial Intelligence.",
            example = "Machine learning is a subset of AI."
        ),
        Question(
            type = "true_false",
            category = "General Science",
            difficulty = "Medium",
            question = "Supervised learning requires labeled training data.",
            options = listOf("True", "False"),
            answer = "True",
            explanation = "Supervised models require input-output pairs.",
            example = "Classification is supervised."
        ),
        Question(
            type = "mcq",
            category = "General Science",
            difficulty = "Hard",
            question = "Which gradient descent variant uses momentum?",
            options = listOf("Adam", "SGD", "Adagrad", "RMSprop"),
            answer = "Adam",
            explanation = "Adam computes adaptive learning rates with momentum.",
            example = "Adam optimizer in deep learning."
        )
    )

    @Before
    fun setUp() {
        viewModel = QuizViewModel()
        viewModel.initializeQuiz(testQuestions, initialTier = "Hard", initialComplexity = 2.0f)
    }

    @Test
    fun testInitialState() {
        val state = viewModel.uiState.value
        assertEquals(3, state.totalQuestions)
        assertEquals(0, state.currentIndex)
        assertEquals("Hard", state.currentTier)
        assertEquals(2.0f, state.currentComplexity, 0.001f)
        assertNull(state.selectedAnswer)
        assertNull(state.selectedConfidence)
        assertFalse(state.isAnswerSubmitted)
    }

    @Test
    fun testTrackSelectedConfidenceState() {
        viewModel.selectConfidence(AnswerConfidence.CONFIDENT)
        assertEquals(AnswerConfidence.CONFIDENT, viewModel.uiState.value.selectedConfidence)

        viewModel.selectConfidence(AnswerConfidence.GUESSING)
        assertEquals(AnswerConfidence.GUESSING, viewModel.uiState.value.selectedConfidence)
    }

    @Test
    fun testConfidentAndCorrectStandardScoreProgression() {
        viewModel.selectAnswer("Artificial Intelligence")
        viewModel.selectConfidence(AnswerConfidence.CONFIDENT)
        viewModel.submitAnswer(confidence = AnswerConfidence.CONFIDENT, timeTakenSeconds = 5)

        val state = viewModel.uiState.value
        assertTrue(state.isAnswerSubmitted)
        assertEquals(true, state.isCorrect)
        assertEquals(1, state.score)
        assertEquals(1, state.streak)
        assertEquals(0, state.consecutiveErrors)
        assertTrue("Complexity should increase", state.currentComplexity > 2.0f)
        assertNotNull(state.lastAdjustment)
        assertTrue(state.ddaMessage?.contains("Confident") == true)
    }

    @Test
    fun testConfidentAndIncorrectPenaltyToLowerLevels() {
        viewModel.selectAnswer("Automated Interface") // Wrong answer
        viewModel.selectConfidence(AnswerConfidence.CONFIDENT)
        viewModel.submitAnswer(confidence = AnswerConfidence.CONFIDENT, timeTakenSeconds = 10)

        val state = viewModel.uiState.value
        assertTrue(state.isAnswerSubmitted)
        assertEquals(false, state.isCorrect)
        assertEquals(0, state.score)
        assertEquals(0, state.streak)
        assertEquals(1, state.consecutiveErrors)

        // Hard tier drops to lower level (Medium)
        assertEquals("Medium", state.currentTier)
        assertTrue("Complexity should be penalized", state.currentComplexity < 2.0f)
        assertTrue(state.ddaMessage?.contains("Misconception") == true || state.ddaMessage?.contains("penalty") == true)
    }

    @Test
    fun testGuessingAndCorrectLeadsToLowerLevelsWith50PercentPenalty() {
        // First determine full penalty from a wrong answer
        val wrongVm = QuizViewModel()
        wrongVm.initializeQuiz(testQuestions, initialTier = "Hard", initialComplexity = 2.0f)
        wrongVm.selectAnswer("Automated Interface")
        wrongVm.submitAnswer(confidence = AnswerConfidence.CONFIDENT, timeTakenSeconds = 10)
        val fullPenalty = 2.0f - wrongVm.uiState.value.currentComplexity

        // Now test Guessing & Correct
        viewModel.selectAnswer("Artificial Intelligence") // Correct answer
        viewModel.selectConfidence(AnswerConfidence.GUESSING)
        viewModel.submitAnswer(confidence = AnswerConfidence.GUESSING, timeTakenSeconds = 10)

        val state = viewModel.uiState.value
        assertTrue(state.isAnswerSubmitted)
        assertEquals(true, state.isCorrect)
        assertEquals(1, state.score) // Grants score
        assertEquals(0, state.streak) // Resets streak so user doesn't jump to harder levels

        // Calibrates to lower level (Hard -> Medium)
        assertEquals("Medium", state.currentTier)

        // Must drop complexity by exactly 50% of the penalty
        val halfPenalty = 2.0f - state.currentComplexity
        assertEquals(fullPenalty * 0.5f, halfPenalty, 0.001f)
        assertTrue(state.ddaMessage?.contains("50% penalty") == true)
    }

    @Test
    fun testGuessingAndIncorrectHasSamePenaltyAsConfidentAndIncorrect() {
        val confidentVm = QuizViewModel()
        confidentVm.initializeQuiz(testQuestions, initialTier = "Hard", initialComplexity = 2.0f)
        confidentVm.selectAnswer("Automated Interface")
        confidentVm.submitAnswer(confidence = AnswerConfidence.CONFIDENT, timeTakenSeconds = 10)

        val guessingVm = QuizViewModel()
        guessingVm.initializeQuiz(testQuestions, initialTier = "Hard", initialComplexity = 2.0f)
        guessingVm.selectAnswer("Automated Interface")
        guessingVm.submitAnswer(confidence = AnswerConfidence.GUESSING, timeTakenSeconds = 10)

        val confState = confidentVm.uiState.value
        val guessState = guessingVm.uiState.value

        assertEquals(confState.currentTier, guessState.currentTier)
        assertEquals(confState.currentComplexity, guessState.currentComplexity, 0.001f)
        assertEquals(confState.score, guessState.score)
        assertEquals(confState.streak, guessState.streak)
    }

    @Test
    fun testNextQuestionAdvancesAndClearsSelection() {
        viewModel.selectAnswer("Artificial Intelligence")
        viewModel.selectConfidence(AnswerConfidence.CONFIDENT)
        viewModel.submitAnswer(confidence = AnswerConfidence.CONFIDENT)

        viewModel.nextQuestion()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentIndex)
        assertNull(state.selectedAnswer)
        assertNull(state.selectedConfidence)
        assertFalse(state.isAnswerSubmitted)
        assertNull(state.isCorrect)
    }
}
