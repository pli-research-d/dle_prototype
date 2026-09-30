package com.example.dle_prototype.data.ml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.max
import kotlin.random.Random

data class ModelWeights(
    val w1: Array<FloatArray>, // [hiddenSize][inputSize]
    val b1: FloatArray,        // [hiddenSize]
    val w2: Array<FloatArray>, // [outputSize][hiddenSize]
    val b2: FloatArray,        // [outputSize]
    val version: Int = 1,
    val trainedEpochs: Int = 0,
    val finalLoss: Float = 0f,
    val updatedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ModelWeights
        return version == other.version && trainedEpochs == other.trainedEpochs
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + trainedEpochs
        return result
    }

    fun toJson(): String {
        val sb = StringBuilder()
        sb.append("{")
        sb.append("\"version\":$version,")
        sb.append("\"trainedEpochs\":$trainedEpochs,")
        sb.append("\"finalLoss\":$finalLoss,")
        sb.append("\"updatedAt\":$updatedAt,")
        sb.append("\"w1\":[")
        for (i in w1.indices) {
            sb.append("[")
            sb.append(w1[i].joinToString(","))
            sb.append("]")
            if (i < w1.size - 1) sb.append(",")
        }
        sb.append("],")
        sb.append("\"b1\":[")
        sb.append(b1.joinToString(","))
        sb.append("],")
        sb.append("\"w2\":[")
        for (i in w2.indices) {
            sb.append("[")
            sb.append(w2[i].joinToString(","))
            sb.append("]")
            if (i < w2.size - 1) sb.append(",")
        }
        sb.append("],")
        sb.append("\"b2\":[")
        sb.append(b2.joinToString(","))
        sb.append("]")
        sb.append("}")
        return sb.toString()
    }

    fun toByteArray(): ByteArray {
        val jsonBytes = toJson().toByteArray(Charsets.UTF_8)
        val buffer = ByteBuffer.allocate(4 + jsonBytes.size)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(jsonBytes.size)
        buffer.put(jsonBytes)
        return buffer.array()
    }

    companion object {
        fun fromJson(jsonStr: String): ModelWeights {
            fun parseJsonArrayOfFloats(str: String): FloatArray {
                val list = mutableListOf<Float>()
                val matches = Regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").findAll(str)
                for (m in matches) {
                    m.value.toFloatOrNull()?.let { list.add(it) }
                }
                return list.toFloatArray()
            }

            fun parseJson2DArrayOfFloats(str: String): Array<FloatArray> {
                val list = mutableListOf<FloatArray>()
                val rowMatches = Regex("\\[([^\\[\\]]*)\\]").findAll(str)
                for (rm in rowMatches) {
                    val row = parseJsonArrayOfFloats(rm.groupValues[1])
                    if (row.isNotEmpty()) {
                        list.add(row)
                    }
                }
                return list.toTypedArray()
            }

            fun extractValue(key: String, default: String): String {
                val pattern = Regex("\"$key\"\\s*:\\s*([^,}\\]]+)")
                return pattern.find(jsonStr)?.groupValues?.get(1)?.trim() ?: default
            }

            val version = extractValue("version", "1").toIntOrNull() ?: 1
            val trainedEpochs = extractValue("trainedEpochs", "0").toIntOrNull() ?: 0
            val finalLoss = extractValue("finalLoss", "0").toFloatOrNull() ?: 0f
            val updatedAt = extractValue("updatedAt", "0").toLongOrNull() ?: System.currentTimeMillis()

            val w1Pattern = Regex("\"w1\"\\s*:\\s*(\\[\\[.*?\\]\\])")
            val w1Match = w1Pattern.find(jsonStr)?.groupValues?.get(1) ?: "[]"
            val w1 = parseJson2DArrayOfFloats(w1Match)

            val b1Pattern = Regex("\"b1\"\\s*:\\s*(\\[[^\\[\\]]*?\\])")
            val b1Match = b1Pattern.find(jsonStr)?.groupValues?.get(1) ?: "[]"
            val b1 = parseJsonArrayOfFloats(b1Match)

            val w2Pattern = Regex("\"w2\"\\s*:\\s*(\\[\\[.*?\\]\\])")
            val w2Match = w2Pattern.find(jsonStr)?.groupValues?.get(1) ?: "[]"
            val w2 = parseJson2DArrayOfFloats(w2Match)

            val b2Pattern = Regex("\"b2\"\\s*:\\s*(\\[[^\\[\\]]*?\\])")
            val b2Match = b2Pattern.find(jsonStr)?.groupValues?.get(1) ?: "[]"
            val b2 = parseJsonArrayOfFloats(b2Match)

            return ModelWeights(w1, b1, w2, b2, version, trainedEpochs, finalLoss, updatedAt)
        }

        fun defaultInit(inputDim: Int = 5, hiddenDim: Int = 8, outputDim: Int = 4): ModelWeights {
            val rng = Random(42)
            val scale1 = kotlin.math.sqrt(2.0f / inputDim.toFloat())
            val w1 = Array(hiddenDim) {
                FloatArray(inputDim) { (rng.nextFloat() * 2f - 1f) * scale1 }
            }
            val b1 = FloatArray(hiddenDim) { 0.05f }

            val scale2 = kotlin.math.sqrt(2.0f / hiddenDim.toFloat())
            val w2 = Array(outputDim) {
                FloatArray(hiddenDim) { (rng.nextFloat() * 2f - 1f) * scale2 }
            }
            val b2 = FloatArray(outputDim) { 0.0f }

            return ModelWeights(w1, b1, w2, b2, version = 1, trainedEpochs = 0, finalLoss = 0f)
        }
    }
}

