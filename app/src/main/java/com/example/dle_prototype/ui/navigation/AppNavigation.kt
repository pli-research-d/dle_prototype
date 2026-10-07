package com.example.dle_prototype.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.LeaderboardEntry
import com.example.dle_prototype.data.LeaderboardScope
import com.example.dle_prototype.data.LeaderboardSort
import com.example.dle_prototype.data.PersonalizationProfile
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.QuizPerformanceStats
import com.example.dle_prototype.data.StudySessionRecord
import com.example.dle_prototype.data.TFLiteEngine
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.UserSettingsManager
import com.example.dle_prototype.data.ml.InferenceLatencySnapshot
import com.example.dle_prototype.data.ml.InferenceLatencyTracker
import com.example.dle_prototype.ui.screens.CompeteScreen
import com.example.dle_prototype.ui.screens.FeedScreen
import com.example.dle_prototype.ui.screens.LearnTabView
import com.example.dle_prototype.ui.screens.ProfileTabView
import com.example.dle_prototype.ui.screens.ProgressTabView
import com.example.dle_prototype.ui.theme.CyanAccent
import kotlinx.coroutines.launch

/**
 * Navigation routes for the 5 primary tabs:
 * 1. Feed: Endless vertical swipe micro-learning hub
 * 2. Practice: Adaptive quizzes, flashcards, focus sessions
 * 3. Compete: Daily duels, peer leagues, leaderboard
 * 4. Progress: Learner analytics, retention graphs, history
 * 5. Profile: User account, cognitive traits, settings
 */
sealed class AppRoute(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    data object Feed : AppRoute("feed", "Feed", Icons.Default.RocketLaunch, "nav_tab_Feed")
    data object Practice : AppRoute("practice", "Practice", Icons.Default.School, "nav_tab_Practice")
    data object Compete : AppRoute("compete", "Compete", Icons.Default.SportsKabaddi, "nav_tab_Compete")
    data object Progress : AppRoute("progress", "Progress", Icons.AutoMirrored.Filled.TrendingUp, "nav_tab_Progress")
    data object Profile : AppRoute("profile", "Profile", Icons.Default.Person, "nav_tab_Profile")

    companion object {
        const val FEED_ROUTE = "feed"
        const val PRACTICE_ROUTE = "practice"
        const val COMPETE_ROUTE = "compete"
        const val PROGRESS_ROUTE = "progress"
        const val PROFILE_ROUTE = "profile"

        val items = listOf(Feed, Practice, Compete, Progress, Profile)

        fun fromRoute(route: String?): AppRoute = when (route) {
            FEED_ROUTE -> Feed
            PRACTICE_ROUTE -> Practice
            COMPETE_ROUTE -> Compete
            PROGRESS_ROUTE -> Progress
            PROFILE_ROUTE -> Profile
            else -> Feed
        }

        fun getIndex(route: String?): Int = when (route) {
            FEED_ROUTE -> 0
            PRACTICE_ROUTE -> 1
            COMPETE_ROUTE -> 2
            PROGRESS_ROUTE -> 3
            PROFILE_ROUTE -> 4
            else -> 0
        }
    }
}

