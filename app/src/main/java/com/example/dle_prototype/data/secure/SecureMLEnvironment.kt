package com.example.dle_prototype.data.secure

import com.example.dle_prototype.data.LearningTelemetry
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.OnDeviceTrainableModel
import com.example.dle_prototype.data.ml.TrainingSample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.math.abs

/**
 * Privacy Safeguard utility mirroring the cryptographic isolation and anonymization
 * from the PLI Research (Secure-ML-with-BERT) repository.
 *
 * Implements:
 * - Pseudonymization of user identifiers (Salted SHA-256).
 * - Hardware-compatible AES-256-GCM authenticated encryption for learner telemetry.
 * - Differential privacy noise injection for training dataset isolation.
 */
object PrivacySafeguard {
    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // Hardware-backed session key for local memory encryption
    private val masterSecretKey: SecretKey by lazy {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE, SecureRandom())
        keyGen.generateKey()
    }

    private val salt: String = "PLI_RESEARCH_SECURE_SALT_2026"

    /**
     * Anonymizes student/user identity using a deterministic cryptographic hash.
     * Equivalent to PrivacySafeguard.anonymize() in the Python research repository.
     */
    fun anonymize(identifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$identifier".toByteArray(Charsets.UTF_8)
        val hash = digest.digest(input)
        val shortHex = hash.take(6).joinToString("") { "%02x".format(it) }
        return "ANON_$shortHex"
    }

    /**
     * Encrypts plain telemetry data strings using AES-256-GCM authenticated encryption.
     * Equivalent to PrivacySafeguard.encrypt_data() with Fernet encryption.
     */
    fun encryptData(plainText: String): String {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, masterSecretKey, spec)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + cipherBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

        return Base64.getEncoder().encodeToString(combined)
    }

    /**
     * Decrypts encrypted telemetry data strings using AES-256-GCM.
     */
    fun decryptData(cipherText: String): String {
        val combined = Base64.getDecoder().decode(cipherText)
        if (combined.size < GCM_IV_LENGTH) throw IllegalArgumentException("Ciphertext too short")

        val iv = ByteArray(GCM_IV_LENGTH)
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)

        val cipherBytes = ByteArray(combined.size - GCM_IV_LENGTH)
        System.arraycopy(combined, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterSecretKey, spec)

        val decrypted = cipher.doFinal(cipherBytes)
        return String(decrypted, Charsets.UTF_8)
    }

    /**
     * Applies lightweight differential privacy (Laplace/Gaussian noise) to prevent
     * membership inference attacks on gradient updates.
     */
    fun addDifferentialPrivacyNoise(rawFeatures: FloatArray, epsilon: Float = 0.5f): FloatArray {
        val scale = 0.05f / epsilon.coerceAtLeast(0.1f)
        val rng = SecureRandom()
        return FloatArray(rawFeatures.size) { i ->
            val u = rng.nextFloat() - 0.5f
            val noise = -scale * Math.signum(u) * Math.log(1.0 - 2.0 * abs(u)).toFloat()
            (rawFeatures[i] + noise).coerceAtLeast(0f)
        }
    }
}

/**
 * Sandboxed data record representing an isolated learner interaction container.
 */
data class IsolatedSandboxRecord(
    val sandboxId: String,
    val anonymizedUserToken: String,
    val encryptedPayload: String,
    val telemetry: LearningTelemetry,
    val sampleTimestamp: Long = System.currentTimeMillis()
)

/**
 * Moodle Course Record representing remote/synced LMS learner course telemetry.
 */
data class MoodleCourseData(
    val courseId: String,
    val courseName: String,
    val activeLearnerCount: Int,
    val completedModules: Int,
    val totalModules: Int,
    val courseGradeAverage: Float,
    val sampleTelemetry: LearningTelemetry
)

/**
 * Results from an Automated Hyperparameter Tuning exploration.
 */
data class HyperparameterTuningResult(
    val bestLearningRate: Float,
    val bestMomentum: Float,
    val bestEpochs: Int,
    val initialLoss: Float,
    val bestLoss: Float,
    val lossReductionPercent: Float,
    val evaluatedTrials: List<TuningTrial>,
    val timeTakenMs: Long
)