data class TrainingProgress(
    val epoch: Int,
    val totalEpochs: Int,
    val loss: Float,
    val isComplete: Boolean = false
)

data class TrainingCheckpoint(
    val username: String,
    val sessionId: String,
    val currentEpoch: Int,
    val targetEpochs: Int,
    val currentLoss: Float,
    val lossHistory: List<Float>,
    val weights: ModelWeights,
    val savedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    fun lossHistoryToJson(): String {
        return lossHistory.joinToString(separator = ",", prefix = "[", postfix = "]")
    }

    companion object {
        fun parseLossHistory(json: String): List<Float> {
            val list = mutableListOf<Float>()
            val matches = Regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?").findAll(json)
            for (m in matches) {
                m.value.toFloatOrNull()?.let { list.add(it) }
            }
            return list
        }
    }
}

data class TrainingResult(
    val initialLoss: Float,
    val finalLoss: Float,
    val lossHistory: List<Float>,
    val lossReductionPercent: Float,
    val weights: ModelWeights,
    val sampleCount: Int
)

data class TrainingSample(
    val inputs: FloatArray, // 5 features (normalized)
    val targets: FloatArray  // 4 target traits: [conscientiousness, motivation, understanding, engagement]
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TrainingSample
        return inputs.contentEquals(other.inputs) && targets.contentEquals(other.targets)
    }

    override fun hashCode(): Int {
        var result = inputs.contentHashCode()
        result = 31 * result + targets.contentHashCode()
        return result
    }
}

