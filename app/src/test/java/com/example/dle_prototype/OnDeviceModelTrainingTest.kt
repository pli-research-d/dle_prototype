package com.example.dle_prototype

import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.OnDeviceTrainableModel
import com.example.dle_prototype.data.ml.TrainingSample
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnDeviceModelTrainingTest {

    @Test
    fun testForwardPassYieldsValidProbabilities() {
        val model = OnDeviceTrainableModel(inputDim = 5, hiddenDim = 8, outputDim = 4)
        val sampleInput = OnDeviceTrainableModel.normalize(floatArrayOf(4.0f, 15.0f, 80.0f, 2.0f, 1.0f))

        val output = model.forward(sampleInput)

        assertEquals("Output dimension should be 4", 4, output.size)
        for (i in output.indices) {
            assertTrue("Trait output at index $i should be >= 0.0, got ${output[i]}", output[i] >= 0.0f)
            assertTrue("Trait output at index $i should be <= 1.0, got ${output[i]}", output[i] <= 1.0f)
        }
    }

    @Test
    fun testBackpropagationReducesLoss() = runBlocking {
        val model = OnDeviceTrainableModel(inputDim = 5, hiddenDim = 8, outputDim = 4)

        // Synthetic calibration dataset with normalized inputs
        val dataset = listOf(
            TrainingSample(
                inputs = OnDeviceTrainableModel.normalize(floatArrayOf(2.0f, 5.0f, 40.0f, 1.0f, 1.0f)),
                targets = floatArrayOf(0.35f, 0.40f, 0.38f, 0.30f)
            ),
            TrainingSample(
                inputs = OnDeviceTrainableModel.normalize(floatArrayOf(4.0f, 15.0f, 75.0f, 2.0f, 2.0f)),
                targets = floatArrayOf(0.65f, 0.70f, 0.72f, 0.65f)
            ),
            TrainingSample(
                inputs = OnDeviceTrainableModel.normalize(floatArrayOf(8.0f, 45.0f, 95.0f, 3.0f, 3.0f)),
                targets = floatArrayOf(0.90f, 0.92f, 0.95f, 0.90f)
            ),
            TrainingSample(
                inputs = OnDeviceTrainableModel.normalize(floatArrayOf(1.0f, 2.0f, 20.0f, 1.0f, 1.0f)),
                targets = floatArrayOf(0.25f, 0.30f, 0.22f, 0.20f)
            )
        )

        val initialLoss = model.computeLoss(dataset)
        assertTrue("Initial loss should be greater than zero", initialLoss > 0.0f)

        val result = model.train(
            dataset = dataset,
            epochs = 50,
            learningRate = 0.1f,
            momentum = 0.9f
        )

        assertTrue(
            "Final loss (${result.finalLoss}) must be strictly less than initial loss (${result.initialLoss})",
            result.finalLoss < result.initialLoss
        )
        assertTrue(
            "Loss reduction percentage should be > 20%, got ${result.lossReductionPercent}%",
            result.lossReductionPercent > 20f
        )
        assertEquals("History size should match epochs", 50, result.lossHistory.size)
    }

    @Test
    fun testModelWeightsSerializationDeserialization() {
        val defaultWeights = ModelWeights.defaultInit(inputDim = 5, hiddenDim = 8, outputDim = 4)
        val jsonStr = defaultWeights.toJson()
        assertNotNull(jsonStr)
        assertTrue(jsonStr.contains("w1"))
        assertTrue(jsonStr.contains("b1"))
        assertTrue(jsonStr.contains("w2"))
        assertTrue(jsonStr.contains("b2"))

        val restored = ModelWeights.fromJson(jsonStr)

        assertEquals(defaultWeights.version, restored.version)
        assertEquals(defaultWeights.w1.size, restored.w1.size)
        assertEquals(defaultWeights.b1.size, restored.b1.size)
        assertEquals(defaultWeights.w2.size, restored.w2.size)
        assertEquals(defaultWeights.b2.size, restored.b2.size)

        for (i in defaultWeights.w1.indices) {
            for (j in defaultWeights.w1[i].indices) {
                assertEquals(defaultWeights.w1[i][j], restored.w1[i][j], 0.0001f)
            }
        }
    }

    @Test
    fun testFederatedAveragingConvergence() = runBlocking {
        val client1 = OnDeviceTrainableModel(5, 8, 4)
        val client2 = OnDeviceTrainableModel(5, 8, 4)

        val dataset1 = listOf(
            TrainingSample(
                OnDeviceTrainableModel.normalize(floatArrayOf(1f, 2f, 30f, 1f, 1f)),
                floatArrayOf(0.3f, 0.3f, 0.3f, 0.3f)
            )
        )
        val dataset2 = listOf(
            TrainingSample(
                OnDeviceTrainableModel.normalize(floatArrayOf(10f, 50f, 95f, 3f, 6f)),
                floatArrayOf(0.9f, 0.9f, 0.9f, 0.9f)
            )
        )

        client1.train(dataset1, epochs = 20, learningRate = 0.08f)
        client2.train(dataset2, epochs = 20, learningRate = 0.08f)

        val federatedWeights = client1.federatedAverage(listOf(client2.weights))
        assertNotNull(federatedWeights)

        val globalModel = OnDeviceTrainableModel(5, 8, 4, initialWeights = federatedWeights)
        val pred = globalModel.forward(OnDeviceTrainableModel.normalize(floatArrayOf(5f, 25f, 60f, 2f, 3f)))

        assertEquals(4, pred.size)
        for (v in pred) {
            assertTrue("Federated prediction should be between 0 and 1, got $v", v in 0.0f..1.0f)
        }
    }

    @Test
    fun testTrainingCheckpointSerialization() {
        val sampleLossHistory = listOf(0.185f, 0.142f, 0.098f, 0.065f, 0.042f)
        val dummyWeights = ModelWeights.defaultInit(5, 8, 4)
        val checkpoint = com.example.dle_prototype.data.ml.TrainingCheckpoint(
            username = "alice",
            sessionId = "sess_123",
            currentEpoch = 5,
            targetEpochs = 20,
            currentLoss = 0.042f,
            lossHistory = sampleLossHistory,
            weights = dummyWeights
        )

        val json = checkpoint.lossHistoryToJson()
        val parsed = com.example.dle_prototype.data.ml.TrainingCheckpoint.parseLossHistory(json)

        assertEquals("Loss history length should match", sampleLossHistory.size, parsed.size)
        for (i in sampleLossHistory.indices) {
            assertEquals("Loss value at index $i should match", sampleLossHistory[i], parsed[i], 0.0001f)
        }
    }

    @Test
    fun testPeriodicCheckpointCallbackTriggered() = runBlocking {
        val model = OnDeviceTrainableModel(inputDim = 5, hiddenDim = 8, outputDim = 4)
        val dataset = listOf(
            TrainingSample(
                OnDeviceTrainableModel.normalize(floatArrayOf(2.0f, 5.0f, 40.0f, 1.0f, 1.0f)),
                floatArrayOf(0.35f, 0.40f, 0.38f, 0.30f)
            )
        )

        val checkpointEpochs = mutableListOf<Int>()
        model.train(
            dataset = dataset,
            epochs = 9,
            checkpointInterval = 3,
            onCheckpoint = { ep, total, loss, w, hist ->
                checkpointEpochs.add(ep)
            }
        )

        // For 9 epochs with interval 3, checkpoints should trigger at epochs 3, 6, 9
        assertEquals("Checkpoints should be triggered 3 times", 3, checkpointEpochs.size)
        assertEquals(listOf(3, 6, 9), checkpointEpochs)
    }

    @Test
    fun testResumingTrainingFromCheckpointContinuesConvergence() = runBlocking {
        val dataset = listOf(
            TrainingSample(
                OnDeviceTrainableModel.normalize(floatArrayOf(3.0f, 10.0f, 60.0f, 2.0f, 2.0f)),
                floatArrayOf(0.55f, 0.60f, 0.62f, 0.58f)
            ),
            TrainingSample(
                OnDeviceTrainableModel.normalize(floatArrayOf(6.0f, 30.0f, 85.0f, 3.0f, 1.0f)),
                floatArrayOf(0.80f, 0.82f, 0.85f, 0.78f)
            )
        )

        val modelA = OnDeviceTrainableModel(inputDim = 5, hiddenDim = 8, outputDim = 4)
        var savedCheckpoint: com.example.dle_prototype.data.ml.TrainingCheckpoint? = null

        // Train Phase 1: 6 epochs with checkpointing at epoch 3 and 6
        modelA.train(
            dataset = dataset,
            epochs = 6,
            learningRate = 0.08f,
            checkpointInterval = 3,
            onCheckpoint = { ep, total, loss, w, hist ->
                if (ep == 6) {
                    savedCheckpoint = com.example.dle_prototype.data.ml.TrainingCheckpoint(
                        username = "bob",
                        sessionId = "sess_456",
                        currentEpoch = ep,
                        targetEpochs = 12,
                        currentLoss = loss,
                        lossHistory = hist,
                        weights = w
                    )
                }
            }
        )

        assertNotNull("Checkpoint at epoch 6 should be saved", savedCheckpoint)
        val cp = savedCheckpoint!!
        val lossAtCheckpoint = cp.currentLoss

        // Resume Phase 2: Start new model with checkpointed weights from epoch 7 to 12
        val modelB = OnDeviceTrainableModel(inputDim = 5, hiddenDim = 8, outputDim = 4, initialWeights = cp.weights)
        val result = modelB.train(
            dataset = dataset,
            epochs = 12,
            startEpoch = cp.currentEpoch + 1, // 7
            initialLossHistory = cp.lossHistory,
            learningRate = 0.08f
        )

        assertEquals("Resumed training should complete 12 total epochs in history", 12, result.lossHistory.size)
        assertTrue(
            "Final loss after resumed training (${result.finalLoss}) should be lower than checkpoint loss ($lossAtCheckpoint)",
            result.finalLoss <= lossAtCheckpoint
        )
    }
}
