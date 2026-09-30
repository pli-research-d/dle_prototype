package com.example.dle_prototype.data

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.OnDeviceTrainableModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.system.measureTimeMillis

object TFLiteEngine {
    private const val TAG = "TFLiteEngine"
    private const val MODEL_NAME = "dle_model.tflite"

    // Means and STDs for standardizing the 5 input features
    private val MEANS = floatArrayOf(4.2f, 8.0f, 45.7f, 2.0f, 3.8f)
    private val STDS = floatArrayOf(2.0f, 4.1f, 28.5f, 0.8f, 1.9f)

    private var interpreter: Interpreter? = null

    @Synchronized
    private fun getOrInitInterpreter(context: Context): Interpreter? {
        if (interpreter == null) {
            try {
                val buffer = loadModelFile(context, MODEL_NAME)
                val options = Interpreter.Options().apply {
                    setNumThreads(2)
                }
                interpreter = Interpreter(buffer, options)
                Log.d(TAG, "TFLite interpreter initialized successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load TFLite model: ${e.message}", e)
                interpreter = null
            }
        }
        return interpreter
    }

    private fun loadModelFile(context: Context, modelFilename: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(modelFilename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    suspend fun runInference(
        context: Context,
        telemetry: LearningTelemetry,
        customWeights: ModelWeights? = null
    ): PersonalizationProfile = withContext(Dispatchers.Default) {
        val rawInput = floatArrayOf(
            telemetry.loginCount.toFloat(),
            telemetry.totalTimeMinutes,
            telemetry.lastQuizScore,
            telemetry.lastDifficultyReached,
            telemetry.lastCategorySelected
        )

        val inputScaled = FloatArray(rawInput.size)
        for (i in rawInput.indices) {
            val safeVal = if (rawInput[i].isNaN() || rawInput[i].isInfinite()) 0f else rawInput[i]
            inputScaled[i] = (safeVal - MEANS[i]) / STDS[i]
        }

        var latency = 0L
        val preds: FloatArray
        val modelSource: String
        val isPersonalized: Boolean

        if (customWeights != null) {
            // Run inference with personalized on-device trained weights
            val onDeviceModel = OnDeviceTrainableModel(initialWeights = customWeights)
            latency = measureTimeMillis {
                preds = onDeviceModel.forward(inputScaled)
            }
            modelSource = "On-Device Trained (Epochs: ${customWeights.trainedEpochs})"
            isPersonalized = true
        } else {
            // Run inference with baseline TFLite model
            val inputBatch = Array(1) { inputScaled }
            val outputBatch = Array(1) { FloatArray(4) }
            val success = try {
                val interp = getOrInitInterpreter(context)
                if (interp != null) {
                    latency = measureTimeMillis {
                        interp.run(inputBatch, outputBatch)
                    }
                    true
                } else false
            } catch (e: Exception) {
                Log.e(TAG, "Inference error: ${e.message}", e)
                false
            }
            preds = if (success) outputBatch[0] else floatArrayOf(0.5f, 0.5f, 0.5f, 0.5f)
            modelSource = "Baseline TFLite"
            isPersonalized = false
        }

        // Map and clamp scores into 0..1 display range with labels
        fun traitLabel(score: Float): String = when {
            score >= 0.75f -> "Advanced"
            score >= 0.5f -> "Proficient"
            score >= 0.3f -> "Developing"
            else -> "Needs Attention"
        }

        val cScore = preds[0].coerceIn(0f, 1f)
        val mScore = preds[1].coerceIn(0f, 1f)
        val uScore = preds[2].coerceIn(0f, 1f)
        val eScore = preds[3].coerceIn(0f, 1f)

        val traits = listOf(
            TraitScore("Conscientiousness", cScore, preds[0], "Study consistency & habit tracking", traitLabel(cScore)),
            TraitScore("Motivation", mScore, preds[1], "Drive to explore & master challenging topics", traitLabel(mScore)),
            TraitScore("Understanding", uScore, preds[2], "Conceptual accuracy & retention", traitLabel(uScore)),
            TraitScore("Engagement", eScore, preds[3], "Activity frequency & quiz responsiveness", traitLabel(eScore))
        )

        val topTrait = traits.maxByOrNull { it.score } ?: traits[0]
        val lowTrait = traits.minByOrNull { it.score } ?: traits[1]

        val tips = mutableListOf<String>()
        when (lowTrait.name) {
            "Conscientiousness" -> tips.add("Set a recurring 10-minute daily review habit to build consistent momentum.")
            "Motivation" -> tips.add("Pick topics aligned with a personal passion project to renew your drive.")
            "Understanding" -> tips.add("Review question explanations and try recreating example snippets by hand.")
            "Engagement" -> tips.add("Take a short quiz after every study session to reinforce active recall.")
        }
        when (topTrait.name) {
            "Conscientiousness" -> tips.add("High consistency unlocked! Ready for medium & hard challenges.")
            "Motivation" -> tips.add("Excellent drive! Channel your enthusiasm into multi-topic mixed quizzes.")
            "Understanding" -> tips.add("Strong conceptual grasp! Solid retention across fundamental rules.")
            "Engagement" -> tips.add("Top engagement level! Maintain your active daily rhythm.")
        }

        PersonalizationProfile(
            conscientiousness = traits[0],
            motivation = traits[1],
            understanding = traits[2],
            engagement = traits[3],
            primaryStrength = topTrait.name,
            focusArea = lowTrait.name,
            tips = tips,
            inferenceTimeMs = latency,
            isPersonalizedWeights = isPersonalized,
            modelSource = modelSource
        )
    }

    suspend fun benchmarkInference(context: Context, iterations: Int = 10): BenchmarkResult = withContext(Dispatchers.Default) {
        val interp = getOrInitInterpreter(context) ?: return@withContext BenchmarkResult(
            success = false,
            avgLatencyMs = 0f,
            latencies = emptyList(),
            inputShape = emptyList(),
            outputShape = emptyList(),
            modelSizeKb = 0
        )

        val input = Array(1) { floatArrayOf(3f, 12f, 80f, 2f, 1f) }
        val output = Array(1) { FloatArray(4) }

        // Warm up
        interp.run(input, output)

        val latencies = mutableListOf<Long>()
        for (i in 0 until iterations) {
            val t = measureTimeMillis {
                interp.run(input, output)
            }
            latencies.add(t)
        }

        val avgMs = latencies.average().toFloat()
        val inTensor = interp.getInputTensor(0)
        val outTensor = interp.getOutputTensor(0)

        BenchmarkResult(
            success = true,
            avgLatencyMs = avgMs,
            latencies = latencies,
            inputShape = inTensor.shape().toList(),
            outputShape = outTensor.shape().toList(),
            modelSizeKb = (context.assets.openFd(MODEL_NAME).declaredLength / 1024).toInt()
        )
    }

    data class BenchmarkResult(
        val success: Boolean,
        val avgLatencyMs: Float,
        val latencies: List<Long>,
        val inputShape: List<Int> = emptyList(),
        val outputShape: List<Int> = emptyList(),
        val modelSizeKb: Int = 0
    )
}
