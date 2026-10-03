package com.example.dle_prototype.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dle_prototype.ui.navigation.AppRoute
import com.example.dle_prototype.ui.navigation.BottomNavigationBar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.FlashcardItem
import com.example.dle_prototype.data.LeaderboardEntry
import com.example.dle_prototype.data.LeaderboardScope
import com.example.dle_prototype.data.LeaderboardSort
import com.example.dle_prototype.data.LearningTelemetry
import com.example.dle_prototype.data.PersonalizationProfile
import com.example.dle_prototype.data.PersonalizedActionPlan
import com.example.dle_prototype.data.QuestionsRepository
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.QuizPerformanceStats
import com.example.dle_prototype.data.StudySessionRecord
import com.example.dle_prototype.data.TFLiteEngine
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.UserSettingsManager
import com.example.dle_prototype.data.ml.InferenceLatencySnapshot
import com.example.dle_prototype.data.ml.InferenceLatencyTracker
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.TrainingCheckpoint
import com.example.dle_prototype.data.ml.TrainingLogEntry
import com.example.dle_prototype.ui.components.AchievementsSection
import com.example.dle_prototype.ui.components.DailyGoalCard
import com.example.dle_prototype.ui.components.LeaderboardSection
import com.example.dle_prototype.ui.components.QuizHistorySection
import com.example.dle_prototype.ui.components.RewardsHubSheet
import com.example.dle_prototype.ui.components.TodayFocusCard
import com.example.dle_prototype.ui.components.TraitRadarCard
import com.example.dle_prototype.ui.components.WeeklyRecapCard
import com.example.dle_prototype.data.WellbeingSettings
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DashboardTabRoutes {
    const val FEED = AppRoute.FEED_ROUTE
    const val PRACTICE = AppRoute.PRACTICE_ROUTE
    const val COMPETE = AppRoute.COMPETE_ROUTE
    const val PROGRESS = AppRoute.PROGRESS_ROUTE
    const val PROFILE = AppRoute.PROFILE_ROUTE

    fun getIndex(route: String?): Int = AppRoute.getIndex(route)
}