class OnDeviceTrainableModel(
    val inputDim: Int = 5,
    val hiddenDim: Int = 8,
    val outputDim: Int = 4,
    initialWeights: ModelWeights? = null
) {
    companion object {
        val MEANS = floatArrayOf(4.2f, 8.0f, 45.7f, 2.0f, 3.8f)
        val STDS = floatArrayOf(2.0f, 4.1f, 28.5f, 0.8f, 1.9f)

        fun normalize(rawInput: FloatArray): FloatArray {
            val out = FloatArray(rawInput.size)
            for (i in rawInput.indices) {
                val m = if (i < MEANS.size) MEANS[i] else 0f
                val s = if (i < STDS.size && STDS[i] > 0f) STDS[i] else 1f
                val v = if (rawInput[i].isNaN() || rawInput[i].isInfinite()) 0f else rawInput[i]
                out[i] = (v - m) / s
            }
            return out
        }
    }

    var weights: ModelWeights = initialWeights ?: ModelWeights.defaultInit(inputDim, hiddenDim, outputDim)
        private set

    fun updateWeights(newWeights: ModelWeights) {
        this.weights = newWeights
    }

    private fun sigmoid(x: Float): Float {
        return 1.0f / (1.0f + exp(-x.coerceIn(-15f, 15f)))
    }

    private fun relu(x: Float): Float = max(0f, x)

    /**
     * Forward pass through the network:
     * h = ReLU(W1 * x + b1)
     * y = Sigmoid(W2 * h + b2)
     */
    fun forward(x: FloatArray): FloatArray {
        val h = FloatArray(hiddenDim)
        for (i in 0 until hiddenDim) {
            var sum = weights.b1[i]
            for (j in 0 until inputDim) {
                sum += weights.w1[i][j] * x[j]
            }
            h[i] = relu(sum)
        }

        val y = FloatArray(outputDim)
        for (k in 0 until outputDim) {
            var sum = weights.b2[k]
            for (i in 0 until hiddenDim) {
                sum += weights.w2[k][i] * h[i]
            }
            y[k] = sigmoid(sum)
        }
        return y
    }

    /**
     * Compute Mean Squared Error (MSE) loss across a dataset.
     */
    fun computeLoss(dataset: List<TrainingSample>): Float {
        if (dataset.isEmpty()) return 0f
        var totalLoss = 0f
        for (sample in dataset) {
            val pred = forward(sample.inputs)
            for (k in 0 until outputDim) {
                val diff = pred[k] - sample.targets[k]
                totalLoss += diff * diff
            }
        }
        return totalLoss / (dataset.size * outputDim)
    }

    /**
     * Train on-device using Gradient Descent with Momentum and periodic checkpointing.
     */
    suspend fun train(
        dataset: List<TrainingSample>,
        epochs: Int = 40,
        startEpoch: Int = 1,
        initialLossHistory: List<Float> = emptyList(),
        learningRate: Float = 0.08f,
        momentum: Float = 0.9f,
        checkpointInterval: Int = 3,
        onCheckpoint: (suspend (epoch: Int, totalEpochs: Int, currentLoss: Float, currentWeights: ModelWeights, lossHistory: List<Float>) -> Unit)? = null,
        onProgress: (TrainingProgress) -> Unit = {}
    ): TrainingResult = withContext(Dispatchers.Default) {
        if (dataset.isEmpty()) {
            return@withContext TrainingResult(0f, 0f, emptyList(), 0f, weights, 0)
        }

        val initialLoss = if (initialLossHistory.isNotEmpty()) initialLossHistory.first() else computeLoss(dataset)
        val lossHistory = initialLossHistory.toMutableList()

        // Momentum velocity buffers
        val vW1 = Array(hiddenDim) { FloatArray(inputDim) }
        val vb1 = FloatArray(hiddenDim)
        val vW2 = Array(outputDim) { FloatArray(hiddenDim) }
        val vb2 = FloatArray(outputDim)

        val currentW1 = Array(hiddenDim) { i -> weights.w1[i].clone() }
        val currentB1 = weights.b1.clone()
        val currentW2 = Array(outputDim) { k -> weights.w2[k].clone() }
        val currentB2 = weights.b2.clone()

        for (epoch in startEpoch..epochs) {
            val gradW1 = Array(hiddenDim) { FloatArray(inputDim) }
            val gradB1 = FloatArray(hiddenDim)
            val gradW2 = Array(outputDim) { FloatArray(hiddenDim) }
            val gradB2 = FloatArray(outputDim)

            for (sample in dataset) {
                val x = sample.inputs
                val t = sample.targets

                // 1. Forward Pass with saved activations
                val z1 = FloatArray(hiddenDim)
                val h = FloatArray(hiddenDim)
                for (i in 0 until hiddenDim) {
                    var sum = currentB1[i]
                    for (j in 0 until inputDim) {
                        sum += currentW1[i][j] * x[j]
                    }
                    z1[i] = sum
                    h[i] = relu(sum)
                }

                val z2 = FloatArray(outputDim)
                val y = FloatArray(outputDim)
                for (k in 0 until outputDim) {
                    var sum = currentB2[k]
                    for (i in 0 until hiddenDim) {
                        sum += currentW2[k][i] * h[i]
                    }
                    z2[k] = sum
                    y[k] = sigmoid(sum)
                }

                // 2. Backward Pass (Analytical Gradients)
                val dZ2 = FloatArray(outputDim)
                for (k in 0 until outputDim) {
                    val dLoss = 2.0f * (y[k] - t[k])
                    val dSigmoid = y[k] * (1.0f - y[k])
                    dZ2[k] = dLoss * dSigmoid

                    gradB2[k] += dZ2[k]
                    for (i in 0 until hiddenDim) {
                        gradW2[k][i] += dZ2[k] * h[i]
                    }
                }

                val dH = FloatArray(hiddenDim)
                for (i in 0 until hiddenDim) {
                    var sum = 0f
                    for (k in 0 until outputDim) {
                        sum += currentW2[k][i] * dZ2[k]
                    }
                    dH[i] = sum
                }

                for (i in 0 until hiddenDim) {
                    val dRelu = if (z1[i] > 0f) 1.0f else 0.0f
                    val dZ1 = dH[i] * dRelu

                    gradB1[i] += dZ1
                    for (j in 0 until inputDim) {
                        gradW1[i][j] += dZ1 * x[j]
                    }
                }
            }

            // 3. Update parameters with Momentum
            val n = dataset.size.toFloat()
            for (k in 0 until outputDim) {
                vb2[k] = momentum * vb2[k] + (learningRate * (gradB2[k] / n))
                currentB2[k] -= vb2[k]
                for (i in 0 until hiddenDim) {
                    vW2[k][i] = momentum * vW2[k][i] + (learningRate * (gradW2[k][i] / n))
                    currentW2[k][i] -= vW2[k][i]
                }
            }

            for (i in 0 until hiddenDim) {
                vb1[i] = momentum * vb1[i] + (learningRate * (gradB1[i] / n))
                currentB1[i] -= vb1[i]
                for (j in 0 until inputDim) {
                    vW1[i][j] = momentum * vW1[i][j] + (learningRate * (gradW1[i][j] / n))
                    currentW1[i][j] -= vW1[i][j]
                }
            }

            weights = ModelWeights(currentW1, currentB1, currentW2, currentB2, version = weights.version + 1, trainedEpochs = epoch)
            val currentLoss = computeLoss(dataset)
            lossHistory.add(currentLoss)

            onProgress(TrainingProgress(epoch, epochs, currentLoss, epoch == epochs))

            if (epoch % checkpointInterval == 0 || epoch == epochs) {
                onCheckpoint?.invoke(epoch, epochs, currentLoss, weights, lossHistory.toList())
            }
        }

        val finalLoss = lossHistory.lastOrNull() ?: initialLoss
        val reduction = if (initialLoss > 0.0001f) {
            ((initialLoss - finalLoss) / initialLoss * 100f).coerceIn(0f, 100f)
        } else 0f

        val finalWeights = ModelWeights(
            currentW1, currentB1, currentW2, currentB2,
            version = weights.version,
            trainedEpochs = epochs,
            finalLoss = finalLoss,
            updatedAt = System.currentTimeMillis()
        )
        weights = finalWeights

        TrainingResult(
            initialLoss = initialLoss,
            finalLoss = finalLoss,
            lossHistory = lossHistory,
            lossReductionPercent = reduction,
            weights = finalWeights,
            sampleCount = dataset.size
        )
    }

    /**
     * Federated Learning Weight Averaging (FedAvg):
     * Computes the element-wise average of multiple client model weights.
     */
    fun federatedAverage(otherWeights: List<ModelWeights>): ModelWeights {
        val all = listOf(weights) + otherWeights
        val count = all.size.toFloat()

        val avgW1 = Array(hiddenDim) { i ->
            FloatArray(inputDim) { j ->
                all.sumOf { it.w1[i][j].toDouble() }.toFloat() / count
            }
        }
        val avgB1 = FloatArray(hiddenDim) { i ->
            all.sumOf { it.b1[i].toDouble() }.toFloat() / count
        }
        val avgW2 = Array(outputDim) { k ->
            FloatArray(hiddenDim) { i ->
                all.sumOf { it.w2[k][i].toDouble() }.toFloat() / count
            }
        }
        val avgB2 = FloatArray(outputDim) { k ->
            all.sumOf { it.b2[k].toDouble() }.toFloat() / count
        }

        return ModelWeights(
            w1 = avgW1,
            b1 = avgB1,
            w2 = avgW2,
            b2 = avgB2,
            version = weights.version + 1,
            trainedEpochs = weights.trainedEpochs,
            finalLoss = weights.finalLoss,
            updatedAt = System.currentTimeMillis()
        )
    }
}
