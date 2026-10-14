package com.example.dle_prototype.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.graph.KnowledgeGraphAdaptiveQuizEngine
import com.example.dle_prototype.data.graph.KnowledgeGraphAdaptiveSession
import com.example.dle_prototype.data.graph.KnowledgeGraphAdjustmentEvent
import com.example.dle_prototype.data.graph.KnowledgeGraphData
import com.example.dle_prototype.data.graph.KnowledgeGraphQuestionBank
import com.example.dle_prototype.data.graph.KnowledgeGraphQuizQuestion
import com.example.dle_prototype.data.graph.KnowledgeGraphTriggerType
import com.example.dle_prototype.data.graph.KnowledgeMasteryLevel
import com.example.dle_prototype.data.graph.KnowledgeTopicNode
import com.example.dle_prototype.data.graph.KnowledgeQuizDifficultyTier
import com.example.dle_prototype.data.ml.AnswerConfidence
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.launch

/**
 * Real-Time Adaptive Quiz Component calibrated directly to the user's progress
 * through the Knowledge Graph.
 *
 * Dynamically adjusts question difficulty after every answer based on:
 * - Live Node Mastery Level (Beginner -> Intermediate -> Expert)
 * - Prerequisite edge health in the Knowledge Graph topology
 * - Real-time Confidence Calibration (Certain vs Guessing)
 * - Downstream concept unlocking when reaching Expert mastery.
 */
