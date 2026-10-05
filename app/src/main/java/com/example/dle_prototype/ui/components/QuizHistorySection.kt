package com.example.dle_prototype.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuizHistorySection(
    attempts: List<QuizAttempt>,
    onStartNewQuiz: () -> Unit,
    modifier: Modifier = Modifier,
    onExportClick: (() -> Unit)? = null
) {
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categories = remember(attempts) {
        listOf("All") + attempts.map { it.category }.distinct()
    }

    val filteredAttempts = remember(attempts, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") attempts
        else attempts.filter { it.category == selectedCategoryFilter }
    }

    val totalAttempts = attempts.size
    val totalScore = attempts.sumOf { it.score }
    val totalQuestions = attempts.sumOf { it.totalQuestions }
    val avgAccuracy = if (totalQuestions > 0) (totalScore.toFloat() / totalQuestions.toFloat()) * 100f else 0f
    val bestScore = attempts.maxOfOrNull { it.score } ?: 0

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quiz_history_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // High-level SQL Performance Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TOTAL QUIZZES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalAttempts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanAccent
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "AVG ACCURACY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${avgAccuracy.toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldSuccess
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TOP SCORE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$bestScore pts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberAccent
                    )
                }
            }
        }

        // Section Title & Export Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "QUIZ ATTEMPTS (${filteredAttempts.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8)
            )

            if (onExportClick != null && attempts.isNotEmpty()) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onExportClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = CyanAccent.copy(alpha = 0.1f),
                        contentColor = CyanAccent
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_export_history")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = "Export History",
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Export (PDF/CSV)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        if (categories.size > 2) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E293B)
                        )
                    )
                }
            }
        }

        // List of Attempts or Empty State
        if (filteredAttempts.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "No Quiz Records Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1F5F9)
                    )

                    Text(
                        text = "Take your first adaptive quiz to begin tracking your scores, mastery history, and rocket ascent progression.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Button(
                        onClick = onStartNewQuiz,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        modifier = Modifier.testTag("history_start_quiz_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start First Quiz", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredAttempts.forEach { attempt ->
                    QuizHistoryCard(
                        attempt = attempt,
                        formattedDate = dateFormat.format(Date(attempt.timestamp))
                    )
                }
            }
        }
    }
}

@Composable
fun QuizHistoryCard(
    attempt: QuizAttempt,
    formattedDate: String,
    modifier: Modifier = Modifier
) {
    val pct = if (attempt.totalQuestions > 0) {
        ((attempt.score.toFloat() / attempt.totalQuestions.toFloat()) * 100).toInt()
    } else 0

    val (badgeColor, statusLabel) = when {
        pct >= 90 -> EmeraldSuccess to "MASTERED"
        pct >= 70 -> CyanAccent to "PROFICIENT"
        pct >= 50 -> AmberAccent to "PROGRESSING"
        else -> RoseAccent to "NEEDS REVIEW"
    }

    val difficultyLabel = when {
        attempt.difficultyLevel >= 2.5f -> "Hard"
        attempt.difficultyLevel >= 1.5f -> "Medium"
        else -> "Easy"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("quiz_history_item_${attempt.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = attempt.category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1F5F9)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = difficultyLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Score and Status Pill
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${attempt.score}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor
                    )
                    Text(
                        text = "/${attempt.totalQuestions}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "($pct%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, badgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
