package com.example.dle_prototype.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.TrainingCheckpoint
import com.example.dle_prototype.data.ml.TrainingSample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "app.db"
        const val DATABASE_VERSION = 4

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

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

    suspend fun clearUserData(username: String) = withContext(Dispatchers.IO) {
        val args = arrayOf(username)
        writableDatabase.delete("dle_data", "username = ?", args)
        writableDatabase.delete("quiz_attempts", "username = ?", args)
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

    suspend fun saveTrainingCheckpoint(checkpoint: TrainingCheckpoint) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
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
        writableDatabase.insertWithOnConflict("training_checkpoints", null, values, SQLiteDatabase.CONFLICT_REPLACE)
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
}
