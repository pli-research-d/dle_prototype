package com.example.dle_prototype.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.Question
import com.example.dle_prototype.data.QuestionsRepository
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.ml.AdaptiveDifficultyEngine
import com.example.dle_prototype.data.ml.AdaptiveDifficultyProfile
import com.example.dle_prototype.data.ml.AnswerConfidence
import com.example.dle_prototype.ui.components.RocketProgressView
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.delay
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
    initialTier: String? = null,
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
    var consecutiveErrors by remember { mutableIntStateOf(0) }
    var ddaTier by remember(initialTier) { mutableStateOf(initialTier ?: "Easy") } // "Easy", "Medium", "Hard"
    var isDdaActive by remember { mutableStateOf(true) }
    var ddaEventMessage by remember(initialTier) {
        mutableStateOf(if (initialTier != null && initialTier != "Easy") "🎯 Personalized starting tier: $initialTier calibrated by AI" else null)
    }
    var savedToDeckMessage by remember { mutableStateOf(false) }

    var selectedAnswer by remember { mutableStateOf<Any?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var lastSubmittedConfidence by remember { mutableStateOf<AnswerConfidence?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showResultDialog by remember { mutableStateOf(false) }
    var maxDifficultyReached by remember { mutableStateOf(1f) }

    // Adaptive Engine & Daily Goal States
    var adaptiveProfile by remember { mutableStateOf<AdaptiveDifficultyProfile?>(null) }
    var currentComplexity by remember { mutableFloatStateOf(1.5f) }
    var answeredTodayBase by remember { mutableIntStateOf(0) }
    var dailyTarget by remember { mutableIntStateOf(10) }
    var isDailyGoalAchieved by remember { mutableStateOf(false) }

    var isRocketRailVisible by remember { mutableStateOf(true) }
    var showDdaExplanationSheet by remember { mutableStateOf(false) }
    var showMistakesSheet by remember { mutableStateOf(false) }
    val sessionStartTime = remember { System.currentTimeMillis() }
    val missedQuestionsList = remember { mutableStateListOf<Question>() }

    // Time-Pressure Countdown Challenge & Rocket Telemetry State
    val questionDurationSeconds = 25
    var remainingSeconds by remember { mutableIntStateOf(questionDurationSeconds) }
    var isTimerActive by remember { mutableStateOf(true) }
    var isTimedOut by remember { mutableStateOf(false) }
    var lastAnswerWasCorrect by remember { mutableStateOf<Boolean?>(null) }

    BackHandler { onBack() }

    // Countdown challenge timer loop
    LaunchedEffect(currentIndex, isAnswerSubmitted, isTimerActive) {
        if (!isAnswerSubmitted && isTimerActive) {
            remainingSeconds = questionDurationSeconds
            isTimedOut = false
            while (remainingSeconds > 0 && !isAnswerSubmitted && isTimerActive) {
                delay(1000L)
                if (!isAnswerSubmitted && isTimerActive) {
                    remainingSeconds--
                }
            }
            if (remainingSeconds <= 0 && !isAnswerSubmitted && isTimerActive) {
                isTimedOut = true
                isAnswerSubmitted = true
                lastAnswerWasCorrect = false
                streak = 0
                val currentQ = sessionQuestions.getOrNull(currentIndex)
                if (currentQ != null) {
                    coroutineScope.launch {
                        dbHelper.addMissedQuestionToFlashcard(user.username, currentQ)
                    }
                }
                if (isDdaActive && ddaTier != "Easy") {
                    val nextTier = if (ddaTier == "Hard") "Medium" else "Easy"
                    ddaTier = nextTier
                    ddaEventMessage = "⏱️ Time expired! Scaffolded to $nextTier tier."
                } else {
                    ddaEventMessage = "⏱️ Time expired! Try to answer before countdown reaches zero."
                }
            }
        }
    }

    fun pickNextQuestion(tier: String): Question? {
        val allCat = questionsByDifficulty.values.flatten()
        val selected = AdaptiveDifficultyEngine.selectBestAdaptiveQuestion(
            candidateQuestions = allCat,
            usedQuestions = usedQuestions,
            targetComplexity = currentComplexity,
            targetTier = tier
        ) ?: allCat.filterNot { usedQuestions.contains(it.question) }.shuffled().firstOrNull()
        selected?.let { usedQuestions.add(it.question) }
        return selected
    }

    LaunchedEffect(categoryName) {
        isLoading = true
        val grouped = QuestionsRepository.getQuestionsByDifficulty(context, categoryName)
        questionsByDifficulty = grouped

        // Load historical performance from SQLite for Adaptive Difficulty
        val profile = dbHelper.getAdaptiveDifficultyProfile(user.username, categoryName)
        adaptiveProfile = profile
        currentComplexity = profile.targetComplexityScore
        val startingTier = initialTier ?: profile.initialTier
        ddaTier = startingTier
        ddaEventMessage = "🎯 Adaptive Baseline: ${profile.reasoning}"

        // Query daily goal progress
        val goal = dbHelper.getDailyGoalProgress(user.username)
        answeredTodayBase = goal.answeredToday
        dailyTarget = goal.targetQuestions
        isDailyGoalAchieved = goal.isAchieved

        // Pick initial question using Adaptive Difficulty Engine
        val allCatQuestions = grouped.values.flatten()
        val firstQ = AdaptiveDifficultyEngine.selectBestAdaptiveQuestion(
            candidateQuestions = allCatQuestions,
            usedQuestions = usedQuestions,
            targetComplexity = profile.targetComplexityScore,
            targetTier = startingTier
        ) ?: grouped[startingTier.lowercase()]?.shuffled()?.firstOrNull()
          ?: allCatQuestions.shuffled().firstOrNull()

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
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Question ${currentIndex + 1} of $totalSessionQuestions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("quiz_back_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Quiz")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (streak > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AmberAccent.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = AmberAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "$streak",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Small "Adaptive" dot (tap → explains DDA)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDdaExplanationSheet = true }
                                .testTag("dda_adaptive_dot")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CyanAccent)
                                )
                                Text(
                                    text = "Adaptive",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                        }

                        // Collapsible Rocket Rail Toggle
                        IconButton(
                            onClick = { isRocketRailVisible = !isRocketRailVisible },
                            modifier = Modifier.size(36.dp).testTag("toggle_rocket_rail_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = if (isRocketRailVisible) "Hide Rocket Rail" else "Show Rocket Rail",
                                tint = if (isRocketRailVisible) CyanAccent else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
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

                // Time-Pressure Countdown Challenge Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            remainingSeconds <= 5 -> RoseAccent
                            remainingSeconds <= 10 -> AmberAccent
                            else -> Color(0xFF1E293B)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_timer_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (remainingSeconds <= 5) Icons.Default.HourglassTop else Icons.Default.Timer,
                                contentDescription = "Countdown Timer",
                                tint = when {
                                    remainingSeconds <= 5 -> RoseAccent
                                    remainingSeconds <= 10 -> AmberAccent
                                    else -> CyanAccent
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isTimedOut) "Time Expired! (0s)" else "Timer: ${remainingSeconds}s",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    remainingSeconds <= 5 -> RoseAccent
                                    remainingSeconds <= 10 -> AmberAccent
                                    else -> Color(0xFFF1F5F9)
                                },
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (remainingSeconds.toFloat() / questionDurationSeconds.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(90.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                remainingSeconds <= 5 -> RoseAccent
                                remainingSeconds <= 10 -> AmberAccent
                                else -> CyanAccent
                            },
                            trackColor = Color(0xFF1E293B)
                        )
                    }
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

                // Question Workspace with Rocket Progress Telemetry on the side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
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

                                lastSubmittedConfidence?.let { conf ->
                                    val (badgeText, badgeColor) = when {
                                        conf == AnswerConfidence.CONFIDENT && isCorrect ->
                                            Pair("Confident · Standard Score", EmeraldSuccess)
                                        conf == AnswerConfidence.CONFIDENT && !isCorrect ->
                                            Pair("Confident · Level Penalty", RoseAccent)
                                        conf == AnswerConfidence.GUESSING && isCorrect ->
                                            Pair("Guessing · 50% Level Penalty", AmberAccent)
                                        else ->
                                            Pair("Guessing · Full Level Penalty", RoseAccent)
                                    }
                                    Surface(
                                        color = badgeColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, badgeColor)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (badgeColor == AmberAccent) Color(0xFFD97706) else badgeColor,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
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
            }

                    // Side-by-side Rocket Lift-Off and Fall Visualizer (Collapsible)
                    if (isRocketRailVisible) {
                        RocketProgressView(
                            score = score,
                            totalQuestions = totalSessionQuestions,
                            streak = streak,
                            isAscending = lastAnswerWasCorrect ?: true,
                            lastAnswerCorrect = lastAnswerWasCorrect,
                            isDailyGoalAchieved = isDailyGoalAchieved,
                            modifier = Modifier
                                .width(82.dp)
                                .height(440.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action Buttons: Confident vs Guessing (both submit answer)
                if (!isAnswerSubmitted) {
                    val submitWithConfidence = { confidence: AnswerConfidence ->
                        if (selectedAnswer != null) {
                            isAnswerSubmitted = true
                            lastSubmittedConfidence = confidence
                            val correct = selectedAnswer == currentQ.answer
                            val qDiff = QuestionsRepository.difficultyToFloat(currentQ.difficulty)
                            if (qDiff > maxDifficultyReached) maxDifficultyReached = qDiff

                            val timeTaken = (questionDurationSeconds - remainingSeconds).coerceIn(1, questionDurationSeconds)

                            when {
                                // 1. Confident and correct answers is good standard score
                                confidence == AnswerConfidence.CONFIDENT && correct -> {
                                    consecutiveErrors = 0
                                    lastAnswerWasCorrect = true
                                    score++
                                    streak++
                                    if (streak > highestStreak) highestStreak = streak
                                    savedToDeckMessage = false
                                }

                                // 2. Guessing and correct answer leads to lower levels with 50% penalty
                                // Still grants question score point, but resets streak so difficulty doesn't leap forward
                                confidence == AnswerConfidence.GUESSING && correct -> {
                                    consecutiveErrors = 0
                                    lastAnswerWasCorrect = true
                                    score++
                                    streak = 0
                                    savedToDeckMessage = false
                                }

                                // 3. Confident and incorrect: penalty to lower levels
                                confidence == AnswerConfidence.CONFIDENT && !correct -> {
                                    consecutiveErrors++
                                    lastAnswerWasCorrect = false
                                    streak = 0
                                    savedToDeckMessage = true
                                    missedQuestionsList.add(currentQ)
                                    coroutineScope.launch {
                                        dbHelper.addMissedQuestionToFlashcard(user.username, currentQ)
                                    }
                                }

                                // 4. Guessing and incorrect: same penalty as Confident and incorrect
                                else -> {
                                    consecutiveErrors++
                                    lastAnswerWasCorrect = false
                                    streak = 0
                                    savedToDeckMessage = true
                                    missedQuestionsList.add(currentQ)
                                    coroutineScope.launch {
                                        dbHelper.addMissedQuestionToFlashcard(user.username, currentQ)
                                    }
                                }
                            }

                            // Check daily goal achieved during session
                            if (!isDailyGoalAchieved && (answeredTodayBase + currentIndex + 1) >= dailyTarget) {
                                isDailyGoalAchieved = true
                            }

                            // Dynamic Adaptive Complexity Adjustment with confidence calibration
                            if (isDdaActive) {
                                val sessionAccuracy = score.toFloat() / (currentIndex + 1).toFloat()
                                val escalationThresh = adaptiveProfile?.escalationThreshold ?: 2
                                val adj = AdaptiveDifficultyEngine.computeDynamicAdjustment(
                                    currentComplexity = currentComplexity,
                                    currentTier = ddaTier,
                                    isCorrect = correct,
                                    confidence = confidence,
                                    secondsTaken = timeTaken,
                                    currentStreak = streak,
                                    consecutiveErrors = consecutiveErrors,
                                    sessionAccuracy = sessionAccuracy,
                                    escalationThreshold = escalationThresh
                                )
                                currentComplexity = adj.newComplexity
                                ddaTier = adj.newTier
                                ddaEventMessage = adj.message
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (selectedAnswer == null) "Select an answer above, then submit with confidence:" else "Submit with your confidence level:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Confident Button (Standard score if correct, penalty to lower levels if wrong)
                            Button(
                                onClick = { submitWithConfidence(AnswerConfidence.CONFIDENT) },
                                enabled = selectedAnswer != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("confident_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldSuccess,
                                    contentColor = Color.White,
                                    disabledContainerColor = EmeraldSuccess.copy(alpha = 0.3f),
                                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Confident",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Standard score",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }

                            // 2. Guessing Button (50% penalty to lower levels if correct, full penalty if wrong)
                            Button(
                                onClick = { submitWithConfidence(AnswerConfidence.GUESSING) },
                                enabled = selectedAnswer != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("guessing_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberAccent,
                                    contentColor = Color.Black,
                                    disabledContainerColor = AmberAccent.copy(alpha = 0.3f),
                                    disabledContentColor = Color.Black.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Guessing",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "50% penalty",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = Color.Black.copy(alpha = 0.75f)
                                        )
                                    }
                                }
                            }
                        }
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
                                    dbHelper.updateUserStreakOnQuizCompletion(user.username)
                                    showResultDialog = true
                                }
                            } else {
                                currentIndex++
                                selectedAnswer = null
                                lastSubmittedConfidence = null
                                isAnswerSubmitted = false
                                isTimedOut = false
                                remainingSeconds = questionDurationSeconds
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

    // Results Sheet: score, streak, time, "Review Mistakes" · "Done"
    if (showResultDialog) {
        val percentage = ((score.toFloat() / totalSessionQuestions) * 100).toInt()
        val elapsedSeconds = ((System.currentTimeMillis() - sessionStartTime) / 1000L).coerceAtLeast(1)

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
                    text = "Mission Completed!",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "$score / $totalSessionQuestions Correct ($percentage%)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (percentage >= 70) EmeraldSuccess else CyanAccent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Best Streak:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                Text("$highestStreak answers 🔥", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AmberAccent)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Time Elapsed:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                Text("${elapsedSeconds}s ⏱️", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Difficulty Mastery:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                Text(
                                    when {
                                        maxDifficultyReached >= 3.0f -> "Advanced (Hard)"
                                        maxDifficultyReached >= 2.0f -> "Intermediate (Medium)"
                                        else -> "Foundational (Easy)"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = CyanAccent
                                )
                            }
                        }
                    }

                    if (missedQuestionsList.isNotEmpty()) {
                        Text(
                            text = "${missedQuestionsList.size} missed questions automatically saved to your Leitner review deck.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
                    Text("Done")
                }
            },
            dismissButton = {
                if (missedQuestionsList.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showMistakesSheet = true },
                        modifier = Modifier.testTag("review_mistakes_button")
                    ) {
                        Text("Review Mistakes (${missedQuestionsList.size})")
                    }
                }
            }
        )
    }

    // DDA Explanation Sheet / Dialog
    if (showDdaExplanationSheet) {
        AlertDialog(
            onDismissRequest = { showDdaExplanationSheet = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanAccent)
                    Text("Adaptive Difficulty (DDA)")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("CURRENT TARGET COMPLEXITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent, fontFamily = FontFamily.Monospace)
                            Text("${"%.2f".format(currentComplexity)} / 3.0 ($ddaTier Tier)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = adaptiveProfile?.reasoning ?: "Dynamically calibrated based on SQLite historical performance.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Text(
                        text = "• Correct answers & rapid responses escalate question complexity toward higher orbital tiers.\n• Incorrect answers trigger scaffolding to reinforce foundational concepts without penalty.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showDdaExplanationSheet = false }) {
                    Text("Got It")
                }
            }
        )
    }

    // Review Mistakes Dialog
    if (showMistakesSheet) {
        AlertDialog(
            onDismissRequest = { showMistakesSheet = false },
            title = { Text("Session Mistakes (${missedQuestionsList.size})") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    missedQuestionsList.forEachIndexed { i, q ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("${i + 1}. ${q.question}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFF8FAFC))
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("Answer: ${q.answer}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                if (q.explanation.isNotBlank()) {
                                    Text(q.explanation, fontSize = 10.sp, color = Color(0xFF94A3B8))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showMistakesSheet = false }) {
                    Text("Close")
                }
            }
        )
    }
}
