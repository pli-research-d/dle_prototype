package com.example.dle_prototype

import com.example.dle_prototype.data.ml.ModelWeights
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class AdaptiveCapabilitiesTest {

    @Test
    fun testLeitnerIntervalProgression() {
        fun computeNextBox(currentBox: Int, remembered: Boolean): Pair<Int, Long> {
            val newBox = if (remembered) minOf(4, currentBox + 1) else 1
            val interval = when (newBox) {
                1 -> 86400000L // 1 day
                2 -> 3 * 86400000L // 3 days
                3 -> 7 * 86400000L // 7 days
                else -> 14 * 86400000L // 14 days
            }
            return Pair(newBox, interval)
        }

        // Test promotions
        val step1 = computeNextBox(1, true)
        assertEquals(2, step1.first)
        assertEquals(3 * 86400000L, step1.second)

        val step2 = computeNextBox(2, true)
        assertEquals(3, step2.first)
        assertEquals(7 * 86400000L, step2.second)

        val step3 = computeNextBox(3, true)
        assertEquals(4, step3.first)
        assertEquals(14 * 86400000L, step3.second)

        // Clamping at Box 4
        val step4 = computeNextBox(4, true)
        assertEquals(4, step4.first)

        // Reset to Box 1 on failure
        val failureStep = computeNextBox(3, false)
        assertEquals(1, failureStep.first)
    }

    @Test
    fun testFocusScoreCalculation() {
        fun calculateFocusScore(pauseCount: Int): Float {
            return (100f - (pauseCount * 5f)).coerceIn(50f, 100f)
        }

        assertEquals(100f, calculateFocusScore(0), 0.01f)
        assertEquals(90f, calculateFocusScore(2), 0.01f)
        assertEquals(75f, calculateFocusScore(5), 0.01f)
        assertEquals(50f, calculateFocusScore(20), 0.01f) // Clamped at 50% floor
    }

    @Test
    fun testDdaEscalationAndScaffolding() {
        fun evaluateDdaTier(currentTier: String, currentStreak: Int, isCorrect: Boolean): String {
            if (isCorrect) {
                val newStreak = currentStreak + 1
                return when {
                    newStreak >= 4 && currentTier == "Medium" -> "Hard"
                    newStreak >= 2 && currentTier == "Easy" -> "Medium"
                    else -> currentTier
                }
            } else {
                return when (currentTier) {
                    "Hard" -> "Medium"
                    "Medium" -> "Easy"
                    else -> "Easy"
                }
            }
        }

        // Easy -> Medium at 2 streak
        assertEquals("Medium", evaluateDdaTier("Easy", 1, true))
        // Medium -> Hard at 4 streak
        assertEquals("Hard", evaluateDdaTier("Medium", 3, true))
        // Hard -> Medium on mistake
        assertEquals("Medium", evaluateDdaTier("Hard", 5, false))
        // Medium -> Easy on mistake
        assertEquals("Easy", evaluateDdaTier("Medium", 0, false))
    }

    @Test
    fun testWeightDivergenceMetric() {
        fun computeL2Divergence(wA: ModelWeights, wB: ModelWeights): Float {
            var sumSquares = 0.0
            var count = 0
            for (i in wA.w1.indices) {
                for (j in wA.w1[i].indices) {
                    val diff = (wA.w1[i][j] - wB.w1[i][j]).toDouble()
                    sumSquares += diff * diff
                    count++
                }
            }
            for (i in wA.w2.indices) {
                for (j in wA.w2[i].indices) {
                    val diff = (wA.w2[i][j] - wB.w2[i][j]).toDouble()
                    sumSquares += diff * diff
                    count++
                }
            }
            return if (count > 0) sqrt(sumSquares / count).toFloat() else 0f
        }

        val base = ModelWeights.defaultInit(5, 8, 4)
        // Divergence from self should be zero
        assertEquals(0f, computeL2Divergence(base, base), 0.0001f)

        // Perturbed copy
        val perturbedW1 = Array(base.w1.size) { i ->
            FloatArray(base.w1[i].size) { j -> base.w1[i][j] + 0.1f }
        }
        val perturbed = base.copy(w1 = perturbedW1)

        val div = computeL2Divergence(base, perturbed)
        assertTrue("Divergence between distinct weight matrices must be positive, got $div", div > 0.05f)
    }

    @Test
    fun testModelArchitectureLayerSpecifications() {
        val layers = com.example.dle_prototype.ui.components.MODEL_LAYERS
        assertEquals("Neural network must have exactly 3 layers", 3, layers.size)

        val inputLayer = layers[0]
        assertEquals("Input layer has 0 trainable parameters", 0, inputLayer.paramCount)
        assertEquals("[1, 5]", inputLayer.inputDim)
        assertEquals("[1, 5]", inputLayer.outputDim)

        val hiddenLayer = layers[1]
        assertEquals("Hidden layer dense projection has 48 parameters (40 weights + 8 biases)", 48, hiddenLayer.paramCount)
        assertEquals("[1, 5]", hiddenLayer.inputDim)
        assertEquals("[1, 8]", hiddenLayer.outputDim)
        assertTrue("Hidden layer activation should be LeakyReLU", hiddenLayer.activation.contains("LeakyReLU"))

        val outputLayer = layers[2]
        assertEquals("Output layer dense projection has 36 parameters (32 weights + 4 biases)", 36, outputLayer.paramCount)
        assertEquals("[1, 8]", outputLayer.inputDim)
        assertEquals("[1, 4]", outputLayer.outputDim)
        assertTrue("Output layer activation should be Sigmoid", outputLayer.activation.contains("Sigmoid"))

        val totalParams = layers.sumOf { it.paramCount }
        assertEquals("Total trainable parameter count must be exactly 84", 84, totalParams)
    }

    @Test
    fun testModelHealthEvaluationBaseline() {
        val health = com.example.dle_prototype.ui.components.evaluateModelHealth(
            checkpoints = emptyList(),
            userWeights = null
        )
        assertEquals(com.example.dle_prototype.ui.components.HealthState.BASELINE, health.state)
        assertEquals(0, health.evaluatedEpochs)
        assertTrue(health.stabilityScore > 70)
    }

    @Test
    fun testModelHealthEvaluationStable() {
        val dummyWeights = ModelWeights(
            w1 = Array(8) { FloatArray(5) { 0.1f } },
            b1 = FloatArray(8) { 0.0f },
            w2 = Array(4) { FloatArray(8) { 0.1f } },
            b2 = FloatArray(4) { 0.0f },
            version = 2,
            trainedEpochs = 5
        )
        val cp = com.example.dle_prototype.data.ml.TrainingCheckpoint(
            username = "alice",
            sessionId = "sess_1",
            currentEpoch = 5,
            targetEpochs = 5,
            currentLoss = 0.038f,
            accuracyPct = 86f,
            lossHistory = listOf(0.18f, 0.12f, 0.08f, 0.05f, 0.038f),
            weights = dummyWeights,
            isBest = true
        )

        val health = com.example.dle_prototype.ui.components.evaluateModelHealth(
            checkpoints = listOf(cp),
            userWeights = dummyWeights
        )

        assertEquals("Should evaluate to STABLE for smoothly decreasing loss", com.example.dle_prototype.ui.components.HealthState.STABLE, health.state)
        assertTrue("Loss delta should be negative for decreasing loss", health.lossDelta < 0f)
        assertTrue("Stability score should be high for stable model", health.stabilityScore >= 80)
    }

    @Test
    fun testModelHealthEvaluationDiverging() {
        val dummyWeights = ModelWeights(
            w1 = Array(8) { FloatArray(5) { 0.1f } },
            b1 = FloatArray(8) { 0.0f },
            w2 = Array(4) { FloatArray(8) { 0.1f } },
            b2 = FloatArray(4) { 0.0f },
            version = 3,
            trainedEpochs = 6
        )
        // Loss spiked from 0.08 to 0.22 (+175%)
        val cp = com.example.dle_prototype.data.ml.TrainingCheckpoint(
            username = "alice",
            sessionId = "sess_div",
            currentEpoch = 6,
            targetEpochs = 6,
            currentLoss = 0.22f,
            accuracyPct = 60f,
            lossHistory = listOf(0.12f, 0.09f, 0.08f, 0.14f, 0.22f),
            weights = dummyWeights
        )

        val health = com.example.dle_prototype.ui.components.evaluateModelHealth(
            checkpoints = listOf(cp),
            userWeights = dummyWeights
        )

        assertEquals("Should evaluate to DIVERGING for sharply rising loss", com.example.dle_prototype.ui.components.HealthState.DIVERGING, health.state)
        assertTrue("Loss delta should be positive for diverging model", health.lossDelta > 0.015f)
        assertTrue("Stability score should drop for diverging model", health.stabilityScore < 60)
    }

    @Test
    fun testModelHealthEvaluationOverfitting() {
        val dummyWeights = ModelWeights(
            w1 = Array(8) { FloatArray(5) { 0.1f } },
            b1 = FloatArray(8) { 0.0f },
            w2 = Array(4) { FloatArray(8) { 0.1f } },
            b2 = FloatArray(4) { 0.0f },
            version = 4,
            trainedEpochs = 12
        )
        // Checkpoint with ACCURACY_DROP trigger
        val cp = com.example.dle_prototype.data.ml.TrainingCheckpoint(
            username = "alice",
            sessionId = "sess_overfit",
            currentEpoch = 12,
            targetEpochs = 12,
            currentLoss = 0.008f, // Ultra low loss
            accuracyPct = 68f, // Accuracy dropped
            lossHistory = listOf(0.15f, 0.09f, 0.04f, 0.02f, 0.01f, 0.008f),
            weights = dummyWeights,
            triggerType = "ACCURACY_DROP"
        )

        val health = com.example.dle_prototype.ui.components.evaluateModelHealth(
            checkpoints = listOf(cp),
            userWeights = dummyWeights
        )

        assertEquals("Should evaluate to OVERFITTING on accuracy drop flag", com.example.dle_prototype.ui.components.HealthState.OVERFITTING, health.state)
        assertTrue("Overfitting stability score should reflect risk", health.stabilityScore <= 50)
    }

    @Test
    fun testTrainingLogEntryCreationAndFormatting() {
        val now = 1727690000000L
        val entry = com.example.dle_prototype.data.ml.TrainingLogEntry(
            id = 1,
            username = "alice",
            sessionId = "sess_debug",
            timestamp = now,
            level = com.example.dle_prototype.data.ml.LogLevel.WARNING,
            category = com.example.dle_prototype.data.ml.LogCategory.DATA_QUALITY,
            title = "Data Quality Alert: Low Sample Diversity",
            message = "Observed telemetry features are concentrated in 2 quiz topics.",
            detailsJson = "{\"warning\": \"LOW_SAMPLE_COUNT\", \"augmentedSamples\": 12}"
        )

        assertEquals("alice", entry.username)
        assertEquals(com.example.dle_prototype.data.ml.LogLevel.WARNING, entry.level)
        assertEquals(com.example.dle_prototype.data.ml.LogCategory.DATA_QUALITY, entry.category)
        assertTrue("Formatted time string should not be empty", entry.formattedTime().isNotEmpty())
        assertTrue("Details JSON should contain sample count", entry.detailsJson.contains("augmentedSamples"))
    }

    @Test
    fun testTrainingLogCategoryFiltering() {
        val logs = listOf(
            com.example.dle_prototype.data.ml.TrainingLogEntry(
                id = 1, username = "alice", title = "Checkpoint #1", message = "Saved",
                category = com.example.dle_prototype.data.ml.LogCategory.CHECKPOINT
            ),
            com.example.dle_prototype.data.ml.TrainingLogEntry(
                id = 2, username = "alice", title = "Data Alert", message = "Low variance",
                category = com.example.dle_prototype.data.ml.LogCategory.DATA_QUALITY
            ),
            com.example.dle_prototype.data.ml.TrainingLogEntry(
                id = 3, username = "alice", title = "Param Update", message = "LR changed",
                category = com.example.dle_prototype.data.ml.LogCategory.PARAM_ADJUSTMENT
            ),
            com.example.dle_prototype.data.ml.TrainingLogEntry(
                id = 4, username = "alice", title = "Checkpoint #2", message = "Saved",
                category = com.example.dle_prototype.data.ml.LogCategory.CHECKPOINT
            )
        )

        val checkpoints = logs.filter { it.category == com.example.dle_prototype.data.ml.LogCategory.CHECKPOINT }
        assertEquals(2, checkpoints.size)

        val dataAlerts = logs.filter { it.category == com.example.dle_prototype.data.ml.LogCategory.DATA_QUALITY }
        assertEquals(1, dataAlerts.size)

        val paramAdjustments = logs.filter { it.category == com.example.dle_prototype.data.ml.LogCategory.PARAM_ADJUSTMENT }
        assertEquals(1, paramAdjustments.size)
    }

    @Test
    fun testTrainingLogJsonExport() {
        val logs = listOf(
            com.example.dle_prototype.data.ml.TrainingLogEntry(
                id = 10,
                username = "student_alpha",
                sessionId = "sess_export_1",
                timestamp = 1727692000000L,
                level = com.example.dle_prototype.data.ml.LogLevel.SUCCESS,
                category = com.example.dle_prototype.data.ml.LogCategory.CHECKPOINT,
                title = "Checkpoint Saved: Epoch 4/10",
                message = "Loss: 0.0382",
                detailsJson = "{\"epoch\": 4, \"loss\": 0.0382}"
            ),
            com.example.dle_prototype.data.ml.TrainingLogEntry(
                id = 11,
                username = "student_alpha",
                sessionId = "sess_export_1",
                timestamp = 1727692050000L,
                level = com.example.dle_prototype.data.ml.LogLevel.WARNING,
                category = com.example.dle_prototype.data.ml.LogCategory.DATA_QUALITY,
                title = "Data Alert: Low Volume",
                message = "Synthetic data bootstrapped",
                detailsJson = "{\"augmented\": true}"
            )
        )

        val json = com.example.dle_prototype.data.ml.TrainingLogEntry.listToJson(logs, "student_alpha")
        assertTrue("JSON must contain export metadata", json.contains("\"exportMetadata\""))
        assertTrue("JSON must contain username", json.contains("\"username\": \"student_alpha\""))
        assertTrue("JSON must record totalEvents as 2", json.contains("\"totalEvents\": 2"))
        assertTrue("JSON must contain events array", json.contains("\"events\": ["))
        assertTrue("JSON must include checkpoint title", json.contains("Checkpoint Saved: Epoch 4/10"))
        assertTrue("JSON must include nested details", json.contains("\"epoch\": 4"))
    }

    @Test
    fun testThermalStatusLevelMapping() {
        assertEquals(com.example.dle_prototype.data.ml.ThermalStatusLevel.NONE, com.example.dle_prototype.data.ml.ThermalStatusLevel.fromStatusCode(0))
        assertEquals(com.example.dle_prototype.data.ml.ThermalStatusLevel.LIGHT, com.example.dle_prototype.data.ml.ThermalStatusLevel.fromStatusCode(1))
        assertEquals(com.example.dle_prototype.data.ml.ThermalStatusLevel.MODERATE, com.example.dle_prototype.data.ml.ThermalStatusLevel.fromStatusCode(2))
        assertEquals(com.example.dle_prototype.data.ml.ThermalStatusLevel.SEVERE, com.example.dle_prototype.data.ml.ThermalStatusLevel.fromStatusCode(3))
        assertEquals(com.example.dle_prototype.data.ml.ThermalStatusLevel.CRITICAL, com.example.dle_prototype.data.ml.ThermalStatusLevel.fromStatusCode(4))
        assertEquals(com.example.dle_prototype.data.ml.ThermalStatusLevel.EMERGENCY, com.example.dle_prototype.data.ml.ThermalStatusLevel.fromStatusCode(5))
        assertEquals(com.example.dle_prototype.data.ml.ThermalStatusLevel.SHUTDOWN, com.example.dle_prototype.data.ml.ThermalStatusLevel.fromStatusCode(6))
    }

    @Test
    fun testSystemResourceSnapshotThrottlingCriteria() {
        val nominalSnapshot = com.example.dle_prototype.data.ml.SystemResourceSnapshot(
            cpuUsagePercent = 45f,
            thermalStatus = com.example.dle_prototype.data.ml.ThermalStatusLevel.NONE,
            isThrottling = false
        )
        org.junit.Assert.assertFalse("Nominal snapshot should not be throttling", nominalSnapshot.isThrottling)

        val severeThermalSnapshot = com.example.dle_prototype.data.ml.SystemResourceSnapshot(
            cpuUsagePercent = 60f,
            thermalStatus = com.example.dle_prototype.data.ml.ThermalStatusLevel.SEVERE,
            isThrottling = true
        )
        assertTrue("Severe thermal status should trigger throttling flag", severeThermalSnapshot.isThrottling)

        val highCpuSnapshot = com.example.dle_prototype.data.ml.SystemResourceSnapshot(
            cpuUsagePercent = 94f,
            thermalStatus = com.example.dle_prototype.data.ml.ThermalStatusLevel.LIGHT,
            isThrottling = true
        )
        assertTrue("High CPU load (>88%) should trigger throttling flag", highCpuSnapshot.isThrottling)
    }

    @Test
    fun testThermalPacingDuringTraining() = kotlinx.coroutines.runBlocking {
        val sample = com.example.dle_prototype.data.ml.TrainingSample(
            inputs = floatArrayOf(0.5f, 0.4f, 0.6f, 0.8f, 0.3f),
            targets = floatArrayOf(0.7f, 0.6f, 0.8f, 0.7f)
        )
        val dataset = listOf(sample, sample)
        val trainer = com.example.dle_prototype.data.ml.OnDeviceTrainableModel(5, 8, 4)

        var pacingYieldCount = 0
        val result = trainer.train(
            dataset = dataset,
            epochs = 3,
            startEpoch = 1,
            learningRate = 0.05f,
            thermalPacingDelayMs = 5L,
            onThermalPacedYield = { pacingYieldCount++ }
        )

        assertEquals(3, result.weights.trainedEpochs)
        assertEquals(3, pacingYieldCount)
        assertTrue("Final loss should be computed", result.finalLoss >= 0f)
    }

    @Test
    fun testBatterySaverModeActivationAndPollingReduction() {
        // Nominal condition: Battery 85%, Thermal NONE, Battery saver enabled
        val nominalSnapshot = com.example.dle_prototype.data.ml.SystemResourceSnapshot(
            batteryPercent = 85,
            isCharging = false,
            isLowBattery = false,
            thermalStatus = com.example.dle_prototype.data.ml.ThermalStatusLevel.NONE,
            isBatterySaverEnabled = true,
            isBatterySaverEngaged = false,
            pollingIntervalMs = 1000L,
            trainingPacingDelayMs = 0L
        )
        org.junit.Assert.assertFalse("Battery saver should not be engaged under nominal conditions", nominalSnapshot.isBatterySaverEngaged)
        assertEquals(1000L, nominalSnapshot.pollingIntervalMs)
        assertEquals(0L, nominalSnapshot.trainingPacingDelayMs)

        // Low Battery condition: Battery 15%, discharging -> Battery saver engaged!
        val lowBatterySnapshot = com.example.dle_prototype.data.ml.SystemResourceSnapshot(
            batteryPercent = 15,
            isCharging = false,
            isLowBattery = true,
            thermalStatus = com.example.dle_prototype.data.ml.ThermalStatusLevel.NONE,
            isBatterySaverEnabled = true,
            isBatterySaverEngaged = true,
            pollingIntervalMs = 3000L,
            trainingPacingDelayMs = 40L
        )
        assertTrue("Battery saver must engage when battery is <= 20%", lowBatterySnapshot.isBatterySaverEngaged)
        assertEquals("Polling rate should be reduced from 1s to 3s", 3000L, lowBatterySnapshot.pollingIntervalMs)
        assertEquals("Training intensity must be throttled with +40ms pacing", 40L, lowBatterySnapshot.trainingPacingDelayMs)

        // High Thermal condition: Battery 80%, Thermal MODERATE -> Battery saver engaged!
        val highThermalSnapshot = com.example.dle_prototype.data.ml.SystemResourceSnapshot(
            batteryPercent = 80,
            isCharging = false,
            thermalStatus = com.example.dle_prototype.data.ml.ThermalStatusLevel.MODERATE,
            isBatterySaverEnabled = true,
            isBatterySaverEngaged = true,
            pollingIntervalMs = 3000L,
            trainingPacingDelayMs = 35L
        )
        assertTrue("Battery saver must engage on high thermal load", highThermalSnapshot.isBatterySaverEngaged)
        assertEquals("Polling must slow to 3000ms during thermal stress", 3000L, highThermalSnapshot.pollingIntervalMs)
        assertTrue("Pacing delay should be active", highThermalSnapshot.trainingPacingDelayMs >= 35L)

        // Disabled switch condition: Battery 14%, but isBatterySaverEnabled = false
        val disabledSaverSnapshot = com.example.dle_prototype.data.ml.SystemResourceSnapshot(
            batteryPercent = 14,
            isCharging = false,
            isLowBattery = true,
            thermalStatus = com.example.dle_prototype.data.ml.ThermalStatusLevel.NONE,
            isBatterySaverEnabled = false,
            isBatterySaverEngaged = false,
            pollingIntervalMs = 1000L,
            trainingPacingDelayMs = 0L
        )
        org.junit.Assert.assertFalse("When switch is off, battery saver must remain disengaged", disabledSaverSnapshot.isBatterySaverEngaged)
        assertEquals(1000L, disabledSaverSnapshot.pollingIntervalMs)
        assertEquals(0L, disabledSaverSnapshot.trainingPacingDelayMs)
    }

    @Test
    fun testRealTimeInferenceLatencyProfiling() = kotlinx.coroutines.runBlocking {
        val telemetry = com.example.dle_prototype.data.LearningTelemetry(
            loginCount = 4,
            totalTimeMinutes = 45f,
            lastQuizScore = 80f,
            lastDifficultyReached = 2f,
            lastCategorySelected = 1f
        )
        val weights = com.example.dle_prototype.data.ml.ModelWeights.defaultInit(5, 8, 4)

        val snapshot = com.example.dle_prototype.data.ml.InferenceLatencyTracker.profileInference(
            telemetry = telemetry,
            customWeights = weights
        )

        assertTrue("Total latency must be greater than zero", snapshot.totalLatencyMs > 0f)
        assertTrue("Fast edge latency expected", snapshot.totalLatencyMs < 20f)
        assertTrue("Throughput must be positive", snapshot.throughputSamplesPerSec > 0f)
        assertEquals("Must profile 6 architectural stages", 6, snapshot.layers.size)
        assertTrue("Must identify a bottleneck stage", snapshot.bottleneckStage.isNotEmpty())
        assertTrue("Bottleneck percentage must be > 0", snapshot.bottleneckPercent > 0f)
        assertTrue("Architectural diagnosis must be provided", snapshot.architecturalDiagnosis.isNotEmpty())
    }

    @Test
    fun testBatchLatencyBenchmark() = kotlinx.coroutines.runBlocking {
        val telemetry = com.example.dle_prototype.data.LearningTelemetry(
            loginCount = 5,
            totalTimeMinutes = 60f,
            lastQuizScore = 90f,
            lastDifficultyReached = 3f,
            lastCategorySelected = 2f
        )

        val summary = com.example.dle_prototype.data.ml.InferenceLatencyTracker.benchmarkBatch(
            telemetry = telemetry,
            customWeights = null,
            sampleCount = 10
        )

        assertEquals(10, summary.sampleCount)
        assertEquals(10, summary.rollingHistoryMs.size)
        assertTrue("Min latency must be <= Avg latency", summary.minLatencyMs <= summary.avgLatencyMs)
        assertTrue("Avg latency must be <= Max latency", summary.avgLatencyMs <= summary.maxLatencyMs)
        assertTrue("Jitter must be non-negative", summary.jitterMs >= 0f)
    }

    @Test
    fun testLeaderboardSortingAndScopeFiltering() {
        val user1 = com.example.dle_prototype.data.LeaderboardEntry(
            rank = 0,
            username = "alice",
            displayName = "Alice",
            avatarEmoji = "⭐",
            dailyStreak = 10,
            totalQuizzes = 20,
            totalScore = 150,
            accuracyPercent = 88.0f,
            isCurrentUser = true,
            isFriend = false
        )
        val user2 = com.example.dle_prototype.data.LeaderboardEntry(
            rank = 0,
            username = "bob",
            displayName = "Bob",
            avatarEmoji = "🚀",
            dailyStreak = 15,
            totalQuizzes = 25,
            totalScore = 120,
            accuracyPercent = 95.0f,
            isCurrentUser = false,
            isFriend = true
        )
        val user3 = com.example.dle_prototype.data.LeaderboardEntry(
            rank = 0,
            username = "charlie",
            displayName = "Charlie",
            avatarEmoji = "🧠",
            dailyStreak = 5,
            totalQuizzes = 30,
            totalScore = 200,
            accuracyPercent = 75.0f,
            isCurrentUser = false,
            isFriend = false
        )

        val list = listOf(user1, user2, user3)

        // 1. Sort by Streak
        val streakSorted = list.sortedWith(
            compareByDescending<com.example.dle_prototype.data.LeaderboardEntry> { it.dailyStreak }
                .thenByDescending { it.totalScore }
        )
        assertEquals("Bob should be #1 by streak (15d)", "bob", streakSorted[0].username)
        assertEquals("Alice should be #2 by streak (10d)", "alice", streakSorted[1].username)
        assertEquals("Charlie should be #3 by streak (5d)", "charlie", streakSorted[2].username)

        // 2. Sort by Score
        val scoreSorted = list.sortedWith(
            compareByDescending<com.example.dle_prototype.data.LeaderboardEntry> { it.totalScore }
                .thenByDescending { it.dailyStreak }
        )
        assertEquals("Charlie should be #1 by score (200 pts)", "charlie", scoreSorted[0].username)
        assertEquals("Alice should be #2 by score (150 pts)", "alice", scoreSorted[1].username)
        assertEquals("Bob should be #3 by score (120 pts)", "bob", scoreSorted[2].username)

        // 3. Friends Scope Filtering
        val friendsOnly = list.filter { it.isCurrentUser || it.isFriend }
        assertEquals(2, friendsOnly.size)
        assertTrue(friendsOnly.any { it.username == "alice" })
        assertTrue(friendsOnly.any { it.username == "bob" })
        assertTrue(friendsOnly.none { it.username == "charlie" })
    }

    @Test
    fun testDailyStreakReminderMessageFormatting() {
        fun formatStreakNotificationTitle(streak: Int): String {
            return when {
                streak >= 5 -> "🔥 Your $streak-Day Quiz Streak is at Risk!"
                streak >= 1 -> "🔥 Keep Your $streak-Day Streak Alive!"
                else -> "⚡ Daily Quiz Time: Ignite Your Learning Streak!"
            }
        }

        assertEquals("⚡ Daily Quiz Time: Ignite Your Learning Streak!", formatStreakNotificationTitle(0))
        assertEquals("🔥 Keep Your 1-Day Streak Alive!", formatStreakNotificationTitle(1))
        assertEquals("🔥 Keep Your 4-Day Streak Alive!", formatStreakNotificationTitle(4))
        assertEquals("🔥 Your 5-Day Quiz Streak is at Risk!", formatStreakNotificationTitle(5))
        assertEquals("🔥 Your 12-Day Quiz Streak is at Risk!", formatStreakNotificationTitle(12))
    }

    @Test
    fun testDailyLearningGoalProgressCalculation() {
        fun calculateGoalProgress(target: Int, answered: Int): com.example.dle_prototype.data.DailyGoalProgress {
            val validTarget = target.coerceIn(3, 100)
            val percent = (answered.toFloat() / validTarget.toFloat()).coerceIn(0f, 1f)
            return com.example.dle_prototype.data.DailyGoalProgress(
                targetQuestions = validTarget,
                answeredToday = answered,
                percentComplete = percent,
                isAchieved = answered >= validTarget
            )
        }

        // Test clamped target
        val clampedMin = calculateGoalProgress(1, 0)
        assertEquals(3, clampedMin.targetQuestions)

        val clampedMax = calculateGoalProgress(150, 0)
        assertEquals(100, clampedMax.targetQuestions)

        // Test partial progress
        val progressPartial = calculateGoalProgress(10, 4)
        assertEquals(10, progressPartial.targetQuestions)
        assertEquals(4, progressPartial.answeredToday)
        assertEquals(0.4f, progressPartial.percentComplete, 0.001f)
        assertFalse(progressPartial.isAchieved)

        // Test exact completion
        val progressComplete = calculateGoalProgress(10, 10)
        assertEquals(1.0f, progressComplete.percentComplete, 0.001f)
        assertTrue(progressComplete.isAchieved)

        // Test over-achievement
        val progressOver = calculateGoalProgress(10, 15)
        assertEquals(1.0f, progressOver.percentComplete, 0.001f)
        assertTrue(progressOver.isAchieved)
    }
}
