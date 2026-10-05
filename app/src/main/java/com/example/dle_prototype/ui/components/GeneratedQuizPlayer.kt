package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.ml.GeneratedQuiz
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.launch

/**
 * Interactive Quiz Player for AI-generated multiple-choice questions
 * crafted by the on-device TFLite model from summarized learning material.
 */
@Composable
fun GeneratedQuizPlayer(
    quiz: GeneratedQuiz,
    username: String? = null,
    dbHelper: DatabaseHelper? = null,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var correctAnswersCount by remember { mutableIntStateOf(0) }
    var isQuizCompleted by remember { mutableStateOf(false) }
    var isSavedToDb by remember { mutableStateOf(false) }

    val currentQ = quiz.questions.getOrNull(currentQuestionIndex)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("generated_quiz_player")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI GENERATED PRACTICE QUIZ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${quiz.generationLatencyMs}ms TFLite",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }
            }

            if (!isQuizCompleted && currentQ != null) {
                // Progress Indicator
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${currentQ.difficulty} · Concept: ${currentQ.keyConcept}",
                            fontSize = 11.sp,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex + 1).toFloat() / quiz.questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyanAccent,
                        trackColor = Color(0xFF1E293B)
                    )
                }

                // Question Stem
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF131D31),
                    border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentQ.question,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                // Multiple Choice Options (A, B, C, D)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentQ.options.forEachIndexed { optIndex, optionText ->
                        val isSelected = selectedOptionIndex == optIndex
                        val isCorrect = optIndex == currentQ.correctOptionIndex

                        val (cardBg, cardBorder, textCol) = when {
                            isSubmitted && isCorrect -> Triple(
                                EmeraldSuccess.copy(alpha = 0.15f),
                                EmeraldSuccess,
                                EmeraldSuccess
                            )
                            isSubmitted && isSelected && !isCorrect -> Triple(
                                RoseAccent.copy(alpha = 0.15f),
                                RoseAccent,
                                RoseAccent
                            )
                            isSelected -> Triple(
                                CyanAccent.copy(alpha = 0.15f),
                                CyanAccent,
                                Color.White
                            )
                            else -> Triple(
                                Color(0xFF131D31),
                                Color(0xFF1E293B),
                                Color(0xFFCBD5E1)
                            )
                        }

                        val letter = ('A'.code + optIndex).toChar()

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = !isSubmitted) {
                                    selectedOptionIndex = optIndex
                                }
                                .testTag("mcq_option_$optIndex")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) CyanAccent.copy(alpha = 0.3f) else Color(0xFF1E293B),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = letter.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) CyanAccent else Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Text(
                                    text = optionText,
                                    fontSize = 12.sp,
                                    color = textCol,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSubmitted) {
                                    if (isCorrect) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Correct",
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Incorrect",
                                            tint = RoseAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Explanation Card (Visible once submitted)
                AnimatedVisibility(visible = isSubmitted, enter = fadeIn(), exit = fadeOut()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "EXPLANATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = AmberAccent
                                )
                                Text(
                                    text = currentQ.explanation,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // Action Button: Submit or Next
                if (!isSubmitted) {
                    Button(
                        onClick = {
                            if (selectedOptionIndex != null) {
                                isSubmitted = true
                                if (selectedOptionIndex == currentQ.correctOptionIndex) {
                                    correctAnswersCount++
                                }
                            }
                        },
                        enabled = selectedOptionIndex != null,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("submit_answer_button")
                    ) {
                        Text(
                            text = "Check Answer",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (currentQuestionIndex + 1 < quiz.questions.size) {
                                currentQuestionIndex++
                                selectedOptionIndex = null
                                isSubmitted = false
                            } else {
                                isQuizCompleted = true
                                // Record to database if available
                                if (username != null && dbHelper != null && !isSavedToDb) {
                                    isSavedToDb = true
                                    coroutineScope.launch {
                                        dbHelper.recordQuizResult(
                                            username = username,
                                            category = "AI Generated",
                                            categoryNumber = 7f,
                                            score = correctAnswersCount,
                                            totalQuestions = quiz.questions.size,
                                            difficultyLevel = 2f
                                        )
                                        dbHelper.checkAndAwardBadges(username)
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("next_question_button")
                    ) {
                        Text(
                            text = if (currentQuestionIndex + 1 < quiz.questions.size) "Next Question →" else "See Results 🏆",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                // Completed Results Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AmberAccent.copy(alpha = 0.2f),
                        border = BorderStroke(2.dp, AmberAccent),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Practice Quiz Completed!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        val pct = ((correctAnswersCount.toFloat() / quiz.questions.size.toFloat()) * 100).toInt()
                        Text(
                            text = "Score: $correctAnswersCount / ${quiz.questions.size} ($pct%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pct >= 70) EmeraldSuccess else AmberAccent
                        )
                    }

                    // XP Earned Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color(0xFF1E2E4A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Reward Earned:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "+${correctAnswersCount * 25} XP",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = AmberAccent
                            )
                        }
                    }

                    // Finish & Done
                    Button(
                        onClick = onFinish,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("finish_generated_quiz_button")
                    ) {
                        Text("Done & Return to Summary", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }
            }
        }
    }
}
