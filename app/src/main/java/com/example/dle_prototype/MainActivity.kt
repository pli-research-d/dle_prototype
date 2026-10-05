package com.example.dle_prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.User
import com.example.dle_prototype.notifications.DailyStreakReminderManager
import com.example.dle_prototype.ui.screens.AuthScreen
import com.example.dle_prototype.ui.screens.DashboardScreen
import com.example.dle_prototype.ui.screens.DiagnosticsScreen
import com.example.dle_prototype.ui.screens.FederatedLabScreen
import com.example.dle_prototype.ui.screens.FlashcardReviewScreen
import com.example.dle_prototype.ui.screens.FocusSessionScreen
import com.example.dle_prototype.ui.screens.QuizScreen
import com.example.dle_prototype.ui.screens.SettingsScreen
import com.example.dle_prototype.ui.screens.TrainingStudioScreen
import com.example.dle_prototype.ui.screens.TraitEvolutionScreen
import com.example.dle_prototype.ui.screens.UxTestViewScreen
import com.example.dle_prototype.ui.theme.DLETheme
import kotlinx.coroutines.launch

sealed class Screen {
    data object Auth : Screen()
    data object Dashboard : Screen()
    data class Quiz(val categoryName: String, val categoryNumber: Float, val initialTier: String? = null) : Screen()
    data object Diagnostics : Screen()
    data object Training : Screen()
    data object Flashcards : Screen()
    data object TraitEvolution : Screen()
    data object FocusSession : Screen()
    data object FederatedLab : Screen()
    data object UxTestView : Screen()
    data object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var sessionStartTime: Long = 0
    private var activeUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dbHelper = DatabaseHelper.getInstance(this)
        sessionStartTime = System.currentTimeMillis()

        // Initialize notification channel and restore scheduled streak alarms
        DailyStreakReminderManager.createNotificationChannel(this)
        if (DailyStreakReminderManager.isReminderEnabled(this)) {
            val (hour, minute) = DailyStreakReminderManager.getReminderTime(this)
            DailyStreakReminderManager.scheduleDailyReminder(this, hour, minute)
        }

        // Initialize and sync peak study session notifications based on progress data
        com.example.dle_prototype.notifications.PeakStudyNotificationManager.createNotificationChannel(this)
        com.example.dle_prototype.notifications.PeakStudyNotificationManager.syncAndSchedulePeakStudyAlert(this, dbHelper)

        val startQuizFromNotification = intent?.getBooleanExtra("EXTRA_START_QUIZ", false) ?: false