/**
 * Persistent Bottom Navigation Bar maintaining saved backstack states across tabs.
 */
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    items: List<AppRoute> = AppRoute.items
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoute.FEED_ROUTE

    NavigationBar(
        modifier = modifier.testTag("main_bottom_navigation"),
        containerColor = Color(0xFF0F172A),
        tonalElevation = 8.dp
    ) {
        items.forEach { tab ->
            val isSelected = currentRoute == tab.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            // Pop up to the start destination to prevent unbounded stack growth
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when reselecting
                            launchSingleTop = true
                            // Restore state when returning to a previously selected tab
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                alwaysShowLabel = true,
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyanAccent,
                    selectedTextColor = CyanAccent,
                    indicatorColor = CyanAccent.copy(alpha = 0.15f),
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}

/**
 * Main NavHost implementing routes for Feed, Practice, Compete, Progress, and Profile,
 * integrated with a persistent BottomNavigationBar.
 */
@Composable
fun AppNavigation(
    user: User,
    dbHelper: DatabaseHelper,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onStartQuiz: (categoryName: String, categoryNumber: Float) -> Unit = { _, _ -> },
    onStartPersonalizedQuiz: ((categoryName: String, categoryNumber: Float, initialTier: String) -> Unit)? = null,
    onOpenDiagnostics: () -> Unit = {},
    onOpenTraining: () -> Unit = {},
    onOpenFlashcards: () -> Unit = {},
    onOpenEvolution: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenFederated: () -> Unit = {},
    onOpenUxTest: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenQaChat: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var userSettings by remember { mutableStateOf(UserSettingsManager.loadSettings(context, user.username)) }
    var profile by remember { mutableStateOf<PersonalizationProfile?>(null) }
    var recentAttempts by remember { mutableStateOf<List<QuizAttempt>>(emptyList()) }
    var allHistoryAttempts by remember { mutableStateOf<List<QuizAttempt>>(emptyList()) }
    var quizPerformanceStats by remember { mutableStateOf(QuizPerformanceStats()) }
    var dailyStreak by remember { mutableIntStateOf(0) }
    var longestStreak by remember { mutableIntStateOf(0) }
    var dueCardsCount by remember { mutableIntStateOf(0) }
    var focusSessions by remember { mutableStateOf<List<StudySessionRecord>>(emptyList()) }
    var leaderboardEntries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var leaderboardScope by remember { mutableStateOf(LeaderboardScope.GLOBAL) }
    var leaderboardSort by remember { mutableStateOf(LeaderboardSort.STREAK) }
    var latencySnapshot by remember { mutableStateOf<InferenceLatencySnapshot?>(null) }

    LaunchedEffect(user.username) {
        val tele = dbHelper.getTelemetry(user.username)
        val weights = dbHelper.loadModelWeights(user.username)
        profile = TFLiteEngine.runInference(context, tele, weights)
        recentAttempts = dbHelper.getRecentQuizAttempts(user.username, 5)
        allHistoryAttempts = dbHelper.getAllQuizHistory(user.username, 50)
        quizPerformanceStats = dbHelper.getQuizPerformanceStats(user.username)
        val streakData = dbHelper.getUserStreakData(user.username)
        dailyStreak = streakData.currentStreak
        longestStreak = streakData.longestStreak
        dueCardsCount = dbHelper.getDueFlashcards(user.username).size
        focusSessions = dbHelper.getStudySessions(user.username, 10)
        leaderboardEntries = dbHelper.getLeaderboardEntries(user.username, leaderboardScope, leaderboardSort)
        latencySnapshot = InferenceLatencyTracker.profileInference(
            context = context,
            telemetry = tele,
            customWeights = weights
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Feed.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                val initialIndex = AppRoute.getIndex(initialState.destination.route)
                val targetIndex = AppRoute.getIndex(targetState.destination.route)
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
                val initialIndex = AppRoute.getIndex(initialState.destination.route)
                val targetIndex = AppRoute.getIndex(targetState.destination.route)
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
                val initialIndex = AppRoute.getIndex(initialState.destination.route)
                val targetIndex = AppRoute.getIndex(targetState.destination.route)
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
                val initialIndex = AppRoute.getIndex(initialState.destination.route)
                val targetIndex = AppRoute.getIndex(targetState.destination.route)
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
            // 1. Route: Feed
            composable(AppRoute.Feed.route) {
                FeedScreen(
                    user = user,
                    dbHelper = dbHelper,
                    onOpenCompete = {
                        navController.navigate(AppRoute.Compete.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 2. Route: Practice
            composable(AppRoute.Practice.route) {
                LearnTabView(
                    user = user,
                    dbHelper = dbHelper,
                    recentAttempts = recentAttempts,
                    dueCardsCount = dueCardsCount,
                    onStartQuiz = onStartQuiz,
                    onOpenFocus = onOpenFocus,
                    onOpenQaChat = onOpenQaChat
                )
            }

            // 3. Route: Compete
            composable(AppRoute.Compete.route) {
                CompeteScreen(
                    user = user,
                    dbHelper = dbHelper,
                    onOpenFeed = {
                        navController.navigate(AppRoute.Feed.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 4. Route: Progress
            composable(AppRoute.Progress.route) {
                ProgressTabView(
                    user = user,
                    dbHelper = dbHelper,
                    profile = profile,
                    dailyStreak = dailyStreak,
                    longestStreak = longestStreak,
                    totalQuizzes = quizPerformanceStats.totalQuizzes,
                    quizPerformanceStats = quizPerformanceStats,
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

            // 5. Route: Profile
            composable(AppRoute.Profile.route) {
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
                    onClearCache = {},
                    onLogout = onLogout,
                    onOpenQaChat = onOpenQaChat
                )
            }
        }
    }
}
