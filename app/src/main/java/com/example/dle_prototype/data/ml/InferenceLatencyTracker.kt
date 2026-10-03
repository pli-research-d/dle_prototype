package com.example.dle_prototype.data.ml

import android.content.Context
import com.example.dle_prototype.data.LearningTelemetry
import com.example.dle_prototype.data.TFLiteEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.max

data class LayerLatencyDetail(
    val name: String,
    val stageTag: String,
    val operationsCount: String,
    val latencyMs: Float,
    val percentOfTotal: Float,
    val isBottleneck: Boolean,
    val optimizationHint: String
)

data class InferenceLatencySnapshot(
    val totalLatencyMs: Float,
    val throughputSamplesPerSec: Float,
    val layers: List<LayerLatencyDetail>,
    val bottleneckStage: String,
    val bottleneckPercent: Float,
    val architecturalDiagnosis: String,
    val isPersonalized: Boolean,
    val modelSource: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class LatencyBenchmarkSummary(
    val sampleCount: Int,
    val minLatencyMs: Float,
    val avgLatencyMs: Float,
    val maxLatencyMs: Float,
    val p95LatencyMs: Float,
    val jitterMs: Float,
    val throughputSamplesPerSec: Float,
    val rollingHistoryMs: List<Float>,
    val snapshot: InferenceLatencySnapshot
)

object InferenceLatencyTracker {

    private val MEANS = floatArrayOf(4.2f, 8.0f, 45.7f, 2.0f, 3.8f)
    private val STDS = floatArrayOf(2.0f, 4.1f, 28.5f, 0.8f, 1.9f)

    /**
     * Executes fine-grained nanosecond-level profiling of each architectural layer in the model.
     */
    suspend fun profileInference(
        context: Context? = null,
        telemetry: LearningTelemetry,
        customWeights: ModelWeights?,
        useTfliteInterpreter: Boolean = false
    ): InferenceLatencySnapshot = withContext(Dispatchers.Default) {
        val rawInput = floatArrayOf(
            telemetry.loginCount.toFloat(),
            telemetry.totalTimeMinutes,
            telemetry.lastQuizScore,
            telemetry.lastDifficultyReached,
            telemetry.lastCategorySelected
        )

        val weights = customWeights ?: ModelWeights.defaultInit(5, 8, 4)

        // Warmup pass
        val dummy = FloatArray(rawInput.size)
        for (i in rawInput.indices) dummy[i] = rawInput[i]
        runLayeredPass(dummy, weights)

        // 5-iteration averaged profiling for sub-millisecond precision
        var sumTNorm = 0L
        var sumTL1Gemm = 0L
        var sumTL1Act = 0L
        var sumTL2Gemm = 0L
        var sumTL2Act = 0L
        var sumTPost = 0L

        val iterations = 5
        for (it in 0 until iterations) {
            val timing = runLayeredPass(rawInput, weights)
            sumTNorm += timing.tNormNs
            sumTL1Gemm += timing.tL1GemmNs
            sumTL1Act += timing.tL1ActNs
            sumTL2Gemm += timing.tL2GemmNs
            sumTL2Act += timing.tL2ActNs
            sumTPost += timing.tPostNs
        }

        val normMs = max(0.01f, (sumTNorm.toFloat() / iterations) / 1_000_000f)
        val l1GemmMs = max(0.01f, (sumTL1Gemm.toFloat() / iterations) / 1_000_000f)
        val l1ActMs = max(0.01f, (sumTL1Act.toFloat() / iterations) / 1_000_000f)
        val l2GemmMs = max(0.01f, (sumTL2Gemm.toFloat() / iterations) / 1_000_000f)
        val l2ActMs = max(0.01f, (sumTL2Act.toFloat() / iterations) / 1_000_000f)
        val postMs = max(0.01f, (sumTPost.toFloat() / iterations) / 1_000_000f)

        val totalMs = normMs + l1GemmMs + l1ActMs + l2GemmMs + l2ActMs + postMs

        val rawLayers = listOf(
            Triple("Input Normalization", "5 Features Standardized", normMs) to
                    Pair("5 MACs / float norm", "Fuse z-score scaling into Layer 1 weight matrix factors"),
            Triple("Hidden Layer 1 GEMM", "Dense Matrix 5x8 (40 Weights)", l1GemmMs) to
                    Pair("40 MACs + 8 Bias additions", "Vectorize dot-products with NEON SIMD or INT8 quantization"),
            Triple("Hidden Layer 1 Activation", "8-Unit ReLU Non-Linearity", l1ActMs) to
                    Pair("8 max(0, x) evaluations", "Zero overhead branchless conditional select"),
            Triple("Output Layer 2 GEMM", "Dense Matrix 8x4 (32 Weights)", l2GemmMs) to
                    Pair("32 MACs + 4 Bias additions", "Compact matrix multiplication, optimal cache locality"),
            Triple("Output Layer 2 Activation", "4-Unit Sigmoid Non-Linearity", l2ActMs) to
                    Pair("4 transcendent exp(-x) calls", "Replace with piecewise Hard-Sigmoid approximation"),
            Triple("Post-Processing & Clamp", "Score Bounding & Labeling", postMs) to
                    Pair("4 Clamps + Trait Mappings", "Inline vector clamp instructions")
        )

        val maxLatency = rawLayers.maxOf { it.first.third }

        val layerDetails = rawLayers.map { (info, ops) ->
            val (name, stageTag, latMs) = info
            val (opsCount, optHint) = ops
            val pct = (latMs / totalMs) * 100f
            val isBottleneck = latMs == maxLatency
            LayerLatencyDetail(
                name = name,
                stageTag = stageTag,
                operationsCount = opsCount,
                latencyMs = latMs,
                percentOfTotal = pct,
                isBottleneck = isBottleneck,
                optimizationHint = optHint
            )
        }

        val bottleneck = layerDetails.first { it.isBottleneck }
        val throughput = if (totalMs > 0) 1000f / totalMs else 1000f

        val diagnosis = when {
            totalMs < 1.0f -> "Optimal: Inference latency (${"%.2f".format(totalMs)} ms) is sub-millisecond, providing ~${"%.0f".format(throughput)} inferences/sec with negligible battery drain."
            totalMs < 3.0f -> "Nominal: Fast real-time inference suitable for immediate UI feedback. ${bottleneck.name} accounts for ${"%.1f".format(bottleneck.percentOfTotal)}% of total runtime."
            else -> "Bottleneck Detected: ${bottleneck.name} is consuming ${"%.1f".format(bottleneck.percentOfTotal)}% of execution time. Recommendation: ${bottleneck.optimizationHint}."
        }

        val modelSource = if (customWeights != null) "Personalized On-Device Model (Epochs: ${customWeights.trainedEpochs})" else "Baseline 5x8x4 MLP"

        InferenceLatencySnapshot(
            totalLatencyMs = totalMs,
            throughputSamplesPerSec = throughput,
            layers = layerDetails,
            bottleneckStage = bottleneck.name,
            bottleneckPercent = bottleneck.percentOfTotal,
            architecturalDiagnosis = diagnosis,
            isPersonalized = customWeights != null,
            modelSource = modelSource
        )
    }

    /**
     * Runs a multi-sample batch benchmark to compute min/avg/max/p95 latency and rolling history.
     */
    suspend fun benchmarkBatch(
        context: Context? = null,
        telemetry: LearningTelemetry,
        customWeights: ModelWeights?,
        sampleCount: Int = 30
    ): LatencyBenchmarkSummary = withContext(Dispatchers.Default) {
        val latencies = mutableListOf<Float>()
        val rawInput = floatArrayOf(
            telemetry.loginCount.toFloat(),
            telemetry.totalTimeMinutes,
            telemetry.lastQuizScore,
            telemetry.lastDifficultyReached,
            telemetry.lastCategorySelected
        )
        val weights = customWeights ?: ModelWeights.defaultInit(5, 8, 4)

        // Warm up pass
        runLayeredPass(rawInput, weights)

        for (i in 0 until sampleCount) {
            val t0 = System.nanoTime()
            val timing = runLayeredPass(rawInput, weights)
            val t1 = System.nanoTime()
            val sampleMs = max(0.08f, (t1 - t0) / 1_000_000f)
            latencies.add(sampleMs)
        }

        val sorted = latencies.sorted()
        val minMs = sorted.firstOrNull() ?: 0.1f
        val maxMs = sorted.lastOrNull() ?: 0.5f
        val avgMs = latencies.average().toFloat()
        val p95Idx = ((sampleCount * 0.95f).toInt()).coerceIn(0, sampleCount - 1)
        val p95Ms = sorted[p95Idx]
        val jitterMs = maxMs - minMs
        val throughput = if (avgMs > 0) 1000f / avgMs else 1000f

        val snapshot = profileInference(context, telemetry, customWeights)

        LatencyBenchmarkSummary(
            sampleCount = sampleCount,
            minLatencyMs = minMs,
            avgLatencyMs = avgMs,
            maxLatencyMs = maxMs,
            p95LatencyMs = p95Ms,
            jitterMs = jitterMs,
            throughputSamplesPerSec = throughput,
            rollingHistoryMs = latencies,
            snapshot = snapshot
        )
    }

    private data class LayerTiming(
        val tNormNs: Long,
        val tL1GemmNs: Long,
        val tL1ActNs: Long,
        val tL2GemmNs: Long,
        val tL2ActNs: Long,
        val tPostNs: Long
    )

    private fun runLayeredPass(rawInput: FloatArray, weights: ModelWeights): LayerTiming {
        // 1. Input Normalization
        val t0 = System.nanoTime()
        val inputScaled = FloatArray(rawInput.size)
        for (i in rawInput.indices) {
            val m = if (i < MEANS.size) MEANS[i] else 0f
            val s = if (i < STDS.size && STDS[i] > 0f) STDS[i] else 1f
            val v = if (rawInput[i].isNaN() || rawInput[i].isInfinite()) 0f else rawInput[i]
            inputScaled[i] = (v - m) / s
        }
        val t1 = System.nanoTime()

        // 2. Hidden Layer 1 GEMM
        val hiddenDim = 8
        val inputDim = 5
        val hPre = FloatArray(hiddenDim)
        for (i in 0 until hiddenDim) {
            var sum = weights.b1[i]
            for (j in 0 until inputDim) {
                sum += weights.w1[i][j] * inputScaled[j]
            }
            hPre[i] = sum
        }
        val t2 = System.nanoTime()

        // 3. Hidden Layer 1 ReLU
        val h = FloatArray(hiddenDim)
        for (i in 0 until hiddenDim) {
            h[i] = max(0f, hPre[i])
        }
        val t3 = System.nanoTime()

        // 4. Output Layer 2 GEMM
        val outputDim = 4
        val yPre = FloatArray(outputDim)
        for (k in 0 until outputDim) {
            var sum = weights.b2[k]
            for (i in 0 until hiddenDim) {
                sum += weights.w2[k][i] * h[i]
            }
            yPre[k] = sum
        }
        val t4 = System.nanoTime()

        // 5. Output Layer 2 Sigmoid
        val y = FloatArray(outputDim)
        for (k in 0 until outputDim) {
            y[k] = 1.0f / (1.0f + exp(-yPre[k].coerceIn(-15f, 15f)))
        }
        val t5 = System.nanoTime()

        // 6. Post-Processing & Clamping
        val traits = FloatArray(outputDim)
        for (k in 0 until outputDim) {
            traits[k] = y[k].coerceIn(0f, 1f)
        }
        val t6 = System.nanoTime()

        return LayerTiming(
            tNormNs = max(100L, t1 - t0),
            tL1GemmNs = max(200L, t2 - t1),
            tL1ActNs = max(50L, t3 - t2),
            tL2GemmNs = max(180L, t4 - t3),
            tL2ActNs = max(80L, t5 - t4),
            tPostNs = max(50L, t6 - t5)
        )
    }
}
