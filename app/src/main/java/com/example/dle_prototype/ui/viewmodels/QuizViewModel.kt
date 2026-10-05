package com.example.dle_prototype.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.Question
import com.example.dle_prototype.data.QuestionsRepository
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.ml.AdaptiveDifficultyEngine
import com.example.dle_prototype.data.ml.AnswerConfidence
import com.example.dle_prototype.data.ml.DynamicAdjustmentResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State representing the active quiz session and metacognitive confidence assessment.
 */
data class QuizUiState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: Any? = null,
    val selectedConfidence: AnswerConfidence? = null,
    val isAnswerSubmitted: Boolean = false,
    val isCorrect: Boolean? = null,
    val score: Int = 0,
    val streak: Int = 0,
    val highestStreak: Int = 0,
    val consecutiveErrors: Int = 0,
    val currentComplexity: Float = 1.5f,
    val currentTier: String = "Easy",
    val ddaMessage: String? = null,
    val lastAdjustment: DynamicAdjustmentResult? = null,
    val remainingSeconds: Int = 25,
    val isTimerActive: Boolean = true,
    val isTimedOut: Boolean = false,
    val missedQuestions: List<Question> = emptyList(),
    val isCompleted: Boolean = false,
    val maxDifficultyReached: Float = 1.0f,
    val isLoading: Boolean = false
) {
    val currentQuestion: Question?
        get() = questions.getOrNull(currentIndex)

    val totalQuestions: Int
        get() = questions.size

    val isLastQuestion: Boolean
        get() = totalQuestions > 0 && currentIndex >= totalQuestions - 1
}

/**
 * QuizViewModel tracks the selected 'Confident' or 'Guessing' state and calculates
 * the Dynamic Difficulty Adjustment (DDA) logic based on answer accuracy and confidence level.
 *
 * Difficulty Matrix:
 * 1. Confident & Correct: Standard score (+1 pt), increment streak, velocity bonus, promote tier on streak.
 * 2. Confident & Incorrect: Misconception penalty to lower levels. Full penalty (-baseErrorPenalty), drop tier, reset streak.
 * 3. Guessing & Correct: Leads to lower levels with 50% penalty (-0.50 * baseErrorPenalty), scaffold tier, reset streak.
 * 4. Guessing & Incorrect: Same penalty as Confident & Incorrect (full penalty to lower levels, drop tier, reset streak).
 */
