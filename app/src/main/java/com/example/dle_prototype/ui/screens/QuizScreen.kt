package com.example.dle_prototype.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.Question
import com.example.dle_prototype.data.QuestionsRepository
import com.example.dle_prototype.data.User
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    user: User,
    categoryName: String,
    categoryNumber: Float,
    dbHelper: DatabaseHelper,
    onQuizCompleted: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val totalSessionQuestions = 10
    var questionsByDifficulty by remember { mutableStateOf<Map<String, List<Question>>>(emptyMap()) }
    val sessionQuestions = remember { mutableStateListOf<Question>() }
    val usedQuestions = remember { mutableSetOf<String>() }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var highestStreak by remember { mutableIntStateOf(0) }
    var ddaTier by remember { mutableStateOf("Easy") } // "Easy", "Medium", "Hard"
    var isDdaActive by remember { mutableStateOf(true) }
    var ddaEventMessage by remember { mutableStateOf<String?>(null) }
    var savedToDeckMessage by remember { mutableStateOf(false) }

    var selectedAnswer by remember { mutableStateOf<Any?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showResultDialog by remember { mutableStateOf(false) }
    var maxDifficultyReached by remember { mutableStateOf(1f) }

    BackHandler { onBack() }

    fun pickNextQuestion(tier: String): Question? {
        val tierKey = tier.lowercase()
        val pool = questionsByDifficulty[tierKey]?.filterNot { usedQuestions.contains(it.question) }
        val fallback = questionsByDifficulty.values.flatten().filterNot { usedQuestions.contains(it.question) }

        val selected = pool?.shuffled()?.firstOrNull() ?: fallback.shuffled().firstOrNull()
        selected?.let { usedQuestions.add(it.question) }
        return selected
    }

    LaunchedEffect(categoryName) {
        isLoading = true
        val grouped = QuestionsRepository.getQuestionsByDifficulty(context, categoryName)
        questionsByDifficulty = grouped

        // Pick initial question
        val firstQ = grouped["easy"]?.shuffled()?.firstOrNull()
            ?: grouped.values.flatten().shuffled().firstOrNull()
        if (firstQ != null) {
            usedQuestions.add(firstQ.question)
            sessionQuestions.add(firstQ)
        }
        isLoading = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$categoryName Quiz",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (isDdaActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyanAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "DDA Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                        if (sessionQuestions.isNotEmpty()) {
                            Text(
                                text = "Question ${currentIndex + 1} of $totalSessionQuestions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("quiz_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Quiz")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (streak > 1) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = AmberAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Score: $score",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (sessionQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No questions available for $categoryName.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onBack) { Text("Back to Dashboard") }
                }
            }
        } else {
            val currentQ = sessionQuestions[currentIndex]
            val progress = (currentIndex + 1).toFloat() / totalSessionQuestions.toFloat()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Adaptive Status & Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zone: ${
                                when (ddaTier) {
                                    "Hard" -> "Mastery Peak (2.0x)"
                                    "Medium" -> "Optimal ZPD (1.5x)"
                                    else -> "Foundational (1.0x)"
                                }
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${((currentIndex + 1) * 10)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // DDA Real-Time Adjustment Banner
                ddaEventMessage?.let { banner ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (banner.contains("Up")) EmeraldSuccess.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (banner.contains("Up")) EmeraldSuccess else AmberAccent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (banner.contains("Up")) Icons.Default.Bolt else Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (banner.contains("Up")) EmeraldSuccess else AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = banner,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (banner.contains("Up")) EmeraldSuccess else AmberAccent
                            )
                        }
                    }
                }

                // Question Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (currentQ.difficulty.lowercase()) {
                                            "hard" -> RoseAccent.copy(alpha = 0.15f)
                                            "medium" -> AmberAccent.copy(alpha = 0.15f)
                                            else -> EmeraldSuccess.copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${currentQ.difficulty.uppercase()} TIER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (currentQ.difficulty.lowercase()) {
                                        "hard" -> RoseAccent
                                        "medium" -> AmberAccent
                                        else -> EmeraldSuccess
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (currentQ.type == "mcq") "Multiple Choice" else "True / False",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentQ.question,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Options Section
                if (currentQ.type == "mcq") {
                    currentQ.options.forEachIndexed { idx, opt ->
                        val isSelected = selectedAnswer == opt
                        val isCorrect = opt == currentQ.answer
                        val optionLetter = ('A' + idx).toString()

                        val borderColor = when {
                            !isAnswerSubmitted -> if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            isCorrect -> EmeraldSuccess
                            isSelected -> RoseAccent
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        }

                        val bgColor = when {
                            !isAnswerSubmitted -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            isCorrect -> EmeraldSuccess.copy(alpha = 0.15f)
                            isSelected -> RoseAccent.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                                .clickable(enabled = !isAnswerSubmitted) {
                                    selectedAnswer = opt
                                }
                                .testTag("mcq_option_$idx"),
                            color = bgColor,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isAnswerSubmitted && isCorrect) EmeraldSuccess
                                            else if (isAnswerSubmitted && isSelected) RoseAccent
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionLetter,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAnswerSubmitted && (isCorrect || isSelected)) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    // True / False options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(true to "True", false to "False").forEach { (value, label) ->
                            val isSelected = selectedAnswer == value
                            val isCorrect = value == currentQ.answer

                            val borderColor = when {
                                !isAnswerSubmitted -> if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                isCorrect -> EmeraldSuccess
                                isSelected -> RoseAccent
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            }

                            val bgColor = when {
                                !isAnswerSubmitted -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                isCorrect -> EmeraldSuccess.copy(alpha = 0.15f)
                                isSelected -> RoseAccent.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                                    .clickable(enabled = !isAnswerSubmitted) {
                                        selectedAnswer = value
                                    }
                                    .testTag("tf_option_$label"),
                                color = bgColor,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = if (value) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (value) EmeraldSuccess else RoseAccent,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Feedback & Leitner Spaced Repetition Notification
                AnimatedVisibility(
                    visible = isAnswerSubmitted,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val isCorrect = selectedAnswer == currentQ.answer
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCorrect) EmeraldSuccess.copy(alpha = 0.12f) else RoseAccent.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCorrect) EmeraldSuccess else RoseAccent
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isCorrect) EmeraldSuccess else RoseAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isCorrect) "Correct!" else "Incorrect!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) EmeraldSuccess else RoseAccent
                                )
                            }
                            if (savedToDeckMessage) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Style,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Saved to Spaced Repetition Review Deck",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (currentQ.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentQ.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (currentQ.example.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = currentQ.example,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action Button
                if (!isAnswerSubmitted) {
                    Button(
                        onClick = {
                            if (selectedAnswer != null) {
                                isAnswerSubmitted = true
                                val correct = selectedAnswer == currentQ.answer
                                val qDiff = QuestionsRepository.difficultyToFloat(currentQ.difficulty)
                                if (qDiff > maxDifficultyReached) maxDifficultyReached = qDiff

                                if (correct) {
                                    score++
                                    streak++
                                    if (streak > highestStreak) highestStreak = streak
                                    savedToDeckMessage = false

                                    // DDA Escalation Logic
                                    if (isDdaActive) {
                                        if (streak >= 2 && ddaTier == "Easy") {
                                            ddaTier = "Medium"
                                            ddaEventMessage = "Level Up! Advancing to Intermediate Tier 🚀"
                                        } else if (streak >= 4 && ddaTier == "Medium") {
                                            ddaTier = "Hard"
                                            ddaEventMessage = "Level Up! Reached Advanced Mastery Tier 🔥"
                                        }
                                    }
                                } else {
                                    streak = 0
                                    savedToDeckMessage = true
                                    // Save to Spaced Repetition Flashcards!
                                    coroutineScope.launch {
                                        dbHelper.addMissedQuestionToFlashcard(user.username, currentQ)
                                    }

                                    // DDA Scaffolding Logic
                                    if (isDdaActive) {
                                        if (ddaTier == "Hard") {
                                            ddaTier = "Medium"
                                            ddaEventMessage = "Adjusting to Intermediate for conceptual reinforcement 💡"
                                        } else if (ddaTier == "Medium") {
                                            ddaTier = "Easy"
                                            ddaEventMessage = "Providing foundational scaffolding 💡"
                                        }
                                    }
                                }
                            }
                        },
                        enabled = selectedAnswer != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_answer_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Confirm Answer", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    val isLastQuestion = currentIndex >= totalSessionQuestions - 1
                    Button(
                        onClick = {
                            if (isLastQuestion) {
                                coroutineScope.launch {
                                    dbHelper.recordQuizResult(
                                        username = user.username,
                                        category = categoryName,
                                        categoryNumber = categoryNumber,
                                        score = score,
                                        totalQuestions = totalSessionQuestions,
                                        difficultyLevel = maxDifficultyReached
                                    )
                                    showResultDialog = true
                                }
                            } else {
                                currentIndex++
                                selectedAnswer = null
                                isAnswerSubmitted = false
                                savedToDeckMessage = false
                                ddaEventMessage = null

                                // Pick next adaptive question if not yet populated
                                if (currentIndex >= sessionQuestions.size) {
                                    val nextQ = pickNextQuestion(ddaTier)
                                    if (nextQ != null) {
                                        sessionQuestions.add(nextQ)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("next_question_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (isLastQuestion) "View Adaptive Results" else "Next Question →",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    if (showResultDialog) {
        val percentage = ((score.toFloat() / totalSessionQuestions) * 100).toInt()
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Text(
                    text = "Quiz Finished!",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score / $totalSessionQuestions Points ($percentage%)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Best Streak:", style = MaterialTheme.typography.bodySmall)
                                Text("$highestStreak answers", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Peak Difficulty:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    when {
                                        maxDifficultyReached >= 3.0f -> "Advanced (Hard)"
                                        maxDifficultyReached >= 2.0f -> "Intermediate (Medium)"
                                        else -> "Foundational (Easy)"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Results saved to local SQLite. Missed questions enrolled in your Leitner Spaced Repetition deck!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        onQuizCompleted()
                    },
                    modifier = Modifier.testTag("finish_quiz_dialog_button")
                ) {
                    Text("Return to Workspace")
                }
            }
        )
    }
}