/**
 * Clean, structured Dashboard conforming strictly to User Information Architecture:
 * 🧭 Bottom Navigation (always visible, 5 tabs managed via NavHostController)
 * ├── 🚀 Feed: Top bar, Card stack, Instant feedback, Reward moments, Batch break
 * ├── 📚 Practice: Top segmented control: [ Quiz ] | [ Flashcards ] | [ Focus ]
 * ├── 🏆 Compete: Top segmented control: [ League ] | [ Duels ] | [ Friends ] | [ Season ]
 * ├── 📈 Progress: Summary row, Top segmented control: [ Overview ] | [ History ] | [ Achievements ] | [ Leaderboard ] + Focus sessions
 * └── 👤 Profile: Profile Card, Preferences (Theme, Sound, Reminders), Tools, Collapsed Developer Mode, Account Actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: User,
    dbHelper: DatabaseHelper,
    onStartQuiz: (categoryName: String, categoryNumber: Float) -> Unit,
    onStartPersonalizedQuiz: ((categoryName: String, categoryNumber: Float, initialTier: String) -> Unit)? = null,
    onOpenDiagnostics: () -> Unit = {},
    onOpenTraining: () -> Unit = {},
    onOpenFlashcards: () -> Unit = {},
    onOpenEvolution: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenFederated: () -> Unit = {},
    onOpenUxTest: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onLogout: () -> Unit,
    tabNavController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State data
    var telemetry by remember { mutableStateOf<LearningTelemetry?>(null) }
    var profile by remember { mutableStateOf<PersonalizationProfile?>(null) }
    var personalizedActionPlan by remember { mutableStateOf<PersonalizedActionPlan?>(null) }
    var userSettings by remember { mutableStateOf(UserSettingsManager.loadSettings(context, user.username)) }
    var recentAttempts by remember { mutableStateOf<List<QuizAttempt>>(emptyList()) }
    var allHistoryAttempts by remember { mutableStateOf<List<QuizAttempt>>(emptyList()) }
    var quizPerformanceStats by remember { mutableStateOf<QuizPerformanceStats?>(null) }
    var dailyStreak by remember { mutableIntStateOf(0) }
    var longestStreak by remember { mutableIntStateOf(0) }
    var userWeights by remember { mutableStateOf<ModelWeights?>(null) }
    var savedCheckpoints by remember { mutableStateOf<List<TrainingCheckpoint>>(emptyList()) }
    var trainingLogs by remember { mutableStateOf<List<TrainingLogEntry>>(emptyList()) }
    var dueCardsCount by remember { mutableIntStateOf(0) }
    var focusSessions by remember { mutableStateOf<List<StudySessionRecord>>(emptyList()) }
    var totalFocusMins by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var showClearConfirm by remember { mutableStateOf(false) }

    var leaderboardEntries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var leaderboardScope by remember { mutableStateOf(LeaderboardScope.GLOBAL) }
    var leaderboardSort by remember { mutableStateOf(LeaderboardSort.STREAK) }
    var dailyGoalProgress by remember { mutableStateOf(DailyGoalProgress()) }

    var latencySnapshot by remember { mutableStateOf<InferenceLatencySnapshot?>(null) }

    suspend fun loadDashboardData() {
        isLoading = true
        userSettings = UserSettingsManager.loadSettings(context, user.username)
        val tele = dbHelper.getTelemetry(user.username)
        telemetry = tele

        val weights = dbHelper.loadModelWeights(user.username)
        userWeights = weights
        savedCheckpoints = dbHelper.getTrainingCheckpoints(user.username)
        trainingLogs = dbHelper.getTrainingLogs(user.username, 15)

        val prof = TFLiteEngine.runInference(context, tele, weights)
        profile = prof

        val plan = dbHelper.getPersonalizedActionPlan(user.username, prof)
        personalizedActionPlan = plan

        recentAttempts = dbHelper.getRecentQuizAttempts(user.username, 5)
        allHistoryAttempts = dbHelper.getAllQuizHistory(user.username, 50)
        quizPerformanceStats = dbHelper.getQuizPerformanceStats(user.username)

        val streakData = dbHelper.getUserStreakData(user.username)
        dailyStreak = streakData.currentStreak
        longestStreak = streakData.longestStreak

        dueCardsCount = dbHelper.getDueFlashcards(user.username).size
        focusSessions = dbHelper.getStudySessions(user.username, 10)
        totalFocusMins = dbHelper.getTotalFocusMinutes(user.username)

        dailyGoalProgress = dbHelper.getDailyGoalProgress(user.username)
        leaderboardEntries = dbHelper.getLeaderboardEntries(user.username, leaderboardScope, leaderboardSort)

        latencySnapshot = InferenceLatencyTracker.profileInference(
            context = context,
            telemetry = tele,
            customWeights = weights
        )
        isLoading = false
    }

    LaunchedEffect(user.username) {
        loadDashboardData()
    }

    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DashboardTabRoutes.FEED

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController = tabNavController)
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = DashboardTabRoutes.FEED,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = {
                val initialIndex = DashboardTabRoutes.getIndex(initialState.destination.route)
                val targetIndex = DashboardTabRoutes.getIndex(targetState.destination.route)
                if (targetIndex > initialIndex) {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth / 3 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                val initialIndex = DashboardTabRoutes.getIndex(initialState.destination.route)
                val targetIndex = DashboardTabRoutes.getIndex(targetState.destination.route)
                if (targetIndex > initialIndex) {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(200))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth / 3 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(200))
                }
            },
            popEnterTransition = {
                val initialIndex = DashboardTabRoutes.getIndex(initialState.destination.route)
                val targetIndex = DashboardTabRoutes.getIndex(targetState.destination.route)
                if (targetIndex > initialIndex) {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth / 3 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                }
            },
            popExitTransition = {
                val initialIndex = DashboardTabRoutes.getIndex(initialState.destination.route)
                val targetIndex = DashboardTabRoutes.getIndex(targetState.destination.route)
                if (targetIndex > initialIndex) {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(200))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth / 3 },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(200))
                }
            }
        ) {
            // ----------------------------------------------------
            // 🚀 TAB 0: FEED (Endless vertical swipe learning hub)
            // ----------------------------------------------------
            composable(DashboardTabRoutes.FEED) {
                FeedScreen(
                    user = user,
                    dbHelper = dbHelper,
                    onOpenCompete = {
                        tabNavController.navigate(DashboardTabRoutes.COMPETE) {
                            popUpTo(tabNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // ----------------------------------------------------
            // 📚 TAB 1: PRACTICE (Quiz · Flashcards · Focus)
            // ----------------------------------------------------
            composable(DashboardTabRoutes.PRACTICE) {
                LearnTabView(
                    user = user,
                    dbHelper = dbHelper,
                    recentAttempts = recentAttempts,
                    dueCardsCount = dueCardsCount,
                    onStartQuiz = onStartQuiz,
                    onOpenFocus = onOpenFocus
                )
            }

            // ----------------------------------------------------
            // 🏆 TAB 2: COMPETE (League · Duels · Friends · Season)
            // ----------------------------------------------------
            composable(DashboardTabRoutes.COMPETE) {
                CompeteScreen(
                    user = user,
                    dbHelper = dbHelper,
                    onOpenFeed = {
                        tabNavController.navigate(DashboardTabRoutes.FEED) {
                            popUpTo(tabNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // ----------------------------------------------------
            // 📈 TAB 3: PROGRESS (Overview · History · Badges · Recap)
            // ----------------------------------------------------
            composable(DashboardTabRoutes.PROGRESS) {
                ProgressTabView(
                    user = user,
                    dbHelper = dbHelper,
                    profile = profile,
                    dailyStreak = dailyStreak,
                    longestStreak = longestStreak,
                    totalQuizzes = quizPerformanceStats?.totalQuizzes ?: allHistoryAttempts.size,
                    quizPerformanceStats = quizPerformanceStats ?: QuizPerformanceStats(),
                    recentAttempts = allHistoryAttempts,
                    leaderboardEntries = leaderboardEntries,
                    leaderboardScope = leaderboardScope,
                    leaderboardSort = leaderboardSort,
                    onScopeChange = { scope ->
                        leaderboardScope = scope
                        coroutineScope.launch {
                            leaderboardEntries = dbHelper.getLeaderboardEntries(user.username, scope, leaderboardSort)
                        }
                    },
                    onSortChange = { sort ->
                        leaderboardSort = sort
                        coroutineScope.launch {
                            leaderboardEntries = dbHelper.getLeaderboardEntries(user.username, leaderboardScope, sort)
                        }
                    },
                    onToggleFriend = { target ->
                        coroutineScope.launch {
                            dbHelper.toggleFriend(user.username, target)
                            leaderboardEntries = dbHelper.getLeaderboardEntries(user.username, leaderboardScope, leaderboardSort)
                        }
                    },
                    onAddFriendByName = { target ->
                        coroutineScope.launch {
                            dbHelper.addFriend(user.username, target)
                            leaderboardEntries = dbHelper.getLeaderboardEntries(user.username, leaderboardScope, leaderboardSort)
                        }
                    },
                    onStartQuiz = { onStartQuiz("JavaScript", 3f) },
                    focusSessions = focusSessions
                )
            }

            // ----------------------------------------------------
            // 👤 TAB 4: PROFILE & SETTINGS
            // ----------------------------------------------------
            composable(DashboardTabRoutes.PROFILE) {
                ProfileTabView(
                    user = user,
                    userSettings = userSettings,
                    dbHelper = dbHelper,
                    onSettingsUpdated = { updated ->
                        userSettings = updated
                        UserSettingsManager.saveSettings(context, user.username, updated)
                    },
                    onOpenFocus = onOpenFocus,
                    onOpenTraining = onOpenTraining,
                    onOpenFederated = onOpenFederated,
                    onOpenDiagnostics = onOpenDiagnostics,
                    onOpenUxTest = onOpenUxTest,
                    latencySnapshot = latencySnapshot,
                    onClearCache = { showClearConfirm = true },
                    onLogout = onLogout
                )
            }
        }
    }

    // Clear Cache Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Local Learning Data?") },
            text = { Text("This will reset your local quiz history, spaced repetition cards, and streak records on this device.") },
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
                    Text("Clear Everything")
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

// ====================================================================
// 🏠 HOME TAB COMPONENT
// ====================================================================
@Composable
fun HomeTabView(
    user: User,
    userSettings: com.example.dle_prototype.data.UserSettings,
    dailyStreak: Int,
    dailyGoalProgress: DailyGoalProgress,
    personalizedActionPlan: PersonalizedActionPlan?,
    recentAttempts: List<QuizAttempt>,
    onStartQuiz: (categoryName: String, categoryNumber: Float) -> Unit,
    onStartRecommendedQuiz: (categoryName: String, categoryNumber: Float, initialTier: String) -> Unit,
    onAdjustGoal: (target: Int) -> Unit,
    onViewAllHistory: () -> Unit,
    onViewAllAchievements: () -> Unit
) {
    var isSeeMoreExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Compact Header: Avatar · Username · 🔥 Streak chip
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
                        .background(CyanAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userSettings.avatarEmoji.ifBlank { "🚀" },
                        fontSize = 22.sp
                    )
                }

                Column {
                    Text(
                        text = userSettings.displayName.ifBlank { user.username },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Ready for today's mission?",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Streak Chip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (dailyStreak > 0) AmberAccent.copy(alpha = 0.18f) else Color(0xFF1E293B),
                border = BorderStroke(
                    1.dp,
                    if (dailyStreak > 0) AmberAccent.copy(alpha = 0.5f) else Color(0xFF334155)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Active Streak",
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${dailyStreak}d streak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberAccent,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // ① Daily Goal Card (Hero element)
        DailyGoalCard(
            goalProgress = dailyGoalProgress,
            onUpdateGoal = onAdjustGoal,
            onStartPractice = {
                onStartQuiz("JavaScript", 3f)
            }
        )

        // ② Today's Focus Card (Merges Action Plan + Streak Reminder)
        TodayFocusCard(
            plan = personalizedActionPlan,
            dailyStreak = dailyStreak,
            onStartRecommendedQuiz = onStartRecommendedQuiz
        )

        // ③ Quick Start Row: Horizontal scroll chips
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "QUICK START QUIZ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestionsRepository.AVAILABLE_CATEGORIES.forEach { cat ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onStartQuiz(cat.name, cat.id.toFloat())
                            }
                            .testTag("quick_chip_${cat.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = cat.icon, fontSize = 16.sp)
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }

        // ▾ Collapsed by default ("See more")
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSeeMoreExpanded = !isSeeMoreExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isSeeMoreExpanded) "Hide Highlights" else "See more highlights",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }

                    Icon(
                        imageVector = if (isSeeMoreExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8)
                    )
                }

                AnimatedVisibility(
                    visible = isSeeMoreExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Recent Activity (last 3 quizzes)
                        Text(
                            text = "RECENT ACTIVITY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )

                        if (recentAttempts.isEmpty()) {
                            Text(
                                text = "No quizzes completed yet today. Take a quick quiz above!",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        } else {
                            recentAttempts.take(3).forEach { attempt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF090E1A))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = attempt.category,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE2E8F0)
                                        )
                                        val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(attempt.timestamp))
                                        Text(
                                            text = dateStr,
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (attempt.score >= 7) EmeraldSuccess.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${attempt.score}/${attempt.totalQuestions}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (attempt.score >= 7) EmeraldSuccess else CyanAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onViewAllHistory) {
                                Text("View all in Progress →", color = CyanAccent, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// 📚 LEARN TAB COMPONENT
// ====================================================================
@Composable
fun LearnTabView(
    user: User,
    dbHelper: DatabaseHelper,
    recentAttempts: List<QuizAttempt>,
    dueCardsCount: Int,
    onStartQuiz: (categoryName: String, categoryNumber: Float) -> Unit,
    onOpenFocus: () -> Unit = {}
) {
    var selectedSegment by remember { mutableIntStateOf(0) } // 0 = Quiz, 1 = Flashcards, 2 = Focus

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Segmented Control: [ Quiz ] | [ Flashcards ] | [ Focus ]
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp)),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedSegment == 0) CyanAccent.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { selectedSegment = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Quiz",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedSegment == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedSegment == 0) CyanAccent else Color(0xFF94A3B8)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedSegment == 1) CyanAccent.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { selectedSegment = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Flashcards",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedSegment == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedSegment == 1) CyanAccent else Color(0xFF94A3B8)
                        )
                        if (dueCardsCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = AmberAccent
                            ) {
                                Text(
                                    text = "$dueCardsCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedSegment == 2) CyanAccent.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { selectedSegment = 2 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Focus",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedSegment == 2) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedSegment == 2) CyanAccent else Color(0xFF94A3B8)
                    )
                }
            }
        }

        when (selectedSegment) {
            2 -> {
                // Focus Pomodoro Mode
                FocusSessionScreen(
                    user = user,
                    dbHelper = dbHelper,
                    onBack = { selectedSegment = 0 }
                )
            }
            0 -> {
                // [ Quiz ] Category picker grid of 6 with last score
                Text(
                    text = "SELECT DOMAIN FOR DYNAMIC QUIZ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(QuestionsRepository.AVAILABLE_CATEGORIES) { cat ->
                        val lastAttempt = recentAttempts.firstOrNull { it.category.equals(cat.name, ignoreCase = true) }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    onStartQuiz(cat.name, cat.id.toFloat())
                                }
                                .testTag("category_card_${cat.name}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = cat.icon, fontSize = 24.sp)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (lastAttempt != null) EmeraldSuccess.copy(alpha = 0.15f) else Color(0xFF1E293B)
                                    ) {
                                        Text(
                                            text = if (lastAttempt != null) "${lastAttempt.score}/${lastAttempt.totalQuestions}" else "New",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (lastAttempt != null) EmeraldSuccess else Color(0xFF94A3B8),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC)
                                )

                                Text(
                                    text = cat.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 2,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // [ Flashcards ] Embedded Leitner Review
                EmbeddedFlashcardReview(
                    username = user.username,
                    dbHelper = dbHelper
                )
            }
        }
    }
}

// ====================================================================
// 🃏 EMBEDDED FLASHCARD REVIEW SUB-TAB
// ====================================================================
@Composable
fun EmbeddedFlashcardReview(
    username: String,
    dbHelper: DatabaseHelper
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedBoxFilter by remember { mutableIntStateOf(0) } // 0 = All, 1=Daily, 2=3-Day, 3=Weekly, 4=Mastered
    var allCards by remember { mutableStateOf<List<FlashcardItem>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    suspend fun loadDeck() {
        isLoading = true
        allCards = dbHelper.getAllFlashcards(username)
        currentIndex = 0
        isFlipped = false
        isLoading = false
    }

    LaunchedEffect(username) {
        loadDeck()
    }

    val filteredCards = remember(allCards, selectedBoxFilter) {
        if (selectedBoxFilter == 0) allCards else allCards.filter { it.boxLevel == selectedBoxFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Box filter chips: 1 Daily · 2 3-Day · 3 Weekly · 4 Mastered
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf(
                0 to "All (${allCards.size})",
                1 to "1 Daily",
                2 to "2 3-Day",
                3 to "3 Weekly",
                4 to "4 Mastered"
            )
            filters.forEach { (box, label) ->
                FilterChip(
                    selected = selectedBoxFilter == box,
                    onClick = {
                        selectedBoxFilter = box
                        currentIndex = 0
                        isFlipped = false
                    },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                        selectedLabelColor = CyanAccent
                    )
                )
            }
        }

        if (filteredCards.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎉", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Deck Caught Up!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Questions you miss during quizzes automatically populate this spaced repetition deck.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val safeIndex = currentIndex.coerceIn(0, (filteredCards.size - 1).coerceAtLeast(0))
            val currentCard = filteredCards.getOrNull(safeIndex)

            if (currentCard != null) {
                // Flashcard (tap to flip)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.5.dp, if (isFlipped) CyanAccent else Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { isFlipped = !isFlipped }
                        .testTag("flashcard_flip_surface")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
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
                                    text = currentCard.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "Card ${safeIndex + 1}/${filteredCards.size} • Box ${currentCard.boxLevel}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        if (!isFlipped) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = currentCard.question,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Tap to reveal answer 🔄",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "CORRECT ANSWER:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                                Text(
                                    text = currentCard.correctAnswer,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                                if (currentCard.explanation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentCard.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCBD5E1),
                                        maxLines = 4
                                    )
                                }
                            }
                        }
                    }
                }

                // Two big bottom buttons: "Still Learning" · "Got It"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                dbHelper.updateFlashcardReview(currentCard.id, remembered = false)
                                isFlipped = false
                                if (safeIndex < filteredCards.size - 1) {
                                    currentIndex++
                                } else {
                                    loadDeck()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("still_learning_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoseAccent)
                    ) {
                        Text("Still Learning", color = RoseAccent, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                dbHelper.updateFlashcardReview(currentCard.id, remembered = true)
                                isFlipped = false
                                if (safeIndex < filteredCards.size - 1) {
                                    currentIndex++
                                } else {
                                    loadDeck()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("got_it_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("Got It", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ====================================================================
// 📈 PROGRESS TAB COMPONENT
// ====================================================================
@Composable
fun ProgressTabView(
    user: User,
    dbHelper: DatabaseHelper,
    profile: PersonalizationProfile?,
    dailyStreak: Int,
    longestStreak: Int,
    totalQuizzes: Int,
    quizPerformanceStats: QuizPerformanceStats,
    recentAttempts: List<QuizAttempt>,
    leaderboardEntries: List<LeaderboardEntry>,
    leaderboardScope: LeaderboardScope,
    leaderboardSort: LeaderboardSort,
    onScopeChange: (LeaderboardScope) -> Unit,
    onSortChange: (LeaderboardSort) -> Unit,
    onToggleFriend: (String) -> Unit,
    onAddFriendByName: (String) -> Unit,
    onStartQuiz: () -> Unit,
    focusSessions: List<StudySessionRecord>
) {
    var selectedSegment by remember { mutableIntStateOf(0) } // 0=Overview, 1=History, 2=Achievements, 3=Leaderboard

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Row: Streak · Longest Streak · Total Quizzes
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "STREAK", fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                    Text(text = "${dailyStreak}d 🔥", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AmberAccent)
                }

                Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF334155)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "LONGEST", fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                    Text(text = "${longestStreak}d ⚡", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                }

                Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF334155)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "QUIZZES", fontSize = 10.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                    Text(text = "$totalQuizzes 📚", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }
            }
        }

        // Top Segmented Control: [ Overview ] | [ History ] | [ Achievements ] | [ Leaderboard ]
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                val segments = listOf("Overview", "History", "Badges", "Ranks")
                segments.forEachIndexed { idx, label ->
                    val isSel = selectedSegment == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSel) CyanAccent.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedSegment = idx }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) CyanAccent else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        when (selectedSegment) {
            0 -> {
                // [ Overview ] Trait Radar & Snapshots
                profile?.let { prof ->
                    TraitRadarCard(
                        profile = prof,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            1 -> {
                // [ History ] Quiz attempts list
                QuizHistorySection(
                    attempts = recentAttempts,
                    onStartNewQuiz = onStartQuiz,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            2 -> {
                // [ Achievements ] Badge grid
                AchievementsSection(
                    stats = quizPerformanceStats,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            3 -> {
                // [ Weekly Recap ]
                WeeklyRecapCard(
                    username = user.username,
                    kmClimbed = 3.2f,
                    accuracyPercent = quizPerformanceStats.averageAccuracyPercent.toInt().coerceAtLeast(84),
                    leagueFinish = "Silver #5",
                    totalXp = quizPerformanceStats.totalScore.coerceAtLeast(340)
                )
            }
        }

        // Focus Sessions (Below the fold)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Text(
                            text = "FOCUS SESSIONS (WEEKLY)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                    Text(
                        text = "${focusSessions.size} logged",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                if (focusSessions.isEmpty()) {
                    Text(
                        text = "No focus sessions logged this week. Start a Pomodoro timer from Tools!",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                } else {
                    focusSessions.take(3).forEach { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF090E1A))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${s.durationSeconds / 60}m focus • ${s.pauseCount} pauses",
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = "${(s.focusScore * 100).toInt()}% score",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// 👤 PROFILE & SETTINGS TAB COMPONENT
// ====================================================================
@Composable
fun ProfileTabView(
    user: User,
    userSettings: com.example.dle_prototype.data.UserSettings,
    dbHelper: DatabaseHelper,
    onSettingsUpdated: (com.example.dle_prototype.data.UserSettings) -> Unit,
    onOpenFocus: () -> Unit,
    onOpenTraining: () -> Unit,
    onOpenFederated: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenUxTest: () -> Unit,
    latencySnapshot: InferenceLatencySnapshot?,
    onClearCache: () -> Unit,
    onLogout: () -> Unit
) {
    var isDevModeExpanded by remember { mutableStateOf(false) }
    var showRewardsHub by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(CyanAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = userSettings.avatarEmoji.ifBlank { "👤" }, fontSize = 28.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userSettings.displayName.ifBlank { user.username },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "@${user.username} • Sync: SQLite Local",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    val memberSince = SimpleDateFormat("MMM yyyy", Locale.US).format(Date(user.createdAt))
                    Text(
                        text = "Member since $memberSince",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // 🎁 Rewards Hub & Hangar Shortcut
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1035),
            border = BorderStroke(1.dp, Color(0xFFA855F7)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showRewardsHub = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(24.dp))
                    Column {
                        Text("Rewards Hub & Hangar ⛽", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text("Mystery chests, rocket skins, and streak freezes", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    }
                }
                Text("Open →", color = Color(0xFFC084FC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Wellbeing & Personalisation
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Spa, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                    Text("WELLBEING & PACING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Daily Time Cap", color = Color(0xFFE2E8F0), style = MaterialTheme.typography.bodyMedium)
                        Text("Limit daily feed scrolling to 20m", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Text("20 min", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Quiet Hours (10pm - 7am)", color = Color(0xFFE2E8F0), style = MaterialTheme.typography.bodyMedium)
                        Text("No league notifications during rest", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Text("Enabled", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Soft-Stop Reminder", color = Color(0xFFE2E8F0), style = MaterialTheme.typography.bodyMedium)
                        Text("Suggests cognitive breaks every 10 cards", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Text("On", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Preferences Section
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "PREFERENCES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )

                // Haptic Feedback / Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Text("Haptic Feedback & Sound", color = Color(0xFFE2E8F0), style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = userSettings.hapticFeedbackEnabled,
                        onCheckedChange = { onSettingsUpdated(userSettings.copy(hapticFeedbackEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanAccent.copy(alpha = 0.3f))
                    )
                }

                // Daily Streak Reminders Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                        Text("Daily Streak Reminders", color = Color(0xFFE2E8F0), style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = userSettings.dailyReminderEnabled,
                        onCheckedChange = { onSettingsUpdated(userSettings.copy(dailyReminderEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AmberAccent, checkedTrackColor = AmberAccent.copy(alpha = 0.3f))
                    )
                }
            }
        }

        // Tools Section
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "TOOLS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )

                // Focus Session Shortcut
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF090E1A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFocus() }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Start Focus Session", fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                                Text("Full-screen circular timer with focus telemetry", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Text("Launch →", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 🔬 Advanced (collapsed, optional "Developer Mode" toggle)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDevModeExpanded = !isDevModeExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Text(
                            text = "🔬 Advanced & Developer Telemetry",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                    Icon(
                        imageVector = if (isDevModeExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8)
                    )
                }

                AnimatedVisibility(
                    visible = isDevModeExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Training Studio Quick Link
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF090E1A),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenTraining() }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.ModelTraining, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("On-Device Training Studio", fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC), fontSize = 13.sp)
                                        Text("Loss curves, backpropagation & checkpoints", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                            }
                        }

                        // Federated Learning Lab Quick Link
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF090E1A),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenFederated() }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Hub, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("Federated Learning Lab", fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC), fontSize = 13.sp)
                                        Text("CPU/RAM monitor, thermal & secure aggregation", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                            }
                        }

                        // Diagnostics & Sensor Inspector
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF090E1A),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDiagnostics() }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.BugReport, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("System Diagnostics & Database", fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC), fontSize = 13.sp)
                                        Text("SQLite table inspector, metric tooltips & sensors", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                            }
                        }

                        // Real-Time Inference Latency
                        latencySnapshot?.let { snap ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF090E1A),
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Speed, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                        Text("TFLite Latency: ${snap.totalLatencyMs}ms (${snap.modelSource})", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Privacy Note
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF090E1A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Spa, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                Text(
                    text = "🔒 Privacy: Your learning telemetry & models stay on this device.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        if (showRewardsHub) {
            RewardsHubSheet(
                username = user.username,
                dbHelper = dbHelper,
                onDismiss = { showRewardsHub = false }
            )
        }

        // Account Actions (Bottom, separated)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onClearCache,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("clear_cache_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RoseAccent.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = RoseAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear Local Cache", color = RoseAccent, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sign Out", color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
            }
        }
    }
}