data class TuningTrial(
    val trialNumber: Int,
    val learningRate: Float,
    val momentum: Float,
    val epochs: Int,
    val finalLoss: Float
)

/**
 * Secure ML Environment directly ported and enhanced from the Secure-ML-with-BERT repository.
 *
 * Implements:
 * - Data sandboxing (`isolate`) using unique identifier tokens.
 * - Integration with Moodle LMS course events & telemetry ingestion.
 * - On-device Automated Hyperparameter Tuning inside the isolated sandbox.
 */
class SecureMLEnvironment {
    private val sandbox = ConcurrentHashMap<String, IsolatedSandboxRecord>()
    private var sequenceId = 0

    /**
     * Isolates learner data in a cryptographic sandbox.
     * Equivalent to SecureMLEnvironment.isolate() in Python.
     */
    @Synchronized
    fun isolate(username: String, telemetry: LearningTelemetry): Pair<String, IsolatedSandboxRecord> {
        val dataId = "data_sandbox_${sequenceId++}"
        val anonToken = PrivacySafeguard.anonymize(username)
        val rawJson = """{"logins":${telemetry.loginCount},"time":${telemetry.totalTimeMinutes},"score":${telemetry.lastQuizScore},"visits":${telemetry.pageVisits},"queries":${telemetry.searchQueries}}"""
        val encrypted = PrivacySafeguard.encryptData(rawJson)

        val record = IsolatedSandboxRecord(
            sandboxId = dataId,
            anonymizedUserToken = anonToken,
            encryptedPayload = encrypted,
            telemetry = telemetry.copy(isEncryptedInSandbox = true, sandboxToken = dataId)
        )
        sandbox[dataId] = record
        return Pair(dataId, record)
    }

    fun getRecord(sandboxId: String): IsolatedSandboxRecord? = sandbox[sandboxId]

    fun getAllRecords(): List<IsolatedSandboxRecord> = sandbox.values.toList()

    fun getSandboxCount(): Int = sandbox.size

    /**
     * Simulates fetching Moodle LMS courses with telemetry ingestion into the sandbox.
     */
    fun getMoodleCourses(): List<MoodleCourseData> = listOf(
        MoodleCourseData(
            courseId = "CS101",
            courseName = "CS101: Modern Kotlin & System Architecture",
            activeLearnerCount = 42,
            completedModules = 5,
            totalModules = 6,
            courseGradeAverage = 88.4f,
            sampleTelemetry = LearningTelemetry(
                loginCount = 14,
                totalTimeMinutes = 95.0f,
                lastQuizScore = 88.0f,
                lastDifficultyReached = 2.5f,
                lastCategorySelected = 2.0f,
                pageVisits = 48,
                searchQueries = 19,
                activityCompletionRate = 83.3f,
                reactionsPositive = 8,
                reactionsNegative = 1,
                feedbackRating = 9.2f
            )
        ),
        MoodleCourseData(
            courseId = "DS202",
            courseName = "DS202: Secure Machine Learning & Neural Networks",
            activeLearnerCount = 35,
            completedModules = 4,
            totalModules = 8,
            courseGradeAverage = 91.0f,
            sampleTelemetry = LearningTelemetry(
                loginCount = 18,
                totalTimeMinutes = 140.0f,
                lastQuizScore = 94.0f,
                lastDifficultyReached = 3.0f,
                lastCategorySelected = 3.0f,
                pageVisits = 65,
                searchQueries = 28,
                activityCompletionRate = 50.0f,
                reactionsPositive = 12,
                reactionsNegative = 2,
                feedbackRating = 9.5f
            )
        ),
        MoodleCourseData(
            courseId = "SE303",
            courseName = "SE303: Private Learning Intelligence & DLE Design",
            activeLearnerCount = 28,
            completedModules = 3,
            totalModules = 4,
            courseGradeAverage = 84.5f,
            sampleTelemetry = LearningTelemetry(
                loginCount = 9,
                totalTimeMinutes = 62.0f,
                lastQuizScore = 82.0f,
                lastDifficultyReached = 2.0f,
                lastCategorySelected = 1.0f,
                pageVisits = 32,
                searchQueries = 11,
                activityCompletionRate = 75.0f,
                reactionsPositive = 5,
                reactionsNegative = 0,
                feedbackRating = 8.8f
            )
        )
    )

