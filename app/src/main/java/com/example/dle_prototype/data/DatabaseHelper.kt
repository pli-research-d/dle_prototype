package com.example.dle_prototype.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.dle_prototype.data.ml.LogCategory
import com.example.dle_prototype.data.ml.LogLevel
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.TrainingCheckpoint
import com.example.dle_prototype.data.ml.TrainingLogEntry
import com.example.dle_prototype.data.ml.TrainingSample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.dle_prototype.data.ml.AdaptiveDifficultyProfile
import com.example.dle_prototype.data.qa.QaChatMessage

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    private val appContext: Context = context.applicationContext

    companion object {
        const val DATABASE_NAME = "app.db"
        const val DATABASE_VERSION = 4

        // Table: user_performance
        const val TABLE_USER_PERFORMANCE = "user_performance"
        const val COL_PERF_ID = "id"
        const val COL_PERF_USERNAME = "username"
        const val COL_PERF_QUIZ_SCORE = "quiz_score"
        const val COL_PERF_TIME_SPENT = "time_spent"
        const val COL_PERF_LOGIN_FREQUENCY = "login_frequency"
        const val COL_PERF_DIFFICULTY_REACHED = "difficulty_reached"
        const val COL_PERF_CATEGORY_SELECTED = "category_selected"
        const val COL_PERF_RETENTION_RATE = "retention_rate"
        const val COL_PERF_LEARNING_VELOCITY = "learning_velocity"
        const val COL_PERF_LAST_UPDATED = "last_updated"

        // Table: quiz_history
        const val TABLE_QUIZ_HISTORY = "quiz_history"
        const val COL_HISTORY_ID = "id"
        const val COL_HISTORY_USERNAME = "username"
        const val COL_HISTORY_CATEGORY = "category"
        const val COL_HISTORY_SCORE = "score"
        const val COL_HISTORY_TOTAL_QUESTIONS = "total_questions"
        const val COL_HISTORY_ACCURACY_PERCENT = "accuracy_percent"
        const val COL_HISTORY_ELAPSED_TIME = "elapsed_time_seconds"
        const val COL_HISTORY_MISSED_INDICES = "missed_questions_indices"
        const val COL_HISTORY_DIFFICULTY = "difficulty_level"
        const val COL_HISTORY_TIMESTAMP = "timestamp"

        // Table: streak_data
        const val TABLE_STREAK_DATA = "streak_data"
        const val COL_STREAK_USERNAME = "username"
        const val COL_STREAK_CURRENT = "current_streak"
        const val COL_STREAK_LONGEST = "longest_streak"
        const val COL_STREAK_LAST_ACTIVE = "last_active_date"
        const val COL_STREAK_TOTAL_DAYS = "total_days_active"
        const val COL_STREAK_LAST_MILESTONE = "last_milestone_reached"
        const val COL_STREAK_UPDATED_AT = "updated_at"

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        data class VirtualPeerTemplate(
            val username: String,
            val displayName: String,
            val avatarEmoji: String,
            val dominantTrait: String,
            val tierLevel: String,
            val baseStreak: Int,
            val baseQuizzes: Int,
            val baseScore: Int,
            val baseAccuracy: Float,
            val consistency: Float
        )

        val VIRTUAL_PEER_TEMPLATES = listOf(
            // Diamond Tier (Elite, top competitors)
            VirtualPeerTemplate("martiantiger67", "MartianTiger67", "🐯", "Consistent", "Diamond", 24, 82, 580, 97.5f, 0.95f),
            VirtualPeerTemplate("snowyowl3", "SnowyOwl3", "🦉", "Deep Thinker", "Diamond", 21, 74, 530, 96.2f, 0.92f),
            VirtualPeerTemplate("cosmicfalcon42", "CosmicFalcon42", "🦅", "Speed Demon", "Diamond", 18, 68, 485, 95.0f, 0.90f),
            VirtualPeerTemplate("cyberotter19", "CyberOtter19", "🦦", "Algorithm Master", "Diamond", 16, 62, 440, 94.5f, 0.88f),

            // Platinum Tier (High performers)
            VirtualPeerTemplate("neonpanda88", "NeonPanda88", "🐼", "Conscientious", "Platinum", 14, 55, 395, 93.0f, 0.85f),
            VirtualPeerTemplate("solarhawk55", "SolarHawk55", "⚡", "Streak Hunter", "Platinum", 12, 48, 350, 91.8f, 0.82f),
            VirtualPeerTemplate("lunarlynx77", "LunarLynx77", "🐱", "Understanding", "Platinum", 10, 42, 315, 90.5f, 0.80f),
            VirtualPeerTemplate("astralfox91", "AstralFox91", "🦊", "Night Owl", "Platinum", 9, 38, 280, 89.2f, 0.78f),

            // Gold Tier (Steady everyday learners)
            VirtualPeerTemplate("quantumbear23", "QuantumBear23", "🐻", "Early Bird", "Gold", 8, 33, 245, 88.0f, 0.75f),
            VirtualPeerTemplate("stellarwolf14", "StellarWolf14", "🐺", "Bug Hunter", "Gold", 7, 29, 215, 86.8f, 0.72f),
            VirtualPeerTemplate("mysticraven12", "MysticRaven12", "🔮", "Motivation", "Gold", 6, 25, 190, 85.5f, 0.70f),
            VirtualPeerTemplate("crimsoncheetah34", "CrimsonCheetah34", "🐆", "Creative Coder", "Gold", 5, 22, 165, 84.2f, 0.68f),
            VirtualPeerTemplate("pixeldragon8", "PixelDragon8", "🐉", "Steadfast", "Gold", 4, 19, 145, 83.0f, 0.65f),
            VirtualPeerTemplate("shadowgecko61", "ShadowGecko61", "🦎", "Engagement", "Gold", 4, 16, 128, 82.0f, 0.62f),

            // Silver & Bronze Tier (Casual / Emerging learners)
            VirtualPeerTemplate("turbohedgehog5", "TurboHedgehog5", "🦔", "Explorer", "Silver", 3, 13, 105, 80.5f, 0.58f),
            VirtualPeerTemplate("apexkoala18", "ApexKoala18", "🐨", "Rising Star", "Silver", 2, 10, 82, 79.0f, 0.55f),
            VirtualPeerTemplate("hyperbadger7", "HyperBadger7", "🦡", "New Joiner", "Silver", 2, 7, 60, 77.5f, 0.50f),
            VirtualPeerTemplate("echodolphin29", "EchoDolphin29", "🐬", "Weekend Warrior", "Bronze", 1, 5, 42, 75.0f, 0.45f)
        )

        data class GeneratedCommunityPeer(
            val username: String,
            val displayName: String,
            val avatarEmoji: String,
            val dailyStreak: Int,
            val totalQuizzes: Int,
            val totalScore: Int,
            val accuracyPercent: Float,
            val dominantTrait: String,
            val tier: String
        ) {
            fun toContentValues(): ContentValues = ContentValues().apply {
                put("username", username)
                put("display_name", displayName)
                put("avatar_emoji", avatarEmoji)
                put("daily_streak", dailyStreak)
                put("total_quizzes", totalQuizzes)
                put("total_score", totalScore)
                put("accuracy_percent", accuracyPercent)
                put("dominant_trait", dominantTrait)
                put("tier", tier)
            }
        }

        fun generateDailyCommunityLearners(calendar: Calendar = Calendar.getInstance()): List<GeneratedCommunityPeer> {
            val epochDay = (calendar.timeInMillis / (1000L * 60 * 60 * 24)).toInt()
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val dayProgressRatio = ((hourOfDay + 1).toFloat() / 24f).coerceIn(0.25f, 1.0f)

            return VIRTUAL_PEER_TEMPLATES.map { template ->
                val peerSeed = (template.username.hashCode().toLong() * 373587883L) xor (epochDay.toLong() * 1000000007L)
                val rng = java.util.Random(peerSeed)

                // 1. Daily activity variation
                val studiedToday = rng.nextFloat() < template.consistency
                val targetQuizzesToday = if (studiedToday) {
                    when (template.tierLevel) {
                        "Diamond" -> rng.nextInt(3) + 3
                        "Platinum" -> rng.nextInt(3) + 2
                        "Gold" -> rng.nextInt(3) + 1
                        else -> rng.nextInt(2) + 1
                    }
                } else 0
                val todayQuizzes = (targetQuizzesToday * dayProgressRatio).toInt().coerceAtLeast(
                    if (studiedToday && hourOfDay >= 12) 1 else 0
                )
                val todayXp = todayQuizzes * (12 + rng.nextInt(6))

                // 2. Realistic streak simulation
                val currentStreak = when (template.tierLevel) {
                    "Diamond" -> template.baseStreak + (epochDay % 14)
                    "Platinum" -> template.baseStreak + (epochDay % 10)
                    "Gold" -> {
                        val cycle = Math.abs((epochDay + template.username.hashCode()) % 15)
                        (cycle + template.baseStreak / 2).coerceAtLeast(2)
                    }
                    else -> {
                        val cycle = Math.abs((epochDay + template.username.hashCode()) % 7)
                        (cycle % 4) + 1
                    }
                }

                // 3. Cumulative quizzes & score reflecting progression
                val seasonDay = Math.abs(epochDay % 30)
                val historicalQuizzes = template.baseQuizzes + (seasonDay * (template.baseQuizzes / 20).coerceAtLeast(1))
                val totalQuizzes = historicalQuizzes + todayQuizzes
                val totalScore = template.baseScore + (seasonDay * (template.baseScore / 25).coerceAtLeast(5)) + todayXp
                val accuracy = (template.baseAccuracy + (rng.nextFloat() * 2.2f - 1.1f)).coerceIn(72.0f, 99.8f)
                val roundedAccuracy = Math.round(accuracy * 10.0f) / 10.0f

                GeneratedCommunityPeer(
                    username = template.username,
                    displayName = template.displayName,
                    avatarEmoji = template.avatarEmoji,
                    dailyStreak = currentStreak,
                    totalQuizzes = totalQuizzes,
                    totalScore = totalScore,
                    accuracyPercent = roundedAccuracy,
                    dominantTrait = template.dominantTrait,
                    tier = template.tierLevel
                )
            }
        }

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseHelper(context).also { INSTANCE = it }
            }
        }

        private fun hashPassword(password: String, salt: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val input = "$salt:$password".toByteArray(Charsets.UTF_8)
            val hashBytes = digest.digest(input)
            return hashBytes.joinToString("") { "%02x".format(it) }
        }

        private fun generateSalt(): String {
            val random = SecureRandom()
            val saltBytes = ByteArray(16)
            random.nextBytes(saltBytes)
            return saltBytes.joinToString("") { "%02x".format(it) }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                salt TEXT,
                created_at INTEGER
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS dle_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                quiz_score REAL DEFAULT 0,
                time_spent REAL DEFAULT 0,
                login_frequency REAL DEFAULT 0,
                difficulty_reached REAL DEFAULT 0,
                category_selected REAL DEFAULT 1,
                last_updated INTEGER
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS quiz_attempts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                category TEXT NOT NULL,
                score INTEGER NOT NULL,
                total_questions INTEGER NOT NULL,
                difficulty_level REAL NOT NULL,
                timestamp INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_quiz_attempts_user_time ON quiz_attempts(username, timestamp DESC);")

        // 1. Table for UserPerformance (learning metrics, velocity, retention)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_USER_PERFORMANCE (
                $COL_PERF_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PERF_USERNAME TEXT NOT NULL,
                $COL_PERF_QUIZ_SCORE REAL DEFAULT 0,
                $COL_PERF_TIME_SPENT REAL DEFAULT 0,
                $COL_PERF_LOGIN_FREQUENCY REAL DEFAULT 0,
                $COL_PERF_DIFFICULTY_REACHED REAL DEFAULT 0,
                $COL_PERF_CATEGORY_SELECTED REAL DEFAULT 1,
                $COL_PERF_RETENTION_RATE REAL DEFAULT 0.85,
                $COL_PERF_LEARNING_VELOCITY REAL DEFAULT 1.0,
                $COL_PERF_LAST_UPDATED INTEGER
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_performance_user ON $TABLE_USER_PERFORMANCE($COL_PERF_USERNAME);")

        // 2. Table for QuizHistory (detailed quiz logs, scores, timing, missed indices)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_QUIZ_HISTORY (
                $COL_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_HISTORY_USERNAME TEXT NOT NULL,
                $COL_HISTORY_CATEGORY TEXT NOT NULL,
                $COL_HISTORY_SCORE INTEGER NOT NULL,
                $COL_HISTORY_TOTAL_QUESTIONS INTEGER NOT NULL,
                $COL_HISTORY_ACCURACY_PERCENT REAL NOT NULL DEFAULT 0.0,
                $COL_HISTORY_ELAPSED_TIME INTEGER NOT NULL DEFAULT 0,
                $COL_HISTORY_MISSED_INDICES TEXT NOT NULL DEFAULT '[]',
                $COL_HISTORY_DIFFICULTY REAL NOT NULL DEFAULT 1.0,
                $COL_HISTORY_TIMESTAMP INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_quiz_history_user_time ON $TABLE_QUIZ_HISTORY($COL_HISTORY_USERNAME, $COL_HISTORY_TIMESTAMP DESC);")

        // 3. Table for StreakData (daily streaks, records, milestones)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_STREAK_DATA (
                $COL_STREAK_USERNAME TEXT PRIMARY KEY,
                $COL_STREAK_CURRENT INTEGER NOT NULL DEFAULT 1,
                $COL_STREAK_LONGEST INTEGER NOT NULL DEFAULT 1,
                $COL_STREAK_LAST_ACTIVE TEXT NOT NULL,
                $COL_STREAK_TOTAL_DAYS INTEGER NOT NULL DEFAULT 1,
                $COL_STREAK_LAST_MILESTONE INTEGER NOT NULL DEFAULT 0,
                $COL_STREAK_UPDATED_AT INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS trained_weights (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                weights_json TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            );
            """.trimIndent()
        )

        createExtendedTables(db)
    }

    private fun createExtendedTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS trait_snapshots (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                conscientiousness REAL NOT NULL,
                motivation REAL NOT NULL,
                understanding REAL NOT NULL,
                engagement REAL NOT NULL,
                primary_strength TEXT NOT NULL,
                focus_area TEXT NOT NULL,
                model_source TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS flashcards (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                category TEXT NOT NULL,
                question TEXT NOT NULL,
                options_json TEXT NOT NULL,
                correct_answer TEXT NOT NULL,
                explanation TEXT NOT NULL,
                box_level INTEGER DEFAULT 1,
                next_review_time INTEGER NOT NULL,
                review_count INTEGER DEFAULT 0,
                last_reviewed INTEGER DEFAULT 0
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS study_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                duration_seconds INTEGER NOT NULL,
                target_minutes INTEGER NOT NULL,
                pause_count INTEGER NOT NULL,
                focus_score REAL NOT NULL,
                timestamp INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_unlocked_badges (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                badge_id TEXT NOT NULL,
                unlocked_at INTEGER NOT NULL,
                xp_awarded INTEGER NOT NULL,
                UNIQUE(username, badge_id)
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS qa_chat_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                sender TEXT NOT NULL,
                message TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS training_checkpoints (
                username TEXT PRIMARY KEY,
                session_id TEXT NOT NULL,
                current_epoch INTEGER NOT NULL,
                target_epochs INTEGER NOT NULL,
                current_loss REAL NOT NULL,
                loss_history_json TEXT NOT NULL,
                weights_json TEXT NOT NULL,
                saved_at INTEGER NOT NULL,
                is_completed INTEGER NOT NULL DEFAULT 0
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS training_checkpoints_v2 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                session_id TEXT NOT NULL,
                checkpoint_name TEXT NOT NULL,
                current_epoch INTEGER NOT NULL,
                target_epochs INTEGER NOT NULL,
                current_loss REAL NOT NULL,
                accuracy_pct REAL NOT NULL DEFAULT 0.0,
                loss_history_json TEXT NOT NULL,
                weights_json TEXT NOT NULL,
                saved_at INTEGER NOT NULL,
                is_completed INTEGER NOT NULL DEFAULT 0,
                is_best INTEGER NOT NULL DEFAULT 0,
                trigger_type TEXT NOT NULL DEFAULT 'PERIODIC'
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS training_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                session_id TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                log_level TEXT NOT NULL,
                category TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                details_json TEXT NOT NULL DEFAULT '{}'
            );
            """.trimIndent()
        )

        createLeaderboardTables(db)
    }

    private fun createLeaderboardTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS friends (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                owner_username TEXT NOT NULL,
                friend_username TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                UNIQUE(owner_username, friend_username)
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS community_learners (
                username TEXT PRIMARY KEY,
                display_name TEXT NOT NULL,
                avatar_emoji TEXT NOT NULL,
                daily_streak INTEGER NOT NULL DEFAULT 1,
                total_quizzes INTEGER NOT NULL DEFAULT 5,
                total_score INTEGER NOT NULL DEFAULT 40,
                accuracy_percent REAL NOT NULL DEFAULT 85.0,
                dominant_trait TEXT NOT NULL DEFAULT 'Conscientious',
                tier TEXT NOT NULL DEFAULT 'Gold'
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS daily_goals (
                username TEXT PRIMARY KEY,
                target_questions INTEGER NOT NULL DEFAULT 10,
                target_hours REAL NOT NULL DEFAULT 1.0,
                updated_at INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_performance (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                quiz_score REAL DEFAULT 0,
                time_spent REAL DEFAULT 0,
                login_frequency REAL DEFAULT 0,
                difficulty_reached REAL DEFAULT 0,
                category_selected REAL DEFAULT 1,
                retention_rate REAL DEFAULT 0.85,
                learning_velocity REAL DEFAULT 1.0,
                last_updated INTEGER
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_performance_user ON user_performance(username);")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS quiz_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                category TEXT NOT NULL,
                score INTEGER NOT NULL,
                total_questions INTEGER NOT NULL,
                accuracy_percent REAL NOT NULL DEFAULT 0.0,
                elapsed_time_seconds INTEGER NOT NULL DEFAULT 0,
                missed_questions_indices TEXT NOT NULL DEFAULT '[]',
                difficulty_level REAL NOT NULL DEFAULT 1.0,
                timestamp INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_quiz_history_user_time ON quiz_history(username, timestamp DESC);")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS streak_data (
                username TEXT PRIMARY KEY,
                current_streak INTEGER NOT NULL DEFAULT 1,
                longest_streak INTEGER NOT NULL DEFAULT 1,
                last_active_date TEXT NOT NULL,
                total_days_active INTEGER NOT NULL DEFAULT 1,
                last_milestone_reached INTEGER NOT NULL DEFAULT 0,
                updated_at INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_streaks (
                username TEXT PRIMARY KEY,
                current_streak INTEGER NOT NULL DEFAULT 1,
                longest_streak INTEGER NOT NULL DEFAULT 1,
                last_active_date TEXT NOT NULL,
                total_days_active INTEGER NOT NULL DEFAULT 1,
                last_milestone_reached INTEGER NOT NULL DEFAULT 0,
                updated_at INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_gamification (
                username TEXT PRIMARY KEY,
                fuel INTEGER NOT NULL DEFAULT 85,
                total_xp INTEGER NOT NULL DEFAULT 340,
                streak_freezes INTEGER NOT NULL DEFAULT 1,
                altitude_km REAL NOT NULL DEFAULT 2.4,
                selected_skin TEXT NOT NULL DEFAULT 'Apollo Standard',
                selected_theme TEXT NOT NULL DEFAULT 'Deep Space',
                selected_frame TEXT NOT NULL DEFAULT 'Bronze Orbit',
                onboarding_completed INTEGER NOT NULL DEFAULT 1,
                selected_topics TEXT NOT NULL DEFAULT 'HTML,CSS,JavaScript,Python',
                selected_skill_level TEXT NOT NULL DEFAULT 'Surprise me',
                daily_goal_questions INTEGER NOT NULL DEFAULT 10,
                current_league TEXT NOT NULL DEFAULT 'Silver',
                league_rank INTEGER NOT NULL DEFAULT 5,
                updated_at INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS chests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                chest_type TEXT NOT NULL,
                is_opened INTEGER NOT NULL DEFAULT 0,
                reward_xp INTEGER NOT NULL DEFAULT 50,
                reward_fuel INTEGER NOT NULL DEFAULT 20,
                reward_badge TEXT NOT NULL DEFAULT '',
                reward_skin TEXT NOT NULL DEFAULT '',
                created_at INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS reward_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                fuel_change INTEGER NOT NULL DEFAULT 0,
                xp_change INTEGER NOT NULL DEFAULT 0,
                timestamp INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_duels (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                opponent_name TEXT NOT NULL,
                opponent_avatar TEXT NOT NULL,
                opponent_tier TEXT NOT NULL DEFAULT 'Gold',
                category TEXT NOT NULL,
                user_score INTEGER NOT NULL,
                opponent_score INTEGER NOT NULL,
                is_win INTEGER NOT NULL,
                xp_earned INTEGER NOT NULL,
                fuel_earned INTEGER NOT NULL,
                timestamp INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS wellbeing_settings (
                username TEXT PRIMARY KEY,
                daily_time_cap_minutes INTEGER NOT NULL DEFAULT 0,
                quiet_hours_enabled INTEGER NOT NULL DEFAULT 0,
                quiet_start_hour INTEGER NOT NULL DEFAULT 22,
                quiet_end_hour INTEGER NOT NULL DEFAULT 7,
                soft_stop_reminder_enabled INTEGER NOT NULL DEFAULT 1,
                sound_enabled INTEGER NOT NULL DEFAULT 1,
                haptics_enabled INTEGER NOT NULL DEFAULT 1,
                learning_style TEXT NOT NULL DEFAULT 'Adaptive Dynamic'
            );
            """.trimIndent()
        )
        try {
            var hasTargetHours = false
            db.rawQuery("PRAGMA table_info(daily_goals);", null).use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIndex != -1 && cursor.getString(nameIndex).equals("target_hours", ignoreCase = true)) {
                        hasTargetHours = true
                        break
                    }
                }
            }
            if (!hasTargetHours) {
                db.execSQL("ALTER TABLE daily_goals ADD COLUMN target_hours REAL NOT NULL DEFAULT 1.0;")
            }
        } catch (_: Exception) {}
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE users ADD COLUMN salt TEXT;")
                db.execSQL("ALTER TABLE users ADD COLUMN created_at INTEGER;")
            } catch (_: Exception) {}

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS quiz_attempts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    category TEXT NOT NULL,
                    score INTEGER NOT NULL,
                    total_questions INTEGER NOT NULL,
                    difficulty_level REAL NOT NULL,
                    timestamp INTEGER NOT NULL
                );
                """.trimIndent()
            )
        }
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS trained_weights (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                weights_json TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            );
            """.trimIndent()
        )
        createExtendedTables(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        createLeaderboardTables(db)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS trained_weights (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                weights_json TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS quiz_attempts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                category TEXT NOT NULL,
                score INTEGER NOT NULL,
                total_questions INTEGER NOT NULL,
                difficulty_level REAL NOT NULL,
                timestamp INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_quiz_attempts_user_time ON quiz_attempts(username, timestamp DESC);")
        createExtendedTables(db)
    }

    suspend fun registerUser(username: String, plainPassword: String): Result<User> = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        if (cleanUser.length < 2) {
            return@withContext Result.failure(IllegalArgumentException("Username must be at least 2 characters."))
        }
        if (plainPassword.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 3 characters."))
        }

        val salt = generateSalt()
        val hashed = hashPassword(plainPassword, salt)
        val now = System.currentTimeMillis()

        val values = ContentValues().apply {
            put("username", cleanUser)
            put("password", hashed)
            put("salt", salt)
            put("created_at", now)
        }

        try {
            val id = writableDatabase.insertOrThrow("users", null, values)
            // Initialize telemetry row
            val initialMetrics = ContentValues().apply {
                put("username", cleanUser)
                put("login_frequency", 1.0)
                put("time_spent", 0.0)
                put("quiz_score", 0.0)
                put("difficulty_reached", 1.0)
                put("category_selected", 1.0)
                put("last_updated", now)
            }
            writableDatabase.insertWithOnConflict("dle_data", null, initialMetrics, SQLiteDatabase.CONFLICT_REPLACE)

            Result.success(User(id, cleanUser, now))
        } catch (e: Exception) {
            Result.failure(Exception("Username already exists or database error: ${e.message}"))
        }
    }

    suspend fun loginUser(username: String, plainPassword: String): Result<User> = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        val cursor: Cursor = readableDatabase.rawQuery(
            "SELECT id, username, password, salt, created_at FROM users WHERE username = ?",
            arrayOf(cleanUser)
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                val id = c.getLong(0)
                val user = c.getString(1)
                val storedHash = c.getString(2)
                val salt = c.getString(3)
                val createdAt = if (!c.isNull(4)) c.getLong(4) else System.currentTimeMillis()

                val matches = if (salt.isNullOrEmpty()) {
                    // Legacy plain text check & auto-upgrade
                    if (storedHash == plainPassword) {
                        val newSalt = generateSalt()
                        val newHash = hashPassword(plainPassword, newSalt)
                        val updateVal = ContentValues().apply {
                            put("password", newHash)
                            put("salt", newSalt)
                        }
                        writableDatabase.update("users", updateVal, "id = ?", arrayOf(id.toString()))
                        true
                    } else false
                } else {
                    hashPassword(plainPassword, salt) == storedHash
                }

                if (matches) {
                    recordLoginSync(cleanUser)
                    return@withContext Result.success(User(id, user, createdAt))
                }
            }
        }
        Result.failure(Exception("Invalid username or password."))
    }

    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        val users = mutableListOf<User>()
        val cursor = readableDatabase.rawQuery("SELECT id, username, created_at FROM users", null)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(0)
            val username = cursor.getString(1)
            val createdAt = if (!cursor.isNull(2)) cursor.getLong(2) else 0L
            users.add(User(id, username, createdAt))
        }
        cursor.close()
        users
    }

    private fun recordLoginSync(username: String) {
        val cursor = readableDatabase.rawQuery(
            "SELECT login_frequency FROM dle_data WHERE username = ?",
            arrayOf(username)
        )
        val currentCount = cursor.use {
            if (it.moveToFirst()) it.getFloat(0) else 0f
        }
        val values = ContentValues().apply {
            put("username", username)
            put("login_frequency", currentCount + 1f)
            put("last_updated", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("dle_data", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun recordSessionTime(username: String, sessionMillis: Long) = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            "SELECT time_spent FROM dle_data WHERE username = ?",
            arrayOf(username)
        )
        val currentSpentMinutes = cursor.use {
            if (it.moveToFirst()) it.getFloat(0) else 0f
        }
        val addedMinutes = sessionMillis / 60000.0f
        val values = ContentValues().apply {
            put("time_spent", currentSpentMinutes + addedMinutes)
            put("last_updated", System.currentTimeMillis())
        }
        writableDatabase.update("dle_data", values, "username = ?", arrayOf(username))
    }

    suspend fun recordQuizResult(
        username: String,
        category: String,
        categoryNumber: Float,
        score: Int,
        totalQuestions: Int,
        difficultyLevel: Float
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        // 1. Insert into history log
        val attemptValues = ContentValues().apply {
            put("username", username)
            put("category", category)
            put("score", score)
            put("total_questions", totalQuestions)
            put("difficulty_level", difficultyLevel)
            put("timestamp", now)
        }
        writableDatabase.insert("quiz_attempts", null, attemptValues)

        // 2. Update DLE metrics
        val scorePercent = if (totalQuestions > 0) (score.toFloat() / totalQuestions.toFloat()) * 100f else 0f
        val metricValues = ContentValues().apply {
            put("username", username)
            put("quiz_score", scorePercent)
            put("difficulty_reached", difficultyLevel)
            put("category_selected", categoryNumber)
            put("last_updated", now)
        }

        val rowsUpdated = writableDatabase.update("dle_data", metricValues, "username = ?", arrayOf(username))
        if (rowsUpdated == 0) {
            metricValues.put("login_frequency", 1f)
            metricValues.put("time_spent", 0.5f)
            writableDatabase.insert("dle_data", null, metricValues)
        }
    }

    suspend fun getTelemetry(username: String): LearningTelemetry = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            "SELECT login_frequency, time_spent, quiz_score, difficulty_reached, category_selected FROM dle_data WHERE username = ?",
            arrayOf(username)
        )
        cursor.use {
            if (it.moveToFirst()) {
                LearningTelemetry(
                    loginCount = it.getInt(0).coerceAtLeast(1),
                    totalTimeMinutes = it.getFloat(1).coerceAtLeast(0f),
                    lastQuizScore = it.getFloat(2).coerceAtLeast(0f),
                    lastDifficultyReached = it.getFloat(3).coerceAtLeast(1f),
                    lastCategorySelected = it.getFloat(4).coerceAtLeast(1f)
                )
            } else {
                LearningTelemetry(1, 0.5f, 0f, 1f, 1f)
            }
        }
    }

    suspend fun getRecentQuizAttempts(username: String, limit: Int = 10): List<QuizAttempt> = withContext(Dispatchers.IO) {
        val list = mutableListOf<QuizAttempt>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, username, category, score, total_questions, difficulty_level, timestamp FROM quiz_attempts WHERE username = ? ORDER BY timestamp DESC LIMIT ?",
            arrayOf(username, limit.toString())
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    QuizAttempt(
                        id = c.getLong(0),
                        username = c.getString(1),
                        category = c.getString(2),
                        score = c.getInt(3),
                        totalQuestions = c.getInt(4),
                        difficultyLevel = c.getFloat(5),
                        timestamp = c.getLong(6)
                    )
                )
            }
        }
        list
    }

    suspend fun getAllQuizHistory(username: String, limit: Int = 100): List<QuizAttempt> = withContext(Dispatchers.IO) {
        val list = mutableListOf<QuizAttempt>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, username, category, score, total_questions, difficulty_level, timestamp FROM quiz_attempts WHERE username = ? ORDER BY timestamp DESC LIMIT ?",
            arrayOf(username, limit.toString())
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    QuizAttempt(
                        id = c.getLong(0),
                        username = c.getString(1),
                        category = c.getString(2),
                        score = c.getInt(3),
                        totalQuestions = c.getInt(4),
                        difficultyLevel = c.getFloat(5),
                        timestamp = c.getLong(6)
                    )
                )
            }
        }
        list
    }

    suspend fun calculateDailyStreak(username: String): Int = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            "SELECT timestamp FROM quiz_attempts WHERE username = ? ORDER BY timestamp DESC",
            arrayOf(username)
        )
        val timestamps = mutableListOf<Long>()
        cursor.use { c ->
            while (c.moveToNext()) {
                timestamps.add(c.getLong(0))
            }
        }
        if (timestamps.isEmpty()) return@withContext 0

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val completedDates = timestamps.map { dateFormat.format(java.util.Date(it)) }.distinct()

        val calendar = java.util.Calendar.getInstance()
        val todayStr = dateFormat.format(calendar.time)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(calendar.time)

        val firstDate = completedDates.firstOrNull() ?: return@withContext 0
        if (firstDate != todayStr && firstDate != yesterdayStr) {
            return@withContext 0
        }

        val checkCal = java.util.Calendar.getInstance()
        if (firstDate == yesterdayStr) {
            checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }

        var streak = 0
        for (date in completedDates) {
            val expectedDateStr = dateFormat.format(checkCal.time)
            if (date == expectedDateStr) {
                streak++
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else if (date < expectedDateStr) {
                break
            }
        }
        streak.coerceAtLeast(1)
    }

    suspend fun getQuizPerformanceStats(username: String): QuizPerformanceStats = withContext(Dispatchers.IO) {
        val attempts = getAllQuizHistory(username, limit = 500)
        if (attempts.isEmpty()) {
            return@withContext QuizPerformanceStats()
        }

        var totalScore = 0
        var totalQuestions = 0
        var perfectCount = 0
        var maxScore = 0
        var earlyBirdCount = 0

        val timeCal = java.util.Calendar.getInstance()
        for (attempt in attempts) {
            totalScore += attempt.score
            totalQuestions += attempt.totalQuestions
            if (attempt.score == attempt.totalQuestions && attempt.totalQuestions > 0) {
                perfectCount++
            }
            if (attempt.score > maxScore) {
                maxScore = attempt.score
            }

            timeCal.timeInMillis = attempt.timestamp
            val hourOfDay = timeCal.get(java.util.Calendar.HOUR_OF_DAY)
            if (hourOfDay in 5..9) {
                earlyBirdCount++
            }
        }

        val avgAccuracy = if (totalQuestions > 0) {
            (totalScore.toFloat() / totalQuestions.toFloat()) * 100f
        } else {
            0f
        }

        val streak = calculateDailyStreak(username)

        QuizPerformanceStats(
            totalQuizzes = attempts.size,
            totalQuestionsAnswered = totalQuestions,
            totalScore = totalScore,
            averageAccuracyPercent = avgAccuracy,
            perfectQuizzesCount = perfectCount,
            highestScore = maxScore,
            earlyBirdQuizzesCount = earlyBirdCount,
            currentDailyStreak = streak
        )
    }

    suspend fun getCategoryMasteryStats(username: String): List<CategoryMastery> = withContext(Dispatchers.IO) {
        val attempts = getAllQuizHistory(username, limit = 500)
        val grouped = attempts.groupBy { it.category.trim() }

        QuestionsRepository.AVAILABLE_CATEGORIES.map { catInfo ->
            val catAttempts = grouped[catInfo.name] ?: emptyList()
            val totalAttempts = catAttempts.size
            val totalQuestions = catAttempts.sumOf { it.totalQuestions }
            val totalScore = catAttempts.sumOf { it.score }
            val accuracy = if (totalQuestions > 0) (totalScore.toFloat() / totalQuestions.toFloat()) * 100f else 0f
            val maxDiff = catAttempts.maxOfOrNull { it.difficultyLevel } ?: 1f

            val masteryLevel = when {
                totalAttempts == 0 -> "Untested"
                accuracy >= 90f -> "Master"
                accuracy >= 75f -> "Proficient"
                accuracy >= 50f -> "Practitioner"
                else -> "Novice"
            }

            val statusBadge = when {
                totalAttempts == 0 -> "Untested"
                accuracy < 60f -> "Needs Reinforcement"
                accuracy >= 85f && totalAttempts >= 2 -> "Mastered"
                else -> "Solidifying"
            }

            CategoryMastery(
                categoryName = catInfo.name,
                icon = catInfo.icon,
                totalAttempts = totalAttempts,
                totalQuestions = totalQuestions,
                correctAnswers = totalScore,
                accuracyPercent = accuracy,
                masteryLevel = masteryLevel,
                highestDifficultyReached = maxDiff,
                statusBadge = statusBadge
            )
        }
    }

    suspend fun getActiveLearningModules(username: String): List<LearningModule> = withContext(Dispatchers.IO) {
        val masteries = getCategoryMasteryStats(username)
        val masteryMap = masteries.associateBy { it.categoryName }

        val moduleDefinitions = listOf(
            LearningModule(
                id = "mod_kotlin_android",
                categoryName = "JavaScript",
                categoryNumber = 3f,
                title = "Kotlin & Modern Android",
                description = "Declarative UI with Compose, State Hoisting, Flow & Coroutines",
                icon = "🤖",
                tag = "Mobile Architecture",
                totalLessons = 20,
                completedLessons = ((masteryMap["JavaScript"]?.totalAttempts ?: 0) * 2 + 5).coerceIn(4, 20),
                progressPercent = (((masteryMap["JavaScript"]?.totalAttempts ?: 0) * 2 + 5).toFloat() / 20f * 100f).coerceIn(20f, 100f),
                currentTopic = "Coroutines & Reactive State Flow",
                difficulty = "Intermediate",
                status = if ((masteryMap["JavaScript"]?.totalAttempts ?: 0) >= 8) ModuleStatus.COMPLETED else ModuleStatus.IN_PROGRESS,
                estimatedTimeMinutes = 12
            ),
            LearningModule(
                id = "mod_algo_dsa",
                categoryName = "Python",
                categoryNumber = 6f,
                title = "Algorithms & Data Structures",
                description = "Binary Search Trees, Graph Traversals, Dynamic Programming & Big-O",
                icon = "⚡",
                tag = "Computer Science",
                totalLessons = 20,
                completedLessons = ((masteryMap["Python"]?.totalAttempts ?: 0) * 2 + 3).coerceIn(3, 20),
                progressPercent = (((masteryMap["Python"]?.totalAttempts ?: 0) * 2 + 3).toFloat() / 20f * 100f).coerceIn(15f, 100f),
                currentTopic = "Dynamic Programming & Memoization",
                difficulty = "Advanced",
                status = if ((masteryMap["Python"]?.accuracyPercent ?: 0f) < 60f && (masteryMap["Python"]?.totalAttempts ?: 0) > 0) ModuleStatus.REVIEW_DUE else ModuleStatus.IN_PROGRESS,
                estimatedTimeMinutes = 15
            ),
            LearningModule(
                id = "mod_web_frontend",
                categoryName = "HTML",
                categoryNumber = 1f,
                title = "Web Foundation & Accessibility",
                description = "Modern semantic HTML5, WCAG accessibility, DOM node trees",
                icon = "🌐",
                tag = "Frontend Core",
                totalLessons = 16,
                completedLessons = ((masteryMap["HTML"]?.totalAttempts ?: 0) * 2 + 8).coerceIn(8, 16),
                progressPercent = (((masteryMap["HTML"]?.totalAttempts ?: 0) * 2 + 8).toFloat() / 16f * 100f).coerceIn(50f, 100f),
                currentTopic = "Accessible Rich Internet Applications (ARIA)",
                difficulty = "Beginner",
                status = if ((masteryMap["HTML"]?.totalAttempts ?: 0) >= 4) ModuleStatus.COMPLETED else ModuleStatus.IN_PROGRESS,
                estimatedTimeMinutes = 8
            ),
            LearningModule(
                id = "mod_visual_css",
                categoryName = "CSS",
                categoryNumber = 2f,
                title = "CSS Visual Systems & Design",
                description = "Flexbox layouts, CSS Grid, animations, variables & responsive rules",
                icon = "🎨",
                tag = "Design Systems",
                totalLessons = 16,
                completedLessons = ((masteryMap["CSS"]?.totalAttempts ?: 0) * 2 + 4).coerceIn(4, 16),
                progressPercent = (((masteryMap["CSS"]?.totalAttempts ?: 0) * 2 + 4).toFloat() / 16f * 100f).coerceIn(25f, 100f),
                currentTopic = "CSS Grid & Fluid Layouts",
                difficulty = "Intermediate",
                status = ModuleStatus.IN_PROGRESS,
                estimatedTimeMinutes = 10
            ),
            LearningModule(
                id = "mod_sql_db",
                categoryName = "MySQL",
                categoryNumber = 5f,
                title = "Relational Data & SQL Systems",
                description = "Complex queries, joins, indexes, transaction ACID properties & migrations",
                icon = "🐬",
                tag = "Database Systems",
                totalLessons = 18,
                completedLessons = ((masteryMap["MySQL"]?.totalAttempts ?: 0) * 2 + 6).coerceIn(6, 18),
                progressPercent = (((masteryMap["MySQL"]?.totalAttempts ?: 0) * 2 + 6).toFloat() / 18f * 100f).coerceIn(33f, 100f),
                currentTopic = "B-Tree Indexing & Query Plans",
                difficulty = "Intermediate",
                status = ModuleStatus.IN_PROGRESS,
                estimatedTimeMinutes = 12
            ),
            LearningModule(
                id = "mod_backend_php",
                categoryName = "PHP",
                categoryNumber = 4f,
                title = "Server Runtime & API Services",
                description = "HTTP request lifecycles, REST API security, sessions & backend patterns",
                icon = "🐘",
                tag = "Backend Services",
                totalLessons = 15,
                completedLessons = ((masteryMap["PHP"]?.totalAttempts ?: 0) * 2 + 2).coerceIn(2, 15),
                progressPercent = (((masteryMap["PHP"]?.totalAttempts ?: 0) * 2 + 2).toFloat() / 15f * 100f).coerceIn(13f, 100f),
                currentTopic = "RESTful APIs & Authentication",
                difficulty = "Beginner",
                status = ModuleStatus.IN_PROGRESS,
                estimatedTimeMinutes = 10
            ),
            LearningModule(
                id = "mod_c_systems",
                categoryName = "C Language",
                categoryNumber = 7f,
                title = "C Systems & Memory Architecture",
                description = "Pointers, dynamic heap allocation, struct padding, POSIX signals & low-level memory control",
                icon = "⚙️",
                tag = "Systems Architecture",
                totalLessons = 20,
                completedLessons = ((masteryMap["C Language"]?.totalAttempts ?: 0) * 2 + 3).coerceIn(3, 20),
                progressPercent = (((masteryMap["C Language"]?.totalAttempts ?: 0) * 2 + 3).toFloat() / 20f * 100f).coerceIn(15f, 100f),
                currentTopic = "Pointers, Dangling References & Heap Allocation",
                difficulty = "Advanced",
                status = if ((masteryMap["C Language"]?.accuracyPercent ?: 0f) < 60f && (masteryMap["C Language"]?.totalAttempts ?: 0) > 0) ModuleStatus.REVIEW_DUE else ModuleStatus.IN_PROGRESS,
                estimatedTimeMinutes = 15
            )
        )
        moduleDefinitions
    }

    suspend fun getPersonalizedActionPlan(
        username: String,
        profile: PersonalizationProfile
    ): PersonalizedActionPlan = withContext(Dispatchers.IO) {
        val attempts = getAllQuizHistory(username, limit = 500)
        val categoryMasteries = getCategoryMasteryStats(username)
        val dueCards = getDueFlashcards(username).size

        // Analyze optimal study time window
        val cal = java.util.Calendar.getInstance()
        var morningCount = 0
        var afternoonCount = 0
        var eveningCount = 0
        for (a in attempts) {
            cal.timeInMillis = a.timestamp
            val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            when (hour) {
                in 5..11 -> morningCount++
                in 12..16 -> afternoonCount++
                in 17..22 -> eveningCount++
            }
        }
        val optimalWindow = when {
            morningCount >= afternoonCount && morningCount >= eveningCount && morningCount > 0 ->
                "Morning Focus (07:00 - 11:00)"
            afternoonCount >= eveningCount && afternoonCount > 0 ->
                "Afternoon Cadence (13:00 - 16:00)"
            eveningCount > 0 ->
                "Evening Retention (18:00 - 22:00)"
            else ->
                "Anytime (Flexible Rhythm)"
        }

        // Derive cognitive learner archetype
        val cScore = profile.conscientiousness.score
        val mScore = profile.motivation.score
        val uScore = profile.understanding.score
        val eScore = profile.engagement.score

        val archetype = when {
            uScore >= 0.65f && cScore >= 0.55f -> LearnerArchetype(
                title = "The Deep Architect",
                subtitle = "Analytical & Rigorous",
                iconEmoji = "🏛️",
                description = "Excels at building robust mental architectures and systematic conceptual comprehension.",
                cognitiveSuperpower = "Conceptual Root Synthesis",
                suggestedPacing = "15-min intensive focus sessions with multi-step reasoning",
                optimalStudyWindow = optimalWindow
            )
            mScore >= 0.65f && eScore >= 0.55f -> LearnerArchetype(
                title = "The Velocity Sprinter",
                subtitle = "Dynamic Trailblazer",
                iconEmoji = "⚡",
                description = "Thrives under time pressure, rapid-fire question challenges, and high competitive momentum.",
                cognitiveSuperpower = "Rapid Heuristic Recall",
                suggestedPacing = "5-minute fast-paced countdown challenges",
                optimalStudyWindow = optimalWindow
            )
            cScore >= 0.60f -> LearnerArchetype(
                title = "The Methodical Builder",
                subtitle = "Disciplined Practitioner",
                iconEmoji = "📐",
                description = "Mastery is achieved through unwavering consistency, steady habits, and deliberate repetition.",
                cognitiveSuperpower = "Compound Knowledge Accumulation",
                suggestedPacing = "Daily 10-question spaced review intervals",
                optimalStudyWindow = optimalWindow
            )
            mScore >= 0.55f -> LearnerArchetype(
                title = "The Intuitive Explorer",
                subtitle = "Curious Concept Seeker",
                iconEmoji = "🧭",
                description = "Enjoys exploring diverse technical domains, discovering novel paradigms through experimentation.",
                cognitiveSuperpower = "Cross-Disciplinary Association",
                suggestedPacing = "Varied multi-category challenge rotations",
                optimalStudyWindow = optimalWindow
            )
            else -> LearnerArchetype(
                title = "The Adaptive Scholar",
                subtitle = "Dynamic Lifelong Learner",
                iconEmoji = "🌟",
                description = "Dynamically adjusts cognitive strategy based on difficulty fluctuations and learning feedback.",
                cognitiveSuperpower = "Flexible Problem Solving",
                suggestedPacing = "Balanced 10-minute micro-learning blocks",
                optimalStudyWindow = optimalWindow
            )
        }

        // Recommend personalized category
        // Priority 1: Category with low accuracy (< 70%) that has been attempted
        val weakCategory = categoryMasteries.filter { it.totalAttempts > 0 && it.accuracyPercent < 70f }
            .minByOrNull { it.accuracyPercent }

        // Priority 2: Untested category
        val untestedCategory = categoryMasteries.firstOrNull { it.totalAttempts == 0 }

        // Priority 3: Highest-accuracy category for mastery expansion
        val topCategory = categoryMasteries.maxByOrNull { it.accuracyPercent } ?: categoryMasteries[0]

        val targetMastery = weakCategory ?: untestedCategory ?: topCategory
        val catInfo = QuestionsRepository.AVAILABLE_CATEGORIES.firstOrNull { it.name == targetMastery.categoryName }
            ?: QuestionsRepository.AVAILABLE_CATEGORIES[0]

        val reason = when {
            weakCategory != null ->
                "Targeted Reinforcement: Your accuracy in ${targetMastery.categoryName} is currently ${targetMastery.accuracyPercent.toInt()}%. Practice now to lock in conceptual proficiency."
            untestedCategory != null ->
                "Cognitive Expansion: You haven't explored ${targetMastery.categoryName} yet. A quick session will broaden your skill radar!"
            else ->
                "Mastery Sprint: You are performing strongly in ${targetMastery.categoryName} (${targetMastery.accuracyPercent.toInt()}%). Ready to test hard-tier challenges!"
        }

        val initialTier = when {
            targetMastery.totalAttempts > 0 && targetMastery.accuracyPercent >= 80f && uScore >= 0.65f -> "Hard"
            targetMastery.totalAttempts > 0 && targetMastery.accuracyPercent >= 55f || uScore >= 0.50f -> "Medium"
            else -> "Easy"
        }

        val readinessScore = ((cScore * 0.25f) + (mScore * 0.25f) + (uScore * 0.35f) + (eScore * 0.15f)).coerceIn(0.1f, 1.0f)

        PersonalizedActionPlan(
            archetype = archetype,
            recommendedCategory = targetMastery.categoryName,
            recommendedCategoryNumber = catInfo.id.toFloat(),
            recommendationReason = reason,
            recommendedCategoryIcon = targetMastery.icon,
            recommendedInitialTier = initialTier,
            dueFlashcardsCount = dueCards,
            categoryMasteries = categoryMasteries,
            optimalTimeWindow = optimalWindow,
            cognitiveReadinessScore = readinessScore
        )
    }

    suspend fun clearUserData(username: String) = withContext(Dispatchers.IO) {
        val args = arrayOf(username)
        writableDatabase.delete("dle_data", "username = ?", args)
        writableDatabase.delete("quiz_attempts", "username = ?", args)
        writableDatabase.delete("daily_goals", "username = ?", args)
        writableDatabase.delete("flashcards", "username = ?", args)
        writableDatabase.delete("trait_snapshots", "username = ?", args)
        writableDatabase.delete("trained_weights", "username = ?", args)
        writableDatabase.delete("study_sessions", "username = ?", args)
        writableDatabase.delete("training_checkpoints", "username = ?", args)
        writableDatabase.delete("training_checkpoints_v2", "username = ?", args)
        writableDatabase.delete("training_logs", "username = ?", args)
        writableDatabase.delete("friends", "owner_username = ? OR friend_username = ?", arrayOf(username, username))
        writableDatabase.delete("users", "username = ?", args)
    }

    suspend fun saveModelWeights(username: String, weights: ModelWeights) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("username", username)
            put("weights_json", weights.toJson())
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("trained_weights", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun loadModelWeights(username: String): ModelWeights? = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            "SELECT weights_json FROM trained_weights WHERE username = ?",
            arrayOf(username)
        )
        cursor.use {
            if (it.moveToFirst()) {
                val json = it.getString(0)
                try {
                    ModelWeights.fromJson(json)
                } catch (e: Exception) {
                    null
                }
            } else null
        }
    }

    suspend fun resetModelWeights(username: String) = withContext(Dispatchers.IO) {
        writableDatabase.delete("trained_weights", "username = ?", arrayOf(username))
    }

    // --- Model Checkpoints Management & Reversion ---
    suspend fun saveTrainingCheckpoint(checkpoint: TrainingCheckpoint): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        // Also update legacy single-checkpoint table for crash-recovery compatibility
        val legacyValues = ContentValues().apply {
            put("username", checkpoint.username)
            put("session_id", checkpoint.sessionId)
            put("current_epoch", checkpoint.currentEpoch)
            put("target_epochs", checkpoint.targetEpochs)
            put("current_loss", checkpoint.currentLoss)
            put("loss_history_json", checkpoint.lossHistoryToJson())
            put("weights_json", checkpoint.weights.toJson())
            put("saved_at", checkpoint.savedAt)
            put("is_completed", if (checkpoint.isCompleted) 1 else 0)
        }
        db.insertWithOnConflict("training_checkpoints", null, legacyValues, SQLiteDatabase.CONFLICT_REPLACE)

        // Insert into multi-checkpoint persistent table
        val values = ContentValues().apply {
            put("username", checkpoint.username)
            put("session_id", checkpoint.sessionId)
            put("checkpoint_name", checkpoint.checkpointName.ifEmpty { "Epoch ${checkpoint.currentEpoch}" })
            put("current_epoch", checkpoint.currentEpoch)
            put("target_epochs", checkpoint.targetEpochs)
            put("current_loss", checkpoint.currentLoss)
            put("accuracy_pct", checkpoint.accuracyPct)
            put("loss_history_json", checkpoint.lossHistoryToJson())
            put("weights_json", checkpoint.weights.toJson())
            put("saved_at", checkpoint.savedAt)
            put("is_completed", if (checkpoint.isCompleted) 1 else 0)
            put("is_best", if (checkpoint.isBest) 1 else 0)
            put("trigger_type", checkpoint.triggerType)
        }
        db.insert("training_checkpoints_v2", null, values)
    }

    suspend fun getTrainingCheckpoints(username: String, limit: Int = 30): List<TrainingCheckpoint> = withContext(Dispatchers.IO) {
        val list = mutableListOf<TrainingCheckpoint>()
        readableDatabase.rawQuery(
            """
            SELECT id, username, session_id, checkpoint_name, current_epoch, target_epochs,
                   current_loss, accuracy_pct, loss_history_json, weights_json, saved_at,
                   is_completed, is_best, trigger_type
            FROM training_checkpoints_v2
            WHERE username = ?
            ORDER BY saved_at DESC, id DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(username, limit.toString())
        ).use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow("id")
            val uIdx = cursor.getColumnIndexOrThrow("username")
            val sIdx = cursor.getColumnIndexOrThrow("session_id")
            val nameIdx = cursor.getColumnIndexOrThrow("checkpoint_name")
            val epIdx = cursor.getColumnIndexOrThrow("current_epoch")
            val tEpIdx = cursor.getColumnIndexOrThrow("target_epochs")
            val lossIdx = cursor.getColumnIndexOrThrow("current_loss")
            val accIdx = cursor.getColumnIndexOrThrow("accuracy_pct")
            val histIdx = cursor.getColumnIndexOrThrow("loss_history_json")
            val wIdx = cursor.getColumnIndexOrThrow("weights_json")
            val savedIdx = cursor.getColumnIndexOrThrow("saved_at")
            val compIdx = cursor.getColumnIndexOrThrow("is_completed")
            val bestIdx = cursor.getColumnIndexOrThrow("is_best")
            val trigIdx = cursor.getColumnIndexOrThrow("trigger_type")

            while (cursor.moveToNext()) {
                val weights = ModelWeights.fromJson(cursor.getString(wIdx))
                val lossHist = TrainingCheckpoint.parseLossHistory(cursor.getString(histIdx))
                list.add(
                    TrainingCheckpoint(
                        id = cursor.getLong(idIdx),
                        username = cursor.getString(uIdx),
                        sessionId = cursor.getString(sIdx),
                        checkpointName = cursor.getString(nameIdx),
                        currentEpoch = cursor.getInt(epIdx),
                        targetEpochs = cursor.getInt(tEpIdx),
                        currentLoss = cursor.getFloat(lossIdx),
                        accuracyPct = cursor.getFloat(accIdx),
                        lossHistory = lossHist,
                        weights = weights,
                        savedAt = cursor.getLong(savedIdx),
                        isCompleted = cursor.getInt(compIdx) == 1,
                        isBest = cursor.getInt(bestIdx) == 1,
                        triggerType = cursor.getString(trigIdx)
                    )
                )
            }
        }
        list
    }

    suspend fun getCheckpointById(id: Long): TrainingCheckpoint? = withContext(Dispatchers.IO) {
        readableDatabase.rawQuery(
            """
            SELECT id, username, session_id, checkpoint_name, current_epoch, target_epochs,
                   current_loss, accuracy_pct, loss_history_json, weights_json, saved_at,
                   is_completed, is_best, trigger_type
            FROM training_checkpoints_v2
            WHERE id = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(id.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val idIdx = cursor.getColumnIndexOrThrow("id")
                val uIdx = cursor.getColumnIndexOrThrow("username")
                val sIdx = cursor.getColumnIndexOrThrow("session_id")
                val nameIdx = cursor.getColumnIndexOrThrow("checkpoint_name")
                val epIdx = cursor.getColumnIndexOrThrow("current_epoch")
                val tEpIdx = cursor.getColumnIndexOrThrow("target_epochs")
                val lossIdx = cursor.getColumnIndexOrThrow("current_loss")
                val accIdx = cursor.getColumnIndexOrThrow("accuracy_pct")
                val histIdx = cursor.getColumnIndexOrThrow("loss_history_json")
                val wIdx = cursor.getColumnIndexOrThrow("weights_json")
                val savedIdx = cursor.getColumnIndexOrThrow("saved_at")
                val compIdx = cursor.getColumnIndexOrThrow("is_completed")
                val bestIdx = cursor.getColumnIndexOrThrow("is_best")
                val trigIdx = cursor.getColumnIndexOrThrow("trigger_type")

                val weights = ModelWeights.fromJson(cursor.getString(wIdx))
                val lossHist = TrainingCheckpoint.parseLossHistory(cursor.getString(histIdx))
                TrainingCheckpoint(
                    id = cursor.getLong(idIdx),
                    username = cursor.getString(uIdx),
                    sessionId = cursor.getString(sIdx),
                    checkpointName = cursor.getString(nameIdx),
                    currentEpoch = cursor.getInt(epIdx),
                    targetEpochs = cursor.getInt(tEpIdx),
                    currentLoss = cursor.getFloat(lossIdx),
                    accuracyPct = cursor.getFloat(accIdx),
                    lossHistory = lossHist,
                    weights = weights,
                    savedAt = cursor.getLong(savedIdx),
                    isCompleted = cursor.getInt(compIdx) == 1,
                    isBest = cursor.getInt(bestIdx) == 1,
                    triggerType = cursor.getString(trigIdx)
                )
            } else null
        }
    }

    suspend fun getBestCheckpoint(username: String): TrainingCheckpoint? = withContext(Dispatchers.IO) {
        val checkpoints = getTrainingCheckpoints(username, 50)
        checkpoints.firstOrNull { it.isBest } ?: checkpoints.minByOrNull { it.currentLoss }
    }

    suspend fun revertModelToCheckpoint(username: String, checkpointId: Long): TrainingCheckpoint? = withContext(Dispatchers.IO) {
        val checkpoint = getCheckpointById(checkpointId) ?: return@withContext null
        saveModelWeights(username, checkpoint.weights)
        checkpoint
    }

    suspend fun deleteCheckpoint(checkpointId: Long): Boolean = withContext(Dispatchers.IO) {
        writableDatabase.delete("training_checkpoints_v2", "id = ?", arrayOf(checkpointId.toString())) > 0
    }

    suspend fun clearTrainingCheckpoints(username: String): Int = withContext(Dispatchers.IO) {
        writableDatabase.delete("training_checkpoints_v2", "username = ?", arrayOf(username))
    }

    suspend fun getLatestResumableCheckpoint(username: String): TrainingCheckpoint? = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT username, session_id, current_epoch, target_epochs, current_loss, loss_history_json, weights_json, saved_at, is_completed
            FROM training_checkpoints
            WHERE username = ? AND is_completed = 0 AND current_epoch < target_epochs
            LIMIT 1
            """.trimIndent(),
            arrayOf(username)
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val uname = c.getString(0)
                val sessId = c.getString(1)
                val currEpoch = c.getInt(2)
                val targetEpochs = c.getInt(3)
                val currLoss = c.getFloat(4)
                val lossHistoryJson = c.getString(5)
                val weightsJson = c.getString(6)
                val savedAt = c.getLong(7)
                val isComp = c.getInt(8) == 1

                val weights = ModelWeights.fromJson(weightsJson)
                val lossHist = TrainingCheckpoint.parseLossHistory(lossHistoryJson)

                TrainingCheckpoint(
                    username = uname,
                    sessionId = sessId,
                    currentEpoch = currEpoch,
                    targetEpochs = targetEpochs,
                    currentLoss = currLoss,
                    lossHistory = lossHist,
                    weights = weights,
                    savedAt = savedAt,
                    isCompleted = isComp
                )
            } else null
        }
    }

    suspend fun clearTrainingCheckpoint(username: String) = withContext(Dispatchers.IO) {
        writableDatabase.delete("training_checkpoints", "username = ?", arrayOf(username))
    }

    suspend fun markCheckpointCompleted(username: String) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("is_completed", 1)
        }
        writableDatabase.update("training_checkpoints", values, "username = ?", arrayOf(username))
    }

    suspend fun generateTrainingDataset(username: String): List<TrainingSample> = withContext(Dispatchers.IO) {
        val samples = mutableListOf<TrainingSample>()
        val attempts = getRecentQuizAttempts(username, 50)
        val telemetry = getTelemetry(username)

        // 1. From real user attempts
        for (att in attempts) {
            val scorePct = if (att.totalQuestions > 0) (att.score.toFloat() / att.totalQuestions) * 100f else 50f
            val catNum = when (att.category.lowercase()) {
                "html" -> 1f; "css" -> 2f; "javascript" -> 3f
                "php" -> 4f; "mysql" -> 5f; "python" -> 6f
                else -> 1f
            }
            val inputs = floatArrayOf(
                telemetry.loginCount.toFloat().coerceAtLeast(1f),
                telemetry.totalTimeMinutes.coerceAtLeast(1f),
                scorePct,
                att.difficultyLevel.coerceIn(1f, 3f),
                catNum
            )

            val conscientiousness = (telemetry.loginCount.toFloat() / 10f).coerceIn(0.2f, 0.95f)
            val motivation = (telemetry.totalTimeMinutes / 30f + scorePct / 200f).coerceIn(0.2f, 0.95f)
            val understanding = (scorePct / 100f * 0.8f + (att.difficultyLevel / 3f) * 0.2f).coerceIn(0.1f, 0.98f)
            val engagement = ((scorePct / 100f) * 0.6f + (telemetry.loginCount / 8f) * 0.4f).coerceIn(0.15f, 0.95f)

            samples.add(TrainingSample(inputs, floatArrayOf(conscientiousness, motivation, understanding, engagement)))
        }

        // 2. Calibration base samples
        val calibrationProfiles = listOf(
            floatArrayOf(1f, 2f, 40f, 1f, 1f) to floatArrayOf(0.35f, 0.40f, 0.38f, 0.30f),
            floatArrayOf(2f, 5f, 50f, 1f, 2f) to floatArrayOf(0.42f, 0.45f, 0.46f, 0.40f),
            floatArrayOf(4f, 15f, 70f, 2f, 3f) to floatArrayOf(0.65f, 0.68f, 0.70f, 0.62f),
            floatArrayOf(5f, 22f, 75f, 2f, 1f) to floatArrayOf(0.70f, 0.72f, 0.74f, 0.68f),
            floatArrayOf(8f, 45f, 90f, 3f, 6f) to floatArrayOf(0.88f, 0.92f, 0.90f, 0.89f),
            floatArrayOf(12f, 60f, 95f, 3f, 5f) to floatArrayOf(0.94f, 0.95f, 0.96f, 0.93f),
            floatArrayOf(6f, 50f, 55f, 2f, 3f) to floatArrayOf(0.82f, 0.75f, 0.52f, 0.70f),
            floatArrayOf(3f, 8f, 90f, 2f, 6f) to floatArrayOf(0.55f, 0.78f, 0.92f, 0.72f)
        )

        for ((inp, tgt) in calibrationProfiles) {
            samples.add(TrainingSample(inp, tgt))
        }

        samples
    }

    suspend fun getDatabaseStats(): Map<String, Int> = withContext(Dispatchers.IO) {
        val stats = mutableMapOf<String, Int>()
        fun count(table: String): Int {
            return readableDatabase.rawQuery("SELECT COUNT(*) FROM $table", null).use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }
        }
        stats["users"] = count("users")
        stats["dle_data"] = count("dle_data")
        stats["quiz_attempts"] = count("quiz_attempts")
        stats["trained_weights"] = count("trained_weights")
        stats["trait_snapshots"] = count("trait_snapshots")
        stats["flashcards"] = count("flashcards")
        stats["study_sessions"] = count("study_sessions")
        stats
    }

    // --- Trait Snapshots (Timeline & Evolution) ---
    suspend fun saveTraitSnapshot(username: String, profile: PersonalizationProfile) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("conscientiousness", profile.conscientiousness.score)
            put("motivation", profile.motivation.score)
            put("understanding", profile.understanding.score)
            put("engagement", profile.engagement.score)
            put("primary_strength", profile.primaryStrength)
            put("focus_area", profile.focusArea)
            put("model_source", profile.modelSource)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert("trait_snapshots", null, values)
    }

    suspend fun getTraitSnapshots(username: String, limit: Int = 30): List<TraitSnapshot> = withContext(Dispatchers.IO) {
        val list = mutableListOf<TraitSnapshot>()
        readableDatabase.rawQuery(
            "SELECT * FROM trait_snapshots WHERE username = ? ORDER BY timestamp DESC LIMIT ?",
            arrayOf(username, limit.toString())
        ).use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow("id")
            val uIdx = cursor.getColumnIndexOrThrow("username")
            val cIdx = cursor.getColumnIndexOrThrow("conscientiousness")
            val mIdx = cursor.getColumnIndexOrThrow("motivation")
            val unIdx = cursor.getColumnIndexOrThrow("understanding")
            val eIdx = cursor.getColumnIndexOrThrow("engagement")
            val psIdx = cursor.getColumnIndexOrThrow("primary_strength")
            val faIdx = cursor.getColumnIndexOrThrow("focus_area")
            val msIdx = cursor.getColumnIndexOrThrow("model_source")
            val tIdx = cursor.getColumnIndexOrThrow("timestamp")

            while (cursor.moveToNext()) {
                list.add(
                    TraitSnapshot(
                        id = cursor.getLong(idIdx),
                        username = cursor.getString(uIdx),
                        conscientiousness = cursor.getFloat(cIdx),
                        motivation = cursor.getFloat(mIdx),
                        understanding = cursor.getFloat(unIdx),
                        engagement = cursor.getFloat(eIdx),
                        primaryStrength = cursor.getString(psIdx),
                        focusArea = cursor.getString(faIdx),
                        modelSource = cursor.getString(msIdx),
                        timestamp = cursor.getLong(tIdx)
                    )
                )
            }
        }
        list.reversed()
    }

    // --- Spaced Repetition (Leitner Flashcards) ---
    suspend fun addMissedQuestionToFlashcard(username: String, question: Question) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val exists = db.rawQuery(
            "SELECT id FROM flashcards WHERE username = ? AND question = ?",
            arrayOf(username, question.question)
        ).use { it.moveToFirst() }

        if (!exists) {
            val optionsJson = question.options.joinToString("|||")
            val values = ContentValues().apply {
                put("username", username)
                put("category", question.category)
                put("question", question.question)
                put("options_json", optionsJson)
                put("correct_answer", question.answer.toString())
                put("explanation", question.explanation.ifBlank { question.example })
                put("box_level", 1)
                put("next_review_time", System.currentTimeMillis())
                put("review_count", 0)
                put("last_reviewed", 0)
            }
            db.insert("flashcards", null, values)
        }
    }

    suspend fun addFlashcard(
        username: String,
        category: String,
        question: String,
        options: List<String>,
        correctAnswer: String,
        explanation: String
    ) = withContext(Dispatchers.IO) {
        val exists = writableDatabase.rawQuery(
            "SELECT id FROM flashcards WHERE username = ? AND question = ?",
            arrayOf(username, question)
        ).use { it.moveToFirst() }

        if (!exists) {
            val cv = ContentValues().apply {
                put("username", username)
                put("category", category)
                put("question", question)
                put("options_json", options.joinToString("|||"))
                put("correct_answer", correctAnswer)
                put("explanation", explanation)
                put("box_level", 1)
                put("next_review_time", System.currentTimeMillis() + 86400000L)
                put("review_count", 0)
                put("last_reviewed", 0)
            }
            writableDatabase.insert("flashcards", null, cv)
        }
    }

    suspend fun getDueFlashcards(username: String): List<FlashcardItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<FlashcardItem>()
        val now = System.currentTimeMillis()
        readableDatabase.rawQuery(
            "SELECT * FROM flashcards WHERE username = ? AND next_review_time <= ? ORDER BY box_level ASC, next_review_time ASC",
            arrayOf(username, now.toString())
        ).use { cursor ->
            mapFlashcards(cursor, list)
        }
        list
    }

    suspend fun getAllFlashcards(username: String): List<FlashcardItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<FlashcardItem>()
        readableDatabase.rawQuery(
            "SELECT * FROM flashcards WHERE username = ? ORDER BY box_level ASC, id DESC",
            arrayOf(username)
        ).use { cursor ->
            mapFlashcards(cursor, list)
        }
        list
    }

    private fun mapFlashcards(cursor: Cursor, list: MutableList<FlashcardItem>) {
        val idIdx = cursor.getColumnIndexOrThrow("id")
        val uIdx = cursor.getColumnIndexOrThrow("username")
        val cIdx = cursor.getColumnIndexOrThrow("category")
        val qIdx = cursor.getColumnIndexOrThrow("question")
        val oIdx = cursor.getColumnIndexOrThrow("options_json")
        val aIdx = cursor.getColumnIndexOrThrow("correct_answer")
        val eIdx = cursor.getColumnIndexOrThrow("explanation")
        val bIdx = cursor.getColumnIndexOrThrow("box_level")
        val nrIdx = cursor.getColumnIndexOrThrow("next_review_time")
        val rcIdx = cursor.getColumnIndexOrThrow("review_count")
        val lrIdx = cursor.getColumnIndexOrThrow("last_reviewed")

        while (cursor.moveToNext()) {
            list.add(
                FlashcardItem(
                    id = cursor.getLong(idIdx),
                    username = cursor.getString(uIdx),
                    category = cursor.getString(cIdx),
                    question = cursor.getString(qIdx),
                    optionsJson = cursor.getString(oIdx),
                    correctAnswer = cursor.getString(aIdx),
                    explanation = cursor.getString(eIdx),
                    boxLevel = cursor.getInt(bIdx),
                    nextReviewTimestamp = cursor.getLong(nrIdx),
                    reviewCount = cursor.getInt(rcIdx),
                    lastReviewedTimestamp = cursor.getLong(lrIdx)
                )
            )
        }
    }

    suspend fun updateFlashcardReview(id: Long, remembered: Boolean) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        var currentBox = 1
        var count = 0
        db.rawQuery("SELECT box_level, review_count FROM flashcards WHERE id = ?", arrayOf(id.toString())).use {
            if (it.moveToFirst()) {
                currentBox = it.getInt(0)
                count = it.getInt(1)
            }
        }

        val now = System.currentTimeMillis()
        val newBox: Int
        val intervalMillis: Long

        if (remembered) {
            newBox = minOf(4, currentBox + 1)
            intervalMillis = when (newBox) {
                1 -> 86400000L
                2 -> 3 * 86400000L
                3 -> 7 * 86400000L
                else -> 14 * 86400000L
            }
        } else {
            newBox = 1
            intervalMillis = 2 * 3600000L
        }

        val values = ContentValues().apply {
            put("box_level", newBox)
            put("next_review_time", now + intervalMillis)
            put("review_count", count + 1)
            put("last_reviewed", now)
        }
        db.update("flashcards", values, "id = ?", arrayOf(id.toString()))
    }

    // --- Study & Deep Work Sessions ---
    suspend fun saveStudySession(
        username: String,
        durationSeconds: Long,
        targetMinutes: Int,
        pauseCount: Int,
        focusScore: Float
    ) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("duration_seconds", durationSeconds)
            put("target_minutes", targetMinutes)
            put("pause_count", pauseCount)
            put("focus_score", focusScore)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert("study_sessions", null, values)

        recordSessionTime(username, durationSeconds * 1000L)
    }

    suspend fun getStudySessions(username: String, limit: Int = 20): List<StudySessionRecord> = withContext(Dispatchers.IO) {
        val list = mutableListOf<StudySessionRecord>()
        readableDatabase.rawQuery(
            "SELECT * FROM study_sessions WHERE username = ? ORDER BY timestamp DESC LIMIT ?",
            arrayOf(username, limit.toString())
        ).use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow("id")
            val uIdx = cursor.getColumnIndexOrThrow("username")
            val dIdx = cursor.getColumnIndexOrThrow("duration_seconds")
            val tIdx = cursor.getColumnIndexOrThrow("target_minutes")
            val pIdx = cursor.getColumnIndexOrThrow("pause_count")
            val fIdx = cursor.getColumnIndexOrThrow("focus_score")
            val tsIdx = cursor.getColumnIndexOrThrow("timestamp")

            while (cursor.moveToNext()) {
                list.add(
                    StudySessionRecord(
                        id = cursor.getLong(idIdx),
                        username = cursor.getString(uIdx),
                        durationSeconds = cursor.getLong(dIdx),
                        targetMinutes = cursor.getInt(tIdx),
                        pauseCount = cursor.getInt(pIdx),
                        focusScore = cursor.getFloat(fIdx),
                        timestamp = cursor.getLong(tsIdx)
                    )
                )
            }
        }
        list
    }

    suspend fun getTotalFocusMinutes(username: String): Int = withContext(Dispatchers.IO) {
        readableDatabase.rawQuery(
            "SELECT SUM(duration_seconds) FROM study_sessions WHERE username = ?",
            arrayOf(username)
        ).use {
            if (it.moveToFirst()) (it.getLong(0) / 60L).toInt() else 0
        }
    }

    // ----------------------------------------------------
    // TRAINING LOG & EVENT AUDIT TRAIL
    // ----------------------------------------------------

    suspend fun recordTrainingLog(log: TrainingLogEntry): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("username", log.username)
            put("session_id", log.sessionId)
            put("timestamp", log.timestamp)
            put("log_level", log.level.name)
            put("category", log.category.name)
            put("title", log.title)
            put("message", log.message)
            put("details_json", log.detailsJson)
        }
        writableDatabase.insert("training_logs", null, values)
    }

    suspend fun getTrainingLogs(username: String, limit: Int = 100): List<TrainingLogEntry> = withContext(Dispatchers.IO) {
        val list = mutableListOf<TrainingLogEntry>()
        try {
            readableDatabase.rawQuery(
                """
                SELECT id, username, session_id, timestamp, log_level, category, title, message, details_json
                FROM training_logs
                WHERE username = ?
                ORDER BY timestamp DESC, id DESC
                LIMIT ?
                """.trimIndent(),
                arrayOf(username, limit.toString())
            ).use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow("id")
                val uIdx = cursor.getColumnIndexOrThrow("username")
                val sIdx = cursor.getColumnIndexOrThrow("session_id")
                val tsIdx = cursor.getColumnIndexOrThrow("timestamp")
                val lvlIdx = cursor.getColumnIndexOrThrow("log_level")
                val catIdx = cursor.getColumnIndexOrThrow("category")
                val tIdx = cursor.getColumnIndexOrThrow("title")
                val mIdx = cursor.getColumnIndexOrThrow("message")
                val dIdx = cursor.getColumnIndexOrThrow("details_json")

                while (cursor.moveToNext()) {
                    val lvlStr = cursor.getString(lvlIdx)
                    val catStr = cursor.getString(catIdx)
                    val level = try { LogLevel.valueOf(lvlStr) } catch (_: Exception) { LogLevel.INFO }
                    val cat = try { LogCategory.valueOf(catStr) } catch (_: Exception) { LogCategory.SYSTEM }

                    list.add(
                        TrainingLogEntry(
                            id = cursor.getLong(idIdx),
                            username = cursor.getString(uIdx),
                            sessionId = cursor.getString(sIdx),
                            timestamp = cursor.getLong(tsIdx),
                            level = level,
                            category = cat,
                            title = cursor.getString(tIdx),
                            message = cursor.getString(mIdx),
                            detailsJson = cursor.getString(dIdx)
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        list
    }

    suspend fun clearTrainingLogs(username: String): Int = withContext(Dispatchers.IO) {
        writableDatabase.delete("training_logs", "username = ?", arrayOf(username))
    }

    suspend fun initializeDefaultTrainingLogsIfEmpty(username: String): List<TrainingLogEntry> = withContext(Dispatchers.IO) {
        val existing = getTrainingLogs(username, 1)
        if (existing.isEmpty()) {
            val now = System.currentTimeMillis()
            val initialLogs = listOf(
                TrainingLogEntry(
                    username = username,
                    sessionId = "sess_init",
                    timestamp = now - 180000,
                    level = LogLevel.SUCCESS,
                    category = LogCategory.SYSTEM,
                    title = "Neural Model Initialized",
                    message = "Loaded 84-parameter FP32 multi-layer perceptron (5 inputs, 8 hidden LeakyReLU, 4 output Sigmoid).",
                    detailsJson = "{\"parameters\": 84, \"precision\": \"FP32\", \"inputDim\": 5, \"hiddenDim\": 8, \"outputDim\": 4}"
                ),
                TrainingLogEntry(
                    username = username,
                    sessionId = "sess_init",
                    timestamp = now - 130000,
                    level = LogLevel.INFO,
                    category = LogCategory.DATA_QUALITY,
                    title = "Telemetry Normalization Audit",
                    message = "Z-score feature scaler validated across 5 student behavioral dimensions with zero-mean unit-variance.",
                    detailsJson = "{\"features\": [\"quiz_score\", \"time_spent\", \"login_freq\", \"difficulty\", \"category_num\"], \"status\": \"PASSED\"}"
                ),
                TrainingLogEntry(
                    username = username,
                    sessionId = "sess_init",
                    timestamp = now - 95000,
                    level = LogLevel.INFO,
                    category = LogCategory.PARAM_ADJUSTMENT,
                    title = "Hyperparameters Configured",
                    message = "Optimizer set to SGD with Momentum (learning_rate = 0.035, momentum = 0.90, batch_size = 16, epochs = 10).",
                    detailsJson = "{\"learningRate\": 0.035, \"momentum\": 0.9, \"batchSize\": 16, \"targetEpochs\": 10}"
                ),
                TrainingLogEntry(
                    username = username,
                    sessionId = "sess_init",
                    timestamp = now - 60000,
                    level = LogLevel.WARNING,
                    category = LogCategory.DATA_QUALITY,
                    title = "Data Quality Alert: Low Sample Diversity",
                    message = "Observed telemetry features are concentrated in 2 quiz topics. Synthetic Dirichlet bootstrap active to prevent gradient collapse.",
                    detailsJson = "{\"warning\": \"LOW_SAMPLE_COUNT\", \"augmentedSamples\": 12, \"status\": \"AUGMENTED\"}"
                ),
                TrainingLogEntry(
                    username = username,
                    sessionId = "sess_init",
                    timestamp = now - 25000,
                    level = LogLevel.SUCCESS,
                    category = LogCategory.CHECKPOINT,
                    title = "Checkpoint Saved: Baseline Calibration",
                    message = "Saved initial baseline checkpoint with loss 0.1850 MSE to local SQLite store.",
                    detailsJson = "{\"checkpoint\": \"cp_baseline\", \"epoch\": 0, \"loss\": 0.185, \"accuracy\": 75.0}"
                )
            )
            for (log in initialLogs) {
                recordTrainingLog(log)
            }
            return@withContext getTrainingLogs(username, 100)
        }
        existing
    }

    fun generateDailyCommunityLearners(calendar: Calendar = Calendar.getInstance()): List<GeneratedCommunityPeer> =
        Companion.generateDailyCommunityLearners(calendar)

    suspend fun syncCommunityLearnersDaily(force: Boolean = false) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val prefs = appContext.getSharedPreferences("virtual_peers_prefs", Context.MODE_PRIVATE)
        val cal = Calendar.getInstance()
        val currentEpochDay = (cal.timeInMillis / (1000L * 60 * 60 * 24)).toInt()
        val lastEpochDay = prefs.getInt("last_virtual_peer_epoch_day", -1)

        val cursorSudo = db.rawQuery(
            "SELECT COUNT(*) FROM community_learners WHERE username LIKE '%sudo%' OR display_name LIKE '%sudo%' OR username IN ('elena_r', 'marcus_c', 'kai_n', 'sophia_v')",
            null
        )
        val sudoCount = if (cursorSudo.moveToFirst()) cursorSudo.getInt(0) else 0
        cursorSudo.close()

        val cursorCount = db.rawQuery("SELECT COUNT(*) FROM community_learners", null)
        val totalCount = if (cursorCount.moveToFirst()) cursorCount.getInt(0) else 0
        cursorCount.close()

        val needsRegen = force || sudoCount > 0 || totalCount < 10 || lastEpochDay != currentEpochDay

        if (needsRegen) {
            db.delete("community_learners", null, null)
            db.delete("friends", "friend_username LIKE '%sudo%' OR owner_username LIKE '%sudo%' OR friend_username IN ('elena_r', 'marcus_c', 'kai_n')", null)

            val peers = generateDailyCommunityLearners(cal)
            for (peer in peers) {
                db.insertWithOnConflict("community_learners", null, peer.toContentValues(), SQLiteDatabase.CONFLICT_REPLACE)
            }
            prefs.edit().putInt("last_virtual_peer_epoch_day", currentEpochDay).apply()
        }
    }

    suspend fun seedCommunityLearnersIfEmpty() = syncCommunityLearnersDaily(false)

    suspend fun ensureDefaultFriends(ownerUsername: String) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.delete("friends", "friend_username LIKE '%sudo%' OR owner_username LIKE '%sudo%' OR friend_username IN ('elena_r', 'marcus_c', 'kai_n')", null)

        val cursor = db.rawQuery("SELECT COUNT(*) FROM friends WHERE owner_username = ?", arrayOf(ownerUsername))
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()

        if (count == 0) {
            val defaults = listOf("martiantiger67", "snowyowl3", "cosmicfalcon42")
            val now = System.currentTimeMillis()
            for (f in defaults) {
                if (f != ownerUsername) {
                    val cv = ContentValues().apply {
                        put("owner_username", ownerUsername)
                        put("friend_username", f)
                        put("created_at", now)
                    }
                    db.insertWithOnConflict("friends", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                }
            }
        }
    }

    suspend fun getFriends(ownerUsername: String): List<String> = withContext(Dispatchers.IO) {
        val friends = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery(
            "SELECT friend_username FROM friends WHERE owner_username = ?",
            arrayOf(ownerUsername)
        )
        while (cursor.moveToNext()) {
            friends.add(cursor.getString(0))
        }
        cursor.close()
        friends
    }

    suspend fun addFriend(ownerUsername: String, friendUsername: String): Boolean = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("owner_username", ownerUsername)
            put("friend_username", friendUsername)
            put("created_at", System.currentTimeMillis())
        }
        val rowId = writableDatabase.insertWithOnConflict("friends", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        rowId != -1L
    }

    suspend fun removeFriend(ownerUsername: String, friendUsername: String): Boolean = withContext(Dispatchers.IO) {
        val deleted = writableDatabase.delete(
            "friends",
            "owner_username = ? AND friend_username = ?",
            arrayOf(ownerUsername, friendUsername)
        )
        deleted > 0
    }

    suspend fun toggleFriend(ownerUsername: String, friendUsername: String): Boolean = withContext(Dispatchers.IO) {
        val isAlreadyFriend = readableDatabase.rawQuery(
            "SELECT id FROM friends WHERE owner_username = ? AND friend_username = ?",
            arrayOf(ownerUsername, friendUsername)
        ).use { it.moveToFirst() }

        if (isAlreadyFriend) {
            removeFriend(ownerUsername, friendUsername)
            false
        } else {
            addFriend(ownerUsername, friendUsername)
            true
        }
    }

    suspend fun getLeaderboardEntries(
        currentUsername: String,
        scope: LeaderboardScope = LeaderboardScope.GLOBAL,
        sortBy: LeaderboardSort = LeaderboardSort.STREAK
    ): List<LeaderboardEntry> = withContext(Dispatchers.IO) {
        seedCommunityLearnersIfEmpty()
        ensureDefaultFriends(currentUsername)

        val friends = getFriends(currentUsername).toSet()
        val candidates = mutableMapOf<String, LeaderboardEntry>()

        // 1. Load community peers from SQLite
        val cursor = readableDatabase.rawQuery(
            "SELECT username, display_name, avatar_emoji, daily_streak, total_quizzes, total_score, accuracy_percent, dominant_trait, tier FROM community_learners",
            null
        )
        while (cursor.moveToNext()) {
            val u = cursor.getString(0)
            val cleanName = cursor.getString(1)
            val isUser = u.equals(currentUsername, ignoreCase = true)
            candidates[u] = LeaderboardEntry(
                rank = 0,
                username = u,
                displayName = if (isUser) "You" else cleanName,
                avatarEmoji = cursor.getString(2),
                dailyStreak = cursor.getInt(3),
                totalQuizzes = cursor.getInt(4),
                totalScore = cursor.getInt(5),
                accuracyPercent = cursor.getFloat(6),
                isCurrentUser = isUser,
                isFriend = friends.contains(u),
                dominantTrait = cursor.getString(7),
                tier = cursor.getString(8),
                rankDelta = 0
            )
        }
        cursor.close()

        // 2. Load and calculate current user's real SQLite stats
        val userStreak = calculateDailyStreak(currentUsername)
        val userStats = getQuizPerformanceStats(currentUsername)
        val userTelemetry = getTelemetry(currentUsername)
        val dominantTrait = when {
            userTelemetry.loginCount >= 10 -> "Conscientious"
            userTelemetry.lastQuizScore >= 80 -> "Understanding"
            userTelemetry.totalTimeMinutes >= 30 -> "Motivation"
            else -> "Engagement"
        }

        candidates[currentUsername] = LeaderboardEntry(
            rank = 0,
            username = currentUsername,
            displayName = "You",
            avatarEmoji = "🚀",
            dailyStreak = maxOf(userStreak, userStats.currentDailyStreak),
            totalQuizzes = userStats.totalQuizzes,
            totalScore = userStats.totalScore,
            accuracyPercent = userStats.averageAccuracyPercent,
            isCurrentUser = true,
            isFriend = false,
            dominantTrait = dominantTrait,
            tier = "Gold",
            rankDelta = +1
        )

        // 3. Filter by scope if FRIENDS
        val filtered = if (scope == LeaderboardScope.FRIENDS) {
            candidates.values.filter { it.isCurrentUser || it.isFriend }
        } else {
            candidates.values.toList()
        }

        // 4. Sort according to sortBy
        val comparator = when (sortBy) {
            LeaderboardSort.STREAK -> compareByDescending<LeaderboardEntry> { it.dailyStreak }
                .thenByDescending { it.totalScore }
                .thenByDescending { it.accuracyPercent }
            LeaderboardSort.SCORE -> compareByDescending<LeaderboardEntry> { it.totalScore }
                .thenByDescending { it.dailyStreak }
                .thenByDescending { it.accuracyPercent }
            LeaderboardSort.ACCURACY -> compareByDescending<LeaderboardEntry> { it.accuracyPercent }
                .thenByDescending { it.totalScore }
                .thenByDescending { it.dailyStreak }
        }

        val sorted = filtered.sortedWith(comparator)

        // 5. Assign rank (1-based) and tier
        sorted.mapIndexed { index, entry ->
            val rank = index + 1
            val tier = when {
                rank <= 3 -> "Diamond"
                rank <= 6 -> "Platinum"
                rank <= 10 -> "Gold"
                else -> "Silver"
            }
            entry.copy(rank = rank, tier = tier)
        }
    }

    suspend fun getDailyGoalProgress(username: String): DailyGoalProgress = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis

        // Count today's questions
        val cursorQuestions = readableDatabase.rawQuery(
            "SELECT SUM(total_questions) FROM quiz_attempts WHERE username = ? AND timestamp >= ?",
            arrayOf(username, startOfDay.toString())
        )
        val answeredToday = if (cursorQuestions.moveToFirst() && !cursorQuestions.isNull(0)) {
            cursorQuestions.getInt(0)
        } else {
            0
        }
        cursorQuestions.close()

        // Get target questions goal and target hours
        var targetQuestions = 10
        var targetHours = 1.0f
        try {
            val cursorGoal = readableDatabase.rawQuery(
                "SELECT target_questions, target_hours FROM daily_goals WHERE username = ?",
                arrayOf(username)
            )
            if (cursorGoal.moveToFirst()) {
                if (!cursorGoal.isNull(0)) targetQuestions = cursorGoal.getInt(0)
                if (!cursorGoal.isNull(1)) targetHours = cursorGoal.getFloat(1).coerceAtLeast(0.25f)
            }
            cursorGoal.close()
        } catch (_: Exception) {
            try {
                val cursorGoal = readableDatabase.rawQuery(
                    "SELECT target_questions FROM daily_goals WHERE username = ?",
                    arrayOf(username)
                )
                if (cursorGoal.moveToFirst() && !cursorGoal.isNull(0)) {
                    targetQuestions = cursorGoal.getInt(0)
                }
                cursorGoal.close()
            } catch (_: Exception) {}
        }

        // Count today's study sessions
        var studyDurationSeconds = 0L
        try {
            val cursorStudy = readableDatabase.rawQuery(
                "SELECT SUM(duration_seconds) FROM study_sessions WHERE username = ? AND timestamp >= ?",
                arrayOf(username, startOfDay.toString())
            )
            if (cursorStudy.moveToFirst() && !cursorStudy.isNull(0)) {
                studyDurationSeconds = cursorStudy.getLong(0)
            }
            cursorStudy.close()
        } catch (_: Exception) {}

        // Calculate time spent today: study sessions + active quiz answering (1.5 mins per question)
        val quizMinutes = answeredToday * 1.5f
        val studyMinutes = studyDurationSeconds / 60.0f
        val totalMinutesToday = quizMinutes + studyMinutes
        val hoursCompletedToday = totalMinutesToday / 60.0f

        val questionsPercent = if (targetQuestions > 0) {
            (answeredToday.toFloat() / targetQuestions.toFloat()).coerceIn(0f, 1f)
        } else {
            1f
        }

        val hoursPercent = if (targetHours > 0f) {
            (hoursCompletedToday / targetHours).coerceAtLeast(0f)
        } else {
            1f
        }

        val isAchieved = (hoursCompletedToday >= targetHours) || (answeredToday >= targetQuestions && answeredToday > 0)

        DailyGoalProgress(
            targetQuestions = targetQuestions,
            answeredToday = answeredToday,
            percentComplete = questionsPercent,
            isAchieved = isAchieved,
            targetHours = targetHours,
            hoursCompletedToday = hoursCompletedToday,
            minutesCompletedToday = totalMinutesToday,
            hoursPercentComplete = hoursPercent
        )
    }

    suspend fun setDailyGoal(username: String, targetQuestions: Int): DailyGoalProgress = withContext(Dispatchers.IO) {
        val validTarget = targetQuestions.coerceIn(3, 100)
        val existing = getDailyGoalProgress(username)
        val cv = ContentValues().apply {
            put("username", username)
            put("target_questions", validTarget)
            put("target_hours", existing.targetHours)
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("daily_goals", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        getDailyGoalProgress(username)
    }

    suspend fun setDailyTargetHours(username: String, targetHours: Float): DailyGoalProgress = withContext(Dispatchers.IO) {
        val validTargetHours = targetHours.coerceIn(0.25f, 12.0f)
        val existing = getDailyGoalProgress(username)
        val cv = ContentValues().apply {
            put("username", username)
            put("target_questions", existing.targetQuestions)
            put("target_hours", validTargetHours)
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("daily_goals", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        getDailyGoalProgress(username)
    }

    suspend fun getPeakLearningHoursAnalysis(username: String): com.example.dle_prototype.data.ml.PeakLearningHoursAnalysis = withContext(Dispatchers.IO) {
        val attempts = getRecentQuizAttempts(username, limit = 300)
        val sessions = getStudySessions(username, limit = 300)
        com.example.dle_prototype.data.ml.PeakLearningHoursAnalyzer.analyze(attempts, sessions)
    }

    // ----------------------------------------------------
    // DIGITAL ACHIEVEMENT BADGES DATA LAYER
    // ----------------------------------------------------

    suspend fun getUnlockedBadges(username: String): Map<String, Long> = withContext(Dispatchers.IO) {
        val map = mutableMapOf<String, Long>()
        try {
            val cursor = readableDatabase.rawQuery(
                "SELECT badge_id, unlocked_at FROM user_unlocked_badges WHERE username = ?",
                arrayOf(username)
            )
            cursor.use {
                val bIdx = it.getColumnIndexOrThrow("badge_id")
                val uIdx = it.getColumnIndexOrThrow("unlocked_at")
                while (it.moveToNext()) {
                    map[it.getString(bIdx)] = it.getLong(uIdx)
                }
            }
        } catch (_: Exception) {}
        map
    }

    suspend fun awardBadge(
        username: String,
        badgeId: String,
        xpAwarded: Int,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val cv = ContentValues().apply {
                put("username", username)
                put("badge_id", badgeId)
                put("unlocked_at", timestamp)
                put("xp_awarded", xpAwarded)
            }
            val rowId = writableDatabase.insertWithOnConflict(
                "user_unlocked_badges",
                null,
                cv,
                SQLiteDatabase.CONFLICT_IGNORE
            )
            if (rowId > 0) {
                val def = com.example.dle_prototype.data.badges.BadgeCatalog.getDefinition(badgeId)
                val badgeTitle = def?.title ?: "Milestone Badge"
                val historyCv = ContentValues().apply {
                    put("username", username)
                    put("title", "Badge Awarded: $badgeTitle")
                    put("description", "+$xpAwarded XP for unlocking ${def?.category?.displayName ?: "milestone"}")
                    put("fuel_change", 15)
                    put("xp_change", xpAwarded)
                    put("timestamp", timestamp)
                }
                writableDatabase.insert("reward_history", null, historyCv)
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun checkAndAwardBadges(username: String): com.example.dle_prototype.data.badges.BadgeEvaluationResult = withContext(Dispatchers.IO) {
        val streakData = getUserStreakData(username)
        val streak = streakData.currentStreak
        val longest = streakData.longestStreak
        val attempts = getRecentQuizAttempts(username, 300)
        val sessions = getStudySessions(username, 300)
        val dailyGoal = getDailyGoalProgress(username)
        val previouslyUnlocked = getUnlockedBadges(username)

        val evalResult = com.example.dle_prototype.data.badges.BadgeSystemEngine.evaluate(
            dailyStreak = streak,
            longestStreak = longest,
            quizAttempts = attempts,
            studySessions = sessions,
            dailyGoalProgress = dailyGoal,
            previouslyUnlockedMap = previouslyUnlocked
        )

        for (newBadge in evalResult.newlyAwardedBadges) {
            awardBadge(
                username = username,
                badgeId = newBadge.id,
                xpAwarded = newBadge.xpReward,
                timestamp = newBadge.unlockedAt ?: System.currentTimeMillis()
            )
        }

        evalResult
    }

    suspend fun getSpacedRepetitionOverview(username: String): com.example.dle_prototype.data.ml.SpacedRepetitionOverview = withContext(Dispatchers.IO) {
        val attempts = getAllQuizHistory(username, limit = 300)
        val flashcards = getAllFlashcards(username)
        com.example.dle_prototype.data.ml.SpacedRepetitionScheduler.scheduleTopics(
            quizAttempts = attempts,
            flashcards = flashcards,
            currentTimeMillis = System.currentTimeMillis()
        )
    }

    // ----------------------------------------------------
    // User Streak Data Access Layer
    // ----------------------------------------------------
    suspend fun getUserStreakData(username: String): UserStreakData = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val cursor = readableDatabase.rawQuery(
            "SELECT current_streak, longest_streak, last_active_date, total_days_active, last_milestone_reached, updated_at FROM user_streaks WHERE username = ?",
            arrayOf(username)
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val current = c.getInt(0)
                val longest = c.getInt(1)
                val lastDate = c.getString(2) ?: ""
                val totalDays = c.getInt(3)
                val lastMilestone = c.getInt(4)
                val updated = c.getLong(5)
                UserStreakData(
                    username = username,
                    currentStreak = current,
                    longestStreak = longest,
                    lastActiveDate = lastDate,
                    totalDaysActive = totalDays,
                    lastMilestoneReached = lastMilestone,
                    isStreakActiveToday = lastDate == todayStr,
                    updatedAt = updated
                )
            } else {
                val calculatedStreak = calculateDailyStreak(username)
                UserStreakData(
                    username = username,
                    currentStreak = calculatedStreak,
                    longestStreak = calculatedStreak,
                    lastActiveDate = todayStr,
                    totalDaysActive = 1,
                    lastMilestoneReached = 0,
                    isStreakActiveToday = true,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
    }

    suspend fun updateUserStreakOnQuizCompletion(username: String): UserStreakData = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

        val current = getUserStreakData(username)
        var newCurrentStreak = current.currentStreak
        var newLongest = current.longestStreak
        var newDaysActive = current.totalDaysActive

        if (current.lastActiveDate == todayStr) {
            // Already active today, maintain streak
        } else if (current.lastActiveDate == yesterdayStr) {
            newCurrentStreak += 1
            newDaysActive += 1
            if (newCurrentStreak > newLongest) {
                newLongest = newCurrentStreak
            }
        } else {
            newCurrentStreak = 1
            newDaysActive += 1
        }

        val cv = ContentValues().apply {
            put("username", username)
            put("current_streak", newCurrentStreak)
            put("longest_streak", newLongest)
            put("last_active_date", todayStr)
            put("total_days_active", newDaysActive)
            put("last_milestone_reached", current.lastMilestoneReached)
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("user_streaks", null, cv, SQLiteDatabase.CONFLICT_REPLACE)

        UserStreakData(
            username = username,
            currentStreak = newCurrentStreak,
            longestStreak = newLongest,
            lastActiveDate = todayStr,
            totalDaysActive = newDaysActive,
            lastMilestoneReached = current.lastMilestoneReached,
            isStreakActiveToday = true,
            updatedAt = System.currentTimeMillis()
        )
    }

    // ----------------------------------------------------
    // Adaptive Difficulty Historical Profile Engine
    // ----------------------------------------------------
    suspend fun getAdaptiveDifficultyProfile(username: String, categoryName: String): AdaptiveDifficultyProfile = withContext(Dispatchers.IO) {
        val catAttempts = mutableListOf<QuizAttempt>()
        val catCursor = readableDatabase.rawQuery(
            "SELECT id, username, category, score, total_questions, difficulty_level, timestamp FROM quiz_attempts WHERE username = ? AND category = ? ORDER BY timestamp DESC LIMIT 20",
            arrayOf(username, categoryName)
        )
        catCursor.use { c ->
            while (c.moveToNext()) {
                catAttempts.add(
                    QuizAttempt(
                        id = c.getLong(0),
                        username = c.getString(1),
                        category = c.getString(2),
                        score = c.getInt(3),
                        totalQuestions = c.getInt(4),
                        difficultyLevel = c.getFloat(5),
                        timestamp = c.getLong(6)
                    )
                )
            }
        }

        val allCursor = readableDatabase.rawQuery(
            "SELECT score, total_questions FROM quiz_attempts WHERE username = ? ORDER BY timestamp DESC LIMIT 50",
            arrayOf(username)
        )
        var totalGlobalQuestions = 0
        var totalGlobalCorrect = 0
        var globalQuizCount = 0
        allCursor.use { c ->
            while (c.moveToNext()) {
                globalQuizCount++
                val sc = c.getInt(0)
                val tot = c.getInt(1)
                totalGlobalCorrect += sc
                totalGlobalQuestions += tot
            }
        }
        val globalAccuracy = if (totalGlobalQuestions > 0) {
            (totalGlobalCorrect.toFloat() / totalGlobalQuestions.toFloat() * 100f).toInt()
        } else {
            70
        }

        var catQuestions = 0
        var catCorrect = 0
        catAttempts.forEach {
            catCorrect += it.score
            catQuestions += it.totalQuestions
        }
        val allTimeCatAccuracy = if (catQuestions > 0) {
            (catCorrect.toFloat() / catQuestions.toFloat() * 100f).toInt()
        } else {
            globalAccuracy
        }

        val recent5 = catAttempts.take(5)
        val recentAccuracy = if (recent5.isNotEmpty()) {
            val rCorrect = recent5.sumOf { it.score }
            val rTotal = recent5.sumOf { it.totalQuestions }
            if (rTotal > 0) (rCorrect.toFloat() / rTotal.toFloat() * 100f).toInt() else allTimeCatAccuracy
        } else {
            allTimeCatAccuracy
        }

        val streak = calculateDailyStreak(username)
        val streakBonus = (streak * 0.05f).coerceAtMost(0.25f)

        val initialTier: String
        val targetComplexity: Float
        val escalationThreshold: Int
        val reasoning: String

        when {
            recentAccuracy >= 80 -> {
                initialTier = "Hard"
                targetComplexity = (2.6f + streakBonus).coerceAtMost(3.0f)
                escalationThreshold = 1
                reasoning = "High historical category mastery ($recentAccuracy% accuracy). Calibrated to Hard tier with agile escalation."
            }
            recentAccuracy >= 60 -> {
                initialTier = "Medium"
                targetComplexity = (1.9f + streakBonus).coerceIn(1.5f, 2.4f)
                escalationThreshold = 2
                reasoning = "Solid historical foundation ($recentAccuracy% accuracy). Calibrated to Medium tier for balanced challenge."
            }
            else -> {
                initialTier = "Easy"
                targetComplexity = 1.2f
                escalationThreshold = 2
                reasoning = "Historical performance ($recentAccuracy% accuracy) suggests scaffolding foundational concepts in Easy tier."
            }
        }

        AdaptiveDifficultyProfile(
            category = categoryName,
            initialTier = initialTier,
            targetComplexityScore = targetComplexity,
            categoryAttemptsCount = catAttempts.size,
            recentAccuracyPercent = recentAccuracy,
            allTimeAccuracyPercent = allTimeCatAccuracy,
            globalQuizzesCount = globalQuizCount,
            globalAccuracyPercent = globalAccuracy,
            currentStreak = streak,
            momentumIndex = if (recentAccuracy >= 75) 0.5f else if (recentAccuracy >= 50) 0.1f else -0.3f,
            escalationThreshold = escalationThreshold,
            scaffoldingThreshold = 1,
            reasoning = reasoning
        )
    }

    // =========================================================================
    // GAMIFICATION, FUEL, CHESTS & REWARDS DAO
    // =========================================================================

    suspend fun getGamificationState(username: String): UserGamificationState = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.query(
            "user_gamification",
            null,
            "username = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val topicsStr = it.getString(it.getColumnIndexOrThrow("selected_topics"))
                val topics = if (topicsStr.isNullOrBlank()) listOf("HTML", "CSS", "JavaScript") else topicsStr.split(",")
                UserGamificationState(
                    username = username,
                    fuel = it.getInt(it.getColumnIndexOrThrow("fuel")),
                    totalXp = it.getInt(it.getColumnIndexOrThrow("total_xp")),
                    streakFreezes = it.getInt(it.getColumnIndexOrThrow("streak_freezes")),
                    altitudeKm = it.getFloat(it.getColumnIndexOrThrow("altitude_km")),
                    selectedSkin = it.getString(it.getColumnIndexOrThrow("selected_skin")),
                    selectedTheme = it.getString(it.getColumnIndexOrThrow("selected_theme")),
                    selectedFrame = it.getString(it.getColumnIndexOrThrow("selected_frame")),
                    onboardingCompleted = it.getInt(it.getColumnIndexOrThrow("onboarding_completed")) == 1,
                    selectedTopics = topics,
                    selectedSkillLevel = it.getString(it.getColumnIndexOrThrow("selected_skill_level")),
                    dailyGoalQuestions = it.getInt(it.getColumnIndexOrThrow("daily_goal_questions")),
                    currentLeague = it.getString(it.getColumnIndexOrThrow("current_league")),
                    leagueRank = it.getInt(it.getColumnIndexOrThrow("league_rank")),
                    unopenedChestsCount = getUnopenedChestsCount(username)
                )
            } else {
                // Initialize default
                val defaultState = UserGamificationState(username = username)
                val cv = ContentValues().apply {
                    put("username", username)
                    put("fuel", defaultState.fuel)
                    put("total_xp", defaultState.totalXp)
                    put("streak_freezes", defaultState.streakFreezes)
                    put("altitude_km", defaultState.altitudeKm)
                    put("selected_skin", defaultState.selectedSkin)
                    put("selected_theme", defaultState.selectedTheme)
                    put("selected_frame", defaultState.selectedFrame)
                    put("onboarding_completed", if (defaultState.onboardingCompleted) 1 else 0)
                    put("selected_topics", defaultState.selectedTopics.joinToString(","))
                    put("selected_skill_level", defaultState.selectedSkillLevel)
                    put("daily_goal_questions", defaultState.dailyGoalQuestions)
                    put("current_league", defaultState.currentLeague)
                    put("league_rank", defaultState.leagueRank)
                    put("updated_at", System.currentTimeMillis())
                }
                writableDatabase.insertWithOnConflict("user_gamification", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                // Add initial starter chest
                addStarterChest(username)
                defaultState
            }
        }
    }

    private fun addStarterChest(username: String) {
        val cv = ContentValues().apply {
            put("username", username)
            put("chest_type", "Mystery Starter Drop")
            put("is_opened", 0)
            put("reward_xp", 75)
            put("reward_fuel", 25)
            put("reward_badge", "Pioneer Capsule")
            put("reward_skin", "Cosmic Voyager")
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insert("chests", null, cv)
    }

    private fun getUnopenedChestsCount(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM chests WHERE username = ? AND is_opened = 0", arrayOf(username))
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    suspend fun addFuelAndXp(
        username: String,
        fuelDelta: Int,
        xpDelta: Int,
        altitudeDeltaKm: Float = 0.2f
    ): UserGamificationState = withContext(Dispatchers.IO) {
        val current = getGamificationState(username)
        val newFuel = (current.fuel + fuelDelta).coerceAtLeast(0)
        val newXp = (current.totalXp + xpDelta).coerceAtLeast(0)
        val newAltitude = (current.altitudeKm + altitudeDeltaKm).coerceAtLeast(0f)

        val cv = ContentValues().apply {
            put("fuel", newFuel)
            put("total_xp", newXp)
            put("altitude_km", newAltitude)
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update("user_gamification", cv, "username = ?", arrayOf(username))

        // Record in history if meaningful change
        if (fuelDelta != 0 || xpDelta != 0) {
            val historyCv = ContentValues().apply {
                put("username", username)
                put("title", if (fuelDelta > 0 || xpDelta > 0) "Quiz Session Rewards" else "Fuel Spent")
                put("description", "+$xpDelta XP, ${if (fuelDelta >= 0) "+$fuelDelta" else "$fuelDelta"} Fuel")
                put("fuel_change", fuelDelta)
                put("xp_change", xpDelta)
                put("timestamp", System.currentTimeMillis())
            }
            writableDatabase.insert("reward_history", null, historyCv)
        }

        current.copy(fuel = newFuel, totalXp = newXp, altitudeKm = newAltitude)
    }

    suspend fun buyStreakFreeze(username: String, cost: Int = 30): Boolean = withContext(Dispatchers.IO) {
        val current = getGamificationState(username)
        if (current.fuel < cost) return@withContext false

        val newFuel = current.fuel - cost
        val newFreezes = current.streakFreezes + 1
        val cv = ContentValues().apply {
            put("fuel", newFuel)
            put("streak_freezes", newFreezes)
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update("user_gamification", cv, "username = ?", arrayOf(username))

        val historyCv = ContentValues().apply {
            put("username", username)
            put("title", "Streak Freeze Acquired")
            put("description", "Purchased 1 Streak Freeze shield (-$cost Fuel)")
            put("fuel_change", -cost)
            put("xp_change", 0)
            put("timestamp", System.currentTimeMillis())
        }
        writableDatabase.insert("reward_history", null, historyCv)
        true
    }

    suspend fun getChests(username: String): List<MysteryChest> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MysteryChest>()
        val db = readableDatabase
        val cursor = db.query(
            "chests",
            null,
            "username = ?",
            arrayOf(username),
            null,
            null,
            "is_opened ASC, created_at DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    MysteryChest(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        username = it.getString(it.getColumnIndexOrThrow("username")),
                        chestType = it.getString(it.getColumnIndexOrThrow("chest_type")),
                        isOpened = it.getInt(it.getColumnIndexOrThrow("is_opened")) == 1,
                        rewardXp = it.getInt(it.getColumnIndexOrThrow("reward_xp")),
                        rewardFuel = it.getInt(it.getColumnIndexOrThrow("reward_fuel")),
                        rewardBadge = it.getString(it.getColumnIndexOrThrow("reward_badge")),
                        rewardSkin = it.getString(it.getColumnIndexOrThrow("reward_skin")),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        list
    }

    suspend fun openMysteryChest(chestId: Long): MysteryChest? = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val cursor = db.query("chests", null, "id = ?", arrayOf(chestId.toString()), null, null, null)
        val chest = cursor.use {
            if (it.moveToFirst()) {
                MysteryChest(
                    id = it.getLong(it.getColumnIndexOrThrow("id")),
                    username = it.getString(it.getColumnIndexOrThrow("username")),
                    chestType = it.getString(it.getColumnIndexOrThrow("chest_type")),
                    isOpened = it.getInt(it.getColumnIndexOrThrow("is_opened")) == 1,
                    rewardXp = it.getInt(it.getColumnIndexOrThrow("reward_xp")),
                    rewardFuel = it.getInt(it.getColumnIndexOrThrow("reward_fuel")),
                    rewardBadge = it.getString(it.getColumnIndexOrThrow("reward_badge")),
                    rewardSkin = it.getString(it.getColumnIndexOrThrow("reward_skin")),
                    createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                )
            } else null
        } ?: return@withContext null

        if (!chest.isOpened) {
            val cv = ContentValues().apply {
                put("is_opened", 1)
            }
            db.update("chests", cv, "id = ?", arrayOf(chestId.toString()))
            // Award rewards
            addFuelAndXp(chest.username, chest.rewardFuel, chest.rewardXp, 0.5f)
        }
        chest.copy(isOpened = true)
    }

    suspend fun addChest(
        username: String,
        type: String,
        rewardXp: Int,
        rewardFuel: Int,
        badge: String = "",
        skin: String = ""
    ) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("username", username)
            put("chest_type", type)
            put("is_opened", 0)
            put("reward_xp", rewardXp)
            put("reward_fuel", rewardFuel)
            put("reward_badge", badge)
            put("reward_skin", skin)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insert("chests", null, cv)
    }

    suspend fun getRewardHistory(username: String, limit: Int = 20): List<RewardHistoryItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<RewardHistoryItem>()
        val db = readableDatabase
        val cursor = db.query(
            "reward_history",
            null,
            "username = ?",
            arrayOf(username),
            null,
            null,
            "timestamp DESC",
            limit.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    RewardHistoryItem(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        username = it.getString(it.getColumnIndexOrThrow("username")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        description = it.getString(it.getColumnIndexOrThrow("description")),
                        fuelChange = it.getInt(it.getColumnIndexOrThrow("fuel_change")),
                        xpChange = it.getInt(it.getColumnIndexOrThrow("xp_change")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                    )
                )
            }
        }
        list
    }

    // =========================================================================
    // DUELS & COMPETE DAO
    // =========================================================================

    suspend fun getRecentDuels(username: String, limit: Int = 5): List<DuelMatchRecord> = withContext(Dispatchers.IO) {
        val list = mutableListOf<DuelMatchRecord>()
        val wdb = writableDatabase
        // Migrate legacy names to fictional names
        wdb.execSQL("UPDATE user_duels SET opponent_name = 'SnowyOwl3', opponent_avatar = '🦉' WHERE opponent_name IN ('Sarah Chen', 'Sarah', 'sarah_chen')")
        wdb.execSQL("UPDATE user_duels SET opponent_name = 'MartianTiger67', opponent_avatar = '🐯' WHERE opponent_name IN ('Alex Rivera', 'Alex', 'alex_rivera')")

        val cursor = wdb.query(
            "user_duels",
            null,
            "username = ?",
            arrayOf(username),
            null,
            null,
            "timestamp DESC",
            limit.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    DuelMatchRecord(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        username = it.getString(it.getColumnIndexOrThrow("username")),
                        opponentName = it.getString(it.getColumnIndexOrThrow("opponent_name")),
                        opponentAvatar = it.getString(it.getColumnIndexOrThrow("opponent_avatar")),
                        opponentTier = it.getString(it.getColumnIndexOrThrow("opponent_tier")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        userScore = it.getInt(it.getColumnIndexOrThrow("user_score")),
                        opponentScore = it.getInt(it.getColumnIndexOrThrow("opponent_score")),
                        isWin = it.getInt(it.getColumnIndexOrThrow("is_win")) == 1,
                        xpEarned = it.getInt(it.getColumnIndexOrThrow("xp_earned")),
                        fuelEarned = it.getInt(it.getColumnIndexOrThrow("fuel_earned")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                    )
                )
            }
        }
        if (list.isEmpty()) {
            // Seed sample duels with fictional opponent names
            val seeded = listOf(
                DuelMatchRecord(
                    username = username,
                    opponentName = "SnowyOwl3",
                    opponentAvatar = "🦉",
                    opponentTier = "Diamond",
                    category = "JavaScript",
                    userScore = 8,
                    opponentScore = 7,
                    isWin = true,
                    xpEarned = 45,
                    fuelEarned = 15,
                    timestamp = System.currentTimeMillis() - 86400000L
                ),
                DuelMatchRecord(
                    username = username,
                    opponentName = "MartianTiger67",
                    opponentAvatar = "🐯",
                    opponentTier = "Diamond",
                    category = "Python",
                    userScore = 6,
                    opponentScore = 7,
                    isWin = false,
                    xpEarned = 15,
                    fuelEarned = 5,
                    timestamp = System.currentTimeMillis() - 172800000L
                )
            )
            seeded.forEach { d ->
                val cv = ContentValues().apply {
                    put("username", d.username)
                    put("opponent_name", d.opponentName)
                    put("opponent_avatar", d.opponentAvatar)
                    put("opponent_tier", d.opponentTier)
                    put("category", d.category)
                    put("user_score", d.userScore)
                    put("opponent_score", d.opponentScore)
                    put("is_win", if (d.isWin) 1 else 0)
                    put("xp_earned", d.xpEarned)
                    put("fuel_earned", d.fuelEarned)
                    put("timestamp", d.timestamp)
                }
                writableDatabase.insert("user_duels", null, cv)
            }
            return@withContext seeded
        }
        list
    }

    suspend fun recordDuel(
        username: String,
        opponentName: String,
        opponentAvatar: String,
        category: String,
        userScore: Int,
        opponentScore: Int,
        isWin: Boolean,
        xp: Int,
        fuel: Int
    ): DuelMatchRecord = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("username", username)
            put("opponent_name", opponentName)
            put("opponent_avatar", opponentAvatar)
            put("opponent_tier", if (isWin) "Gold" else "Platinum")
            put("category", category)
            put("user_score", userScore)
            put("opponent_score", opponentScore)
            put("is_win", if (isWin) 1 else 0)
            put("xp_earned", xp)
            put("fuel_earned", fuel)
            put("timestamp", System.currentTimeMillis())
        }
        val id = writableDatabase.insert("user_duels", null, cv)
        addFuelAndXp(username, fuel, xp, 0.4f)
        DuelMatchRecord(
            id = id,
            username = username,
            opponentName = opponentName,
            opponentAvatar = opponentAvatar,
            category = category,
            userScore = userScore,
            opponentScore = opponentScore,
            isWin = isWin,
            xpEarned = xp,
            fuelEarned = fuel
        )
    }

    // =========================================================================
    // WELLBEING & ONBOARDING DAO
    // =========================================================================

    suspend fun getWellbeingSettings(username: String): WellbeingSettings = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.query(
            "wellbeing_settings",
            null,
            "username = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                WellbeingSettings(
                    username = username,
                    dailyTimeCapMinutes = it.getInt(it.getColumnIndexOrThrow("daily_time_cap_minutes")),
                    quietHoursEnabled = it.getInt(it.getColumnIndexOrThrow("quiet_hours_enabled")) == 1,
                    quietStartHour = it.getInt(it.getColumnIndexOrThrow("quiet_start_hour")),
                    quietEndHour = it.getInt(it.getColumnIndexOrThrow("quiet_end_hour")),
                    softStopReminderEnabled = it.getInt(it.getColumnIndexOrThrow("soft_stop_reminder_enabled")) == 1,
                    soundEnabled = it.getInt(it.getColumnIndexOrThrow("sound_enabled")) == 1,
                    hapticsEnabled = it.getInt(it.getColumnIndexOrThrow("haptics_enabled")) == 1,
                    learningStyle = it.getString(it.getColumnIndexOrThrow("learning_style"))
                )
            } else {
                val defaults = WellbeingSettings(username = username)
                saveWellbeingSettings(defaults)
                defaults
            }
        }
    }

    suspend fun saveWellbeingSettings(settings: WellbeingSettings) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("username", settings.username)
            put("daily_time_cap_minutes", settings.dailyTimeCapMinutes)
            put("quiet_hours_enabled", if (settings.quietHoursEnabled) 1 else 0)
            put("quiet_start_hour", settings.quietStartHour)
            put("quiet_end_hour", settings.quietEndHour)
            put("soft_stop_reminder_enabled", if (settings.softStopReminderEnabled) 1 else 0)
            put("sound_enabled", if (settings.soundEnabled) 1 else 0)
            put("haptics_enabled", if (settings.hapticsEnabled) 1 else 0)
            put("learning_style", settings.learningStyle)
        }
        writableDatabase.insertWithOnConflict("wellbeing_settings", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun saveOnboardingPreferences(
        username: String,
        topics: List<String>,
        skillLevel: String,
        dailyGoalQuestions: Int
    ) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("selected_topics", topics.joinToString(","))
            put("selected_skill_level", skillLevel)
            put("daily_goal_questions", dailyGoalQuestions)
            put("onboarding_completed", 1)
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update("user_gamification", cv, "username = ?", arrayOf(username))
        setDailyGoal(username, dailyGoalQuestions)
    }

    // =========================================================================
    // USER PERFORMANCE DATA ACCESS LAYER (CRUD)
    // =========================================================================
    suspend fun createUserPerformance(performance: UserPerformance): Long = saveUserPerformance(performance)

    suspend fun saveUserPerformance(performance: UserPerformance): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put(COL_PERF_USERNAME, performance.username)
            put(COL_PERF_QUIZ_SCORE, performance.quizScore)
            put(COL_PERF_TIME_SPENT, performance.timeSpentMinutes)
            put(COL_PERF_LOGIN_FREQUENCY, performance.loginFrequency)
            put(COL_PERF_DIFFICULTY_REACHED, performance.difficultyReached)
            put(COL_PERF_CATEGORY_SELECTED, performance.categorySelected)
            put(COL_PERF_RETENTION_RATE, performance.retentionRate)
            put(COL_PERF_LEARNING_VELOCITY, performance.learningVelocity)
            put(COL_PERF_LAST_UPDATED, performance.lastUpdated)
        }
        writableDatabase.insert("dle_data", null, cv)
        writableDatabase.insert(TABLE_USER_PERFORMANCE, null, cv)
    }

    suspend fun getUserPerformance(username: String): UserPerformance? = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT $COL_PERF_ID, $COL_PERF_USERNAME, $COL_PERF_QUIZ_SCORE, $COL_PERF_TIME_SPENT, $COL_PERF_LOGIN_FREQUENCY, $COL_PERF_DIFFICULTY_REACHED, $COL_PERF_CATEGORY_SELECTED, $COL_PERF_RETENTION_RATE, $COL_PERF_LEARNING_VELOCITY, $COL_PERF_LAST_UPDATED 
            FROM $TABLE_USER_PERFORMANCE 
            WHERE $COL_PERF_USERNAME = ? 
            ORDER BY $COL_PERF_LAST_UPDATED DESC LIMIT 1
            """.trimIndent(),
            arrayOf(username)
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                UserPerformance(
                    id = c.getLong(0),
                    username = c.getString(1),
                    quizScore = c.getFloat(2),
                    timeSpentMinutes = c.getFloat(3),
                    loginFrequency = c.getFloat(4),
                    difficultyReached = c.getFloat(5),
                    categorySelected = c.getFloat(6),
                    retentionRate = c.getFloat(7),
                    learningVelocity = c.getFloat(8),
                    lastUpdated = c.getLong(9)
                )
            } else {
                val dleCursor = readableDatabase.rawQuery(
                    "SELECT id, username, quiz_score, time_spent, login_frequency, difficulty_reached, category_selected, last_updated FROM dle_data WHERE username = ? ORDER BY id DESC LIMIT 1",
                    arrayOf(username)
                )
                dleCursor.use { dc ->
                    if (dc.moveToFirst()) {
                        UserPerformance(
                            id = dc.getLong(0),
                            username = dc.getString(1),
                            quizScore = dc.getFloat(2),
                            timeSpentMinutes = dc.getFloat(3),
                            loginFrequency = dc.getFloat(4),
                            difficultyReached = dc.getFloat(5),
                            categorySelected = dc.getFloat(6),
                            lastUpdated = dc.getLong(7)
                        )
                    } else null
                }
            }
        }
    }

    suspend fun getUserPerformanceById(id: Long): UserPerformance? = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT $COL_PERF_ID, $COL_PERF_USERNAME, $COL_PERF_QUIZ_SCORE, $COL_PERF_TIME_SPENT, $COL_PERF_LOGIN_FREQUENCY, $COL_PERF_DIFFICULTY_REACHED, $COL_PERF_CATEGORY_SELECTED, $COL_PERF_RETENTION_RATE, $COL_PERF_LEARNING_VELOCITY, $COL_PERF_LAST_UPDATED 
            FROM $TABLE_USER_PERFORMANCE 
            WHERE $COL_PERF_ID = ?
            """.trimIndent(),
            arrayOf(id.toString())
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                UserPerformance(
                    id = c.getLong(0),
                    username = c.getString(1),
                    quizScore = c.getFloat(2),
                    timeSpentMinutes = c.getFloat(3),
                    loginFrequency = c.getFloat(4),
                    difficultyReached = c.getFloat(5),
                    categorySelected = c.getFloat(6),
                    retentionRate = c.getFloat(7),
                    learningVelocity = c.getFloat(8),
                    lastUpdated = c.getLong(9)
                )
            } else null
        }
    }

    suspend fun getAllUserPerformance(username: String): List<UserPerformance> = withContext(Dispatchers.IO) {
        val list = mutableListOf<UserPerformance>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT $COL_PERF_ID, $COL_PERF_USERNAME, $COL_PERF_QUIZ_SCORE, $COL_PERF_TIME_SPENT, $COL_PERF_LOGIN_FREQUENCY, $COL_PERF_DIFFICULTY_REACHED, $COL_PERF_CATEGORY_SELECTED, $COL_PERF_RETENTION_RATE, $COL_PERF_LEARNING_VELOCITY, $COL_PERF_LAST_UPDATED 
            FROM $TABLE_USER_PERFORMANCE 
            WHERE $COL_PERF_USERNAME = ? 
            ORDER BY $COL_PERF_LAST_UPDATED DESC
            """.trimIndent(),
            arrayOf(username)
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    UserPerformance(
                        id = c.getLong(0),
                        username = c.getString(1),
                        quizScore = c.getFloat(2),
                        timeSpentMinutes = c.getFloat(3),
                        loginFrequency = c.getFloat(4),
                        difficultyReached = c.getFloat(5),
                        categorySelected = c.getFloat(6),
                        retentionRate = c.getFloat(7),
                        learningVelocity = c.getFloat(8),
                        lastUpdated = c.getLong(9)
                    )
                )
            }
        }
        list
    }

    suspend fun updateUserPerformance(performance: UserPerformance): Boolean = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put(COL_PERF_QUIZ_SCORE, performance.quizScore)
            put(COL_PERF_TIME_SPENT, performance.timeSpentMinutes)
            put(COL_PERF_LOGIN_FREQUENCY, performance.loginFrequency)
            put(COL_PERF_DIFFICULTY_REACHED, performance.difficultyReached)
            put(COL_PERF_CATEGORY_SELECTED, performance.categorySelected)
            put(COL_PERF_RETENTION_RATE, performance.retentionRate)
            put(COL_PERF_LEARNING_VELOCITY, performance.learningVelocity)
            put(COL_PERF_LAST_UPDATED, System.currentTimeMillis())
        }
        val rows = writableDatabase.update(TABLE_USER_PERFORMANCE, cv, "$COL_PERF_ID = ?", arrayOf(performance.id.toString()))
        rows > 0
    }

    suspend fun deleteUserPerformance(id: Long): Boolean = withContext(Dispatchers.IO) {
        val rows = writableDatabase.delete(TABLE_USER_PERFORMANCE, "$COL_PERF_ID = ?", arrayOf(id.toString()))
        rows > 0
    }

    suspend fun clearUserPerformance(username: String): Int = withContext(Dispatchers.IO) {
        writableDatabase.delete(TABLE_USER_PERFORMANCE, "$COL_PERF_USERNAME = ?", arrayOf(username))
    }

    // =========================================================================
    // QUIZ HISTORY DATA ACCESS LAYER (CRUD)
    // =========================================================================
    suspend fun createQuizHistory(history: QuizHistory): Long = recordQuizHistory(history)

    suspend fun recordQuizHistory(history: QuizHistory): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put(COL_HISTORY_USERNAME, history.username)
            put(COL_HISTORY_CATEGORY, history.category)
            put(COL_HISTORY_SCORE, history.score)
            put(COL_HISTORY_TOTAL_QUESTIONS, history.totalQuestions)
            put(COL_HISTORY_ACCURACY_PERCENT, history.accuracyPercent)
            put(COL_HISTORY_ELAPSED_TIME, history.elapsedTimeSeconds)
            put(COL_HISTORY_MISSED_INDICES, history.missedQuestionsIndicesJson)
            put(COL_HISTORY_DIFFICULTY, history.difficultyLevel)
            put(COL_HISTORY_TIMESTAMP, history.timestamp)
        }
        val attemptCv = ContentValues().apply {
            put("username", history.username)
            put("category", history.category)
            put("score", history.score)
            put("total_questions", history.totalQuestions)
            put("difficulty_level", history.difficultyLevel)
            put("timestamp", history.timestamp)
        }
        writableDatabase.insert("quiz_attempts", null, attemptCv)
        writableDatabase.insert(TABLE_QUIZ_HISTORY, null, cv)
    }

    suspend fun getQuizHistoryById(id: Long): QuizHistory? = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT $COL_HISTORY_ID, $COL_HISTORY_USERNAME, $COL_HISTORY_CATEGORY, $COL_HISTORY_SCORE, $COL_HISTORY_TOTAL_QUESTIONS, $COL_HISTORY_ACCURACY_PERCENT, $COL_HISTORY_ELAPSED_TIME, $COL_HISTORY_MISSED_INDICES, $COL_HISTORY_DIFFICULTY, $COL_HISTORY_TIMESTAMP 
            FROM $TABLE_QUIZ_HISTORY 
            WHERE $COL_HISTORY_ID = ?
            """.trimIndent(),
            arrayOf(id.toString())
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                QuizHistory(
                    id = c.getLong(0),
                    username = c.getString(1),
                    category = c.getString(2),
                    score = c.getInt(3),
                    totalQuestions = c.getInt(4),
                    accuracyPercent = c.getFloat(5),
                    elapsedTimeSeconds = c.getInt(6),
                    missedQuestionsIndicesJson = c.getString(7) ?: "[]",
                    difficultyLevel = c.getFloat(8),
                    timestamp = c.getLong(9)
                )
            } else null
        }
    }

    suspend fun getQuizHistoryList(username: String, limit: Int = 50): List<QuizHistory> = withContext(Dispatchers.IO) {
        val list = mutableListOf<QuizHistory>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT $COL_HISTORY_ID, $COL_HISTORY_USERNAME, $COL_HISTORY_CATEGORY, $COL_HISTORY_SCORE, $COL_HISTORY_TOTAL_QUESTIONS, $COL_HISTORY_ACCURACY_PERCENT, $COL_HISTORY_ELAPSED_TIME, $COL_HISTORY_MISSED_INDICES, $COL_HISTORY_DIFFICULTY, $COL_HISTORY_TIMESTAMP 
            FROM $TABLE_QUIZ_HISTORY 
            WHERE $COL_HISTORY_USERNAME = ? 
            ORDER BY $COL_HISTORY_TIMESTAMP DESC LIMIT ?
            """.trimIndent(),
            arrayOf(username, limit.toString())
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    QuizHistory(
                        id = c.getLong(0),
                        username = c.getString(1),
                        category = c.getString(2),
                        score = c.getInt(3),
                        totalQuestions = c.getInt(4),
                        accuracyPercent = c.getFloat(5),
                        elapsedTimeSeconds = c.getInt(6),
                        missedQuestionsIndicesJson = c.getString(7) ?: "[]",
                        difficultyLevel = c.getFloat(8),
                        timestamp = c.getLong(9)
                    )
                )
            }
        }
        if (list.isEmpty()) {
            val attempts = getAllQuizHistory(username, limit)
            attempts.map { att ->
                QuizHistory(
                    id = att.id,
                    username = att.username,
                    category = att.category,
                    score = att.score,
                    totalQuestions = att.totalQuestions,
                    accuracyPercent = if (att.totalQuestions > 0) (att.score.toFloat() / att.totalQuestions) * 100f else 0f,
                    difficultyLevel = att.difficultyLevel,
                    timestamp = att.timestamp
                )
            }
        } else {
            list
        }
    }

    suspend fun getQuizHistoryByCategory(username: String, category: String, limit: Int = 50): List<QuizHistory> = withContext(Dispatchers.IO) {
        val list = mutableListOf<QuizHistory>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT $COL_HISTORY_ID, $COL_HISTORY_USERNAME, $COL_HISTORY_CATEGORY, $COL_HISTORY_SCORE, $COL_HISTORY_TOTAL_QUESTIONS, $COL_HISTORY_ACCURACY_PERCENT, $COL_HISTORY_ELAPSED_TIME, $COL_HISTORY_MISSED_INDICES, $COL_HISTORY_DIFFICULTY, $COL_HISTORY_TIMESTAMP 
            FROM $TABLE_QUIZ_HISTORY 
            WHERE $COL_HISTORY_USERNAME = ? AND $COL_HISTORY_CATEGORY = ? 
            ORDER BY $COL_HISTORY_TIMESTAMP DESC LIMIT ?
            """.trimIndent(),
            arrayOf(username, category, limit.toString())
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    QuizHistory(
                        id = c.getLong(0),
                        username = c.getString(1),
                        category = c.getString(2),
                        score = c.getInt(3),
                        totalQuestions = c.getInt(4),
                        accuracyPercent = c.getFloat(5),
                        elapsedTimeSeconds = c.getInt(6),
                        missedQuestionsIndicesJson = c.getString(7) ?: "[]",
                        difficultyLevel = c.getFloat(8),
                        timestamp = c.getLong(9)
                    )
                )
            }
        }
        list
    }

    suspend fun updateQuizHistory(history: QuizHistory): Boolean = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put(COL_HISTORY_CATEGORY, history.category)
            put(COL_HISTORY_SCORE, history.score)
            put(COL_HISTORY_TOTAL_QUESTIONS, history.totalQuestions)
            put(COL_HISTORY_ACCURACY_PERCENT, history.accuracyPercent)
            put(COL_HISTORY_ELAPSED_TIME, history.elapsedTimeSeconds)
            put(COL_HISTORY_MISSED_INDICES, history.missedQuestionsIndicesJson)
            put(COL_HISTORY_DIFFICULTY, history.difficultyLevel)
            put(COL_HISTORY_TIMESTAMP, history.timestamp)
        }
        val rows = writableDatabase.update(TABLE_QUIZ_HISTORY, cv, "$COL_HISTORY_ID = ?", arrayOf(history.id.toString()))
        rows > 0
    }

    suspend fun deleteQuizHistory(id: Long): Boolean = withContext(Dispatchers.IO) {
        val rows = writableDatabase.delete(TABLE_QUIZ_HISTORY, "$COL_HISTORY_ID = ?", arrayOf(id.toString()))
        rows > 0
    }

    suspend fun clearQuizHistory(username: String): Int = withContext(Dispatchers.IO) {
        writableDatabase.delete(TABLE_QUIZ_HISTORY, "$COL_HISTORY_USERNAME = ?", arrayOf(username))
    }

    // =========================================================================
    // STREAK DATA ACCESS LAYER (CRUD)
    // =========================================================================
    suspend fun createStreakData(streak: StreakData): Boolean = saveStreakData(streak)

    suspend fun saveStreakData(streak: StreakData): Boolean = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put(COL_STREAK_USERNAME, streak.username)
            put(COL_STREAK_CURRENT, streak.currentStreak)
            put(COL_STREAK_LONGEST, streak.longestStreak)
            put(COL_STREAK_LAST_ACTIVE, streak.lastActiveDate)
            put(COL_STREAK_TOTAL_DAYS, streak.totalDaysActive)
            put(COL_STREAK_LAST_MILESTONE, streak.lastMilestoneReached)
            put(COL_STREAK_UPDATED_AT, streak.updatedAt)
        }
        writableDatabase.insertWithOnConflict("user_streaks", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        val rowId = writableDatabase.insertWithOnConflict(TABLE_STREAK_DATA, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        rowId != -1L
    }

    suspend fun getStreakData(username: String): StreakData = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            "SELECT $COL_STREAK_CURRENT, $COL_STREAK_LONGEST, $COL_STREAK_LAST_ACTIVE, $COL_STREAK_TOTAL_DAYS, $COL_STREAK_LAST_MILESTONE, $COL_STREAK_UPDATED_AT FROM $TABLE_STREAK_DATA WHERE $COL_STREAK_USERNAME = ?",
            arrayOf(username)
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val lastDate = c.getString(2) ?: ""
                StreakData(
                    username = username,
                    currentStreak = c.getInt(0),
                    longestStreak = c.getInt(1),
                    lastActiveDate = lastDate,
                    totalDaysActive = c.getInt(3),
                    lastMilestoneReached = c.getInt(4),
                    isStreakActiveToday = lastDate == todayStr,
                    updatedAt = c.getLong(5)
                )
            } else {
                val existing = getUserStreakData(username)
                val sd = StreakData(
                    username = existing.username,
                    currentStreak = existing.currentStreak,
                    longestStreak = existing.longestStreak,
                    lastActiveDate = existing.lastActiveDate,
                    totalDaysActive = existing.totalDaysActive,
                    lastMilestoneReached = existing.lastMilestoneReached,
                    isStreakActiveToday = existing.isStreakActiveToday,
                    updatedAt = existing.updatedAt
                )
                saveStreakData(sd)
                sd
            }
        }
    }

    suspend fun updateStreakData(streak: StreakData): Boolean = saveStreakData(streak)

    suspend fun updateStreakOnActivity(username: String): StreakData = withContext(Dispatchers.IO) {
        val updated = updateUserStreakOnQuizCompletion(username)
        val sd = StreakData(
            username = updated.username,
            currentStreak = updated.currentStreak,
            longestStreak = updated.longestStreak,
            lastActiveDate = updated.lastActiveDate,
            totalDaysActive = updated.totalDaysActive,
            lastMilestoneReached = updated.lastMilestoneReached,
            isStreakActiveToday = updated.isStreakActiveToday,
            updatedAt = updated.updatedAt
        )
        saveStreakData(sd)
        sd
    }

    suspend fun resetStreakData(username: String): Boolean = withContext(Dispatchers.IO) {
        val reset = StreakData(
            username = username,
            currentStreak = 0,
            longestStreak = getStreakData(username).longestStreak,
            lastActiveDate = "",
            totalDaysActive = getStreakData(username).totalDaysActive,
            lastMilestoneReached = 0,
            isStreakActiveToday = false,
            updatedAt = System.currentTimeMillis()
        )
        saveStreakData(reset)
    }

    suspend fun deleteStreakData(username: String): Boolean = withContext(Dispatchers.IO) {
        writableDatabase.delete("user_streaks", "username = ?", arrayOf(username))
        val rows = writableDatabase.delete(TABLE_STREAK_DATA, "$COL_STREAK_USERNAME = ?", arrayOf(username))
        rows > 0
    }

    // -------------------------------------------------------------
    // QA STUDY CHAT PERSISTENCE METHODS
    // -------------------------------------------------------------
    suspend fun insertQaChatMessage(
        username: String,
        sender: String,
        message: String,
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("username", username)
            put("sender", sender)
            put("message", message)
            put("timestamp", timestamp)
        }
        writableDatabase.insert("qa_chat_messages", null, values)
    }

    suspend fun getQaChatHistory(
        username: String,
        limit: Int = 100
    ): List<QaChatMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<QaChatMessage>()
        readableDatabase.rawQuery(
            """
            SELECT id, username, sender, message, timestamp
            FROM qa_chat_messages
            WHERE username = ?
            ORDER BY timestamp ASC, id ASC
            LIMIT ?
            """.trimIndent(),
            arrayOf(username, limit.toString())
        ).use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow("id")
            val userCol = cursor.getColumnIndexOrThrow("username")
            val senderCol = cursor.getColumnIndexOrThrow("sender")
            val msgCol = cursor.getColumnIndexOrThrow("message")
            val timeCol = cursor.getColumnIndexOrThrow("timestamp")

            while (cursor.moveToNext()) {
                messages.add(
                    QaChatMessage(
                        id = cursor.getLong(idCol),
                        username = cursor.getString(userCol),
                        sender = cursor.getString(senderCol),
                        message = cursor.getString(msgCol),
                        timestamp = cursor.getLong(timeCol)
                    )
                )
            }
        }
        messages
    }

    suspend fun clearQaChatHistory(username: String): Boolean = withContext(Dispatchers.IO) {
        val rows = writableDatabase.delete("qa_chat_messages", "username = ?", arrayOf(username))
        rows > 0
    }

    suspend fun getQaChatMessageCount(username: String): Int = withContext(Dispatchers.IO) {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM qa_chat_messages WHERE username = ?",
            arrayOf(username)
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }
}