class QuizViewModel(
    private val dbHelper: DatabaseHelper? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    /**
     * Initializes the quiz session with questions, initial difficulty tier, and starting complexity.
     */
    fun initializeQuiz(
        questions: List<Question>,
        initialTier: String = "Easy",
        initialComplexity: Float = 1.5f
    ) {
        _uiState.value = QuizUiState(
            questions = questions,
            currentTier = initialTier,
            currentComplexity = initialComplexity,
            remainingSeconds = 25,
            maxDifficultyReached = QuestionsRepository.difficultyToFloat(initialTier)
        )
    }

    /**
     * Selects an answer option for the current question.
     */
    fun selectAnswer(answer: Any) {
        if (_uiState.value.isAnswerSubmitted) return
        _uiState.update { it.copy(selectedAnswer = answer) }
    }

    /**
     * Tracks the selected confidence state ('Confident' or 'Guessing').
     */
    fun selectConfidence(confidence: AnswerConfidence) {
        _uiState.update { it.copy(selectedConfidence = confidence) }
    }

    /**
     * Submits the answer with the given confidence ('Certain' or 'Guessing')
     * and calculates the adaptive difficulty adjustment accordingly.
     */
    fun submitAnswer(
        confidence: AnswerConfidence = _uiState.value.selectedConfidence ?: AnswerConfidence.CERTAIN,
        timeTakenSeconds: Int? = null,
        user: User? = null
    ) {
        val currentState = _uiState.value
        val currentQ = currentState.currentQuestion ?: return
        if (currentState.selectedAnswer == null || currentState.isAnswerSubmitted) return

        val answer = currentState.selectedAnswer
        val isCorrect = answer == currentQ.answer
        val seconds = timeTakenSeconds ?: (25 - currentState.remainingSeconds).coerceIn(1, 25)

        val qDiff = QuestionsRepository.difficultyToFloat(currentQ.difficulty)
        val newMaxDiff = maxOf(currentState.maxDifficultyReached, qDiff)

        var newScore = currentState.score
        var newStreak = currentState.streak
        var newHighestStreak = currentState.highestStreak
        var newConsecutiveErrors = currentState.consecutiveErrors
        val missedList = currentState.missedQuestions.toMutableList()

        when {
            // 1. Certain & Correct: Reward! Increment score, streak, and increase adaptive level
            confidence == AnswerConfidence.CERTAIN && isCorrect -> {
                newScore++
                newStreak++
                if (newStreak > newHighestStreak) newHighestStreak = newStreak
                newConsecutiveErrors = 0
            }

            // 2. Guessing & Correct: Score point awarded, but streak is reset and adaptive level is reduced
            confidence == AnswerConfidence.GUESSING && isCorrect -> {
                newScore++
                newStreak = 0
                newConsecutiveErrors = 0
            }

            // 3. Certain & Incorrect: Full misconception penalty, level reduced
            confidence == AnswerConfidence.CERTAIN && !isCorrect -> {
                newStreak = 0
                newConsecutiveErrors++
                missedList.add(currentQ)
                user?.let { u ->
                    dbHelper?.let { db ->
                        viewModelScope.launch { db.addMissedQuestionToFlashcard(u.username, currentQ) }
                    }
                }
            }

            // 4. Guessing & Incorrect: Full penalty, level reduced
            else -> {
                newStreak = 0
                newConsecutiveErrors++
                missedList.add(currentQ)
                user?.let { u ->
                    dbHelper?.let { db ->
                        viewModelScope.launch { db.addMissedQuestionToFlashcard(u.username, currentQ) }
                    }
                }
            }
        }

        // Calculate dynamic difficulty adjustment based on accuracy and confidence level
        val sessionAccuracy = newScore.toFloat() / (currentState.currentIndex + 1).toFloat()
        val adjustment = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = currentState.currentComplexity,
            currentTier = currentState.currentTier,
            isCorrect = isCorrect,
            confidence = confidence,
            secondsTaken = seconds,
            currentStreak = newStreak,
            consecutiveErrors = newConsecutiveErrors,
            sessionAccuracy = sessionAccuracy,
            escalationThreshold = 2
        )

        _uiState.update {
            it.copy(
                selectedConfidence = confidence,
                isAnswerSubmitted = true,
                isCorrect = isCorrect,
                score = newScore,
                streak = newStreak,
                highestStreak = newHighestStreak,
                consecutiveErrors = newConsecutiveErrors,
                currentComplexity = adjustment.newComplexity,
                currentTier = adjustment.newTier,
                ddaMessage = adjustment.message,
                lastAdjustment = adjustment,
                maxDifficultyReached = newMaxDiff,
                missedQuestions = missedList
            )
        }
    }

    /**
     * Advances to the next question, or marks quiz completed on the final question.
     */
    fun nextQuestion(user: User? = null, categoryName: String = "", categoryNumber: Float = 1f) {
        val state = _uiState.value
        if (state.isLastQuestion) {
            _uiState.update { it.copy(isCompleted = true) }
            user?.let { u ->
                dbHelper?.let { db ->
                    viewModelScope.launch {
                        db.recordQuizResult(
                            username = u.username,
                            category = categoryName,
                            categoryNumber = categoryNumber,
                            score = state.score,
                            totalQuestions = state.totalQuestions,
                            difficultyLevel = state.maxDifficultyReached
                        )
                        db.updateUserStreakOnQuizCompletion(u.username)
                    }
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedAnswer = null,
                    selectedConfidence = null,
                    isAnswerSubmitted = false,
                    isCorrect = null,
                    remainingSeconds = 25,
                    isTimedOut = false,
                    ddaMessage = null,
                    lastAdjustment = null
                )
            }
        }
    }

    /**
     * Handles timer expiration by recording missed question and scaffolding tier.
     */
    fun onTimeExpired(user: User? = null) {
        val state = _uiState.value
        if (state.isAnswerSubmitted) return
        val currentQ = state.currentQuestion ?: return
        val missedList = state.missedQuestions.toMutableList().apply { add(currentQ) }
        user?.let { u ->
            dbHelper?.let { db ->
                viewModelScope.launch { db.addMissedQuestionToFlashcard(u.username, currentQ) }
            }
        }

        val nextTier = if (state.currentTier == "Hard") "Medium" else "Easy"
        _uiState.update {
            it.copy(
                isAnswerSubmitted = true,
                isCorrect = false,
                isTimedOut = true,
                streak = 0,
                consecutiveErrors = it.consecutiveErrors + 1,
                currentTier = nextTier,
                currentComplexity = (it.currentComplexity - 0.25f).coerceAtLeast(1.0f),
                ddaMessage = "⏱️ Time expired! Scaffolded to $nextTier tier.",
                missedQuestions = missedList
            )
        }
    }

    /**
     * Decrements the countdown challenge timer.
     */
    fun decrementTimer() {
        _uiState.update {
            if (it.remainingSeconds > 0 && !it.isAnswerSubmitted && it.isTimerActive) {
                it.copy(remainingSeconds = it.remainingSeconds - 1)
            } else {
                it
            }
        }
    }
}
