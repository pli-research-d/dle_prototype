package com.example.dle_prototype.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.LearningTelemetry
import com.example.dle_prototype.data.PersonalizationProfile
import com.example.dle_prototype.data.QuestionsRepository
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.TFLiteEngine
import com.example.dle_prototype.data.User
import com.example.dle_prototype.ui.components.TraitRadarCard
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timeline
import com.example.dle_prototype.data.ml.ModelWeights

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: User,
    dbHelper: DatabaseHelper,
    onStartQuiz: (categoryName: String, categoryNumber: Float) -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenTraining: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenEvolution: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenFederated: () -> Unit,
    onOpenUxTest: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var telemetry by remember { mutableStateOf<LearningTelemetry?>(null) }
    var profile by remember { mutableStateOf<PersonalizationProfile?>(null) }
    var recentAttempts by remember { mutableStateOf<List<QuizAttempt>>(emptyList()) }
    var userWeights by remember { mutableStateOf<ModelWeights?>(null) }
    var dueCardsCount by remember { mutableIntStateOf(0) }
    var totalFocusMins by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var showClearConfirm by remember { mutableStateOf(false) }

    suspend fun loadDashboardData() {
        isLoading = true
        val t = dbHelper.getTelemetry(user.username)
        telemetry = t
        val weights = dbHelper.loadModelWeights(user.username)
        userWeights = weights
        val p = TFLiteEngine.runInference(context, t, weights)
        profile = p
        // Record trajectory snapshot in SQLite
        dbHelper.saveTraitSnapshot(user.username, p)
        recentAttempts = dbHelper.getRecentQuizAttempts(user.username, 5)
        dueCardsCount = dbHelper.getDueFlashcards(user.username).size
        totalFocusMins = dbHelper.getTotalFocusMinutes(user.username)
        isLoading = false
    }

    LaunchedEffect(user.username) {
        loadDashboardData()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DLE Workspace",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Signed in as @${user.username}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenTraining,
                        modifier = Modifier.testTag("open_training_button")
                    ) {
                        Icon(
                            Icons.Default.ModelTraining,
                            contentDescription = "Train Model",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { coroutineScope.launch { loadDashboardData() } },
                        modifier = Modifier.testTag("refresh_dashboard_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
                    IconButton(
                        onClick = onOpenUxTest,
                        modifier = Modifier.testTag("open_ux_test_button")
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "UX Test View",
                            tint = EmeraldSuccess
                        )
                    }
                    IconButton(
                        onClick = onOpenDiagnostics,
                        modifier = Modifier.testTag("diagnostics_button")
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = "Diagnostics",
                            tint = CyanAccent
                        )
                    }
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.testTag("clear_data_button")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear Data",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (isLoading && profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Greeting & AI Status Header
                item {
                    HeaderBanner(
                        username = user.username,
                        topStrength = profile?.primaryStrength ?: "Learning",
                        modelSource = profile?.modelSource ?: "Baseline TFLite",
                        isPersonalized = profile?.isPersonalizedWeights ?: false,
                        onOpenTraining = onOpenTraining
                    )
                }

                // Metric Cards Grid
                item {
                    telemetry?.let { t ->
                        TelemetryMetricsRow(t)
                    }
                }

                // AI Trait Radar & Progress
                item {
                    profile?.let { p ->
                        TraitRadarCard(profile = p)
                    }
                }

                // AI Pedagogical Recommendations
                item {
                    profile?.let { p ->
                        RecommendationsCard(p)
                    }
                }

                // Category Quick-Launch Section
                item {
                    CategorySection(
                        onSelectCategory = { cat ->
                            onStartQuiz(cat.name, cat.id.toFloat())
                        }
                    )
                }

                // Advanced AI Capabilities Suite
                item {
                    AdvancedFeaturesSection(
                        dueCardsCount = dueCardsCount,
                        totalFocusMins = totalFocusMins,
                        onOpenFlashcards = onOpenFlashcards,
                        onOpenEvolution = onOpenEvolution,
                        onOpenFocus = onOpenFocus,
                        onOpenFederated = onOpenFederated,
                        onOpenUxTest = onOpenUxTest,
                        onOpenTraining = onOpenTraining
                    )
                }

                // Recent Quiz Attempts History
                item {
                    RecentActivitySection(recentAttempts)
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Reset User Data?") },
            text = { Text("This will permanently clear your quiz attempts and learning telemetry in SQLite.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        coroutineScope.launch {
                            dbHelper.clearUserData(user.username)
                            loadDashboardData()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HeaderBanner(
    username: String,
    topStrength: String,
    modelSource: String,
    isPersonalized: Boolean,
    onOpenTraining: () -> Unit
) {
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
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome back, $username! 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Strength: $topStrength • $modelSource",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isPersonalized) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPersonalized) EmeraldSuccess else MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPersonalized) "Personalized" else "Online",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPersonalized) EmeraldSuccess else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onOpenTraining,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_train_model_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.ModelTraining,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Train On-Device Neural Model →",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TelemetryMetricsRow(telemetry: LearningTelemetry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricTile(
            label = "Sessions",
            value = "${telemetry.loginCount}",
            icon = "⚡",
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "Study Time",
            value = String.format(Locale.getDefault(), "%.1fm", telemetry.totalTimeMinutes),
            icon = "⏱️",
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "Last Score",
            value = "${telemetry.lastQuizScore.toInt()}%",
            icon = "🎯",
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "Difficulty",
            value = "Lv ${telemetry.lastDifficultyReached.toInt()}",
            icon = "📈",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecommendationsCard(profile: PersonalizationProfile) {
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
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Personalized AI Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            profile.tips.forEach { tip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySection(
    onSelectCategory: (QuestionsRepository.CategoryInfo) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Interactive Learning Modules",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "6 Topics",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // 2x3 Grid using Columns & Rows
        val categories = QuestionsRepository.AVAILABLE_CATEGORIES
        categories.chunked(2).forEach { rowCats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowCats.forEach { cat ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectCategory(cat) }
                            .testTag("category_card_${cat.name.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = cat.icon, fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Start Quiz →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun RecentActivitySection(attempts: List<QuizAttempt>) {
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
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Recent Quiz History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (attempts.isEmpty()) {
                Text(
                    text = "No quizzes taken yet. Complete a quiz to see your performance history!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                attempts.forEach { att ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = att.category,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = dateFormat.format(Date(att.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (att.score >= (att.totalQuestions * 0.7))
                                        EmeraldSuccess.copy(alpha = 0.15f)
                                    else
                                        AmberAccent.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${att.score}/${att.totalQuestions} pts",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (att.score >= (att.totalQuestions * 0.7)) EmeraldSuccess else AmberAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedFeaturesSection(
    dueCardsCount: Int,
    totalFocusMins: Int,
    onOpenFlashcards: () -> Unit,
    onOpenEvolution: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenFederated: () -> Unit,
    onOpenUxTest: () -> Unit,
    onOpenTraining: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Adaptive Learning Suite",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Cognitive Augmentation",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CapabilityCard(
                    title = "Spaced Repetition",
                    subtitle = if (dueCardsCount > 0) "$dueCardsCount cards due" else "Deck synchronized",
                    pillText = if (dueCardsCount > 0) "Review Due" else "Up to date",
                    pillColor = if (dueCardsCount > 0) AmberAccent else EmeraldSuccess,
                    icon = Icons.Default.Style,
                    iconTint = CyanAccent,
                    testTag = "card_spaced_repetition",
                    onClick = onOpenFlashcards,
                    modifier = Modifier.weight(1f)
                )

                CapabilityCard(
                    title = "Cognitive Timeline",
                    subtitle = "Trajectory & velocity graph",
                    pillText = "Insights",
                    pillColor = IndigoPrimaryLight,
                    icon = Icons.Default.Timeline,
                    iconTint = IndigoPrimaryLight,
                    testTag = "card_trait_evolution",
                    onClick = onOpenEvolution,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CapabilityCard(
                    title = "Deep Work Timer",
                    subtitle = "$totalFocusMins focus mins logged",
                    pillText = "Cadence",
                    pillColor = EmeraldSuccess,
                    icon = Icons.Default.HourglassBottom,
                    iconTint = AmberAccent,
                    testTag = "card_focus_timer",
                    onClick = onOpenFocus,
                    modifier = Modifier.weight(1f)
                )

                CapabilityCard(
                    title = "Federated Peer Lab",
                    subtitle = "FedAvg consensus & tensors",
                    pillText = "Decentralized",
                    pillColor = CyanAccent,
                    icon = Icons.Default.Hub,
                    iconTint = EmeraldSuccess,
                    testTag = "card_federated_lab",
                    onClick = onOpenFederated,
                    modifier = Modifier.weight(1f)
                )
            }

            // Interactive UX Test Harness Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenUxTest)
                    .testTag("card_ux_test_harness"),
                shape = RoundedCornerShape(16.dp),
                color = CyanAccent.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "UX Test Harness (HTML / JS / CSS)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Interactive device frame & telemetry simulator",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Open →",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            }
        }
    }
}

@Composable
fun CapabilityCard(
    title: String,
    subtitle: String,
    pillText: String,
    pillColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(pillColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pillText,
                        style = MaterialTheme.typography.labelSmall,
                        color = pillColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