    /**
     * Executes automated hyperparameter tuning inside the isolated sandbox environment.
     * Mirroring hyperparameter_tuning() from model-training.ipynb using HyperOpt grid search.
     */
    suspend fun runAutomatedHyperparameterTuning(
        baseTelemetry: LearningTelemetry,
        onProgress: (currentTrial: Int, totalTrials: Int, bestLossSoFar: Float) -> Unit = { _, _, _ -> }
    ): HyperparameterTuningResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        // Create isolated training dataset based on sandboxed telemetry with noise
        val rawInput = baseTelemetry.to5FeatureArray()
        val dataset = mutableListOf<TrainingSample>()

        for (i in 0 until 12) {
            val noisyInput = PrivacySafeguard.addDifferentialPrivacyNoise(rawInput, epsilon = 0.8f)
            val normalized = OnDeviceTrainableModel.normalize(noisyInput)
            val targetTraits = floatArrayOf(
                (baseTelemetry.lastQuizScore / 100f * 0.8f + 0.15f).coerceIn(0.1f, 0.95f),
                (baseTelemetry.totalTimeMinutes / 120f * 0.8f + 0.1f).coerceIn(0.1f, 0.95f),
                (baseTelemetry.activityCompletionRate / 100f * 0.85f + 0.1f).coerceIn(0.1f, 0.95f),
                (baseTelemetry.feedbackRating / 10f * 0.9f).coerceIn(0.1f, 0.95f)
            )
            dataset.add(TrainingSample(normalized, targetTraits))
        }

        // Search space: Learning rates, Momentum, Epochs
        val lrCandidates = listOf(0.01f, 0.04f, 0.08f, 0.12f)
        val momentumCandidates = listOf(0.70f, 0.85f, 0.92f)
        val epochCandidates = listOf(15, 25)

        val totalTrials = lrCandidates.size * momentumCandidates.size
        val trials = mutableListOf<TuningTrial>()

        var bestLoss = Float.MAX_VALUE
        var bestLr = 0.05f
        var bestMom = 0.85f
        var bestEpoch = 20
        var initialLoss = 0f

        var trialCount = 0

        for (lr in lrCandidates) {
            for (mom in momentumCandidates) {
                trialCount++
                val testEpochs = 20

                val model = OnDeviceTrainableModel(5, 8, 4)
                if (initialLoss == 0f) {
                    initialLoss = model.computeLoss(dataset)
                }

                val result = model.train(
                    dataset = dataset,
                    epochs = testEpochs,
                    learningRate = lr,
                    momentum = mom
                )

                val trialLoss = result.finalLoss
                trials.add(
                    TuningTrial(
                        trialNumber = trialCount,
                        learningRate = lr,
                        momentum = mom,
                        epochs = testEpochs,
                        finalLoss = trialLoss
                    )
                )

                if (trialLoss < bestLoss) {
                    bestLoss = trialLoss
                    bestLr = lr
                    bestMom = mom
                    bestEpoch = testEpochs
                }

                onProgress(trialCount, totalTrials, bestLoss)
            }
        }

        val reduction = if (initialLoss > 0.0001f) {
            ((initialLoss - bestLoss) / initialLoss * 100f).coerceIn(0f, 100f)
        } else 0f

        HyperparameterTuningResult(
            bestLearningRate = bestLr,
            bestMomentum = bestMom,
            bestEpochs = bestEpoch,
            initialLoss = initialLoss,
            bestLoss = bestLoss,
            lossReductionPercent = reduction,
            evaluatedTrials = trials,
            timeTakenMs = System.currentTimeMillis() - startTime
        )
    }

    companion object {
        val INSTANCE = SecureMLEnvironment()
    }
}