@Composable
fun KnowledgeGraphAdaptiveQuizComponent(
    node: KnowledgeTopicNode,
    graphData: KnowledgeGraphData? = null,
    user: User,
    dbHelper: DatabaseHelper,
    onDismiss: () -> Unit,
    onQuizFinished: (finalMastery: Float, newLevel: KnowledgeMasteryLevel) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val totalSessionQuestions = 8
    var questionPool by remember { mutableStateOf<List<KnowledgeGraphQuizQuestion>>(emptyList()) }
    val usedQuestionIds = remember { mutableSetOf<String>() }

    var session by remember {
        mutableStateOf(KnowledgeGraphAdaptiveQuizEngine.initializeSession(node, graphData))
    }
    var currentQuestion by remember { mutableStateOf<KnowledgeGraphQuizQuestion?>(null) }
    var selectedOption by remember { mutableStateOf<Any?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var selectedConfidence by remember { mutableStateOf(AnswerConfidence.CONFIDENT) }
    var lastAdjustmentEvent by remember { mutableStateOf<KnowledgeGraphAdjustmentEvent?>(null) }
    var isQuizCompleted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Pulsing halo for Expert tier transitions
    val infiniteTransition = rememberInfiniteTransition(label = "adaptive_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Load question pool on start
    LaunchedEffect(node.id) {
        isLoading = true
        val questions = KnowledgeGraphQuestionBank.getQuestionsForNode(context, node.id)
        questionPool = questions

        val firstQ = KnowledgeGraphAdaptiveQuizEngine.selectNextQuestion(session, questions, usedQuestionIds)
            ?: questions.firstOrNull()

        firstQ?.let {
            usedQuestionIds.add(it.id)
            currentQuestion = it
        }
        isLoading = false
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0B132B),
        border = BorderStroke(1.5.dp, session.currentTier.run {
            when (this) {
                KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE -> EmeraldSuccess
                KnowledgeQuizDifficultyTier.CORE_APPLICATION -> CyanAccent
                KnowledgeQuizDifficultyTier.FOUNDATIONAL -> AmberAccent
            }
        }.copy(alpha = 0.7f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("kg_adaptive_quiz_component")
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(36.dp))
                    Text(
                        text = "Calibrating Knowledge Graph Telemetry...",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            return@Surface
        }

        if (isQuizCompleted) {
            KnowledgeGraphQuizCompletionView(
                session = session,
                initialMastery = node.accuracyPercent,
                onDismiss = onDismiss,
                onRestart = {
                    coroutineScope.launch {
                        session = KnowledgeGraphAdaptiveQuizEngine.initializeSession(node, graphData)
                        usedQuestionIds.clear()
                        isSubmitted = false
                        selectedOption = null
                        lastAdjustmentEvent = null
                        isQuizCompleted = false
                        currentQuestion = KnowledgeGraphAdaptiveQuizEngine.selectNextQuestion(
                            session, questionPool, usedQuestionIds
                        )
                    }
                }
            )
            return@Surface
        }

        val q = currentQuestion
        if (q == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No candidate questions available for this Knowledge Graph node.", color = Color.White)
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // -------------------------------------------------------------
            // 1. TOP BAR: Node Monogram, Title, Ring Glow, & Close
            // -------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        session.masteryLevel.outerGlowColor.copy(alpha = 0.35f),
                                        Color(0xFF131D31)
                                    )
                                )
                            )
                            .border(
                                2.dp,
                                session.masteryLevel.ringColor,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = node.iconMonogram,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = session.masteryLevel.ringColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = node.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = session.masteryLevel.ringColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${session.masteryLevel.label} (Ring ${session.masteryLevel.ringCount})",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = session.masteryLevel.ringColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Knowledge Graph Adaptive Quiz • Q ${session.totalAnswered + 1}/$totalSessionQuestions",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("kg_quiz_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            // -------------------------------------------------------------
            // 2. REAL-TIME DIFFICULTY GAUGE & KNOWLEDGE GRAPH MASTERY BAR
            // -------------------------------------------------------------
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF131D31),
                border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Current Real-Time Difficulty Tier Indicator
                        val tierColor = when (session.currentTier) {
                            KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE -> EmeraldSuccess
                            KnowledgeQuizDifficultyTier.CORE_APPLICATION -> CyanAccent
                            KnowledgeQuizDifficultyTier.FOUNDATIONAL -> AmberAccent
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = tierColor.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, tierColor.copy(alpha = if (session.currentTier == KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE) pulseAlpha else 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = session.currentTier.iconEmoji, fontSize = 11.sp)
                                    Text(
                                        text = session.currentTier.label.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = tierColor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Text(
                                text = "Difficulty: ${"%.2f".format(session.currentComplexity)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCBD5E1),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Real-time Node Mastery %
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = session.masteryLevel.ringColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Node Mastery: ${session.currentMasteryPercent.toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = session.masteryLevel.ringColor
                            )
                        }
                    }

                    // Continuous Linear Progress of Node Mastery
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        LinearProgressIndicator(
                            progress = { (session.currentMasteryPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = session.masteryLevel.ringColor,
                            trackColor = Color(0xFF1E293B)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0% Beginner", fontSize = 8.sp, color = Color(0xFF64748B))
                            Text("60% Intermediate", fontSize = 8.sp, color = CyanAccent)
                            Text("80% Expert 🥇", fontSize = 8.sp, color = EmeraldSuccess)
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. REAL-TIME ADJUSTMENT BANNER (Explains why difficulty changed)
            // -------------------------------------------------------------
            lastAdjustmentEvent?.let { evt ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val eventBorder = when (evt.triggerType) {
                        KnowledgeGraphTriggerType.EXPERT_MASTERY_REACHED -> EmeraldSuccess
                        KnowledgeGraphTriggerType.TIER_ESCALATION -> CyanAccent
                        KnowledgeGraphTriggerType.PREREQUISITE_SCAFFOLDING -> AmberAccent
                        KnowledgeGraphTriggerType.MISCONCEPTION_REPAIR -> RoseAccent
                        else -> Color(0xFF334155)
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF070E1E),
                        border = BorderStroke(1.dp, eventBorder.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (evt.triggerType) {
                                    KnowledgeGraphTriggerType.EXPERT_MASTERY_REACHED -> Icons.Default.AutoAwesome
                                    KnowledgeGraphTriggerType.TIER_ESCALATION -> Icons.Default.Speed
                                    KnowledgeGraphTriggerType.PREREQUISITE_SCAFFOLDING -> Icons.Default.Warning
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = eventBorder,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = evt.reason,
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 4. TOPOLOGICAL PATHWAY / PREREQUISITE INDICATOR
            // -------------------------------------------------------------
            if (session.scaffoldActive && session.scaffoldPrerequisiteTitle != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Hub, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
                        Text(
                            text = "PREREQUISITE SCAFFOLD: Addressing gap in '${session.scaffoldPrerequisiteTitle}'",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 5. QUESTION CARD & CONCEPT TAG
            // -------------------------------------------------------------
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF131D31),
                border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "CONCEPT: ${q.conceptTag.uppercase()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "${q.difficultyTier.iconEmoji} Tier: ${q.difficultyTier.label}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = q.question,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF8FAFC),
                        lineHeight = 22.sp
                    )
                }
            }

            // -------------------------------------------------------------
            // 6. INTERACTIVE OPTIONS (MCQ / True-False)
            // -------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                q.options.forEachIndexed { idx, optionText ->
                    val isSelected = selectedOption == optionText
                    val isCorrectOption = optionText == q.correctAnswer.toString()

                    val optionBorderColor by animateColorAsState(
                        targetValue = when {
                            !isSubmitted && isSelected -> CyanAccent
                            isSubmitted && isCorrectOption -> EmeraldSuccess
                            isSubmitted && isSelected && !isCorrectOption -> RoseAccent
                            else -> Color(0xFF1E2E4A)
                        },
                        label = "option_border"
                    )

                    val optionBgColor by animateColorAsState(
                        targetValue = when {
                            !isSubmitted && isSelected -> CyanAccent.copy(alpha = 0.15f)
                            isSubmitted && isCorrectOption -> EmeraldSuccess.copy(alpha = 0.20f)
                            isSubmitted && isSelected && !isCorrectOption -> RoseAccent.copy(alpha = 0.20f)
                            else -> Color(0xFF131D31)
                        },
                        label = "option_bg"
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = optionBgColor,
                        border = BorderStroke(if (isSelected || (isSubmitted && isCorrectOption)) 1.5.dp else 1.dp, optionBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isSubmitted) {
                                selectedOption = optionText
                            }
                            .testTag("kg_quiz_option_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyanAccent else Color(0xFF1E293B))
                                    .border(1.dp, if (isSelected) CyanAccent else Color(0xFF475569), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A'.code + idx).toChar().toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)
                                )
                            }

                            Text(
                                text = optionText,
                                fontSize = 13.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSubmitted && isCorrectOption) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 7. CONFIDENCE SELECTOR & SUBMIT / NEXT CONTROLS
            // -------------------------------------------------------------
            if (!isSubmitted) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CALIBRATE CONFIDENCE:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )

                        Text(
                            text = if (selectedConfidence == AnswerConfidence.CONFIDENT) "Accelerates Difficulty" else "Consolidates Fundamentals",
                            fontSize = 9.sp,
                            color = if (selectedConfidence == AnswerConfidence.CONFIDENT) CyanAccent else AmberAccent
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedConfidence == AnswerConfidence.CONFIDENT) CyanAccent.copy(alpha = 0.2f) else Color(0xFF131D31),
                            border = BorderStroke(1.dp, if (selectedConfidence == AnswerConfidence.CONFIDENT) CyanAccent else Color(0xFF1E2E4A)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedConfidence = AnswerConfidence.CONFIDENT }
                                .testTag("confidence_certain_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🎯 Certain (Confident)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedConfidence == AnswerConfidence.CONFIDENT) CyanAccent else Color(0xFF94A3B8))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedConfidence == AnswerConfidence.GUESSING) AmberAccent.copy(alpha = 0.2f) else Color(0xFF131D31),
                            border = BorderStroke(1.dp, if (selectedConfidence == AnswerConfidence.GUESSING) AmberAccent else Color(0xFF1E2E4A)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedConfidence = AnswerConfidence.GUESSING }
                                .testTag("confidence_guessing_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🎲 Guessing", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedConfidence == AnswerConfidence.GUESSING) AmberAccent else Color(0xFF94A3B8))
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                isSubmitted = true
                                val isCorrect = selectedOption.toString() == q.correctAnswer.toString()
                                val (newSession, event) = KnowledgeGraphAdaptiveQuizEngine.processAnswer(
                                    session = session,
                                    question = q,
                                    isCorrect = isCorrect,
                                    confidence = selectedConfidence
                                )
                                session = newSession
                                lastAdjustmentEvent = event
                            }
                        },
                        enabled = selectedOption != null,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("kg_quiz_submit_button")
                    ) {
                        Text(
                            text = "Submit & Adjust Difficulty",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // Post-submission feedback & Continue
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val wasCorrect = selectedOption.toString() == q.correctAnswer.toString()

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (wasCorrect) EmeraldSuccess.copy(alpha = 0.15f) else RoseAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (wasCorrect) EmeraldSuccess else RoseAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (wasCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (wasCorrect) EmeraldSuccess else RoseAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (wasCorrect) "Correct! Telemetry updated." else "Incorrect: Conceptual gap registered.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (wasCorrect) EmeraldSuccess else RoseAccent
                                )
                            }
                            Text(
                                text = q.explanation,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (session.totalAnswered >= totalSessionQuestions) {
                                // Record in SQLite
                                coroutineScope.launch {
                                    dbHelper.recordQuizResult(
                                        username = user.username,
                                        category = node.category,
                                        categoryNumber = node.categoryNumber,
                                        score = session.correctCount,
                                        totalQuestions = session.totalAnswered,
                                        difficultyLevel = session.currentComplexity
                                    )
                                    dbHelper.updateUserStreakOnQuizCompletion(user.username)
                                }
                                isQuizCompleted = true
                                onQuizFinished(session.currentMasteryPercent, session.masteryLevel)
                            } else {
                                // Advance to next real-time adjusted question
                                isSubmitted = false
                                selectedOption = null
                                val nextQ = KnowledgeGraphAdaptiveQuizEngine.selectNextQuestion(
                                    session, questionPool, usedQuestionIds
                                )
                                if (nextQ != null) {
                                    usedQuestionIds.add(nextQ.id)
                                    currentQuestion = nextQ
                                } else {
                                    isQuizCompleted = true
                                    onQuizFinished(session.currentMasteryPercent, session.masteryLevel)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("kg_quiz_continue_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (session.totalAnswered >= totalSessionQuestions) "View Knowledge Graph Advancement" else "Next Question →",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Summary view presented when the adaptive session finishes, visualizing
 * real-time growth across the Knowledge Graph.
 */
@Composable
private fun KnowledgeGraphQuizCompletionView(
    session: KnowledgeGraphAdaptiveSession,
    initialMastery: Float,
    onDismiss: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Milestone Trophy Header
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(session.masteryLevel.ringColor.copy(alpha = 0.2f))
                .border(2.dp, session.masteryLevel.ringColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = session.masteryLevel.ringColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Knowledge Graph Updated!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${session.currentNode.title} • Dynamic Calibration Complete",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // Stats Matrix Surface
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131D31),
            border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("FINAL MASTERY", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                        Text(
                            text = "${session.currentMasteryPercent.toInt()}% (${session.masteryLevel.label})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = session.masteryLevel.ringColor
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldSuccess.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "+${(session.currentMasteryPercent - initialMastery).toInt().coerceAtLeast(0)}% Growth",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("QUESTIONS ANSWERED", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                        Text("${session.correctCount} / ${session.totalAnswered} Correct", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("FINAL DIFFICULTY TIER", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                        Text(
                            text = "${session.currentTier.iconEmoji} ${session.currentTier.label} (${"%.2f".format(session.currentComplexity)})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }

                if (session.unlockedDownstreamNodes.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("UNLOCKED DOWNSTREAM TOPICS:", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                        Text(
                            text = session.unlockedDownstreamNodes.joinToString(" • ") { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRestart,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF475569)),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Practice Again", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1.2f)
            ) {
                Text("Return to Graph", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Bottom Sheet container for modal launching of the Knowledge Graph Adaptive Quiz.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeGraphAdaptiveQuizSheet(
    node: KnowledgeTopicNode,
    graphData: KnowledgeGraphData? = null,
    user: User,
    dbHelper: DatabaseHelper,
    onDismiss: () -> Unit,
    onQuizFinished: (finalMastery: Float, newLevel: KnowledgeMasteryLevel) -> Unit = { _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF070E1E),
        dragHandle = null
    ) {
        KnowledgeGraphAdaptiveQuizComponent(
            node = node,
            graphData = graphData,
            user = user,
            dbHelper = dbHelper,
            onDismiss = onDismiss,
            onQuizFinished = onQuizFinished,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}
