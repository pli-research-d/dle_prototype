package com.example.dle_prototype.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.FeedCard
import com.example.dle_prototype.data.FeedCardType
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.delay

@Composable
fun FeedCardView(
    card: FeedCard,
    multiplier: Float,
    streak: Int,
    onAnswerSubmitted: (isCorrect: Boolean, xpEarned: Int, fuelEarned: Int) -> Unit,
    onNextCard: () -> Unit,
    onChallengeFriend: (FeedCard) -> Unit,
    onSaveToFlashcards: (FeedCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedOption by remember(card.id) { mutableStateOf<String?>(null) }
    var selectedBugLine by remember(card.id) { mutableIntStateOf(-1) }
    var isSubmitted by remember(card.id) { mutableStateOf(false) }
    var isCorrect by remember(card.id) { mutableStateOf(false) }
    var isFlipped by remember(card.id) { mutableStateOf(false) }
    var isSaved by remember(card.id) { mutableStateOf(false) }
    var showExplanationDialog by remember(card.id) { mutableStateOf(false) }

    // 25s countdown timer bar
    val timerProgress = remember(card.id) { Animatable(1f) }
    var isUrgentTimer by remember(card.id) { mutableStateOf(false) }

    // 15s countdown for EXPLAINER_15S
    var explainerRemainingSeconds by remember(card.id) { mutableIntStateOf(15) }

    LaunchedEffect(card.id, isSubmitted) {
        if (!isSubmitted && card.type == FeedCardType.EXPLAINER_15S) {
            explainerRemainingSeconds = 15
            while (explainerRemainingSeconds > 0 && !isSubmitted) {
                delay(1000L)
                if (!isSubmitted) {
                    explainerRemainingSeconds--
                }
            }
        }
    }

    LaunchedEffect(card.id, isSubmitted) {
        if (!isSubmitted && card.type != FeedCardType.EXPLAINER_15S && card.type != FeedCardType.REVIEW_CARD) {
            timerProgress.snapTo(1f)
            timerProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 25000, easing = LinearEasing)
            )
        }
    }

    LaunchedEffect(timerProgress.value) {
        isUrgentTimer = timerProgress.value <= 0.25f
    }

    val xpGained = (card.xpReward * multiplier).toInt()
    val timerColor = when {
        timerProgress.value <= 0.20f -> RoseAccent
        timerProgress.value <= 0.40f -> AmberAccent
        else -> CyanAccent
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp)
        ) {
            // Timer Bar (25s) across top
            if (card.type != FeedCardType.EXPLAINER_15S && card.type != FeedCardType.REVIEW_CARD) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(timerProgress.value)
                            .height(3.dp)
                            .background(timerColor)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Main Interactive Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (card.isMystery) Color(0xFF1E1035) else Color(0xFF131D31)
                ),
                border = BorderStroke(
                    1.dp,
                    if (card.isMystery) AmberAccent.copy(alpha = 0.5f) else Color(0xFF1E2E4A)
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                            .padding(end = 44.dp), // space for side action rail
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card Header Badge & Type
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (card.type) {
                                    FeedCardType.SPOT_THE_BUG -> RoseAccent.copy(alpha = 0.2f)
                                    FeedCardType.WHATS_THE_OUTPUT -> CyanAccent.copy(alpha = 0.2f)
                                    FeedCardType.FILL_BLANK -> AmberAccent.copy(alpha = 0.2f)
                                    FeedCardType.EXPLAINER_15S -> EmeraldSuccess.copy(alpha = 0.2f)
                                    FeedCardType.REVIEW_CARD -> Color(0xFF8B5CF6).copy(alpha = 0.25f)
                                    FeedCardType.MYSTERY_CARD -> AmberAccent.copy(alpha = 0.3f)
                                    else -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = card.category.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (card.type) {
                                        FeedCardType.SPOT_THE_BUG -> RoseAccent
                                        FeedCardType.WHATS_THE_OUTPUT -> CyanAccent
                                        FeedCardType.FILL_BLANK -> AmberAccent
                                        FeedCardType.EXPLAINER_15S -> EmeraldSuccess
                                        FeedCardType.REVIEW_CARD -> Color(0xFFA78BFA)
                                        FeedCardType.MYSTERY_CARD -> AmberAccent
                                        else -> Color(0xFF60A5FA)
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = card.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Question / Prompt
                        Text(
                            text = card.prompt,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC),
                            lineHeight = 22.sp
                        )

                        // Code Snippet (if present)
                        if (card.codeSnippet.isNotBlank() && card.type != FeedCardType.SPOT_THE_BUG) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF090D16),
                                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = card.codeSnippet,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // RENDER INTERACTION BY TYPE
                        when (card.type) {
                            // 1. SPOT THE BUG (Tap the faulty line)
                            FeedCardType.SPOT_THE_BUG -> {
                                Text(
                                    text = "Tap the faulty code line below:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    card.bugLines.forEachIndexed { idx, line ->
                                        val isChosen = selectedBugLine == idx
                                        val isTargetBug = idx == card.bugLineIndex
                                        val lineBg = when {
                                            isSubmitted && isTargetBug -> EmeraldSuccess.copy(alpha = 0.25f)
                                            isSubmitted && isChosen && !isTargetBug -> RoseAccent.copy(alpha = 0.25f)
                                            isChosen -> CyanAccent.copy(alpha = 0.2f)
                                            else -> Color(0xFF090D16)
                                        }
                                        val borderCol = when {
                                            isSubmitted && isTargetBug -> EmeraldSuccess
                                            isSubmitted && isChosen && !isTargetBug -> RoseAccent
                                            isChosen -> CyanAccent
                                            else -> Color(0xFF1E293B)
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(lineBg)
                                                .border(1.dp, borderCol, RoundedCornerShape(6.dp))
                                                .clickable(enabled = !isSubmitted) {
                                                    selectedBugLine = idx
                                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                    val correct = idx == card.bugLineIndex
                                                    isCorrect = correct
                                                    isSubmitted = true
                                                    if (correct) {
                                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                    } else {
                                                        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                                        onSaveToFlashcards(card)
                                                    }
                                                    onAnswerSubmitted(correct, if (correct) xpGained else 3, if (correct) card.fuelReward else 1)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.width(22.dp)
                                            )
                                            Text(
                                                text = line,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                color = if (isChosen) Color.White else Color(0xFFE2E8F0)
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. REVIEW CARD (Leitner card, flips in place)
                            FeedCardType.REVIEW_CARD -> {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF1E1B4B),
                                    border = BorderStroke(1.dp, Color(0xFF4338CA)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isFlipped = !isFlipped
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.Sync,
                                            contentDescription = "Flip",
                                            tint = Color(0xFFA78BFA),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (!isFlipped) "Tap to reveal answer & code" else "Answer: ${card.correctAnswer}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (!isFlipped) Color(0xFFC7D2FE) else EmeraldSuccess,
                                            textAlign = TextAlign.Center
                                        )
                                        if (isFlipped && card.explanation.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = card.explanation,
                                                fontSize = 12.sp,
                                                color = Color(0xFFCBD5E1),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                if (!isSubmitted) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                isSubmitted = true
                                                isCorrect = false
                                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                onAnswerSubmitted(false, 3, 1)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Still Learning", fontSize = 12.sp)
                                        }
                                        Button(
                                            onClick = {
                                                isSubmitted = true
                                                isCorrect = true
                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                onAnswerSubmitted(true, xpGained, card.fuelReward)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Got It! (+15 XP)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 3. 15-SEC MICRO EXPLAINER (No penalty, 15s read with countdown slider at bottom)
                            FeedCardType.EXPLAINER_15S -> {
                                Text(
                                    text = card.explanation,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 19.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Countdown slider at the bottom (15s read)
                                val explainerProgress = (explainerRemainingSeconds.toFloat() / 15f).coerceIn(0f, 1f)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("explainer_15s_countdown_slider"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0F172A),
                                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (explainerRemainingSeconds <= 3) Icons.Default.HourglassTop else Icons.Default.Timer,
                                                    contentDescription = "Read Countdown",
                                                    tint = when {
                                                        explainerRemainingSeconds <= 3 -> RoseAccent
                                                        explainerRemainingSeconds <= 7 -> AmberAccent
                                                        else -> CyanAccent
                                                    },
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = if (explainerRemainingSeconds == 0) "Reading completed! (0s)" else "15s Read Countdown: ${explainerRemainingSeconds}s remaining",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        explainerRemainingSeconds <= 3 -> RoseAccent
                                                        explainerRemainingSeconds <= 7 -> AmberAccent
                                                        else -> Color(0xFFF1F5F9)
                                                    },
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            Text(
                                                text = "${(explainerProgress * 100).toInt()}%",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyanAccent,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        LinearProgressIndicator(
                                            progress = { explainerProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = when {
                                                explainerRemainingSeconds <= 3 -> RoseAccent
                                                explainerRemainingSeconds <= 7 -> AmberAccent
                                                else -> CyanAccent
                                            },
                                            trackColor = Color(0xFF1E293B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                if (!isSubmitted) {
                                    Button(
                                        onClick = {
                                            isSubmitted = true
                                            isCorrect = true
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            onAnswerSubmitted(true, xpGained, card.fuelReward)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("explainer_understood_button")
                                    ) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Understood! (+10 XP)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // 4. MCQ, OUTPUT, FILL_BLANK, MYSTERY_CARD
                            else -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    card.options.forEach { opt ->
                                        val isChosen = selectedOption == opt
                                        val isTarget = opt == card.correctAnswer
                                        val optBg = when {
                                            isSubmitted && isTarget -> EmeraldSuccess.copy(alpha = 0.25f)
                                            isSubmitted && isChosen && !isTarget -> RoseAccent.copy(alpha = 0.25f)
                                            isChosen -> CyanAccent.copy(alpha = 0.2f)
                                            else -> Color(0xFF090D16)
                                        }
                                        val optBorder = when {
                                            isSubmitted && isTarget -> EmeraldSuccess
                                            isSubmitted && isChosen && !isTarget -> RoseAccent
                                            isChosen -> CyanAccent
                                            else -> Color(0xFF1E293B)
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(optBg)
                                                .border(1.dp, optBorder, RoundedCornerShape(10.dp))
                                                .clickable(enabled = !isSubmitted) {
                                                    selectedOption = opt
                                                    val correct = opt == card.correctAnswer
                                                    isCorrect = correct
                                                    isSubmitted = true
                                                    if (correct) {
                                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                    } else {
                                                        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                                        onSaveToFlashcards(card)
                                                    }
                                                    onAnswerSubmitted(correct, if (correct) xpGained else 3, if (correct) card.fuelReward else 1)
                                                }
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = opt,
                                                fontSize = 13.sp,
                                                color = Color(0xFFF1F5F9),
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Side Actions Rail (Right Edge)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 🔖 Save to Flashcards
                        IconButton(
                            onClick = {
                                isSaved = !isSaved
                                onSaveToFlashcards(card)
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF0F172A).copy(alpha = 0.8f), CircleShape)
                                .testTag("side_action_save")
                        ) {
                            Icon(
                                if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = if (isSaved) AmberAccent else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 💬 Explain More
                        IconButton(
                            onClick = { showExplanationDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF0F172A).copy(alpha = 0.8f), CircleShape)
                                .testTag("side_action_explain")
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = "Explain",
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // ⚔️ Challenge a Friend
                        IconButton(
                            onClick = { onChallengeFriend(card) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF0F172A).copy(alpha = 0.8f), CircleShape)
                                .testTag("side_action_challenge")
                        ) {
                            Icon(
                                Icons.Default.SportsKabaddi,
                                contentDescription = "Duel",
                                tint = RoseAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post-Answer Instant Feedback Strip
            AnimatedVisibility(visible = isSubmitted, enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCorrect) Color(0xFF064E3B).copy(alpha = 0.9f) else Color(0xFF4C0519).copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, if (isCorrect) EmeraldSuccess else RoseAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isCorrect) EmeraldSuccess else RoseAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isCorrect) "Correct! +$xpGained XP (${multiplier}x)" else "Incorrect · +3 XP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }

                            // Swipe up or Next button
                            Button(
                                onClick = onNextCard,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCorrect) EmeraldSuccess else Color(0xFFE11D48)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("feed_next_card_button")
                            ) {
                                Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // One-line explanation with "Why?" expand
                        Text(
                            text = card.explanation,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            maxLines = 2,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Near-miss & Adaptive notices
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🚀 ${if (streak % 5 == 0 && streak > 0) "Milestone reached!" else "${5 - (streak % 5)} more to Orbit milestone"}",
                                fontSize = 10.sp,
                                color = AmberAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!isCorrect) {
                                Text(
                                    text = "Saved to Flashcards deck 🔖",
                                    fontSize = 10.sp,
                                    color = Color(0xFFA78BFA)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog for Explain More
        if (showExplanationDialog) {
            AlertDialog(
                onDismissRequest = { showExplanationDialog = false },
                title = { Text("Deep Explanation · ${card.category}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(card.prompt, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(card.explanation, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 17.sp)
                        if (card.codeSnippet.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = card.codeSnippet,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showExplanationDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