        setContent {
            val context = LocalContext.current
            var activeSettings by remember { mutableStateOf<com.example.dle_prototype.data.UserSettings?>(null) }

            DLETheme(userSettings = activeSettings) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DleApp(
                        dbHelper = dbHelper,
                        startQuizImmediately = startQuizFromNotification,
                        onUserChanged = { user ->
                            activeUsername = user?.username
                            activeSettings = if (user != null) {
                                com.example.dle_prototype.data.UserSettingsManager.loadSettings(context, user.username)
                            } else null
                        },
                        onThemeSettingsChanged = { updated ->
                            activeSettings = updated
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sessionStartTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        val username = activeUsername
        if (username != null) {
            val sessionMillis = System.currentTimeMillis() - sessionStartTime
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                dbHelper.recordSessionTime(username, sessionMillis)
            }
        }
    }
}

@Composable
fun DleApp(
    dbHelper: DatabaseHelper,
    startQuizImmediately: Boolean = false,
    onUserChanged: (User?) -> Unit,
    onThemeSettingsChanged: (com.example.dle_prototype.data.UserSettings) -> Unit = {}
) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Auth) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is Screen.Auth -> {
                AuthScreen(
                    dbHelper = dbHelper,
                    onLoginSuccess = { user ->
                        currentUser = user
                        onUserChanged(user)
                        currentScreen = if (startQuizImmediately) {
                            Screen.Quiz("General Science", 1f)
                        } else {
                            Screen.Dashboard
                        }
                    }
                )
            }
            is Screen.Dashboard -> {
                currentUser?.let { user ->
                    DashboardScreen(
                        user = user,
                        dbHelper = dbHelper,
                        onStartQuiz = { catName, catNum ->
                            currentScreen = Screen.Quiz(catName, catNum)
                        },
                        onStartPersonalizedQuiz = { catName, catNum, tier ->
                            currentScreen = Screen.Quiz(catName, catNum, tier)
                        },
                        onOpenDiagnostics = {
                            currentScreen = Screen.Diagnostics
                        },
                        onOpenTraining = {
                            currentScreen = Screen.Training
                        },
                        onOpenFlashcards = {
                            currentScreen = Screen.Flashcards
                        },
                        onOpenEvolution = {
                            currentScreen = Screen.TraitEvolution
                        },
                        onOpenFocus = {
                            currentScreen = Screen.FocusSession
                        },
                        onOpenFederated = {
                            currentScreen = Screen.FederatedLab
                        },
                        onOpenUxTest = {
                            currentScreen = Screen.UxTestView
                        },
                        onOpenSettings = {
                            currentScreen = Screen.Settings
                        },
                        onThemeSettingsChanged = onThemeSettingsChanged,
                        onLogout = {
                            currentUser = null
                            onUserChanged(null)
                            currentScreen = Screen.Auth
                        }
                    )
                } ?: run {
                    currentScreen = Screen.Auth
                }
            }
            is Screen.Quiz -> {
                currentUser?.let { user ->
                    QuizScreen(
                        user = user,
                        categoryName = screen.categoryName,
                        categoryNumber = screen.categoryNumber,
                        dbHelper = dbHelper,
                        initialTier = screen.initialTier,
                        onQuizCompleted = {
                            currentScreen = Screen.Dashboard
                        },
                        onBack = {
                            currentScreen = Screen.Dashboard
                        }
                    )
                } ?: run {
                    currentScreen = Screen.Auth
                }
            }
            is Screen.Diagnostics -> {
                DiagnosticsScreen(
                    dbHelper = dbHelper,
                    user = currentUser,
                    onBack = {
                        currentScreen = if (currentUser != null) Screen.Dashboard else Screen.Auth
                    }
                )
            }
            is Screen.Training -> {
                currentUser?.let { user ->
                    TrainingStudioScreen(
                        user = user,
                        dbHelper = dbHelper,
                        onBack = {
                            currentScreen = Screen.Dashboard
                        }
                    )
                } ?: run {
                    currentScreen = Screen.Auth
                }
            }
            is Screen.Flashcards -> {
                currentUser?.let { user ->
                    FlashcardReviewScreen(
                        user = user,
                        dbHelper = dbHelper,
                        onBack = {
                            currentScreen = Screen.Dashboard
                        }
                    )
                } ?: run {
                    currentScreen = Screen.Auth
                }
            }
            is Screen.TraitEvolution -> {
                currentUser?.let { user ->
                    TraitEvolutionScreen(
                        user = user,
                        dbHelper = dbHelper,
                        onBack = {
                            currentScreen = Screen.Dashboard
                        }
                    )
                } ?: run {
                    currentScreen = Screen.Auth
                }
            }
            is Screen.FocusSession -> {
                currentUser?.let { user ->
                    FocusSessionScreen(
                        user = user,
                        dbHelper = dbHelper,
                        onBack = {
                            currentScreen = Screen.Dashboard
                        }
                    )
                } ?: run {
                    currentScreen = Screen.Auth
                }
            }
            is Screen.FederatedLab -> {
                currentUser?.let { user ->
                    FederatedLabScreen(
                        user = user,
                        dbHelper = dbHelper,
                        onBack = {
                            currentScreen = Screen.Dashboard
                        }
                    )
                } ?: run {
                    currentScreen = Screen.Auth
                }
            }
            is Screen.UxTestView -> {
                UxTestViewScreen(
                    onBack = {
                        currentScreen = Screen.Dashboard
                    }
                )
            }
            is Screen.Settings -> {
                currentUser?.let { user ->
                    SettingsScreen(
                        user = user,
                        onBack = {
                            currentScreen = Screen.Dashboard
                        }
                    )
                } ?: run {
                    currentScreen = Screen.Auth
                }
            }
        }
    }
}
