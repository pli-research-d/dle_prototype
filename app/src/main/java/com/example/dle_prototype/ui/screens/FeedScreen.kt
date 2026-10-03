package com.example.dle_prototype.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.FeedCard
import com.example.dle_prototype.data.FeedCardsRepository
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.UserGamificationState
import com.example.dle_prototype.ui.components.AscentSheet
import com.example.dle_prototype.ui.components.BatchBreakDialog
import com.example.dle_prototype.ui.components.DailyGoalStepperSheet
import com.example.dle_prototype.ui.components.FeedCardView
import com.example.dle_prototype.ui.components.RewardMomentType
import com.example.dle_prototype.ui.components.RewardMomentsOverlay
import com.example.dle_prototype.ui.components.RewardsHubSheet
import com.example.dle_prototype.ui.components.RocketProgressView
import com.example.dle_prototype.ui.components.SoftStopDialog
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    user: User,
    dbHelper: DatabaseHelper,
    onOpenCompete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    var gamification by remember { mutableStateOf<UserGamificationState?>(null) }
    var dailyGoal by remember { mutableStateOf(DailyGoalProgress(10, 0, 0f, false)) }
    var streakCount by remember { mutableIntStateOf(1) }
    var multiplier by remember { mutableFloatStateOf(1.0f) }

    val cards = remember { mutableStateListOf<FeedCard>() }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // Session stats for Batch Break & Soft Stop
    var sessionCardsAnswered by remember { mutableIntStateOf(0) }
    var sessionCorrectCount by remember { mutableIntStateOf(0) }
    var sessionXpGained by remember { mutableIntStateOf(0) }
    var sessionKmGained by remember { mutableFloatStateOf(0f) }
    var sessionStartMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    // Dialogs & Sheets
    var showRewardsHub by remember { mutableStateOf(false) }
    var showAscentSheet by remember { mutableStateOf(false) }
    var showGoalStepperSheet by remember { mutableStateOf(false) }
    var showBatchBreak by remember { mutableStateOf(false) }
    var showSoftStop by remember { mutableStateOf(false) }
    var triggerMilestoneCelebration by remember { mutableStateOf(false) }

    // Reward moments overlay
    var activeRewardMoment by remember { mutableStateOf<RewardMomentType?>(null) }

    suspend fun loadFeed() {
        isLoading = true
        gamification = dbHelper.getGamificationState(user.username)
        dailyGoal = dbHelper.getDailyGoalProgress(user.username)
        val streakData = dbHelper.getUserStreakData(user.username)
        streakCount = streakData.currentStreak

        val newCards = FeedCardsRepository.generateFeedCards(
            context = context,
            topics = gamification?.selectedTopics ?: listOf("HTML", "CSS", "JavaScript"),
            dbHelper = dbHelper,
            username = user.username,
            batchSize = 14
        )
        cards.clear()
        cards.addAll(newCards)
        currentIndex = 0
        isLoading = false
    }

    LaunchedEffect(user.username) {
        loadFeed()
    }

    // Auto-dismiss reward moment banner after 2.5 seconds
    LaunchedEffect(activeRewardMoment) {
        if (activeRewardMoment != null) {
            delay(2500)
            activeRewardMoment = null
        }
    }

    // Soft-stop check periodically (e.g. after ~20 min or 20 cards)
    LaunchedEffect(sessionCardsAnswered) {
        if (sessionCardsAnswered > 0 && sessionCardsAnswered % 20 == 0) {
            showSoftStop = true
        } else if (sessionCardsAnswered > 0 && sessionCardsAnswered % 10 == 0) {
            showBatchBreak = true
        }
    }

    fun nextCard() {
        if (currentIndex < cards.size - 1) {
            currentIndex++
        } else {
            // Load more endlessly
            coroutineScope.launch {
                val more = FeedCardsRepository.generateFeedCards(
                    context = context,
                    topics = gamification?.selectedTopics ?: listOf("HTML", "CSS", "JavaScript"),
                    dbHelper = dbHelper,
                    username = user.username,
                    batchSize = 10
                )
                cards.addAll(more)
                currentIndex++
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF070B12)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // -------------------------------------------------------------
                // TOP BAR (Thin, always visible)
                // -------------------------------------------------------------
                Surface(
                    color = Color(0xFF0F172A),
                    tonalElevation = 4.dp,
                    border = BorderStroke(0.5.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // ① Daily Goal Bar (Tap -> Bottom sheet)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showGoalStepperSheet = true }
                                .testTag("top_bar_daily_goal")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { dailyGoal.percentComplete },
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = EmeraldSuccess,
                                    trackColor = Color(0xFF334155),
                                )
                                Text(
                                    text = "${dailyGoal.answeredToday}/${dailyGoal.targetQuestions}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // ② 🔥 Streak + Multiplier Chip (1x -> 1.5x -> 2x)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmberAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("top_bar_streak_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "$streakCount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AmberAccent
                                )
                                Text(
                                    text = "· ${multiplier}x",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE68A)
                                )
                            }
                        }

                        // ③ ⛽ Fuel Balance (Tap -> Rewards Hub)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF131D31),
                            border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showRewardsHub = true }
                                .testTag("top_bar_fuel_hub")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "${gamification?.fuel ?: 0}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AmberAccent
                                )
                            }
                        }

                        // ④ 🚀 Rocket Altitude Pill (Tap -> Ascent sheet)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CyanAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showAscentSheet = true }
                                .testTag("top_bar_altitude_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(13.dp))
                                Text(
                                    text = "${"%.1f".format(gamification?.altitudeKm ?: 0f)} km",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // CARD STACK (One card per screen, swipe up = next)
                // -------------------------------------------------------------
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CyanAccent)
                    }
                } else if (cards.isNotEmpty() && currentIndex < cards.size) {
                    val currentCard = cards[currentIndex]

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    if (delta < -35) {
                                        // Swipe Up gesture detected
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        nextCard()
                                    }
                                }
                            )
                    ) {
                        AnimatedContent(
                            targetState = currentCard,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "card_flip_transition"
                        ) { card ->
                            FeedCardView(
                                card = card,
                                multiplier = multiplier,
                                streak = streakCount,
                                onAnswerSubmitted = { isCorrect, xp, fuel ->
                                    sessionCardsAnswered++
                                    if (isCorrect) {
                                        sessionCorrectCount++
                                        sessionXpGained += xp
                                        sessionKmGained += 0.3f
                                        streakCount++
                                        // Dynamic multiplier escalation
                                        multiplier = when {
                                            streakCount >= 10 -> 2.0f
                                            streakCount >= 5 -> 1.5f
                                            else -> 1.0f
                                        }

                                        // Reward moment triggers
                                        if (streakCount in listOf(5, 10, 25)) {
                                            activeRewardMoment = RewardMomentType.StreakMilestone(streakCount)
                                            triggerMilestoneCelebration = true
                                        } else if (streakCount == 3) {
                                            activeRewardMoment = RewardMomentType.MultiplierChange(1.5f)
                                        } else if (sessionCardsAnswered % 7 == 0) {
                                            // Mystery chest drop!
                                            coroutineScope.launch {
                                                dbHelper.addChest(
                                                    username = user.username,
                                                    type = "Streak Mystery Drop",
                                                    rewardXp = 40,
                                                    rewardFuel = 15,
                                                    badge = "Orbital Drifter",
                                                    skin = "Solar Flare"
                                                )
                                                activeRewardMoment = RewardMomentType.ChestDrop(1L, "Streak Mystery Drop")
                                            }
                                        }
                                    } else {
                                        streakCount = 0
                                        multiplier = 1.0f
                                    }

                                    // Save in database
                                    coroutineScope.launch {
                                        gamification = dbHelper.addFuelAndXp(user.username, fuel, xp, if (isCorrect) 0.3f else 0.05f)
                                        dbHelper.updateUserStreakOnQuizCompletion(user.username)
                                        dailyGoal = dbHelper.getDailyGoalProgress(user.username)
                                    }
                                },
                                onNextCard = { nextCard() },
                                onChallengeFriend = { onOpenCompete() },
                                onSaveToFlashcards = { c ->
                                    coroutineScope.launch {
                                        dbHelper.addFlashcard(
                                            username = user.username,
                                            category = c.category,
                                            question = c.prompt,
                                            options = c.options,
                                            correctAnswer = c.correctAnswer,
                                            explanation = c.explanation
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Top Floating Reward Moments Overlay (~2 sec, non-blocking)
            RewardMomentsOverlay(
                currentMoment = activeRewardMoment,
                onChestClick = { showRewardsHub = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 44.dp)
            )

            // Hidden Rocket Progress View for Confetti Cannon
            RocketProgressView(
                score = sessionCorrectCount,
                totalQuestions = sessionCardsAnswered.coerceAtLeast(1),
                streak = streakCount,
                triggerConfetti = triggerMilestoneCelebration,
                modifier = Modifier.size(1.dp)
            )
        }
    }

    // Batch Break Dialog (~10 cards)
    if (showBatchBreak) {
        val acc = if (sessionCardsAnswered > 0) (sessionCorrectCount * 100 / sessionCardsAnswered) else 100
        BatchBreakDialog(
            batchCount = sessionCardsAnswered,
            accuracyPercent = acc,
            xpGained = sessionXpGained,
            kmGained = sessionKmGained,
            nextTeaser = "Next up: A tricky MySQL subquery bug & Python slicing!",
            onKeepGoing = { showBatchBreak = false },
            onDoneForNow = { showBatchBreak = false }
        )
    }

    // Soft Stop Session Recap (~20 min session)
    if (showSoftStop) {
        val elapsedMins = ((System.currentTimeMillis() - sessionStartMillis) / 60000).toInt().coerceAtLeast(1)
        SoftStopDialog(
            sessionMinutes = elapsedMins,
            totalKm = gamification?.altitudeKm ?: 2.4f,
            cardsDone = sessionCardsAnswered,
            onOneMoreBatch = { showSoftStop = false },
            onFinish = { showSoftStop = false }
        )
    }

    // Rewards Hub Sheet
    if (showRewardsHub) {
        RewardsHubSheet(
            username = user.username,
            dbHelper = dbHelper,
            onDismiss = {
                showRewardsHub = false
                coroutineScope.launch { gamification = dbHelper.getGamificationState(user.username) }
            }
        )
    }

    // Ascent Stats Sheet
    if (showAscentSheet) {
        AscentSheet(
            altitudeKm = gamification?.altitudeKm ?: 0f,
            streak = streakCount,
            onDismiss = { showAscentSheet = false },
            onReplayCelebration = { triggerMilestoneCelebration = true }
        )
    }

    // Daily Goal Stepper Sheet
    if (showGoalStepperSheet) {
        DailyGoalStepperSheet(
            currentProgress = dailyGoal,
            onDismiss = { showGoalStepperSheet = false },
            onGoalChanged = { target ->
                coroutineScope.launch {
                    dailyGoal = dbHelper.setDailyGoal(user.username, target)
                    showGoalStepperSheet = false
                }
            }
        )
    }
}
